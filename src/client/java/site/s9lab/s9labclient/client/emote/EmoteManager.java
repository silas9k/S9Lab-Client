package site.s9lab.s9labclient.client.emote;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendClient;

public final class EmoteManager {
    private static Emote activeEmote;
    private static int remainingTicks;
    private static int elapsedTicks;

    private EmoteManager() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) {
            stop();
            return;
        }

        if (remainingTicks > 0) {
            remainingTicks--;
            elapsedTicks++;
        } else {
            if (activeEmote != null) {
                BackendClient.sendEmoteStop();
            }
            activeEmote = null;
            elapsedTicks = 0;
        }
    }

    public static boolean play(Emote emote) {
        if (emote == null) {
            return false;
        }
        if (!isOwned(emote)) {
            return false;
        }
        activeEmote = emote;
        remainingTicks = emote.durationTicks();
        elapsedTicks = 0;
        BackendClient.sendEmoteStart(emote.id());
        return true;
    }

    public static boolean play(String idOrDisplayName) {
        Emote emote = byIdOrName(idOrDisplayName);
        if (emote == null) {
            return false;
        }
        return play(emote);
    }

    public static void stop() {
        boolean hadEmote = activeEmote != null;
        activeEmote = null;
        remainingTicks = 0;
        elapsedTicks = 0;
        if (hadEmote) {
            BackendClient.sendEmoteStop();
        }
    }

    public static boolean isActive() {
        return activeEmote != null && remainingTicks > 0;
    }

    public static Emote activeEmote() {
        return activeEmote;
    }

    public static float wave(float age, float speed) {
        return (float) Math.sin(age * speed);
    }

    public static float progress() {
        if (activeEmote == null) {
            return 0.0F;
        }
        return Math.min(1.0F, elapsedTicks / (float) activeEmote.durationTicks());
    }

    /**
     * Only these emotes are exposed to the wheel/commands for now.
     * Old enum constants stay below so other render/mixin code does not break.
     */
    public static List<Emote> all() {
        return List.of(Emote.T_POSE, Emote.GRIDDY, Emote.BIG_HEAD, Emote.BILLY_BOUNCE);
    }

    public static List<Emote> bindable() {
        return all().stream().filter(EmoteManager::isOwned).toList();
    }

    public static List<String> ids() {
        return all().stream().map(Emote::id).toList();
    }

    public static Emote byIdOrName(String value) {
        String normalized = normalize(value);
        for (Emote emote : Emote.values()) {
            if (emote.id().equals(normalized) || normalize(emote.displayName()).equals(normalized)) {
                return emote;
            }
        }
        return null;
    }

    public static boolean isBindable(Emote emote) {
        return all().contains(emote) && isOwned(emote);
    }

    public static boolean isOwned(Emote emote) {
        return emote != null
                && S9LabClientClient.getConfigManager() != null
                && S9LabClientClient.getConfigManager().isUnlocked(cosmeticId(emote));
    }

    public static String cosmeticId(Emote emote) {
        return emote == null ? "" : "s9lab_emote_" + emote.id();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    public enum Emote {
        T_POSE("t_pose", "T-Pose", "Chin up, arms straight!", 20 * 5, 0xFFE8E8E8),
        GRIDDY("griddy", "Griddy", "Get Griddy.", 20 * 8, 0xFF7EA1FF),
        BIG_HEAD("big_head", "Big Head", "Big head pose.", 20 * 8, 0xFF7EA1FF),
        BILLY_BOUNCE("billy_bounce", "Billy Bounce", "Bouncy dance move.", 20 * 8, 0xFF7EA1FF),


        // Kept for compatibility with existing renderer/mixin references.
        LIGHTNING_WAVE("lightning_wave", "Lightning Wave", "Fast hello with S9 energy.", 20 * 7, 0xFF7EA1FF),
        SPIN_FLEX("spin_flex", "Spin Flex", "Confident flex pose.", 20 * 7, 0xFFFFD166),
        CAPE_BOW("cape_bow", "Cape Bow", "Small respectful bow.", 20 * 6, 0xFF8BD3FF),
        DRAGON_FLAP("dragon_flap", "Dragon Flap", "Wing-friendly arm flap.", 20 * 7, 0xFFB388FF),
        DAB("dab", "S9 Dab", "Classic quick dab.", 20 * 5, 0xFFFF6B6B),
        ROBOT("robot", "Robot", "Sharp mechanical moves.", 20 * 8, 0xFF77E6C6),
        CHILL_BOUNCE("chill_bounce", "Chill Bounce", "Relaxed idle dance.", 20 * 9, 0xFF55D66B),
        SKY_POINT("sky_point", "Sky Point", "Hero pose upwards.", 20 * 6, 0xFFFF9F45),
        HEART_BEAT("heart_beat", "Heart Beat", "Soft cute pulse pose.", 20 * 7, 0xFFFF7AC8);

        private final String id;
        private final String displayName;
        private final String description;
        private final int durationTicks;
        private final int accentColor;

        Emote(String id, String displayName, String description, int durationTicks, int accentColor) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.durationTicks = durationTicks;
            this.accentColor = accentColor;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public String description() {
            return description;
        }

        public int durationTicks() {
            return durationTicks;
        }

        public int accentColor() {
            return accentColor;
        }
    }
}
