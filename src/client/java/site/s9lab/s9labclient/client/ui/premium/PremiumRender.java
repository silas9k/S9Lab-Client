package site.s9lab.s9labclient.client.ui.premium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public final class PremiumRender {
    public static final int SHADOW_OFFSET = 1;
    public static final int MIN_RADIUS = 0;
    public static final int SHOP_GLASS = 0x66DDEBFF;
    public static final int SHOP_HEADER = 0xF0121316;
    public static final int SHOP_FOOTER = 0xF0121316;
    public static final int SHOP_BORDER = 0x88FFFFFF;
    public static final int SHOP_SOFT_BORDER = 0x44FFFFFF;
    public static final int SHOP_CARD = 0x24FFFFFF;
    public static final int SHOP_CARD_HOVER = 0x40364A66;
    public static final int SHOP_CARD_ACTIVE = 0x4A2E66C8;
    public static final int SHOP_BUTTON = 0xFF17191D;
    public static final int SHOP_BUTTON_HOVER = 0xFF25282E;
    public static final int SHOP_BUTTON_ACTIVE = 0xFF202736;
    public static final int SHOP_INPUT = 0xEF07080A;

    private PremiumRender() {
    }

    public static void overlay(DrawContext context, ClientTheme theme) {
        if (theme.darkOverlay()) {
            shopBackdrop(context);
        }
    }

    public static void card(DrawContext context, int x, int y, int width, int height, int radius, int color, int borderColor) {
        shadow(context, x, y, width, height, 0);
        roundedRect(context, x, y, width, height, 0, color);
        outline(context, x, y, width, height, 0, borderColor);
    }

    public static void shopBackdrop(DrawContext context) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, 0x16000000);
        context.fill(0, 0, width, Math.max(1, height / 2), 0x22D8E8FF);
        context.fill(0, Math.max(0, height - 96), width, height, 0x44000000);
    }

    public static void shopPanel(DrawContext context, int x, int y, int width, int height, int topbarHeight, int footerHeight) {
        shadow(context, x, y, width, height, 0);
        context.fill(x, y, x + width, y + height, SHOP_GLASS);
        outline(context, x, y, width, height, 0, SHOP_BORDER);
        if (topbarHeight > 0) {
            context.fill(x, y, x + width, y + Math.min(height, topbarHeight), SHOP_HEADER);
            context.fill(x, y + Math.min(height, topbarHeight) - 1, x + width, y + Math.min(height, topbarHeight), SHOP_SOFT_BORDER);
        }
        if (footerHeight > 0) {
            int footerY = Math.max(y, y + height - footerHeight);
            context.fill(x, footerY, x + width, y + height, SHOP_FOOTER);
            context.fill(x, footerY, x + width, footerY + 1, SHOP_SOFT_BORDER);
        }
    }

    public static void shopButton(DrawContext context, Text text, int x, int y, int width, int height, boolean active, boolean hovered, int accentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        int background = active ? SHOP_BUTTON_ACTIVE : hovered ? SHOP_BUTTON_HOVER : SHOP_BUTTON;
        int border = active ? accentColor : hovered ? 0xAAFFFFFF : 0x667A7F88;
        context.fill(x, y, x + width, y + height, background);
        outline(context, x, y, width, height, 0, border);
        centeredText(context, text, x + width / 2, y + (height - client.textRenderer.fontHeight) / 2, active ? accentColor : 0xFFE7EAF2);
    }

    public static void shopInput(DrawContext context, int x, int y, int width, int height, boolean focused, int accentColor) {
        context.fill(x, y, x + width, y + height, SHOP_INPUT);
        outline(context, x, y, width, height, 0, focused ? accentColor : 0xAA7A7F88);
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
