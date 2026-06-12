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

    public record EmoteRequest(String uuid, String emoteId) {
    }

    public record SettingsRequest(String uuid, Map<String, Object> settings) {
    }

    public record SettingsResponse(boolean ok, String uuid, Map<String, Object> settings) {
    }

    public record CosmeticDto(String id, String type, String name, String description, long price, boolean enabled) {
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
            boolean online
    ) {
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
            boolean s9labUser
    ) {
    }

    public record StateUpdate(String event, String uuid, Map<String, String> equippedCosmetics, String emoteId, boolean online) {
    }
}
