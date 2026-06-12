package site.s9lab.s9labclient.client.backend;

import java.util.Collection;
import java.util.Comparator;
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
    private static final Map<Long, Notification> NOTIFICATIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> ONLINE_S9_PLAYERS = ConcurrentHashMap.newKeySet();
    private static volatile long coins;
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

    public record ShopCosmetic(String id, String type, String name, String description, long price, boolean enabled) {
        public static ShopCosmetic fallback(String cosmeticId) {
            return new ShopCosmetic(cosmeticId, "", cosmeticId, "Waiting for backend catalog.", 0, true);
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
