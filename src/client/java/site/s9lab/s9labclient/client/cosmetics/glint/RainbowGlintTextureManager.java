package site.s9lab.s9labclient.client.cosmetics.glint;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

public final class RainbowGlintTextureManager {
    private static final Identifier TEXTURE =
            Identifier.of(S9LabClient.MOD_ID, "dynamic/rainbow_body_glint");

    private static NativeImageBackedTexture texture;
    private static float lastAge = Float.MIN_VALUE;

    // How often (in age units) the texture is regenerated.
    // Higher = less CPU cost, slightly choppier animation.
    private static final float UPDATE_INTERVAL = 0.5F;

    private RainbowGlintTextureManager() {}

    public static Identifier get(float age) {
        // Snap age to intervals so we only rebuild when it actually changes visually
        float snapped = (float) Math.floor(age / UPDATE_INTERVAL) * UPDATE_INTERVAL;

        if (texture != null && snapped == lastAge) {
            return TEXTURE;
        }
        lastAge = snapped;

        NativeImage image = new NativeImage(128, 128, false);

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                // Diagonal sweep across the entire body texture
                // x * 0.007 + y * 0.007 gives a 45° band that covers the whole UV space
                // age * 0.004 makes the animation ~4.5× slower than before
                float wave = (x * 0.007F) + (y * 0.007F) + (snapped * 0.004F);
                float hue = wave - (float) Math.floor(wave);

                // Full saturation and brightness for vivid clean rainbow
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0xFFFFFF;

                // overlay strength of 50 (out of 255) to make it more subtle and less distracting
                int alpha = 50;
                image.setColorArgb(x, y, (alpha << 24) | rgb);
            }
        }

        if (texture != null) {
            texture.close();
        }

        texture = new NativeImageBackedTexture(() -> "S9Lab Rainbow Body Glint", image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(TEXTURE, texture);

        return TEXTURE;
    }
}