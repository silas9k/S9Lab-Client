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
    private static final Map<UUID, Friend> FRIENDS = new ConcurrentHashMap<>();
    private static final Map<UUID, FriendRequest> INCOMING_FRIEND_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<UUID, FriendRequest> OUTGOING_FRIEND_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<UUID, List<DirectMessage>> FRIEND_MESSAGES = new ConcurrentHashMap<>();
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

    public static synchronized void applyFriendsSnapshot(
            Collection<Friend> friends,
            Collection<FriendRequest> incoming,
            Collection<FriendRequest> outgoing
    ) {
        FRIENDS.clear();
        if (friends != null) {
            for (Friend friend : friends) {
                if (friend == null || friend.uuid() == null || friend.uuid().isBlank()) {
                    continue;
                }
                try {
                    FRIENDS.put(UUID.fromString(friend.uuid()), friend);
                } catch (RuntimeException ignored) {
                }
            }
        }
        INCOMING_FRIEND_REQUESTS.clear();
        if (incoming != null) {
            for (FriendRequest request : incoming) {
                putFriendRequest(INCOMING_FRIEND_REQUESTS, request);
            }
        }
        OUTGOING_FRIEND_REQUESTS.clear();
        if (outgoing != null) {
            for (FriendRequest request : outgoing) {
                putFriendRequest(OUTGOING_FRIEND_REQUESTS, request);
            }
        }
    }

    public static List<Friend> friendsSnapshot() {
        return FRIENDS.values().stream()
                .sorted(Comparator
                        .comparing(Friend::favorite).reversed()
                        .thenComparing(Comparator.comparing(Friend::online).reversed())
                        .thenComparing(Friend::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static List<FriendRequest> incomingFriendRequestsSnapshot() {
        return INCOMING_FRIEND_REQUESTS.values().stream()
                .sorted(Comparator.comparingLong(FriendRequest::createdAt).reversed())
                .toList();
    }

    public static List<FriendRequest> outgoingFriendRequestsSnapshot() {
        return OUTGOING_FRIEND_REQUESTS.values().stream()
                .sorted(Comparator.comparingLong(FriendRequest::createdAt).reversed())
                .toList();
    }

    public static Friend friend(UUID uuid) {
        return uuid == null ? null : FRIENDS.get(uuid);
    }

    public static boolean applyFriendPresence(UUID uuid, String name, boolean online, long lastSeen, String friendStatus) {
        if (uuid == null) {
            return false;
        }
        Friend previous = FRIENDS.get(uuid);
        if (previous == null) {
            return false;
        }
        Friend updated = new Friend(
                uuid.toString(),
                name == null || name.isBlank() ? previous.name() : name,
                online,
                Math.max(lastSeen, previous.lastSeen()),
                friendStatus == null || friendStatus.isBlank() ? (online ? "Online" : "Offline") : friendStatus,
                previous.favorite(),
                previous.unreadMessages()
        );
        FRIENDS.put(uuid, updated);
        return previous.online() != online;
    }

    public static void applyConversation(String friendUuid, Collection<DirectMessage> messages) {
        try {
            UUID uuid = UUID.fromString(friendUuid);
            List<DirectMessage> normalized = messages == null ? List.of() : messages.stream()
                    .filter(message -> message != null && message.messageId() > 0)
                    .sorted(Comparator.comparingLong(DirectMessage::sentAt).thenComparingLong(DirectMessage::messageId))
                    .toList();
            FRIEND_MESSAGES.put(uuid, List.copyOf(normalized));
            Friend friend = FRIENDS.get(uuid);
            if (friend != null && friend.unreadMessages() != 0) {
                FRIENDS.put(uuid, friend.withUnreadMessages(0));
            }
        } catch (RuntimeException ignored) {
        }
    }

    public static UUID applyFriendMessage(DirectMessage message) {
        if (message == null || message.messageId() <= 0) {
            return null;
        }
        UUID own = ownUuid();
        if (own == null) {
            return null;
        }
        try {
            UUID sender = UUID.fromString(message.senderUuid());
            UUID receiver = UUID.fromString(message.receiverUuid());
            UUID friendUuid = own.equals(sender) ? receiver : sender;
            List<DirectMessage> existing = FRIEND_MESSAGES.getOrDefault(friendUuid, List.of());
            if (existing.stream().noneMatch(entry -> entry.messageId() == message.messageId())) {
                List<DirectMessage> updated = new ArrayList<>(existing);
                updated.add(message);
                updated.sort(Comparator.comparingLong(DirectMessage::sentAt).thenComparingLong(DirectMessage::messageId));
                if (updated.size() > 100) {
                    updated = new ArrayList<>(updated.subList(updated.size() - 100, updated.size()));
                }
                FRIEND_MESSAGES.put(friendUuid, List.copyOf(updated));
            }
            if (own.equals(receiver)) {
                Friend friend = FRIENDS.get(friendUuid);
                if (friend != null) {
                    FRIENDS.put(friendUuid, friend.withUnreadMessages(friend.unreadMessages() + 1));
                }
            }
            return friendUuid;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static List<DirectMessage> conversationSnapshot(UUID friendUuid) {
        return friendUuid == null ? List.of() : FRIEND_MESSAGES.getOrDefault(friendUuid, List.of());
    }

    public static int totalUnreadFriendMessages() {
        return FRIENDS.values().stream().mapToInt(Friend::unreadMessages).sum();
    }

    private static void putFriendRequest(Map<UUID, FriendRequest> target, FriendRequest request) {
        if (request == null || request.uuid() == null || request.uuid().isBlank()) {
            return;
        }
        try {
            target.put(UUID.fromString(request.uuid()), request);
        } catch (RuntimeException ignored) {
        }
    }

    private static UUID ownUuid() {
        try {
            return net.minecraft.client.MinecraftClient.getInstance().getSession().getUuidOrNull();
        } catch (RuntimeException ignored) {
            return null;
        }
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

    public record Friend(
            String uuid,
            String name,
            boolean online,
            long lastSeen,
            String status,
            boolean favorite,
            int unreadMessages
    ) {
        public Friend withUnreadMessages(int unread) {
            return new Friend(uuid, name, online, lastSeen, status, favorite, Math.max(0, unread));
        }
    }

    public record FriendRequest(String uuid, String name, long createdAt) {
    }

    public record DirectMessage(
            long messageId,
            String senderUuid,
            String receiverUuid,
            String senderName,
            String message,
            long sentAt,
            boolean read
    ) {
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
