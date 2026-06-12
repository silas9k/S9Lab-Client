package site.s9lab.s9labclient.client.cosmetics.wings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class WingColorTextureManager {
    private static final Identifier COLOR_TEXTURE =
            Identifier.of(S9LabClient.MOD_ID, "dynamic/color_dragon_wings");

    private static String lastKey = "";
    private static NativeImageBackedTexture texture;

    private WingColorTextureManager() {
    }

    public static Identifier getTexture(Module module, float age, Identifier normalTexture) {
        String mode = mode(module, "Color Mode", "Normal");

        if ("Normal".equalsIgnoreCase(mode)) {
            return normalTexture;
        }

        String key = buildKey(module, mode, age);

        if (key.equals(lastKey) && texture != null) {
            return COLOR_TEXTURE;
        }

        lastKey = key;

        NativeImage image = createColoredDragonTexture(module, mode, age, normalTexture);
        if (image == null) {
            return normalTexture;
        }

        if (texture != null) {
            texture.close();
        }

        texture = new NativeImageBackedTexture(() -> "S9Lab Color Dragon Wings", image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(COLOR_TEXTURE, texture);

        return COLOR_TEXTURE;
    }

    private static NativeImage createColoredDragonTexture(Module module, String mode, float age, Identifier normalTexture) {
        MinecraftClient client = MinecraftClient.getInstance();
        Optional<Resource> resource = client.getResourceManager().getResource(normalTexture);

        if (resource.isEmpty()) {
            return null;
        }

        try (InputStream stream = resource.get().getInputStream()) {
            NativeImage original = NativeImage.read(stream);
            NativeImage output = new NativeImage(original.getWidth(), original.getHeight(), false);

            int color1 = rgb(
                    number(module, "Color 1 Red", 120.0D),
                    number(module, "Color 1 Green", 0.0D),
                    number(module, "Color 1 Blue", 255.0D)
            );

            int color2 = rgb(
                    number(module, "Color 2 Red", 255.0D),
                    number(module, "Color 2 Green", 0.0D),
                    number(module, "Color 2 Blue", 80.0D)
            );

            for (int y = 0; y < original.getHeight(); y++) {
                float gradientT = y / (float) Math.max(1, original.getHeight() - 1);

                for (int x = 0; x < original.getWidth(); x++) {
                    int argb = original.getColorArgb(x, y);
                    int alpha = (argb >>> 24) & 255;

                    if (alpha <= 5) {
                        output.setColorArgb(x, y, 0x00000000);
                        continue;
                    }

                    int tint;

                    if ("Rainbow".equalsIgnoreCase(mode)) {
                        float speed = (float) number(module, "Rainbow Speed", 1.0D);
                        float saturation = (float) number(module, "Rainbow Saturation", 1.0D);
                        float rainbowBrightness = (float) number(module, "Rainbow Brightness", 1.0D);

                        float wave = ((x / (float) original.getWidth()) * 0.55F)
                                + ((y / (float) original.getHeight()) * 0.35F)
                                + (age * 0.018F * speed);

                        float hue = wave - (float) Math.floor(wave);
                        tint = hsv(hue, saturation, rainbowBrightness);
                    } else {
                        tint = lerp(color1, color2, gradientT);
                    }

                    int r = (argb >> 16) & 255;
                    int g = (argb >> 8) & 255;
                    int b = argb & 255;

                    float brightness = Math.max(0.15F, (r + g + b) / 765.0F);
                    int colored = multiplyBrightness(tint, brightness);

                    output.setColorArgb(x, y, (alpha << 24) | colored);
                }
            }

            original.close();
            return output;
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static String buildKey(Module module, String mode, float age) {
        if ("Rainbow".equalsIgnoreCase(mode)) {
            long frame = (long) age;
            return mode + ":" + frame
                    + ":" + number(module, "Rainbow Speed", 1.0D)
                    + ":" + number(module, "Rainbow Saturation", 1.0D)
                    + ":" + number(module, "Rainbow Brightness", 1.0D);
        }

        return mode
                + ":" + number(module, "Color 1 Red", 120.0D)
                + ":" + number(module, "Color 1 Green", 0.0D)
                + ":" + number(module, "Color 1 Blue", 255.0D)
                + ":" + number(module, "Color 2 Red", 255.0D)
                + ":" + number(module, "Color 2 Green", 0.0D)
                + ":" + number(module, "Color 2 Blue", 80.0D);
    }

    private static int rgb(double r, double g, double b) {
        return (clamp((int) Math.round(r)) << 16)
                | (clamp((int) Math.round(g)) << 8)
                | clamp((int) Math.round(b));
    }

    private static int hsv(float h, float s, float v) {
        return java.awt.Color.HSBtoRGB(h, s, v) & 0xFFFFFF;
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 255;
        int ag = (a >> 8) & 255;
        int ab = a & 255;

        int br = (b >> 16) & 255;
        int bg = (b >> 8) & 255;
        int bb = b & 255;

        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);

        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
    }

    private static int multiplyBrightness(int color, float brightness) {
        int r = (int) (((color >> 16) & 255) * brightness);
        int g = (int) (((color >> 8) & 255) * brightness);
        int b = (int) ((color & 255) * brightness);

        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static String mode(Module module, String name, String fallback) {
        if (module == null) {
            return fallback;
        }

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof ModeSetting modeSetting && setting.getName().equalsIgnoreCase(name)) {
                return modeSetting.getValue();
            }
        }

        return fallback;
    }

    private static double number(Module module, String name, double fallback) {
        if (module == null) {
            return fallback;
        }

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting numberSetting && setting.getName().equalsIgnoreCase(name)) {
                return numberSetting.getValue();
            }
        }

        return fallback;
    }
}