package site.s9lab.s9labclient.client.ui;

import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.S9LabClientClient;
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
    private static final int PANEL = 0xEA121419;
    private static final int CARD = 0xD9161A22;
    private static final int CARD_HOVER = 0xEE1D2430;
    private static final int LINE = 0x66404A61;

    private final Screen parent;
    private int bindingSlot = -1;

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

        PremiumRender.shopBackdrop(context);
        int panelW = Math.min(520, Math.max(300, width - 60));
        int panelH = Math.min(330, Math.max(230, height - 60));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        PremiumRender.shopPanel(context, x, y, panelW, panelH, 44, 0);

        context.drawTextWithShadow(textRenderer, Text.literal("EMOTE WHEEL"), x + 18, y + 14, WHITE);
        String right = bindingSlot >= 0 ? "bind slot " + (bindingSlot + 1) : "4 slots";
        context.drawTextWithShadow(textRenderer, Text.literal(right), x + panelW - 18 - textRenderer.getWidth(right), y + 14, MUTED);

        int centerX = x + panelW / 2;
        int centerY = y + 44 + (panelH - 74) / 2;
        int radius = Math.min(105, Math.max(72, Math.min(panelW, panelH) / 3));
        String[] slots = S9LabClientClient.getConfigManager().getEmoteWheelSlots();

        for (int i = 0; i < 4; i++) {
            renderSlot(context, i, slots[i], centerX, centerY, radius, mouseX, mouseY, accent);
        }

        boolean stopHovered = inside(mouseX, mouseY, centerX - 42, centerY - 28, 84, 56);
        PremiumRender.roundedRect(context, centerX - 42, centerY - 28, 84, 56, 0, stopHovered ? 0xFF24151C : PremiumRender.SHOP_BUTTON);
        PremiumRender.outline(context, centerX - 42, centerY - 28, 84, 56, 0, stopHovered ? 0xFFFF5A67 : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("STOP"), centerX, centerY - 8, stopHovered ? 0xFFFF9CA3 : WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("active"), centerX, centerY + 8, DIM);

        if (bindingSlot >= 0) {
            renderBindPopup(context, x, y, panelW, panelH, mouseX, mouseY, accent);
        } else {
            renderHelp(context, x, y, panelW, panelH, mouseX, mouseY, slots);
        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int panelW = Math.min(520, Math.max(300, width - 60));
        int panelH = Math.min(330, Math.max(230, height - 60));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int centerX = x + panelW / 2;
        int centerY = y + 44 + (panelH - 74) / 2;
        int radius = Math.min(105, Math.max(72, Math.min(panelW, panelH) / 3));

        if (bindingSlot >= 0) {
            if (handleBindPopupClick(mouseX, mouseY, x, y, panelW, panelH)) {
                return true;
            }
            bindingSlot = -1;
            return true;
        }

        if (inside(mouseX, mouseY, centerX - 42, centerY - 28, 84, 56)) {
            EmoteManager.stop();
            close();
            return true;
        }

        int slot = hoveredSlot(mouseX, mouseY, centerX, centerY, radius);
        if (slot >= 0) {
            String id = S9LabClientClient.getConfigManager().getEmoteWheelSlot(slot);
            Emote emote = EmoteManager.byIdOrName(id);
            if (emote == null || !EmoteManager.isBindable(emote)) {
                bindingSlot = slot;
                return true;
            }
            if (EmoteManager.play(emote)) {
                close();
            } else {
                bindingSlot = slot;
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.isEscape()) {
            if (bindingSlot >= 0) {
                bindingSlot = -1;
                return true;
            }
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    private void renderSlot(DrawContext context, int slot, String emoteId, int centerX, int centerY, int radius, int mouseX, int mouseY, int accent) {
        int slotW = 120;
        int slotH = 48;
        int[] pos = slotPosition(slot, centerX, centerY, radius, slotW, slotH);
        int x = pos[0];
        int y = pos[1];
        boolean hovered = inside(mouseX, mouseY, x, y, slotW, slotH);
        Emote emote = EmoteManager.byIdOrName(emoteId);
        boolean valid = emote != null && EmoteManager.isBindable(emote);
        boolean active = valid && emote == EmoteManager.activeEmote();

        PremiumRender.roundedRect(context, x, y, slotW, slotH, 0, hovered ? PremiumRender.SHOP_CARD_HOVER : PremiumRender.SHOP_CARD);
        PremiumRender.outline(context, x, y, slotW, slotH, 0, active ? 0xFF49F26F : hovered ? accent : PremiumRender.SHOP_SOFT_BORDER);

        if (!valid) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("+"), x + slotW / 2, y + 9, accent);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("bind emote"), x + slotW / 2, y + 27, DIM);
            return;
        }

        context.fill(x + 10, y + 10, x + 30, y + 30, emote.accentColor());
        context.drawTextWithShadow(textRenderer, Text.literal(trim(emote.displayName(), 12)), x + 38, y + 10, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(active ? "ACTIVE" : "PLAY"), x + 38, y + 25, active ? 0xFF49F26F : DIM);
    }

    private void renderBindPopup(DrawContext context, int panelX, int panelY, int panelW, int panelH, int mouseX, int mouseY, int accent) {
        int w = 210;
        int h = 118;
        int x = panelX + (panelW - w) / 2;
        int y = panelY + (panelH - h) / 2;
        context.fill(panelX, panelY + 44, panelX + panelW, panelY + panelH, 0xAA000000);
        PremiumRender.shopPanel(context, x, y, w, h, 28, 0);
        context.drawTextWithShadow(textRenderer, Text.literal("Bind Slot " + (bindingSlot + 1)), x + 12, y + 10, WHITE);

        int rowY = y + 34;
        List<Emote> emotes = EmoteManager.bindable();
        for (int i = 0; i < emotes.size(); i++) {
            Emote emote = emotes.get(i);
            boolean hovered = inside(mouseX, mouseY, x + 10, rowY, w - 20, 22);
            PremiumRender.roundedRect(context, x + 10, rowY, w - 20, 22, 0, hovered ? PremiumRender.SHOP_CARD_HOVER : PremiumRender.SHOP_CARD);
            context.fill(x + 18, rowY + 6, x + 28, rowY + 16, emote.accentColor());
            context.drawTextWithShadow(textRenderer, Text.literal(emote.displayName()), x + 36, rowY + 7, hovered ? WHITE : TEXT);
            rowY += 26;
        }

        boolean clearHovered = inside(mouseX, mouseY, x + 10, y + h - 30, w - 20, 20);
        PremiumRender.roundedRect(context, x + 10, y + h - 30, w - 20, 20, 0, clearHovered ? 0xFF2A1820 : PremiumRender.SHOP_BUTTON);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Clear Slot"), x + w / 2, y + h - 24, clearHovered ? 0xFFFF9CA3 : MUTED);
    }

    private void renderHelp(DrawContext context, int x, int y, int panelW, int panelH, int mouseX, int mouseY, String[] slots) {
        int centerX = x + panelW / 2;
        String line1 = "Click + to bind T-Pose or Griddy";
        String line2 = "Bound slots play instantly";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(line1), centerX, y + panelH - 34, MUTED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(line2), centerX, y + panelH - 18, DIM);
    }

    private boolean handleBindPopupClick(int mouseX, int mouseY, int panelX, int panelY, int panelW, int panelH) {
        int w = 210;
        int h = 118;
        int x = panelX + (panelW - w) / 2;
        int y = panelY + (panelH - h) / 2;
        int rowY = y + 34;
        for (Emote emote : EmoteManager.bindable()) {
            if (inside(mouseX, mouseY, x + 10, rowY, w - 20, 22)) {
                S9LabClientClient.getConfigManager().setEmoteWheelSlot(bindingSlot, emote.id());
                S9LabClientClient.getConfigManager().save();
                bindingSlot = -1;
                return true;
            }
            rowY += 26;
        }
        if (inside(mouseX, mouseY, x + 10, y + h - 30, w - 20, 20)) {
            S9LabClientClient.getConfigManager().setEmoteWheelSlot(bindingSlot, "");
            S9LabClientClient.getConfigManager().save();
            bindingSlot = -1;
            return true;
        }
        return inside(mouseX, mouseY, x, y, w, h);
    }

    private int hoveredSlot(int mouseX, int mouseY, int centerX, int centerY, int radius) {
        int slotW = 120;
        int slotH = 48;
        for (int i = 0; i < 4; i++) {
            int[] pos = slotPosition(i, centerX, centerY, radius, slotW, slotH);
            if (inside(mouseX, mouseY, pos[0], pos[1], slotW, slotH)) {
                return i;
            }
        }
        return -1;
    }

    private static int[] slotPosition(int slot, int centerX, int centerY, int radius, int width, int height) {
        return switch (slot) {
            case 0 -> new int[] {centerX - width / 2, centerY - radius - height / 2};
            case 1 -> new int[] {centerX + radius - width / 2, centerY - height / 2};
            case 2 -> new int[] {centerX - width / 2, centerY + radius - height / 2};
            default -> new int[] {centerX - radius - width / 2, centerY - height / 2};
        };
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        return text == null ? "" : text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }
}
