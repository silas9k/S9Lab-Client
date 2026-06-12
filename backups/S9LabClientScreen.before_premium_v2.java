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
    // Sketch / client-store inspired UI colors
    private static final int PANEL = 0xFFADADAD;
    private static final int PANEL_DARK = 0xFF969696;
    private static final int CARD = 0xFFF4F4F4;
    private static final int CARD_HOVER = 0xFFFFFFFF;
    private static final int LINE = 0xFF000000;
    private static final int WHITE = 0xFF000000;
    private static final int MUTED = 0xFF111111;
    private static final int DIM = 0xFF444444;
    private static final int GREEN = 0xFF00FF26;
    private static final int BLUE = 0xFF003BFF;
    private static final int RED = 0xFFFF2525;
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
            Identifier.of("s9labclient", "textures/gui/logo.png");

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
        // Keep the world visible behind the menu, but make the custom GUI clean and readable.
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x33000000);

        Layout layout = layout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.roundedRect(context, layout.x, layout.y, layout.width, layout.height, 0, PANEL);
        drawOutline(context, layout.x, layout.y, layout.width, layout.height, LINE);

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
        int headerY = layout.y + 6;
        int iconX = layout.x + 8;
        int iconY = headerY + 1;

        // Sketch logo block. This avoids the missing-texture icon until the final logo path is fixed.
        context.fill(iconX, iconY, iconX + 27, iconY + 17, 0xFFEDEDED);
        drawOutline(context, iconX, iconY, 27, 17, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("ICON"), iconX + 3, iconY + 5, WHITE);

        int tabX = iconX + 86;
        int tabY = headerY + 6;
        for (ClientTab tab : ClientTab.values()) {
            int tabW = tabWidth(tab) + 14;
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX - 3, tabY - 4, tabW, 17);
            if (active) {
                context.fill(tabX - 3, tabY - 4, tabX + tabW - 3, tabY + 13, 0xFFD2D2D2);
                drawOutline(context, tabX - 3, tabY - 4, tabW, 17, BLUE);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(tab.label), tabX, tabY, hovered || active ? WHITE : DIM);
            tabX += tabW + 12;
            context.drawTextWithShadow(textRenderer, Text.literal("|"), tabX - 7, tabY, DIM);
        }

        int coinsX = layout.x + layout.width - 104;
        context.fill(coinsX, iconY, layout.x + layout.width - 8, iconY + 17, 0xFFEDEDED);
        drawOutline(context, coinsX, iconY, layout.x + layout.width - 8 - coinsX, 17, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("Coins coming soon"), coinsX + 4, iconY + 5, WHITE);

        context.fill(layout.x + 8, layout.y + layout.headerHeight - 1, layout.x + layout.width - 8, layout.y + layout.headerHeight, LINE);
    }


    private void renderBody(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        switch (selectedTab) {
            case MODS -> renderModsSketch(context, layout, mouseX, mouseY, theme);
            case SETTINGS -> renderSettings(context, layout, mouseX, mouseY, theme);
            case COSMETICS, SHOP -> renderCosmetics(context, layout, mouseX, mouseY, theme);
        }
    }


    private void renderModsSketch(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int sideX = layout.x + 10;
        int sideY = layout.bodyY() + 10;
        int sideW = 86;
        int contentX = sideX + sideW + 16;
        int contentY = sideY;
        int contentW = layout.x + layout.width - contentX - 14;
        int contentH = layout.y + layout.height - contentY - 14;

        context.fill(sideX + sideW + 8, layout.bodyY() + 4, sideX + sideW + 10, layout.y + layout.height - 18, LINE);
        int rowY = sideY + 4;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean active = category == selectedCategory;
            boolean hovered = inside(mouseX, mouseY, sideX, rowY - 4, sideW, 18);
            if (active) {
                context.fill(sideX, rowY + 11, sideX + 48, rowY + 13, LINE);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(titleCase(category.name()).toUpperCase(Locale.ROOT)), sideX + 4, rowY, hovered || active ? WHITE : DIM);
            rowY += 26;
        }
        context.drawTextWithShadow(textRenderer, Text.literal("BACK"), sideX + 4, layout.y + layout.height - 35, WHITE);

        renderSearch(context, contentX, contentY, Math.min(190, contentW - 6), mouseX, mouseY, theme);

        int gridY = contentY + 34;
        context.enableScissor(contentX, gridY, contentX + contentW, contentY + contentH);
        renderModuleGridAt(context, contentX, gridY - scroll, contentW, contentH - 34, mouseX, mouseY, theme);
        context.disableScissor();
        renderScrollbar(context, layout, theme);
    }

    private void renderToolbar(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        // Legacy method kept intentionally empty. The sketch UI renders per tab.
    }


    private void renderModuleGrid(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        renderModuleGridAt(context, layout.bodyX(), layout.contentY() - scroll, layout.bodyWidth(), layout.contentHeight(), mouseX, mouseY, theme);
    }

    private void renderModuleGridAt(DrawContext context, int baseX, int baseY, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        Grid grid = grid(width, height, 118, 86, 4);
        List<Module> modules = filteredModules();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = baseX + col * (grid.cardW + grid.gap);
            int cardY = baseY + row * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < baseY + scroll || cardY > baseY + scroll + height + 40) {
                continue;
            }
            renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, theme);
        }
    }


    private void renderModuleCard(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean selected = module == selectedModule;
        int border = module.isEnabled() ? GREEN : selected ? BLUE : LINE;
        context.fill(x, y, x + width, y + height, hovered ? CARD_HOVER : CARD);
        drawOutline(context, x, y, width, height, border);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName().toUpperCase(Locale.ROOT), width - 8)), x + width / 2, y + 8, WHITE);
        renderModuleIcon(context, module, x + width / 2, y + 42, theme);
        context.drawTextWithShadow(textRenderer, Text.literal(module.isEnabled() ? "enabled" : "disabled"), x + 8, y + height - 17, module.isEnabled() ? GREEN : DIM);
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
        context.fill(x, y, x + width, y + 27, hovered ? CARD_HOVER : 0xFFD1D1D1);
        drawOutline(context, x, y, width, 27, active ? GREEN : LINE);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width / 2)), x + 10, y + 9, WHITE);
        int valueW = Math.min(130, Math.max(58, width / 4));
        drawSwitchValue(context, x + width - valueW - 10, y + 6, valueW, 15, value, active, theme);
    }


    private void renderCosmetics(DrawContext context, Layout layout, int mouseX, int mouseY, ClientTheme theme) {
        int sideX = layout.x + 16;
        int sideY = layout.bodyY() + 16;
        int sideW = 88;
        int dividerX = sideX + sideW + 10;
        int searchX = dividerX + 10;
        int searchY = sideY;
        int previewW = Math.max(132, Math.min(170, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 34;
        int gridX = searchX;
        int gridY = searchY + 42;
        int gridW = Math.max(120, previewX - gridX - 26);
        int gridH = layout.y + layout.height - gridY - 26;

        context.fill(dividerX, layout.bodyY() + 4, dividerX + 2, layout.y + layout.height - 20, LINE);

        int catY = sideY + 8;
        for (CosmeticType type : CosmeticType.values()) {
            String name = cosmeticMenuLabel(type);
            boolean active = type == selectedCosmeticType;
            boolean hovered = inside(mouseX, mouseY, sideX, catY - 5, sideW, 18);
            context.drawTextWithShadow(textRenderer, Text.literal(name), sideX + 4, catY, hovered || active ? WHITE : DIM);
            if (active) {
                context.fill(sideX + 4, catY + 12, sideX + 48, catY + 14, LINE);
            }
            catY += 28;
        }
        context.drawTextWithShadow(textRenderer, Text.literal("BACK"), sideX + 4, layout.y + layout.height - 36, WHITE);

        renderSearch(context, searchX, searchY, Math.min(205, Math.max(120, gridW)), mouseX, mouseY, theme);

        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        Grid grid = grid(gridW, gridH, 82, 80, 4);
        List<Cosmetic> cosmetics = filteredCosmetics();
        int baseY = gridY - scroll;
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = gridX + col * (grid.cardW + grid.gap);
            int cardY = baseY + row * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < gridY || cardY > gridY + gridH) {
                continue;
            }
            renderCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, theme);
        }
        context.disableScissor();

        renderCosmeticPreview(context, previewX, sideY + 12, previewW, layout.y + layout.height - sideY - 34, theme);
        renderScrollbar(context, layout, theme);
    }


    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, ClientTheme theme) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean selected = cosmetic == selectedCosmetic;
        int border = selected || equipped ? GREEN : hovered ? BLUE : LINE;
        context.fill(x, y, x + width, y + height, hovered ? CARD_HOVER : CARD);
        drawOutline(context, x, y, width, height, border);

        String name = TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 8);
        context.drawTextWithShadow(textRenderer, Text.literal(name), x + 5, y + 5, WHITE);
        renderCosmeticIcon(context, cosmetic, x + width / 2, y + height / 2 + 4, theme);
        if (equipped) {
            context.drawTextWithShadow(textRenderer, Text.literal("equipped"), x + 5, y + height - 14, GREEN);
        }
    }


    private void renderCosmeticPreview(DrawContext context, int x, int y, int width, int height, ClientTheme theme) {
        Cosmetic cosmetic = selectedCosmetic;
        int modelTop = y + 12;
        int modelBottom = y + height - 28;

        // 3D player preview. The selected cosmetic is equipped on click, so your existing cosmetic renderer
        // should render the cape/wing/hat/etc on this player if it hooks into the normal player renderer.
        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = modelBottom;
            int size = Math.min(previewZoom, Math.max(44, height / 3));
            context.enableScissor(x - 28, modelTop, x + width + 28, modelBottom + 8);
            InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            context.disableScissor();
        } else {
            context.fill(x + width / 2 - 24, y + 40, x + width / 2 + 24, y + 105, LINE);
        }

        context.fill(x + 14, modelBottom + 10, x + width - 14, modelBottom + 12, LINE);
        if (cosmetic != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 8)), x + width / 2, y + height - 16, WHITE);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("select cosmetic"), x + width / 2, y + height - 16, DIM);
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
        return switch (selectedTab) {
            case MODS -> handleModsSketchClick(layout, mouseX, mouseY);
            case SETTINGS -> handleSettingsSketchClick(layout, mouseX, mouseY);
            case COSMETICS, SHOP -> handleCosmeticClick(layout, mouseX, mouseY);
        };
    }


    private boolean handleModsSketchClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 10;
        int sideY = layout.bodyY() + 10;
        int sideW = 86;
        int contentX = sideX + sideW + 16;
        int contentY = sideY;
        int contentW = layout.x + layout.width - contentX - 14;

        if (inside(mouseX, mouseY, sideX + 4, layout.y + layout.height - 38, 52, 20)) {
            close();
            return true;
        }
        if (inside(mouseX, mouseY, contentX, contentY, Math.min(190, contentW - 6), 24)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int rowY = sideY + 4;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (inside(mouseX, mouseY, sideX, rowY - 4, sideW, 18)) {
                selectedCategory = category;
                scroll = 0;
                return true;
            }
            rowY += 26;
        }
        return handleModuleGridClickSketch(layout, mouseX, mouseY);
    }

    private boolean handleSettingsSketchClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 10;
        if (inside(mouseX, mouseY, sideX + 4, layout.y + layout.height - 38, 52, 20)) {
            close();
            return true;
        }
        return handleSettingsClick(layout, mouseX, mouseY);
    }

    private boolean handleToolbarClick(Layout layout, int mouseX, int mouseY) {
        return false;
    }


    private boolean handleModuleGridClick(Layout layout, int mouseX, int mouseY) {
        return handleModuleGridClickSketch(layout, mouseX, mouseY);
    }

    private boolean handleModuleGridClickSketch(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 10;
        int sideY = layout.bodyY() + 10;
        int sideW = 86;
        int contentX = sideX + sideW + 16;
        int contentY = sideY + 34;
        int contentW = layout.x + layout.width - contentX - 14;
        int contentH = layout.y + layout.height - sideY - 48;
        Grid grid = grid(contentW, contentH, 118, 86, 4);
        List<Module> modules = filteredModules();
        int baseY = contentY - scroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % grid.columns;
            int row = i / grid.columns;
            int cardX = contentX + col * (grid.cardW + grid.gap);
            int cardY = baseY + row * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                if (mouseY <= cardY + 24) {
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
        int sideX = layout.x + 16;
        int sideY = layout.bodyY() + 16;
        int sideW = 88;
        int dividerX = sideX + sideW + 10;
        int searchX = dividerX + 10;
        int searchY = sideY;
        int previewW = Math.max(132, Math.min(170, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 34;
        int gridX = searchX;
        int gridY = searchY + 42;
        int gridW = Math.max(120, previewX - gridX - 26);
        int gridH = layout.y + layout.height - gridY - 26;

        if (inside(mouseX, mouseY, sideX + 4, layout.y + layout.height - 39, 55, 22)) {
            close();
            return true;
        }
        if (inside(mouseX, mouseY, searchX, searchY, Math.min(205, Math.max(120, gridW)), 24)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int catY = sideY + 8;
        for (CosmeticType type : CosmeticType.values()) {
            if (inside(mouseX, mouseY, sideX, catY - 5, sideW, 18)) {
                selectedCosmeticType = type;
                scroll = 0;
                return true;
            }
            catY += 28;
        }

        Grid grid = grid(gridW, gridH, 82, 80, 4);
        int baseY = gridY - scroll;
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int cardX = gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                selectedCosmetic = cosmetic;
                S9LabClientClient.getConfigManager().equipCosmetic(cosmetic.type(), cosmetic.id());
                syncModuleSelection(cosmetic);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
        }

        if (previewBounds(layout).contains(mouseX, mouseY)) {
            previewDragging = true;
            return true;
        }
        return false;
    }


    private Rect previewBounds(Layout layout) {
        int sideX = layout.x + 16;
        int sideY = layout.bodyY() + 16;
        int previewW = Math.max(132, Math.min(170, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 34;
        return new Rect(previewX - 28, sideY + 12, previewW + 56, layout.y + layout.height - sideY - 34);
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
        boolean hovered = inside(mouseX, mouseY, x, y, width, 24);
        context.fill(x, y, x + width, y + 24, searchFocused ? 0xFFFFFFFF : hovered ? 0xFFE1E1E1 : 0xFFC6C6C6);
        drawOutline(context, x, y, width, 24, LINE);
        String text = search.isBlank() && !searchFocused ? "search" : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text, width - 14)), x + 10, y + 8, search.isBlank() && !searchFocused ? DIM : WHITE);
    }


    private void drawPill(DrawContext context, int x, int y, int width, int height, String label, boolean active, boolean hovered, ClientTheme theme) {
        context.fill(x, y, x + width, y + height, active ? 0xFFE7E7E7 : hovered ? CARD_HOVER : 0xFFCFCFCF);
        drawOutline(context, x, y, width, height, active ? BLUE : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 8)), x + width / 2, y + 5, WHITE);
    }


    private void drawFlatButton(DrawContext context, int x, int y, int width, int height, String label, boolean active, boolean hovered, ClientTheme theme) {
        context.fill(x, y, x + width, y + height, active ? 0xFFEDEDED : hovered ? CARD_HOVER : 0xFFCFCFCF);
        drawOutline(context, x, y, width, height, active ? GREEN : LINE);
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
        int x = layout.x + layout.width - 10;
        int y = layout.bodyY() + 12;
        int h = layout.height - layout.headerHeight - 28;
        int thumbH = Math.max(18, h * h / (h + max));
        int thumbY = y + (h - thumbH) * scroll / max;
        context.fill(x, y, x + 2, y + h, LINE);
        context.fill(x - 2, thumbY, x + 4, thumbY + thumbH, GREEN);
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
        int header = screen.height() < 280 ? 28 : 31;
        int toolbar = 0;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header, toolbar);
    }


    private int maxScroll(Layout layout) {
        int content = switch (selectedTab) {
            case MODS -> {
                int sideX = layout.x + 10;
                int sideY = layout.bodyY() + 10;
                int sideW = 86;
                int contentX = sideX + sideW + 16;
                int contentW = layout.x + layout.width - contentX - 14;
                int contentH = layout.y + layout.height - sideY - 48;
                Grid grid = grid(contentW, contentH, 118, 86, 4);
                yield rows(filteredModules().size(), grid.columns) * (grid.cardH + grid.gap);
            }
            case SETTINGS -> selectedModule == null ? 0 : 34 + 32 + selectedModule.getSettings().size() * 32;
            case COSMETICS, SHOP -> {
                int sideX = layout.x + 16;
                int dividerX = sideX + 88 + 10;
                int searchX = dividerX + 10;
                int previewW = Math.max(132, Math.min(170, layout.width / 4));
                int previewX = layout.x + layout.width - previewW - 34;
                int gridW = Math.max(120, previewX - searchX - 26);
                int gridH = layout.height - layout.headerHeight - 72;
                Grid grid = grid(gridW, gridH, 82, 80, 4);
                yield rows(filteredCosmetics().size(), grid.columns) * (grid.cardH + grid.gap);
            }
        };
        return Math.max(0, content - Math.max(1, layout.height - layout.headerHeight - 72));
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


    private static String cosmeticMenuLabel(CosmeticType type) {
        return switch (type) {
            case CAPE -> "CAPE";
            case BANDANA -> "BAND";
            case WINGS -> "WINGS";
            case HAT -> "HAT";
            case HALO -> "HALO";
            case SHOULDER -> "SHOULDER";
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
