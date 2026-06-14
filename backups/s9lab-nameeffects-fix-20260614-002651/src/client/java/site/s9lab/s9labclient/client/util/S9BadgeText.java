package site.s9lab.s9labclient.client.util;

import java.util.List;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

public final class S9BadgeText {
    private S9BadgeText() {
    }

    public static MutableText withBadge(Text originalName) {
        return withBadge(originalName, false);
    }

    public static MutableText withBadge(Text originalName, boolean plus) {
        return withBadge(originalName, plus, false, false);
    }

    public static MutableText withBadge(Text originalName, boolean plus, boolean rainbowName, boolean animatedName) {
        return withBadge(originalName, plus, rainbowName && plus
                ? (animatedName ? List.of("rainbow", "wave") : List.of("rainbow"))
                : List.of());
    }

    public static MutableText withBadge(Text originalName, boolean plus, List<String> effects) {
        MutableText result = Text.empty();
        result.append(Text.literal(plus ? "\uE001" : "\uE000")
                .styled(style -> style
                        .withFont(new StyleSpriteSource.Font(Identifier.of(S9LabClient.MOD_ID, "icons")))
                        .withShadowColor(0x00000000)));
        result.append(Text.literal(" "));
        List<String> normalized = normalizeEffects(effects);
        if (plus && !normalized.isEmpty()) {
            result.append(shaderEffectText(originalName.getString(), normalized));
        } else {
            result.append(originalName.copy());
        }
        return result;
    }

    public static MutableText withoutBadge() {
        return withoutBadge(false);
    }

    public static MutableText withoutBadge(boolean plus) {
        MutableText result = Text.empty();
        result.append(Text.literal(plus ? "\uE001" : "\uE000")
                .styled(style -> style
                        .withFont(new StyleSpriteSource.Font(Identifier.of(S9LabClient.MOD_ID, "icons")))
                        .withShadowColor(0x00000000)));
        result.append(Text.literal(" "));
        return result;
    }

    private static MutableText shaderEffectText(String value, List<String> effects) {
        MutableText result = Text.empty();
        int color = shaderColor(effects);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            result.append(Text.literal(String.valueOf(character)).styled(style -> style.withColor(color)));
        }
        return result;
    }

    private static int shaderColor(List<String> effects) {
        return S9TextEffects.triggerColor(effects);
    }

    private static List<String> normalizeEffects(List<String> effects) {
        return S9TextEffects.normalize(effects);
    }
}
