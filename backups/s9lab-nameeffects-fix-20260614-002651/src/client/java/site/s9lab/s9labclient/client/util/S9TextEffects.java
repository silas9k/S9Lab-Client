package site.s9lab.s9labclient.client.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class S9TextEffects {
    public static final List<String> EFFECT_IDS = List.of(
            "rainbow",
            "wave",
            "shake",
            "spin",
            "bounce",
            "pulse",
            "color_white",
            "color_red",
            "color_orange",
            "color_yellow",
            "color_green",
            "color_cyan",
            "color_blue",
            "color_purple",
            "color_pink"
    );

    private S9TextEffects() {
    }

    public static List<String> normalize(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        Set<String> unique = new LinkedHashSet<>();
        boolean hasColor = false;

        for (String item : raw) {
            String id = item == null ? "" : item.trim().toLowerCase(Locale.ROOT);
            if (id.isBlank() || id.equals("none") || !EFFECT_IDS.contains(id)) {
                continue;
            }

            if (id.startsWith("color_")) {
                if (hasColor) {
                    continue;
                }
                hasColor = true;
            }

            unique.add(id);
            if (unique.size() == 3) {
                break;
            }
        }

        return List.copyOf(unique);
    }

    /**
     * Uses only trigger colors already defined by the original Text Effects pack.
     * The shader configuration itself is never generated or modified.
     */
    public static int triggerColor(List<String> raw) {
        List<String> effects = normalize(raw);

        boolean rainbow = effects.contains("rainbow");
        boolean wave = effects.contains("wave");
        boolean bounce = effects.contains("bounce");

        if (rainbow && wave) {
            return 0xF8F898;
        }
        if (rainbow && bounce) {
            return 0xF8F89C;
        }
        if (effects.contains("shake")) {
            return 0xF8F854;
        }
        if (effects.contains("spin")) {
            return 0xF8F86C;
        }
        if (bounce) {
            return 0xF8F864;
        }
        if (wave) {
            return 0xF8F860;
        }
        if (effects.contains("pulse")) {
            return 0xF8F880;
        }
        if (rainbow) {
            return 0xF8F85C;
        }

        return selectedPlainColor(effects);
    }

    private static int selectedPlainColor(List<String> effects) {
        if (effects.contains("color_red")) return 0xFF5050;
        if (effects.contains("color_orange")) return 0xFFA040;
        if (effects.contains("color_yellow")) return 0xFFEB50;
        if (effects.contains("color_green")) return 0x50FF78;
        if (effects.contains("color_cyan")) return 0x50EBFF;
        if (effects.contains("color_blue")) return 0x5082FF;
        if (effects.contains("color_purple")) return 0xB45AFF;
        if (effects.contains("color_pink")) return 0xFF5ABE;
        return 0xFFFFFF;
    }
}
