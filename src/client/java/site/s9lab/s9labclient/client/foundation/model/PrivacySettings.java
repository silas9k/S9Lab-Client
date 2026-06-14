package site.s9lab.s9labclient.client.foundation.model;

public record PrivacySettings(
        boolean showOnlineStatus,
        boolean showCurrentServer,
        boolean allowJoinFriend,
        boolean allowFriendRequests,
        boolean showCreatorCode,
        boolean showPlaytime
) {
    public static PrivacySettings defaults() {
        return new PrivacySettings(true, true, true, true, true, true);
    }
}
