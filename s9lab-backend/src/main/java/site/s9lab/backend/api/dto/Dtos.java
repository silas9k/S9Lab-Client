package site.s9lab.backend.api.dto;

import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {
    }

    public record ApiError(boolean ok, String error) {
        public static ApiError of(String error) {
            return new ApiError(false, error);
        }
    }

    public record Ok(boolean ok) {
        public static Ok yes() {
            return new Ok(true);
        }
    }

    public record HandshakeRequest(String uuid, String name, String clientVersion) {
    }

    public record HeartbeatRequest(String uuid, String name, long playtimeSeconds, String status) {
    }

    public record CoinRequest(String uuid, long amount) {
    }

    public record PlusPlanDto(String id, String name, int months, long price) {
    }

    public record PlusPurchaseRequest(String uuid, String plan) {
    }

    public record PlusGiftRequest(String senderUuid, String receiverUuid, String receiverName, String plan) {
    }

    public record CosmeticRequest(String uuid, String cosmeticId, String type) {
    }

    public record GiftRequest(String senderUuid, String receiverUuid, String receiverName, String cosmeticId) {
    }

    public record NotificationDto(
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

    public record NotificationListResponse(boolean ok, List<NotificationDto> notifications) {
    }

    public record NotificationReadRequest(String uuid, List<Long> notificationIds) {
    }

    public record NotificationReadResponse(boolean ok, int markedRead) {
    }

    public record GiftResult(PlayerAdminResponse senderProfile, String receiverUuid, NotificationDto notification) {
    }

    public record PlusGiftResult(PlayerAdminResponse senderProfile, String receiverUuid) {
    }

    public record EmoteRequest(String uuid, String emoteId) {
    }

    public record SettingsRequest(String uuid, Map<String, Object> settings) {
    }

    public record SettingsResponse(boolean ok, String uuid, Map<String, Object> settings) {
    }

    public record CosmeticDto(
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
            String previewAsset,
            Map<String, String> metadata
    ) {
        public CosmeticDto(String id, String type, String name, String description, long price, boolean enabled) {
            this(id, type, name, description, price, enabled, "COMMON", false, 0L, 0L, false, "", "", Map.of());
        }
    }

    public record ProfileResponse(
            boolean ok,
            String uuid,
            String name,
            long coins,
            List<String> ownedCosmetics,
            Map<String, String> equippedCosmetics,
            String activeEmote,
            long firstSeen,
            long lastSeen,
            long totalPlaytimeSeconds,
            boolean online,
            String rank,
            List<String> badges,
            boolean plusActive,
            long plusExpiresAt,
            boolean nameEffectsEnabled,
            List<String> nameEffects,
            List<CosmeticDto> catalog,
            Map<String, Object> settings,
            List<NotificationDto> notifications,
            String sessionToken
    ) {
    }

    public record PlayerAdminResponse(
            boolean ok,
            String uuid,
            String name,
            long coins,
            List<String> ownedCosmetics,
            Map<String, String> equippedCosmetics,
            String activeEmote,
            long firstSeen,
            long lastSeen,
            long totalPlaytimeSeconds,
            boolean online,
            String rank,
            List<String> badges,
            boolean plusActive,
            long plusExpiresAt,
            boolean nameEffectsEnabled,
            List<String> nameEffects
    ) {
        public PlayerAdminResponse(
                boolean ok,
                String uuid,
                String name,
                long coins,
                List<String> ownedCosmetics,
                Map<String, String> equippedCosmetics,
                String activeEmote,
                long firstSeen,
                long lastSeen,
                long totalPlaytimeSeconds,
                boolean online
        ) {
            this(ok, uuid, name, coins, ownedCosmetics, equippedCosmetics, activeEmote, firstSeen, lastSeen, totalPlaytimeSeconds, online, "USER", List.of(), false, 0L, false, List.of());
        }
    }

    public record PublicProfileResponse(
            boolean ok,
            String uuid,
            String name,
            long coins,
            int ownedCosmeticsCount,
            Map<String, String> equippedCosmetics,
            String activeEmote,
            long firstSeen,
            long lastSeen,
            long totalPlaytimeSeconds,
            boolean online,
            boolean s9labUser,
            String rank,
            List<String> badges,
            boolean plusActive,
            long plusExpiresAt,
            boolean nameEffectsEnabled,
            List<String> nameEffects
    ) {
        public PublicProfileResponse(
                boolean ok,
                String uuid,
                String name,
                long coins,
                int ownedCosmeticsCount,
                Map<String, String> equippedCosmetics,
                String activeEmote,
                long firstSeen,
                long lastSeen,
                long totalPlaytimeSeconds,
                boolean online,
                boolean s9labUser
        ) {
            this(ok, uuid, name, coins, ownedCosmeticsCount, equippedCosmetics, activeEmote, firstSeen, lastSeen, totalPlaytimeSeconds, online, s9labUser, "USER", List.of(), false, 0L, false, List.of());
        }
    }

    public record StateUpdate(String event, String uuid, Map<String, String> equippedCosmetics, String emoteId, boolean online) {
    }
}
