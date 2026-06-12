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
        switch (selectedTab) {
            case MODS -> renderMods(context, layout, mouseX, mouseY, accent);
            case COSMETICS -> renderCosmetics(context, layout, mouseX, mouseY, accent);
            case SHOP -> renderCatalog(context, layout, mouseX, mouseY, accent);
            case SETTINGS -> renderSettingsPage(context, layout, mouseX, mouseY, accent);
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
        int y = layout.y + 14;
        int logoX = layout.x + 22;
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB"), logoX, y + 2, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("CLIENT"), logoX, y + 14, ClientTheme.withAlpha(accent, 210));

        int tabX = layout.x + layout.width / 2 - 150;
        for (ClientTab tab : new ClientTab[]{ClientTab.MODS, ClientTab.SETTINGS, ClientTab.COSMETICS, ClientTab.SHOP}) {
            int w = tab == ClientTab.COSMETICS ? 82 : 70;
            boolean active = tab == selectedTab;
            boolean hovered = inside(mouseX, mouseY, tabX, y, w, 24);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.label), tabX + w / 2, y + 7, active ? accent : hovered ? WHITE : MUTED);
            if (active) {
                context.fill(tabX + 10, y + 26, tabX + w - 10, y + 28, accent);
            }
            tabX += w + 18;
        }

        int dropW = 78;
        int dropX = layout.x + layout.width - dropW - 28;
        outline(context, dropX, y + 1, dropW, 18, 1, 0xFF3A3D45);
        context.drawTextWithShadow(textRenderer, Text.literal("Default"), dropX + 7, y + 6, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("▾"), dropX + dropW - 13, y + 6, MUTED);

        int coinsX = dropX - 92;
        context.drawTextWithShadow(textRenderer, Text.literal("Coins"), coinsX, y + 6, MUTED);
        rect(context, coinsX + 42, y + 3, 14, 14, 7, ClientTheme.withAlpha(accent, 230));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S"), coinsX + 49, y + 7, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("0"), coinsX + 63, y + 6, WHITE);

        context.fill(layout.x + 14, layout.y + layout.headerHeight - 1, layout.x + layout.width - 14, layout.y + layout.headerHeight, 0xFF25282E);
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
        rect(context, azX, y, 23, 20, 3, ClientTheme.withAlpha(accent, 210));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("A↓"), azX + 11, y + 6, WHITE);
        rect(context, azX + 28, y, 23, 20, 3, 0xFF22262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Z↓"), azX + 39, y + 6, MUTED);

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
        renderSearch(context, x + 12, y + 16, width - 24, mouseX, mouseY, accent, modules ? "Search..." : "Search...");

        int itemY = y + 66;
        if (modules) {
            for (ModuleCategory category : ModuleCategory.values()) {
                boolean active = category == selectedCategory;
                renderSideItem(context, titleCase(category.name()), moduleCount(category), x + 12, itemY, width - 24, 27, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 27), accent);
                itemY += 36;
            }
        } else {
            for (CosmeticType type : CosmeticType.values()) {
                boolean active = type == selectedCosmeticType;
                renderSideItem(context, cosmeticMenuLabel(type), -1, x + 12, itemY, width - 24, 28, active, inside(mouseX, mouseY, x + 12, itemY, width - 24, 28), accent);
                itemY += 42;
            }
            renderSideItem(context, "Glint", -1, x + 12, itemY, width - 24, 28, false, inside(mouseX, mouseY, x + 12, itemY, width - 24, 28), accent);
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
        renderModuleIcon(context, module, x + width / 2, y + 45, accent);
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
        if (module == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select a module first."), x + width / 2, y + 82, MUTED);
            return;
        }
        int rowY = y + 48;
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
        int sideY = layout.bodyY() + 22;
        int sideW = 150;
        int previewW = Math.max(230, Math.min(300, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 30;
        int gridX = sideX + sideW + 22;
        int searchY = sideY;
        int gridY = searchY + 48;
        int gridW = previewX - gridX - 22;
        int gridH = layout.y + layout.height - gridY - 28;
        int panelH = layout.y + layout.height - sideY - 28;

        renderLeftShell(context, sideX, sideY, sideW, panelH, mouseX, mouseY, accent, false);
        renderSearch(context, gridX, searchY, gridW, mouseX, mouseY, accent, "Search cosmetics...");

        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        Grid grid = grid(gridW, gridH, 120, 130, 4);
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
        int x = layout.x + layout.width - 124;
        int y = layout.bodyY() + 24;
        rect(context, x, y, 82, 20, 10, ClientTheme.withAlpha(accent, 120));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Catalog"), x + 41, y + 6, WHITE);
    }

    private void renderCosmeticCard(DrawContext context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean selected = cosmetic == selectedCosmetic;
        rect(context, x, y, width, height, 13, selected ? ClientTheme.withAlpha(accent, 48) : hovered ? CARD_HOVER : CARD);
        outline(context, x, y, width, height, 13, selected || equipped ? ClientTheme.withAlpha(accent, 240) : hovered ? 0xAA45516B : LINE_SOFT);
        if (equipped) {
            rect(context, x + width - 22, y + 8, 14, 14, 7, accent);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("✓"), x + width - 15, y + 12, WHITE);
        }
        int previewH = height - 52;
        drawCosmeticTexture(context, cosmetic, x + 18, y + 16, width - 36, previewH - 10, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 14)), x + width / 2, y + height - 33, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(cosmeticRarity(cosmetic)), x + width / 2, y + height - 18, rarityColor(cosmetic));
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
        rect(context, x, y, width, height, 15, 0x82070A10);
        outline(context, x, y, width, height, 15, LINE_SOFT);
        context.drawTextWithShadow(textRenderer, Text.literal(cosmetic == null ? "Preview" : TextLayout.ellipsize(textRenderer, cosmetic.displayName(), width - 34)), x + 18, y + 18, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Drag • Scroll • Double click reset"), x + 18, y + 32, DIM);

        int modelTop = y + 56;
        int modelBottom = y + height - 82;
        rect(context, x + 14, modelTop, width - 28, modelBottom - modelTop, 16, 0xAA0D111B);
        rect(context, x + width / 2 - 74, modelBottom - 16, 148, 22, 12, ClientTheme.withAlpha(accent, 36));
        outline(context, x + width / 2 - 74, modelBottom - 16, 148, 22, 12, ClientTheme.withAlpha(accent, 95));

        if (client != null && client.player != null) {
            int centerX = x + width / 2;
            int entityBottom = modelBottom - 4;
            int size = Math.min(previewZoom, Math.max(58, height / 3));
            context.enableScissor(x + 14, modelTop, x + width - 14, modelBottom + 20);
            if (cosmetic != null) CosmeticPreviewContext.begin(cosmetic);
            InventoryScreen.drawEntity(context, centerX - size, entityBottom - size * 2, centerX + size, entityBottom, size, 0.05F, previewYaw, previewPitch, client.player);
            if (cosmetic != null) CosmeticPreviewContext.end();
            context.disableScissor();

            if (cosmetic != null && cosmetic.type() == CosmeticType.CAPE) {
                drawCosmeticTexture(context, cosmetic, centerX - 34, modelTop + 28, 68, 92, accent);
            }
        }

        int buttonY = y + height - 58;
        String status = cosmetic == null ? "Select cosmetic" : cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type())) ? "Equipped" : "Equip";
        rect(context, x + 18, buttonY, width - 36, 30, 12, cosmetic == null ? 0x55151A25 : ClientTheme.withAlpha(accent, 210));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), x + width / 2, buttonY + 11, WHITE);
    }


    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.y + 14;
        int tabX = layout.x + layout.width / 2 - 150;
        for (ClientTab tab : new ClientTab[]{ClientTab.MODS, ClientTab.SETTINGS, ClientTab.COSMETICS, ClientTab.SHOP}) {
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
        int sideY = layout.bodyY() + 22;
        int sideW = 150;
        int panelH = layout.y + layout.height - sideY - 28;
        int previewW = Math.max(230, Math.min(300, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 30;
        int gridX = sideX + sideW + 22;
        int searchY = sideY;
        int gridY = searchY + 48;
        int gridW = previewX - gridX - 22;
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
        int sideY = layout.bodyY() + 22;
        if (selectedTab == ClientTab.COSMETICS || selectedTab == ClientTab.SHOP) {
            int previewW = Math.max(230, Math.min(300, layout.width / 3));
            int previewX = layout.x + layout.width - previewW - 30;
            return new Rect(previewX, sideY, previewW, layout.y + layout.height - sideY - 28);
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
        int sideW = 150;
        int previewW = Math.max(230, Math.min(300, layout.width / 3));
        int previewX = layout.x + layout.width - previewW - 30;
        int gridX = sideX + sideW + 22;
        int gridW = previewX - gridX - 22;
        Grid grid = grid(gridW, layout.bodyHeight(), 120, 130, 4);
        int rows = rows(filteredCosmetics().size(), grid.columns);
        return Math.max(0, rows * (grid.cardH + grid.gap) - (layout.bodyHeight() - 56));
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
