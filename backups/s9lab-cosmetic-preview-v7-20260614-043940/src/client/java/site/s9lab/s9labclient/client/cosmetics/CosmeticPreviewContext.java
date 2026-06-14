package site.s9lab.s9labclient.client.cosmetics;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import site.s9lab.s9labclient.client.emote.EmoteManager;

/**
 * Bridges queued GUI entity rendering with the cosmetic feature renderers.
 * DrawContext#addEntity queues work, so a short-lived state-id mapping is used
 * instead of relying only on a thread-local/current preview cosmetic.
 */
public final class CosmeticPreviewContext {
    private static final long TTL_MS = 5_000L;
    private static final Map<Integer, PreviewEntry> STATE_PREVIEWS = new ConcurrentHashMap<>();
    private static Cosmetic preparingCosmetic;

    private CosmeticPreviewContext() {
    }

    public static void begin(Cosmetic cosmetic) {
        preparingCosmetic = cosmetic;
        cleanup();
    }

    public static void bindState(int stateId, Cosmetic cosmetic, boolean hideBasePlayer) {
        if (cosmetic == null) {
            return;
        }
        STATE_PREVIEWS.put(stateId, new PreviewEntry(cosmetic, hideBasePlayer, System.currentTimeMillis()));
        cleanup();
    }

    public static void end() {
        preparingCosmetic = null;
    }

    public static boolean active() {
        return preparingCosmetic != null;
    }

    public static boolean activeForState(int stateId) {
        return entry(stateId).isPresent();
    }

    public static boolean hideBasePlayer(int stateId) {
        return entry(stateId).map(PreviewEntry::hideBasePlayer).orElse(false);
    }

    public static Optional<Cosmetic> get(CosmeticType type) {
        if (preparingCosmetic == null || preparingCosmetic.type() != type) {
            return Optional.empty();
        }
        return Optional.of(preparingCosmetic);
    }

    public static Optional<Cosmetic> getForState(int stateId, CosmeticType type) {
        return entry(stateId)
                .map(PreviewEntry::cosmetic)
                .filter(cosmetic -> cosmetic.type() == type);
    }

    public static EmoteManager.Emote emoteForState(int stateId) {
        Cosmetic cosmetic = entry(stateId).map(PreviewEntry::cosmetic).orElse(null);
        if (cosmetic == null || cosmetic.type() != CosmeticType.EMOTE) {
            return null;
        }
        String id = cosmetic.id().replace("s9lab_emote_", "");
        EmoteManager.Emote emote = EmoteManager.byIdOrName(id);
        return emote != null ? emote : EmoteManager.byIdOrName(cosmetic.displayName());
    }

    private static Optional<PreviewEntry> entry(int stateId) {
        PreviewEntry entry = STATE_PREVIEWS.get(stateId);
        if (entry == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() - entry.createdAt() > TTL_MS) {
            STATE_PREVIEWS.remove(stateId, entry);
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    private static void cleanup() {
        long cutoff = System.currentTimeMillis() - TTL_MS;
        STATE_PREVIEWS.entrySet().removeIf(entry -> entry.getValue().createdAt() < cutoff);
        if (STATE_PREVIEWS.size() > 256) {
            STATE_PREVIEWS.clear();
        }
    }

    private record PreviewEntry(Cosmetic cosmetic, boolean hideBasePlayer, long createdAt) {
    }
}
