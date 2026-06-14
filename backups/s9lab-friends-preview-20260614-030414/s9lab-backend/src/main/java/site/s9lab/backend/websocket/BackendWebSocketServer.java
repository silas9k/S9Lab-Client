package site.s9lab.backend.websocket;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.network.Json;
import site.s9lab.backend.security.SessionManager;
import site.s9lab.backend.storage.DatabaseManager;

public final class BackendWebSocketServer extends WebSocketServer {
    private static final Logger LOGGER = Logger.getLogger("S9LabBackend");
    private final DatabaseManager database;
    private final SessionManager sessions;
    private final Map<WebSocket, String> clients = new ConcurrentHashMap<>();

    public BackendWebSocketServer(int port, DatabaseManager database, SessionManager sessions) {
        super(new InetSocketAddress("0.0.0.0", port));
        this.database = database;
        this.sessions = sessions;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String uuid = queryParam(handshake.getResourceDescriptor(), "uuid");
        String token = queryParam(handshake.getResourceDescriptor(), "token");
        if (!validUuid(uuid)) {
            conn.close(1008, "missing_uuid");
            return;
        }
        uuid = UUID.fromString(uuid).toString();
        if (!sessions.validate(uuid, token)) {
            conn.close(1008, "session_required");
            return;
        }
        String canonicalUuid = uuid;
        clients.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(canonicalUuid)) {
                entry.getKey().close(1000, "replaced");
                return true;
            }
            return false;
        });
        clients.put(conn, canonicalUuid);
        try {
            database.setOnline(canonicalUuid, true);
            for (String onlineUuid : database.onlinePlayers()) {
                conn.send(Json.GSON.toJson(new Dtos.StateUpdate(
                        "CosmeticStateUpdate",
                        onlineUuid,
                        database.equippedCosmetics(onlineUuid),
                        "",
                        true
                )));
                database.activeEmote(onlineUuid).ifPresent(emote -> conn.send(Json.GSON.toJson(new Dtos.StateUpdate(
                        "EmoteStateUpdate",
                        onlineUuid,
                        Map.of(),
                        emote,
                        true
                ))));
            }
            broadcastState("PlayerStatusUpdate", canonicalUuid, database.equippedCosmetics(canonicalUuid), database.activeEmote(canonicalUuid).orElse(""), true);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "websocket_open_state_failed", exception);
        }
        LOGGER.info(() -> "ws_open uuid=" + canonicalUuid);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String uuid = clients.remove(conn);
        if (uuid == null) {
            return;
        }
        try {
            database.setOnline(uuid, false);
            broadcastState("PlayerStatusUpdate", uuid, null, null, false);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "websocket_close_state_failed", exception);
        }
        LOGGER.info(() -> "ws_close uuid=" + uuid + " code=" + code);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Client gameplay changes go through HTTP endpoints so validation stays central.
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.log(Level.WARNING, "websocket_error", ex);
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(30);
    }

    public void broadcastCosmeticState(String uuid) {
        try {
            broadcastState("CosmeticStateUpdate", uuid, database.equippedCosmetics(uuid), null, true);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "cosmetic_state_broadcast_failed", exception);
        }
    }

    public void broadcastEmoteState(String uuid, String emoteId) {
        broadcastState("EmoteStateUpdate", uuid, null, emoteId == null ? "" : emoteId, true);
    }

    public void sendProfileUpdate(String uuid) {
        try {
            Dtos.PlayerAdminResponse profile = database.profile(uuid);
            if (profile == null) {
                return;
            }
            Map<String, Object> update = new LinkedHashMap<>();
            update.put("event", "ProfileUpdate");
            update.put("ok", true);
            update.put("uuid", profile.uuid());
            update.put("name", profile.name());
            update.put("coins", profile.coins());
            update.put("ownedCosmetics", profile.ownedCosmetics());
            update.put("equippedCosmetics", profile.equippedCosmetics());
            update.put("activeEmote", profile.activeEmote());
            update.put("firstSeen", profile.firstSeen());
            update.put("lastSeen", profile.lastSeen());
            update.put("totalPlaytimeSeconds", profile.totalPlaytimeSeconds());
            update.put("online", profile.online());
            update.put("rank", profile.rank());
            update.put("badges", profile.badges());
            update.put("plusActive", profile.plusActive());
            update.put("plusExpiresAt", profile.plusExpiresAt());
            update.put("nameEffectsEnabled", profile.nameEffectsEnabled());
            update.put("nameEffects", profile.nameEffects());
            update.put("catalog", database.cosmetics());
            String body = Json.GSON.toJson(update);
            clients.forEach((socket, clientUuid) -> {
                if (uuid.equals(clientUuid) && socket.isOpen()) {
                    socket.send(body);
                }
            });
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "profile_update_send_failed", exception);
        }
    }

    public void broadcastPlayerMetadata(String uuid) {
        try {
            Dtos.PlayerAdminResponse profile = database.profile(uuid);
            if (profile == null) {
                return;
            }
            Map<String, Object> update = new LinkedHashMap<>();
            update.put("event", "PlayerMetadataUpdate");
            update.put("ok", true);
            update.put("uuid", profile.uuid());
            update.put("name", profile.name());
            update.put("rank", profile.rank());
            update.put("badges", profile.badges());
            update.put("plusActive", profile.plusActive());
            update.put("plusExpiresAt", profile.plusExpiresAt());
            update.put("nameEffectsEnabled", profile.nameEffectsEnabled());
            update.put("nameEffects", profile.nameEffects());
            broadcast(Json.GSON.toJson(update));
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "player_metadata_broadcast_failed", exception);
        }
    }

    public void sendNotification(String uuid, String event, Dtos.NotificationDto notification) {
        if (uuid == null || notification == null) {
            return;
        }
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("event", event == null || event.isBlank() ? "NotificationCreated" : event);
        update.put("ok", true);
        update.put("uuid", uuid);
        update.put("notification", notification);
        sendToUuid(uuid, Json.GSON.toJson(update));
    }

    public void sendCosmeticAction(String uuid, String event, String cosmeticId, String cosmeticName) {
        if (uuid == null || event == null || event.isBlank()) {
            return;
        }
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("event", event);
        update.put("ok", true);
        update.put("uuid", uuid);
        update.put("cosmeticId", cosmeticId == null ? "" : cosmeticId);
        update.put("cosmeticName", cosmeticName == null || cosmeticName.isBlank() ? cosmeticId : cosmeticName);
        sendToUuid(uuid, Json.GSON.toJson(update));
    }

    public void broadcastState(String event, String uuid, Map<String, String> equipped, String emoteId, boolean online) {
        Dtos.StateUpdate update = new Dtos.StateUpdate(event, uuid, equipped == null ? Map.of() : equipped, emoteId == null ? "" : emoteId, online);
        broadcast(Json.GSON.toJson(update));
    }

    private void sendToUuid(String uuid, String body) {
        clients.forEach((socket, clientUuid) -> {
            if (uuid.equals(clientUuid) && socket.isOpen()) {
                socket.send(body);
            }
        });
    }

    private static String queryParam(String descriptor, String key) {
        if (descriptor == null) {
            return "";
        }
        int queryStart = descriptor.indexOf('?');
        if (queryStart < 0 || queryStart + 1 >= descriptor.length()) {
            return "";
        }
        String[] parts = descriptor.substring(queryStart + 1).split("&");
        for (String part : parts) {
            String[] split = part.split("=", 2);
            if (split.length == 2 && split[0].equals(key)) {
                return split[1];
            }
        }
        return "";
    }

    private static boolean validUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
