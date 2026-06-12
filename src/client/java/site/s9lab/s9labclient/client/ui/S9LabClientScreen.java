package site.s9lab.s9labclient.client.ui;

import java.util.Comparator;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.S9LabClient;
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
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;
import site.s9lab.s9labclient.client.util.S9BadgeText;

public class S9LabClientScreen extends ResponsiveScreen {
    private static final Identifier CLIENT_ICON = Identifier.of(S9LabClient.MOD_ID, "icon.png");
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
    private boolean previewDragging;
    private float previewYaw = 180.0F;
    private float previewPitch = 8.0F;
    private int previewZoom = 78;
    private boolean searchFocused;
    private String search = "";
    private boolean sortAscending = true;
    private boolean giftDialogOpen;
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
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();

        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x66000000);
        context.fill(layout.x + 3, layout.y + 3, layout.x + layout.width + 3, layout.y + layout.height + 3, 0x77000000);
        rect(context, layout.x, layout.y, layout.width, layout.height, 2, 0xE914161A);
        context.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.headerHeight, 0xDD1A1C21);
        outline(context, layout.x, layout.y, layout.width, layout.height, 2, 0xFF2D3138);

        renderHeader(context, layout, mouseX, mouseY, accent);
        renderNotificationBanner(context, layout, mouseX, mouseY, accent);
        switch (selectedTab) {
            case MODS -> renderMods(context, layout, mouseX, mouseY, accent);
            case COSMETICS -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SHOP -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SETTINGS -> renderSettingsPage(context, layout, mouseX, mouseY, accent);
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
        if (handleHeaderClick(layout, mouseX, mouseY)) {
            return true;
        }
        if (handleHudDragStart(mouseX, mouseY)) {
            return true;
        }
        boolean handled = switch (selectedTab) {
            case MODS -> handleModsClick(layout, mouseX, mouseY);
            case COSMETICS -> handleCosmeticClick(layout, mouseX, mouseY);
            case SHOP -> handleCosmeticClick(layout, mouseX, mouseY);
            case SETTINGS -> handleSettingsPageClick(layout, mouseX, mouseY);
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
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            int sideX = layout.x + 26;
            int sideY = layout.bodyY() + 18;
            int sideW = 132;
            int panelH = layout.y + layout.height - sideY - 24;
            if (inside(mouseX, mouseY, sideX, sideY, sideW, panelH)) {
                cosmeticSideScroll = ResponsiveLayout.scroll(cosmeticSideScroll, verticalAmount, maxCosmeticSideScroll(panelH));
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
            return;
        }
        S9LabClientClient.getConfigManager().save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    public static void renderDarkBackground(DrawContext context) {
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x66000000);
    }

    private void renderHeader(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        int y = layout.y + 14;
        int logoX = layout.x + 22;
        rect(context, logoX, y + 1, 26, 26, 7, 0x77101420);
        outline(context, logoX, y + 1, 26, 26, 7, ClientTheme.withAlpha(accent, 150));
        context.drawTexture(RenderPipelines.GUI_TEXTURED, CLIENT_ICON, logoX + 4, y + 5, 0.0F, 0.0F, 18, 18, 1254, 1254);
        context.drawTextWithShadow(textRenderer, Text.literal("S9Lab"), logoX + 34, y + 3, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Client"), logoX + 34, y + 15, ClientTheme.withAlpha(accent, 210));

        int tabX = layout.x + layout.width / 2 - 118;
        for (ClientTab tab : visibleTabs()) {
            int w = tab == ClientTab.COSMETICS ? 82 : 70;
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX, y, w, 24);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.label), tabX + w / 2, y + 7, active ? accent : hovered ? WHITE : MUTED);
            if (active) {
                context.fill(tabX + 10, y + 26, tabX + w - 10, y + 28, accent);
            }
            tabX += w + 18;
        }

        int coinsBoxW = 116;
        int coinsBoxX = layout.x + layout.width - coinsBoxW - 28;
        // rect(context, coinsBoxX, y + 1, coinsBoxW, 20, 4, 0x66101520);
        outline(context, coinsBoxX, y + 1, coinsBoxW, 20, 2, BackendState.online() ? ClientTheme.withAlpha(accent, 210) : 0xFF3A3D45);
        // rect(context, coinsBoxX + 8, y + 5, 10, 10, 5, BackendState.online() ? 0xFF2EE86B : 0xFFB94848);
        context.drawTextWithShadow(textRenderer, Text.literal(BackendState.coins() + " coins"), coinsBoxX + 24, y + 7, WHITE);

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
        int header = module.isEnabled() ? ClientTheme.withAlpha(accent, 120) : 0xFF30333A;
        rect(context, x, y, width, height, 2, hovered ? 0xE224272D : 0xD91B1E24);
        outline(context, x, y, width, height, 2, selected ? accent : hovered ? 0xFF4A4E58 : 0xFF30343C);
        context.fill(x, y, x + width, y + 22, header);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName().toUpperCase(Locale.ROOT), width - 8)), x + width / 2, y + 7, WHITE);
        renderModuleIcon(context, module, x + width / 2, y + 47, accent);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getDescription(), width - 12)), x + 6, y + height - 16, DIM);
        renderMiniSwitch(context, x + width - 34, y + height - 18, module.isEnabled(), accent);
    }



    private void renderModuleIcon(DrawContext context, Module module, int cx, int cy, int accent) {
        int color = module.isEnabled() ? accent : 0xFF8B92A0;
        String initial = module.getName().isEmpty() ? "?" : module.getName().substring(0, 1).toUpperCase(Locale.ROOT);
        if (module.getCategory() == ModuleCategory.HUD) {
            outline(context, cx - 15, cy - 13, 30, 24, 2, color);
            context.fill(cx - 10, cy + 5, cx + 10, cy + 7, ClientTheme.withAlpha(color, 160));
        } else if (module.getCategory() == ModuleCategory.COSMETICS) {
            outline(context, cx - 14, cy - 14, 28, 28, 2, color);
        } else {
            rect(context, cx - 14, cy - 14, 28, 28, 4, ClientTheme.withAlpha(color, 45));
            outline(context, cx - 14, cy - 14, 28, 28, 4, color);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(initial), cx, cy - 4, WHITE);
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
        int sideX = layout.x + 26;
        int sideY = layout.bodyY() + 18;
        int sideW = 132;
        int panelH = layout.y + layout.height - sideY - 24;
        int previewW = Math.max(190, Math.min(235, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 28;
        int gridX = sideX + sideW + 18;
        int searchY = sideY;
        int searchH = 32;
        int gridY = searchY + searchH + 16;
        int gridW = previewX - gridX - 18;
        int gridH = layout.y + layout.height - gridY - 24;

        renderLeftShell(context, sideX, sideY, sideW, panelH, mouseX, mouseY, accent, false);
        renderSearch(context, gridX, searchY, gridW - 66, mouseX, mouseY, accent, "Search cosmetics...");

        int sortX = gridX + gridW - 58;
        rect(context, sortX, searchY + 6, 23, 20, 3, sortAscending ? ClientTheme.withAlpha(accent, 210) : 0xFF22262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("A↓"), sortX + 11, searchY + 12, sortAscending ? WHITE : MUTED);
        rect(context, sortX + 28, searchY + 6, 23, 20, 3, !sortAscending ? ClientTheme.withAlpha(accent, 210) : 0xFF22262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Z↓"), sortX + 39, searchY + 12, !sortAscending ? WHITE : MUTED);

        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        Grid grid = grid(gridW, gridH, 105, 112, 4);
        List<Cosmetic> cosmetics = filteredCosmetics();
        int baseY = gridY - scroll;
        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            int cardX = gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (cardY + grid.cardH < gridY || cardY > gridY + gridH) continue;
            renderCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
        }
        context.disableScissor();

        renderCosmeticPreview(context, previewX, sideY, previewW, panelH, accent);
    }


    private void renderCatalog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        renderCosmetics(context, layout, mouseX, mouseY, accent);
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean owned = S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id());
        boolean selected = cosmetic == selectedCosmetic;
        BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
        rect(context, x, y, width, height, 8, selected ? ClientTheme.withAlpha(accent, 44) : hovered ? CARD_HOVER : CARD);
        outline(context, x, y, width, height, 8, selected || equipped ? ClientTheme.withAlpha(accent, 235) : hovered ? 0xAA45516B : owned ? 0x8849F26F : LINE_SOFT);
        if (equipped) {
            rect(context, x + width - 19, y + 7, 12, 12, 6, accent);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("✓"), x + width - 13, y + 10, WHITE);
        } else if (!owned) {
            rect(context, x + width - 23, y + 7, 16, 12, 4, 0xAA1A1E28);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("¢"), x + width - 15, y + 10, WARN);
        }

        int previewX = x + 12;
        int previewY = y + 14;
        int previewW = width - 24;
        int previewH = height - 54;
        rect(context, previewX, previewY, previewW, previewH, 7, 0x55101520);
        outline(context, previewX, previewY, previewW, previewH, 7, 0x22384255);
        drawCosmeticTexture(context, cosmetic, previewX + 8, previewY + 7, previewW - 16, previewH - 14, accent);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 14)), x + width / 2, y + height - 31, WHITE);
        String footer = owned ? cosmeticRarity(cosmetic) : shop.price() + " coins";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(footer), x + width / 2, y + height - 17, owned ? rarityColor(cosmetic) : WARN);
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
        rect(context, x, y, width, height, 12, 0x82070A10);
        outline(context, x, y, width, height, 12, LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal(cosmetic == null ? "Preview" : TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 34)), x + 16, y + 16, WHITE);
        String state = BackendState.online() ? "Backend synced" : BackendState.status();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, state, width - 34)), x + 16, y + 30, BackendState.online() ? GREEN : WARN);

        int boxY = y + 58;
        int boxH = height - 116;
        rect(context, x + 14, boxY, width - 28, boxH, 12, 0x68101520);
        outline(context, x + 14, boxY, width - 28, boxH, 12, 0x22384255);
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
                        previewZoom,
                        previewYaw,
                        previewPitch,
                        client.player
                );
            } finally {
                CosmeticPreviewContext.end();
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag to rotate, scroll to zoom"), x + width / 2, boxY + boxH - 16, DIM);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Join a world for 3D preview"), x + width / 2, boxY + boxH / 2, MUTED);
        }

        int buttonY = y + height - 46;
        String status = cosmeticActionLabel(cosmetic);
        boolean disabled = cosmetic == null || !BackendState.online();
        boolean owned = cosmetic != null && BackendState.owned(cosmetic.id());
        if (owned) {
            int half = (width - 40) / 2;
            rect(context, x + 16, buttonY, half, 28, 9, disabled ? 0x55151A25 : ClientTheme.withAlpha(accent, 210));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + 16 + half / 2, buttonY + 10, WHITE);
            rect(context, x + 24 + half, buttonY, half, 28, 9, disabled ? 0x55151A25 : 0xFF1B2534);
            outline(context, x + 24 + half, buttonY, half, 28, 9, disabled ? LINE_SOFT : ClientTheme.withAlpha(accent, 190));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Gift"), x + 24 + half + half / 2, buttonY + 10, disabled ? MUTED : WHITE);
        } else {
            rect(context, x + 16, buttonY, width - 32, 28, 9, disabled ? 0x55151A25 : ClientTheme.withAlpha(accent, 210));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + width / 2, buttonY + 10, WHITE);
        }
    }

    private void renderGiftDialog(DrawContext context, Layout layout, int mouseX, int mouseY, int accent) {
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0xA9000000);
        int w = Math.min(330, Math.max(260, layout.width / 2));
        int h = 154;
        int x = layout.x + (layout.width - w) / 2;
        int y = layout.y + (layout.height - h) / 2;
        rect(context, x, y, w, h, 12, 0xF20B0F17);
        outline(context, x, y, w, h, 12, ClientTheme.withAlpha(accent, 210));
        context.drawTextWithShadow(textRenderer, Text.literal("Gift Cosmetic"), x + 18, y + 16, WHITE);
        String name = selectedCosmetic == null ? "" : selectedCosmetic.displayName();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, name, w - 36)), x + 18, y + 31, MUTED);

        int inputY = y + 58;
        rect(context, x + 18, inputY, w - 36, 30, 8, 0xAA111824);
        outline(context, x + 18, inputY, w - 36, 30, 8, ClientTheme.withAlpha(accent, 180));
        String input = giftReceiver.isBlank() ? "Player name or UUID" : giftReceiver + (System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, input, w - 58)), x + 30, inputY + 10, giftReceiver.isBlank() ? DIM : WHITE);
        if (!giftStatus.isBlank()) {
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, giftStatus, w - 36)), x + 18, y + 94, WARN);
        }

        int buttonY = y + h - 38;
        int bw = (w - 46) / 2;
        boolean cancelHovered = inside(mouseX, mouseY, x + 18, buttonY, bw, 26);
        boolean sendHovered = inside(mouseX, mouseY, x + 28 + bw, buttonY, bw, 26);
        rect(context, x + 18, buttonY, bw, 26, 8, cancelHovered ? 0xFF262B36 : 0xFF171C26);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Cancel"), x + 18 + bw / 2, buttonY + 9, cancelHovered ? WHITE : MUTED);
        rect(context, x + 28 + bw, buttonY, bw, 26, 8, sendHovered ? ClientTheme.withAlpha(accent, 235) : ClientTheme.withAlpha(accent, 190));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Send Gift"), x + 28 + bw + bw / 2, buttonY + 9, WHITE);
    }


    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.y + 14;
        int tabX = layout.x + layout.width / 2 - 118;
        for (ClientTab tab : visibleTabs()) {
            int w = tab == ClientTab.COSMETICS ? 82 : 70;
            if (inside(mouseX, mouseY, tabX, y, w, 28)) {
                selectedTab = tab;
                searchFocused = false;
                search = "";
                scroll = 0;
                resetPreviewForTab();
                return true;
            }
            tabX += w + 18;
        }
        return false;
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
        int sideX = layout.x + 26;
        int sideY = layout.bodyY() + 18;
        int sideW = 132;
        int panelH = layout.y + layout.height - sideY - 24;
        int previewW = Math.max(190, Math.min(235, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 28;
        int gridX = sideX + sideW + 18;
        int searchY = sideY;
        int searchH = 32;
        int gridY = searchY + searchH + 16;
        int gridW = previewX - gridX - 18;
        int gridH = layout.y + layout.height - gridY - 24;

        if (inside(mouseX, mouseY, sideX + 12, sideY + panelH - 38, sideW - 24, 26)) {
            close();
            return true;
        }
        int sortX = gridX + gridW - 58;
        if (inside(mouseX, mouseY, sortX, searchY + 6, 23, 20)) {
            sortAscending = true;
            scroll = 0;
            return true;
        }
        if (inside(mouseX, mouseY, sortX + 28, searchY + 6, 23, 20)) {
            sortAscending = false;
            scroll = 0;
            return true;
        }
        if (inside(mouseX, mouseY, gridX, searchY, gridW - 66, 30)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        int listTop = sideY + 18;
        int listBottom = sideY + panelH - 54;
        if (inside(mouseX, mouseY, sideX, listTop, sideW, listBottom - listTop)) {
            int itemY = listTop - cosmeticSideScroll;
            for (CosmeticType type : CosmeticType.values()) {
                if (inside(mouseX, mouseY, sideX + 12, itemY, sideW - 24, 30)) {
                selectedCosmeticType = type;
                selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse(null);
                scroll = 0;
                // keep the category sidebar clamped inside its panel
                cosmeticSideScroll = clamp(cosmeticSideScroll, 0, maxCosmeticSideScroll(panelH));
                resetPreviewForCosmeticType(type);
                    return true;
                }
                itemY += 42;
            }
        }
        Grid grid = grid(gridW, gridH, 105, 112, 4);
        int baseY = gridY - scroll;
        List<Cosmetic> cosmetics = filteredCosmetics();
        for (int i = 0; i < cosmetics.size(); i++) {
            int cardX = gridX + (i % grid.columns) * (grid.cardW + grid.gap);
            int cardY = baseY + (i / grid.columns) * (grid.cardH + grid.gap);
            if (inside(mouseX, mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                Cosmetic cosmetic = cosmetics.get(i);
                selectedCosmetic = cosmetic;
                resetPreviewForCosmeticType(cosmetic.type());
                return true;
            }
        }
        Rect preview = previewBounds(layout);
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
            previewDragging = true;
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
        int sideY = layout.bodyY() + 22;
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            sideY = layout.bodyY() + 18;
            int previewW = Math.max(190, Math.min(235, layout.width / 4));
            int previewX = layout.x + layout.width - previewW - 28;
            return new Rect(previewX, sideY, previewW, layout.y + layout.height - sideY - 24);
        }
        return Rect.empty();
    }

    private Rect notificationBannerBounds(Layout layout) {
        int width = Math.min(270, layout.width - 40);
        if (width < 180 || layout.height < 230) {
            return Rect.empty();
        }
        return new Rect(layout.x + layout.width - width - 18, layout.y + layout.headerHeight + 5, width, 28);
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
            case HALO, HAT, GLINT, EMOTE -> 96;
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
        Comparator<Module> comparator = Comparator.comparing(module -> module.getName().toLowerCase(Locale.ROOT));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return S9LabClientClient.getModuleManager().getModules().stream()
                .filter(module -> module.getCategory() == selectedCategory)
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
                .filter(cosmetic -> cosmetic.type() == selectedCosmeticType)
                .filter(cosmetic -> query.isEmpty()
                        || cosmetic.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || cosmetic.id().toLowerCase(Locale.ROOT).contains(query))
                .sorted(comparator)
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
        giftReceiver = "";
        giftStatus = "";
        giftDialogOpen = true;
        searchFocused = false;
    }

    private void confirmGift() {
        if (selectedCosmetic == null) {
            giftDialogOpen = false;
            return;
        }
        String receiver = giftReceiver.trim();
        if (receiver.isBlank()) {
            giftStatus = "Enter a player name or UUID.";
            return;
        }
        BackendClient.giftCosmetic(receiver, selectedCosmetic.id());
        giftStatus = "Gift request sent.";
        giftDialogOpen = false;
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
        int total = CosmeticType.values().length * 42 + 28;
        int visible = Math.max(1, panelH - 72);
        return Math.max(0, total - visible);
    }

    private int maxScroll(Layout layout) {
        if (selectedTab == ClientTab.MODS) {
            int x = layout.x + 20;
            int y = layout.bodyY() + 48;
            int width = layout.width - 40;
            int gridH = layout.y + layout.height - y - 20;
            Grid grid = grid(width, gridH, 132, 84, 5);
            return Math.max(0, rows(filteredModules().size(), grid.columns) * (grid.cardH + grid.gap) - gridH);
        }
        if (selectedTab == ClientTab.SETTINGS) {
            return 0;
        }
        int sideX = layout.x + 26;
        int sideW = 132;
        int previewW = Math.max(190, Math.min(235, layout.width / 4));
        int previewX = layout.x + layout.width - previewW - 28;
        int gridX = sideX + sideW + 18;
        int gridW = previewX - gridX - 18;
        Grid grid = grid(gridW, layout.bodyHeight(), 105, 112, 4);
        int rows = rows(filteredCosmetics().size(), grid.columns);
        return Math.max(0, rows * (grid.cardH + grid.gap) - (layout.bodyHeight() - 72));
    }


    private void clampScroll() {
        scroll = clamp(scroll, 0, maxScroll(layout()));
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(780, 400, 360, 250);
        int header = screen.height() < 280 ? 46 : 58;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header);
    }


    private Grid grid(int width, int height, int minCardW, int preferredCardH, int maxColumns) {
        int columns = ResponsiveLayout.columns(width, minCardW, maxColumns);
        int gap = 7;
        int cardW = Math.max(86, (width - gap * (columns - 1)) / columns);
        int cardH = preferredCardH;
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
            case GLINT -> "Glint";
            case EMOTE -> "Emotes";
        };
    }

    private static ClientTab[] visibleTabs() {
        return new ClientTab[]{ClientTab.MODS, ClientTab.SETTINGS, ClientTab.COSMETICS};
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
