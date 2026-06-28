package site.s9lab.s9labclient.client.ui;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.backend.BackendState.CosmeticLoadout;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public final class CosmeticLoadoutScreen extends ResponsiveScreen {
    private static final int HEADER_H = 48;
    private static final int FOOTER_H = 44;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE7EAF2;
    private static final int MUTED = 0xFF9AA1B2;
    private static final int DIM = 0xFF687083;
    private static final int GREEN = 0xFF49F26F;

    private final Screen parent;
    private long selectedId;
    private String name = "";
    private boolean nameFocused;
    private int scroll;
    private int maxScroll;

    public CosmeticLoadoutScreen(Screen parent) {
        super(Text.literal("S9Lab Outfits"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BackendClient.fetchCosmeticLoadouts();
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
        View view = view();
        PremiumRender.shopPanel(context, view.x, view.y, view.width, view.height, HEADER_H, FOOTER_H);

        context.drawTextWithShadow(textRenderer, Text.literal("COSMETIC LOADOUTS"), view.x + 16, view.y + 11, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Server-synced outfits  •  " + BackendState.cosmeticLoadoutsSnapshot().size() + "/12"),
                view.x + 16, view.y + 27, BackendState.online() ? GREEN : MUTED);
        button(context, "×", view.x + view.width - 38, view.y + 11, 26, 26, mouseX, mouseY, false, 0xFFFF7B86);

        int toolbarY = view.y + HEADER_H + 10;
        int inputX = view.x + 14;
        int saveW = Math.min(142, Math.max(102, view.width / 5));
        int inputW = Math.max(92, view.width - 42 - saveW);
        PremiumRender.shopInput(context, inputX, toolbarY, inputW, 28, nameFocused, accent);
        String input = name.isBlank() && !nameFocused ? "OUTFIT NAME..." : name + (nameFocused && System.currentTimeMillis() / 450L % 2L == 0 ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, input, inputW - 14)),
                inputX + 7, toolbarY + 10, name.isBlank() && !nameFocused ? DIM : WHITE);
        button(context, "SAVE CURRENT", inputX + inputW + 8, toolbarY, saveW, 28, mouseX, mouseY, false, accent);

        int gridX = view.x + 14;
        int gridY = toolbarY + 40;
        int gridW = view.width - 28;
        int gridH = Math.max(1, view.footerY() - gridY - 8);
        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        List<CosmeticLoadout> loadouts = BackendState.cosmeticLoadoutsSnapshot();
        if (loadouts.isEmpty()) {
            renderEmpty(context, gridX, gridY, gridW, gridH, accent);
            maxScroll = 0;
        } else {
            int columns = gridW >= 700 ? 3 : gridW >= 430 ? 2 : 1;
            int gap = 9;
            int cardW = (gridW - gap * (columns - 1)) / columns;
            int cardH = 118;
            int baseY = gridY - scroll;
            for (int i = 0; i < loadouts.size(); i++) {
                CosmeticLoadout loadout = loadouts.get(i);
                int x = gridX + (i % columns) * (cardW + gap);
                int y = baseY + (i / columns) * (cardH + gap);
                if (y + cardH >= gridY && y <= gridY + gridH) {
                    renderCard(context, loadout, x, y, cardW, cardH, mouseX, mouseY, accent);
                }
            }
            int rows = (loadouts.size() + columns - 1) / columns;
            maxScroll = Math.max(0, rows * (cardH + gap) - gap - gridH);
            scroll = Math.min(scroll, maxScroll);
        }
        context.disableScissor();
        renderScrollbar(context, gridX + gridW - 2, gridY, gridH, accent);

        int footerY = view.footerY() + 9;
        button(context, "REFRESH", view.x + 12, footerY, 76, 26, mouseX, mouseY, false, accent);
        button(context, "CLOSE", view.x + view.width - 82, footerY, 70, 26, mouseX, mouseY, false, accent);
    }

    private void renderCard(DrawContext context, CosmeticLoadout loadout, int x, int y, int width, int height,
                            int mouseX, int mouseY, int accent) {
        boolean selected = selectedId == loadout.loadoutId();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 0,
                selected ? PremiumRender.SHOP_CARD_ACTIVE : hovered ? PremiumRender.SHOP_CARD_HOVER : PremiumRender.SHOP_CARD,
                selected ? accent : 0x55FFFFFF);
        context.fill(x, y, x + 3, y + height, selected ? accent : 0x668A93A6);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, loadout.name().toUpperCase(Locale.ROOT), width - 22)),
                x + 11, y + 10, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(loadout.cosmetics().size() + " COSMETICS"), x + 11, y + 27, MUTED);

        String slots = loadout.cosmetics().entrySet().stream()
                .map(entry -> title(entry.getKey()) + ": " + cosmeticName(entry.getValue()))
                .reduce((left, right) -> left + "  •  " + right)
                .orElse("Empty outfit");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, slots, width - 22)),
                x + 11, y + 47, loadout.cosmetics().isEmpty() ? DIM : TEXT);

        int gap = 5;
        int buttonY = y + height - 31;
        int buttonW = Math.max(44, (width - 22 - gap * 2) / 3);
        button(context, "APPLY", x + 8, buttonY, buttonW, 23, mouseX, mouseY, false, accent);
        button(context, "UPDATE", x + 8 + buttonW + gap, buttonY, buttonW, 23, mouseX, mouseY, false, accent);
        button(context, "DELETE", x + 8 + (buttonW + gap) * 2, buttonY, buttonW, 23, mouseX, mouseY, false, 0xFFFF6B77);
    }

    private void renderEmpty(DrawContext context, int x, int y, int width, int height, int accent) {
        int cardW = Math.min(360, width);
        int cardH = Math.min(104, height);
        int cardX = x + (width - cardW) / 2;
        int cardY = y + Math.max(0, (height - cardH) / 2);
        PremiumRender.card(context, cardX, cardY, cardW, cardH, 0, PremiumRender.SHOP_CARD, 0x55FFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("NO OUTFITS YET"), cardX + cardW / 2, cardY + 24, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Equip cosmetics, enter a name and save the current look."),
                cardX + cardW / 2, cardY + 47, MUTED);
        context.fill(cardX + 24, cardY + cardH - 4, cardX + cardW - 24, cardY + cardH - 2, accent);
    }

    private void renderScrollbar(DrawContext context, int x, int y, int height, int accent) {
        if (maxScroll <= 0) return;
        int thumbH = Math.max(18, height * height / (height + maxScroll));
        int thumbY = y + (height - thumbH) * scroll / maxScroll;
        context.fill(x, y, x + 2, y + height, 0x55000000);
        context.fill(x, thumbY, x + 2, thumbY + thumbH, accent);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        View view = view();
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (inside(mouseX, mouseY, view.x + view.width - 38, view.y + 11, 26, 26)) { close(); return true; }

        int toolbarY = view.y + HEADER_H + 10;
        int saveW = Math.min(142, Math.max(102, view.width / 5));
        int inputW = Math.max(92, view.width - 42 - saveW);
        int inputX = view.x + 14;
        if (inside(mouseX, mouseY, inputX, toolbarY, inputW, 28)) { nameFocused = true; return true; }
        if (inside(mouseX, mouseY, inputX + inputW + 8, toolbarY, saveW, 28)) {
            if (!name.isBlank()) BackendClient.saveCosmeticLoadout(null, name);
            nameFocused = name.isBlank();
            return true;
        }
        nameFocused = false;

        int gridX = view.x + 14;
        int gridY = toolbarY + 40;
        int gridW = view.width - 28;
        int gridH = Math.max(1, view.footerY() - gridY - 8);
        List<CosmeticLoadout> loadouts = BackendState.cosmeticLoadoutsSnapshot();
        int columns = gridW >= 700 ? 3 : gridW >= 430 ? 2 : 1;
        int gap = 9;
        int cardW = (gridW - gap * (columns - 1)) / columns;
        int cardH = 118;
        for (int i = 0; i < loadouts.size(); i++) {
            CosmeticLoadout loadout = loadouts.get(i);
            int x = gridX + (i % columns) * (cardW + gap);
            int y = gridY - scroll + (i / columns) * (cardH + gap);
            if (!inside(mouseX, mouseY, x, y, cardW, cardH) || !inside(mouseX, mouseY, gridX, gridY, gridW, gridH)) continue;
            if (selectedId != loadout.loadoutId()) {
                selectedId = loadout.loadoutId();
                name = loadout.name();
            }
            int buttonW = Math.max(44, (cardW - 22 - gap * 2) / 3);
            int buttonY = y + cardH - 31;
            if (inside(mouseX, mouseY, x + 8, buttonY, buttonW, 23)) {
                BackendClient.applyCosmeticLoadout(loadout.loadoutId(), loadout.name());
            } else if (inside(mouseX, mouseY, x + 8 + buttonW + gap, buttonY, buttonW, 23)) {
                BackendClient.saveCosmeticLoadout(loadout.loadoutId(), name);
            } else if (inside(mouseX, mouseY, x + 8 + (buttonW + gap) * 2, buttonY, buttonW, 23)) {
                BackendClient.deleteCosmeticLoadout(loadout.loadoutId(), loadout.name());
                selectedId = 0;
                name = "";
            }
            return true;
        }

        int footerY = view.footerY() + 9;
        if (inside(mouseX, mouseY, view.x + 12, footerY, 76, 26)) { BackendClient.fetchCosmeticLoadouts(); return true; }
        if (inside(mouseX, mouseY, view.x + view.width - 82, footerY, 70, 26)) { close(); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        View view = view();
        int gridY = view.y + HEADER_H + 50;
        if (inside(mouseX, mouseY, view.x + 14, gridY, view.width - 28, view.footerY() - gridY)) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.round(verticalAmount * 28)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (nameFocused && input.isValidChar() && name.length() < 24) { name += input.asString(); return true; }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) { if (nameFocused) nameFocused = false; else close(); return true; }
        if (nameFocused && input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) { name = removeLast(name); return true; }
        if (nameFocused && input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
            if (!name.isBlank()) BackendClient.saveCosmeticLoadout(null, name);
            nameFocused = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void button(DrawContext context, String label, int x, int y, int width, int height,
                        int mouseX, int mouseY, boolean active, int accent) {
        PremiumRender.shopButton(context, Text.literal(label), x, y, width, height, active,
                inside(mouseX, mouseY, x, y, width, height), accent);
    }

    private View view() {
        ScreenLayout layout = centeredLayout(900, 520, 320, 250);
        return new View(layout.x(), layout.y(), layout.width(), layout.height());
    }

    private static String cosmeticName(String id) {
        BackendState.ShopCosmetic cosmetic = BackendState.catalog(id);
        return cosmetic.name() == null || cosmetic.name().isBlank() ? title(id) : cosmetic.name();
    }

    private static String title(String value) {
        if (value == null || value.isBlank()) return "Unknown";
        String[] words = value.replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static String removeLast(String value) {
        return value == null || value.isEmpty() ? "" : value.substring(0, value.offsetByCodePoints(value.length(), -1));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return PremiumRender.inside(mouseX, mouseY, x, y, width, height);
    }

    private record View(int x, int y, int width, int height) {
        int footerY() { return y + height - FOOTER_H; }
    }
}
