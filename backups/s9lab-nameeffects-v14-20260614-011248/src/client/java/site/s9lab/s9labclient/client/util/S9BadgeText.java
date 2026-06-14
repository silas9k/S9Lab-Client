package site.s9lab.s9labclient.client.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
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
        return withBadgeForPlayer(originalName, originalName.getString(), plus, effects);
    }

    /**
     * Adds the badge to the full display text, but applies the shader marker only
     * to the exact player-name portion. Prefixes such as "Supporter |" keep their
     * original formatting and never receive S9C+ effects.
     */
    public static MutableText withBadgeForPlayer(
            Text displayText,
            String playerName,
            boolean plus,
            List<String> effects
    ) {
        MutableText result = badgePrefix(plus);
        List<String> normalized = S9TextEffects.normalize(effects);

        if (!plus || normalized.isEmpty() || playerName == null || playerName.isBlank()) {
            result.append(displayText.copy());
            return result;
        }

        StyledCharacters characters = flatten(displayText);
        int start = characters.value.indexOf(playerName);
        if (start < 0) {
            // Safe fallback: never color an entire rank prefix just because the
            // server changed the display-name format.
            result.append(displayText.copy());
            return result;
        }

        int end = start + playerName.length();
        appendRange(result, characters, 0, start, null);
        appendRange(result, characters, start, end, S9TextEffects.triggerColor(normalized));
        appendRange(result, characters, end, characters.codePoints.size(), null);
        return result;
    }

    public static MutableText withoutBadge() {
        return withoutBadge(false);
    }

    public static MutableText withoutBadge(boolean plus) {
        return badgePrefix(plus);
    }

    private static MutableText badgePrefix(boolean plus) {
        MutableText result = Text.empty();
        result.append(Text.literal(plus ? "\uE001" : "\uE000")
                .styled(style -> style
                        .withFont(new StyleSpriteSource.Font(Identifier.of(S9LabClient.MOD_ID, "icons")))
                        .withShadowColor(0x00000000)));
        result.append(Text.literal(" "));
        return result;
    }

    private static StyledCharacters flatten(Text text) {
        StringBuilder value = new StringBuilder();
        List<Integer> codePoints = new ArrayList<>();
        List<Style> styles = new ArrayList<>();

        text.asOrderedText().accept((index, style, codePoint) -> {
            value.appendCodePoint(codePoint);
            codePoints.add(codePoint);
            styles.add(style);
            return true;
        });

        return new StyledCharacters(value.toString(), codePoints, styles);
    }

    private static void appendRange(
            MutableText target,
            StyledCharacters characters,
            int start,
            int end,
            Integer overrideColor
    ) {
        for (int index = start; index < end && index < characters.codePoints.size(); index++) {
            String character = new String(Character.toChars(characters.codePoints.get(index)));
            Style style = characters.styles.get(index);
            if (overrideColor != null) {
                style = style.withColor(overrideColor);
            }
            target.append(Text.literal(character).setStyle(style));
        }
    }

    private record StyledCharacters(String value, List<Integer> codePoints, List<Style> styles) {
    }
}
