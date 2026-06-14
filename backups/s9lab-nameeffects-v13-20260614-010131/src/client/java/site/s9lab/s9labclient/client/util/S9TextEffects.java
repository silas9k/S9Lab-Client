package site.s9lab.s9labclient.client.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class S9TextEffects {
    public static final List<String> EFFECT_IDS = List.of(
            "shake", "wave", "rainbow", "bounce", "blink", "pulse",
            "spin", "sequential_spin", "fade", "iterate", "glitch",
            "scale", "offset", "gradient", "dynamic_gradient_red_blue",
            "dynamic_gradient_green_yellow", "lava",
            "color_white", "color_red", "color_orange", "color_yellow",
            "color_green", "color_cyan", "color_blue", "color_purple", "color_pink"
    );

    public static final List<String> ANIMATION_IDS = List.of(
            "shake", "wave", "rainbow", "bounce", "blink", "pulse",
            "spin", "sequential_spin", "fade", "iterate", "glitch",
            "scale", "offset", "gradient", "dynamic_gradient_red_blue",
            "dynamic_gradient_green_yellow", "lava"
    );

    public static final List<String> COLOR_IDS = List.of(
            "color_white", "color_red", "color_orange", "color_yellow",
            "color_green", "color_cyan", "color_blue", "color_purple", "color_pink"
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

    public static String displayName(String id) {
        return switch (id) {
            case "shake" -> "SHAKING";
            case "wave" -> "WAVING";
            case "rainbow" -> "RAINBOW";
            case "bounce" -> "BOUNCING";
            case "blink" -> "BLINKING";
            case "pulse" -> "PULSING";
            case "spin" -> "SPINNING";
            case "sequential_spin" -> "SEQUENTIAL SPIN";
            case "fade" -> "FADING";
            case "iterate" -> "ITERATING";
            case "glitch" -> "GLITCH";
            case "scale" -> "GROWING";
            case "offset" -> "FLOATING";
            case "gradient" -> "GRADIENT";
            case "dynamic_gradient_red_blue" -> "RED-BLUE FLOW";
            case "dynamic_gradient_green_yellow" -> "GREEN-GOLD FLOW";
            case "lava" -> "LAVA";
            case "color_white" -> "WHITE";
            case "color_red" -> "RED";
            case "color_orange" -> "ORANGE";
            case "color_yellow" -> "YELLOW";
            case "color_green" -> "GREEN";
            case "color_cyan" -> "CYAN";
            case "color_blue" -> "BLUE";
            case "color_purple" -> "PURPLE";
            case "color_pink" -> "PINK";
            default -> id.toUpperCase(Locale.ROOT);
        };
    }

    /** Exact trigger markers from the bundled S9Lab Client Resourcepack. */
    public static int triggerColor(List<String> raw) {
        List<String> effects = normalize(raw);
        boolean rainbow = effects.contains("rainbow");
        boolean wave = effects.contains("wave");
        boolean bounce = effects.contains("bounce");

        if (rainbow && wave) return 0xFCFC98;
        if (rainbow && bounce) return 0xFCFC9C;
        if (effects.contains("shake")) return 0xFCFC54;
        if (wave) return 0xFCFC58;
        if (rainbow) return 0xFCFC5C;
        if (bounce) return 0xFCFC60;
        if (effects.contains("blink")) return 0xFCFC64;
        if (effects.contains("pulse")) return 0xFCFC68;
        if (effects.contains("spin")) return 0xFCFC6C;
        if (effects.contains("sequential_spin")) return 0xFCFC70;
        if (effects.contains("fade")) return 0xFCFC74;
        if (effects.contains("iterate")) return 0xFCFC78;
        if (effects.contains("glitch")) return 0xFCFC7C;
        if (effects.contains("scale")) return 0xFCFC80;
        if (effects.contains("offset")) return 0xFCFC84;
        if (effects.contains("gradient")) return 0xFCFC88;
        if (effects.contains("dynamic_gradient_red_blue")) return 0xFCFC8C;
        if (effects.contains("dynamic_gradient_green_yellow")) return 0xFCFC90;
        if (effects.contains("lava")) return 0xFCFC94;
        return selectedPlainColor(effects);
    }

    public static int previewColor(String id) {
        return switch (id) {
            case "rainbow" -> 0xFFFF4DF3;
            case "shake", "glitch", "color_red" -> 0xFFFF5A5A;
            case "bounce", "offset", "color_orange" -> 0xFFFFAA40;
            case "fade", "gradient", "dynamic_gradient_green_yellow", "color_yellow" -> 0xFFFFEB50;
            case "wave", "color_green" -> 0xFF50FF78;
            case "pulse", "scale", "color_cyan" -> 0xFF50EBFF;
            case "blink", "dynamic_gradient_red_blue", "color_blue" -> 0xFF5082FF;
            case "iterate", "color_purple" -> 0xFFB45AFF;
            case "spin", "color_pink" -> 0xFFFF5ABE;
            case "lava" -> 0xFFFF6A32;
            default -> 0xFFFFFFFF;
        };
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
