package site.s9lab.s9labclient.client.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Shared S9C+ name-effect registry and compact shader marker encoder. */
public final class S9TextEffects {
    public static final List<String> EFFECT_IDS = List.of(
            "shake",
            "wave",
            "rainbow",
            "bounce",
            "blink",
            "pulse",
            "spin",
            "sequential_spin",
            "fade",
            "iterate",
            "glitch",
            "scale",
            "offset",
            "gradient",
            "dynamic_gradient_red_blue",
            "dynamic_gradient_green_yellow",
            "lava"
    );

    private static final int MARKER_RED = 253;
    private static final int RADIX = EFFECT_IDS.size() + 1;

    private S9TextEffects() {
    }

    public static List<String> normalize(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String value : raw) {
            String id = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            if (id.isBlank() || id.equals("none") || !EFFECT_IDS.contains(id)) {
                continue;
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
            case "gradient" -> "GREEN-YELLOW";
            case "dynamic_gradient_red_blue" -> "RED-BLUE FLOW";
            case "dynamic_gradient_green_yellow" -> "GREEN-GOLD FLOW";
            case "lava" -> "LAVA";
            default -> id.toUpperCase(Locale.ROOT);
        };
    }

    /**
     * Encodes up to three effect IDs into the green/blue channels of one marker.
     * The shader decodes this marker at runtime, so no hundreds of GLSL blocks are generated.
     */
    public static int triggerColor(List<String> raw) {
        List<String> selected = normalize(raw);
        if (selected.isEmpty()) {
            return 0xFFFFFF;
        }

        int first = encodedId(selected, 0);
        int second = encodedId(selected, 1);
        int third = encodedId(selected, 2);
        int code = first + second * RADIX + third * RADIX * RADIX;
        return (MARKER_RED << 16) | (code & 0xFFFF);
    }

    public static int previewColor(String id) {
        return switch (id) {
            case "shake", "glitch" -> 0xFFFF5A5A;
            case "wave" -> 0xFF50FF78;
            case "rainbow" -> 0xFFFF4DF3;
            case "bounce", "offset" -> 0xFFFFAA40;
            case "blink", "dynamic_gradient_red_blue" -> 0xFF5082FF;
            case "pulse", "scale" -> 0xFF50EBFF;
            case "spin" -> 0xFFFF5ABE;
            case "sequential_spin" -> 0xFFFFFFFF;
            case "fade", "gradient", "dynamic_gradient_green_yellow" -> 0xFFFFEB50;
            case "iterate" -> 0xFFB45AFF;
            case "lava" -> 0xFFFF6A32;
            default -> 0xFFFFFFFF;
        };
    }

    private static int encodedId(List<String> selected, int slot) {
        if (slot >= selected.size()) {
            return 0;
        }
        return EFFECT_IDS.indexOf(selected.get(slot)) + 1;
    }
}
