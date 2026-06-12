package site.s9lab.s9labclient.client.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

public final class S9BadgeText {
    private S9BadgeText() {
    }

    public static MutableText withBadge(Text originalName) {
        MutableText result = Text.empty();
        result.append(Text.literal("\uE000")
                .styled(style -> style
                        .withFont(new StyleSpriteSource.Font(Identifier.of(S9LabClient.MOD_ID, "icons")))
                        .withShadowColor(0x00000000)));
        result.append(Text.literal(" "));
        result.append(originalName.copy());
        return result;
    }

    public static MutableText withoutBadge() {
        MutableText result = Text.empty();
        result.append(Text.literal("\uE000")
                .styled(style -> style
                        .withFont(new StyleSpriteSource.Font(Identifier.of(S9LabClient.MOD_ID, "icons")))
                        .withShadowColor(0x00000000)));
        result.append(Text.literal(" "));
        return result;
    }
}
