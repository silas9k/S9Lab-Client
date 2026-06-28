package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

final class ColorWheelTexture {
    static final Identifier ID = Identifier.of(S9LabClient.MOD_ID, "dynamic/ui/color_wheel");
    static final int SIZE = 128;
    private static boolean registered;

    private ColorWheelTexture() {
    }

    static void ensureRegistered() {
        if (registered) return;
        NativeImage image = new NativeImage(SIZE, SIZE, false);
        float center = (SIZE - 1) / 2.0F;
        float radius = center - 1.0F;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                float dx = x - center;
                float dy = center - y;
                float saturation = (float) Math.sqrt(dx * dx + dy * dy) / radius;
                if (saturation > 1.0F) {
                    image.setColorArgb(x, y, 0);
                    continue;
                }
                float hue = (float) (Math.atan2(dy, dx) / (Math.PI * 2.0));
                if (hue < 0.0F) hue += 1.0F;
                int rgb = java.awt.Color.HSBtoRGB(hue, saturation, 1.0F) & 0xFFFFFF;
                image.setColorArgb(x, y, 0xFF000000 | rgb);
            }
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(
                ID,
                new NativeImageBackedTexture(() -> "S9Lab Color Wheel", image)
        );
        registered = true;
    }
}
