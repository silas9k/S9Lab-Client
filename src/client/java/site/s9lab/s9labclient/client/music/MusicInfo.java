package site.s9lab.s9labclient.client.music;

public record MusicInfo(
        String title,
        String artist,
        String source,
        long positionMillis,
        long durationMillis,
        boolean playing
) {
    public double progress() {
        if (durationMillis <= 0L) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (double) positionMillis / (double) durationMillis));
    }

    public String formattedPosition() {
        return format(positionMillis);
    }

    public String formattedDuration() {
        return format(durationMillis);
    }

    private static String format(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        return totalSeconds / 60L + ":" + (totalSeconds % 60L < 10L ? "0" : "") + totalSeconds % 60L;
    }
}
