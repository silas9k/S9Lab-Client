package site.s9lab.s9labclient.client.ui;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public final class FeaturedServerTextureCache {
    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final int MAX_DIMENSION = 2048;
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private static final int MAX_TEXTURES = 32;
    private static final long FAILED_RETRY_MS = 5L * 60L * 1000L;
    private static final Map<String, CacheEntry> CACHE = new LinkedHashMap<>();

    private FeaturedServerTextureCache() {
    }

    public static synchronized Identifier texture(String url) {
        String safe = url == null ? "" : url.trim();
        if (safe.isBlank() || (!safe.startsWith("https://") && !safe.startsWith("http://"))) return null;
        CacheEntry entry = CACHE.get(safe);
        if (entry != null) {
            if (entry.textureId != null) return entry.textureId;
            if (entry.loading || System.currentTimeMillis() - entry.failedAt < FAILED_RETRY_MS) return null;
        }
        load(safe);
        return null;
    }

    private static synchronized void load(String url) {
        CacheEntry existing = CACHE.get(url);
        if (existing != null && existing.loading) return;
        CacheEntry entry = existing == null ? new CacheEntry() : existing;
        entry.loading = true;
        CACHE.put(url, entry);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "S9LabClient/1.0")
                .GET().build();
        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
                String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase();
                byte[] bytes = response.body();
                if (response.statusCode() != 200 || !contentType.startsWith("image/") || bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
                    throw new IllegalArgumentException("invalid_banner_image");
                }
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                if (image.getWidth() <= 0 || image.getHeight() <= 0 || image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
                    image.close();
                    throw new IllegalArgumentException("banner_image_dimensions");
                }
                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> install(url, image));
            } catch (Exception ignored) {
                synchronized (FeaturedServerTextureCache.class) {
                    CacheEntry failed = CACHE.computeIfAbsent(url, ignoredUrl -> new CacheEntry());
                    failed.loading = false;
                    failed.failedAt = System.currentTimeMillis();
                }
            }
        });
    }

    private static synchronized void install(String url, NativeImage image) {
        CacheEntry entry = CACHE.computeIfAbsent(url, ignored -> new CacheEntry());
        entry.texture = new NativeImageBackedTexture(() -> "S9Lab server banner", image);
        entry.textureId = Identifier.of("s9labclient", "server_banner_" + Integer.toUnsignedString(url.hashCode()));
        entry.loading = false;
        entry.failedAt = 0L;
        MinecraftClient.getInstance().getTextureManager().registerTexture(entry.textureId, entry.texture);
        trimCache();
    }

    private static void trimCache() {
        while (CACHE.size() > MAX_TEXTURES) {
            String oldest = CACHE.keySet().iterator().next();
            CacheEntry removed = CACHE.remove(oldest);
            if (removed != null && removed.texture != null) removed.texture.close();
        }
    }

    private static final class CacheEntry {
        private Identifier textureId;
        private NativeImageBackedTexture texture;
        private boolean loading;
        private long failedAt;
    }
}
