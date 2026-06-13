package site.s9lab.s9labclient.client.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Util;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.mixin.client.MinecraftClientAccessor;

public final class AccountLoginHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
    }

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(12))
            .build();
    private static final String MICROSOFT_AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    private static final String MICROSOFT_TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String XBOX_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_ENTITLEMENTS_URL = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String DEFAULT_MICROSOFT_CLIENT_ID = "9409e8cc-cfab-4940-a600-d6f3482a2db1";
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:25585/callback";
    private static final int CALLBACK_TIMEOUT_MS = 120_000;
    private static final AtomicBoolean LOGIN_RUNNING = new AtomicBoolean();
    private static final Map<String, RuntimeCredentials> RUNTIME_CREDENTIALS = new ConcurrentHashMap<>();
    private static volatile String status = "Ready.";
    private static volatile String lastError = "";
    private static volatile ServerSocket callbackSocket;

    private AccountLoginHelper() {
    }

    public static String status() {
        return status;
    }

    public static boolean loginRunning() {
        return LOGIN_RUNNING.get();
    }

    public static String lastError() {
        return lastError;
    }

    public static void beginMicrosoftLogin() {
        if (!LOGIN_RUNNING.compareAndSet(false, true)) {
            status = "Login is already running.";
            return;
        }
        lastError = "";
        CompletableFuture.runAsync(() -> {
            try {
                runLoginFlow();
            } catch (AuthException exception) {
                lastError = exception.userMessage();
                status = "Login failed: " + exception.userMessage();
            } catch (Exception exception) {
                String message = safeBody(exception.getMessage());
                if (message.isBlank()) {
                    message = exception.getClass().getSimpleName();
                }
                lastError = "AUTH_UNEXPECTED: " + message;
                status = "Login failed: " + lastError;
                S9LabClient.LOGGER.warn("Microsoft browser login failed unexpectedly while '{}': {}", status, message, exception);
            } finally {
                closeCallbackSocket();
                LOGIN_RUNNING.set(false);
            }
        });
    }

    public static void cancelMicrosoftLogin() {
        if (!LOGIN_RUNNING.get()) {
            status = "No login is running.";
            return;
        }
        status = "Login cancelled.";
        closeCallbackSocket();
        LOGIN_RUNNING.set(false);
    }

    public static List<StoredAccount> loadAccounts() {
        Path path = accountsPath();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            AccountStore store = GSON.fromJson(reader, AccountStore.class);
            if (store == null || store.accounts == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(store.accounts);
        } catch (IOException | JsonSyntaxException exception) {
            status = "Account file is damaged. It was ignored.";
            return new ArrayList<>();
        }
    }

    public static boolean switchTo(StoredAccount account) {
        RuntimeCredentials credentials = RUNTIME_CREDENTIALS.get(normalizeUuid(account.uuid));
        if (credentials == null) {
            status = "Re-login required for " + account.username + ". Tokens are kept only in this runtime.";
            return false;
        }
        if (credentials.minecraftToken.expiresAt.isBefore(Instant.now().plusSeconds(60))) {
            status = "Token expired. Please login again.";
            return false;
        }
        applySession(account, credentials.minecraftToken.accessToken, credentials.xuid);
        status = "Switched to " + account.username + ".";
        return true;
    }

    public static void removeAccount(String uuid) {
        String normalized = normalizeUuid(uuid);
        RUNTIME_CREDENTIALS.remove(normalized);
        List<StoredAccount> accounts = loadAccounts().stream()
                .filter(account -> !normalizeUuid(account.uuid).equals(normalized))
                .toList();
        saveAccounts(accounts);
        status = "Account removed.";
    }

    public static StoredAccount currentLauncherAccount() {
        Session session = MinecraftClient.getInstance().getSession();
        String uuid = session.getUuidOrNull() == null ? "" : session.getUuidOrNull().toString();
        return new StoredAccount(session.getUsername(), uuid, "", "", false, true, "Launcher session");
    }

    private static void runLoginFlow() throws Exception {
        AuthConfig config = AuthConfig.load();
        config.validate();

        status = "Starting callback server...";
        try (ServerSocket server = new ServerSocket(config.redirectPort, 1)) {
            callbackSocket = server;
            server.setSoTimeout(CALLBACK_TIMEOUT_MS);

            String verifier = secureUrlToken(64);
            String challenge = pkceChallenge(verifier);
            String state = secureUrlToken(32);
            URI loginUri = loginUri(config, challenge, state);

            status = "Opening Microsoft login in your browser...";
            try {
                Util.getOperatingSystem().open(loginUri);
            } catch (RuntimeException exception) {
                throw new AuthException("AUTH_BROWSER_OPEN_FAILED", "Could not open the browser automatically: " + safeBody(exception.getMessage()));
            }

            status = "Waiting for Microsoft login...";
            CallbackResult callback = waitForCallback(server);
            if (callback.error != null && !callback.error.isBlank()) {
                throw new AuthException("AUTH_MICROSOFT_CALLBACK", "Microsoft returned: " + callback.error);
            }
            if (callback.code == null || callback.code.isBlank()) {
                throw new AuthException("AUTH_CALLBACK_MISSING_CODE", "Microsoft callback did not contain a code.");
            }
            if (!state.equals(callback.state)) {
                throw new AuthException("AUTH_STATE_MISMATCH", "Login state mismatch. Please retry.");
            }

            status = "Exchanging Microsoft code...";
            MicrosoftToken microsoft = exchangeMicrosoftCode(config, callback.code, verifier);
            status = "Authenticating Xbox Live...";
            XboxToken xbox = authenticateXbox(microsoft.accessToken);
            status = "Authorizing XSTS...";
            XstsToken xsts = authorizeXsts(xbox.token);
            status = "Authenticating Minecraft...";
            MinecraftToken minecraft = authenticateMinecraft(xsts);
            status = "Checking Minecraft ownership...";
            ensureMinecraftOwned(minecraft.accessToken);
            status = "Loading Minecraft profile...";
            MinecraftProfile profile = loadProfile(minecraft.accessToken);

            StoredAccount account = new StoredAccount(
                    profile.name,
                    profile.uuid.toString(),
                    profile.skinUrl,
                    profile.capeUrl,
                    false,
                    false,
                    "Logged in this session"
            );
            RUNTIME_CREDENTIALS.put(normalizeUuid(account.uuid), new RuntimeCredentials(microsoft, xbox, xsts, minecraft, xsts.xuid));
            upsertAccount(account);
            applySession(account, minecraft.accessToken, xsts.xuid);
            status = "Logged in as " + account.username + ".";
        } catch (BindException exception) {
            throw new AuthException("AUTH_CALLBACK_PORT_BUSY", "Callback port is busy. Close the other login or change the redirect URI.");
        }
    }

    private static URI loginUri(AuthConfig config, String challenge, String state) {
        String query = "client_id=" + enc(config.clientId)
                + "&response_type=code"
                + "&redirect_uri=" + enc(config.redirectUri)
                + "&response_mode=query"
                + "&scope=" + enc("XboxLive.signin offline_access")
                + "&code_challenge=" + enc(challenge)
                + "&code_challenge_method=S256"
                + "&state=" + enc(state);
        return URI.create(MICROSOFT_AUTH_URL + "?" + query);
    }

    private static CallbackResult waitForCallback(ServerSocket server) throws IOException, AuthException {
        try (Socket socket = server.accept()) {
            socket.setSoTimeout(5000);
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET ")) {
                writeCallbackResponse(socket, false, "Invalid S9Lab login callback.");
                throw new AuthException("AUTH_CALLBACK_INVALID", "Invalid callback request.");
            }
            while (true) {
                String line = reader.readLine();
                if (line == null || line.isBlank()) {
                    break;
                }
            }
            String target = requestLine.split(" ")[1];
            Map<String, String> params = parseQuery(target);
            boolean ok = params.containsKey("code") && !params.containsKey("error");
            writeCallbackResponse(socket, ok, ok
                    ? "S9Lab Login erfolgreich. Du kannst dieses Fenster schließen."
                    : "S9Lab Login fehlgeschlagen. Du kannst dieses Fenster schließen.");
            return new CallbackResult(params.get("code"), params.get("state"), params.get("error"));
        } catch (java.net.SocketTimeoutException exception) {
            throw new AuthException("AUTH_CALLBACK_TIMEOUT", "Microsoft login timed out.");
        } catch (SocketException exception) {
            if (!LOGIN_RUNNING.get()) {
                throw new AuthException("AUTH_CANCELLED", "Login cancelled.");
            }
            throw exception;
        }
    }

    private static void writeCallbackResponse(Socket socket, boolean ok, String message) throws IOException {
        String escaped = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String body = "<!doctype html><html><head><meta charset=\"utf-8\"><title>S9Lab Login</title>"
                + "<style>body{font-family:Arial;background:#0d1118;color:#fff;display:grid;place-items:center;height:100vh;margin:0}"
                + "main{border:1px solid #334; padding:32px; background:#151b26} h1{color:" + (ok ? "#49f26f" : "#ff6b6b") + "}</style>"
                + "</head><body><main><h1>" + (ok ? "Login erfolgreich" : "Login fehlgeschlagen") + "</h1><p>" + escaped + "</p></main></body></html>";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + bytes.length + "\r\nConnection: close\r\n\r\n";
        socket.getOutputStream().write(header.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().write(bytes);
    }

    private static MicrosoftToken exchangeMicrosoftCode(AuthConfig config, String code, String verifier) throws Exception {
        String body = form(Map.of(
                "client_id", config.clientId,
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", config.redirectUri,
                "code_verifier", verifier
        ));
        JsonObject json = postForm(MICROSOFT_TOKEN_URL, body, "AUTH_MICROSOFT_FAILED");
        String accessToken = requireString(json, "access_token", "AUTH_MICROSOFT_FAILED");
        String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : "";
        long expires = json.has("expires_in") ? json.get("expires_in").getAsLong() : 3600L;
        return new MicrosoftToken(accessToken, refreshToken, Instant.now().plusSeconds(expires));
    }

    private static XboxToken authenticateXbox(String microsoftAccessToken) throws Exception {
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d=" + microsoftAccessToken);
        JsonObject request = new JsonObject();
        request.add("Properties", properties);
        request.addProperty("RelyingParty", "http://auth.xboxlive.com");
        request.addProperty("TokenType", "JWT");
        JsonObject json = postJson(XBOX_AUTH_URL, request, "", "AUTH_XBOX_FAILED");
        String token = requireString(json, "Token", "AUTH_XBOX_FAILED");
        String uhs = extractUhs(json, "AUTH_XBOX_FAILED");
        return new XboxToken(token, uhs, expiresAt(json));
    }

    private static XstsToken authorizeXsts(String xboxToken) throws Exception {
        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        JsonArray userTokens = new JsonArray();
        userTokens.add(xboxToken);
        properties.add("UserTokens", userTokens);
        JsonObject request = new JsonObject();
        request.add("Properties", properties);
        request.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        request.addProperty("TokenType", "JWT");
        JsonObject json = postJson(XSTS_AUTH_URL, request, "", "AUTH_XSTS_FAILED");
        String token = requireString(json, "Token", "AUTH_XSTS_FAILED");
        String uhs = extractUhs(json, "AUTH_XSTS_FAILED");
        String xuid = extractXuid(json);
        return new XstsToken(token, uhs, xuid, expiresAt(json));
    }

    private static MinecraftToken authenticateMinecraft(XstsToken xsts) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("identityToken", "XBL3.0 x=" + xsts.uhs + ";" + xsts.token);
        JsonObject json = postJson(MC_LOGIN_URL, request, "", "AUTH_MINECRAFT_FAILED");
        String accessToken = requireString(json, "access_token", "AUTH_MINECRAFT_FAILED");
        long expires = json.has("expires_in") ? json.get("expires_in").getAsLong() : 3600L;
        return new MinecraftToken(accessToken, Instant.now().plusSeconds(expires));
    }

    private static void ensureMinecraftOwned(String minecraftToken) throws Exception {
        JsonObject json = getJson(MC_ENTITLEMENTS_URL, minecraftToken, "AUTH_MINECRAFT_ENTITLEMENTS_FAILED");
        JsonArray items = json.has("items") && json.get("items").isJsonArray() ? json.getAsJsonArray("items") : new JsonArray();
        boolean owned = false;
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.has("name") ? item.get("name").getAsString().toLowerCase(Locale.ROOT) : "";
            owned |= name.equals("game_minecraft") || name.equals("product_minecraft") || name.contains("minecraft");
        }
        if (!owned) {
            throw new AuthException("AUTH_MINECRAFT_NOT_OWNED", "This Microsoft account does not own Minecraft: Java Edition.");
        }
    }

    private static MinecraftProfile loadProfile(String minecraftToken) throws Exception {
        JsonObject json = getJson(MC_PROFILE_URL, minecraftToken, "AUTH_MINECRAFT_PROFILE_FAILED");
        String id = requireString(json, "id", "AUTH_MINECRAFT_PROFILE_FAILED");
        String name = requireString(json, "name", "AUTH_MINECRAFT_PROFILE_FAILED");
        String skin = "";
        String cape = "";
        if (json.has("skins") && json.get("skins").isJsonArray()) {
            JsonArray skins = json.getAsJsonArray("skins");
            if (!skins.isEmpty() && skins.get(0).isJsonObject()) {
                JsonObject skinJson = skins.get(0).getAsJsonObject();
                skin = skinJson.has("url") ? skinJson.get("url").getAsString() : "";
            }
        }
        if (json.has("capes") && json.get("capes").isJsonArray()) {
            JsonArray capes = json.getAsJsonArray("capes");
            if (!capes.isEmpty() && capes.get(0).isJsonObject()) {
                JsonObject capeJson = capes.get(0).getAsJsonObject();
                cape = capeJson.has("url") ? capeJson.get("url").getAsString() : "";
            }
        }
        return new MinecraftProfile(uuidFromUndashed(id), name, skin, cape);
    }

    private static JsonObject postForm(String url, String body, String code) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return sendJson(request, code);
    }

    private static JsonObject postJson(String url, JsonObject body, String bearer, String code) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        if (!bearer.isBlank()) {
            builder.header("Authorization", "Bearer " + bearer);
        }
        return sendJson(builder.build(), code);
    }

    private static JsonObject getJson(String url, String bearer, String code) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + bearer)
                .header("Accept", "application/json")
                .GET()
                .build();
        return sendJson(request, code);
    }

    private static JsonObject sendJson(HttpRequest request, String code) throws Exception {
        HttpResponse<String> response;
        try {
            response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            String message = safeBody(exception.getMessage());
            if (message.toLowerCase(Locale.ROOT).contains("permission denied")
                    || message.toLowerCase(Locale.ROOT).contains("getsockopt")) {
                throw new AuthException("AUTH_NETWORK_BLOCKED",
                        "Windows, firewall, antivirus or VPN blocked Java from connecting to " + request.uri().getHost()
                                + ". Allow java.exe/javaw.exe through the firewall and retry. " + message);
            }
            throw new AuthException("AUTH_NETWORK_FAILED",
                    "Could not connect to " + request.uri().getHost() + ". Check internet, proxy/VPN and firewall. " + message);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = safeBody(response.body());
            String message = switch (response.statusCode()) {
                case 401 -> "Unauthorized at " + request.uri().getHost() + ".";
                case 403 -> body.toLowerCase(Locale.ROOT).contains("invalid app registration")
                        ? "Minecraft Services rejected this Microsoft app registration. Check your own Client-ID and Azure setup."
                        : "Forbidden at " + request.uri().getHost() + ".";
                case 404 -> "Minecraft profile was not found. Create a Java profile first.";
                case 429 -> "Rate limited. Please wait and retry.";
                default -> "HTTP " + response.statusCode() + " at " + request.uri().getHost() + ".";
            };
            throw new AuthException(code, message + " " + body);
        }
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new AuthException(code, "Invalid JSON response from " + request.uri().getHost() + ".");
        }
    }

    private static void applySession(StoredAccount account, String accessToken, String xuid) {
        UUID uuid = UUID.fromString(account.uuid);
        Session session = new Session(account.username, uuid, accessToken, optional(xuid), Optional.empty());
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> ((MinecraftClientAccessor) client).s9labclient$setSession(session));
    }

    private static void upsertAccount(StoredAccount account) {
        String uuid = normalizeUuid(account.uuid);
        List<StoredAccount> accounts = new ArrayList<>(loadAccounts());
        accounts.removeIf(existing -> normalizeUuid(existing.uuid).equals(uuid));
        accounts.add(0, account);
        saveAccounts(accounts);
    }

    private static void saveAccounts(List<StoredAccount> accounts) {
        try {
            Path path = accountsPath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                AccountStore store = new AccountStore();
                store.accounts = new ArrayList<>(accounts);
                GSON.toJson(store, writer);
            }
        } catch (IOException exception) {
            status = "Could not save account metadata.";
        }
    }

    private static Path accountsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(S9LabClient.MOD_ID + "-accounts.json");
    }

    private static void closeCallbackSocket() {
        ServerSocket socket = callbackSocket;
        callbackSocket = null;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String pkceChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
    }

    private static String secureUrlToken(int bytes) {
        byte[] data = new byte[bytes];
        RANDOM.nextBytes(data);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static Map<String, String> parseQuery(String target) {
        int queryIndex = target.indexOf('?');
        if (queryIndex < 0 || queryIndex == target.length() - 1) {
            return Map.of();
        }
        Map<String, String> params = new HashMap<>();
        String[] pairs = target.substring(queryIndex + 1).split("&");
        for (String pair : pairs) {
            int equals = pair.indexOf('=');
            String key = equals < 0 ? pair : pair.substring(0, equals);
            String value = equals < 0 ? "" : pair.substring(equals + 1);
            params.put(urlDecode(key), urlDecode(value));
        }
        return params;
    }

    private static String form(Map<String, String> values) {
        StringBuilder builder = new StringBuilder();
        values.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(enc(key)).append('=').append(enc(value));
        });
        return builder.toString();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String requireString(JsonObject json, String key, String code) throws AuthException {
        if (!json.has(key) || json.get(key).isJsonNull() || json.get(key).getAsString().isBlank()) {
            throw new AuthException(code, "Missing '" + key + "' in auth response.");
        }
        return json.get(key).getAsString();
    }

    private static String extractUhs(JsonObject json, String code) throws AuthException {
        JsonArray xui = json.getAsJsonObject("DisplayClaims").getAsJsonArray("xui");
        if (xui == null || xui.isEmpty() || !xui.get(0).isJsonObject()) {
            throw new AuthException(code, "Missing Xbox user hash.");
        }
        JsonObject first = xui.get(0).getAsJsonObject();
        return requireString(first, "uhs", code);
    }

    private static String extractXuid(JsonObject json) {
        try {
            JsonArray xui = json.getAsJsonObject("DisplayClaims").getAsJsonArray("xui");
            if (xui != null && !xui.isEmpty()) {
                JsonObject first = xui.get(0).getAsJsonObject();
                if (first.has("xid")) {
                    return first.get("xid").getAsString();
                }
            }
        } catch (RuntimeException ignored) {
        }
        return "";
    }

    private static Instant expiresAt(JsonObject json) {
        if (json.has("NotAfter")) {
            try {
                return Instant.parse(json.get("NotAfter").getAsString());
            } catch (RuntimeException ignored) {
            }
        }
        return Instant.now().plusSeconds(3600);
    }

    private static String safeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String compact = body.replaceAll("\\s+", " ");
        compact = compact.replaceAll("(?i)(access_token|refresh_token|Token|identityToken)\"?\\s*[:=]\\s*\"?[A-Za-z0-9._\\-]+", "$1=REDACTED");
        return compact.length() > 260 ? compact.substring(0, 260) + "..." : compact;
    }

    private static UUID uuidFromUndashed(String id) throws AuthException {
        String hex = id.replace("-", "");
        if (hex.length() != 32) {
            throw new AuthException("AUTH_MINECRAFT_PROFILE_FAILED", "Minecraft profile UUID has invalid format.");
        }
        return UUID.fromString(hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20));
    }

    private static String normalizeUuid(String uuid) {
        return uuid == null ? "" : uuid.replace("-", "").toLowerCase(Locale.ROOT);
    }

    private static Optional<String> optional(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    public record StoredAccount(String username, String uuid, String skinUrl, String capeUrl, boolean reauthRequired, boolean launcherSession, String status) {
    }

    private record RuntimeCredentials(MicrosoftToken microsoftToken, XboxToken xboxToken, XstsToken xstsToken, MinecraftToken minecraftToken, String xuid) {
    }

    private record MicrosoftToken(String accessToken, String refreshToken, Instant expiresAt) {
    }

    private record XboxToken(String token, String uhs, Instant expiresAt) {
    }

    private record XstsToken(String token, String uhs, String xuid, Instant expiresAt) {
    }

    private record MinecraftToken(String accessToken, Instant expiresAt) {
    }

    private record MinecraftProfile(UUID uuid, String name, String skinUrl, String capeUrl) {
    }

    private record CallbackResult(String code, String state, String error) {
    }

    private static class AccountStore {
        List<StoredAccount> accounts = new ArrayList<>();
    }

    private record AuthConfig(String clientId, String redirectUri, int redirectPort) {
        private static AuthConfig load() throws AuthException {
            String clientId = firstPresent(
                    System.getProperty("s9lab.microsoftClientId"),
                    System.getenv("S9LAB_MICROSOFT_CLIENT_ID"),
                    System.getenv("MICROSOFT_CLIENT_ID"),
                    DEFAULT_MICROSOFT_CLIENT_ID
            );
            String redirectUri = firstPresent(System.getProperty("s9lab.microsoftRedirectUri"), System.getenv("S9LAB_MICROSOFT_REDIRECT_URI"), DEFAULT_REDIRECT_URI);
            URI uri;
            try {
                uri = URI.create(redirectUri);
            } catch (IllegalArgumentException exception) {
                throw new AuthException("AUTH_REDIRECT_INVALID", "Redirect URI is invalid.");
            }
            int port = uri.getPort();
            return new AuthConfig(clientId, redirectUri, port);
        }

        private void validate() throws AuthException {
            if (clientId == null || clientId.isBlank() || clientId.contains("YOUR") || clientId.contains("CHANGE_ME")) {
                throw new AuthException("AUTH_CONFIG_MISSING", "Microsoft login is not configured. Set S9LAB_MICROSOFT_CLIENT_ID.");
            }
            try {
                UUID.fromString(clientId);
            } catch (IllegalArgumentException exception) {
                throw new AuthException("AUTH_CONFIG_INVALID", "Microsoft Client-ID must be a valid UUID.");
            }
            URI uri;
            try {
                uri = URI.create(redirectUri);
            } catch (IllegalArgumentException exception) {
                throw new AuthException("AUTH_REDIRECT_INVALID", "Redirect URI is invalid.");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) || !"localhost".equalsIgnoreCase(uri.getHost()) || uri.getPort() <= 0 || !"/callback".equals(uri.getPath())) {
                throw new AuthException("AUTH_REDIRECT_INVALID", "Redirect URI must look like http://localhost:25585/callback.");
            }
        }

        private static String firstPresent(String... values) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
            return "";
        }
    }

    private static final class AuthException extends Exception {
        private final String code;
        private final String userMessage;

        private AuthException(String code, String userMessage) {
            super(code + ": " + userMessage);
            this.code = code;
            this.userMessage = code + ": " + userMessage;
        }

        private String userMessage() {
            return userMessage;
        }
    }
}
