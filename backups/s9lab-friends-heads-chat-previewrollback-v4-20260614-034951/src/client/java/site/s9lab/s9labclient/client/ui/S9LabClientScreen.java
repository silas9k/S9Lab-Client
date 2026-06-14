package site.s9lab.s9labclient.client.ui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
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
import site.s9lab.s9labclient.client.module.impl.utility.TablistBadgeModule;
import site.s9lab.s9labclient.client.util.S9TextEffects;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.preview.CosmeticPreviewCamera;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class S9LabClientScreen extends ResponsiveScreen {
    private static final Identifier CLIENT_ICON =
            Identifier.of("s9labclient", "textures/font/s9_icon.png");
    private static final Map<Identifier, Boolean> MODULE_ICON_CACHE = new HashMap<>();
    private static final int BG = 0xF005070D;
    private static final int PANEL = 0xD90A0D14;
    private static final int PANEL_2 = 0xB610131C;
    private static final int CARD = 0xB9141822;
    private static final int CARD_HOVER = 0xD91A2030;
    private static final int CARD_ACTIVE = 0xDA23113B;
    private static final int LINE = 0x662C3344;
    private static final int LINE_SOFT = 0x33384255;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE7EAF2;
    private static final int MUTED = 0xFF9AA1B2;
    private static final int DIM = 0xFF687083;
    private static final int GREEN = 0xFF49F26F;
    private static final int WARN = 0xFFFFB454;
    private static final String PLUS_PLAN_1M = "plus_1m";
    private static final String PLUS_PLAN_3M = "plus_3m";
    private static final long PLUS_PRICE_1M = 750L;
    private static final long PLUS_PRICE_3M = 1900L;

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
    private int cosmeticSideScroll;
    private boolean showAllCosmetics;
    private boolean plusShopOpen;
    private boolean showAllModules;
    private boolean moduleDetailsOpen;
    private boolean cosmeticDetailsOpen;
    private final CosmeticPreviewCamera previewCamera = new CosmeticPreviewCamera();
    private boolean searchFocused;
    private String search = "";
    private boolean sortAscending = true;
    private boolean giftDialogOpen;
    private String plusGiftPlan = "";
    private String giftReceiver = "";
    private String giftStatus = "";

    public S9LabClientScreen(Screen parent) {
        this(parent, ClientTab.MODS);
    }

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
        Layout layout = layout();
        Rect activePreview = previewBounds(layout);
        previewCamera.update(deltaTicks, activePreview.contains(mouseX, mouseY), mouseX, mouseY,
                activePreview.x, activePreview.y, activePreview.width, activePreview.height);
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();

        if (isShopLike()) {
            renderShopBackdrop(context);
            renderCosmetics(context, layout, mouseX, mouseY, accent);
            if (giftDialogOpen) {
                renderGiftDialog(context, layout, mouseX, mouseY, accent);
            }
            super.render(context, mouseX, mouseY, deltaTicks);
            return;
        }

        renderShopBackdrop(context);
        renderClientShell(context, layout, mouseX, mouseY, accent);
        renderNotificationBanner(context, layout, mouseX, mouseY, accent);
        switch (selectedTab) {
            case MODS -> renderModsCatalog(context, layout, mouseX, mouseY, accent);
            case COSMETICS -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SHOP -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SETTINGS -> renderSettingsCatalog(context, layout, mouseX, mouseY, accent);
        }
        if (giftDialogOpen) {
            renderGiftDialog(context, layout, mouseX, mouseY, accent);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        Layout layout = layout();

        if (giftDialogOpen) {
            return handleGiftDialogClick(layout, mouseX, mouseY);
        }
        if (doubled && previewBounds(layout).contains(mouseX, mouseY)) {
            resetPreviewCamera();
            return true;
        }
        if (handleNotificationBannerClick(layout, mouseX, mouseY)) {
            return true;
        }
        if (handleFooterTabsClick(layout, mouseX, mouseY)) {
            return true;
        }
        if (!isShopLike() && handleClientShellClick(layout, mouseX, mouseY)) {
            return true;
        }
        if (handleHudDragStart(mouseX, mouseY)) {
            return true;
        }
        boolean handled = switch (selectedTab) {
            case MODS -> handleModsCatalogClick(layout, mouseX, mouseY);
            case COSMETICS -> handleCosmeticClick(layout, mouseX, mouseY);
            case SHOP -> handleCosmeticClick(layout, mouseX, mouseY);
            case SETTINGS -> handleSettingsCatalogClick(layout, mouseX, mouseY);
        };
        if (handled) {
            return true;
        }
        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (previewCamera.dragging()) {
            previewCamera.drag(offsetX, offsetY);
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
        if (previewCamera.dragging()) {
            previewCamera.endDrag();
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
            previewCamera.scroll(verticalAmount);
            return true;
        }
        if (isShopLike()) {
            CosmeticLayout parts = cosmeticLayout(layout);
            if (inside(mouseX, mouseY, parts.sideX, parts.contentY, parts.sideW, parts.contentH)) {
                cosmeticSideScroll = ResponsiveLayout.scroll(cosmeticSideScroll, verticalAmount, maxCosmeticSideScroll(parts.contentH));
                return true;
            }
            if (inside(mouseX, mouseY, parts.gridX, parts.gridY, parts.gridW, parts.gridH)) {
                scroll = ResponsiveLayout.scroll(scroll, verticalAmount, maxScroll(layout));
                return true;
            }
        }
        if (!inside(mouseX, mouseY, layout.bodyX(), layout.bodyY(), layout.bodyWidth(), layout.bodyHeight())) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scroll = ResponsiveLayout.scroll(scroll, verticalAmount, maxScroll(layout));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (giftDialogOpen && input.isValidChar()) {
            giftReceiver = TextLayout.ellipsize(textRenderer, giftReceiver + input.asString(), 190);
            return true;
        }
        if (searchFocused && input.isValidChar()) {
            search = TextLayout.ellipsize(textRenderer, search + input.asString(), 180);
            scroll = 0;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (giftDialogOpen) {
            if (input.isEscape()) {
                giftDialogOpen = false;
                plusGiftPlan = "";
                return true;
            }
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !giftReceiver.isEmpty()) {
                giftReceiver = giftReceiver.substring(0, giftReceiver.length() - 1);
                return true;
            }
            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                confirmGift();
                return true;
            }
        }
        if (input.isEscape()) {
            if (moduleDetailsOpen || cosmeticDetailsOpen) {
                moduleDetailsOpen = false;
                cosmeticDetailsOpen = false;
                previewCamera.endDrag();
                return true;
            }
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
        if (giftDialogOpen) {
            giftDialogOpen = false;
            plusGiftPlan = "";
            return;
        }
        S9LabClientClient.getConfigManager().save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    public static void renderDarkBackground(DrawContext context) {
        PremiumRender.shopBackdrop(context);
    }

    private void renderShopBackdrop(DrawContext context) {
        PremiumRender.shopBackdrop(context);
    }

    private boolean isShopLike() {
        return selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP;
    }

    private String shopTitle() {
        return showAllCosmetics ? "COSMETICS" : cosmeticMenuLabel(selectedCosmeticType).toUpperCase(Locale.ROOT);
    }

    private void renderHeader(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int y = layout.y + 14;
        int logoX = layout.x + 22;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                CLIENT_ICON,
                layout.x + 5,
                layout.y + 16,
                2.0F,
                2.0F,
                16,
                16,
                256,
                256,
                256,
                256
        );

        context.drawTextWithShadow(textRenderer, Text.literal("S9Lab"), logoX, y + 2, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Client"), logoX, y + 14, ClientTheme.withAlpha(accent, 210));

        int tabX = tabStartX(layout);
        for (ClientTab tab : visibleTabs()) {
            int w = tabWidth(tab);
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX, y, w, 24);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.label), tabX + w / 2, y + 7, active ? accent : hovered ? WHITE : MUTED);
            if (active) {
                context.fill(tabX + 10, y + 26, tabX + w - 10, y + 28, accent);
            }
            tabX += w + 12;
        }

        int coinsBoxW = 92;
        int coinsBoxX = layout.x + layout.width - coinsBoxW - 28;
        rect(context, coinsBoxX, y + 1, coinsBoxW, 20, 4, 0x66101520);
        outline(context, coinsBoxX, y + 1, coinsBoxW, 20, 4, 0xFF3A3D45);
        context.drawTextWithShadow(textRenderer, Text.literal(String.valueOf(BackendState.coins())), coinsBoxX + 10, y + 7, WHITE);

        context.fill(layout.x + 14, layout.y + layout.headerHeight - 1, layout.x + layout.width - 14, layout.y + layout.headerHeight, 0xFF25282E);
    }

    private void renderNotificationBanner(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        List<BackendState.Notification> notifications = BackendState.unreadNotificationsSnapshot();
        if (notifications.isEmpty()) {
            return;
        }
        Rect bounds = notificationBannerBounds(layout);
        if (bounds.width <= 0) {
            return;
        }
        BackendState.Notification latest = notifications.get(0);
        boolean hovered = bounds.contains(mouseX, mouseY);
        rect(context, bounds.x, bounds.y, bounds.width, bounds.height, 9, hovered ? 0xF0181E2A : 0xE6111622);
        outline(context, bounds.x, bounds.y, bounds.width, bounds.height, 9, ClientTheme.withAlpha(0xFFFFC857, hovered ? 240 : 185));
        context.drawTextWithShadow(textRenderer, Text.literal("Gift"), bounds.x + 12, bounds.y + 8, 0xFFFFC857);
        String message = notifications.size() == 1
                ? latest.cosmeticName() + " from " + latest.senderName()
                : notifications.size() + " unread gifts";
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, message, bounds.width - 128)), bounds.x + 42, bounds.y + 8, WHITE);

        int readX = bounds.x + bounds.width - 64;
        rect(context, readX, bounds.y + 5, 54, bounds.height - 10, 6, inside(mouseX, mouseY, readX, bounds.y + 5, 54, bounds.height - 10) ? ClientTheme.withAlpha(accent, 210) : 0x77232A36);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Read"), readX + 27, bounds.y + 10, WHITE);
    }



    private void renderMods(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 12;
        int width = layout.width - 40;
        int chipY = y;
        int chipX = x;
        for (ModuleCategory category : ModuleCategory.values()) {
            String label = titleCase(category.name());
            int chipW = Math.max(54, textRenderer.getWidth(label) + 22);
            boolean active = category == selectedCategory;
            boolean hovered = inside(mouseX, mouseY, chipX, chipY, chipW, 20);
            rect(context, chipX, chipY, chipW, 20, 3, active ? accent : hovered ? 0xFF30333A : 0xFF202329);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), chipX + chipW / 2, chipY + 6, active ? WHITE : MUTED);
            chipX += chipW + 8;
        }

        int searchW = 130;
        renderSearch(context, x + width - searchW, y, searchW, mouseX, mouseY, accent, "Search");
        int azX = x + width - searchW - 56;
        rect(context, azX, y, 23, 20, 3, sortAscending ? ClientTheme.withAlpha(accent, 210) : 0xFF22262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("A↓"), azX + 11, y + 6, sortAscending ? WHITE : MUTED);
        rect(context, azX + 28, y, 23, 20, 3, !sortAscending ? ClientTheme.withAlpha(accent, 210) : 0xFF22262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Z↓"), azX + 39, y + 6, !sortAscending ? WHITE : MUTED);

        int gridY = y + 36;
        int gridH = layout.y + layout.height - gridY - 20;
        context.enableScissor(x, gridY, x + width, gridY + gridH);
        Grid grid = grid(width, gridH, 132, 84, 5);
        List<Module> modules = filteredModules();
        int baseY = gridY - scroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cardX = x + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < gridY || cardY > gridY + gridH) continue;
            renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
        }
        context.disableScissor();
    }

    private void renderClientShell(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = cosmeticLayout(layout);
        PremiumRender.shopPanel(context, parts.x, parts.y, parts.width, parts.height, parts.topbarH, parts.footerH);
        renderClientTopbar(context, parts, mouseX, mouseY, accent);
        renderFooterTabs(context, parts, mouseX, mouseY, accent);
        context.fill(parts.gridX - 1, parts.contentY, parts.gridX, parts.y + parts.height - parts.footerH, 0x66FFFFFF);
        if (parts.preview.width > 0) {
            context.fill(parts.preview.x - 1, parts.contentY, parts.preview.x, parts.y + parts.height - parts.footerH, 0x66FFFFFF);
        }
    }

    private void renderClientTopbar(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int x = parts.x + 8;
        String primary = selectedTab == ClientTab.MODS ? titleCase(selectedCategory.name()).toUpperCase(Locale.ROOT) : "SETTINGS";
        renderSquareButton(context, x, buttonY, 116, buttonH, primary, !showAllModules, mouseX, mouseY, accent);
        x += 124;
        renderSquareButton(context, x, buttonY, 58, buttonH, "ALL", showAllModules, mouseX, mouseY, accent);
        x += 66;
        int searchW = Math.max(110, Math.min(220, parts.x + parts.width - x - Math.max(98, parts.width / 8) - 34));
        renderShopSearch(context, x, buttonY, searchW, buttonH, mouseX, mouseY, accent);

        int coinsW = Math.max(78, Math.min(120, parts.width / 8));
        int coinsX = parts.x + parts.width - coinsW - 12;
        rect(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, 0xFF357FB8);
        outline(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, 0xFF65B9F2);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(BackendState.coins() + " COINS"), coinsX + coinsW / 2, buttonY + (buttonH - 8) / 2, WHITE);
    }

    private void renderFooterTabs(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        if (parts.footerH < 28) {
            return;
        }
        int buttonH = Math.min(24, parts.footerH - 12);
        int y = parts.y + parts.height - parts.footerH + (parts.footerH - buttonH) / 2;
        int gap = 8;
        int[] widths = new int[] {70, 86, 96, 86};
        int total = widths[0] + widths[1] + widths[2] + widths[3] + gap * 3;
        int x = parts.x + Math.max(8, (parts.width - total) / 2);
        ClientTab[] tabs = visibleTabs();
        for (int i = 0; i < tabs.length; i++) {
            ClientTab tab = tabs[i];
            renderSquareButton(context, x, y, widths[i], buttonH, tab.label.toUpperCase(Locale.ROOT), selectedTab == tab, mouseX, mouseY, accent);
            x += widths[i] + gap;
        }
    }

    private void renderModsCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = cosmeticLayout(layout);
        if (moduleDetailsOpen && selectedModule != null) {
            renderModuleDetails(context, parts, mouseX, mouseY, accent);
            return;
        }
        renderModuleSidebar(context, parts, mouseX, mouseY, accent);
        context.drawTextWithShadow(textRenderer, Text.literal(showAllModules ? "ALL MODULES" : titleCase(selectedCategory.name()).toUpperCase(Locale.ROOT)), parts.gridX, parts.contentY + 12, WHITE);
        context.fill(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 0x44FFFFFF);

        context.enableScissor(parts.gridX, parts.gridY, parts.gridX + parts.gridW, parts.gridY + parts.gridH);
        Grid grid = grid(parts.gridW, parts.gridH, 136, 116, 4);
        List<Module> modules = filteredModules();
        int baseY = parts.gridY - scroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cardX = parts.gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < parts.gridY || cardY > parts.gridY + parts.gridH) continue;
            renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
        }
        context.disableScissor();

        if (parts.preview.width > 0) {
            renderModulePreviewPanel(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, mouseX, mouseY, accent);
        }
    }

    private void renderSettingsCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = cosmeticLayout(layout);
        Module module = selectedModule;
        if (module == null) {
            List<Module> modules = filteredModules();
            if (!modules.isEmpty()) {
                module = modules.get(0);
                selectedModule = module;
            }
        }
        if (module != null) {
            renderModuleDetails(context, parts, mouseX, mouseY, accent);
            return;
        }
        renderModuleSidebar(context, parts, mouseX, mouseY, accent);
        context.drawTextWithShadow(textRenderer, Text.literal(module == null ? "SETTINGS" : module.getName().toUpperCase(Locale.ROOT)), parts.gridX, parts.contentY + 12, WHITE);
        context.fill(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 0x44FFFFFF);

        int editorY = parts.gridY;
        boolean editorHovered = inside(mouseX, mouseY, parts.gridX, editorY, Math.min(180, parts.gridW), 28);
        renderSquareButton(context, parts.gridX, editorY, Math.min(180, parts.gridW), 28, "OPEN HUD EDITOR", false, mouseX, mouseY, accent);
        if (module == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a module"), parts.gridX + parts.gridW / 2, editorY + 58, MUTED);
            return;
        }
        int rowY = editorY + 46;
        int colW = parts.gridW < 360 ? parts.gridW : (parts.gridW - 28) / 2;
        renderSettingsLine(context, parts.gridX, rowY, colW, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        rowY += 36;
        int i = 0;
        for (Setting<?> setting : module.getSettings()) {
            int col = parts.gridW < 360 ? 0 : i % 2;
            int row = parts.gridW < 360 ? i : i / 2;
            int sx = parts.gridX + col * (colW + 28);
            int sy = rowY + row * 36;
            if (sy > parts.gridY + parts.gridH - 28) {
                break;
            }
            renderSettingsLine(context, sx, sy, colW, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
            i++;
        }

        if (parts.preview.width > 0) {
            renderModulePreviewPanel(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, mouseX, mouseY, accent);
        }
    }

    private void renderModuleDetails(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Module module = selectedModule;
        if (module == null) {
            return;
        }
        if (module instanceof TablistBadgeModule badgeModule) {
            renderNameEffectSettings(context, parts, badgeModule, mouseX, mouseY, accent);
            return;
        }
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        int bottom = parts.y + parts.height - parts.footerH - 12;
        int height = Math.max(1, bottom - y);
        context.fill(x, y, x + width, y + height, 0x17343A44);
        outline(context, x, y, width, height, 0, 0x44FFFFFF);

        String title = "< " + module.getName().toUpperCase(Locale.ROOT);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, title, width - 270)), x + 14, y + 17, WHITE);

        int topButtonY = y + 10;
        int resetW = 72;
        int onW = 54;
        int resetX = x + width - resetW - 62;
        int onX = resetX - onW - 10;
        renderSquareButton(context, onX, topButtonY, onW, 28, module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        renderSquareButton(context, resetX, topButtonY, resetW, 28, "RESET", false, mouseX, mouseY, accent);
        renderSquareButton(context, resetX + resetW + 10, topButtonY, 34, 28, "R", false, mouseX, mouseY, accent);

        context.fill(x, y + 58, x + width, y + 59, 0x332A2E35);
        int rowY = y + 78;
        rowY = renderModuleDetailSection(context, "KEYBIND", x + 18, rowY, width - 36);
        KeybindSetting keybind = keybindSetting(module);
        renderModuleDetailRow(context, x + 34, rowY, width - 68, keybind == null ? "Key" : keybind.getName(), keybind == null ? "Not Bound" : keybindValue(keybind), false, mouseX, mouseY, accent);
        rowY += 48;
        rowY = renderModuleDetailSection(context, "SETTINGS", x + 18, rowY, width - 36);
        if (module.getSettings().isEmpty()) {
            context.drawTextWithShadow(textRenderer, Text.literal("No settings for this module."), x + 34, rowY + 10, MUTED);
            return;
        }
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof KeybindSetting) {
                continue;
            }
            if (rowY > bottom - 34) {
                break;
            }
            renderModuleDetailRow(context, x + 34, rowY, width - 68, setting.getName(), settingValue(setting), setting instanceof BooleanSetting, mouseX, mouseY, accent);
            rowY += 36;
        }
    }

    private void renderNameEffectSettings(
            DrawContext context,
            CosmeticLayout parts,
            TablistBadgeModule module,
            int mouseX,
            int mouseY,
            int accent
    ) {
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        int bottom = parts.y + parts.height - parts.footerH - 12;
        int height = Math.max(1, bottom - y);
        context.fill(x, y, x + width, y + height, 0xF011141B);
        outline(context, x, y, width, height, 0, 0x554A5160);

        context.drawTextWithShadow(textRenderer, Text.literal("< S9C+ NAMETAG STYLE"), x + 14, y + 13, WHITE);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Choose up to 3 effects. They are decoded by one compact built-in shader marker."),
                x + 14, y + 30, MUTED);

        List<String> selected = module.plusNameEffects();
        int slotY = y + 50;
        int slotGap = 8;
        int slotW = Math.max(90, (width - 28 - slotGap * 2) / 3);
        for (int slot = 0; slot < 3; slot++) {
            int sx = x + 14 + slot * (slotW + slotGap);
            String value = slot < selected.size() ? S9TextEffects.displayName(selected.get(slot)) : "EMPTY SLOT";
            boolean active = slot < selected.size();
            context.fill(sx, slotY, sx + slotW, slotY + 28, active ? 0x55305DB8 : 0x4411161F);
            outline(context, sx, slotY, slotW, 28, 0, active ? accent : 0x664A5160);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(TextLayout.ellipsize(textRenderer, (slot + 1) + ". " + value, slotW - 12)),
                    sx + slotW / 2, slotY + 10, active ? WHITE : MUTED);
        }

        String preview = MinecraftClient.getInstance().getSession() == null
                ? "silas0055"
                : MinecraftClient.getInstance().getSession().getUsername();
        int previewColor = selected.isEmpty() ? WHITE : (S9TextEffects.triggerColor(selected) | 0xFF000000);
        context.drawTextWithShadow(textRenderer, Text.literal(preview), x + 14, y + 88, previewColor);
        context.drawTextWithShadow(textRenderer,
                Text.literal(selected.size() + " / 3 EFFECTS SELECTED"),
                x + 14, y + 104, DIM);
        renderSquareButton(context, x + width - 92, y + 84, 76, 26, "CLEAR", false, mouseX, mouseY, accent);

        int sectionY = y + 132;
        context.drawTextWithShadow(textRenderer, Text.literal("— AVAILABLE SHADER EFFECTS —"), x + 14, sectionY, DIM);
        context.fill(x + 206, sectionY + 5, x + width - 14, sectionY + 6, 0x334A5160);
        int effectsBottom = renderNameEffectGrid(context, module, S9TextEffects.EFFECT_IDS,
                x + 14, sectionY + 18, width - 28, mouseX, mouseY, accent);

        int toggleY = Math.min(effectsBottom + 12, bottom - 68);
        renderNameEffectToggle(context, x + 14, toggleY, width - 28,
                "RENDER EFFECTS ON MY NAME", module.plusNameEffectsEnabled(), mouseX, mouseY, accent);
        renderNameEffectToggle(context, x + 14, toggleY + 32, width - 28,
                "SHOW OTHER PLAYERS' EFFECTS", module.showOtherPlayersNameEffects(), mouseX, mouseY, accent);
    }

    private int renderNameEffectGrid(
            DrawContext context,
            TablistBadgeModule module,
            List<String> ids,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY,
            int accent
    ) {
        int columns = width >= 760 ? 5 : width >= 560 ? 4 : 3;
        int gap = 7;
        int buttonW = Math.max(78, (width - gap * (columns - 1)) / columns);
        int buttonH = 27;
        for (int index = 0; index < ids.size(); index++) {
            String id = ids.get(index);
            int col = index % columns;
            int row = index / columns;
            int bx = x + col * (buttonW + gap);
            int by = y + row * (buttonH + gap);
            boolean selectedEffect = module.isEffectSelected(id);
            boolean hovered = inside(mouseX, mouseY, bx, by, buttonW, buttonH);
            int fill = selectedEffect ? 0x66305DB8 : hovered ? 0x332D6DFF : 0x6611161F;
            context.fill(bx, by, bx + buttonW, by + buttonH, fill);
            outline(context, bx, by, buttonW, buttonH, 0,
                    selectedEffect ? accent : hovered ? 0xAA6B8EFF : 0x664A5160);
            int labelColor = selectedEffect ? S9TextEffects.previewColor(id) : hovered ? WHITE : MUTED;
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(TextLayout.ellipsize(textRenderer, S9TextEffects.displayName(id), buttonW - 18)),
                    bx + buttonW / 2, by + 9, labelColor);
            if (selectedEffect) {
                context.drawTextWithShadow(textRenderer, Text.literal("✓"), bx + buttonW - 12, by + 3, WHITE);
            }
        }
        int rows = (ids.size() + columns - 1) / columns;
        return y + rows * (buttonH + gap) - gap;
    }

    private void renderNameEffectToggle(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            boolean enabled,
            int mouseX,
            int mouseY,
            int accent
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 26);
        context.fill(x, y, x + width, y + 26, hovered ? 0x331E2938 : 0x2211161F);
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 8, y + 8, enabled ? WHITE : MUTED);
        int bx = x + width - 22;
        context.fill(bx, y + 4, bx + 16, y + 20, enabled ? 0x663A6FFF : 0x55252C38);
        outline(context, bx, y + 4, 16, 16, 0, enabled ? accent : 0x668A93A6);
        if (enabled) context.drawCenteredTextWithShadow(textRenderer, Text.literal("✓"), bx + 8, y + 7, WHITE);
    }

    private int renderModuleDetailSection(DrawContext context, String label, int x, int y, int width) {
        context.drawTextWithShadow(textRenderer, Text.literal("- " + label + " -"), x, y, DIM);
        context.fill(x + 92, y + 5, x + width, y + 6, 0x33384255);
        return y + 22;
    }

    private void renderModuleDetailRow(DrawContext context, int x, int y, int width, String label, String value, boolean checkbox, int mouseX, int mouseY, int accent) {
        context.drawTextWithShadow(textRenderer, Text.literal(label.toUpperCase(Locale.ROOT)), x, y + 10, WHITE);
        if (checkbox) {
            boolean enabled = "true".equalsIgnoreCase(value);
            int bx = x + width - 28;
            int by = y + 7;
            context.fill(bx, by, bx + 16, by + 16, enabled ? ClientTheme.withAlpha(accent, 150) : 0x55252C38);
            outline(context, bx, by, 16, 16, 0, enabled ? accent : 0x668A93A6);
            if (enabled) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("✓"), bx + 8, by + 3, WHITE);
            }
        } else {
            int buttonW = Math.min(132, Math.max(80, textRenderer.getWidth(value) + 22));
            int bx = x + width - buttonW;
            renderSquareButton(context, bx, y + 4, buttonW, 24, value, false, mouseX, mouseY, accent);
        }
        context.fill(x, y + 34, x + width, y + 35, 0x332A2E35);
    }

    private void renderModuleSidebar(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int itemY = parts.contentY + 6;
        int rowH = Math.max(18, Math.min(27, parts.height / 15));
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean active = !showAllModules && category == selectedCategory;
            String label = titleCase(category.name()).toUpperCase(Locale.ROOT);
            if (active) {
                context.fill(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
                context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x222F65C8);
            }
            if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x22FFFFFF);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, parts.sideW - 18)), parts.sideX + 12, itemY + (rowH - 8) / 2, active ? accent : WHITE);
            itemY += rowH + 5;
        }
    }

    private void renderModulePreviewPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        Module module = selectedModule;
        context.fill(x, y, x + width, y + height, 0x1EFFFFFF);
        outline(context, x, y, width, height, 0, 0x44FFFFFF);
        String title = module == null ? "EMPTY" : TextLayout.ellipsize(textRenderer, module.getName().toUpperCase(Locale.ROOT), width - 18);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(title), x + width / 2, y + 24, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(module == null ? "SELECT MODULE" : titleCase(module.getCategory().name())), x + width / 2, y + 10, MUTED);
        if (module == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Click a module card"), x + width / 2, y + height / 2, DIM);
            return;
        }
        renderModuleIcon(context, module, x + width / 2, y + Math.max(82, height / 2 - 12), accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 24)), x + width / 2, y + height - 72, MUTED);
        int buttonY = y + height - 46;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(module.isEnabled() ? "ENABLED" : "DISABLED"), x + width / 2, buttonY + 1, module.isEnabled() ? WHITE : MUTED);
        renderMiniSwitch(context, x + width / 2 - 13, buttonY + 15, module.isEnabled(), accent);
    }



    private void renderLeftShell(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, boolean modules) {
        rect(context, x, y, width, height, 14, 0x72070A10);
        outline(context, x, y, width, height, 14, LINE_SOFT);
        if (modules) {
            renderSearch(context, x + 12, y + 16, width - 24, mouseX, mouseY, accent, "Search...");
        }

        if (modules) {
            int itemY = y + 66;
            for (ModuleCategory category : ModuleCategory.values()) {
                boolean active = category == selectedCategory;
                renderSideItem(context, titleCase(category.name()), moduleCount(category), x + 12, itemY, width - 24, 27, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 27), accent);
                itemY += 36;
            }
        } else {
            int listTop = y + 18;
            int listBottom = y + height - 54;
            cosmeticSideScroll = clamp(cosmeticSideScroll, 0, maxCosmeticSideScroll(height));
            context.enableScissor(x + 2, listTop, x + width - 2, listBottom);
            int itemY = listTop - cosmeticSideScroll;
            for (CosmeticType type : CosmeticType.values()) {
                boolean active = type == selectedCosmeticType;
                renderSideItem(context, cosmeticMenuLabel(type), -1, x + 12, itemY, width - 24, 28, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 28), accent);
                itemY += 42;
            }
            context.disableScissor();

            int max = maxCosmeticSideScroll(height);
            if (max > 0) {
                int trackX = x + width - 7;
                int trackY = listTop + 4;
                int trackH = Math.max(1, listBottom - listTop - 8);
                int thumbH = Math.max(18, trackH * trackH / (trackH + max));
                int thumbY = trackY + (trackH - thumbH) * cosmeticSideScroll / max;
                context.fill(trackX, trackY, trackX + 2, trackY + trackH, 0x55222A36);
                context.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, ClientTheme.withAlpha(accent, 180));
            }
        }

        renderBackButton(context, x + 12, y + height - 40, width - 24, 28, inside(mouseX, mouseY, x + 12, y + height - 40, width - 24, 28), accent);
    }


    private void renderModuleRow(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        renderModuleCard(context, module, x, y, width, height, mouseX, mouseY, accent);
    }

    private void renderModuleCard(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean selected = module == selectedModule;
        int fill = selected ? ClientTheme.withAlpha(accent, 55) : hovered ? PremiumRender.SHOP_CARD_HOVER : PremiumRender.SHOP_CARD;
        rect(context, x, y, width, height, 0, fill);
        outline(context, x, y, width, height, 0, selected ? accent : hovered ? 0xCCFFFFFF : 0x66FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName().toUpperCase(Locale.ROOT), width - 10)), x + 6, y + 7, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 42)), x + 6, y + height - 18, DIM);
        

        renderModuleIcon(context, module, x + width / 2, y + height / 2 - 4, accent);
            

        renderCardDetailButton(context, x + width - 28, y + 5, inside(mouseX, mouseY, x + width - 28, y + 5, 22, 22), accent);
        renderMiniSwitch(context, x + width - 34, y + height - 18, module.isEnabled(), accent);
    }

    private void renderCardDetailButton(DrawContext context, int x, int y, boolean hovered, int accent) {
        context.fill(x, y, x + 22, y + 22, hovered ? 0x66416886 : 0x44313A4E);
        outline(context, x, y, 22, 22, 0, hovered ? accent : 0x884F6CCF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("..."), x + 11, y + 6, hovered ? WHITE : MUTED);
    }

    private void renderModuleDetailEmpty(DrawContext context, int x, int y, int width, int height, int accent) {
        rect(context, x, y, width, height, 14, 0x8A0B1019);
        outline(context, x, y, width, height, 14, LINE_SOFT);
        rect(context, x + width / 2 - 26, y + height / 2 - 38, 52, 52, 14, ClientTheme.withAlpha(accent, 70));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S9"), x + width / 2, y + height / 2 - 18, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a module"), x + width / 2, y + height / 2 + 22, MUTED);
    }



    private void renderModuleIcon(DrawContext context, Module module, int cx, int cy, int accent) {
        String iconName = module.getClass().getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);

        Identifier icon = Identifier.of(
                "s9labclient",
                "textures/gui/modules/" + iconName + ".png"
        );

        int size = Math.max(30, Math.min(40, Math.min(cx, cy) / 8));
        int x = cx - size / 2;
        int y = cy - size / 2;

        try {
            boolean hasIcon = MODULE_ICON_CACHE.computeIfAbsent(icon, id ->
                    MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent());
            if (!hasIcon) {
                renderGeneratedModuleIcon(context, module, cx, cy, accent);
                return;
            }
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    icon,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    size,
                    size,
                    1024,
                    1024,
                    1024,
                    1024
            );
        } catch (Exception ignored) {
            renderGeneratedModuleIcon(context, module, cx, cy, accent);
        }
    }

    private void renderGeneratedModuleIcon(DrawContext context, Module module, int cx, int cy, int accent) {
        int x = cx - 25;
        int y = cy - 20;
        rect(context, x, y, 50, 40, 6, 0x77212632);
        outline(context, x, y, 50, 40, 6, ClientTheme.withAlpha(accent, 120));

        String name = module.getName().toLowerCase(Locale.ROOT);
        int soft = ClientTheme.withAlpha(accent, 185);
        int bright = ClientTheme.withAlpha(accent, 245);
        int ink = 0xFFEAF0FF;
        if (name.contains("fps")) {
            context.fill(cx - 13, cy + 7, cx - 8, cy + 12, soft);
            context.fill(cx - 4, cy + 2, cx + 1, cy + 12, bright);
            context.fill(cx + 5, cy - 5, cx + 10, cy + 12, 0xFFEAF0FF);
        } else if (name.contains("coordinate") || name.contains("coords")) {
            context.fill(cx - 13, cy, cx + 14, cy + 2, bright);
            context.fill(cx, cy - 13, cx + 2, cy + 14, bright);
            context.fill(cx + 8, cy - 9, cx + 12, cy - 5, ink);
        } else if (name.contains("ping")) {
            context.fill(cx - 13, cy + 8, cx - 9, cy + 12, soft);
            context.fill(cx - 6, cy + 4, cx - 2, cy + 12, soft);
            context.fill(cx + 1, cy, cx + 5, cy + 12, bright);
            context.fill(cx + 8, cy - 5, cx + 12, cy + 12, ink);
        } else if (name.contains("clock") || name.contains("date")) {
            outline(context, cx - 11, cy - 11, 22, 22, 11, bright);
            context.fill(cx, cy - 8, cx + 2, cy + 2, ink);
            context.fill(cx, cy, cx + 8, cy + 2, ink);
        } else if (name.contains("keystroke")) {
            rect(context, cx - 5, cy - 14, 10, 10, 2, soft);
            rect(context, cx - 17, cy - 2, 10, 10, 2, soft);
            rect(context, cx - 5, cy - 2, 10, 10, 2, bright);
            rect(context, cx + 7, cy - 2, 10, 10, 2, soft);
        } else if (name.contains("zoom")) {
            outline(context, cx - 11, cy - 11, 18, 18, 9, bright);
            context.fill(cx + 5, cy + 6, cx + 15, cy + 9, ink);
        } else if (name.contains("armor")) {
            outline(context, cx - 10, cy - 13, 20, 24, 4, bright);
            context.fill(cx - 7, cy - 3, cx + 8, cy, soft);
        } else if (name.contains("cape")) {
            rect(context, cx - 10, cy - 13, 20, 27, 4, bright);
            context.fill(cx - 6, cy - 9, cx + 6, cy - 6, ink);
        } else if (name.contains("bandana")) {
            rect(context, cx - 15, cy - 5, 30, 9, 4, bright);
            context.fill(cx + 6, cy + 3, cx + 16, cy + 9, soft);
        } else if (name.contains("wing")) {
            rect(context, cx - 17, cy - 8, 15, 20, 7, soft);
            rect(context, cx + 2, cy - 8, 15, 20, 7, bright);
            context.fill(cx - 10, cy - 2, cx + 10, cy + 1, ink);
        } else if (name.contains("hat")) {
            rect(context, cx - 10, cy - 12, 20, 15, 4, bright);
            context.fill(cx - 16, cy + 3, cx + 17, cy + 7, ink);
        } else if (name.contains("halo")) {
            outline(context, cx - 15, cy - 12, 30, 10, 5, bright);
            context.fill(cx - 3, cy + 1, cx + 4, cy + 13, ink);
        } else if (name.contains("glint")) {
            outline(context, cx - 10, cy - 10, 20, 20, 4, bright);
            context.fill(cx - 2, cy - 13, cx + 3, cy + 14, soft);
            context.fill(cx - 13, cy - 2, cx + 14, cy + 3, soft);
        } else if (name.contains("music")) {
            context.fill(cx - 8, cy - 12, cx - 4, cy + 9, bright);
            context.fill(cx - 8, cy - 12, cx + 10, cy - 8, bright);
            rect(context, cx - 15, cy + 6, 9, 7, 4, ink);
            rect(context, cx + 5, cy + 4, 9, 7, 4, ink);
        } else {
            String label = module.getName().isBlank() ? "S9" : module.getName().substring(0, Math.min(2, module.getName().length())).toUpperCase(Locale.ROOT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), cx, cy - 4, bright);
        }
    }

    private void renderMiniSwitch(DrawContext context, int x, int y, boolean enabled, int accent) {
        context.fill(x, y, x + 26, y + 12, enabled ? ClientTheme.withAlpha(accent, 210) : 0xFF454A53);
        int knobX = enabled ? x + 15 : x + 2;
        context.fill(knobX, y + 2, knobX + 8, y + 10, WHITE);
    }

    private void renderModuleSettingsPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        Module module = selectedModule;
        rect(context, x, y, width, height, 14, 0x72070A10);
        outline(context, x, y, width, height, 14, LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal(module.getName()), x + 18, y + 18, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Settings"), x + 18, y + 31, DIM);
        int rowY = y + 62;
        renderSettingRow(context, x + 14, rowY, width - 28, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        rowY += 38;
        for (Setting<?> setting : module.getSettings()) {
            renderSettingRow(context, x + 14, rowY, width - 28, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
            rowY += 38;
            if (rowY > y + height - 20) break;
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


    private void renderSettingsPage(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        Module module = selectedModule;
        if (module == null) {
            List<Module> modules = filteredModules();
            if (!modules.isEmpty()) module = modules.get(0);
        }
        int x = layout.x + 20;
        int y = layout.bodyY() + 10;
        int width = layout.width - 40;
        context.drawTextWithShadow(textRenderer, Text.literal(module == null ? "Settings" : module.getName()), x + 8, y + 5, WHITE);
        context.fill(x, y + 30, x + width, y + 31, 0xFF22262C);

        int editorY = y + 44;
        boolean editorHovered = inside(mouseX, mouseY, x + 8, editorY, 180, 30);
        rect(context, x + 8, editorY, 180, 30, 8, editorHovered ? ClientTheme.withAlpha(accent, 145) : 0x66151A25);
        outline(context, x + 8, editorY, 180, 30, 8, editorHovered ? accent : 0x66333B4C);
        context.drawTextWithShadow(textRenderer, Text.literal("Open HUD Editor"), x + 20, editorY + 10, editorHovered ? WHITE : TEXT);

        if (module == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a module first."), x + width / 2, y + 98, MUTED);
            return;
        }
        int rowY = y + 88;
        int colW = (width - 44) / 2;
        renderSettingsLine(context, x + 8, rowY, colW, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        rowY += 36;
        int i = 0;
        for (Setting<?> setting : module.getSettings()) {
            int col = i % 2;
            int row = i / 2;
            int sx = x + 8 + col * (colW + 28);
            int sy = rowY + row * 36;
            renderSettingsLine(context, sx, sy, colW, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
            i++;
        }
    }

    private void renderSettingsLine(DrawContext context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, int accent) {
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width / 2)), x, y + 8, active ? TEXT : MUTED);
        if ("ON".equals(value) || "OFF".equals(value)) {
            renderMiniSwitch(context, x + width - 32, y + 7, active, accent);
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, 86)), x + width - 92, y + 8, active ? accent : DIM);
        }
        context.fill(x, y + 28, x + width, y + 29, 0x332A2E35);
    }

    private void renderCosmetics(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = cosmeticLayout(layout);
        PremiumRender.shopPanel(context, parts.x, parts.y, parts.width, parts.height, parts.topbarH, parts.footerH);

        renderShopTopbar(context, parts, mouseX, mouseY, accent);
        renderFooterTabs(context, parts, mouseX, mouseY, accent);
        renderShopSidebar(context, parts, mouseX, mouseY, accent);

        context.fill(parts.gridX - 1, parts.contentY, parts.gridX, parts.y + parts.height - parts.footerH, 0x66FFFFFF);
        if (parts.preview.width > 0) {
            context.fill(parts.preview.x - 1, parts.contentY, parts.preview.x, parts.y + parts.height - parts.footerH, 0x66FFFFFF);
        }

        if (plusShopOpen) {
            renderPlusShop(context, parts, mouseX, mouseY, accent);
            return;
        }

        if (cosmeticDetailsOpen && selectedCosmetic != null) {
            renderCosmeticDetails(context, parts, mouseX, mouseY, accent);
            return;
        }

        context.drawTextWithShadow(textRenderer, Text.literal(shopTitle()), parts.gridX, parts.contentY + 12, WHITE);
        context.fill(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 0x44FFFFFF);

        context.enableScissor(parts.gridX, parts.gridY, parts.gridX + parts.gridW, parts.gridY + parts.gridH);
        Grid grid = cosmeticGrid(parts.gridW, parts.gridH);
        List<Cosmetic> cosmetics = filteredCosmetics();
        int baseY = parts.gridY - scroll;
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int cardX = parts.gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < parts.gridY || cardY > parts.gridY + parts.gridH) continue;
            renderCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
        }
        context.disableScissor();

        if (parts.preview.width > 0) {
            renderCosmeticPreview(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, accent);
        }
    }

    private void renderCosmeticDetails(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Cosmetic cosmetic = selectedCosmetic;
        int contentBottom = parts.y + parts.height - parts.footerH;
        int variantX = parts.gridX;
        int variantY = parts.contentY + 18;
        int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
        context.drawTextWithShadow(textRenderer, Text.literal("VARIANTS"), variantX, variantY, WHITE);

        List<Cosmetic> variants = variantsForSelectedCosmetic();
        int thumbY = variantY + 24;
        int thumbSize = Math.min(48, Math.max(34, (contentBottom - thumbY - 10) / Math.max(1, Math.min(6, variants.size())) - 5));
        for (int i = 0; i < variants.size() && i < 7; i++) {
            Cosmetic variant = variants.get(i);
            boolean selected = variant == cosmetic;
            int tx = variantX + 4;
            int ty = thumbY + i * (thumbSize + 7);
            context.fill(tx, ty, tx + thumbSize, ty + thumbSize, selected ? 0x552F65C8 : 0x33212632);
            outline(context, tx, ty, thumbSize, thumbSize, 0, selected ? accent : 0x668A93A6);
            drawCosmeticTexture(context, variant, tx + 4, ty + 4, thumbSize - 8, thumbSize - 8, accent);
        }

        int previewX = variantX + variantW + 16;
        int previewW = Math.max(120, parts.width - (previewX - parts.x) - 26);
        int previewY = parts.contentY;
        int previewH = parts.contentH;
        context.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0x10212632);
        outline(context, previewX, previewY, previewW, previewH, 0, 0x44FFFFFF);

        String type = cosmeticMenuLabel(cosmetic.type()).toUpperCase(Locale.ROOT);
        String title = cosmetic.displayName().toUpperCase(Locale.ROOT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, type, previewW - 40)), previewX + previewW / 2, previewY + 18, MUTED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, title, previewW - 40)), previewX + previewW / 2, previewY + 32, WHITE);

        int backX = previewX + 12;
        renderSquareButton(context, backX, previewY + 12, 62, 24, "< BACK", false, mouseX, mouseY, accent);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            CosmeticPreviewContext.begin(cosmetic);
            try {
                InventoryScreen.drawEntity(
                        context,
                        previewX + Math.max(28, previewW / 6),
                        previewY + 58,
                        previewX + previewW - Math.max(28, previewW / 6),
                        previewY + previewH - 72,
                        previewCamera.zoom(),
                        0.0F,
                        previewCamera.yaw(),
                        previewCamera.pitch(),
                        client.player
                );
            } finally {
                CosmeticPreviewContext.end();
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("DRAG / SCROLL"), previewX + previewW / 2, previewY + previewH - 62, 0xCFE7EAF2);
        } else {
            drawCosmeticTexture(context, cosmetic, previewX + previewW / 2 - 48, previewY + previewH / 2 - 48, 96, 96, accent);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Join a world for 3D preview"), previewX + previewW / 2, previewY + previewH / 2 + 58, MUTED);
        }

        context.drawTextWithShadow(textRenderer, Text.literal("<"), previewX + 14, previewY + previewH / 2, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(">"), previewX + previewW - 22, previewY + previewH / 2, WHITE);

        int buttonY = previewY + previewH - 36;
        int buttonW = Math.min(260, previewW - 40);
        rect(context, previewX + (previewW - buttonW) / 2, buttonY, buttonW, 26, 0, BackendState.online() ? ClientTheme.withAlpha(accent, 210) : 0x55151A25);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(cosmeticActionLabel(cosmetic)), previewX + previewW / 2, buttonY + 9, WHITE);
    }

    private void renderPlusShop(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB CLIENT+"), parts.gridX, parts.contentY + 12, WHITE);
        context.fill(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 0x44FFFFFF);

        boolean stacked = parts.gridW < 250;
        int cardGap = Math.max(8, parts.gridW / 42);
        int cardW = stacked ? Math.max(96, parts.gridW - 4) : Math.max(112, (parts.gridW - cardGap) / 2);
        int cardH = stacked ? Math.max(92, (parts.gridH - cardGap - 10) / 2) : Math.min(170, Math.max(122, parts.gridH - 16));
        int cardY = parts.gridY + 4;
        renderPlusPlanCard(context, parts.gridX, cardY, cardW, cardH, "1 MONTH", PLUS_PRICE_1M, PLUS_PLAN_1M, mouseX, mouseY, accent);
        renderPlusPlanCard(context, stacked ? parts.gridX : parts.gridX + cardW + cardGap, stacked ? cardY + cardH + cardGap : cardY, cardW, cardH, "3 MONTHS", PLUS_PRICE_3M, PLUS_PLAN_3M, mouseX, mouseY, accent);

        if (parts.preview.width <= 0) {
            return;
        }
        int px = parts.preview.x;
        int py = parts.preview.y;
        int pw = parts.preview.width;
        int ph = parts.preview.height;
        context.fill(px, py, px + pw, py + ph, 0x1EFFFFFF);
        outline(context, px, py, pw, ph, 0, 0x44FFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S9LAB CLIENT+"), px + pw / 2, py + 14, WHITE);
        if (BackendState.plusActive()) {
            boolean settingsHovered = inside(mouseX, mouseY, px + pw - 34, py + 8, 24, 22);
            rect(context, px + pw - 34, py + 8, 24, 22, 6, settingsHovered ? ClientTheme.withAlpha(accent, 180) : 0x66151A25);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("..."), px + pw - 22, py + 14, WHITE);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(BackendState.plusActive() ? "ACTIVE" : "NOT ACTIVE"), px + pw / 2, py + 30, BackendState.plusActive() ? GREEN : MUTED);
        int icon = Math.min(72, Math.max(42, pw / 2));
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("s9labclient", "textures/font/s9_icon_plus.png"),
                px + (pw - icon) / 2, py + 58, 0.0F, 0.0F, icon, icon, icon, icon);
        String expires = BackendState.plusActive() ? "UNTIL " + plusExpiryLabel(BackendState.plusExpiresAt()) : "BUY A PLAN";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, expires, pw - 16)), px + pw / 2, py + 142, BackendState.plusActive() ? GREEN : WARN);
        int textY = py + 166;
        context.drawTextWithShadow(textRenderer, Text.literal("- Plus icon in tablist"), px + 14, textY, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("- Plus icon in F5 name"), px + 14, textY + 16, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("- Optional rainbow name"), px + 14, textY + 32, TEXT);
    }

    private void renderPlusPlanCard(DrawContext context, int x, int y, int width, int height, String label, long price, String planId, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        context.fill(x, y, x + width, y + height, hovered ? 0x40364A66 : 0x263B66D9);
        outline(context, x, y, width, height, 0, hovered ? accent : 0x66FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 8, y + 8, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("S9Lab Client+"), x + 8, y + 24, MUTED);
        int icon = Math.min(54, Math.max(34, width / 4));
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("s9labclient", "textures/font/s9_icon_plus.png"),
                x + (width - icon) / 2, y + 44, 0.0F, 0.0F, icon, icon, icon, icon);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(price + " COINS"), x + width / 2, y + height - 48, WARN);
        boolean alreadyActive = BackendState.plusActive();
        boolean canBuy = BackendState.online() && !alreadyActive && BackendState.coins() >= price;
        boolean canGift = BackendState.online() && BackendState.coins() >= price;
        int gap = 6;
        int buttonW = Math.max(34, (width - 24 - gap) / 2);
        int buttonY = y + height - 32;
        int buyColor = canBuy ? ClientTheme.withAlpha(accent, 210) : 0x55151A25;
        rect(context, x + 12, buttonY, buttonW, 24, 0, buyColor);
        outline(context, x + 12, buttonY, buttonW, 24, 0, canBuy ? accent : LINE_SOFT);
        String buyLabel = alreadyActive ? "ACTIVE" : canBuy ? "BUY" : BackendState.online() ? "NO COINS" : "OFFLINE";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(buyLabel), x + 12 + buttonW / 2, buttonY + 8, canBuy ? WHITE : MUTED);
        int giftX = x + 12 + buttonW + gap;
        rect(context, giftX, buttonY, buttonW, 24, 0, canGift ? 0xFF1B2534 : 0x55151A25);
        outline(context, giftX, buttonY, buttonW, 24, 0, canGift ? ClientTheme.withAlpha(accent, 190) : LINE_SOFT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("GIFT"), giftX + buttonW / 2, buttonY + 8, canGift ? WHITE : MUTED);
    }


    private void renderCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        renderCosmetics(context, layout, mouseX, mouseY, accent);
    }

    private void renderShopTopbar(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int x = parts.x + 8;
        renderSquareButton(context, x, buttonY, 72, buttonH, cosmeticMenuLabel(selectedCosmeticType).toUpperCase(Locale.ROOT), !showAllCosmetics && !plusShopOpen, mouseX, mouseY, accent);
        x += 78;
        renderSquareButton(context, x, buttonY, 58, buttonH, "ALL", showAllCosmetics && !plusShopOpen, mouseX, mouseY, accent);
        x += 66;
        int searchW = Math.max(90, Math.min(190, parts.gridX + parts.gridW - x - 10));
        renderShopSearch(context, x, buttonY, searchW, buttonH, mouseX, mouseY, accent);

        int coinsW = Math.max(78, Math.min(120, parts.width / 8));
        int coinsX = parts.x + parts.width - coinsW - 12;
        rect(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, 0xFF357FB8);
        outline(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, 0xFF65B9F2);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(BackendState.coins() + " COINS"), coinsX + coinsW / 2, buttonY + (buttonH - 8) / 2, WHITE);
    }

    private void renderShopSidebar(DrawContext context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int itemY = parts.contentY + 6 - cosmeticSideScroll;
        context.enableScissor(parts.sideX, parts.contentY, parts.sideX + parts.sideW, parts.y + parts.height - parts.footerH);
        for (CosmeticType type : CosmeticType.values()) {
            boolean active = type == selectedCosmeticType && !plusShopOpen;
            int rowH = Math.max(18, Math.min(27, parts.height / 15));
            String label = cosmeticMenuLabel(type).toUpperCase(Locale.ROOT);
            int color = active && !showAllCosmetics ? accent : WHITE;
            if (active && !showAllCosmetics) {
                context.fill(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
                context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x222F65C8);
            }
            if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x22FFFFFF);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(label), parts.sideX + 12, itemY + (rowH - 8) / 2, color);
            itemY += rowH + 5;
        }
        int rowH = Math.max(18, Math.min(27, parts.height / 15));
        boolean plusActive = plusShopOpen;
        if (plusActive) {
            context.fill(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
            context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x222F65C8);
        }
        if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
            context.fill(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 0x22FFFFFF);
        }
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB+"), parts.sideX + 12, itemY + (rowH - 8) / 2, plusActive ? accent : WHITE);
        context.disableScissor();
    }

    private void renderShopSearch(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        context.fill(x, y, x + width, y + height, 0xF0000000);
        outline(context, x, y, width, height, 0, searchFocused ? accent : hovered ? 0xAAFFFFFF : 0xFF5B6473);
        String text = search.isBlank() && !searchFocused ? "SEARCH..." : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text.toUpperCase(Locale.ROOT), width - 10)), x + 5, y + (height - 8) / 2, search.isBlank() && !searchFocused ? MUTED : WHITE);
    }

    private void renderSquareButton(DrawContext context, int x, int y, int width, int height, String label, boolean active, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        int color = active ? 0xFF202736 : hovered ? 0xFF25282E : 0xFF17191D;
        context.fill(x, y, x + width, y + height, color);
        outline(context, x, y, width, height, 0, active ? accent : 0xFF3C414B);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 8)), x + width / 2, y + (height - 8) / 2, active ? WHITE : TEXT);
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean owned = S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id());
        boolean selected = cosmetic == selectedCosmetic;
        BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
        int border = selected || equipped ? ClientTheme.withAlpha(accent, 240) : hovered ? 0xCCFFFFFF : 0x66FFFFFF;
        int tint = switch (cosmetic.type()) {
            case WINGS, GLINT -> 0x333B66D9;
            case EMOTE -> 0x33386E3D;
            default -> 0x26FFFFFF;
        };
        context.fill(x, y, x + width, y + height, hovered ? 0x40364A66 : tint);
        outline(context, x, y, width, height, 0, border);

        String name = TextLayout.ellipsize(textRenderer, cosmetic.displayName().toUpperCase(Locale.ROOT), width - 8);
        context.drawTextWithShadow(textRenderer, Text.literal(name), x + 5, y + 5, WHITE);
        renderCardDetailButton(context, x + width - 28, y + 5, inside(mouseX, mouseY, x + width - 28, y + 5, 22, 22), accent);

        int previewX = x + 10;
        int previewY = y + 18;
        int previewW = width - 20;
        int previewH = height - 38;
        drawCosmeticTexture(context, cosmetic, previewX, previewY, previewW, previewH, accent);

        int badgeW = owned ? 58 : Math.min(width - 26, Math.max(52, textRenderer.getWidth(shop.price() + "✦") + 12));
        int badgeX = x + Math.max(6, (width - badgeW) / 2 - 7);
        int badgeY = y + height - 22;
        if (owned) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(equipped ? "EQUIP" : "OWNED"), x + width / 2, y + height - 16, equipped ? accent : GREEN);
        } else {
            int priceColor = rarityColor(cosmetic);
            context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 17, ClientTheme.withAlpha(priceColor, 75));
            outline(context, badgeX, badgeY, badgeW, 17, 0, priceColor);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(shop.price() + "✦"), badgeX + badgeW / 2, badgeY + 5, priceColor);
        }
        if (owned || equipped) {
            context.drawTextWithShadow(textRenderer, Text.literal("✓"), x + width - 13, y + height - 14, GREEN);
        }
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
            case GLINT -> {
                outline(context, x + 8, y + 8, width - 16, height - 16, 8, ClientTheme.withAlpha(color, 255));
                rect(context, x + width / 2 - 6, y + height / 2 - 6, 12, 12, 6, ClientTheme.withAlpha(color, 190));
            }
            case EMOTE -> {
                rect(context, x + width / 2 - 14, y + 12, 28, 28, 8, ClientTheme.withAlpha(color, 180));
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("♪"), x + width / 2, y + 22, WHITE);
            }
        }
    }

    private void renderCosmeticPreview(DrawContext context, int x, int y, int width, int height, int accent) {
        Cosmetic cosmetic = selectedCosmetic;
        context.fill(x, y, x + width, y + height, 0x1EFFFFFF);
        outline(context, x, y, width, height, 0, 0x44FFFFFF);
        String title = cosmetic == null ? "EMPTY" : TextLayout.ellipsize(textRenderer, cosmetic.displayName().toUpperCase(Locale.ROOT), width - 18);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(title), x + width / 2, y + 22, cosmetic == null ? 0xFFE7EAF2 : WHITE);
        String state = BackendState.online() ? "CUSTOM " + cosmeticMenuLabel(selectedCosmeticType).toUpperCase(Locale.ROOT) : BackendState.status();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, state, width - 18)), x + width / 2, y + 10, BackendState.online() ? 0xDDFFFFFF : WARN);

        int boxY = y + 54;
        int boxH = Math.max(80, height - 112);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            CosmeticPreviewContext.begin(cosmetic);
            try {
                InventoryScreen.drawEntity(
                        context,
                        x + 24,
                        boxY + 10,
                        x + width - 24,
                        boxY + boxH - 8,
                        previewCamera.zoom(),
                        0.0F,
                        previewCamera.yaw(),
                        previewCamera.pitch(),
                        client.player
                );
            } finally {
                CosmeticPreviewContext.end();
            }
            if (height > 260) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("DRAG / SCROLL"), x + width / 2, boxY + boxH - 14, 0xBFE7EAF2);
            }
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Join a world for 3D preview"), x + width / 2, boxY + boxH / 2, MUTED);
        }

        int buttonY = y + height - 46;
        String status = cosmeticActionLabel(cosmetic);
        boolean disabled = cosmetic == null || !BackendState.online();
        boolean owned = cosmetic != null && BackendState.owned(cosmetic.id());
        if (owned) {
            int half = (width - 40) / 2;
            rect(context, x + 16, buttonY, half, 28, 0, disabled ? 0x55151A25 : ClientTheme.withAlpha(accent, 210));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + 16 + half / 2, buttonY + 10, WHITE);
            rect(context, x + 24 + half, buttonY, half, 28, 0, disabled ? 0x55151A25 : 0xFF1B2534);
            outline(context, x + 24 + half, buttonY, half, 28, 0, disabled ? LINE_SOFT : ClientTheme.withAlpha(accent, 190));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Gift"), x + 24 + half + half / 2, buttonY + 10, disabled ? MUTED : WHITE);
        } else {
            rect(context, x + 16, buttonY, width - 32, 28, 0, disabled ? 0x55151A25 : ClientTheme.withAlpha(accent, 210));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + width / 2, buttonY + 10, WHITE);
        }
    }

    private void renderGiftDialog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x77000000);
        int w = Math.min(330, Math.max(260, layout.width / 2));
        int h = 154;
        int x = layout.x + (layout.width - w) / 2;
        int y = layout.y + (layout.height - h) / 2;
        PremiumRender.shopPanel(context, x, y, w, h, 42, 0);
        boolean giftingPlus = plusGiftPlan != null && !plusGiftPlan.isBlank();
        context.drawTextWithShadow(textRenderer, Text.literal(giftingPlus ? "Gift S9Lab Client+" : "Gift Cosmetic"), x + 18, y + 16, WHITE);
        String name = giftingPlus ? (PLUS_PLAN_3M.equals(plusGiftPlan) ? "3 Months" : "1 Month") : selectedCosmetic == null ? "" : selectedCosmetic.displayName();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, name, w - 36)), x + 18, y + 31, MUTED);

        int inputY = y + 58;
        PremiumRender.shopInput(context, x + 18, inputY, w - 36, 30, true, ClientTheme.withAlpha(accent, 180));
        String input = giftReceiver.isBlank() ? "Player name or UUID" : giftReceiver + (System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, input, w - 58)), x + 30, inputY + 10, giftReceiver.isBlank() ? DIM : WHITE);
        if (!giftStatus.isBlank()) {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, giftStatus, w - 36)), x + 18, y + 94, WARN);
        }

        int buttonY = y + h - 38;
        int bw = (w - 46) / 2;
        boolean cancelHovered = inside(mouseX, mouseY, x + 18, buttonY, bw, 26);
        boolean sendHovered = inside(mouseX, mouseY, x + 28 + bw, buttonY, bw, 26);
        rect(context, x + 18, buttonY, bw, 26, 0, cancelHovered ? PremiumRender.SHOP_BUTTON_HOVER : PremiumRender.SHOP_BUTTON);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Cancel"), x + 18 + bw / 2, buttonY + 9, cancelHovered ? WHITE : MUTED);
        rect(context, x + 28 + bw, buttonY, bw, 26, 0, sendHovered ? ClientTheme.withAlpha(accent, 235) : ClientTheme.withAlpha(accent, 190));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Send Gift"), x + 28 + bw + bw / 2, buttonY + 9, WHITE);
    }


    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.y + 14;
        int tabX = tabStartX(layout);
        for (ClientTab tab : visibleTabs()) {
            int w = tabWidth(tab);
            if (inside(mouseX, mouseY, tabX, y, w, 28)) {
                selectedTab = tab;
                searchFocused = false;
                search = "";
                scroll = 0;
                resetPreviewForTab();
                return true;
            }
            tabX += w + 12;
        }
        return false;
    }

    private boolean handleClientShellClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int x = parts.x + 8;
        if (inside(mouseX, mouseY, x, buttonY, 116, buttonH)) {
            showAllModules = false;
            moduleDetailsOpen = false;
            scroll = 0;
            return true;
        }
        x += 124;
        if (inside(mouseX, mouseY, x, buttonY, 58, buttonH)) {
            showAllModules = true;
            moduleDetailsOpen = false;
            scroll = 0;
            return true;
        }
        x += 66;
        int searchW = Math.max(110, Math.min(220, parts.x + parts.width - x - Math.max(98, parts.width / 8) - 34));
        if (inside(mouseX, mouseY, x, buttonY, searchW, buttonH)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        return false;
    }

    private boolean handleFooterTabsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);
        if (parts.footerH < 28) {
            return false;
        }
        int buttonH = Math.min(24, parts.footerH - 12);
        int y = parts.y + parts.height - parts.footerH + (parts.footerH - buttonH) / 2;
        int gap = 8;
        int[] widths = new int[] {70, 86, 96, 86};
        int total = widths[0] + widths[1] + widths[2] + widths[3] + gap * 3;
        int x = parts.x + Math.max(8, (parts.width - total) / 2);
        ClientTab[] tabs = visibleTabs();
        for (int i = 0; i < tabs.length; i++) {
            if (inside(mouseX, mouseY, x, y, widths[i], buttonH)) {
                selectedTab = tabs[i];
                moduleDetailsOpen = false;
                cosmeticDetailsOpen = false;
                searchFocused = false;
                search = "";
                scroll = 0;
                resetPreviewForTab();
                return true;
            }
            x += widths[i] + gap;
        }
        return false;
    }

    private boolean handleModuleSidebarClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);
        int itemY = parts.contentY + 6;
        int rowH = Math.max(18, Math.min(27, parts.height / 15));
        for (ModuleCategory category : ModuleCategory.values()) {
            if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                selectedCategory = category;
                showAllModules = false;
                moduleDetailsOpen = false;
                scroll = 0;
                return true;
            }
            itemY += rowH + 5;
        }
        return false;
    }

    private boolean handleModsCatalogClick(Layout layout, int mouseX, int mouseY) {
        if (moduleDetailsOpen && selectedModule != null) {
            return handleModuleDetailsClick(layout, mouseX, mouseY);
        }
        if (handleModuleSidebarClick(layout, mouseX, mouseY)) {
            return true;
        }
        CosmeticLayout parts = cosmeticLayout(layout);
        Grid grid = grid(parts.gridW, parts.gridH, 136, 116, 4);
        int baseY = parts.gridY - scroll;
        List<Module> modules = filteredModules();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cardX = parts.gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                selectedModule = module;
                if (inside(mouseX, mouseY, cardX + grid.cardW - 28, cardY + 5, 22, 22)) {
                    moduleDetailsOpen = true;
                    scroll = 0;
                    return true;
                }
                if (inside(mouseX, mouseY, cardX + grid.cardW - 38, cardY + grid.cardH - 22, 34, 20)) {
                    module.setEnabled(!module.isEnabled());
                    S9LabClientClient.getConfigManager().save();
                }
                return true;
            }
        }
        if (parts.preview.width > 0 && selectedModule != null) {
            int buttonY = parts.preview.y + parts.preview.height - 46;
            int switchX = parts.preview.x + parts.preview.width / 2 - 13;
            int switchY = buttonY + 15;
            if (inside(mouseX, mouseY, switchX - 4, switchY - 4, 34, 20)) {
                selectedModule.setEnabled(!selectedModule.isEnabled());
                S9LabClientClient.getConfigManager().save();
                return true;
            }
        }
        return false;
    }

    private boolean handleSettingsCatalogClick(Layout layout, int mouseX, int mouseY) {
        if (selectedModule != null) {
            return handleModuleDetailsClick(layout, mouseX, mouseY);
        }
        if (handleModuleSidebarClick(layout, mouseX, mouseY)) {
            return true;
        }
        CosmeticLayout parts = cosmeticLayout(layout);
        if (inside(mouseX, mouseY, parts.gridX, parts.gridY, Math.min(180, parts.gridW), 28)) {
            MinecraftClient.getInstance().setScreen(new HudEditorScreen(this));
            return true;
        }
        Module module = selectedModule;
        if (module == null) {
            List<Module> modules = filteredModules();
            if (!modules.isEmpty()) {
                module = modules.get(0);
                selectedModule = module;
            }
        }
        if (module == null) {
            return false;
        }
        int rowY = parts.gridY + 46;
        int colW = parts.gridW < 360 ? parts.gridW : (parts.gridW - 28) / 2;
        if (inside(mouseX, mouseY, parts.gridX + colW - 36, rowY + 3, 34, 20)) {
            module.setEnabled(!module.isEnabled());
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        rowY += 36;
        int i = 0;
        for (Setting<?> setting : module.getSettings()) {
            int col = parts.gridW < 360 ? 0 : i % 2;
            int row = parts.gridW < 360 ? i : i / 2;
            int sx = parts.gridX + col * (colW + 28);
            int sy = rowY + row * 36;
            if (setting instanceof BooleanSetting && inside(mouseX, mouseY, sx + colW - 36, sy + 3, 34, 20)) {
                changeSetting(module, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            if (!(setting instanceof BooleanSetting) && inside(mouseX, mouseY, sx, sy, colW, 30)) {
                changeSetting(module, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean handleModuleDetailsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);
        Module module = selectedModule;
        if (module == null) {
            return false;
        }
        if (module instanceof TablistBadgeModule badgeModule) {
            return handleNameEffectSettingsClick(parts, badgeModule, mouseX, mouseY);
        }
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        if (inside(mouseX, mouseY, x + 8, y + 6, 120, 34)) {
            moduleDetailsOpen = false;
            return true;
        }
        int topButtonY = y + 10;
        int resetW = 72;
        int onW = 54;
        int resetX = x + width - resetW - 62;
        int onX = resetX - onW - 10;
        if (inside(mouseX, mouseY, onX, topButtonY, onW, 28)) {
            module.setEnabled(!module.isEnabled());
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        if (inside(mouseX, mouseY, resetX, topButtonY, resetW, 28)
                || inside(mouseX, mouseY, resetX + resetW + 10, topButtonY, 34, 28)) {
            resetModule(module);
            S9LabClientClient.getConfigManager().save();
            return true;
        }

        int rowY = y + 78;
        rowY += 22;
        KeybindSetting keybind = keybindSetting(module);
        int rowX = x + 34;
        int rowW = width - 68;
        if (keybind != null && inside(mouseX, mouseY, rowX + rowW - 132, rowY + 4, 132, 24)) {
            changeSetting(module, keybind);
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        rowY += 48 + 22;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof KeybindSetting) {
                continue;
            }
            if (setting instanceof BooleanSetting && inside(mouseX, mouseY, rowX + rowW - 34, rowY + 3, 34, 24)) {
                changeSetting(module, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            if (!(setting instanceof BooleanSetting) && inside(mouseX, mouseY, rowX + rowW - 142, rowY + 4, 142, 24)) {
                changeSetting(module, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            rowY += 36;
        }
        return false;
    }

    private boolean handleNameEffectSettingsClick(
            CosmeticLayout parts,
            TablistBadgeModule module,
            int mouseX,
            int mouseY
    ) {
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        int bottom = parts.y + parts.height - parts.footerH - 12;

        if (inside(mouseX, mouseY, x + 6, y + 4, 210, 34)) {
            moduleDetailsOpen = false;
            return true;
        }
        if (inside(mouseX, mouseY, x + width - 92, y + 84, 76, 26)) {
            module.clearEffects();
            S9LabClientClient.getConfigManager().save();
            return true;
        }

        int gridX = x + 14;
        int gridWidth = width - 28;
        int effectsY = y + 150;
        String clicked = clickedNameEffect(S9TextEffects.EFFECT_IDS, gridX, effectsY, gridWidth, mouseX, mouseY);
        if (clicked != null) {
            module.toggleEffect(clicked);
            S9LabClientClient.getConfigManager().save();
            return true;
        }

        int effectsBottom = nameEffectGridBottom(S9TextEffects.EFFECT_IDS, effectsY, gridWidth);
        int toggleY = Math.min(effectsBottom + 12, bottom - 68);
        if (inside(mouseX, mouseY, gridX, toggleY, gridWidth, 26)) {
            module.setPlusNameEffectsEnabled(!module.plusNameEffectsEnabled());
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        if (inside(mouseX, mouseY, gridX, toggleY + 32, gridWidth, 26)) {
            module.setShowOtherPlayersNameEffects(!module.showOtherPlayersNameEffects());
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        return true;
    }

    private String clickedNameEffect(List<String> ids, int x, int y, int width, int mouseX, int mouseY) {
        int columns = width >= 760 ? 5 : width >= 560 ? 4 : 3;
        int gap = 7;
        int buttonW = Math.max(78, (width - gap * (columns - 1)) / columns);
        int buttonH = 27;
        for (int index = 0; index < ids.size(); index++) {
            int col = index % columns;
            int row = index / columns;
            int bx = x + col * (buttonW + gap);
            int by = y + row * (buttonH + gap);
            if (inside(mouseX, mouseY, bx, by, buttonW, buttonH)) return ids.get(index);
        }
        return null;
    }

    private int nameEffectGridBottom(List<String> ids, int y, int width) {
        int columns = width >= 760 ? 5 : width >= 560 ? 4 : 3;
        int rows = (ids.size() + columns - 1) / columns;
        return y + rows * 34 - 7;
    }

    private boolean handleNotificationBannerClick(Layout layout, int mouseX, int mouseY) {
        List<BackendState.Notification> notifications = BackendState.unreadNotificationsSnapshot();
        if (notifications.isEmpty()) {
            return false;
        }
        Rect bounds = notificationBannerBounds(layout);
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        int readX = bounds.x + bounds.width - 64;
        if (inside(mouseX, mouseY, readX, bounds.y + 5, 54, bounds.height - 10)) {
            BackendClient.markNotificationsRead();
            return true;
        }
        BackendState.Notification latest = notifications.get(0);
        S9LabClientClient.getCosmeticRegistry().get(latest.cosmeticId()).ifPresent(cosmetic -> {
            selectedTab = ClientTab.COSMETICS;
            selectedCosmeticType = cosmetic.type();
            selectedCosmetic = cosmetic;
            search = "";
            searchFocused = false;
            scroll = 0;
            resetPreviewForCosmeticType(cosmetic.type());
        });
        return true;
    }


    private boolean handleModsClick(Layout layout, int mouseX, int mouseY) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 12;
        int width = layout.width - 40;
        int chipX = x;
        for (ModuleCategory category : ModuleCategory.values()) {
            String label = titleCase(category.name());
            int chipW = Math.max(54, textRenderer.getWidth(label) + 22);
            if (inside(mouseX, mouseY, chipX, y, chipW, 20)) {
                selectedCategory = category;
                selectedModule = null;
                scroll = 0;
                return true;
            }
            chipX += chipW + 8;
        }
        int searchW = 130;
        int azX = x + width - searchW - 56;
        if (inside(mouseX, mouseY, azX, y, 23, 20)) {
            sortAscending = true;
            scroll = 0;
            return true;
        }
        if (inside(mouseX, mouseY, azX + 28, y, 23, 20)) {
            sortAscending = false;
            scroll = 0;
            return true;
        }
        if (inside(mouseX, mouseY, x + width - searchW, y, searchW, 20)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        return handleModuleListClick(layout, mouseX, mouseY);
    }


    private boolean handleModuleListClick(Layout layout, int mouseX, int mouseY) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 48;
        int width = layout.width - 40;
        int gridH = layout.y + layout.height - y - 20;
        Grid grid = grid(width, gridH, 132, 84, 5);
        int baseY = y - scroll;
        List<Module> modules = filteredModules();
        for (int i = 0; i < modules.size(); i++) {
            int cardX = x + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                Module module = modules.get(i);
                if (inside(mouseX, mouseY, cardX + grid.cardW - 40, cardY + grid.cardH - 24, 38, 22)) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                } else {
                    selectedModule = module;
                    selectedTab = ClientTab.SETTINGS;
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleInlineModuleSettingsClick(int x, int y, int width, int mouseX, int mouseY) {
        int rowY = y + 84;
        if (inside(mouseX, mouseY, x + 14, rowY, width - 28, 30)) {
            selectedModule.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        rowY += 38;
        for (Setting<?> setting : selectedModule.getSettings()) {
            if (inside(mouseX, mouseY, x + 14, rowY, width - 28, 30)) {
                changeSetting(selectedModule, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            rowY += 38;
        }
        return false;
    }


    private boolean handleSettingsClick(Layout layout, int mouseX, int mouseY) {
        if (selectedModule == null) {
            return false;
        }
        int sideX = layout.x + 26;
        int sideY = layout.bodyY() + 22;
        int sideW = 150;
        int settingsW = 250;
        int listX = sideX + sideW + 22;
        int listW = layout.x + layout.width - listX - 30 - settingsW - 22;
        int x = listX + listW + 22 + 14;
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


    private boolean handleSettingsPageClick(Layout layout, int mouseX, int mouseY) {
        Module module = selectedModule;
        if (module == null) {
            List<Module> modules = filteredModules();
            if (!modules.isEmpty()) module = modules.get(0);
        }
        if (module == null) return false;
        int x = layout.x + 28;
        int y = layout.bodyY() + 68;
        int width = layout.width - 56;
        if (inside(mouseX, mouseY, x, y, 180, 30)) {
            MinecraftClient.getInstance().setScreen(new HudEditorScreen(this));
            return true;
        }
        y += 40;
        int colW = (width - 44) / 2;
        if (inside(mouseX, mouseY, x, y, colW, 30)) {
            module.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        }
        y += 36;
        int i = 0;
        for (Setting<?> setting : module.getSettings()) {
            int col = i % 2;
            int row = i / 2;
            int sx = x + col * (colW + 28);
            int sy = y + row * 36;
            if (inside(mouseX, mouseY, sx, sy, colW, 30)) {
                changeSetting(module, setting);
                S9LabClientClient.getConfigManager().save();
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean handleCosmeticClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);

        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int typeX = parts.x + 8;
        int allX = typeX + 78;
        if (inside(mouseX, mouseY, typeX, buttonY, 72, buttonH)) {
            plusShopOpen = false;
            showAllCosmetics = false;
            cosmeticDetailsOpen = false;
            scroll = 0;
            return true;
        }
        if (inside(mouseX, mouseY, allX, buttonY, 58, buttonH)) {
            plusShopOpen = false;
            showAllCosmetics = true;
            cosmeticDetailsOpen = false;
            scroll = 0;
            return true;
        }
        int searchX = allX + 66;
        int searchW = Math.max(90, Math.min(190, parts.gridX + parts.gridW - searchX - 10));
        if (inside(mouseX, mouseY, searchX, buttonY, searchW, buttonH)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (inside(mouseX, mouseY, parts.sideX, parts.contentY, parts.sideW, parts.contentH)) {
            int itemY = parts.contentY + 6 - cosmeticSideScroll;
            int rowH = Math.max(18, Math.min(27, parts.height / 15));
            for (CosmeticType type : CosmeticType.values()) {
                if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                    plusShopOpen = false;
                    selectedCosmeticType = type;
                    showAllCosmetics = false;
                    cosmeticDetailsOpen = false;
                    selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse(null);
                    scroll = 0;
                    cosmeticSideScroll = clamp(cosmeticSideScroll, 0, maxCosmeticSideScroll(parts.contentH));
                    resetPreviewForCosmeticType(type);
                    return true;
                }
                itemY += rowH + 5;
            }
            if (inside(mouseX, mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                plusShopOpen = true;
                showAllCosmetics = false;
                cosmeticDetailsOpen = false;
                scroll = 0;
                return true;
            }
        }
        if (plusShopOpen) {
            return handlePlusShopClick(parts, mouseX, mouseY);
        }
        if (cosmeticDetailsOpen && selectedCosmetic != null) {
            return handleCosmeticDetailsClick(layout, mouseX, mouseY);
        }
        Grid grid = cosmeticGrid(parts.gridW, parts.gridH);
        int baseY = parts.gridY - scroll;
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            int cardX = parts.gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                Cosmetic cosmetic = cosmetics.get(i);
                selectedCosmetic = cosmetic;
                selectedCosmeticType = cosmetic.type();
                resetPreviewForCosmeticType(cosmetic.type());
                if (inside(mouseX, mouseY, cardX + grid.cardW - 28, cardY + 5, 22, 22)) {
                    cosmeticDetailsOpen = true;
                    scroll = 0;
                    return true;
                }
                return true;
            }
        }
        Rect preview = previewBounds(layout);
        if (preview.width <= 0) {
            return false;
        }
        int actionY = preview.y + preview.height - 46;
        boolean owned = selectedCosmetic != null && BackendState.owned(selectedCosmetic.id());
        if (owned) {
            int half = (preview.width - 40) / 2;
            if (inside(mouseX, mouseY, preview.x + 16, actionY, half, 28)) {
                performCosmeticAction();
                return true;
            }
            if (inside(mouseX, mouseY, preview.x + 24 + half, actionY, half, 28)) {
                openGiftDialog();
                return true;
            }
        } else if (inside(mouseX, mouseY, preview.x + 16, actionY, preview.width - 32, 28)) {
            performCosmeticAction();
            return true;
        }
        if (preview.contains(mouseX, mouseY)) {
            previewCamera.beginDrag();
            return true;
        }
        return false;
    }

    private boolean handleCosmeticDetailsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = cosmeticLayout(layout);
        Cosmetic cosmetic = selectedCosmetic;
        if (cosmetic == null) {
            return false;
        }
        Rect bounds = cosmeticDetailPreviewBounds(layout);
        if (inside(mouseX, mouseY, bounds.x + 12, bounds.y + 12, 62, 24)) {
            cosmeticDetailsOpen = false;
            return true;
        }
        int contentBottom = parts.y + parts.height - parts.footerH;
        int variantX = parts.gridX;
        int variantY = parts.contentY + 42;
        int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
        List<Cosmetic> variants = variantsForSelectedCosmetic();
        int thumbSize = Math.min(48, Math.max(34, (contentBottom - variantY - 10) / Math.max(1, Math.min(6, variants.size())) - 5));
        for (int i = 0; i < variants.size() && i < 7; i++) {
            int tx = variantX + 4;
            int ty = variantY + i * (thumbSize + 7);
            if (inside(mouseX, mouseY, tx, ty, thumbSize, thumbSize)) {
                selectedCosmetic = variants.get(i);
                selectedCosmeticType = selectedCosmetic.type();
                resetPreviewForCosmeticType(selectedCosmetic.type());
                return true;
            }
        }
        if (inside(mouseX, mouseY, bounds.x + 8, bounds.y + bounds.height / 2 - 18, 34, 42)) {
            selectAdjacentCosmeticVariant(-1);
            return true;
        }
        if (inside(mouseX, mouseY, bounds.x + bounds.width - 42, bounds.y + bounds.height / 2 - 18, 34, 42)) {
            selectAdjacentCosmeticVariant(1);
            return true;
        }
        int buttonW = Math.min(260, bounds.width - 40);
        int buttonX = bounds.x + (bounds.width - buttonW) / 2;
        int buttonY = bounds.y + bounds.height - 36;
        if (inside(mouseX, mouseY, buttonX, buttonY, buttonW, 26)) {
            performCosmeticAction();
            return true;
        }
        if (bounds.contains(mouseX, mouseY)) {
            previewCamera.beginDrag();
            return true;
        }
        return false;
    }

    private boolean handlePlusShopClick(CosmeticLayout parts, int mouseX, int mouseY) {
        if (BackendState.plusActive() && parts.preview.width > 0
                && inside(mouseX, mouseY, parts.preview.x + parts.preview.width - 34, parts.preview.y + 8, 24, 22)) {
            S9LabClientClient.getModuleManager().getModule("Tablist Badge").ifPresent(module -> {
                selectedModule = module;
                selectedCategory = ModuleCategory.UTILITY;
                selectedTab = ClientTab.SETTINGS;
                plusShopOpen = false;
            });
            return true;
        }
        boolean stacked = parts.gridW < 250;
        int cardGap = Math.max(8, parts.gridW / 42);
        int cardW = stacked ? Math.max(96, parts.gridW - 4) : Math.max(112, (parts.gridW - cardGap) / 2);
        int cardH = stacked ? Math.max(92, (parts.gridH - cardGap - 10) / 2) : Math.min(170, Math.max(122, parts.gridH - 16));
        int cardY = parts.gridY + 4;
        if (handlePlusPlanButton(parts.gridX, cardY, cardW, cardH, PLUS_PRICE_1M, PLUS_PLAN_1M, mouseX, mouseY)) {
            return true;
        }
        return handlePlusPlanButton(stacked ? parts.gridX : parts.gridX + cardW + cardGap, stacked ? cardY + cardH + cardGap : cardY, cardW, cardH, PLUS_PRICE_3M, PLUS_PLAN_3M, mouseX, mouseY);
    }

    private boolean handlePlusPlanButton(int x, int y, int width, int height, long price, String planId, int mouseX, int mouseY) {
        int gap = 6;
        int buttonW = Math.max(34, (width - 24 - gap) / 2);
        int buttonY = y + height - 32;
        if (inside(mouseX, mouseY, x + 12, buttonY, buttonW, 24)) {
            if (BackendState.online() && !BackendState.plusActive() && BackendState.coins() >= price) {
                BackendClient.buyPlus(planId);
            }
            return true;
        }
        int giftX = x + 12 + buttonW + gap;
        if (inside(mouseX, mouseY, giftX, buttonY, buttonW, 24)) {
            if (BackendState.online() && BackendState.coins() >= price) {
                openPlusGiftDialog(planId);
            }
            return true;
        }
        return false;
    }

    private boolean handleGiftDialogClick(Layout layout, int mouseX, int mouseY) {
        int w = Math.min(330, Math.max(260, layout.width / 2));
        int h = 154;
        int x = layout.x + (layout.width - w) / 2;
        int y = layout.y + (layout.height - h) / 2;
        int buttonY = y + h - 38;
        int bw = (w - 46) / 2;
        if (inside(mouseX, mouseY, x + 18, buttonY, bw, 26)) {
            giftDialogOpen = false;
            plusGiftPlan = "";
            return true;
        }
        if (inside(mouseX, mouseY, x + 28 + bw, buttonY, bw, 26)) {
            confirmGift();
            return true;
        }
        return true;
    }

    private boolean handleCatalogClick(Layout layout, int mouseX, int mouseY) {
        return handleCosmeticClick(layout, mouseX, mouseY);
    }

    private Rect previewBounds(Layout layout) {
        if (selectedTab == ClientTab.MODS && selectedModule == null) {
            return Rect.empty();
        }
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            if (cosmeticDetailsOpen && selectedCosmetic != null) {
                return cosmeticDetailPreviewBounds(layout);
            }
            return cosmeticLayout(layout).preview;
        }
        return Rect.empty();
    }

    private Rect cosmeticDetailPreviewBounds(Layout layout) {
        CosmeticLayout parts = cosmeticLayout(layout);
        int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
        int previewX = parts.gridX + variantW + 16;
        int previewW = Math.max(120, parts.width - (previewX - parts.x) - 26);
        return new Rect(previewX, parts.contentY, previewW, parts.contentH);
    }

    private Rect notificationBannerBounds(Layout layout) {
        int width = Math.min(270, layout.width - 40);
        if (width < 180 || layout.height < 230) {
            return Rect.empty();
        }
        return new Rect(layout.x + layout.width - width - 18, layout.y + layout.headerHeight + 5, width, 28);
    }

    private boolean handleHudDragStart(int mouseX, int mouseY) {
        if (moduleDetailsOpen || cosmeticDetailsOpen) {
            return false;
        }
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
        boolean hovered = inside(mouseX, mouseY, x, y, width, 32);
        rect(context, x, y, width, 32, 10, 0x8A0A0D14);
        outline(context, x, y, width, 32, 10, searchFocused ? ClientTheme.withAlpha(accent, 230) : hovered ? 0xAA3B455B : LINE_SOFT);
        String text = search.isBlank() && !searchFocused ? placeholder : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal("⌕"), x + 13, y + 11, DIM);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, text, width - 42)), x + 32, y + 11, search.isBlank() && !searchFocused ? DIM : WHITE);
    }


    private void renderSideItem(DrawContext context, String label, int count, int x, int y, int width, int height, boolean active, boolean hovered, int accent) {
        if (active) {
            rect(context, x, y, width, height, 9, ClientTheme.withAlpha(accent, 145));
            rect(context, x, y + 5, 3, height - 10, 2, WHITE);
        } else if (hovered) {
            rect(context, x, y, width, height, 9, 0x3D252C3C);
        }
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 14, y + (height - 8) / 2, active ? WHITE : hovered ? TEXT : MUTED);
        if (count >= 0) {
            context.drawTextWithShadow(textRenderer, Text.literal(String.valueOf(count)), x + width - 24, y + (height - 8) / 2, active ? WHITE : DIM);
        }
    }


    private void renderBackButton(DrawContext context, int x, int y, int width, int height, boolean hovered, int accent) {
        rect(context, x, y, width, height, 8, hovered ? 0x5518202E : 0x330B0E15);
        outline(context, x, y, width, height, 8, hovered ? accent : LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal("‹  Back"), x + 14, y + 9, hovered ? WHITE : MUTED);
    }

    private void renderSwitch(DrawContext context, int x, int y, boolean enabled, int accent) {
        rect(context, x, y, 40, 20, 10, enabled ? ClientTheme.withAlpha(accent, 220) : 0xFF333A4B);
        int knobX = enabled ? x + 22 : x + 3;
        rect(context, knobX, y + 3, 14, 14, 7, WHITE);
    }


    private void resetPreviewCamera() {
        previewCamera.reset(180.0F, 8.0F, 78.0F);
    }

    private void resetPreviewForTab() {
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(selectedCosmeticType).orElse(selectedCosmetic);
            resetPreviewForCosmeticType(selectedCosmeticType);
        }
    }

    private void resetPreviewForCosmeticType(CosmeticType type) {
        float zoom = switch (type) {
            case CAPE, WINGS -> 82.0F;
            case HALO, HAT, GLINT, EMOTE -> 96.0F;
            default -> 78.0F;
        };
        float yaw = switch (type) {
            case CAPE, WINGS -> 180.0F;
            default -> 25.0F;
        };
        float pitch = switch (type) {
            case HALO, HAT -> -8.0F;
            default -> 8.0F;
        };
        previewCamera.reset(yaw, pitch, zoom);
    }

    private int moduleCount(ModuleCategory category) {
        return (int) S9LabClientClient.getModuleManager().getModules().stream()
                .filter(module -> module.getCategory() == category)
                .count();
    }

    private List<Module> filteredModules() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        Comparator<Module> comparator = Comparator.comparing(module -> module.getName().toLowerCase(Locale.ROOT));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return S9LabClientClient.getModuleManager().getModules().stream()
                .filter(module -> showAllModules || module.getCategory() == selectedCategory)
                .filter(module -> query.isEmpty()
                        || module.getName().toLowerCase(Locale.ROOT).contains(query)
                        || module.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .sorted(comparator)
                .toList();
    }

    private List<Cosmetic> filteredCosmetics() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        Comparator<Cosmetic> comparator = Comparator.comparing(cosmetic -> cosmetic.displayName().toLowerCase(Locale.ROOT));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return S9LabClientClient.getCosmeticRegistry().all().stream()
                .filter(cosmetic -> showAllCosmetics || cosmetic.type() == selectedCosmeticType)
                .filter(cosmetic -> query.isEmpty()
                        || cosmetic.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || cosmetic.id().toLowerCase(Locale.ROOT).contains(query))
                .sorted(comparator)
                .toList();
    }

    private List<Cosmetic> variantsForSelectedCosmetic() {
        Cosmetic cosmetic = selectedCosmetic;
        CosmeticType type = cosmetic == null ? selectedCosmeticType : cosmetic.type();
        return S9LabClientClient.getCosmeticRegistry().all().stream()
                .filter(variant -> variant.type() == type)
                .sorted(Comparator.comparing(variant -> variant.displayName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private void selectAdjacentCosmeticVariant(int direction) {
        List<Cosmetic> variants = variantsForSelectedCosmetic();
        if (variants.isEmpty()) {
            return;
        }
        int index = Math.max(0, variants.indexOf(selectedCosmetic));
        selectedCosmetic = variants.get(Math.floorMod(index + direction, variants.size()));
        selectedCosmeticType = selectedCosmetic.type();
        resetPreviewForCosmeticType(selectedCosmetic.type());
    }

    private KeybindSetting keybindSetting(Module module) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof KeybindSetting keybindSetting) {
                return keybindSetting;
            }
        }
        return null;
    }

    private static String keybindValue(KeybindSetting keybindSetting) {
        return keybindSetting.getValue() == 0 ? "Not Bound" : String.valueOf(keybindSetting.getValue());
    }

    private void resetModule(Module module) {
        module.setEnabled(false);
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                booleanSetting.setValue(false);
            } else if (setting instanceof KeybindSetting keybindSetting) {
                keybindSetting.setValue(0);
            } else if (setting instanceof ModeSetting modeSetting && !modeSetting.getModes().isEmpty()) {
                modeSetting.setValue(modeSetting.getModes().get(0));
            } else if (setting instanceof NumberSetting numberSetting) {
                numberSetting.setValue(numberSetting.getMin());
            }
            syncSettingToCosmetic(module, setting);
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
            case GLINT -> "Glint";
            case EMOTE -> "S9 Emote";
        };
        S9LabClientClient.getModuleManager().getModule(moduleName).ifPresent(module -> {
            module.setEnabled(true);
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.setValue(cosmetic.type() == CosmeticType.EMOTE
                            ? cosmetic.id().replace("s9lab_emote_", "")
                            : cosmetic.id());
                }
            }
        });
    }

    private String cosmeticActionLabel(Cosmetic cosmetic) {
        if (cosmetic == null) {
            return "Select cosmetic";
        }
        if (!BackendState.online()) {
            return "Backend offline";
        }
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        if (equipped) {
            return "Unequip";
        }
        if (S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id())) {
            return "Equip";
        }
        BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
        return BackendState.online() ? "Buy - " + shop.price() + " coins" : "Backend offline";
    }

    private void performCosmeticAction() {
        Cosmetic cosmetic = selectedCosmetic;
        if (cosmetic == null) {
            return;
        }
        if (!BackendState.online()) {
            return;
        }
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        if (equipped) {
            BackendClient.unequipCosmetic(cosmetic.type());
            return;
        }
        if (S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id())) {
            syncModuleSelection(cosmetic);
            BackendClient.equipCosmetic(cosmetic.type(), cosmetic.id());
            return;
        }
        BackendClient.buyCosmetic(cosmetic.id());
    }

    private void openGiftDialog() {
        if (selectedCosmetic == null || !BackendState.online() || !BackendState.owned(selectedCosmetic.id())) {
            return;
        }
        plusGiftPlan = "";
        giftReceiver = "";
        giftStatus = "";
        giftDialogOpen = true;
        searchFocused = false;
    }

    private void openPlusGiftDialog(String planId) {
        if (!BackendState.online() || planId == null || planId.isBlank()) {
            return;
        }
        plusGiftPlan = planId;
        giftReceiver = "";
        giftStatus = "";
        giftDialogOpen = true;
        searchFocused = false;
    }

    private void confirmGift() {
        boolean giftingPlus = plusGiftPlan != null && !plusGiftPlan.isBlank();
        if (!giftingPlus && selectedCosmetic == null) {
            giftDialogOpen = false;
            return;
        }
        String receiver = giftReceiver.trim();
        if (receiver.isBlank()) {
            giftStatus = "Enter a player name or UUID.";
            return;
        }
        if (giftingPlus) {
            BackendClient.giftPlus(receiver, plusGiftPlan);
        } else {
            BackendClient.giftCosmetic(receiver, selectedCosmetic.id());
        }
        giftStatus = "Gift request sent.";
        giftDialogOpen = false;
        plusGiftPlan = "";
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
            case "glint" -> CosmeticType.GLINT;
            case "s9 emote" -> CosmeticType.EMOTE;
            default -> null;
        };
        if (type == null) {
            return;
        }
        String cosmeticId = type == CosmeticType.EMOTE ? "s9lab_emote_" + modeSetting.getValue() : modeSetting.getValue();
        S9LabClientClient.getCosmeticRegistry().get(cosmeticId)
                .filter(cosmetic -> cosmetic.type() == type)
                .filter(cosmetic -> S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id()))
                .filter(cosmetic -> BackendState.online())
                .ifPresent(cosmetic -> BackendClient.equipCosmetic(type, cosmetic.id()));
    }

    private int maxCosmeticSideScroll(int panelH) {
        int total = (CosmeticType.values().length + 1) * 42 + 28;
        int visible = Math.max(1, panelH - 72);
        return Math.max(0, total - visible);
    }

    private int maxScroll(Layout layout) {
        if (selectedTab == ClientTab.MODS) {
            if (moduleDetailsOpen) {
                return 0;
            }
            CosmeticLayout parts = cosmeticLayout(layout);
            Grid grid = grid(parts.gridW, parts.gridH, 136, 116, 4);
            return Math.max(0, rows(filteredModules().size(), grid.columns) * (grid.cardH + grid.gap) - parts.gridH);
        }
        if (selectedTab == ClientTab.SETTINGS) {
            return 0;
        }
        if (cosmeticDetailsOpen) {
            return 0;
        }
        if (plusShopOpen) {
            return 0;
        }
        CosmeticLayout parts = cosmeticLayout(layout);
        Grid grid = cosmeticGrid(parts.gridW, parts.gridH);
        int rows = rows(filteredCosmetics().size(), grid.columns);
        return Math.max(0, rows * (grid.cardH + grid.gap) - parts.gridH);
    }


    private void clampScroll() {
        scroll = clamp(scroll, 0, maxScroll(layout()));
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(960, 520, 360, 250);
        int header = screen.height() < 300 ? 42 : 48;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header);
    }


    private CosmeticLayout cosmeticLayout(Layout layout) {
        int margin = Math.max(6, layout.pad / 2);
        int x = layout.x + margin;
        int y = layout.y + margin;
        int width = Math.max(1, layout.width - margin * 2);
        int height = Math.max(1, layout.y + layout.height - y - margin);
        int topbarH = height < 260 ? 38 : 48;
        int footerH = height < 270 ? 30 : 44;
        int contentY = y + topbarH;
        int contentH = Math.max(1, height - topbarH - footerH);
        int gap = Math.max(5, ResponsiveLayout.adaptiveGap(width, height));
        int sideW = clamp(width / 8, 70, 126);
        int previewW = clamp(width / 4, 116, 238);
        if (width < 470) {
            previewW = clamp(width / 4, 96, 128);
        }
        int sideX = x;
        int previewX = x + width - previewW;
        int gridX = sideX + sideW + gap;
        int gridW = previewX - gridX - gap;
        if (gridW < 96) {
            previewW = Math.max(90, previewW - (96 - gridW));
            previewX = x + width - previewW;
            gridW = Math.max(96, previewX - gridX - gap);
        }
        int gridY = contentY + (height < 290 ? 42 : 58);
        int gridH = Math.max(1, y + height - footerH - gridY);
        Rect preview = new Rect(previewX, contentY, previewW, contentH);
        return new CosmeticLayout(x, y, width, height, topbarH, footerH, contentY, contentH, sideX, sideW, gridX, gridY, gridW, gridH, preview);
    }

    private Grid cosmeticGrid(int width, int height) {
        int gap = Math.max(6, ResponsiveLayout.adaptiveGap(width, height));
        int minCardW = width < 270 ? 86 : 106;
        int columns = Math.max(1, Math.min(4, (width + gap) / Math.max(1, minCardW + gap)));
        int cardW = Math.max(76, (width - gap * (columns - 1)) / columns);
        int cardH = clamp(Math.round(cardW * 1.08F), 96, height < 210 ? 112 : 148);
        return new Grid(columns, gap, cardW, cardH);
    }

    private Grid grid(int width, int height, int minCardW, int preferredCardH, int maxColumns) {
        int gap = ResponsiveLayout.adaptiveGap(width, height);
        int columns = Math.max(1, Math.min(maxColumns, (width + gap) / Math.max(1, minCardW + gap)));
        int cardW = Math.max(64, (width - gap * (columns - 1)) / columns);
        int cardH = Math.max(64, preferredCardH);
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

    private static String plusExpiryLabel(long expiresAt) {
        long secondsLeft = expiresAt - System.currentTimeMillis() / 1000L;
        if (secondsLeft <= 0L) {
            return "EXPIRED";
        }
        long days = Math.max(1L, secondsLeft / 86_400L);
        return days + " DAY" + (days == 1L ? "" : "S");
    }

    private static String cosmeticMenuLabel(CosmeticType type) {
        return switch (type) {
            case CAPE -> "Cape";
            case BANDANA -> "Bandana";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Shoulder";
            case GLINT -> "Glint";
            case EMOTE -> "Emotes";
        };
    }

    private static ClientTab[] visibleTabs() {
        return new ClientTab[]{ClientTab.MODS, ClientTab.SETTINGS, ClientTab.COSMETICS, ClientTab.SHOP};
    }

    private int tabStartX(Layout layout) {
        int total = 0;
        for (ClientTab tab : visibleTabs()) {
            total += tabWidth(tab);
        }
        total += (visibleTabs().length - 1) * 12;
        int minX = layout.x + 72;
        int maxX = layout.x + layout.width - total - 130;
        return clamp(layout.x + layout.width / 2 - total / 2, minX, Math.max(minX, maxX));
    }

    private int tabWidth(ClientTab tab) {
        int base = textRenderer == null ? tab.label.length() * 7 : textRenderer.getWidth(tab.label);
        return Math.max(48, Math.min(78, base + 20));
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
        private int contentHeight(int itemCount) {
            return rows(itemCount, columns) * (cardH + gap);
        }
    }

    private record CosmeticLayout(
            int x,
            int y,
            int width,
            int height,
            int topbarH,
            int footerH,
            int contentY,
            int contentH,
            int sideX,
            int sideW,
            int gridX,
            int gridY,
            int gridW,
            int gridH,
            Rect preview
    ) {
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
