package site.s9lab.backend.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.config.BackendConfig;
import site.s9lab.backend.emotes.EmoteService;
import site.s9lab.backend.profiles.ProfileService;
import site.s9lab.backend.security.SessionManager;
import site.s9lab.backend.shop.ShopService;
import site.s9lab.backend.storage.DatabaseManager;
import site.s9lab.backend.websocket.BackendWebSocketServer;

public final class ApiServer {
    private static final Logger LOGGER = Logger.getLogger("S9LabBackend");
    private static final long MAX_COINS = 1_000_000_000_000L;
    private static final Set<String> COSMETIC_TYPES = Set.of("cape", "bandana", "wings", "hat", "halo", "shoulder", "glint", "emote");

    private final BackendConfig config;
    private final DatabaseManager database;
    private final ProfileService profiles;
    private final ShopService shop;
    private final EmoteService emotes;
    private final SessionManager sessions;
    private final BackendWebSocketServer webSocketServer;
    private final RateLimiter rateLimiter;
    private HttpServer server;

    public ApiServer(
            BackendConfig config,
            DatabaseManager database,
            ProfileService profiles,
            ShopService shop,
            SessionManager sessions,
            BackendWebSocketServer webSocketServer
    ) {
        this.config = config;
        this.database = database;
        this.profiles = profiles;
        this.shop = shop;
        this.emotes = new EmoteService(database);
        this.sessions = sessions;
        this.webSocketServer = webSocketServer;
        this.rateLimiter = new RateLimiter(config.rateLimitPerMinute());
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(config.host(), config.resolvedPort()), 32);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        LOGGER.info(() -> "http_started host=" + config.host() + " port=" + config.resolvedPort());
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String ip = exchange.getRemoteAddress() == null ? "unknown" : exchange.getRemoteAddress().getAddress().getHostAddress();
            if (!rateLimiter.allow(ip)) {
                Json.send(exchange, 429, Dtos.ApiError.of("rate_limited"));
                return;
            }
            route(exchange);
        } catch (IllegalArgumentException exception) {
            Json.send(exchange, 400, Dtos.ApiError.of(exception.getMessage()));
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "database_error", exception);
            Json.send(exchange, 500, Dtos.ApiError.of("database_error"));
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "api_error", exception);
            Json.send(exchange, 500, Dtos.ApiError.of("internal_error"));
        } finally {
            exchange.close();
        }
    }

    private void route(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
        String path = normalize(exchange.getRequestURI().getPath());

        if (method.equals("GET") && path.equals("/health")) {
            Json.send(exchange, 200, Map.of("ok", true, "service", "s9lab-backend"));
            return;
        }

        if (path.startsWith("/admin/")) {
            handleAdmin(exchange, method, path);
            return;
        }

        String apiRoot = "/api/" + config.apiVersion();
        if (!path.startsWith(apiRoot)) {
            Json.send(exchange, 404, Dtos.ApiError.of("not_found"));
            return;
        }
        String route = path.substring(apiRoot.length());

        if (method.equals("POST") && route.equals("/handshake")) {
            Dtos.HandshakeRequest request = requireBody(Json.read(exchange, Dtos.HandshakeRequest.class));
            Dtos.ProfileResponse response = profiles.handshake(request);
            if (webSocketServer != null) {
                webSocketServer.broadcastState("PlayerStatusUpdate", response.uuid(), response.equippedCosmetics(), response.activeEmote(), true);
            }
            LOGGER.info(() -> "handshake uuid=" + response.uuid() + " name=" + response.name());
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("POST") && route.equals("/heartbeat")) {
            Dtos.HeartbeatRequest request = requireBody(Json.read(exchange, Dtos.HeartbeatRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            Dtos.ProfileResponse response = profiles.heartbeat(new Dtos.HeartbeatRequest(uuid, request.name(), request.playtimeSeconds(), request.status()));
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("GET") && route.equals("/cosmetics")) {
            Json.send(exchange, 200, Map.of("ok", true, "catalog", database.cosmetics()));
            return;
        }

        if (method.equals("GET") && route.equals("/plus/plans")) {
            String uuid = requireSessionUuid(exchange);
            if (uuid == null) {
                return;
            }
            Json.send(exchange, 200, Map.of("ok", true, "plans", shop.plusPlans(), "profile", database.profile(uuid)));
            return;
        }

        if (method.equals("GET") && route.equals("/settings")) {
            String uuid = requireSessionUuid(exchange);
            if (uuid == null) {
                return;
            }
            Json.send(exchange, 200, profiles.settings(uuid));
            return;
        }

        if (method.equals("POST") && route.equals("/settings")) {
            String uuid = requireSessionUuid(exchange);
            if (uuid == null) {
                return;
            }
            Dtos.SettingsRequest request = requireBody(Json.read(exchange, Dtos.SettingsRequest.class));
            if (request.uuid() != null && !request.uuid().isBlank() && !requireUuid(request.uuid()).equals(uuid)) {
                throw new IllegalArgumentException("uuid_mismatch");
            }
            Dtos.SettingsResponse response = profiles.saveSettings(uuid, request.settings());
            if (webSocketServer != null) {
                webSocketServer.broadcastPlayerMetadata(uuid);
            }
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("GET") && route.equals("/notifications")) {
            String uuid = requireSessionUuid(exchange);
            if (uuid == null) {
                return;
            }
            Json.send(exchange, 200, new Dtos.NotificationListResponse(true, database.unreadNotifications(uuid)));
            return;
        }

        if (method.equals("POST") && route.equals("/notifications/read")) {
            String uuid = requireSessionUuid(exchange);
            if (uuid == null) {
                return;
            }
            Dtos.NotificationReadRequest request = Json.read(exchange, Dtos.NotificationReadRequest.class);
            String requestedUuid = request == null ? null : request.uuid();
            if (requestedUuid != null && !requestedUuid.isBlank() && !requireUuid(requestedUuid).equals(uuid)) {
                throw new IllegalArgumentException("uuid_mismatch");
            }
            int markedRead = database.markNotificationsRead(uuid, request == null ? List.of() : request.notificationIds());
            Json.send(exchange, 200, new Dtos.NotificationReadResponse(true, markedRead));
            return;
        }

        if (method.equals("GET") && route.startsWith("/profile/name/")) {
            if (requireSessionUuid(exchange) == null) {
                return;
            }
            String name = decodePathPart(route.substring("/profile/name/".length()));
            Dtos.PublicProfileResponse response = profiles.publicProfileByName(name);
            Json.send(exchange, response == null ? 404 : 200, response == null ? Dtos.ApiError.of("player_not_found") : response);
            return;
        }

        if (method.equals("GET") && route.startsWith("/profile/")) {
            if (requireSessionUuid(exchange) == null) {
                return;
            }
            String uuid = requireUuid(route.substring("/profile/".length()));
            Dtos.PublicProfileResponse response = profiles.publicProfileByUuid(uuid);
            Json.send(exchange, response == null ? 404 : 200, response == null ? Dtos.ApiError.of("player_not_found") : response);
            return;
        }

        if (method.equals("POST") && route.equals("/shop/buy")) {
            Dtos.CosmeticRequest request = requireBody(Json.read(exchange, Dtos.CosmeticRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            String cosmeticId = requireText(request.cosmeticId(), "missing_cosmetic");
            Dtos.PlayerAdminResponse response = shop.buy(uuid, cosmeticId);
            if (webSocketServer != null) {
                webSocketServer.sendCosmeticAction(uuid, "CosmeticPurchased", cosmeticId, database.cosmetic(cosmeticId).map(Dtos.CosmeticDto::name).orElse(cosmeticId));
            }
            LOGGER.info(() -> "cosmetic_buy uuid=" + request.uuid() + " cosmetic=" + request.cosmeticId());
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("POST") && route.equals("/plus/buy")) {
            Dtos.PlusPurchaseRequest request = requireBody(Json.read(exchange, Dtos.PlusPurchaseRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            Dtos.PlayerAdminResponse response = shop.buyPlus(uuid, requireText(request.plan(), "missing_plus_plan"));
            if (webSocketServer != null) {
                webSocketServer.sendProfileUpdate(uuid);
                webSocketServer.broadcastPlayerMetadata(uuid);
            }
            LOGGER.info(() -> "plus_buy uuid=" + uuid + " plan=" + request.plan());
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("POST") && route.equals("/plus/gift")) {
            Dtos.PlusGiftRequest request = requireBody(Json.read(exchange, Dtos.PlusGiftRequest.class));
            String senderUuid = requireUuid(request.senderUuid());
            if (!requireClientSession(exchange, senderUuid)) {
                return;
            }
            String receiverUuid = request.receiverUuid() == null || request.receiverUuid().isBlank() ? "" : requireUuid(request.receiverUuid());
            Dtos.PlusGiftResult result = shop.giftPlus(
                    senderUuid,
                    receiverUuid,
                    request.receiverName(),
                    requireText(request.plan(), "missing_plus_plan")
            );
            if (webSocketServer != null) {
                webSocketServer.sendProfileUpdate(senderUuid);
                webSocketServer.sendProfileUpdate(result.receiverUuid());
                webSocketServer.broadcastPlayerMetadata(result.receiverUuid());
            }
            LOGGER.info(() -> "plus_gift sender=" + senderUuid + " receiver=" + (receiverUuid.isBlank() ? request.receiverName() : receiverUuid) + " plan=" + request.plan());
            Json.send(exchange, 200, result.senderProfile());
            return;
        }

        if (method.equals("POST") && route.equals("/cosmetics/gift")) {
            Dtos.GiftRequest request = requireBody(Json.read(exchange, Dtos.GiftRequest.class));
            String senderUuid = requireUuid(request.senderUuid());
            if (!requireClientSession(exchange, senderUuid)) {
                return;
            }
            String receiverUuid = request.receiverUuid() == null || request.receiverUuid().isBlank() ? "" : requireUuid(request.receiverUuid());
            Dtos.GiftResult result = shop.gift(
                    senderUuid,
                    receiverUuid,
                    request.receiverName(),
                    requireText(request.cosmeticId(), "missing_cosmetic")
            );
            if (webSocketServer != null) {
                webSocketServer.sendProfileUpdate(senderUuid);
                webSocketServer.sendProfileUpdate(result.receiverUuid());
                webSocketServer.sendNotification(result.receiverUuid(), "GiftReceived", result.notification());
                webSocketServer.sendNotification(result.receiverUuid(), "NotificationCreated", result.notification());
            }
            LOGGER.info(() -> "cosmetic_gift sender=" + senderUuid + " cosmetic=" + request.cosmeticId());
            Json.send(exchange, 200, result.senderProfile());
            return;
        }

        if (method.equals("POST") && route.equals("/cosmetics/equip")) {
            Dtos.CosmeticRequest request = requireBody(Json.read(exchange, Dtos.CosmeticRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            String cosmeticId = requireText(request.cosmeticId(), "missing_cosmetic");
            Dtos.PlayerAdminResponse response = shop.equip(uuid, cosmeticId);
            if (webSocketServer != null) {
                webSocketServer.broadcastCosmeticState(uuid);
                webSocketServer.sendCosmeticAction(uuid, "CosmeticEquipped", cosmeticId, database.cosmetic(cosmeticId).map(Dtos.CosmeticDto::name).orElse(cosmeticId));
            }
            LOGGER.info(() -> "cosmetic_equip uuid=" + request.uuid() + " cosmetic=" + request.cosmeticId());
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("POST") && route.equals("/cosmetics/unequip")) {
            Dtos.CosmeticRequest request = requireBody(Json.read(exchange, Dtos.CosmeticRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            Dtos.PlayerAdminResponse response = shop.unequip(uuid, requireCosmeticType(request.type()));
            if (webSocketServer != null) {
                webSocketServer.broadcastCosmeticState(uuid);
                webSocketServer.sendCosmeticAction(uuid, "CosmeticUnequipped", request.type(), request.type());
            }
            LOGGER.info(() -> "cosmetic_unequip uuid=" + request.uuid() + " type=" + request.type());
            Json.send(exchange, 200, response);
            return;
        }

        if (method.equals("POST") && route.equals("/emotes/start")) {
            Dtos.EmoteRequest request = requireBody(Json.read(exchange, Dtos.EmoteRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            String emoteId = requireText(request.emoteId(), "missing_emote");
            emotes.start(uuid, emoteId);
            if (webSocketServer != null) {
                webSocketServer.broadcastEmoteState(uuid, emoteId);
            }
            LOGGER.info(() -> "emote_start uuid=" + request.uuid() + " emote=" + request.emoteId());
            Json.send(exchange, 200, Dtos.Ok.yes());
            return;
        }

        if (method.equals("POST") && route.equals("/emotes/stop")) {
            Dtos.EmoteRequest request = requireBody(Json.read(exchange, Dtos.EmoteRequest.class));
            String uuid = requireUuid(request.uuid());
            if (!requireClientSession(exchange, uuid)) {
                return;
            }
            emotes.stop(uuid);
            if (webSocketServer != null) {
                webSocketServer.broadcastEmoteState(uuid, "");
            }
            LOGGER.info(() -> "emote_stop uuid=" + request.uuid());
            Json.send(exchange, 200, Dtos.Ok.yes());
            return;
        }

        Json.send(exchange, 404, Dtos.ApiError.of("not_found"));
    }

    private void handleAdmin(HttpExchange exchange, String method, String path) throws IOException, SQLException {
        if (!isAdmin(exchange)) {
            Json.send(exchange, 401, Dtos.ApiError.of("admin_secret_required"));
            return;
        }

        if (method.equals("GET") && path.startsWith("/admin/player/")) {
            String uuid = requireUuid(path.substring("/admin/player/".length()));
            Dtos.PlayerAdminResponse response = database.profile(uuid);
            Json.send(exchange, response == null ? 404 : 200, response == null ? Dtos.ApiError.of("player_not_found") : response);
            return;
        }

        if (method.equals("POST") && List.of("/admin/coins/set", "/admin/coins/add", "/admin/coins/remove").contains(path)) {
            Dtos.CoinRequest request = requireBody(Json.read(exchange, Dtos.CoinRequest.class));
            String uuid = requireUuid(request.uuid());
            long amount = requireCoinAmount(request.amount());
            switch (path) {
                case "/admin/coins/set" -> database.setCoins(uuid, amount, "admin-api");
                case "/admin/coins/add" -> database.addCoins(uuid, amount, "admin-api");
                case "/admin/coins/remove" -> database.addCoins(uuid, -amount, "admin-api");
                default -> throw new IllegalArgumentException("unknown_admin_route");
            }
            LOGGER.info(() -> "admin_coin_change route=" + path + " uuid=" + uuid + " amount=" + amount);
            Json.send(exchange, 200, database.profile(uuid));
            return;
        }

        Json.send(exchange, 404, Dtos.ApiError.of("not_found"));
    }

    private boolean isAdmin(HttpExchange exchange) {
        String provided = exchange.getRequestHeaders().getFirst("X-Admin-Secret");
        String expected = config.adminSecret();
        if (provided == null || expected == null || expected.isBlank() || expected.startsWith("CHANGE_ME")) {
            return false;
        }
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean requireClientSession(HttpExchange exchange, String uuid) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("X-S9Lab-Session");
        if (!sessions.validate(uuid, token)) {
            Json.send(exchange, 401, Dtos.ApiError.of("session_required"));
            return false;
        }
        return true;
    }

    private String requireSessionUuid(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("X-S9Lab-Session");
        return sessions.authenticate(token)
                .orElseGet(() -> {
                    try {
                        Json.send(exchange, 401, Dtos.ApiError.of("session_required"));
                    } catch (IOException ignored) {
                    }
                    return null;
                });
    }

    private static String requireUuid(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("missing_uuid");
        }
        try {
            return UUID.fromString(uuid.trim()).toString();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid_uuid");
        }
    }

    private static String requireText(String value, String error) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(error);
        }
        return value.trim();
    }

    private static <T> T requireBody(T request) {
        if (request == null) {
            throw new IllegalArgumentException("missing_body");
        }
        return request;
    }

    private static String requireCosmeticType(String value) {
        String type = requireText(value, "missing_type").toLowerCase(Locale.ROOT);
        if (!COSMETIC_TYPES.contains(type)) {
            throw new IllegalArgumentException("invalid_cosmetic_type");
        }
        return type;
    }

    private static long requireCoinAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("invalid_amount");
        }
        if (amount > MAX_COINS) {
            throw new IllegalArgumentException("amount_too_large");
        }
        return amount;
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = new String(path.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        return normalized.length() > 1 && normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private static String decodePathPart(String value) {
        return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Admin-Secret, X-S9Lab-Session");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}
