package site.s9lab.s9labclient.client.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class HudEditorScreen extends ResponsiveScreen {
    private static final int BG = 0xD806080D;
    private static final int PANEL = 0xEC11141B;
    private static final int PANEL_SOFT = 0xC70B0E14;
    private static final int CARD = 0xD9181D28;
    private static final int CARD_HOVER = 0xEA202637;
    private static final int LINE = 0x66343B4B;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE6EAF4;
    private static final int MUTED = 0xFF9AA3B6;
    private static final int DIM = 0xFF697386;
    private static final int GREEN = 0xFF45F078;
    private static final int WARN = 0xFFFFB454;
    private static final int GRID = 10;

    private final Screen parent;
    private HudModule selected;
    private HudModule dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private int panelScroll;
    private boolean scaleDragging;
    private boolean snapGrid = true;
    private boolean showLines = true;
    private String status = "Drag widgets to move them";
    private long statusUntil;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("S9Lab HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<HudModule> modules = hudModules();
        if (selected == null && !modules.isEmpty()) {
            selected = modules.get(0);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        saveConfig();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();
        context.fill(0, 0, width, height, 0x77000000);

        int margin = 18;
        int panelW = Math.min(980, width - margin * 2);
        int panelH = Math.min(560, height - margin * 2);
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        rect(context, panelX + 3, panelY + 3, panelW, panelH, 6, 0x66000000);
        rect(context, panelX, panelY, panelW, panelH, 6, PANEL);
        outline(context, panelX, panelY, panelW, panelH, 6, 0xFF2D3442);

        renderHeader(context, panelX, panelY, panelW, accent, mouseX, mouseY);

        int bodyY = panelY + 54;
        int bodyH = panelH - 70;
        int settingsW = 250;
        int previewX = panelX + 18;
        int previewY = bodyY + 14;
        int previewW = panelW - settingsW - 54;
        int previewH = bodyH - 14;
        int settingsX = previewX + previewW + 18;
        int settingsY = previewY;

        renderPreview(context, previewX, previewY, previewW, previewH, accent, mouseX, mouseY);
        renderSettingsPanel(context, settingsX, settingsY, settingsW, previewH, accent, mouseX, mouseY);
        renderStatus(context, panelX, panelY, panelW);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderHeader(DrawContext context, int x, int y, int w, int accent, int mouseX, int mouseY) {
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB HUD EDITOR"), x + 24, y + 17, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("drag • snap • scale • save"), x + 24, y + 31, MUTED);

        int buttonY = y + 16;
        renderHeaderButton(context, x + w - 390, buttonY, 86, 24, "Load JSON", inside(mouseX, mouseY, x + w - 390, buttonY, 86, 24), accent);
        renderHeaderButton(context, x + w - 296, buttonY, 86, 24, "Save JSON", inside(mouseX, mouseY, x + w - 296, buttonY, 86, 24), accent);
        renderHeaderButton(context, x + w - 202, buttonY, 82, 24, snapGrid ? "Snap ON" : "Snap OFF", inside(mouseX, mouseY, x + w - 202, buttonY, 82, 24), accent);
        renderHeaderButton(context, x + w - 112, buttonY, 86, 24, "Done", inside(mouseX, mouseY, x + w - 112, buttonY, 86, 24), accent);
        context.fill(x + 18, y + 52, x + w - 18, y + 53, LINE);
    }

    private void renderPreview(DrawContext context, int x, int y, int w, int h, int accent, int mouseX, int mouseY) {
        rect(context, x, y, w, h, 8, PANEL_SOFT);
        outline(context, x, y, w, h, 8, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("Preview Canvas"), x + 16, y + 12, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("Left drag = move  |  Wheel over panel = scroll settings"), x + 16, y + 25, DIM);

        int canvasX = x + 14;
        int canvasY = y + 46;
        int canvasW = w - 28;
        int canvasH = h - 60;
        rect(context, canvasX, canvasY, canvasW, canvasH, 7, 0xAA070A10);
        outline(context, canvasX, canvasY, canvasW, canvasH, 7, 0x66414A5D);

        drawGrid(context, canvasX, canvasY, canvasW, canvasH);
        drawAlignmentGuides(context, canvasX, canvasY, canvasW, canvasH, accent);

        context.enableScissor(canvasX, canvasY, canvasX + canvasW, canvasY + canvasH);
        for (HudModule module : hudModules()) {
            renderWidget(context, module, canvasX, canvasY, canvasW, canvasH, accent, mouseX, mouseY);
        }
        context.disableScissor();
    }

    private void renderWidget(DrawContext context, HudModule module, int canvasX, int canvasY, int canvasW, int canvasH, int accent, int mouseX, int mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float scale = scaleOf(module);
        int widgetW = Math.max(42, Math.round(module.getWidth(mc) * scale));
        int widgetH = Math.max(20, Math.round(module.getHeight(mc) * scale));
        int x = canvasX + clamp(module.getX(), 0, Math.max(0, canvasW - widgetW));
        int y = canvasY + clamp(module.getY(), 0, Math.max(0, canvasH - widgetH));
        boolean active = module == selected;
        boolean hovered = inside(mouseX, mouseY, x, y, widgetW, widgetH);
        int fill = !module.isEnabled() ? 0x55222830 : active ? ClientTheme.withAlpha(accent, 125) : hovered ? CARD_HOVER : CARD;
        rect(context, x, y, widgetW, widgetH, 5, fill);
        outline(context, x, y, widgetW, widgetH, 5, active ? accent : hovered ? 0xAA556178 : 0x553A4355);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, module.getName(), widgetW - 12)), x + 6, y + 7, module.isEnabled() ? WHITE : DIM);
        if (!module.isEnabled()) {
            context.drawTextWithShadow(textRenderer, Text.literal("hidden"), x + 6, y + widgetH - 11, DIM);
        }
    }

    private void renderSettingsPanel(DrawContext context, int x, int y, int w, int h, int accent, int mouseX, int mouseY) {
        rect(context, x, y, w, h, 8, PANEL_SOFT);
        outline(context, x, y, w, h, 8, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal("Widget Settings"), x + 16, y + 14, WHITE);
        if (selected == null) {
            context.drawTextWithShadow(textRenderer, Text.literal("Select a HUD widget."), x + 16, y + 44, MUTED);
            return;
        }
        int rowY = y + 44 - panelScroll;
        renderSelectedHeader(context, x + 14, rowY, w - 28, accent);
        rowY += 58;
        renderToggleRow(context, x + 14, rowY, w - 28, "Visible", selected.isEnabled(), mouseX, mouseY, accent);
        rowY += 38;
        renderSliderRow(context, x + 14, rowY, w - 28, "Scale", scaleOf(selected), mouseX, mouseY, accent);
        rowY += 46;
        renderButtonRow(context, x + 14, rowY, w - 28, mouseX, mouseY, accent);
        rowY += 48;
        renderToggleRow(context, x + 14, rowY, w - 28, "Snap Grid", snapGrid, mouseX, mouseY, accent);
        rowY += 34;
        renderToggleRow(context, x + 14, rowY, w - 28, "Alignment Lines", showLines, mouseX, mouseY, accent);
        rowY += 42;
        context.drawTextWithShadow(textRenderer, Text.literal("Position"), x + 16, rowY, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("X " + selected.getX() + "   Y " + selected.getY()), x + 16, rowY + 15, TEXT);
        rowY += 42;
        context.drawTextWithShadow(textRenderer, Text.literal("JSON"), x + 16, rowY, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("config/s9lab_hud_editor.json"), x + 16, rowY + 15, DIM);
    }

    private void renderSelectedHeader(DrawContext context, int x, int y, int w, int accent) {
        rect(context, x, y, w, 44, 7, 0x9A171C28);
        outline(context, x, y, w, 44, 7, ClientTheme.withAlpha(accent, 120));
        context.drawTextWithShadow(textRenderer, Text.literal(selected.getName()), x + 12, y + 10, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(selected.isEnabled() ? "Enabled" : "Disabled"), x + 12, y + 24, selected.isEnabled() ? GREEN : DIM);
    }

    private void renderToggleRow(DrawContext context, int x, int y, int w, String label, boolean value, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 28);
        rect(context, x, y, w, 28, 6, hovered ? 0x551A2230 : 0x33141A25);
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 10, y + 10, TEXT);
        renderSwitch(context, x + w - 46, y + 5, value, accent);
    }

    private void renderSliderRow(DrawContext context, int x, int y, int w, String label, float value, int mouseX, int mouseY, int accent) {
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 10, y, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal(Math.round(value * 100) + "%"), x + w - 44, y, MUTED);
        int sliderX = x + 10;
        int sliderY = y + 22;
        int sliderW = w - 20;
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 3, 0xFF293040);
        int knob = sliderX + Math.round((value - 0.5F) / 1.5F * sliderW);
        context.fill(sliderX, sliderY, knob, sliderY + 3, accent);
        rect(context, knob - 4, sliderY - 4, 8, 11, 4, WHITE);
    }

    private void renderButtonRow(DrawContext context, int x, int y, int w, int mouseX, int mouseY, int accent) {
        int bw = (w - 8) / 2;
        renderPanelButton(context, x, y, bw, 30, "Reset Position", inside(mouseX, mouseY, x, y, bw, 30), accent);
        renderPanelButton(context, x + bw + 8, y, bw, 30, "Center", inside(mouseX, mouseY, x + bw + 8, y, bw, 30), accent);
    }

    private void renderStatus(DrawContext context, int x, int y, int w) {
        if (System.currentTimeMillis() > statusUntil) {
            return;
        }
        context.drawTextWithShadow(textRenderer, Text.literal(status), x + w / 2 - textRenderer.getWidth(status) / 2, y + 58, WARN);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int margin = 18;
        int panelW = Math.min(980, width - margin * 2);
        int panelH = Math.min(560, height - margin * 2);
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        if (handleHeaderClick(panelX, panelY, panelW, mouseX, mouseY)) return true;

        int bodyY = panelY + 54;
        int bodyH = panelH - 70;
        int settingsW = 250;
        int previewX = panelX + 18;
        int previewY = bodyY + 14;
        int previewW = panelW - settingsW - 54;
        int previewH = bodyH - 14;
        int canvasX = previewX + 14;
        int canvasY = previewY + 46;
        int canvasW = previewW - 28;
        int canvasH = previewH - 60;

        if (inside(mouseX, mouseY, canvasX, canvasY, canvasW, canvasH)) {
            for (int i = hudModules().size() - 1; i >= 0; i--) {
                HudModule module = hudModules().get(i);
                MinecraftClient mc = MinecraftClient.getInstance();
                float scale = scaleOf(module);
                int widgetW = Math.max(42, Math.round(module.getWidth(mc) * scale));
                int widgetH = Math.max(20, Math.round(module.getHeight(mc) * scale));
                int x = canvasX + clamp(module.getX(), 0, Math.max(0, canvasW - widgetW));
                int y = canvasY + clamp(module.getY(), 0, Math.max(0, canvasH - widgetH));
                if (inside(mouseX, mouseY, x, y, widgetW, widgetH)) {
                    selected = module;
                    dragging = module;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return true;
                }
            }
            return true;
        }
        return handleSettingsClick(panelX, panelY, panelW, panelH, mouseX, mouseY) || super.mouseClicked(click, doubled);
    }

    private boolean handleHeaderClick(int x, int y, int w, int mouseX, int mouseY) {
        int buttonY = y + 16;
        if (inside(mouseX, mouseY, x + w - 390, buttonY, 86, 24)) {
            loadJson();
            return true;
        }
        if (inside(mouseX, mouseY, x + w - 296, buttonY, 86, 24)) {
            exportJson();
            saveConfig();
            return true;
        }
        if (inside(mouseX, mouseY, x + w - 202, buttonY, 82, 24)) {
            snapGrid = !snapGrid;
            toast(snapGrid ? "Snap grid enabled" : "Snap grid disabled");
            return true;
        }
        if (inside(mouseX, mouseY, x + w - 112, buttonY, 86, 24)) {
            close();
            return true;
        }
        return false;
    }

    private boolean handleSettingsClick(int panelX, int panelY, int panelW, int panelH, int mouseX, int mouseY) {
        if (selected == null) return false;
        int bodyY = panelY + 54;
        int bodyH = panelH - 70;
        int settingsW = 250;
        int sx = panelX + 18 + (panelW - settingsW - 54) + 18;
        int sy = bodyY + 14;
        int rowY = sy + 44 - panelScroll;
        rowY += 58;
        if (inside(mouseX, mouseY, sx + 14, rowY, settingsW - 28, 28)) {
            selected.toggle();
            saveConfig();
            return true;
        }
        rowY += 38;
        int sliderX = sx + 24;
        int sliderY = rowY + 22;
        int sliderW = settingsW - 48;
        if (inside(mouseX, mouseY, sliderX - 4, sliderY - 8, sliderW + 8, 20)) {
            scaleDragging = true;
            setScaleFromMouse(mouseX, sliderX, sliderW);
            saveConfig();
            return true;
        }
        rowY += 46;
        int bw = (settingsW - 28 - 8) / 2;
        if (inside(mouseX, mouseY, sx + 14, rowY, bw, 30)) {
            selected.setPosition(8, 8);
            saveConfig();
            toast("Position reset");
            return true;
        }
        if (inside(mouseX, mouseY, sx + 14 + bw + 8, rowY, bw, 30)) {
            centerSelected(panelW - settingsW - 82, panelH - 130);
            saveConfig();
            toast("Centered");
            return true;
        }
        rowY += 48;
        if (inside(mouseX, mouseY, sx + 14, rowY, settingsW - 28, 28)) {
            snapGrid = !snapGrid;
            return true;
        }
        rowY += 34;
        if (inside(mouseX, mouseY, sx + 14, rowY, settingsW - 28, 28)) {
            showLines = !showLines;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            int margin = 18;
            int panelW = Math.min(980, width - margin * 2);
            int panelH = Math.min(560, height - margin * 2);
            int panelX = (width - panelW) / 2;
            int panelY = (height - panelH) / 2;
            int settingsW = 250;
            int canvasX = panelX + 32;
            int canvasY = panelY + 114;
            int canvasW = panelW - settingsW - 82;
            int canvasH = panelH - 130;
            MinecraftClient mc = MinecraftClient.getInstance();
            int widgetW = Math.max(42, Math.round(dragging.getWidth(mc) * scaleOf(dragging)));
            int widgetH = Math.max(20, Math.round(dragging.getHeight(mc) * scaleOf(dragging)));
            int newX = (int) click.x() - canvasX - dragOffsetX;
            int newY = (int) click.y() - canvasY - dragOffsetY;
            if (snapGrid) {
                newX = Math.round(newX / (float) GRID) * GRID;
                newY = Math.round(newY / (float) GRID) * GRID;
            }
            dragging.setPosition(clamp(newX, 0, Math.max(0, canvasW - widgetW)), clamp(newY, 0, Math.max(0, canvasH - widgetH)));
            return true;
        }
        if (scaleDragging && selected != null) {
            int margin = 18;
            int panelW = Math.min(980, width - margin * 2);
            int panelX = (width - panelW) / 2;
            int settingsW = 250;
            int sx = panelX + 18 + (panelW - settingsW - 54) + 18;
            int sliderX = sx + 24;
            int sliderW = settingsW - 48;
            setScaleFromMouse((int) click.x(), sliderX, sliderW);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null || scaleDragging) {
            dragging = null;
            scaleDragging = false;
            saveConfig();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int margin = 18;
        int panelW = Math.min(980, width - margin * 2);
        int panelH = Math.min(560, height - margin * 2);
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        int settingsW = 250;
        int settingsX = panelX + 18 + (panelW - settingsW - 54) + 18;
        int settingsY = panelY + 68;
        if (inside(mouseX, mouseY, settingsX, settingsY, settingsW, panelH - 84)) {
            panelScroll = clamp(panelScroll - (int) Math.round(verticalAmount * 22), 0, 110);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        if (selected != null) {
            int key = input.getKeycode();
            int step = 1;
            if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
                int dx = key == GLFW.GLFW_KEY_LEFT ? -step : key == GLFW.GLFW_KEY_RIGHT ? step : 0;
                int dy = key == GLFW.GLFW_KEY_UP ? -step : key == GLFW.GLFW_KEY_DOWN ? step : 0;
                selected.setPosition(Math.max(0, selected.getX() + dx), Math.max(0, selected.getY() + dy));
                saveConfig();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    private void drawGrid(DrawContext context, int x, int y, int w, int h) {
        for (int gx = x + GRID; gx < x + w; gx += GRID) {
            context.fill(gx, y, gx + 1, y + h, 0x0FFFFFFF);
        }
        for (int gy = y + GRID; gy < y + h; gy += GRID) {
            context.fill(x, gy, x + w, gy + 1, 0x0FFFFFFF);
        }
    }

    private void drawAlignmentGuides(DrawContext context, int x, int y, int w, int h, int accent) {
        if (!showLines) return;
        context.fill(x + w / 2, y, x + w / 2 + 1, y + h, ClientTheme.withAlpha(accent, 75));
        context.fill(x, y + h / 2, x + w, y + h / 2 + 1, ClientTheme.withAlpha(accent, 75));
        if (dragging != null) {
            int px = x + dragging.getX();
            int py = y + dragging.getY();
            context.fill(px, y, px + 1, y + h, ClientTheme.withAlpha(accent, 125));
            context.fill(x, py, x + w, py + 1, ClientTheme.withAlpha(accent, 125));
        }
    }

    private void centerSelected(int canvasW, int canvasH) {
        if (selected == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int widgetW = Math.max(42, Math.round(selected.getWidth(mc) * scaleOf(selected)));
        int widgetH = Math.max(20, Math.round(selected.getHeight(mc) * scaleOf(selected)));
        selected.setPosition(Math.max(0, (canvasW - widgetW) / 2), Math.max(0, (canvasH - widgetH) / 2));
    }

    private void setScaleFromMouse(int mouseX, int sliderX, int sliderW) {
        float t = clamp((mouseX - sliderX) / (float) Math.max(1, sliderW), 0.0F, 1.0F);
        setScale(selected, 0.5F + t * 1.5F);
    }

    private float scaleOf(HudModule module) {
        NumberSetting scale = scaleSetting(module);
        if (scale != null) return clamp(scale.getValue().floatValue(), 0.5F, 2.0F);
        return 1.0F;
    }

    private void setScale(HudModule module, float scale) {
        NumberSetting setting = scaleSetting(module);
        if (setting != null) {
            setting.setValue((double) clamp(scale, 0.5F, 2.0F));
        }
    }

    private NumberSetting scaleSetting(HudModule module) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting numberSetting && setting.getName().toLowerCase(Locale.ROOT).contains("scale")) {
                return numberSetting;
            }
        }
        return null;
    }

    private List<HudModule> hudModules() {
        return S9LabClientClient.getModuleManager().getHudModules();
    }

    private void saveConfig() {
        if (S9LabClientClient.getConfigManager() != null) {
            S9LabClientClient.getConfigManager().save();
        }
    }

    private void exportJson() {
        JsonObject root = new JsonObject();
        JsonObject widgets = new JsonObject();
        for (HudModule module : hudModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", module.getX());
            obj.addProperty("y", module.getY());
            obj.addProperty("enabled", module.isEnabled());
            obj.addProperty("scale", scaleOf(module));
            widgets.add(module.getName(), obj);
        }
        root.add("widgets", widgets);
        try {
            File file = jsonFile();
            File parentFile = file.getParentFile();
            if (parentFile != null) parentFile.mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
            toast("HUD JSON saved");
        } catch (IOException e) {
            toast("JSON save failed");
        }
    }

    private void loadJson() {
        File file = jsonFile();
        if (!file.exists()) {
            toast("No HUD JSON found");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject widgets = root.getAsJsonObject("widgets");
            if (widgets == null) return;
            Map<String, HudModule> byName = new HashMap<>();
            for (HudModule module : hudModules()) byName.put(module.getName(), module);
            for (Map.Entry<String, JsonElement> entry : widgets.entrySet()) {
                HudModule module = byName.get(entry.getKey());
                if (module == null || !entry.getValue().isJsonObject()) continue;
                JsonObject obj = entry.getValue().getAsJsonObject();
                int x = obj.has("x") ? obj.get("x").getAsInt() : module.getX();
                int y = obj.has("y") ? obj.get("y").getAsInt() : module.getY();
                module.setPosition(x, y);
                if (obj.has("enabled")) module.setEnabled(obj.get("enabled").getAsBoolean());
                if (obj.has("scale")) setScale(module, obj.get("scale").getAsFloat());
            }
            saveConfig();
            toast("HUD JSON loaded");
        } catch (Exception e) {
            toast("JSON load failed");
        }
    }

    private File jsonFile() {
        return new File("config/s9lab_hud_editor.json");
    }

    private void toast(String text) {
        status = text;
        statusUntil = System.currentTimeMillis() + 1800L;
    }

    private void renderHeaderButton(DrawContext context, int x, int y, int w, int h, String label, boolean hovered, int accent) {
        rect(context, x, y, w, h, 5, hovered ? ClientTheme.withAlpha(accent, 155) : 0xFF1A202C);
        outline(context, x, y, w, h, 5, hovered ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 8, hovered ? WHITE : MUTED);
    }

    private void renderPanelButton(DrawContext context, int x, int y, int w, int h, String label, boolean hovered, int accent) {
        rect(context, x, y, w, h, 6, hovered ? ClientTheme.withAlpha(accent, 180) : 0xFF1A202C);
        outline(context, x, y, w, h, 6, hovered ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, w - 8)), x + w / 2, y + 11, hovered ? WHITE : MUTED);
    }

    private void renderSwitch(DrawContext context, int x, int y, boolean enabled, int accent) {
        rect(context, x, y, 36, 18, 9, enabled ? ClientTheme.withAlpha(accent, 220) : 0xFF333A4B);
        int knobX = enabled ? x + 20 : x + 3;
        rect(context, knobX, y + 3, 12, 12, 6, WHITE);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static void rect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        for (int row = 0; row < r; row++) {
            int inset = cornerInset(r, row);
            context.fill(x + inset, y + row, x + r, y + row + 1, color);
            context.fill(x + width - r, y + row, x + width - inset, y + row + 1, color);
            context.fill(x + inset, y + height - row - 1, x + r, y + height - row, color);
            context.fill(x + width - r, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    private static void outline(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        context.fill(x + r, y, x + width - r, y + 1, color);
        context.fill(x + r, y + height - 1, x + width - r, y + height, color);
        context.fill(x, y + r, x + 1, y + height - r, color);
        context.fill(x + width - 1, y + r, x + width, y + height - r, color);
    }

    private static int cornerInset(int radius, int row) {
        double center = radius - 0.5D;
        double dy = center - row;
        double dx = Math.sqrt(Math.max(0.0D, center * center - dy * dy));
        return Math.max(0, radius - (int) Math.ceil(dx));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
