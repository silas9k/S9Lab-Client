package site.s9lab.s9labclient.client.emote;

import net.minecraft.util.Identifier;

/** Optional GeckoLib object rendered together with an emote. */
public record EmotePropDefinition(
        Identifier model,
        Identifier texture,
        Identifier animation,
        String animationName,
        float scale,
        float offsetX,
        float offsetY,
        float offsetZ,
        float rotationX,
        float rotationY,
        float rotationZ
) {
}
