package site.s9lab.backend.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {
    private final int limitPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int limitPerMinute) {
        this.limitPerMinute = Math.max(10, limitPerMinute);
    }

    public boolean allow(String key) {
        long minute = System.currentTimeMillis() / 60_000L;
        Window window = windows.compute(key, (ignored, old) -> {
            if (old == null || old.minute != minute) {
                return new Window(minute, 1);
            }
            return new Window(minute, old.count + 1);
        });
        return window.count <= limitPerMinute;
    }

    private record Window(long minute, int count) {
    }
}
