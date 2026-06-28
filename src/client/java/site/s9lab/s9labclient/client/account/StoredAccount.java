package site.s9lab.s9labclient.client.account;

public record StoredAccount(
        String username, String uuid, String skinUrl, String capeUrl,
        String microsoftAccountId, String maskedEmail,
        boolean reauthRequired, boolean launcherSession, String status,
        long lastUsedAt, long addedAt
) {
    public StoredAccount normalized() {
        return new StoredAccount(value(username), value(uuid), value(skinUrl), value(capeUrl),
                value(microsoftAccountId), value(maskedEmail), reauthRequired, launcherSession,
                value(status), Math.max(0L, lastUsedAt), Math.max(0L, addedAt));
    }

    public StoredAccount withState(String nextStatus, boolean needsLogin) {
        return new StoredAccount(username, uuid, skinUrl, capeUrl, microsoftAccountId, maskedEmail,
                needsLogin, launcherSession, value(nextStatus), lastUsedAt, addedAt);
    }

    public StoredAccount usedNow() {
        return new StoredAccount(username, uuid, skinUrl, capeUrl, microsoftAccountId, maskedEmail,
                reauthRequired, launcherSession, status, System.currentTimeMillis(), addedAt);
    }

    public StoredAccount withProfile(String name, String skin, String cape, String nextStatus) {
        return new StoredAccount(value(name), uuid, value(skin), value(cape), microsoftAccountId, maskedEmail,
                false, launcherSession, value(nextStatus), lastUsedAt, addedAt);
    }

    private static String value(String value) { return value == null ? "" : value; }
}
