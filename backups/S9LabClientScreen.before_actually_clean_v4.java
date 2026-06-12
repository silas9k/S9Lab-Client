package site.s9lab.s9labclient.client.ui;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
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
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
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

public class S9LabClientScreen extends ResponsiveScreen {
    private static final int BG = 0xF3090B12;
    private static final int PANEL = 0xEE10131C;
    private static final int PANEL_SOFT = 0xB81A1E2A;
    private static final int CARD = 0xD8171B25;
    private static final int CARD_HOVER = 0xEA202637;
    private static final int LINE = 0x553D4356;
    private static final int LINE_STRONG = 0xAA3E465B;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFB6BBC8;
    private static final int DIM = 0xFF777F91;
    private static final int GREEN = 0xFF42F56F;
    private static final int RED = 0xFFFF556A;

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
    private float previewYaw = 180.0F;
    private float previewPitch = 6.0F;
    private int previewZoom = 70;
    private boolean searchFocused;
    private String search = "";

    public S9LabClientScreen(Screen parent) {
        this(parent, ClientTab.MODS);
    }

    public S9LabClientScreen(Screen parent, ClientTab selectedTab) {
        super(Text.literal("S9Lab Client"));
        this.parent = parent;
        this.selectedTab = selectedTab == ClientTab.SETTINGS ? ClientTab.MODS : selectedTab;
    }

    @Override
    protected void init() {
        clampScroll();
        ensureSelectedCosmetic();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        Layout layout = layout();
        ClientTheme theme = ThemeManager.theme();

        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x66000000);
        PremiumRender.roundedRect(context, layout.x, layout.y, layout.width, layout.height, 12, BG);
        glow(context, layout.x, layout.y, layout.width, layout.height, ClientTheme.withAlpha(theme.accentColor(), 45));
        PremiumRender.outline(context, layout.x, layout.y, layout.width, layout.height, 12, LINE_STRONG);

        renderHeader(context, layout, mouseX, mouseY, theme);
        switch (selectedTab) {
            case MODS, SETTINGS -> renderModsPage(context, layout, mouseX, mouseY, theme);
            case COSMETICS -> renderCosmeticsPage(context, layout, mouseX, mouseY, theme, false);
            case SHOP -> renderCosmeticsPage(context, layout, mouseX, mouseY, theme, true);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        Layout layout = layout();

        if (doubled && previewBounds(layout).contains(mouseX, mouseY)) {
            resetPreviewCamera();
            return true;
        }
        if (handleHudDragStart(mouseX, mouseY)) {
            return true;
        }
        if (handleHeaderClick(layout, mouseX, mouseY)
                || handleSidebarClick(layout, mouseX, mouseY)
                || handleBodyClick(layout, mouseX, mouseY)) {
            return true;
        }
        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (previewDragging) {
            previewYaw += (float) offsetX * 1.8F;
            previewPitch = clamp(Math.round(previewPitch - (float) offsetY * 1.2F), -35, 35);
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
            previewZoom = clamp(previewZoom + (int) Math.round(verticalAmount * 6.0D), 42, 124);
            return true;
        }
        if (!inside(mouseX, mouseY, layout.contentX(), layout.contentY(), layout.contentWidth(), layout.contentHeight())) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scroll = ResponsiveLayout.scroll(scroll, verticalAmount, maxScroll(layout));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar()) {
            search = TextLayout.ellipsize(this.textRenderer, search + input.asString(), 220);
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
        previewZoom = clamp(previewZoom, 42, 124);
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
        int accent = theme.accentColor();
        int y = layout.y + 16;
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB"), layout.x + 24, y, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("CLIENT"), layout.x + 24, y + 11, ClientTheme.withAlpha(accent, 230));

        int tabX = layout.x + layout.width / 2 - 160;
        int tabY = layout.y + 18;
        for (ClientTab tab : visibleTabs()) {
            int tabW = tabWidth(tab) + 42;
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX, tabY - 8, tabW, 28);
            int color = active ? WHITE : hovered ? MUTED : DIM;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.label), tabX + tabW / 2, tabY, color);
            if (active) {
                PremiumRender.roundedRect(context, tabX + 10, tabY + 17, tabW - 20, 3, 2, accent);
                PremiumRender.roundedRect(context, tabX + 23, tabY + 21, tabW - 46, 2, 2, ClientTheme.withAlpha(accent, 80));
            }
            tabX += tabW + 12;
        }

        int coinsX = layout.x + layout.width - 115;
        context.drawTextWithShadow(textRenderer, Text.literal("Coins"), coinsX, tabY, MUTED);
        PremiumRender.roundedRect(context, coinsX + 44, tabY - 3, 14, 14, 7, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S"), coinsX + 51, tabY, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("0"), coinsX + 66, tabY, WHITE);
        context.fill(layout.x + 18, layout.y + layout.headerHeight - 1, layout.x + layout.width - 18, layout.y + layout.headerHeight, LINE);
    }

    private void renderModsPage(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int sideX = layout.x + 18;
        int sideY = layout.y + layout.headerHeight + 18;
        int sideW = 132;
        int sideH = layout.height - layout.headerHeight - 36;
        renderLeftShell(context, sideX, sideY, sideW, sideH, theme);
        renderSearch(context, sideX + 12, sideY + 14, sideW - 24, 28, mouseX, mouseY, "Search modules...", theme);

        int catY = sideY + 58;
        for (ModuleCategory category : ModuleCategory.values()) {
            int count = (int) S9LabClientClient.getModuleManager().getModules().stream().filter(m -> m.getCategory() == category).count();
            renderSidebarItem(context, sideX + 10, catY, sideW - 20, 26, titleCase(category.name()), String.valueOf(count), category == selectedCategory, inside(mouseX, mouseY, sideX + 10, catY, sideW - 20, 26), theme);
            catY += 34;
        }
        renderBack(context, sideX + 14, sideY + sideH - 40, sideW - 28, mouseX, mouseY, theme);

        int listX = sideX + sideW + 20;
        int listY = sideY;
        int settingsW = selectedModule == null ? 0 : Math.min(230, Math.max(180, layout.width / 4));
        int listW = layout.x + layout.width - 22 - listX - (settingsW > 0 ? settingsW + 16 : 0);
        int listH = sideH;

        renderModuleList(context, listX, listY, listW, listH, mouseX, mouseY, theme);
        if (selectedModule != null) {
            renderModuleSettingsPanel(context, listX + listW + 16, listY, settingsW, listH, mouseX, mouseY, theme);
        }
    }

    private void renderModuleList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        context.drawTextWithShadow(textRenderer, Text.literal(titleCase(selectedCategory.name())), x + 4, y + 2, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(filteredModules().size() + " modules"), x + 4, y + 14, DIM);
        int contentY = y + 34;
        context.enableScissor(x, contentY, x + width, y + height);
        List<Module> modules = filteredModules();
        int rowY = contentY - scroll;
        for (Module module : modules) {
            boolean hovered = inside(mouseX, mouseY, x, rowY, width, 42);
            boolean selected = module == selectedModule;
            renderModuleRow(context, module, x, rowY, width, 42, hovered, selected, theme);
            rowY += 48;
        }
        context.disableScissor();
        renderScrollbar(context, x + width - 4, contentY, height - 34, maxScroll(layout()), theme);
    }

    private void renderModuleRow(DrawContext context, Module module, int x, int y, int width, int height, boolean hovered, boolean selected, ClientTheme theme) {
        int accent = theme.accentColor();
        int fill = selected ? ClientTheme.withAlpha(accent, 50) : hovered ? CARD_HOVER : CARD;
        int border = selected ? accent : hovered ? ClientTheme.withAlpha(accent, 120) : LINE;
        PremiumRender.card(context, x, y, width, height, 8, fill, border);
        PremiumRender.roundedRect(context, x + 12, y + 11, 20, 20, 6, ClientTheme.withAlpha(accent, selected ? 110 : 55));
        String initial = module.getName().isBlank() ? "?" : module.getName().substring(0, 1).toUpperCase(Locale.ROOT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(initial), x + 22, y + 17, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(module.getName()), x + 42, y + 10, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), Math.max(30, width - 165))), x + 42, y + 22, DIM);
        renderSwitch(context, x + width - 60, y + 12, module.isEnabled(), theme);
        context.drawTextWithShadow(textRenderer, Text.literal("›"), x + width - 18, y + 16, MUTED);
    }

    private void renderModuleSettingsPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        PremiumRender.card(context, x, y, width, height, 10, PANEL_SOFT, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal(selectedModule.getName()), x + 14, y + 14, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Module settings"), x + 14, y + 27, DIM);
        int rowY = y + 54;
        renderSettingRow(context, x + 12, rowY, width - 24, "Enabled", selectedModule.isEnabled() ? "ON" : "OFF", selectedModule.isEnabled(), mouseX, mouseY, theme);
        rowY += 34;
        for (Setting<?> setting : selectedModule.getSettings()) {
            if (rowY > y + height - 34) break;
            renderSettingRow(context, x + 12, rowY, width - 24, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, theme);
            rowY += 34;
        }
    }

    private void renderCosmeticsPage(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme, boolean catalog) {
        ensureSelectedCosmetic();
        int sideX = layout.x + 18;
        int sideY = layout.y + layout.headerHeight + 18;
        int sideW = 132;
        int sideH = layout.height - layout.headerHeight - 36;
        int previewW = Math.min(230, Math.max(190, layout.width / 4));
        int previewX = layout.x + layout.width - 18 - previewW;
        int gridX = sideX + sideW + 20;
        int gridY = sideY;
        int gridW = previewX - gridX - 18;
        int gridH = sideH;

        renderLeftShell(context, sideX, sideY, sideW, sideH, theme);
        int itemY = sideY + 22;
        for (CosmeticType type : CosmeticType.values()) {
            renderSidebarItem(context, sideX + 10, itemY, sideW - 20, 28, shortCosmetic(type), "›", type == selectedCosmeticType, inside(mouseX, mouseY, sideX + 10, itemY, sideW - 20, 28), theme);
            itemY += 40;
        }
        renderBack(context, sideX + 14, sideY + sideH - 40, sideW - 28, mouseX, mouseY, theme);

        int searchW = Math.max(120, gridW - 94);
        renderSearch(context, gridX, gridY, searchW, 30, mouseX, mouseY, "Search cosmetics...", theme);
        renderSortChip(context, gridX + searchW + 10, gridY, 84, 30, catalog ? "Featured" : "Newest", theme);

        renderCosmeticGrid(context, gridX, gridY + 46, gridW, gridH - 46, mouseX, mouseY, theme, catalog);
        renderPreviewPanel(context, previewX, sideY, previewW, sideH, mouseX, mouseY, theme);
    }

    private void renderCosmeticGrid(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme, boolean catalog) {
        List<Cosmetic> cosmetics = filteredCosmetics();
        int gap = 10;
        int columns = Math.max(1, Math.min(4, width / 112));
        int cardW = Math.max(82, (width - gap * (columns - 1)) / columns);
        int cardH = 124;
        context.enableScissor(x, y, x + width, y + height);
        for (int i = 0; i < cosmetics.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int cardX = x + col * (cardW + gap);
            int cardY = y + row * (cardH + gap) - scroll;
            if (cardY + cardH < y || cardY > y + height) continue;
            renderCosmeticCard(context, cosmetics.get(i), cardX, cardY, cardW, cardH, mouseX, mouseY, theme, catalog);
        }
        context.disableScissor();
        renderScrollbar(context, x + width - 4, y, height, maxScroll(layout()), theme);
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme, boolean catalog) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean selected = cosmetic.equals(selectedCosmetic);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        int accent = theme.accentColor();
        int border = selected ? accent : hovered ? ClientTheme.withAlpha(accent, 130) : LINE;
        PremiumRender.card(context, x, y, width, height, 9, hovered ? CARD_HOVER : CARD, border);
        if (selected) {
            glow(context, x, y, width, height, ClientTheme.withAlpha(accent, 36));
        }
        int iconSize = Math.min(50, Math.max(34, width - 44));
        int iconX = x + width / 2 - iconSize / 2;
        int iconY = y + 18;
        drawCosmeticPreviewTexture(context, cosmetic, iconX, iconY, iconSize, iconSize, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 10)), x + width / 2, y + height - 43, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(rarityLabel(cosmetic, catalog)), x + width / 2, y + height - 28, rarityColor(cosmetic, catalog, theme));
        if (equipped) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Equipped"), x + width / 2, y + height - 14, GREEN);
        } else if (catalog) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Preview"), x + width / 2, y + height - 14, MUTED);
        } else {
            PremiumRender.roundedRect(context, x + 14, y + height - 20, width - 28, 14, 5, ClientTheme.withAlpha(accent, 170));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Equip"), x + width / 2, y + height - 17, WHITE);
        }
    }

    private void renderPreviewPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        int accent = theme.accentColor();
        PremiumRender.card(context, x, y, width, height, 10, PANEL_SOFT, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("Preview"), x + 14, y + 14, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(selectedCosmetic == null ? selectedCosmeticType.displayName() : selectedCosmetic.displayName()), x + 14, y + 28, MUTED);

        int previewTop = y + 48;
        int previewBottom = y + height - 76;
        PremiumRender.roundedRect(context, x + 12, previewTop, width - 24, previewBottom - previewTop, 10, 0x88101520);
        PremiumRender.outline(context, x + 12, previewTop, width - 24, previewBottom - previewTop, 10, ClientTheme.withAlpha(accent, 65));
        glow(context, x + 28, previewBottom - 40, width - 56, 26, ClientTheme.withAlpha(accent, 80));
        PremiumRender.roundedRect(context, x + 42, previewBottom - 33, width - 84, 2, 2, ClientTheme.withAlpha(accent, 170));

        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = previewBottom - 14;
            int size = Math.min(previewZoom, Math.max(44, (previewBottom - previewTop) / 2));
            context.enableScissor(x + 12, previewTop, x + width - 12, previewBottom);
            if (selectedCosmetic != null) CosmeticPreviewContext.begin(selectedCosmetic);
            try {
                InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            } finally {
                CosmeticPreviewContext.end();
            }
            if (selectedCosmetic != null && selectedCosmetic.type() == CosmeticType.CAPE) {
                int capeW = Math.max(34, width / 4);
                int capeH = capeW + 10;
                drawCosmeticPreviewTexture(context, selectedCosmetic, centerX - capeW / 2, previewTop + 36, capeW, capeH, accent);
            }
            context.disableScissor();
        }

        boolean equipped = selectedCosmetic != null && selectedCosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(selectedCosmetic.type()));
        PremiumRender.card(context, x + 18, y + height - 64, width - 36, 26, 8, equipped ? ClientTheme.withAlpha(accent, 80) : CARD, equipped ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(equipped ? "✓ Equipped" : "Click a card to equip"), x + width / 2, y + height - 55, equipped ? GREEN : MUTED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag to rotate  •  Scroll to zoom  •  Double click reset"), x + width / 2, y + height - 24, DIM);
    }

    private void renderLeftShell(DrawContext context, int x, int y, int width, int height, ClientTheme theme) {
        PremiumRender.roundedRect(context, x, y, width, height, 10, 0x9910131B);
        PremiumRender.outline(context, x, y, width, height, 10, LINE);
    }

    private void renderSearch(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, String placeholder, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        int border = searchFocused ? theme.accentColor() : hovered ? ClientTheme.withAlpha(theme.accentColor(), 120) : LINE_STRONG;
        PremiumRender.card(context, x, y, width, height, 8, 0xAA0C1018, border);
        context.drawTextWithShadow(textRenderer, Text.literal("⌕"), x + 10, y + height / 2 - 4, DIM);
        String value = search.isBlank() && !searchFocused ? placeholder : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, width - 35)), x + 26, y + height / 2 - 4, search.isBlank() && !searchFocused ? DIM : WHITE);
    }

    private void renderSidebarItem(DrawContext context, int x, int y, int width, int height, String label, String meta, boolean active, boolean hovered, ClientTheme theme) {
        int accent = theme.accentColor();
        if (active || hovered) {
            PremiumRender.roundedRect(context, x, y, width, height, 8, active ? ClientTheme.withAlpha(accent, 145) : 0x55272D3A);
        }
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 12, y + (height - 8) / 2, active ? WHITE : hovered ? MUTED : DIM);
        if (meta != null && !meta.isBlank()) {
            context.drawTextWithShadow(textRenderer, Text.literal(meta), x + width - textRenderer.getWidth(meta) - 12, y + (height - 8) / 2, active ? WHITE : DIM);
        }
    }

    private void renderBack(DrawContext context, int x, int y, int width, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 24);
        PremiumRender.card(context, x, y, width, 24, 7, hovered ? CARD_HOVER : 0x770D1017, hovered ? theme.accentColor() : LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("‹  Back"), x + 12, y + 8, hovered ? WHITE : MUTED);
    }

    private void renderSortChip(DrawContext context, int x, int y, int width, int height, String label, ClientTheme theme) {
        PremiumRender.card(context, x, y, width, height, 8, 0xAA0C1018, LINE_STRONG);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label + " ˅"), x + width / 2, y + height / 2 - 4, WHITE);
    }

    private void renderSettingRow(DrawContext context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 28);
        PremiumRender.card(context, x, y, width, 28, 7, hovered ? CARD_HOVER : CARD, active ? ClientTheme.withAlpha(theme.accentColor(), 140) : LINE);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width / 2)), x + 10, y + 10, WHITE);
        if ("ON".equals(value) || "OFF".equals(value)) {
            renderSwitch(context, x + width - 48, y + 7, active, theme);
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, width / 3)), x + width - textRenderer.getWidth(TextLayout.ellipsize(textRenderer, value, width / 3)) - 12, y + 10, active ? WHITE : MUTED);
        }
    }

    private void renderSwitch(DrawContext context, int x, int y, boolean enabled, ClientTheme theme) {
        int track = enabled ? ClientTheme.withAlpha(theme.accentColor(), 220) : 0xFF454B59;
        PremiumRender.roundedRect(context, x, y, 34, 17, 8, track);
        PremiumRender.roundedRect(context, enabled ? x + 18 : x + 3, y + 3, 11, 11, 6, WHITE);
    }

    private void drawCosmeticPreviewTexture(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int accent) {
        if (cosmetic.texture() != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, cosmetic.texture(), x, y, 0.0F, 0.0F, width, height, width, height);
        } else {
            PremiumRender.roundedRect(context, x, y, width, height, 8, ClientTheme.withAlpha(accent, 90));
        }
    }

    private void glow(DrawContext context, int x, int y, int width, int height, int color) {
        PremiumRender.roundedRect(context, x - 1, y - 1, width + 2, height + 2, 10, color);
    }

    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int tabX = layout.x + layout.width / 2 - 160;
        int tabY = layout.y + 18;
        for (ClientTab tab : visibleTabs()) {
            int tabW = tabWidth(tab) + 42;
            if (inside(mouseX, mouseY, tabX, tabY - 8, tabW, 28)) {
                selectedTab = tab;
                selectedModule = null;
                searchFocused = false;
                search = "";
                scroll = 0;
                ensureSelectedCosmetic();
                resetPreviewCamera();
                return true;
            }
            tabX += tabW + 12;
        }
        return false;
    }

    private boolean handleSidebarClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 18;
        int sideY = layout.y + layout.headerHeight + 18;
        int sideW = 132;
        int sideH = layout.height - layout.headerHeight - 36;
        if (inside(mouseX, mouseY, sideX + 14, sideY + sideH - 40, sideW - 28, 24)) {
            close();
            return true;
        }
        if (selectedTab == ClientTab.MODS || selectedTab == ClientTab.SETTINGS) {
            int catY = sideY + 58;
            for (ModuleCategory category : ModuleCategory.values()) {
                if (inside(mouseX, mouseY, sideX + 10, catY, sideW - 20, 26)) {
                    selectedCategory = category;
                    selectedModule = null;
                    searchFocused = false;
                    scroll = 0;
                    return true;
                }
                catY += 34;
            }
        } else {
            int itemY = sideY + 22;
            for (CosmeticType type : CosmeticType.values()) {
                if (inside(mouseX, mouseY, sideX + 10, itemY, sideW - 20, 28)) {
                    selectedCosmeticType = type;
                    selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse(null);
                    searchFocused = false;
                    search = "";
                    scroll = 0;
                    resetPreviewCamera();
                    return true;
                }
                itemY += 40;
            }
        }
        return false;
    }

    private boolean handleBodyClick(Layout layout, int mouseX, int mouseY) {
        if (selectedTab == ClientTab.MODS || selectedTab == ClientTab.SETTINGS) {
            return handleModsClick(layout, mouseX, mouseY);
        }
        return handleCosmeticClick(layout, mouseX, mouseY);
    }

    private boolean handleModsClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 18;
        int sideY = layout.y + layout.headerHeight + 18;
        int sideW = 132;
        int listX = sideX + sideW + 20;
        int listY = sideY;
        int settingsW = selectedModule == null ? 0 : Math.min(230, Math.max(180, layout.width / 4));
        int listW = layout.x + layout.width - 22 - listX - (settingsW > 0 ? settingsW + 16 : 0);
        if (inside(mouseX, mouseY, sideX + 12, sideY + 14, sideW - 24, 28)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        int rowY = listY + 34 - scroll;
        for (Module module : filteredModules()) {
            if (inside(mouseX, mouseY, listX, rowY, listW, 42)) {
                if (inside(mouseX, mouseY, listX + listW - 66, rowY + 6, 48, 30)) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                } else {
                    selectedModule = module;
                }
                return true;
            }
            rowY += 48;
        }
        if (selectedModule != null) {
            int panelX = listX + listW + 16;
            int panelY = listY;
            int width = settingsW;
            int settingY = panelY + 54;
            if (inside(mouseX, mouseY, panelX + 12, settingY, width - 24, 28)) {
                selectedModule.toggle();
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            settingY += 34;
            for (Setting<?> setting : selectedModule.getSettings()) {
                if (inside(mouseX, mouseY, panelX + 12, settingY, width - 24, 28)) {
                    changeSetting(selectedModule, setting);
                    S9LabClientClient.getConfigManager().save();
                    return true;
                }
                settingY += 34;
            }
        }
        return false;
    }

    private boolean handleCosmeticClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 18;
        int sideY = layout.y + layout.headerHeight + 18;
        int sideW = 132;
        int previewW = Math.min(230, Math.max(190, layout.width / 4));
        int previewX = layout.x + layout.width - 18 - previewW;
        int gridX = sideX + sideW + 20;
        int gridY = sideY;
        int gridW = previewX - gridX - 18;
        if (inside(mouseX, mouseY, gridX, gridY, Math.max(120, gridW - 94), 30)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        if (previewBounds(layout).contains(mouseX, mouseY)) {
            previewDragging = true;
            return true;
        }
        int gap = 10;
        int columns = Math.max(1, Math.min(4, gridW / 112));
        int cardW = Math.max(82, (gridW - gap * (columns - 1)) / columns);
        int cardH = 124;
        int cardsY = gridY + 46;
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int cardX = gridX + col * (cardW + gap);
            int cardY = cardsY + row * (cardH + gap) - scroll;
            if (inside(mouseX, mouseY, cardX, cardY, cardW, cardH)) {
                selectedCosmetic = cosmetics.get(i);
                S9LabClientClient.getConfigManager().equipCosmetic(selectedCosmetic.type(), selectedCosmetic.id());
                syncModuleSelection(selectedCosmetic);
                S9LabClientClient.getConfigManager().save();
                resetPreviewCamera();
                return true;
            }
        }
        return false;
    }

    private boolean handleHudDragStart(int mouseX, int mouseY) {
        if (selectedTab != ClientTab.MODS || selectedCategory != ModuleCategory.HUD) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<HudModule> modules = S9LabClientClient.getModuleManager().getHudModules();
        for (int i = modules.size() - 1; i >= 0; i--) {
            HudModule module = modules.get(i);
            if (!module.isEnabled()) continue;
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

    private Rect previewBounds(Layout layout) {
        if (!(selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP)) return Rect.empty();
        int sideY = layout.y + layout.headerHeight + 18;
        int sideH = layout.height - layout.headerHeight - 36;
        int previewW = Math.min(230, Math.max(190, layout.width / 4));
        int previewX = layout.x + layout.width - 18 - previewW;
        return new Rect(previewX + 12, sideY + 48, previewW - 24, sideH - 124);
    }

    private void renderScrollbar(DrawContext context, int x, int y, int height, int max, ClientTheme theme) {
        if (max <= 0) return;
        int thumbH = Math.max(20, height * height / (height + max));
        int thumbY = y + (height - thumbH) * scroll / max;
        PremiumRender.roundedRect(context, x, y, 3, height, 2, 0x55343A49);
        PremiumRender.roundedRect(context, x, thumbY, 3, thumbH, 2, ClientTheme.withAlpha(theme.accentColor(), 190));
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(900, 480, 420, 280);
        int header = screen.height() < 320 ? 46 : 54;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header);
    }

    private int maxScroll(Layout layout) {
        if (selectedTab == ClientTab.MODS || selectedTab == ClientTab.SETTINGS) {
            return Math.max(0, filteredModules().size() * 48 - (layout.height - layout.headerHeight - 70));
        }
        int sideX = layout.x + 18;
        int sideW = 132;
        int previewW = Math.min(230, Math.max(190, layout.width / 4));
        int previewX = layout.x + layout.width - 18 - previewW;
        int gridX = sideX + sideW + 20;
        int gridW = previewX - gridX - 18;
        int columns = Math.max(1, Math.min(4, gridW / 112));
        int rows = rows(filteredCosmetics().size(), columns);
        return Math.max(0, rows * 134 - (layout.height - layout.headerHeight - 82));
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

    private void ensureSelectedCosmetic() {
        if (selectedCosmetic == null || selectedCosmetic.type() != selectedCosmeticType) {
            selectedCosmetic = S9LabClientClient.getConfigManager().getEquippedCosmetic(selectedCosmeticType)
                    .or(() -> S9LabClientClient.getCosmeticRegistry().firstByType(selectedCosmeticType))
                    .orElse(null);
        }
    }

    private void resetPreviewCamera() {
        previewZoom = 72;
        previewPitch = 6.0F;
        if (selectedCosmeticType == CosmeticType.CAPE || selectedCosmeticType == CosmeticType.WINGS || selectedCosmeticType == CosmeticType.SHOULDER) {
            previewYaw = 180.0F;
        } else {
            previewYaw = 20.0F;
        }
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
        if (!(setting instanceof ModeSetting modeSetting)) return;
        CosmeticType type = switch (module.getName().toLowerCase(Locale.ROOT)) {
            case "cape" -> CosmeticType.CAPE;
            case "bandana" -> CosmeticType.BANDANA;
            case "wings" -> CosmeticType.WINGS;
            case "hat" -> CosmeticType.HAT;
            case "halo" -> CosmeticType.HALO;
            case "shoulder buddy" -> CosmeticType.SHOULDER;
            default -> null;
        };
        if (type == null) return;
        S9LabClientClient.getCosmeticRegistry().get(modeSetting.getValue())
                .filter(cosmetic -> cosmetic.type() == type)
                .ifPresent(cosmetic -> S9LabClientClient.getConfigManager().equipCosmetic(type, cosmetic.id()));
    }

    private static boolean settingActive(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value instanceof Number numberValue) return numberValue.doubleValue() > 0.0D;
        return value != null && !String.valueOf(value).isBlank();
    }

    private static String settingValue(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Double doubleValue) return String.valueOf(Math.round(doubleValue));
        if (value instanceof Integer intValue && intValue == 0) return "None";
        return String.valueOf(value);
    }

    private static ClientTab[] visibleTabs() {
        return new ClientTab[]{ClientTab.MODS, ClientTab.COSMETICS, ClientTab.SHOP};
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
            case BANDANA -> "Bandana";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Shoulder";
        };
    }

    private static String rarityLabel(Cosmetic cosmetic, boolean catalog) {
        String id = cosmetic.id().toLowerCase(Locale.ROOT);
        if (id.contains("dragon") || id.contains("gold") || id.contains("magma")) return "Legendary";
        if (id.contains("galaxy") || id.contains("void") || id.contains("s9lab")) return "Epic";
        if (id.contains("ice") || id.contains("blue") || id.contains("aurora")) return "Rare";
        return catalog ? "Common" : cosmetic.type().displayName();
    }

    private static int rarityColor(Cosmetic cosmetic, boolean catalog, ClientTheme theme) {
        String rarity = rarityLabel(cosmetic, catalog);
        return switch (rarity) {
            case "Legendary" -> 0xFFFFA726;
            case "Epic" -> theme.accentColor();
            case "Rare" -> 0xFF38BDF8;
            default -> DIM;
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

    private record Layout(int x, int y, int width, int height, int pad, int headerHeight) {
        private int contentX() { return x + pad; }
        private int contentY() { return y + headerHeight; }
        private int contentWidth() { return Math.max(1, width - pad * 2); }
        private int contentHeight() { return Math.max(1, height - headerHeight - pad); }
    }

    private record Rect(int x, int y, int width, int height) {
        private static Rect empty() { return new Rect(0, 0, 0, 0); }
        private boolean contains(double mouseX, double mouseY) { return inside(mouseX, mouseY, x, y, width, height); }
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
