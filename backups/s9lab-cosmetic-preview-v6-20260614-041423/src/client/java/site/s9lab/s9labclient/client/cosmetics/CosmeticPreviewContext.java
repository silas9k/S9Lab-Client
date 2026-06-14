package site.s9lab.s9labclient.client.cosmetics;

import java.util.Optional;

public final class CosmeticPreviewContext {
    private static Cosmetic previewCosmetic;

    private CosmeticPreviewContext() {
    }

    public static void begin(Cosmetic cosmetic) {
        previewCosmetic = cosmetic;
    }

    public static void end() {
        previewCosmetic = null;
    }

    public static boolean active() {
        return previewCosmetic != null;
    }

    public static Optional<Cosmetic> get(CosmeticType type) {
        if (previewCosmetic == null || previewCosmetic.type() != type) {
            return Optional.empty();
        }
        return Optional.of(previewCosmetic);
    }
}
