package site.s9lab.s9labclient.client.ui;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.KeybindSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class S9LabClientScreen extends ResponsiveScreen {
    private static final int PANEL = 0xEE15171D;
    private static final int PANEL_DARK = 0xF00C0D11;
    private static final int CARD = 0xE31C1E24;
    private static final int CARD_HOVER = 0xEE282B34;
    private static final int LINE = 0xFF2F323B;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF9DA3AE;
    private static final int DIM = 0xFF666C78;
    private static final int GREEN = 0xFF4ADE80;
    private final Screen parent;
    private ClientTab selectedTab;
    private ModuleCategory selectedCategory = ModuleCategory.HUD;
    private Module selectedModule;
    private CosmeticType selectedCosmeticType = CosmeticType.CAPE;
    private Cosmetic selectedCosmetic;
    private HudModule draggingModule;
    private int dragOffsetX;
    private int dragOffsetY;
    private int scroll;
    private boolean previewDragging;
    private float previewYaw = 25.0F;
    private float previewPitch = 5.0F;
    private int previewZoom = 62;
    private boolean searchFocused;
    private String search = "";

    public S9LabClientScreen(Screen parent) {
        this(parent, ClientTab.MODS);
    }

private static final Identifier CLIENT_ICON =
        Identifier.of("s9labclient", "font/icon.png");

    public S9LabClientScreen(Screen parent, ClientTab selectedTab) {
        super(Text.literal("S9Lab Client"));
        this.parent = parent;
        this.selectedTab = selectedTab;
    }

    @Override
    protected void init() {
        clampScroll();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        renderDarkBackground(context);
        Layout layout = layout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, layout.x, layout.y, layout.width, layout.height, 3, PANEL, ClientTheme.withAlpha(theme.accentColor(), 120));
        renderHeader(context, layout, mouseX, mouseY, theme);
        renderBody(context, layout, mouseX, mouseY, theme);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        Layout layout = layout();
        if (handleHudDragStart(mouseX, mouseY)) {
            return true;
        }
        if (handleHeaderClick(layout, mouseX, mouseY) || handleBodyClick(layout, mouseX, mouseY)) {
            return true;
        }
        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (previewDragging) {
            previewYaw += (float) offsetX * 2.4F;
            previewPitch = clamp(Math.round(previewPitch - (float) offsetY * 1.6F), -45, 45);
            return true;
        }
        if (draggingModule != null) {
            draggingModule.setPosition((int) click.x() - dragOffsetX, (int) click.y() - dragOffsetY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (previewDragging) {
            previewDragging = false;
            return true;
        }
        if (draggingModule != null) {
            draggingModule = null;
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = layout();
        if ((selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) && previewBounds(layout).contains(mouseX, mouseY)) {
            previewZoom = clamp(previewZoom + (int) Math.round(verticalAmount * 5.0D), 34, 118);
            return true;
        }
        if (!inside(mouseX, mouseY, layout.bodyX(), layout.bodyY(), layout.bodyWidth(), layout.bodyHeight())) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scroll = ResponsiveLayout.scroll(scroll, verticalAmount, maxScroll(layout));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar()) {
            search = TextLayout.ellipsize(this.textRenderer, search + input.asString(), 170);
            scroll = 0;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
            close();
            return true;
        }
        if (searchFocused) {
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
                return true;
            }
            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    protected void onResponsiveResize() {
        clampScroll();
        previewZoom = clamp(previewZoom, 34, 118);
        int maxX = Math.max(0, this.width - 20);
        int maxY = Math.max(0, this.height - 20);
        S9LabClientClient.getModuleManager().getHudModules().forEach(module ->
                module.setPosition(clamp(module.getX(), 0, maxX), clamp(module.getY(), 0, maxY)));
    }

    @Override
    public void close() {
        S9LabClientClient.getConfigManager().save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    public static void renderDarkBackground(DrawContext context) {
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.overlay(context, theme);
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x55000000);
    }

private void renderHeader(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
    int x = layout.x + 10;
    int y = layout.y + 8;

context.drawTexture(
        RenderPipelines.GUI_TEXTURED,
        CLIENT_ICON,
        x,
        y - 2,
        0.0F,
        0.0F,
        28,
        28,
        28,
        28
);

    context.drawTextWithShadow(
        textRenderer,
        Text.literal("S9Lab Client"),
        x + 36,
        y + 6,
        0xFF7FE7FF
);

    int tabX = layout.tabX();
    for (ClientTab tab : ClientTab.values()) {
        int tabW = tabWidth(tab);
        boolean active = tab == selectedTab;
        boolean hovered = inside(mouseX, mouseY, tabX, y, tabW, 18);

        context.drawTextWithShadow(
                textRenderer,
                Text.literal(tab.label),
                tabX,
                y + 5,
                active ? theme.accentColor() : hovered ? WHITE : MUTED
        );

        if (active) {
            context.fill(
                    tabX,
                    y + 17,
                    tabX + textRenderer.getWidth(tab.label),
                    y + 18,
                    theme.accentColor()
            );
        }

        tabX += tabW;
    }

    context.fill(
            layout.x + 8,
            layout.y + layout.headerHeight - 1,
            layout.x + layout.width - 8,
            layout.y + layout.headerHeight,
            LINE
    );
}

    private void renderBody(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            renderCosmeticsCatalog(context, layout, mouseX, mouseY, theme);
            return;
        }

        renderToolbar(context, layout, mouseX, mouseY, theme);
        int contentY = layout.contentY();
        int contentH = layout.contentHeight();
        context.enableScissor(layout.bodyX(), contentY, layout.bodyX() + layout.bodyWidth(), contentY + contentH);
        switch (selectedTab) {
            case MODS -> renderModuleGrid(context, layout, mouseX, mouseY, theme);
            case SETTINGS -> renderSettings(context, layout, mouseX, mouseY, theme);
            case COSMETICS, SHOP -> renderCosmeticsCatalog(context, layout, mouseX, mouseY, theme);
        }
        context.disableScissor();
        renderScrollbar(context, layout, theme);
    }

    private void renderToolbar(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int x = layout.bodyX();
        int y = layout.toolbarY();
        if (selectedTab == ClientTab.MODS) {
            int chipX = x;
            for (ModuleCategory category : ModuleCategory.values()) {
                int chipW = Math.max(42, Math.min(78, (layout.bodyWidth() - 160) / ModuleCategory.values().length));
                if (chipX + chipW > layout.x + layout.width - 120) {
                    break;
                }
                drawPill(context, chipX, y, chipW, 18, titleCase(category.name()), category == selectedCategory, inside(mouseX, mouseY, chipX, y, chipW, 18), theme);
                chipX += chipW + 5;
            }
            renderSearch(context, layout.searchX(), y, layout.searchWidth(), mouseX, mouseY, theme);
        } else if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            int chipX = x;
            for (CosmeticType type : CosmeticType.values()) {
                int chipW = Math.max(54, Math.min(82, (layout.bodyWidth() - 130) / CosmeticType.values().length));
                if (chipX + chipW > layout.x + layout.width - 120) {
                    break;
                }
                drawPill(context, chipX, y, chipW, 18, shortCosmetic(type), type == selectedCosmeticType, inside(mouseX, mouseY, chipX, y, chipW, 18), theme);
                chipX += chipW + 5;
            }
            renderSearch(context, layout.searchX(), y, layout.searchWidth(), mouseX, mouseY, theme);
        } else {
            String name = selectedModule == null ? "Select a module in Mods" : selectedModule.getName();
            context.drawTextWithShadow(textRenderer, Text.literal(name), x, y + 5, WHITE);
            if (selectedModule != null) {
                drawFlatButton(context, layout.searchX(), y, layout.searchWidth(), 18, selectedModule.isEnabled() ? "Enabled" : "Disabled", selectedModule.isEnabled(), inside(mouseX, mouseY, layout.searchX(), y, layout.searchWidth(), 18), theme);
            }
        }
    }

    private void renderModuleGrid(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        Grid grid = grid(layout.bodyWidth(), layout.contentHeight(), 116, 92, 5);
        List<Module> modules = filteredModules();
        int baseX = layout.bodyX();
        int baseY = layout.contentY() - scroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = baseX + col * (grid.cardW + grid.gap);
            int cardY = baseY + row * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < layout.contentY() || cardY > layout.contentY() + layout.contentHeight()) {
                continue;
            }
            renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, theme);
        }
    }

    private void renderModuleCard(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean selected = module == selectedModule;
        int border = module.isEnabled() ? theme.accentColor() : selected ? 0xFF6B7280 : 0xFF30333B;
        PremiumRender.card(context, x, y, width, height, 2, hovered ? CARD_HOVER : CARD, border);
        context.fill(x, y, x + width, y + 21, module.isEnabled() ? ClientTheme.withAlpha(theme.accentColor(), 145) : 0xFF272A31);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName().toUpperCase(Locale.ROOT), width - 10)), x + width / 2, y + 7, WHITE);
        renderModuleIcon(context, module, x + width / 2, y + 40, theme);
        if (height > 76) {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 12)), x + 6, y + height - 18, module.isEnabled() ? 0xFF9FE9FF : DIM);
        }
    }

    private void renderSettings(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int x = layout.bodyX();
        int y = layout.contentY() - scroll;
        int width = layout.bodyWidth();
        Module module = selectedModule;
        if (module == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a module card to edit its settings."), x + width / 2, layout.contentY() + 34, MUTED);
            return;
        }
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 20)), x + 4, y + 6, MUTED);
        int rowY = y + 30;
        renderSettingRow(context, x, rowY, width, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, theme);
        rowY += 32;
        for (Setting<?> setting : module.getSettings()) {
            renderSettingRow(context, x, rowY, width, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, theme);
            rowY += 32;
        }
    }

    private void renderSettingRow(DrawContext context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 27);
        PremiumRender.card(context, x, y, width, 27, 2, hovered ? CARD_HOVER : CARD, active ? theme.accentColor() : 0xFF30333B);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width / 2)), x + 10, y + 9, WHITE);
        int valueW = Math.min(130, Math.max(58, width / 4));
        drawSwitchValue(context, x + width - valueW - 10, y + 6, valueW, 15, value, active, theme);
    }

    private void renderCosmeticsCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int x = layout.bodyX();
        int y = layout.bodyY() + 8;
        int w = layout.bodyWidth();
        int h = layout.bodyHeight() - 8;

        int sidebarW = Math.max(92, Math.min(120, w / 5));
        int previewW = w >= 610 ? Math.max(150, Math.min(210, w / 4)) : 0;
        int gap = 10;
        int searchH = 24;

        int contentX = x + sidebarW + gap;
        int contentW = w - sidebarW - gap - (previewW > 0 ? previewW + gap : 0);
        int previewX = x + w - previewW;

        // Main cosmetics frame, based on your sketch: left category list, center cards, right preview.
        PremiumRender.card(context, x, y, w, h, 2, 0xD9101117, 0xFF343842);
        context.fill(x + sidebarW, y + 1, x + sidebarW + 1, y + h - 1, LINE);
        if (previewW > 0) {
            context.fill(previewX - gap / 2, y + 1, previewX - gap / 2 + 1, y + h - 1, LINE);
        }

        renderCatalogSidebar(context, x, y, sidebarW, h, mouseX, mouseY, theme);
        renderCatalogSearch(context, contentX, y + 10, Math.min(210, Math.max(110, contentW)), searchH, mouseX, mouseY, theme);

        int gridY = y + 44;
        int gridH = Math.max(1, h - 52);
        context.enableScissor(contentX, gridY, contentX + contentW, gridY + gridH);
        renderCatalogGrid(context, contentX, gridY - scroll, contentW, gridH, mouseX, mouseY, theme);
        context.disableScissor();

        if (previewW > 0) {
            renderCatalogPreview(context, previewX, y, previewW, h, theme);
        }

        drawFlatButton(context, x + 12, y + h - 28, Math.min(58, sidebarW - 24), 18, "Back", false, inside(mouseX, mouseY, x + 12, y + h - 28, Math.min(58, sidebarW - 24), 18), theme);
        renderScrollbar(context, layout, theme);
    }

    private void renderCatalogSidebar(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        context.drawTextWithShadow(textRenderer, Text.literal("COSMETICS"), x + 12, y + 12, WHITE);
        int itemY = y + 38;
        for (CosmeticType type : CosmeticType.values()) {
            boolean active = type == selectedCosmeticType;
            boolean hovered = inside(mouseX, mouseY, x + 8, itemY - 4, width - 16, 20);
            int color = active ? ClientTheme.withAlpha(theme.accentColor(), 120) : hovered ? 0xFF20232B : 0x00000000;
            if (color != 0) {
                PremiumRender.roundedRect(context, x + 8, itemY - 4, width - 16, 20, 2, color);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(sidebarCosmetic(type)), x + 15, itemY + 2, active ? WHITE : MUTED);
            if (active) {
                context.fill(x + 10, itemY + 15, x + 10 + Math.min(50, textRenderer.getWidth(sidebarCosmetic(type))), itemY + 16, theme.accentColor());
            }
            itemY += 24;
        }
    }

    private void renderCatalogSearch(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 2, 0xFF151820, searchFocused ? theme.accentColor() : hovered ? 0xFF555B66 : 0xFF3A3D45);
        String text = search.isBlank() && !searchFocused ? "search" : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text, width - 16)), x + 8, y + 8, search.isBlank() && !searchFocused ? DIM : WHITE);
    }

    private void renderCatalogGrid(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        Grid grid = grid(width, height, 120, 105, 4);
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = x + col * (grid.cardW + grid.gap);
            int cardY = y + row * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < y + scroll || cardY > y + scroll + height) {
                continue;
            }
            renderCatalogCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, theme);
        }
        if (cosmetics.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No cosmetics found"), x + width / 2, y + 30, MUTED);
        }
    }

    private void renderCatalogCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean selected = cosmetic == selectedCosmetic;
        int border = equipped ? theme.accentColor() : selected ? 0xFFB9C2D8 : hovered ? 0xFF5A6070 : 0xFF30333B;
        PremiumRender.card(context, x, y, width, height, 2, hovered ? CARD_HOVER : CARD, border);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 12)), x + 7, y + 7, WHITE);

        int previewX = x + 12;
        int previewY = y + 25;
        int previewW = width - 24;
        int previewH = Math.max(34, height - 52);
        PremiumRender.card(context, previewX, previewY, previewW, previewH, 2, 0xFF090B12, equipped ? ClientTheme.withAlpha(theme.accentColor(), 180) : 0xFF20242E);
        renderCosmeticIcon(context, cosmetic, previewX + previewW / 2, previewY + previewH / 2, theme);

        context.drawTextWithShadow(textRenderer, Text.literal(equipped ? "EQUIPPED" : "CLICK"), x + 7, y + height - 15, equipped ? GREEN : DIM);
    }

    private void renderCatalogPreview(DrawContext context, int x, int y, int width, int height, ClientTheme theme) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PREVIEW"), x + width / 2, y + 12, MUTED);
        int previewTop = y + 34;
        int previewBottom = y + height - 54;
        PremiumRender.card(context, x + 10, previewTop, width - 20, previewBottom - previewTop, 2, 0xFF080A10, 0xFF343842);
        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = previewBottom - 8;
            int size = Math.min(previewZoom, Math.max(42, (previewBottom - previewTop) / 2));
            context.enableScissor(x + 10, previewTop, x + width - 10, previewBottom);
            InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            context.disableScissor();
        }
        context.fill(x + 26, previewBottom + 7, x + width - 26, previewBottom + 9, LINE);
        Cosmetic cosmetic = selectedCosmetic;
        if (cosmetic != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 18)), x + width / 2, y + height - 38, WHITE);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("drag rotate | wheel zoom"), x + width / 2, y + height - 22, DIM);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a cosmetic"), x + width / 2, y + height - 34, DIM);
        }
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        PremiumRender.card(context, x, y, width, height, 2, hovered ? CARD_HOVER : CARD, equipped ? theme.accentColor() : 0xFF30333B);
        context.fill(x, y, x + width, y + 20, equipped ? ClientTheme.withAlpha(theme.accentColor(), 150) : 0xFF272A31);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName().toUpperCase(Locale.ROOT), width - 8)), x + width / 2, y + 7, WHITE);
        renderCosmeticIcon(context, cosmetic, x + 18, y + 32, theme);
        context.drawTextWithShadow(textRenderer, Text.literal(cosmetic.type().displayName()), x + 42, y + 32, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal(equipped ? "EQUIPPED" : "CLICK"), x + 42, y + 47, equipped ? GREEN : DIM);
    }

    private void renderCosmeticPreview(DrawContext context, int x, int y, int width, int height, ClientTheme theme) {
        PremiumRender.card(context, x, y, width, height, 2, 0xDD111318, 0xFF30333B);
        Cosmetic cosmetic = selectedCosmetic;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PREVIEW"), x + width / 2, y + 10, MUTED);
        int previewTop = y + 25;
        int previewBottom = y + Math.max(70, height - 58);
        PremiumRender.card(context, x + 10, previewTop, width - 20, previewBottom - previewTop, 2, PANEL_DARK, 0xFF252934);
        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = previewBottom - 8;
            int size = Math.min(previewZoom, Math.max(34, (previewBottom - previewTop) / 2));
            context.enableScissor(x + 10, previewTop, x + width - 10, previewBottom);
            InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            context.disableScissor();
        }
        if (cosmetic != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 16)), x + width / 2, y + height - 48, WHITE);
            drawFlatButton(context, x + 12, y + height - 30, width - 24, 20, "Equip", true, false, theme);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag rotate | Wheel zoom"), x + width / 2, y + height - 16, DIM);
        }
    }

    private void renderModuleIcon(DrawContext context, Module module, int cx, int cy, ClientTheme theme) {
        int color = module.isEnabled() ? theme.accentColor() : 0xFF9CA3AF;
        String initial = module.getName().isEmpty() ? "?" : module.getName().substring(0, 1).toUpperCase(Locale.ROOT);
        if (module.getCategory() == ModuleCategory.HUD) {
            drawOutline(context, cx - 15, cy - 13, 30, 24, color);
        } else if (module.getCategory() == ModuleCategory.COSMETICS) {
            context.fill(cx - 14, cy - 10, cx + 14, cy + 10, ClientTheme.withAlpha(color, 160));
            drawOutline(context, cx - 14, cy - 10, 28, 20, color);
        } else {
            PremiumRender.roundedRect(context, cx - 14, cy - 14, 28, 28, 6, ClientTheme.withAlpha(color, 95));
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(initial), cx, cy - 4, WHITE);
    }

    private void renderCosmeticIcon(DrawContext context, Cosmetic cosmetic, int cx, int cy, ClientTheme theme) {
        int color = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type())) ? theme.accentColor() : 0xFF9CA3AF;
        switch (cosmetic.type()) {
            case CAPE -> context.fill(cx - 8, cy - 12, cx + 8, cy + 14, color);
            case BANDANA -> context.fill(cx - 12, cy - 4, cx + 12, cy + 3, color);
            case WINGS -> {
                context.fill(cx - 20, cy - 8, cx - 4, cy + 10, color);
                context.fill(cx + 4, cy - 8, cx + 20, cy + 10, color);
            }
            case HAT -> {
                context.fill(cx - 11, cy - 10, cx + 11, cy + 2, color);
                context.fill(cx - 16, cy + 2, cx + 16, cy + 6, color);
            }
            case HALO -> drawOutline(context, cx - 14, cy - 12, 28, 8, color);
            case SHOULDER -> {
                context.fill(cx - 8, cy - 8, cx + 8, cy + 8, color);
                context.fill(cx - 4, cy + 8, cx + 4, cy + 16, color);
            }
        }
    }

    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int tabX = layout.tabX();
        int y = layout.y + 8;
        for (ClientTab tab : ClientTab.values()) {
            int tabW = tabWidth(tab);
            if (inside(mouseX, mouseY, tabX, y, tabW, 18)) {
                selectedTab = tab;
                scroll = 0;
                searchFocused = false;
                return true;
            }
            tabX += tabW;
        }
        return false;
    }

    private boolean handleBodyClick(Layout layout, int mouseX, int mouseY) {
        if (handleToolbarClick(layout, mouseX, mouseY)) {
            return true;
        }
        return switch (selectedTab) {
            case MODS -> handleModuleGridClick(layout, mouseX, mouseY);
            case SETTINGS -> handleSettingsClick(layout, mouseX, mouseY);
            case COSMETICS, SHOP -> handleCosmeticClick(layout, mouseX, mouseY);
        };
    }

    private boolean handleToolbarClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.toolbarY();
        if ((selectedTab == ClientTab.MODS || selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) && inside(mouseX, mouseY, layout.searchX(), y, layout.searchWidth(), 18)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        if (selectedTab == ClientTab.MODS) {
            int chipX = layout.bodyX();
            for (ModuleCategory category : ModuleCategory.values()) {
                int chipW = Math.max(42, Math.min(78, (layout.bodyWidth() - 160) / ModuleCategory.values().length));
                if (chipX + chipW > layout.x + layout.width - 120) {
                    break;
                }
                if (inside(mouseX, mouseY, chipX, y, chipW, 18)) {
                    selectedCategory = category;
                    scroll = 0;
                    return true;
                }
                chipX += chipW + 5;
            }
        } else if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            int chipX = layout.bodyX();
            for (CosmeticType type : CosmeticType.values()) {
                int chipW = Math.max(54, Math.min(82, (layout.bodyWidth() - 130) / CosmeticType.values().length));
                if (chipX + chipW > layout.x + layout.width - 120) {
                    break;
                }
                if (inside(mouseX, mouseY, chipX, y, chipW, 18)) {
                    selectedCosmeticType = type;
                    scroll = 0;
                    return true;
                }
                chipX += chipW + 5;
            }
        } else if (selectedTab == ClientTab.SETTINGS && selectedModule != null && inside(mouseX, mouseY, layout.searchX(), y, layout.searchWidth(), 18)) {
            selectedModule.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        return false;
    }

    private boolean handleModuleGridClick(Layout layout, int mouseX, int mouseY) {
        Grid grid = grid(layout.bodyWidth(), layout.contentHeight(), 116, 92, 5);
        List<Module> modules = filteredModules();
        int baseX = layout.bodyX();
        int baseY = layout.contentY() - scroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = baseX + col * (grid.cardW + grid.gap);
            int cardY = baseY + row * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                if (mouseY <= cardY + 22) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                } else {
                    selectedModule = module;
                    selectedTab = ClientTab.SETTINGS;
                    scroll = 0;
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleSettingsClick(Layout layout, int mouseX, int mouseY) {
        if (selectedModule == null) {
            return false;
        }
        int x = layout.bodyX();
        int y = layout.contentY() - scroll + 30;
        int width = layout.bodyWidth();
        if (inside(mouseX, mouseY, x, y, width, 27)) {
            selectedModule.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        y += 32;
        for (Setting<?> setting : selectedModule.getSettings()) {
            if (inside(mouseX, mouseY, x, y, width, 27)) {
                changeSetting(selectedModule, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            y += 32;
        }
        return false;
    }

    private boolean handleCosmeticClick(Layout layout, int mouseX, int mouseY) {
        int x = layout.bodyX();
        int y = layout.bodyY() + 8;
        int w = layout.bodyWidth();
        int h = layout.bodyHeight() - 8;
        int sidebarW = Math.max(92, Math.min(120, w / 5));
        int previewW = w >= 610 ? Math.max(150, Math.min(210, w / 4)) : 0;
        int gap = 10;
        int contentX = x + sidebarW + gap;
        int contentW = w - sidebarW - gap - (previewW > 0 ? previewW + gap : 0);
        int previewX = x + w - previewW;

        if (inside(mouseX, mouseY, contentX, y + 10, Math.min(210, Math.max(110, contentW)), 24)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int itemY = y + 38;
        for (CosmeticType type : CosmeticType.values()) {
            if (inside(mouseX, mouseY, x + 8, itemY - 4, sidebarW - 16, 20)) {
                selectedCosmeticType = type;
                scroll = 0;
                return true;
            }
            itemY += 24;
        }

        if (inside(mouseX, mouseY, x + 12, y + h - 28, Math.min(58, sidebarW - 24), 18)) {
            close();
            return true;
        }

        int gridY = y + 44;
        int gridH = Math.max(1, h - 52);
        Grid grid = grid(contentW, gridH, 120, 105, 4);
        List<Cosmetic> cosmetics = filteredCosmetics();
        int baseY = gridY - scroll;
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int cardX = contentX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                selectedCosmetic = cosmetic;
                S9LabClientClient.getConfigManager().equipCosmetic(cosmetic.type(), cosmetic.id());
                syncModuleSelection(cosmetic);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
        }

        if (previewW > 0 && inside(mouseX, mouseY, previewX + 10, y + 34, previewW - 20, h - 88)) {
            previewDragging = true;
            return true;
        }
        return false;
    }

    private Rect previewBounds(Layout layout) {
        int w = layout.bodyWidth();
        if (w < 610) {
            return Rect.empty();
        }
        int previewW = Math.max(150, Math.min(210, w / 4));
        int x = layout.bodyX() + w - previewW;
        int y = layout.bodyY() + 8;
        return new Rect(x + 10, y + 34, previewW - 20, layout.bodyHeight() - 96);
    }

    private boolean handleHudDragStart(int mouseX, int mouseY) {
        if (selectedTab != ClientTab.MODS || selectedCategory != ModuleCategory.HUD) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<HudModule> modules = S9LabClientClient.getModuleManager().getHudModules();
        for (int i = modules.size() - 1; i >= 0; i--) {
            HudModule module = modules.get(i);
            if (!module.isEnabled()) {
                continue;
            }
            int x = module.getX() - 4;
            int y = module.getY() - 4;
            if (inside(mouseX, mouseY, x, y, module.getWidth(client) + 8, module.getHeight(client) + 8)) {
                draggingModule = module;
                dragOffsetX = mouseX - module.getX();
                dragOffsetY = mouseY - module.getY();
                return true;
            }
        }
        return false;
    }

    private void renderSearch(DrawContext context, int x, int y, int width, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 18);
        PremiumRender.card(context, x, y, width, 18, 2, 0xFF111217, searchFocused ? theme.accentColor() : hovered ? 0xFF555B66 : 0xFF3A3D45);
        String text = search.isBlank() && !searchFocused ? "Search" : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text, width - 14)), x + 7, y + 5, search.isBlank() && !searchFocused ? DIM : WHITE);
    }

    private void drawPill(DrawContext context, int x, int y, int width, int height, String label, boolean active, boolean hovered, ClientTheme theme) {
        int color = active ? ClientTheme.withAlpha(theme.accentColor(), 160) : hovered ? 0xFF2A2D35 : 0xFF202229;
        int border = active ? theme.accentColor() : 0xFF343842;
        PremiumRender.card(context, x, y, width, height, 3, color, border);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 8)), x + width / 2, y + 5, active ? WHITE : MUTED);
    }

    private void drawFlatButton(DrawContext context, int x, int y, int width, int height, String label, boolean active, boolean hovered, ClientTheme theme) {
        int color = active ? ClientTheme.withAlpha(theme.accentColor(), 155) : hovered ? 0xFF2A2D35 : 0xFF202229;
        PremiumRender.card(context, x, y, width, height, 2, color, active ? theme.accentColor() : 0xFF3A3D45);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 8)), x + width / 2, y + (height - 8) / 2, WHITE);
    }

    private void drawSwitchValue(DrawContext context, int x, int y, int width, int height, String value, boolean active, ClientTheme theme) {
        int trackW = 24;
        int color = active ? theme.accentColor() : 0xFF5A606B;
        PremiumRender.roundedRect(context, x, y + 2, trackW, height - 4, 5, 0xFF23262D);
        PremiumRender.roundedRect(context, active ? x + 13 : x + 2, y + 4, 8, 8, 4, color);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, width - trackW - 8)), x + trackW + 7, y + 4, active ? WHITE : MUTED);
    }

    private void renderScrollbar(DrawContext context, Layout layout, ClientTheme theme) {
        int max = maxScroll(layout);
        if (max <= 0) {
            return;
        }
        int x = layout.x + layout.width - 8;
        int y = layout.contentY();
        int h = layout.contentHeight();
        int thumbH = Math.max(18, h * h / (h + max));
        int thumbY = y + (h - thumbH) * scroll / max;
        PremiumRender.roundedRect(context, x, y, 3, h, 2, 0xFF272A31);
        PremiumRender.roundedRect(context, x, thumbY, 3, thumbH, 2, theme.accentColor());
    }

    private Grid grid(int width, int height, int minCardW, int preferredCardH, int maxColumns) {
        int columns = ResponsiveLayout.columns(width, minCardW, maxColumns);
        int gap = width < 360 ? 4 : 6;
        int cardW = Math.max(60, (width - gap * (columns - 1)) / columns);
        int cardH = Math.max(58, Math.min(preferredCardH, Math.max(58, height / 3)));
        return new Grid(columns, gap, cardW, cardH);
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(760, 430, 300, 220);
        int header = screen.height() < 280 ? 34 : 42;
        int toolbar = 26;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header, toolbar);
    }

    private int maxScroll(Layout layout) {
        int content = switch (selectedTab) {
            case MODS -> {
                Grid grid = grid(layout.bodyWidth(), layout.contentHeight(), 116, 92, 5);
                yield rows(filteredModules().size(), grid.columns) * (grid.cardH + grid.gap);
            }
            case SETTINGS -> selectedModule == null ? 0 : 30 + 32 + selectedModule.getSettings().size() * 32;
            case COSMETICS, SHOP -> {
                int w = layout.bodyWidth();
                int sidebarW = Math.max(92, Math.min(120, w / 5));
                int previewW = w >= 610 ? Math.max(150, Math.min(210, w / 4)) : 0;
                int contentW = w - sidebarW - 10 - (previewW > 0 ? previewW + 10 : 0);
                int contentH = Math.max(1, layout.bodyHeight() - 60);
                Grid grid = grid(contentW, contentH, 120, 105, 4);
                yield rows(filteredCosmetics().size(), grid.columns) * (grid.cardH + grid.gap);
            }
        };
        return Math.max(0, content - layout.contentHeight());
    }

    private void clampScroll() {
        scroll = clamp(scroll, 0, maxScroll(layout()));
    }

    private List<Module> filteredModules() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        return S9LabClientClient.getModuleManager().getModules().stream()
                .filter(module -> module.getCategory() == selectedCategory)
                .filter(module -> query.isEmpty()
                        || module.getName().toLowerCase(Locale.ROOT).contains(query)
                        || module.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private List<Cosmetic> filteredCosmetics() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        return S9LabClientClient.getCosmeticRegistry().all().stream()
                .filter(cosmetic -> cosmetic.type() == selectedCosmeticType)
                .filter(cosmetic -> query.isEmpty()
                        || cosmetic.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || cosmetic.id().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private void changeSetting(Module module, Setting<?> setting) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.setValue(!booleanSetting.getValue());
        } else if (setting instanceof ModeSetting modeSetting) {
            List<String> modes = modeSetting.getModes();
            modeSetting.setValue(modes.get(Math.floorMod(modes.indexOf(modeSetting.getValue()) + 1, modes.size())));
        } else if (setting instanceof NumberSetting numberSetting) {
            double next = numberSetting.getValue() + numberSetting.getStep();
            numberSetting.setValue(next > numberSetting.getMax() ? numberSetting.getMin() : next);
        } else if (setting instanceof KeybindSetting keybindSetting) {
            keybindSetting.setValue(0);
        }
        syncSettingToCosmetic(module, setting);
    }

    private static void syncModuleSelection(Cosmetic cosmetic) {
        String moduleName = switch (cosmetic.type()) {
            case CAPE -> "Cape";
            case BANDANA -> "Bandana";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Shoulder Buddy";
        };
        S9LabClientClient.getModuleManager().getModule(moduleName).ifPresent(module -> {
            module.setEnabled(true);
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.setValue(cosmetic.id());
                }
            }
        });
    }

    private static void syncSettingToCosmetic(Module module, Setting<?> setting) {
        if (!(setting instanceof ModeSetting modeSetting)) {
            return;
        }
        CosmeticType type = switch (module.getName().toLowerCase(Locale.ROOT)) {
            case "cape" -> CosmeticType.CAPE;
            case "bandana" -> CosmeticType.BANDANA;
            case "wings" -> CosmeticType.WINGS;
            case "hat" -> CosmeticType.HAT;
            case "halo" -> CosmeticType.HALO;
            case "shoulder buddy" -> CosmeticType.SHOULDER;
            default -> null;
        };
        if (type == null) {
            return;
        }
        S9LabClientClient.getCosmeticRegistry().get(modeSetting.getValue())
                .filter(cosmetic -> cosmetic.type() == type)
                .ifPresent(cosmetic -> S9LabClientClient.getConfigManager().equipCosmetic(type, cosmetic.id()));
    }

    private static boolean settingActive(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue() > 0.0D;
        }
        return value != null && !String.valueOf(value).isBlank();
    }

    private static String settingValue(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Double doubleValue) {
            return String.valueOf(Math.round(doubleValue));
        }
        if (value instanceof Integer intValue && intValue == 0) {
            return "None";
        }
        return String.valueOf(value);
    }

    private static int tabWidth(ClientTab tab) {
        return switch (tab) {
            case MODS -> 42;
            case SETTINGS -> 56;
            case COSMETICS -> 68;
            case SHOP -> 46;
        };
    }

    private static int rows(int count, int columns) {
        return (count + Math.max(1, columns) - 1) / Math.max(1, columns);
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private static String shortCosmetic(CosmeticType type) {
        return switch (type) {
            case CAPE -> "Cape";
            case BANDANA -> "Band";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Buddy";
        };
    }

    private static String sidebarCosmetic(CosmeticType type) {
        return switch (type) {
            case CAPE -> "Cape";
            case BANDANA -> "Band";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Buddy";
        };
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static void drawOutline(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Layout(int x, int y, int width, int height, int pad, int headerHeight, int toolbarHeight) {
        private int bodyX() {
            return x + pad;
        }

        private int bodyY() {
            return y + headerHeight;
        }

        private int bodyWidth() {
            return Math.max(1, width - pad * 2);
        }

        private int bodyHeight() {
            return Math.max(1, height - headerHeight - pad);
        }

        private int toolbarY() {
            return bodyY() + 7;
        }

        private int contentY() {
            return bodyY() + toolbarHeight + 9;
        }

        private int contentHeight() {
            return Math.max(1, y + height - pad - contentY());
        }

        private int tabX() {
            return x + width / 2 - 84;
        }

        private int searchWidth() {
            return Math.max(54, Math.min(120, bodyWidth() / 4));
        }

        private int searchX() {
            return x + width - pad - searchWidth();
        }
    }

    private record Grid(int columns, int gap, int cardW, int cardH) {
    }

    private record Rect(int x, int y, int width, int height) {
        private static Rect empty() {
            return new Rect(0, 0, 0, 0);
        }

        private boolean contains(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, x, y, width, height);
        }
    }

    public enum ClientTab {
        MODS("Mods"),
        SETTINGS("Settings"),
        COSMETICS("Cosmetics"),
        SHOP("Catalog");

        private final String label;

        ClientTab(String label) {
            this.label = label;
        }
    }
}
