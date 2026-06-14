package site.s9lab.s9labclient.client.backend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.foundation.model.UserProfile;
import site.s9lab.s9labclient.client.foundation.profile.ProfileCache;
import site.s9lab.s9labclient.client.foundation.session.S9UserSession;
import site.s9lab.s9labclient.client.notification.S9ToastManager;

public final class BackendClient {
    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST = new TypeToken<List<String>>() { }.getType();
    private static final Type STRING_MAP = new TypeToken<Map<String, String>>() { }.getType();
    private static final Type SETTINGS_MAP = new TypeToken<Map<String, Object>>() { }.getType();
    private static final Type CATALOG_LIST = new TypeToken<List<BackendState.ShopCosmetic>>() { }.getType();
    private static final Type NOTIFICATION_LIST = new TypeToken<List<BackendState.Notification>>() { }.getType();
    private static final ProfileCache PROFILE_CACHE = new ProfileCache();
    private static final AtomicBoolean RUNNING = new AtomicBoolean();
    private static ScheduledExecutorService executor;
    private static HttpClient httpClient;
    private static WebSocket webSocket;
    private static final AtomicBoolean WEB_SOCKET_CONNECTING = new AtomicBoolean();
    private static Instant startedAt = Instant.now();
    private static Instant lastHandshake = Instant.EPOCH;
    private static Instant lastWebSocketAttempt = Instant.EPOCH;
    private static volatile String sessionToken = "";

    private BackendClient() {
    }

    public static void start() {
        if (!RUNNING.compareAndSet(false, true)) {
            return;
        }
        startedAt = Instant.now();
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "s9lab-backend-client");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(BackendClient::safeTick, 2, 15, TimeUnit.SECONDS);
    }

    public static void stop() {
        RUNNING.set(false);
        closeSocket("client_shutdown");
        sessionToken = "";
        S9UserSession.clearBackendToken();
        PROFILE_CACHE.clear();
        lastHandshake = Instant.EPOCH;
        WEB_SOCKET_CONNECTING.set(false);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public static void refreshNow() {
        if (executor != null) {
            executor.execute(BackendClient::safeTick);
        }
    }

    public static void buyCosmetic(String cosmeticId) {
        postAction("/shop/buy", cosmeticPayload(cosmeticId, ""), ignored ->
                S9ToastManager.success("Successful purchase", cosmeticName(cosmeticId)));
    }

    public static void buyPlus(String planId) {
        SessionIdentity identity = identity();
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", identity == null ? "" : identity.uuid());
        payload.addProperty("plan", planId);
        postAction("/plus/buy", payload, ignored ->
                S9ToastManager.success("S9Lab Client+", "Plus is now active"));
    }

    public static void giftPlus(String receiver, String planId) {
        SessionIdentity identity = identity();
        JsonObject payload = new JsonObject();
        payload.addProperty("senderUuid", identity == null ? "" : identity.uuid());
        payload.addProperty("plan", planId);
        try {
            UUID uuid = UUID.fromString(receiver == null ? "" : receiver.trim());
            payload.addProperty("receiverUuid", uuid.toString());
            payload.addProperty("receiverName", "");
        } catch (RuntimeException exception) {
            payload.addProperty("receiverUuid", "");
            payload.addProperty("receiverName", receiver == null ? "" : receiver.trim());
        }
        String receiverLabel = receiver == null ? "" : receiver.trim();
        postAction("/plus/gift", payload, ignored ->
                S9ToastManager.gift("S9Lab Client+ gifted", receiverLabel.isBlank() ? "Gift sent" : "To: " + receiverLabel));
    }

    public static void equipCosmetic(CosmeticType type, String cosmeticId) {
        postAction("/cosmetics/equip", cosmeticPayload(cosmeticId, type.commandName()), ignored ->
                S9ToastManager.success("Cosmetic equipped", cosmeticName(cosmeticId)));
    }

    public static void unequipCosmetic(CosmeticType type) {
        postAction("/cosmetics/unequip", cosmeticPayload("", type.commandName()), ignored ->
                S9ToastManager.success("Cosmetic unequipped", typeLabel(type)));
    }

    public static void giftCosmetic(String receiver, String cosmeticId) {
        SessionIdentity identity = identity();
        JsonObject payload = new JsonObject();
        payload.addProperty("senderUuid", identity == null ? "" : identity.uuid());
        payload.addProperty("cosmeticId", cosmeticId);
        try {
            UUID uuid = UUID.fromString(receiver.trim());
            payload.addProperty("receiverUuid", uuid.toString());
            payload.addProperty("receiverName", "");
        } catch (RuntimeException exception) {
            payload.addProperty("receiverUuid", "");
            payload.addProperty("receiverName", receiver == null ? "" : receiver.trim());
        }
        String receiverLabel = receiver == null ? "" : receiver.trim();
        postAction("/cosmetics/gift", payload, ignored ->
                S9ToastManager.gift("Gift sent", receiverLabel.isBlank() ? cosmeticName(cosmeticId) : "To: " + receiverLabel));
    }

    public static void fetchNotificationsAsync() {
        if (executor == null) {
            return;
        }
        executor.execute(() -> {
            try {
                SessionIdentity identity = identity();
                if (identity == null || !ensureSession(identity)) {
                    return;
                }
                applyNotifications(GSON.fromJson(get("/notifications"), JsonObject.class), true);
            } catch (Exception exception) {
                S9LabClient.LOGGER.debug("S9Lab notification fetch failed", exception);
            }
        });
    }

    public static void markNotificationsRead() {
        if (executor == null) {
            return;
        }
        List<Long> ids = BackendState.unreadNotificationsSnapshot().stream()
                .map(BackendState.Notification::notificationId)
                .toList();
        executor.execute(() -> {
            try {
                SessionIdentity identity = identity();
                if (identity == null || !ensureSession(identity)) {
                    return;
                }
                JsonObject payload = new JsonObject();
                payload.addProperty("uuid", identity.uuid());
                payload.add("notificationIds", GSON.toJsonTree(ids));
                request("/notifications/read", payload);
                BackendState.markNotificationsRead(ids);
                S9ToastManager.success("Notifications", "Marked as read");
            } catch (Exception exception) {
                S9ToastManager.warning("Notifications", "Could not mark as read");
                S9LabClient.LOGGER.debug("S9Lab notification read failed", exception);
            }
        });
    }

    public static void pushSettingsAsync() {
        if (executor == null || !RUNNING.get() || sessionToken.isBlank() || S9LabClientClient.getConfigManager() == null) {
            return;
        }
        executor.execute(() -> {
            try {
                SessionIdentity identity = identity();
                if (identity == null || !ensureSession(identity)) {
                    return;
                }
                JsonObject payload = new JsonObject();
                payload.addProperty("uuid", identity.uuid());
                payload.add("settings", GSON.toJsonTree(S9LabClientClient.getConfigManager().backendSettingsSnapshot()));
                applySettings(request("/settings", payload));
            } catch (Exception exception) {
                S9LabClient.LOGGER.debug("S9Lab settings sync failed", exception);
            }
        });
    }

    public static void requestProfile(String target, Consumer<ProfileInfo> success, Consumer<String> failure) {
        if (executor == null) {
            return;
        }
        if (target != null) {
            PROFILE_CACHE.getByName(target).ifPresent(profile -> callbackSuccess(success, ProfileInfo.fromProfile(profile)));
        }
        executor.execute(() -> {
            try {
                SessionIdentity identity = identity();
                if (identity == null || !ensureSession(identity)) {
                    callbackFailure(failure, "No backend session yet");
                    return;
                }
                String route;
                try {
                    route = "/profile/" + UUID.fromString(target.trim());
                } catch (RuntimeException ignored) {
                    route = "/profile/name/" + encode(target.trim());
                }
                ProfileInfo profile = parseProfile(get(route));
                callbackSuccess(success, profile);
            } catch (Exception exception) {
                callbackFailure(failure, exception.getMessage() == null ? "Profile request failed" : exception.getMessage());
            }
        });
    }

    public static void sendEmoteStart(String emoteId) {
        SessionIdentity identity = identity();
        if (identity == null) {
            return;
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", identity.uuid());
        payload.addProperty("emoteId", emoteId);
        postAction("/emotes/start", payload);
    }

    public static void sendEmoteStop() {
        SessionIdentity identity = identity();
        if (identity == null) {
            return;
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", identity.uuid());
        payload.addProperty("emoteId", "");
        postAction("/emotes/stop", payload);
    }

    private static void postAction(String route, JsonObject payload) {
        postAction(route, payload, null);
    }

    private static void postAction(String route, JsonObject payload, Consumer<JsonObject> success) {
        if (!enabled()) {
            BackendState.setOnline(false, "Backend disabled");
            return;
        }
        if (executor == null) {
            return;
        }
        executor.execute(() -> {
            try {
                SessionIdentity identity = identity();
                if (identity == null || !ensureSession(identity)) {
                    BackendState.setOnline(false, "Waiting for Minecraft session");
                    return;
                }
                String body = request(route, payload);
                applyProfile(body);
                if (success != null) {
                    JsonObject json = GSON.fromJson(body, JsonObject.class);
                    if (json != null && json.has("ok") && json.get("ok").getAsBoolean()) {
                        success.accept(json);
                    }
                }
            } catch (Exception exception) {
                offline(exception);
            }
        });
    }

    private static JsonObject cosmeticPayload(String cosmeticId, String type) {
        SessionIdentity identity = identity();
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", identity == null ? "" : identity.uuid());
        payload.addProperty("cosmeticId", cosmeticId);
        payload.addProperty("type", type);
        return payload;
    }

    private static void safeTick() {
        if (!RUNNING.get() || !enabled()) {
            BackendState.setOnline(false, "Backend disabled");
            closeSocket("backend_disabled");
            return;
        }
        try {
            SessionIdentity identity = identity();
            if (identity == null) {
                BackendState.setOnline(false, "Waiting for Minecraft session");
                return;
            }
            if (!ensureSession(identity)) {
                BackendState.setOnline(false, "Backend session unavailable");
                return;
            }

            JsonObject heartbeat = new JsonObject();
            heartbeat.addProperty("uuid", identity.uuid());
            heartbeat.addProperty("name", identity.name());
            heartbeat.addProperty("playtimeSeconds", Duration.between(startedAt, Instant.now()).toSeconds());
            heartbeat.addProperty("status", serverStatus());
            applyProfile(request("/heartbeat", heartbeat));
            connectWebSocket(identity);
        } catch (Exception exception) {
            offline(exception);
        }
    }

    private static boolean ensureSession(SessionIdentity identity) throws Exception {
        if (!sessionToken.isBlank() && Duration.between(lastHandshake, Instant.now()).toMinutes() < 5) {
            return true;
        }
        JsonObject handshake = new JsonObject();
        handshake.addProperty("uuid", identity.uuid());
        handshake.addProperty("name", identity.name());
        handshake.addProperty("clientVersion", "1.0-SNAPSHOT");
        applyProfile(request("/handshake", handshake));
        lastHandshake = Instant.now();
        return !sessionToken.isBlank();
    }

    private static String request(String route, JsonObject payload) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + route))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload), StandardCharsets.UTF_8));
        if (!"/handshake".equals(route) && !sessionToken.isBlank()) {
            builder.header("X-S9Lab-Session", sessionToken);
        }
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 401) {
            sessionToken = "";
            S9UserSession.clearBackendToken();
            lastHandshake = Instant.EPOCH;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("backend_http_" + response.statusCode());
        }
        return response.body();
    }

    private static String get(String route) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + route))
                .timeout(Duration.ofSeconds(5))
                .GET();
        if (!sessionToken.isBlank()) {
            builder.header("X-S9Lab-Session", sessionToken);
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 401) {
            sessionToken = "";
            S9UserSession.clearBackendToken();
            lastHandshake = Instant.EPOCH;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("backend_http_" + response.statusCode());
        }
        return response.body();
    }

    private static void applyProfile(String body) {
        JsonObject json = GSON.fromJson(body, JsonObject.class);
        if (json == null || !json.has("ok") || !json.get("ok").getAsBoolean()) {
            return;
        }
        if (!json.has("coins") && !json.has("ownedCosmetics") && !json.has("equippedCosmetics") && !json.has("catalog")) {
            BackendState.setOnline(true, "Backend connected");
            cacheProfile(json);
            return;
        }

        long coins = json.has("coins") ? json.get("coins").getAsLong() : BackendState.coins();
        Collection<String> owned = json.has("ownedCosmetics")
                ? GSON.fromJson(json.get("ownedCosmetics"), STRING_LIST)
                : List.of();
        Map<String, String> equipped = json.has("equippedCosmetics")
                ? GSON.fromJson(json.get("equippedCosmetics"), STRING_MAP)
                : Map.of();
        Collection<BackendState.ShopCosmetic> catalog = json.has("catalog")
                ? GSON.fromJson(json.get("catalog"), CATALOG_LIST)
                : List.of();
        if (json.has("sessionToken") && !json.get("sessionToken").getAsString().isBlank()) {
            sessionToken = json.get("sessionToken").getAsString();
            S9UserSession.setBackendToken(sessionToken);
            lastHandshake = Instant.now();
        }

        BackendState.applyProfile(coins, owned, equipped, catalog);
        BackendState.applyProfileMetadata(
                string(json, "uuid"),
                string(json, "rank"),
                json.has("plusActive") && json.get("plusActive").getAsBoolean(),
                longValue(json, "plusExpiresAt"),
                json.has("nameEffectsEnabled") && json.get("nameEffectsEnabled").getAsBoolean(),
                json.has("nameEffects") ? GSON.fromJson(json.get("nameEffects"), STRING_LIST) : List.of()
        );
        cacheProfile(json);
        applyNotifications(json, true);
        applySettings(body);
    }

    private static void applyNotifications(JsonObject json, boolean toast) {
        if (json == null || !json.has("notifications")) {
            return;
        }
        try {
            List<BackendState.Notification> notifications = GSON.fromJson(json.get("notifications"), NOTIFICATION_LIST);
            int added = BackendState.applyNotifications(notifications);
            if (toast && added > 0) {
                if (added == 1 && notifications != null && !notifications.isEmpty()) {
                    showNotificationToast(notifications.get(0));
                } else {
                    S9ToastManager.gift("New gifts", "You have " + added + " unread notifications");
                }
            }
        } catch (RuntimeException exception) {
            S9LabClient.LOGGER.debug("S9Lab notifications ignored", exception);
        }
    }

    private static void applySettings(String body) {
        if (S9LabClientClient.getConfigManager() == null) {
            return;
        }
        try {
            JsonObject json = GSON.fromJson(body, JsonObject.class);
            if (json == null || !json.has("settings")) {
                return;
            }
            Map<String, Object> settings = GSON.fromJson(json.get("settings"), SETTINGS_MAP);
            if (settings == null || settings.isEmpty()) {
                pushSettingsAsync();
                return;
            }
            S9LabClientClient.getConfigManager().applyBackendSettings(settings);
        } catch (RuntimeException exception) {
            S9LabClient.LOGGER.debug("S9Lab backend settings ignored", exception);
        }
    }

    private static void connectWebSocket(SessionIdentity identity) {
        if (sessionToken.isBlank() || webSocket != null || !enabled() || Duration.between(lastWebSocketAttempt, Instant.now()).toSeconds() < 60
                || !WEB_SOCKET_CONNECTING.compareAndSet(false, true)) {
            return;
        }
        lastWebSocketAttempt = Instant.now();
        String url = S9LabClientClient.getConfigManager().getBackendWebsocketUrl();
        String query = "?uuid=" + encode(identity.uuid()) + "&name=" + encode(identity.name()) + "&token=" + encode(sessionToken);
        httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .buildAsync(URI.create(url + query), new Listener())
                .thenAccept(socket -> {
                    webSocket = socket;
                    WEB_SOCKET_CONNECTING.set(false);
                })
                .exceptionally(throwable -> {
                    webSocket = null;
                    WEB_SOCKET_CONNECTING.set(false);
                    return null;
                });
    }

    private static void offline(Exception exception) {
        BackendState.setOnline(false, exception.getMessage() == null ? "Backend offline" : exception.getMessage());
        S9LabClient.LOGGER.debug("S9Lab backend offline/failing", exception);
        closeSocket("backend_offline");
        WEB_SOCKET_CONNECTING.set(false);
    }

    private static void closeSocket(String reason) {
        WebSocket socket = webSocket;
        webSocket = null;
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, reason);
        }
    }

    private static boolean enabled() {
        return S9LabClientClient.getConfigManager() != null && S9LabClientClient.getConfigManager().isBackendEnabled();
    }

    private static String baseUrl() {
        String url = S9LabClientClient.getConfigManager().getBackendBaseUrl();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static SessionIdentity identity() {
        return S9UserSession.current()
                .map(identity -> new SessionIdentity(identity.uuidString(), identity.name()))
                .orElse(null);
    }

    private static String serverStatus() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return client.isInSingleplayer() ? "Singleplayer" : "Menu";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record SessionIdentity(String uuid, String name) {
    }

    private static final class Listener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                handleSocketMessage(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            BackendClient.webSocket = webSocket;
            WEB_SOCKET_CONNECTING.set(false);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            BackendClient.webSocket = null;
            WEB_SOCKET_CONNECTING.set(false);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            BackendClient.webSocket = null;
            WEB_SOCKET_CONNECTING.set(false);
        }

        private void handleSocketMessage(String message) {
            try {
                JsonObject json = GSON.fromJson(message, JsonObject.class);
                if (json == null || !json.has("event") || !json.has("uuid")) {
                    return;
                }
                UUID uuid = UUID.fromString(json.get("uuid").getAsString());
                String event = json.get("event").getAsString();
                if ("GiftReceived".equals(event) || "NotificationCreated".equals(event)) {
                    handleNotification(json);
                    return;
                }
                if ("CosmeticPurchased".equals(event) || "CosmeticEquipped".equals(event) || "CosmeticUnequipped".equals(event)) {
                    handleCosmeticAction(event, json);
                    return;
                }
                if ("ProfileUpdate".equals(event)) {
                    applyProfile(message);
                    return;
                }
                if ("PlayerMetadataUpdate".equals(event)) {
                    BackendState.applyProfileMetadata(
                            string(json, "uuid"),
                            string(json, "rank"),
                            json.has("plusActive") && json.get("plusActive").getAsBoolean(),
                            longValue(json, "plusExpiresAt"),
                            json.has("nameEffectsEnabled") && json.get("nameEffectsEnabled").getAsBoolean(),
                            json.has("nameEffects") ? GSON.fromJson(json.get("nameEffects"), STRING_LIST) : List.of()
                    );
                    return;
                }
                if ("CosmeticStateUpdate".equals(event) || "PlayerStatusUpdate".equals(event)) {
                    Map<String, String> equipped = json.has("equippedCosmetics")
                            ? GSON.fromJson(json.get("equippedCosmetics"), STRING_MAP)
                            : new LinkedHashMap<>();
                    boolean online = !json.has("online") || json.get("online").getAsBoolean();
                    BackendState.applyRemoteCosmetics(uuid, equipped, online);
                }
                if ("EmoteStateUpdate".equals(event) || "PlayerStatusUpdate".equals(event)) {
                    String emoteId = json.has("emoteId") ? json.get("emoteId").getAsString() : "";
                    BackendState.applyRemoteEmote(uuid, emoteId);
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void handleNotification(JsonObject json) {
        if (!json.has("notification") || json.get("notification").isJsonNull()) {
            return;
        }
        BackendState.Notification notification = GSON.fromJson(json.get("notification"), BackendState.Notification.class);
        if (BackendState.addNotification(notification)) {
            showNotificationToast(notification);
        }
    }

    private static void handleCosmeticAction(String event, JsonObject json) {
        String cosmeticName = string(json, "cosmeticName");
        switch (event) {
            case "CosmeticPurchased" -> S9ToastManager.success("Successful purchase", cosmeticName);
            case "CosmeticEquipped" -> S9ToastManager.success("Cosmetic equipped", cosmeticName);
            case "CosmeticUnequipped" -> S9ToastManager.success("Cosmetic unequipped", cosmeticName);
            default -> {
            }
        }
    }

    private static void showNotificationToast(BackendState.Notification notification) {
        if (notification == null) {
            return;
        }
        if ("gift_received".equals(notification.type())) {
            S9ToastManager.gift("Gift received", notification.cosmeticName() + " from " + notification.senderName());
        } else {
            S9ToastManager.success("Notification", notification.message());
        }
    }

    private static ProfileInfo parseProfile(String body) {
        JsonObject json = GSON.fromJson(body, JsonObject.class);
        if (json == null || !json.has("ok") || !json.get("ok").getAsBoolean()) {
            throw new IllegalArgumentException("profile_not_found");
        }
        ProfileInfo profile = new ProfileInfo(
                string(json, "uuid"),
                string(json, "name"),
                longValue(json, "coins"),
                intValue(json, "ownedCosmeticsCount"),
                string(json, "activeEmote"),
                longValue(json, "firstSeen"),
                longValue(json, "lastSeen"),
                longValue(json, "totalPlaytimeSeconds"),
                !json.has("online") || json.get("online").getAsBoolean(),
                !json.has("s9labUser") || json.get("s9labUser").getAsBoolean(),
                string(json, "rank").isBlank() ? "USER" : string(json, "rank"),
                json.has("badges") ? GSON.fromJson(json.get("badges"), STRING_LIST) : List.of(),
                json.has("plusActive") && json.get("plusActive").getAsBoolean(),
                longValue(json, "plusExpiresAt")
        );
        cacheProfile(profile);
        return profile;
    }

    private static void callbackSuccess(Consumer<ProfileInfo> success, ProfileInfo profile) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> success.accept(profile));
        }
    }

    private static void callbackFailure(Consumer<String> failure, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> failure.accept(message));
        }
    }

    private static String string(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    private static String cosmeticName(String cosmeticId) {
        if (S9LabClientClient.getCosmeticRegistry() != null) {
            Cosmetic cosmetic = S9LabClientClient.getCosmeticRegistry().get(cosmeticId).orElse(null);
            if (cosmetic != null) {
                return cosmetic.displayName();
            }
        }
        BackendState.ShopCosmetic shopCosmetic = BackendState.catalog(cosmeticId);
        return shopCosmetic.name() == null || shopCosmetic.name().isBlank() ? cosmeticId : shopCosmetic.name();
    }

    private static String typeLabel(CosmeticType type) {
        return type == null ? "Cosmetic" : type.commandName();
    }

    private static long longValue(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : 0L;
    }

    private static int intValue(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    public record ProfileInfo(
            String uuid,
            String name,
            long coins,
            int ownedCosmeticsCount,
            String activeEmote,
            long firstSeen,
            long lastSeen,
            long totalPlaytimeSeconds,
            boolean online,
            boolean s9labUser,
            String rank,
            List<String> badges,
            boolean plusActive,
            long plusExpiresAt
    ) {
        private static ProfileInfo fromProfile(UserProfile profile) {
            return new ProfileInfo(
                    profile.minecraftUuid().toString(),
                    profile.minecraftName(),
                    profile.coins(),
                    profile.equippedCosmetics().size(),
                    "",
                    profile.createdAt(),
                    profile.lastOnlineAt(),
                    profile.playtimeSeconds(),
                    profile.presence() != site.s9lab.s9labclient.client.foundation.model.PresenceStatus.OFFLINE,
                    true,
                    profile.rank().name(),
                    profile.badges(),
                    profile.plusStatus().active(),
                    profile.plusStatus().expiresAt()
            );
        }
    }

    public static ProfileCache profileCache() {
        return PROFILE_CACHE;
    }

    private static void cacheProfile(JsonObject json) {
        if (json == null || !json.has("uuid") || !json.has("name")) {
            return;
        }
        try {
            Map<String, String> equipped = json.has("equippedCosmetics")
                    ? GSON.fromJson(json.get("equippedCosmetics"), STRING_MAP)
                    : Map.of();
            UserProfile profile = UserProfile.basic(
                    UUID.fromString(string(json, "uuid")),
                    string(json, "name"),
                    longValue(json, "coins"),
                    longValue(json, "totalPlaytimeSeconds"),
                    longValue(json, "firstSeen"),
                    longValue(json, "lastSeen"),
                    !json.has("online") || json.get("online").getAsBoolean(),
                    string(json, "rank"),
                    json.has("badges") ? GSON.fromJson(json.get("badges"), STRING_LIST) : List.of(),
                    longValue(json, "plusExpiresAt"),
                    equipped
            );
            PROFILE_CACHE.put(profile);
        } catch (RuntimeException ignored) {
        }
    }

    private static void cacheProfile(ProfileInfo profile) {
        if (profile == null || profile.uuid().isBlank()) {
            return;
        }
        try {
            BackendState.applyProfileMetadata(profile.uuid(), profile.rank(), profile.plusActive(), profile.plusExpiresAt(), false, List.of());
            PROFILE_CACHE.put(UserProfile.basic(
                    UUID.fromString(profile.uuid()),
                    profile.name(),
                    profile.coins(),
                    profile.totalPlaytimeSeconds(),
                    profile.firstSeen(),
                    profile.lastSeen(),
                    profile.online(),
                    profile.rank(),
                    profile.badges(),
                    profile.plusExpiresAt(),
                    Map.of()
            ));
        } catch (RuntimeException ignored) {
        }
    }
}
