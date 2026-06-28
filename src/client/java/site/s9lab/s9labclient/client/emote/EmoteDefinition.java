package site.s9lab.s9labclient.client.emote;

import net.minecraft.util.Identifier;

public record EmoteDefinition(
        String id,
        String displayName,
        String description,
        String category,
        Identifier animationFile,
        String animationName,
        int durationTicks,
        int accentColor,
        boolean loop,
        float headScale,
        boolean headOnly,
        EmotePropDefinition prop
) {
    public String cosmeticId() {
        return "s9lab_emote_" + id;
    }
}
