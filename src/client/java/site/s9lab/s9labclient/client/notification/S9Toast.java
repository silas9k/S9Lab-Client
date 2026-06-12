package site.s9lab.s9labclient.client.notification;

public record S9Toast(String title, String message, int accentColor, long createdAtMillis, long durationMillis) {
    public static S9Toast of(String title, String message, int accentColor) {
        return new S9Toast(title, message == null ? "" : message, accentColor, System.currentTimeMillis(), 4_200L);
    }

    public boolean expired(long now) {
        return now - createdAtMillis > durationMillis;
    }

    public float progress(long now) {
        return Math.min(1.0F, Math.max(0.0F, (now - createdAtMillis) / (float) durationMillis));
    }
}
