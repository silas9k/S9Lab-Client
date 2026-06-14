package site.s9lab.s9labclient.client.foundation.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UserProfile(
        String userId,
        UUID minecraftUuid,
        String minecraftName,
        S9Rank rank,
        List<String> badges,
        PlusStatus plusStatus,
        long coins,
        long playtimeSeconds,
        long createdAt,
        long lastOnlineAt,
        PresenceStatus presence,
        String currentServer,
        Map<String, String> equippedCosmetics,
        List<String> showcasedCosmetics,
        String profileBorder,
        CreatorCodeSummary creatorCode,
        PrivacySettings privacySettings,
        String friendshipStatus
) {
    public static UserProfile basic(
            UUID uuid,
            String name,
            long coins,
            long playtimeSeconds,
            long createdAt,
            long lastOnlineAt,
            boolean online,
            String rank,
            List<String> badges,
            long plusExpiresAt,
            Map<String, String> equippedCosmetics
    ) {
        return new UserProfile(
                uuid.toString(),
                uuid,
                name == null || name.isBlank() ? "Unknown" : name,
                S9Rank.fromWire(rank),
                badges == null ? List.of() : List.copyOf(badges),
                PlusStatus.of(plusExpiresAt),
                Math.max(0L, coins),
                Math.max(0L, playtimeSeconds),
                Math.max(0L, createdAt),
                Math.max(0L, lastOnlineAt),
                online ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE,
                "",
                equippedCosmetics == null ? Map.of() : Map.copyOf(equippedCosmetics),
                List.of(),
                "",
                CreatorCodeSummary.NONE,
                PrivacySettings.defaults(),
                "none"
        );
    }
}
