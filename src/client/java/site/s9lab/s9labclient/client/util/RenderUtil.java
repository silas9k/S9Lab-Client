package site.s9lab.s9labclient.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class RenderUtil {
    private RenderUtil() {
    }

    public static void drawText(DrawContext context, MinecraftClient client, String text, int x, int y, int color) {
        context.drawText(client.textRenderer, text, x, y, color, true);
    }

    public static void drawKey(DrawContext context, MinecraftClient client, String label, int x, int y, int width, int height, boolean active) {
        context.fill(x, y, x + width, y + height, active ? ColorUtil.PANEL_ACTIVE : ColorUtil.PANEL);
        int textWidth = client.textRenderer.getWidth(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;
        context.drawText(client.textRenderer, label, textX, textY, active ? ColorUtil.WHITE : ColorUtil.TEXT_MUTED, true);
    }
}
