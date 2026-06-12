package site.s9lab.s9labclient.client.ui;

import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.emote.EmoteManager.Emote;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class EmoteWheelScreen extends ResponsiveScreen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE7EAF2;
    private static final int MUTED = 0xFF9AA1B2;
    private static final int DIM = 0xFF687083;
    private static final int PANEL = 0xE914161A;
    private static final int CARD = 0xB9141822;
    private static final int CARD_HOVER = 0xD91A2030;
    private static final int LINE = 0x662C3344;

    private final Screen parent;

    public EmoteWheelScreen(Screen parent) {
        super(Text.literal("S9Lab Emotes"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();

        context.fill(0, 0, width, height, 0x77000000);
        int panelW = Math.min(640, Math.max(310, width - 42));
        int panelH = Math.min(420, Math.max(220, height - 42));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        context.fill(x + 3, y + 3, x + panelW + 3, y + panelH + 3, 0x77000000);
        PremiumRender.roundedRect(context, x, y, panelW, panelH, 2, PANEL);
        PremiumRender.outline(context, x, y, panelW, panelH, 2, 0xFF2D3138);
        context.fill(x, y, x + panelW, y + 46, 0xDD1A1C21);

        context.drawTextWithShadow(textRenderer, Text.literal("EMOTE WHEEL"), x + 18, y + 15, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("choose an emote"), x + panelW - 18 - textRenderer.getWidth("choose an emote"), y + 15, MUTED);

        int centerX = x + panelW / 2;
        int centerY = y + 48 + (panelH - 92) / 2;
        int radius = Math.min(Math.min(panelW, panelH) / 3, 132);
        List<Emote> emotes = EmoteManager.all();
        Emote hovered = hoveredEmote(mouseX, mouseY, centerX, centerY, radius, emotes);

        PremiumRender.roundedRect(context, centerX - radius - 60, centerY - radius - 60, (radius + 60) * 2, (radius + 60) * 2, 2, 0x33000000);
        PremiumRender.outline(context, centerX - radius - 60, centerY - radius - 60, (radius + 60) * 2, (radius + 60) * 2, 2, LINE);

        for (int i = 0; i < emotes.size(); i++) {
            renderSlot(context, emotes.get(i), i, emotes.size(), centerX, centerY, radius, emotes.get(i) == hovered, accent);
        }

        boolean stopHovered = inside(mouseX, mouseY, centerX - 42, centerY - 18, 84, 36);
        PremiumRender.roundedRect(context, centerX - 48, centerY - 48, 96, 96, 2, stopHovered ? 0xFF20141A : 0xEE0D1017);
        PremiumRender.outline(context, centerX - 48, centerY - 48, 96, 96, 2, stopHovered ? 0xFFFF5A67 : accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("STOP"), centerX, centerY - 7, stopHovered ? 0xFFFF9CA3 : WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("active emote"), centerX, centerY + 8, DIM);

        Emote shown = hovered != null ? hovered : EmoteManager.activeEmote();
        String name = shown == null ? "Hover an emote" : shown.displayName();
        String desc = shown == null ? "Click a slot to play it" : shown.description();
        int infoW = Math.min(360, panelW - 36);
        int infoX = x + (panelW - infoW) / 2;
        int infoY = y + panelH - 34;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(name), centerX, infoY - 14, TEXT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(desc), centerX, infoY, MUTED);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int panelW = Math.min(640, Math.max(310, width - 42));
        int panelH = Math.min(420, Math.max(220, height - 42));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int centerX = x + panelW / 2;
        int centerY = y + 48 + (panelH - 92) / 2;

        if (inside(mouseX, mouseY, centerX - 48, centerY - 48, 96, 96)) {
            EmoteManager.stop();
            close();
            return true;
        }

        Emote hovered = hoveredEmote(mouseX, mouseY, centerX, centerY, Math.min(Math.min(panelW, panelH) / 3, 132), EmoteManager.all());
        if (hovered != null) {
            EmoteManager.play(hovered);
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    private void renderSlot(DrawContext context, Emote emote, int index, int count, int centerX, int centerY, int radius, boolean hovered, int accent) {
        double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / Math.max(1, count);
        int w = width < 430 ? 82 : 108;
        int h = 42;
        int x = clamp(centerX + (int) Math.round(Math.cos(angle) * radius) - w / 2, 8, Math.max(8, width - w - 8));
        int y = clamp(centerY + (int) Math.round(Math.sin(angle) * radius) - h / 2, 8, Math.max(8, height - h - 8));
        boolean active = emote == EmoteManager.activeEmote();
        int border = active ? 0xFF49F26F : hovered ? emote.accentColor() : LINE;
        PremiumRender.roundedRect(context, x, y, w, h, 2, hovered ? CARD_HOVER : CARD);
        PremiumRender.outline(context, x, y, w, h, 2, border);
        context.fill(x + 8, y + 8, x + 28, y + 28, emote.accentColor());
        context.drawTextWithShadow(textRenderer, Text.literal(trim(emote.displayName(), w < 96 ? 8 : 12)), x + 36, y + 8, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(active ? "ACTIVE" : "PLAY"), x + 36, y + 22, active ? 0xFF49F26F : DIM);
    }

    private Emote hoveredEmote(int mouseX, int mouseY, int centerX, int centerY, int radius, List<Emote> emotes) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 56 || distance > radius + 72 || emotes.isEmpty()) return null;
        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
        while (angle < 0.0D) angle += Math.PI * 2.0D;
        double segment = Math.PI * 2.0D / emotes.size();
        int index = (int) Math.floor((angle + segment / 2.0D) / segment) % emotes.size();
        return emotes.get(index);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        return text == null ? "" : text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
