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
    private static final int PANEL = 0xE80D1018;
    private static final int PANEL_2 = 0xD9131722;
    private static final int CARD = 0xD8191D29;
    private static final int CARD_HOVER = 0xEA222838;
    private static final int CARD_ACTIVE = 0xEC23133D;
    private static final int LINE = 0xFF252B3A;
    private static final int LINE_SOFT = 0x66343B50;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE7EAF2;
    private static final int MUTED = 0xFF9AA1B2;
    private static final int DIM = 0xFF687083;
    private static final int GREEN = 0xFF49F26F;
    private static final int WARN = 0xFFFFB454;

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
    private float previewPitch = 8.0F;
    private int previewZoom = 78;
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
        int accent = theme.accentColor();

        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x66000000);
        shadow(context, layout.x, layout.y, layout.width, layout.height, 14, 0x99000000);
        rect(context, layout.x, layout.y, layout.width, layout.height, 14, BG);
        glowLine(context, layout.x + 18, layout.y + 1, layout.width - 36, accent);
        outline(context, layout.x, layout.y, layout.width, layout.height, 14, 0xFF1E2431);

        renderHeader(context, layout, mouseX, mouseY, accent);
        switch (selectedTab) {
            case MODS -> renderMods(context, layout, mouseX, mouseY, accent);
            case COSMETICS -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SHOP -> renderCatalog(context, layout, mouseX, mouseY, accent);
            case SETTINGS -> renderMods(context, layout, mouseX, mouseY, accent);
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
        if (handleHeaderClick(layout, mouseX, mouseY)) {
            return true;
        }
        if (handleHudDragStart(mouseX, mouseY)) {
            return true;
        }
        boolean handled = switch (selectedTab) {
            case MODS -> handleModsClick(layout, mouseX, mouseY);
            case COSMETICS -> handleCosmeticClick(layout, mouseX, mouseY);
            case SHOP -> handleCatalogClick(layout, mouseX, mouseY);
            case SETTINGS -> handleModsClick(layout, mouseX, mouseY);
        };
        if (handled) {
            return true;
        }
        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (previewDragging) {
            previewYaw += (float) offsetX * 1.9F;
            previewPitch = clamp(Math.round(previewPitch - (float) offsetY * 1.2F), -35, 45);
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
        if (previewBounds(layout).contains(mouseX, mouseY)) {
            previewZoom = clamp(previewZoom + (int) Math.round(verticalAmount * 7.0D), 42, 140);
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
            search = TextLayout.ellipsize(textRenderer, search + input.asString(), 180);
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
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x66000000);
    }

    private void renderHeader(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int y = layout.y + 18;
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB"), layout.x + 30, y, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("CLIENT"), layout.x + 30, y + 12, DIM);

        int tabX = layout.x + layout.width / 2 - 150;
        for (ClientTab tab : new ClientTab[]{ClientTab.MODS, ClientTab.COSMETICS, ClientTab.SHOP}) {
            int w = tab == ClientTab.COSMETICS ? 92 : 72;
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX, y - 7, w, 28);
            int color = active ? WHITE : hovered ? TEXT : MUTED;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.label), tabX + w / 2, y + 4, color);
            if (active) {
                rect(context, tabX + 14, y + 27, w - 28, 2, 2, accent);
                rect(context, tabX + 20, y + 30, w - 40, 2, 2, ClientTheme.withAlpha(accent, 80));
            }
            tabX += w + 34;
        }

        int coinsX = layout.x + layout.width - 150;
        context.drawTextWithShadow(textRenderer, Text.literal("Coins"), coinsX, y + 4, MUTED);
        rect(context, coinsX + 42, y + 1, 15, 15, 8, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S"), coinsX + 49, y + 5, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("0"), coinsX + 66, y + 4, WHITE);
        context.fill(layout.x + 22, layout.y + layout.headerHeight - 1, layout.x + layout.width - 22, layout.y + layout.headerHeight, LINE);
    }

    private void renderMods(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 144;
        int settingsW = selectedModule == null ? 0 : 230;
        int listX = sideX + sideW + 18;
        int listY = sideY;
        int listW = layout.x + layout.width - listX - 24 - settingsW - (settingsW > 0 ? 18 : 0);
        int listH = layout.y + layout.height - listY - 24;
        int settingsX = listX + listW + 18;

        renderLeftShell(context, sideX, sideY, sideW, listH, mouseX, mouseY, accent, true);

        context.drawTextWithShadow(textRenderer, Text.literal(titleCase(selectedCategory.name())), listX, listY + 2, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(filteredModules().size() + " modules"), listX, listY + 15, DIM);
        context.fill(listX, listY + 42, listX + listW, listY + 43, LINE_SOFT);

        context.enableScissor(listX, listY + 50, listX + listW, listY + listH);
        int y = listY + 50 - scroll;
        for (Module module : filteredModules()) {
            renderModuleRow(context, module, listX, y, listW, 40, mouseX, mouseY, accent);
            y += 48;
        }
        context.disableScissor();

        if (selectedModule != null) {
            renderModuleSettingsPanel(context, settingsX, listY, settingsW, listH, mouseX, mouseY, accent);
        }
    }

    private void renderLeftShell(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, boolean modules) {
        rect(context, x, y, width, height, 12, 0x9A070A10);
        outline(context, x, y, width, height, 12, 0x331E2431);
        renderSearch(context, x + 12, y + 16, width - 24, mouseX, mouseY, accent, modules ? "Search modules..." : "Search cosmetics...");

        int itemY = y + 62;
        if (modules) {
            for (ModuleCategory category : ModuleCategory.values()) {
                boolean active = category == selectedCategory;
                renderSideItem(context, titleCase(category.name()), moduleCount(category), x + 12, itemY, width - 24, 28, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 28), accent);
                itemY += 36;
            }
        } else {
            for (CosmeticType type : CosmeticType.values()) {
                boolean active = type == selectedCosmeticType;
                renderSideItem(context, cosmeticMenuLabel(type), -1, x + 12, itemY, width - 24, 30, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 30), accent);
                itemY += 40;
            }
            renderSideItem(context, "Glint", -1, x + 12, itemY, width - 24, 30, false, inside(mouseX, mouseY, x + 12, itemY, width - 24, 30), accent);
        }

        renderBackButton(context, x + 12, y + height - 38, width - 24, 26, inside(mouseX, mouseY, x + 12, y + height - 38, width - 24, 26), accent);
    }

    private void renderModuleRow(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean selected = module == selectedModule;
        int bg = selected ? ClientTheme.withAlpha(accent, 50) : hovered ? CARD_HOVER : CARD;
        rect(context, x, y, width, height, 9, bg);
        outline(context, x, y, width, height, 9, selected ? accent : hovered ? 0xFF333B50 : LINE_SOFT);
        rect(context, x + 12, y + 9, 22, 22, 6, 0x5518202E);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(module.getName().isEmpty() ? "?" : module.getName().substring(0, 1).toUpperCase(Locale.ROOT)), x + 23, y + 15, module.isEnabled() ? accent : MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName(), width - 135)), x + 44, y + 8, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 135)), x + 44, y + 21, DIM);
        renderSwitch(context, x + width - 64, y + 11, module.isEnabled(), accent);
        context.drawTextWithShadow(textRenderer, Text.literal("›"), x + width - 18, y + 14, DIM);
    }

    private void renderModuleSettingsPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        Module module = selectedModule;
        rect(context, x, y, width, height, 12, 0x9A0B0E15);
        outline(context, x, y, width, height, 12, LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal(module.getName()), x + 16, y + 16, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Module settings"), x + 16, y + 29, DIM);
        int rowY = y + 58;
        renderSettingRow(context, x + 14, rowY, width - 28, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        rowY += 36;
        for (Setting<?> setting : module.getSettings()) {
            renderSettingRow(context, x + 14, rowY, width - 28, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
            rowY += 36;
            if (rowY > y + height - 20) {
                break;
            }
        }
    }

    private void renderSettingRow(DrawContext context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 30);
        rect(context, x, y, width, 30, 8, hovered ? CARD_HOVER : 0x66151A25);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 90)), x + 10, y + 10, TEXT);
        if ("ON".equals(value) || "OFF".equals(value)) {
            renderSwitch(context, x + width - 44, y + 7, active, accent);
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, 74)), x + width - 84, y + 10, active ? accent : MUTED);
        }
    }

    private void renderCosmetics(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 142;
        int previewW = Math.max(205, Math.min(260, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 24;
        int gridX = sideX + sideW + 18;
        int searchY = sideY;
        int gridY = searchY + 46;
        int gridW = previewX - gridX - 18;
        int gridH = layout.y + layout.height - gridY - 24;
        int panelH = layout.y + layout.height - sideY - 24;

        renderLeftShell(context, sideX, sideY, sideW, panelH, mouseX, mouseY, accent, false);
        renderSearch(context, gridX, searchY, gridW, mouseX, mouseY, accent, "Search cosmetics...");

        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        Grid grid = grid(gridW, gridH, 112, 128, 4);
        List<Cosmetic> cosmetics = filteredCosmetics();
        int baseY = gridY - scroll;
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int cardX = gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < gridY || cardY > gridY + gridH) {
                continue;
            }
            renderCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
        }
        context.disableScissor();

        renderCosmeticPreview(context, previewX, sideY, previewW, panelH, accent);
    }

    private void renderCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        renderCosmetics(context, layout, mouseX, mouseY, accent);
        int x = layout.x + layout.width - 124;
        int y = layout.bodyY() + 24;
        rect(context, x, y, 82, 20, 10, ClientTheme.withAlpha(accent, 120));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Catalog"), x + 41, y + 6, WHITE);
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean selected = cosmetic == selectedCosmetic;
        rect(context, x, y, width, height, 10, selected ? ClientTheme.withAlpha(accent, 42) : hovered ? CARD_HOVER : CARD);
        outline(context, x, y, width, height, 10, selected || equipped ? accent : hovered ? 0xFF384158 : LINE_SOFT);
        if (equipped) {
            rect(context, x + width - 20, y + 8, 13, 13, 7, accent);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("✓"), x + width - 14, y + 10, WHITE);
        }
        int imageTop = y + 14;
        int imageH = height - 58;
        drawCosmeticTexture(context, cosmetic, x + width / 2 - 28, imageTop + 4, 56, Math.max(44, imageH - 4), accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 12)), x + width / 2, y + height - 36, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(cosmeticRarity(cosmetic)), x + width / 2, y + height - 22, rarityColor(cosmetic));
    }

    private void drawCosmeticTexture(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int accent) {
        try {
            if (cosmetic.texture() != null) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, cosmetic.texture(), x, y, 0.0F, 0.0F, width, height, width, height);
                return;
            }
        } catch (Throwable ignored) {
        }
        int color = cosmetic.type() == CosmeticType.CAPE ? accent : 0xFF596174;
        switch (cosmetic.type()) {
            case CAPE -> rect(context, x + 14, y, width - 28, height, 5, ClientTheme.withAlpha(color, 210));
            case WINGS -> {
                rect(context, x, y + 12, width / 2 - 4, height - 20, 8, ClientTheme.withAlpha(color, 190));
                rect(context, x + width / 2 + 4, y + 12, width / 2 - 4, height - 20, 8, ClientTheme.withAlpha(color, 190));
            }
            case HAT -> rect(context, x + 8, y + 12, width - 16, height / 3, 6, ClientTheme.withAlpha(color, 200));
            case HALO -> outline(context, x + 8, y + 12, width - 16, 14, 7, ClientTheme.withAlpha(color, 255));
            case BANDANA -> rect(context, x + 6, y + height / 2 - 5, width - 12, 10, 5, ClientTheme.withAlpha(color, 200));
            case SHOULDER -> rect(context, x + 16, y + 18, width - 32, height - 30, 7, ClientTheme.withAlpha(color, 190));
        }
    }

    private void renderCosmeticPreview(DrawContext context, int x, int y, int width, int height, int accent) {
        Cosmetic cosmetic = selectedCosmetic;
        rect(context, x, y, width, height, 13, 0x9A0B0E15);
        outline(context, x, y, width, height, 13, LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal(cosmetic == null ? "Preview" : cosmetic.displayName()), x + 18, y + 16, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Drag to rotate • Scroll to zoom"), x + 18, y + 30, DIM);

        int modelTop = y + 55;
        int modelBottom = y + height - 88;
        rect(context, x + 14, modelTop, width - 28, modelBottom - modelTop, 14, 0xAA101521);
        rect(context, x + width / 2 - 58, modelBottom - 14, 116, 18, 10, ClientTheme.withAlpha(accent, 44));
        outline(context, x + width / 2 - 58, modelBottom - 14, 116, 18, 10, ClientTheme.withAlpha(accent, 110));

        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = modelBottom - 2;
            int size = Math.min(previewZoom, Math.max(54, height / 3));
            context.enableScissor(x + 14, modelTop, x + width - 14, modelBottom + 16);
            if (cosmetic != null) {
                CosmeticPreviewContext.begin(cosmetic);
            }
            InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            if (cosmetic != null) {
                CosmeticPreviewContext.end();
            }
            context.disableScissor();

            if (cosmetic != null && cosmetic.type() == CosmeticType.CAPE) {
                // CapeRenderer is currently detached in your project; this keeps cape selection visible in the preview panel.
                drawCosmeticTexture(context, cosmetic, centerX - 34, modelTop + 26, 68, 92, accent);
            }
        }

        int buttonY = y + height - 64;
        String status = cosmetic == null ? "Select cosmetic" : cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type())) ? "Equipped" : "Equip";
        rect(context, x + 16, buttonY, width - 32, 28, 10, cosmetic == null ? 0x66151A25 : accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + width / 2, buttonY + 10, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Double click preview to reset camera"), x + width / 2, y + height - 22, DIM);
    }

    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.y + 18;
        int tabX = layout.x + layout.width / 2 - 150;
        for (ClientTab tab : new ClientTab[]{ClientTab.MODS, ClientTab.COSMETICS, ClientTab.SHOP}) {
            int w = tab == ClientTab.COSMETICS ? 92 : 72;
            if (inside(mouseX, mouseY, tabX, y - 7, w, 28)) {
                selectedTab = tab;
                selectedModule = null;
                searchFocused = false;
                search = "";
                scroll = 0;
                resetPreviewForTab();
                return true;
            }
            tabX += w + 34;
        }
        return false;
    }

    private boolean handleModsClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 144;
        int panelH = layout.y + layout.height - sideY - 24;
        if (inside(mouseX, mouseY, sideX + 12, panelH + sideY - 38, sideW - 24, 26)) {
            close();
            return true;
        }
        if (inside(mouseX, mouseY, sideX + 12, sideY + 16, sideW - 24, 30)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        int itemY = sideY + 62;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (inside(mouseX, mouseY, sideX + 12, itemY, sideW - 24, 28)) {
                selectedCategory = category;
                selectedModule = null;
                scroll = 0;
                return true;
            }
            itemY += 36;
        }
        if (selectedModule != null && handleSettingsClick(layout, mouseX, mouseY)) {
            return true;
        }
        return handleModuleListClick(layout, mouseX, mouseY);
    }

    private boolean handleModuleListClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 144;
        int settingsW = selectedModule == null ? 0 : 230;
        int listX = sideX + sideW + 18;
        int listY = sideY;
        int listW = layout.x + layout.width - listX - 24 - settingsW - (settingsW > 0 ? 18 : 0);
        int y = listY + 50 - scroll;
        for (Module module : filteredModules()) {
            if (inside(mouseX, mouseY, listX, y, listW, 40)) {
                if (inside(mouseX, mouseY, listX + listW - 70, y + 6, 54, 28)) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                } else {
                    selectedModule = module;
                }
                return true;
            }
            y += 48;
        }
        return false;
    }

    private boolean handleSettingsClick(Layout layout, int mouseX, int mouseY) {
        if (selectedModule == null) {
            return false;
        }
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 144;
        int settingsW = 230;
        int listX = sideX + sideW + 18;
        int listW = layout.x + layout.width - listX - 24 - settingsW - 18;
        int x = listX + listW + 18 + 14;
        int y = sideY + 58;
        int width = settingsW - 28;
        if (inside(mouseX, mouseY, x, y, width, 30)) {
            selectedModule.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        y += 36;
        for (Setting<?> setting : selectedModule.getSettings()) {
            if (inside(mouseX, mouseY, x, y, width, 30)) {
                changeSetting(selectedModule, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            y += 36;
        }
        return false;
    }

    private boolean handleCosmeticClick(Layout layout, int mouseX, int mouseY) {
        int sideX = layout.x + 22;
        int sideY = layout.bodyY() + 18;
        int sideW = 142;
        int panelH = layout.y + layout.height - sideY - 24;
        int previewW = Math.max(205, Math.min(260, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 24;
        int gridX = sideX + sideW + 18;
        int searchY = sideY;
        int gridY = searchY + 46;
        int gridW = previewX - gridX - 18;
        int gridH = layout.y + layout.height - gridY - 24;

        if (inside(mouseX, mouseY, sideX + 12, sideY + panelH - 38, sideW - 24, 26)) {
            close();
            return true;
        }
        if (inside(mouseX, mouseY, gridX, searchY, gridW, 30)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        int itemY = sideY + 62;
        for (CosmeticType type : CosmeticType.values()) {
            if (inside(mouseX, mouseY, sideX + 12, itemY, sideW - 24, 30)) {
                selectedCosmeticType = type;
                selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse(null);
                scroll = 0;
                resetPreviewForCosmeticType(type);
                return true;
            }
            itemY += 40;
        }
        Grid grid = grid(gridW, gridH, 112, 128, 4);
        int baseY = gridY - scroll;
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            int cardX = gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                Cosmetic cosmetic = cosmetics.get(i);
                selectedCosmetic = cosmetic;
                S9LabClientClient.getConfigManager().equipCosmetic(cosmetic.type(), cosmetic.id());
                syncModuleSelection(cosmetic);
                resetPreviewForCosmeticType(cosmetic.type());
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

    private boolean handleCatalogClick(Layout layout, int mouseX, int mouseY) {
        return handleCosmeticClick(layout, mouseX, mouseY);
    }

    private Rect previewBounds(Layout layout) {
        if (selectedTab == ClientTab.MODS && selectedModule == null) {
            return Rect.empty();
        }
        int sideY = layout.bodyY() + 18;
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            int previewW = Math.max(205, Math.min(260, layout.width / 3));
            int previewX = layout.x + layout.width - previewW - 24;
            return new Rect(previewX, sideY, previewW, layout.y + layout.height - sideY - 24);
        }
        return Rect.empty();
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

    private void renderSearch(DrawContext context, int x, int y, int width, int mouseX, int mouseY, int accent, String placeholder) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 30);
        rect(context, x, y, width, 30, 8, 0xAA0D111A);
        outline(context, x, y, width, 30, 8, searchFocused ? accent : hovered ? 0xFF394257 : LINE_SOFT);
        String text = search.isBlank() && !searchFocused ? placeholder : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal("⌕"), x + 12, y + 10, DIM);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text, width - 38)), x + 30, y + 10, search.isBlank() && !searchFocused ? DIM : WHITE);
    }

    private void renderSideItem(DrawContext context, String label, int count, int x, int y, int width, int height, boolean active, boolean hovered, int accent) {
        if (active || hovered) {
            rect(context, x, y, width, height, 8, active ? ClientTheme.withAlpha(accent, 150) : 0x5518202E);
        }
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 12, y + (height - 8) / 2, active ? WHITE : hovered ? TEXT : MUTED);
        if (count >= 0) {
            context.drawTextWithShadow(textRenderer, Text.literal(String.valueOf(count)), x + width - 22, y + (height - 8) / 2, active ? WHITE : DIM);
        }
    }

    private void renderBackButton(DrawContext context, int x, int y, int width, int height, boolean hovered, int accent) {
        rect(context, x, y, width, height, 8, hovered ? 0x5518202E : 0x330B0E15);
        outline(context, x, y, width, height, 8, hovered ? accent : LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal("‹  Back"), x + 14, y + 9, hovered ? WHITE : MUTED);
    }

    private void renderSwitch(DrawContext context, int x, int y, boolean enabled, int accent) {
        int track = enabled ? ClientTheme.withAlpha(accent, 220) : 0xFF3A4050;
        rect(context, x, y, 38, 18, 9, track);
        int knobX = enabled ? x + 21 : x + 3;
        rect(context, knobX, y + 3, 12, 12, 6, WHITE);
    }

    private void resetPreviewCamera() {
        previewYaw = 180.0F;
        previewPitch = 8.0F;
        previewZoom = 78;
    }

    private void resetPreviewForTab() {
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(selectedCosmeticType).orElse(selectedCosmetic);
            resetPreviewForCosmeticType(selectedCosmeticType);
        }
    }

    private void resetPreviewForCosmeticType(CosmeticType type) {
        previewZoom = switch (type) {
            case CAPE, WINGS -> 82;
            case HALO, HAT -> 96;
            default -> 78;
        };
        previewYaw = switch (type) {
            case CAPE, WINGS -> 180.0F;
            default -> 25.0F;
        };
        previewPitch = switch (type) {
            case HALO, HAT -> -8.0F;
            default -> 8.0F;
        };
    }

    private int moduleCount(ModuleCategory category) {
        return (int) S9LabClientClient.getModuleManager().getModules().stream()
                .filter(module -> module.getCategory() == category)
                .count();
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

    private int maxScroll(Layout layout) {
        if (selectedTab == ClientTab.MODS) {
            return Math.max(0, filteredModules().size() * 48 - (layout.bodyHeight() - 40));
        }
        int sideX = layout.x + 22;
        int sideW = 142;
        int previewW = Math.max(205, Math.min(260, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 24;
        int gridX = sideX + sideW + 18;
        int gridW = previewX - gridX - 18;
        Grid grid = grid(gridW, layout.bodyHeight(), 112, 128, 4);
        int rows = rows(filteredCosmetics().size(), grid.columns);
        return Math.max(0, rows * (grid.cardH + grid.gap) - (layout.bodyHeight() - 46));
    }

    private void clampScroll() {
        scroll = clamp(scroll, 0, maxScroll(layout()));
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(770, 430, 330, 230);
        int header = screen.height() < 280 ? 44 : 62;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header);
    }

    private Grid grid(int width, int height, int minCardW, int preferredCardH, int maxColumns) {
        int columns = ResponsiveLayout.columns(width, minCardW, maxColumns);
        int gap = width < 360 ? 8 : 10;
        int cardW = Math.max(72, (width - gap * (columns - 1)) / columns);
        int cardH = Math.max(82, Math.min(preferredCardH, Math.max(82, height / 2)));
        return new Grid(columns, gap, cardW, cardH);
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

    private static int rows(int count, int columns) {
        return (count + Math.max(1, columns) - 1) / Math.max(1, columns);
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private static String cosmeticMenuLabel(CosmeticType type) {
        return switch (type) {
            case CAPE -> "Cape";
            case BANDANA -> "Bandana";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Shoulder";
        };
    }

    private static String cosmeticRarity(Cosmetic cosmetic) {
        int hash = Math.abs(cosmetic.id().hashCode());
        return switch (hash % 4) {
            case 0 -> "Common";
            case 1 -> "Rare";
            case 2 -> "Epic";
            default -> "Legendary";
        };
    }

    private static int rarityColor(Cosmetic cosmetic) {
        return switch (cosmeticRarity(cosmetic)) {
            case "Rare" -> 0xFF39A7FF;
            case "Epic" -> 0xFFB642FF;
            case "Legendary" -> WARN;
            default -> DIM;
        };
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static void shadow(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        rect(context, x + 3, y + 4, width, height, radius, color);
    }

    private static void glowLine(DrawContext context, int x, int y, int width, int color) {
        context.fill(x, y, x + width, y + 1, ClientTheme.withAlpha(color, 70));
    }

    private static void rect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        PremiumRender.roundedRect(context, x, y, width, height, radius, color);
    }

    private static void outline(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        PremiumRender.outline(context, x, y, width, height, radius, color);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Layout(int x, int y, int width, int height, int pad, int headerHeight) {
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
