package site.s9lab.backend.profiles;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.network.Json;
import site.s9lab.backend.security.SessionManager;
import site.s9lab.backend.storage.DatabaseManager;

public final class ProfileService {
    private static final Type SETTINGS_TYPE = new TypeToken<Map<String, Object>>() { }.getType();
    private static final Set<String> BLOCKED_SETTINGS_KEYS = Set.of(
            "coins",
            "owned",
            "ownedcosmetics",
            "equipped",
            "equippedcosmetics",
            "catalog",
            "sessiontoken",
            "notifications",
            "adminsecret"
    );

    private final DatabaseManager database;
    private final SessionManager sessions;

    public ProfileService(DatabaseManager database, SessionManager sessions) {
        this.database = database;
        this.sessions = sessions;
    }

    public Dtos.ProfileResponse handshake(Dtos.HandshakeRequest request) throws SQLException {
        String uuid = requireUuid(request.uuid());
        database.ensurePlayer(uuid, request.name());
        Dtos.PlayerAdminResponse profile = database.profile(uuid);
        return new Dtos.ProfileResponse(
                true,
                profile.uuid(),
                profile.name(),
                profile.coins(),
                profile.ownedCosmetics(),
                profile.equippedCosmetics(),
                profile.activeEmote(),
                profile.firstSeen(),
                profile.lastSeen(),
                profile.totalPlaytimeSeconds(),
                true,
                profile.rank(),
                profile.badges(),
                profile.plusActive(),
                profile.plusExpiresAt(),
                profile.nameEffectsEnabled(),
                profile.nameEffects(),
                database.cosmetics(),
                settingsMap(profile.uuid()),
                database.unreadNotifications(profile.uuid()),
                sessions.create(profile.uuid())
        );
    }

    public Dtos.ProfileResponse heartbeat(Dtos.HeartbeatRequest request) throws SQLException {
        String uuid = requireUuid(request.uuid());
        database.heartbeat(uuid, request.name(), request.playtimeSeconds(), true);
        Dtos.PlayerAdminResponse profile = database.profile(uuid);
        return new Dtos.ProfileResponse(
                true,
                profile.uuid(),
                profile.name(),
                profile.coins(),
                profile.ownedCosmetics(),
                profile.equippedCosmetics(),
                profile.activeEmote(),
                profile.firstSeen(),
                profile.lastSeen(),
                profile.totalPlaytimeSeconds(),
                profile.online(),
                profile.rank(),
                profile.badges(),
                profile.plusActive(),
                profile.plusExpiresAt(),
                profile.nameEffectsEnabled(),
                profile.nameEffects(),
                database.cosmetics(),
                settingsMap(profile.uuid()),
                database.unreadNotifications(profile.uuid()),
                ""
        );
    }

    public Dtos.SettingsResponse settings(String uuid) throws SQLException {
        String normalized = requireUuid(uuid);
        return new Dtos.SettingsResponse(true, normalized, settingsMap(normalized));
    }

    public Dtos.SettingsResponse saveSettings(String uuid, Map<String, Object> settings) throws SQLException {
        String normalized = requireUuid(uuid);
        Map<String, Object> safeSettings = sanitizeSettings(settings == null ? Map.of() : settings);
        String json = Json.GSON.toJson(safeSettings);
        if (json.length() > 65_536) {
            throw new IllegalArgumentException("settings_too_large");
        }
        database.saveUserSettingsJson(normalized, json);
        saveNameEffectsFromSettings(normalized, safeSettings);
        return new Dtos.SettingsResponse(true, normalized, settingsMap(normalized));
    }

    public Dtos.PublicProfileResponse publicProfileByUuid(String uuid) throws SQLException {
        Dtos.PlayerAdminResponse profile = database.profile(requireUuid(uuid));
        return publicProfile(profile);
    }

    public Dtos.PublicProfileResponse publicProfileByName(String name) throws SQLException {
        Dtos.PlayerAdminResponse profile = database.profileByName(name);
        return publicProfile(profile);
    }

    private Dtos.PublicProfileResponse publicProfile(Dtos.PlayerAdminResponse profile) throws SQLException {
        if (profile == null) {
            return null;
        }
        return new Dtos.PublicProfileResponse(
                true,
                profile.uuid(),
                profile.name(),
                profile.coins(),
                database.ownedCosmeticsCount(profile.uuid()),
                profile.equippedCosmetics(),
                profile.activeEmote(),
                profile.firstSeen(),
                profile.lastSeen(),
                profile.totalPlaytimeSeconds(),
                profile.online(),
                true,
                profile.rank(),
                profile.badges(),
                profile.plusActive(),
                profile.plusExpiresAt(),
                profile.nameEffectsEnabled(),
                profile.nameEffects()
        );
    }

    private void saveNameEffectsFromSettings(String uuid, Map<String, Object> settings) throws SQLException {
        Map<?, ?> modules = settings.get("modules") instanceof Map<?, ?> map ? map : null;
        Map<?, ?> badge = modules != null && modules.get("Tablist Badge") instanceof Map<?, ?> map ? map : null;
        Map<?, ?> badgeSettings = badge != null && badge.get("settings") instanceof Map<?, ?> map ? map : null;
        if (badgeSettings == null) {
            return;
        }
        boolean enabled = valueAsBoolean(badgeSettings.get("Plus Name Effects Enabled"), false);
        List<String> effects = List.of(
                valueAsString(badgeSettings.get("Plus Effect 1")),
                valueAsString(badgeSettings.get("Plus Effect 2")),
                valueAsString(badgeSettings.get("Plus Effect 3"))
        );
        database.saveNameEffects(uuid, enabled, effects);
    }

    private Map<String, Object> settingsMap(String uuid) throws SQLException {
        try {
            Map<String, Object> settings = Json.GSON.fromJson(database.userSettingsJson(uuid), SETTINGS_TYPE);
            return sanitizeSettings(settings == null ? new LinkedHashMap<>() : settings);
        } catch (RuntimeException exception) {
            return new LinkedHashMap<>();
        }
    }

    private static Map<String, Object> sanitizeSettings(Map<String, Object> input) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            if (count++ >= 96) {
                break;
            }
            String key = sanitizeKey(entry.getKey());
            if (blockedKey(key)) {
                continue;
            }
            Object value = sanitizeValue(entry.getValue(), 0);
            if (value != null) {
                sanitized.put(key, value);
            }
        }
        return sanitized;
    }

    private static Object sanitizeValue(Object value, int depth) {
        if (value == null || depth > 5) {
            return null;
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof String string) {
            return limit(string, 160);
        }
        if (value instanceof Number number) {
            return Double.isFinite(number.doubleValue()) ? number : null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (count++ >= 96) {
                    break;
                }
                String key = sanitizeKey(String.valueOf(entry.getKey()));
                if (blockedKey(key)) {
                    continue;
                }
                Object nestedValue = sanitizeValue(entry.getValue(), depth + 1);
                if (nestedValue != null) {
                    nested.put(key, nestedValue);
                }
            }
            return nested;
        }
        if (value instanceof List<?> list) {
            List<Object> nested = new ArrayList<>();
            for (int i = 0; i < Math.min(32, list.size()); i++) {
                Object nestedValue = sanitizeValue(list.get(i), depth + 1);
                if (nestedValue != null) {
                    nested.add(nestedValue);
                }
            }
            return nested;
        }
        return null;
    }

    private static boolean blockedKey(String key) {
        return key.isBlank() || BLOCKED_SETTINGS_KEYS.contains(key.toLowerCase().replaceAll("[^a-z0-9]", ""));
    }

    private static boolean valueAsBoolean(Object value, boolean fallback) {
        return value instanceof Boolean booleanValue ? booleanValue : fallback;
    }

    private static String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String sanitizeKey(String key) {
        return limit(key == null ? "" : key.trim(), 64);
    }

    private static String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String requireUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("missing_uuid");
        }
        try {
            return UUID.fromString(uuid.trim()).toString();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid_uuid");
        }
    }
}
