package site.s9lab.s9labclient.client.cosmetics;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import site.s9lab.s9labclient.client.emote.EmoteManager;

/**
 * Preview-only render state bridge.
 *
 * GUI entity rendering is queued by DrawContext. The short-lived preparation
 * context is therefore used only while a fresh render state is populated. The
 * actual cosmetic/visibility selection is kept in a stable state-key map until
 * the queued render command executes.
 */
public final class CosmeticPreviewContext {
    private static final long TTL_MS = 10_000L;
    private static final int MAX_ENTRIES = 512;
    private static final Map<Integer, PreviewEntry> STATE_PREVIEWS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Cosmetic> PREPARING_COSMETIC = new ThreadLocal<>();

    private CosmeticPreviewContext() {
    }

    public static void begin(Cosmetic cosmetic) {
        if (cosmetic == null) {
            PREPARING_COSMETIC.remove();
        } else {
            PREPARING_COSMETIC.set(cosmetic);
        }
        cleanup();
    }

    public static void bindState(int stateId, Cosmetic cosmetic, BaseVisibility visibility) {
        if (cosmetic == null) {
            return;
        }
        STATE_PREVIEWS.put(
                stateId,
                new PreviewEntry(
                        cosmetic,
                        visibility == null ? BaseVisibility.FULL : visibility,
                        System.currentTimeMillis()
                )
        );
        cleanup();
    }

    /** Compatibility overload for older call sites. */
    public static void bindState(int stateId, Cosmetic cosmetic, boolean hideBasePlayer) {
        bindState(stateId, cosmetic, hideBasePlayer ? BaseVisibility.HIDDEN : BaseVisibility.FULL);
    }

    public static void end() {
        PREPARING_COSMETIC.remove();
    }

    public static boolean active() {
        return PREPARING_COSMETIC.get() != null;
    }

    public static boolean activeForState(int stateId) {
        return entry(stateId).isPresent();
    }

    public static BaseVisibility visibilityForState(int stateId) {
        return entry(stateId).map(PreviewEntry::visibility).orElse(BaseVisibility.FULL);
    }

    public static boolean hideBasePlayer(int stateId) {
        return visibilityForState(stateId) == BaseVisibility.HIDDEN;
    }

    public static boolean headOnly(int stateId) {
        return visibilityForState(stateId) == BaseVisibility.HEAD_ONLY;
    }

    /**
     * Used while PlayerEntityRenderer builds a fresh preview state (not during
     * the queued render itself), e.g. to inject a preview cape into SkinTextures.
     */
    public static Optional<Cosmetic> get(CosmeticType type) {
        Cosmetic cosmetic = PREPARING_COSMETIC.get();
        if (cosmetic == null || cosmetic.type() != type) {
            return Optional.empty();
        }
        return Optional.of(cosmetic);
    }

    public static Optional<Cosmetic> getForState(int stateId, CosmeticType type) {
        return entry(stateId)
                .map(PreviewEntry::cosmetic)
                .filter(cosmetic -> cosmetic.type() == type);
    }

    public static Optional<Cosmetic> cosmeticForState(int stateId) {
        return entry(stateId).map(PreviewEntry::cosmetic);
    }

    public static EmoteManager.Emote emoteForState(int stateId) {
        Cosmetic cosmetic = cosmeticForState(stateId).orElse(null);
        if (cosmetic == null || cosmetic.type() != CosmeticType.EMOTE) {
            return null;
        }
        String id = cosmetic.id().replace("s9lab_emote_", "");
        EmoteManager.Emote emote = EmoteManager.byIdOrName(id);
        return emote != null ? emote : EmoteManager.byIdOrName(cosmetic.displayName());
    }

    /** Stable negative key, isolated from normal positive entity ids. */
    public static int stableKey(String scope, Cosmetic cosmetic) {
        String cosmeticId = cosmetic == null ? "none" : cosmetic.id();
        int hash = 31 * (scope == null ? 0 : scope.hashCode()) + cosmeticId.hashCode();
        return -1 - (hash & 0x3FFFFFFF);
    }

    private static Optional<PreviewEntry> entry(int stateId) {
        PreviewEntry preview = STATE_PREVIEWS.get(stateId);
        if (preview == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() - preview.createdAt() > TTL_MS) {
            STATE_PREVIEWS.remove(stateId, preview);
            return Optional.empty();
        }
        return Optional.of(preview);
    }

    private static void cleanup() {
        long cutoff = System.currentTimeMillis() - TTL_MS;
        STATE_PREVIEWS.entrySet().removeIf(entry -> entry.getValue().createdAt() < cutoff);
        if (STATE_PREVIEWS.size() > MAX_ENTRIES) {
            STATE_PREVIEWS.clear();
        }
    }

    public enum BaseVisibility {
        FULL,
        HEAD_ONLY,
        HIDDEN
    }

    private record PreviewEntry(Cosmetic cosmetic, BaseVisibility visibility, long createdAt) {
    }
}
