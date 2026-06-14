package site.s9lab.s9labclient.client.backend;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

public final class BackendState {
    private static final Map<String, ShopCosmetic> CATALOG = new ConcurrentHashMap<>();
    private static final Set<String> OWNED = ConcurrentHashMap.newKeySet();
    private static final Map<CosmeticType, String> EQUIPPED = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<CosmeticType, String>> REMOTE_EQUIPPED = new ConcurrentHashMap<>();
    private static final Map<UUID, String> REMOTE_EMOTES = new ConcurrentHashMap<>();
    private static final Map<UUID, String> RANKS = new ConcurrentHashMap<>();
    private static final Map<UUID, NameEffects> NAME_EFFECTS = new ConcurrentHashMap<>();
    private static final Map<Long, Notification> NOTIFICATIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> ONLINE_S9_PLAYERS = ConcurrentHashMap.newKeySet();
    private static volatile long coins;
    private static volatile boolean plusActive;
    private static volatile long plusExpiresAt;
    private static volatile boolean online;
    private static volatile String status = "Backend offline";

    private BackendState() {
    }

    public static void setOnline(boolean online, String status) {
        BackendState.online = online;
        BackendState.status = status == null ? "" : status;
    }

    public static boolean online() {
        return online;
    }

    public static String status() {
        return status;
    }

    public static long coins() {
        return coins;
    }

    public static boolean plusActive() {
        return plusActive && plusExpiresAt > System.currentTimeMillis() / 1000L;
    }

    public static long plusExpiresAt() {
        return plusExpiresAt;
    }

    public static void applyProfile(long coins, Collection<String> owned, Map<String, String> equipped, Collection<ShopCosmetic> catalog) {
        BackendState.coins = Math.max(0, coins);
        OWNED.clear();
        if (owned != null) {
            OWNED.addAll(owned);
        }

        EQUIPPED.clear();
        if (equipped != null) {
            for (Map.Entry<String, String> entry : equipped.entrySet()) {
                CosmeticType.byCommandName(entry.getKey()).ifPresent(type -> EQUIPPED.put(type, entry.getValue()));
            }
        }

        if (catalog != null && !catalog.isEmpty()) {
            CATALOG.clear();
            for (ShopCosmetic cosmetic : catalog) {
                CATALOG.put(cosmetic.id(), cosmetic);
            }
        }
        online = true;
        status = "Backend connected";
    }

    public static int applyNotifications(Collection<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return 0;
        }
        int added = 0;
        for (Notification notification : notifications) {
            if (notification == null || notification.notificationId() <= 0 || notification.read()) {
                continue;
            }
            if (NOTIFICATIONS.putIfAbsent(notification.notificationId(), notification) == null) {
                added++;
            }
        }
        return added;
    }

    public static boolean addNotification(Notification notification) {
        return notification != null
                && notification.notificationId() > 0
                && !notification.read()
                && NOTIFICATIONS.putIfAbsent(notification.notificationId(), notification) == null;
    }

    public static List<Notification> unreadNotificationsSnapshot() {
        return NOTIFICATIONS.values().stream()
                .filter(notification -> !notification.read())
                .sorted(Comparator.comparingLong(Notification::createdAt).reversed())
                .toList();
    }

    public static int unreadNotificationCount() {
        return (int) NOTIFICATIONS.values().stream().filter(notification -> !notification.read()).count();
    }

    public static void markNotificationsRead(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            NOTIFICATIONS.clear();
            return;
        }
        ids.forEach(NOTIFICATIONS::remove);
    }

    public static boolean owned(String cosmeticId) {
        return OWNED.contains(cosmeticId);
    }

    public static Set<String> ownedSnapshot() {
        return Set.copyOf(OWNED);
    }

    public static boolean equipped(CosmeticType type, String cosmeticId) {
        return cosmeticId != null && cosmeticId.equals(EQUIPPED.get(type));
    }

    public static String equippedId(CosmeticType type) {
        return EQUIPPED.getOrDefault(type, "");
    }

    public static ShopCosmetic catalog(String cosmeticId) {
        return CATALOG.getOrDefault(cosmeticId, ShopCosmetic.fallback(cosmeticId));
    }

    public static Map<String, ShopCosmetic> catalogSnapshot() {
        return new LinkedHashMap<>(CATALOG);
    }

    public static void applyRemoteCosmetics(UUID uuid, Map<String, String> equipped, boolean remoteOnline) {
        if (uuid == null) {
            return;
        }
        if (remoteOnline) {
            ONLINE_S9_PLAYERS.add(uuid);
        } else {
            ONLINE_S9_PLAYERS.remove(uuid);
        }
        if (equipped == null || equipped.isEmpty()) {
            REMOTE_EQUIPPED.remove(uuid);
            return;
        }
        Map<CosmeticType, String> normalized = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            CosmeticType.byCommandName(entry.getKey()).ifPresent(type -> normalized.put(type, entry.getValue()));
        }
        REMOTE_EQUIPPED.put(uuid, normalized);
    }

    public static boolean isS9Player(UUID uuid) {
        return uuid != null && (ONLINE_S9_PLAYERS.contains(uuid) || REMOTE_EQUIPPED.containsKey(uuid));
    }

    public static String remoteEquipped(UUID uuid, CosmeticType type) {
        Map<CosmeticType, String> equipped = REMOTE_EQUIPPED.get(uuid);
        return equipped == null ? "" : equipped.getOrDefault(type, "");
    }

    public static void applyRemoteEmote(UUID uuid, String emoteId) {
        if (uuid == null) {
            return;
        }
        if (emoteId == null || emoteId.isBlank()) {
            REMOTE_EMOTES.remove(uuid);
        } else {
            REMOTE_EMOTES.put(uuid, emoteId);
            ONLINE_S9_PLAYERS.add(uuid);
        }
    }

    public static String remoteEmote(UUID uuid) {
        return uuid == null ? "" : REMOTE_EMOTES.getOrDefault(uuid, "");
    }

    public static void applyProfileMetadata(String uuid, String rank, boolean plusActive) {
        applyProfileMetadata(uuid, rank, plusActive, 0L);
    }

    public static void applyProfileMetadata(String uuid, String rank, boolean active, long expiresAt) {
        applyProfileMetadata(uuid, rank, active, expiresAt, false, List.of());
    }

    public static void applyProfileMetadata(String uuid, String rank, boolean active, long expiresAt, boolean effectsEnabled, Collection<String> effects) {
        if (uuid == null || uuid.isBlank()) {
            return;
        }
        try {
            UUID parsed = UUID.fromString(uuid);
            ONLINE_S9_PLAYERS.add(parsed);
            boolean activePlus = active || "PLUS".equalsIgnoreCase(rank);
            RANKS.put(parsed, activePlus ? "PLUS" : "USER");
            NAME_EFFECTS.put(parsed, new NameEffects(activePlus && effectsEnabled, normalizeEffects(effects)));
            if (isOwnUuid(parsed)) {
                plusActive = activePlus;
                plusExpiresAt = Math.max(0L, expiresAt);
            }
        } catch (RuntimeException ignored) {
        }
    }

    public static boolean plusIcon(UUID uuid) {
        return uuid != null && "PLUS".equalsIgnoreCase(RANKS.getOrDefault(uuid, "USER"));
    }

    public static NameEffects nameEffects(UUID uuid) {
        return uuid == null ? NameEffects.DISABLED : NAME_EFFECTS.getOrDefault(uuid, NameEffects.DISABLED);
    }

    private static boolean isOwnUuid(UUID uuid) {
        try {
            return net.minecraft.client.MinecraftClient.getInstance().getSession().getUuidOrNull().equals(uuid);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static List<String> normalizeEffects(Collection<String> effects) {
        if (effects == null || effects.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String effect : effects) {
            String value = effect == null ? "" : effect.trim().toLowerCase();
            if (value.isBlank() || "none".equals(value)) {
                continue;
            }
            if (List.of("rainbow", "wave", "shake", "spin", "bounce", "pulse", "blink", "fade", "iterate", "glitch", "color_white", "color_red", "color_orange", "color_yellow", "color_green", "color_cyan", "color_blue", "color_purple", "color_pink").contains(value)
                    && !normalized.contains(value)) {
                normalized.add(value);
            }
            if (normalized.size() >= 3) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    public record NameEffects(boolean enabled, List<String> effects) {
        public static final NameEffects DISABLED = new NameEffects(false, List.of());
    }

    public record ShopCosmetic(
            String id,
            String type,
            String name,
            String description,
            long price,
            boolean enabled,
            String rarity,
            boolean limited,
            long availableFrom,
            long availableUntil,
            boolean plusExclusive,
            String limitedText,
            String previewAsset
    ) {
        public static ShopCosmetic fallback(String cosmeticId) {
            return new ShopCosmetic(cosmeticId, "", cosmeticId, "Waiting for backend catalog.", 0, true, "COMMON", false, 0L, 0L, false, "", "");
        }
    }

    public record Notification(
            long notificationId,
            String receiverUuid,
            String senderUuid,
            String senderName,
            String type,
            String cosmeticId,
            String cosmeticName,
            String message,
            long createdAt,
            boolean read
    ) {
    }
}
