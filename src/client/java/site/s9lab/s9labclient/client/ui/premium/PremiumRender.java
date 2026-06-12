package site.s9lab.s9labclient.client.ui.premium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public final class PremiumRender {
    public static final int SHADOW_OFFSET = 1;
    public static final int MIN_RADIUS = 0;

    private PremiumRender() {
    }

    public static void overlay(DrawContext context, ClientTheme theme) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        if (theme.darkOverlay()) {
            context.fill(0, 0, width, height, theme.blurBackground() ? theme.backgroundColor() : 0xA8070810);
            if (theme.blurBackground()) {
                context.fill(0, 0, width, height / 2, 0x221B2A55);
                context.fill(0, height - 110, width, height, 0x77000000);
            }
        }
    }

    public static void card(DrawContext context, int x, int y, int width, int height, int radius, int color, int borderColor) {
        shadow(context, x, y, width, height, radius);
        roundedRect(context, x, y, width, height, radius, color);
        outline(context, x, y, width, height, radius, borderColor);
    }

    public static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(MIN_RADIUS, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        fillCorners(context, x, y, width, height, r, color);
    }

    public static void outline(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(MIN_RADIUS, Math.min(radius, Math.min(width, height) / 2));
        context.fill(x + r, y, x + width - r, y + 1, color);
        context.fill(x + r, y + height - 1, x + width - r, y + height, color);
        context.fill(x, y + r, x + 1, y + height - r, color);
        context.fill(x + width - 1, y + r, x + width, y + height - r, color);
    }

    public static void centeredText(DrawContext context, Text text, int centerX, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawTextWithShadow(client.textRenderer, text, centerX - client.textRenderer.getWidth(text) / 2, y, color);
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    private static void shadow(DrawContext context, int x, int y, int width, int height, int radius) {
        roundedRect(context, x + SHADOW_OFFSET, y + SHADOW_OFFSET, width, height, radius, 0x66000000);
    }

    private static void fillCorners(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            context.fill(x + inset, y + row, x + radius, y + row + 1, color);
            context.fill(x + width - radius, y + row, x + width - inset, y + row + 1, color);
            context.fill(x + inset, y + height - row - 1, x + radius, y + height - row, color);
            context.fill(x + width - radius, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    private static int cornerInset(int radius, int row) {
        double center = radius - 0.5D;
        double dy = center - row;
        double dx = Math.sqrt(Math.max(0.0D, center * center - dy * dy));
        return Math.max(0, radius - (int) Math.ceil(dx));
    }
}
