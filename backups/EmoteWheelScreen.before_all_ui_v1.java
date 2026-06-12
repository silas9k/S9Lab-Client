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
    private static final int GRAY = 0xFFAAB0C2;
    private static final int DIM = 0xFF73788A;
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
        PremiumRender.overlay(context, theme);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int radius = wheelRadius();
        List<Emote> emotes = EmoteManager.all();
        Emote hovered = hoveredEmote(mouseX, mouseY, centerX, centerY, radius, emotes);

        drawWheelBackdrop(context, centerX, centerY, radius);
        for (int i = 0; i < emotes.size(); i++) {
            renderWheelSlot(context, emotes.get(i), i, emotes.size(), centerX, centerY, radius, emotes.get(i) == hovered);
        }

        boolean stopHovered = inside(mouseX, mouseY, centerX - 36, centerY - 14, 72, 28);
        PremiumRender.card(context, centerX - 42, centerY - 42, 84, 84, theme.radius(), stopHovered ? 0xFF30384D : 0xEE111722, stopHovered ? 0xFFFF6B6B : theme.accentColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("STOP"), centerX, centerY - 5, stopHovered ? 0xFFFF9C9C : WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("center"), centerX, centerY + 8, DIM);

        Emote shown = hovered != null ? hovered : EmoteManager.activeEmote();
        if (shown != null) {
            int infoW = Math.min(300, this.width - 32);
            int infoX = centerX - infoW / 2;
            int infoY = centerY + radius + 56;
            if (infoY + 44 > this.height - 8) {
                infoY = this.height - 52;
            }
            drawPanel(context, infoX, infoY, infoW, 42, 0xE5101118, shown.accentColor());
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(shown.displayName()), centerX, infoY + 9, WHITE);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(shown.description()), centerX, infoY + 23, GRAY);
        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (inside(mouseX, mouseY, centerX - 42, centerY - 42, 84, 84)) {
            EmoteManager.stop();
            close();
            return true;
        }

        int radius = wheelRadius();
        Emote hovered = hoveredEmote(mouseX, mouseY, centerX, centerY, radius, EmoteManager.all());
        if (hovered != null) {
            EmoteManager.play(hovered);
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private void drawWheelBackdrop(DrawContext context, int centerX, int centerY, int radius) {
        int outer = Math.min(radius + 52, Math.max(70, Math.min(this.width, this.height) / 2 - 8));
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, centerX - outer, centerY - outer, outer * 2, outer * 2, theme.radius() + 6, 0x55101520, 0x665E7CE2);
        int inner = 55;
        PremiumRender.card(context, centerX - inner, centerY - inner, inner * 2, inner * 2, theme.radius(), 0xAA070A10, 0xAA3A4058);
    }

    private void renderWheelSlot(DrawContext context, Emote emote, int index, int count, int centerX, int centerY, int radius, boolean hovered) {
        double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / count;
        int slotW = this.width < 430 ? 70 : 92;
        int slotH = this.height < 300 ? 38 : 48;
        int x = clamp(centerX + (int) Math.round(Math.cos(angle) * radius) - slotW / 2, 6, Math.max(6, this.width - slotW - 6));
        int y = clamp(centerY + (int) Math.round(Math.sin(angle) * radius) - slotH / 2, 6, Math.max(6, this.height - slotH - 6));
        boolean active = emote == EmoteManager.activeEmote();
        int border = active ? 0xFF55D66B : hovered ? emote.accentColor() : 0x773A4058;
        drawPanel(context, x, y, slotW, slotH, hovered ? 0xEE263048 : 0xDD141923, border);
        PremiumRender.roundedRect(context, x + 8, y + 9, 21, 21, 6, emote.accentColor());
        PremiumRender.roundedRect(context, x + 12, y + 13, 13, 13, 4, 0xAA000000);
        context.drawTextWithShadow(this.textRenderer, Text.literal(trim(emote.displayName(), slotW < 84 ? 7 : 10)), x + 36, y + 10, WHITE);
        context.drawTextWithShadow(this.textRenderer, Text.literal(active ? "ACTIVE" : "PLAY"), x + 36, y + 25, active ? 0xFF55D66B : DIM);
    }

    private Emote hoveredEmote(int mouseX, int mouseY, int centerX, int centerY, int radius, List<Emote> emotes) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 58 || distance > radius + 66 || emotes.isEmpty()) {
            return null;
        }

        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
        while (angle < 0.0D) {
            angle += Math.PI * 2.0D;
        }
        double segment = Math.PI * 2.0D / emotes.size();
        int index = (int) Math.floor((angle + segment / 2.0D) / segment) % emotes.size();
        return emotes.get(index);
    }

    private static void drawPanel(DrawContext context, int x, int y, int width, int height, int color, int border) {
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, x, y, width, height, theme.radius(), color, border);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private int wheelRadius() {
        int shortest = Math.min(this.width, this.height);
        int reserved = this.height < 300 ? 68 : 112;
        return clamp((shortest - reserved) / 2, 54, 150);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
