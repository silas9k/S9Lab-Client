package site.s9lab.s9labclient.client.cosmetics.wings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import site.s9lab.s9labclient.S9LabClient;

public final class BlueEnergyWingTextureManager {
    private static final Identifier ENERGY_TEXTURE = Identifier.of(S9LabClient.MOD_ID, "dynamic/blue_energy_wings");
    private static final int PRIMARY = 0x34CFFF;
    private static final int SECONDARY = 0x0B5CFF;
    private static final int GLOW = 0x7FE7FF;
    private static long lastFrame = Long.MIN_VALUE;
    private static NativeImageBackedTexture texture;

    private BlueEnergyWingTextureManager() {
    }

    public static Identifier getTexture(float age, Identifier baseTexture) {
        long frame = (long) (age / 4.0F);
        if (frame == lastFrame && texture != null) {
            return ENERGY_TEXTURE;
        }
        NativeImage image = createTexture(age, baseTexture);
        if (image == null) {
            return baseTexture;
        }
        lastFrame = frame;
        if (texture != null) {
            texture.close();
        }
        texture = new NativeImageBackedTexture(() -> "S9Lab Blue Energy Wings", image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(ENERGY_TEXTURE, texture);
        return ENERGY_TEXTURE;
    }

    private static NativeImage createTexture(float age, Identifier baseTexture) {
        MinecraftClient client = MinecraftClient.getInstance();
        Optional<Resource> resource = client.getResourceManager().getResource(baseTexture);
        if (resource.isEmpty()) {
            return null;
        }

        try (InputStream stream = resource.get().getInputStream()) {
            NativeImage original = NativeImage.read(stream);
            NativeImage output = new NativeImage(original.getWidth(), original.getHeight(), false);
            float pulse = 0.88F + MathHelper.sin(age * 0.12F) * 0.12F;

            for (int y = 0; y < original.getHeight(); y++) {
                float gradient = y / (float) Math.max(1, original.getHeight() - 1);
                int tint = lerp(PRIMARY, SECONDARY, gradient * 0.72F);

                for (int x = 0; x < original.getWidth(); x++) {
                    int argb = original.getColorArgb(x, y);
                    int alpha = (argb >>> 24) & 255;
                    if (alpha <= 5) {
                        output.setColorArgb(x, y, 0x00000000);
                        continue;
                    }

                    int r = (argb >> 16) & 255;
                    int g = (argb >> 8) & 255;
                    int b = argb & 255;
                    float brightness = Math.max(0.22F, (r + g + b) / 765.0F);
                    float glowMix = MathHelper.clamp(brightness * 0.42F + pulse * 0.10F, 0.0F, 0.55F);
                    int color = lerp(tint, GLOW, glowMix);
                    int energy = multiply(color, MathHelper.clamp(0.72F + brightness * 0.55F, 0.0F, 1.18F) * pulse);
                    int softAlpha = Math.max(96, Math.min(230, (int) (alpha * (0.76F + brightness * 0.18F))));

                    output.setColorArgb(x, y, (softAlpha << 24) | energy);
                }
            }

            original.close();
            return output;
        } catch (IOException exception) {
            S9LabClient.LOGGER.warn("Failed to create blue energy wing texture", exception);
            return null;
        }
    }

    private static int lerp(int a, int b, float t) {
        float clamped = MathHelper.clamp(t, 0.0F, 1.0F);
        int ar = (a >> 16) & 255;
        int ag = (a >> 8) & 255;
        int ab = a & 255;
        int br = (b >> 16) & 255;
        int bg = (b >> 8) & 255;
        int bb = b & 255;
        return (channel(ar, br, clamped) << 16) | (channel(ag, bg, clamped) << 8) | channel(ab, bb, clamped);
    }

    private static int multiply(int color, float factor) {
        return (clamp((int) (((color >> 16) & 255) * factor)) << 16)
                | (clamp((int) (((color >> 8) & 255) * factor)) << 8)
                | clamp((int) ((color & 255) * factor));
    }

    private static int channel(int a, int b, float t) {
        return clamp((int) (a + (b - a) * t));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
