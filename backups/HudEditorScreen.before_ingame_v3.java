package site.s9lab.s9labclient.client.ui;

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
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class HudEditorScreen extends ResponsiveScreen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE8ECF6;
    private static final int MUTED = 0xFF9AA4B7;
    private static final int DIM = 0xFF667085;
    private static final int PANEL = 0xDD090D14;
    private static final int PANEL_DARK = 0xEE070A10;
    private static final int CARD = 0xDD111722;
    private static final int CARD_HOVER = 0xEE182234;
    private static final int LINE = 0x66333B4C;
    private static final int GREEN = 0xFF45F078;
    private static final int WARN = 0xFFFFB454;
    private static final int GRID = 10;

    private final net.minecraft.client.gui.screen.Screen parent;
    private HudModule selected;
    private HudModule dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean scaleDragging;
    private boolean snapGrid = true;
    private boolean showGrid = true;
    private boolean showLines = true;
    private String status = "Drag HUD widgets directly on your Minecraft screen";
    private long statusUntil = System.currentTimeMillis() + 2200L;

    public HudEditorScreen(net.minecraft.client.gui.screen.Screen parent) {
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
        saveAll();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int accent = ThemeManager.theme().accentColor();

        // Transparent world overlay: no fake canvas, no centered menu.
        context.fill(0, 0, width, height, 0x22000000);
        if (showGrid) drawDotGrid(context, accent);
        if (showLines) drawScreenAlignment(context, accent);

        renderTopHint(context, accent);

        for (HudModule module : hudModules()) {
            renderHudWidget(context, module, mouseX, mouseY, accent);
        }

        renderSettingsPanel(context, mouseX, mouseY, accent);
        renderStatus(context);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderTopHint(DrawContext context, int accent) {
        int x = 12;
        int y = 10;
        rect(context, x, y, 386, 34, 8, 0xB5070A10);
        outline(context, x, y, 386, 34, 8, 0x66404A5D);
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB HUD EDITOR"), x + 12, y + 8, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Drag widgets • wheel on widget = scale • ESC = save & close"), x + 12, y + 21, MUTED);

        renderTinyButton(context, x + 402, y, 72, 18, snapGrid ? "Snap ON" : "Snap OFF", snapGrid, accent);
        renderTinyButton(context, x + 402, y + 20, 72, 18, showGrid ? "Grid ON" : "Grid OFF", showGrid, accent);
        renderTinyButton(context, x + 480, y, 78, 18, "Save JSON", false, accent);
        renderTinyButton(context, x + 480, y + 20, 78, 18, "Load JSON", false, accent);
    }

    private void renderHudWidget(DrawContext context, HudModule module, int mouseX, int mouseY, int accent) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float scale = scaleOf(module);
        int widgetW = Math.max(46, Math.round(module.getWidth(mc) * scale));
        int widgetH = Math.max(20, Math.round(module.getHeight(mc) * scale));
        int x = clamp(module.getX(), 0, Math.max(0, width - widgetW));
        int y = clamp(module.getY(), 0, Math.max(0, height - widgetH));
        boolean active = module == selected;
        boolean hovered = inside(mouseX, mouseY, x, y, widgetW, widgetH);

        int fill = !module.isEnabled() ? 0x55202834 : active ? ClientTheme.withAlpha(accent, 120) : hovered ? CARD_HOVER : CARD;
        rect(context, x, y, widgetW, widgetH, 5, fill);
        outline(context, x, y, widgetW, widgetH, 5, active ? accent : hovered ? 0xAA56627A : 0x66364050);

        if (active) {
            drawResizeDots(context, x, y, widgetW, widgetH, accent);
        }

        String label = TextLayout.ellipsize(textRenderer, module.getName(), Math.max(20, widgetW - 12));
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 6, y + Math.max(6, (widgetH - 8) / 2), module.isEnabled() ? WHITE : DIM);
        if (!module.isEnabled()) {
            context.drawTextWithShadow(textRenderer, Text.literal("hidden"), x + Math.max(6, widgetW - 44), y + Math.max(6, widgetH - 12), DIM);
        }
    }

    private void renderSettingsPanel(DrawContext context, int mouseX, int mouseY, int accent) {
        int panelW = 238;
        int panelX = width - panelW - 14;
        int panelY = 58;
        int panelH = Math.min(292, height - panelY - 16);
        rect(context, panelX, panelY, panelW, panelH, 10, PANEL);
        outline(context, panelX, panelY, panelW, panelH, 10, 0x77404A5D);

        context.drawTextWithShadow(textRenderer, Text.literal("Widget Settings"), panelX + 14, panelY + 14, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Direct ingame edit"), panelX + 14, panelY + 27, MUTED);

        if (selected == null) {
            context.drawTextWithShadow(textRenderer, Text.literal("Click a widget."), panelX + 14, panelY + 58, MUTED);
            return;
        }

        int y = panelY + 52;
        rect(context, panelX + 12, y, panelW - 24, 40, 7, 0x77141B28);
        outline(context, panelX + 12, y, panelW - 24, 40, 7, ClientTheme.withAlpha(accent, 120));
        context.drawTextWithShadow(textRenderer, Text.literal(selected.getName()), panelX + 22, y + 9, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(selected.isEnabled() ? "Visible" : "Hidden"), panelX + 22, y + 23, selected.isEnabled() ? GREEN : DIM);

        y += 54;
        renderRowToggle(context, panelX + 12, y, panelW - 24, "Visible", selected.isEnabled(), mouseX, mouseY, accent);
        y += 36;
        renderScaleSlider(context, panelX + 12, y, panelW - 24, mouseX, mouseY, accent);
        y += 48;
        renderPanelButton(context, panelX + 12, y, panelW - 24, 28, "Reset Position", inside(mouseX, mouseY, panelX + 12, y, panelW - 24, 28), accent);
        y += 34;
        renderPanelButton(context, panelX + 12, y, panelW - 24, 28, "Center Widget", inside(mouseX, mouseY, panelX + 12, y, panelW - 24, 28), accent);
        y += 38;
        context.drawTextWithShadow(textRenderer, Text.literal("X " + selected.getX() + "  Y " + selected.getY()), panelX + 14, y, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("Scale " + Math.round(scaleOf(selected) * 100) + "%"), panelX + 118, y, MUTED);
    }

    private void renderRowToggle(DrawContext context, int x, int y, int w, String label, boolean value, int mouseX, int mouseY, int accent) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 28);
        rect(context, x, y, w, 28, 6, hovered ? 0x77202738 : 0x44141B28);
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 10, y + 10, TEXT);
        renderSwitch(context, x + w - 46, y + 5, value, accent);
    }

    private void renderScaleSlider(DrawContext context, int x, int y, int w, int mouseX, int mouseY, int accent) {
        float value = selected == null ? 1.0F : scaleOf(selected);
        context.drawTextWithShadow(textRenderer, Text.literal("Scale"), x + 10, y, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal(Math.round(value * 100) + "%"), x + w - 46, y, MUTED);
        int sx = x + 10;
        int sy = y + 23;
        int sw = w - 20;
        context.fill(sx, sy, sx + sw, sy + 3, 0xFF283044);
        int filled = sx + Math.round((value - 0.5F) / 1.5F * sw);
        context.fill(sx, sy, filled, sy + 3, accent);
        rect(context, filled - 5, sy - 5, 10, 13, 5, WHITE);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int accent = ThemeManager.theme().accentColor();

        if (handleTopButtons(mouseX, mouseY)) return true;
        if (handleSettingsClick(mouseX, mouseY)) return true;

        for (int i = hudModules().size() - 1; i >= 0; i--) {
            HudModule module = hudModules().get(i);
            MinecraftClient mc = MinecraftClient.getInstance();
            int widgetW = Math.max(46, Math.round(module.getWidth(mc) * scaleOf(module)));
            int widgetH = Math.max(20, Math.round(module.getHeight(mc) * scaleOf(module)));
            int x = clamp(module.getX(), 0, Math.max(0, width - widgetW));
            int y = clamp(module.getY(), 0, Math.max(0, height - widgetH));
            if (inside(mouseX, mouseY, x, y, widgetW, widgetH)) {
                selected = module;
                dragging = module;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean handleTopButtons(int mouseX, int mouseY) {
        int x = 12;
        int y = 10;
        if (inside(mouseX, mouseY, x + 402, y, 72, 18)) {
            snapGrid = !snapGrid;
            toast(snapGrid ? "Snap grid enabled" : "Snap grid disabled");
            return true;
        }
        if (inside(mouseX, mouseY, x + 402, y + 20, 72, 18)) {
            showGrid = !showGrid;
            return true;
        }
        if (inside(mouseX, mouseY, x + 480, y, 78, 18)) {
            exportJson();
            saveAll();
            return true;
        }
        if (inside(mouseX, mouseY, x + 480, y + 20, 78, 18)) {
            loadJson();
            return true;
        }
        return false;
    }

    private boolean handleSettingsClick(int mouseX, int mouseY) {
        if (selected == null) return false;
        int panelW = 238;
        int panelX = width - panelW - 14;
        int panelY = 58;
        int w = panelW - 24;
        int x = panelX + 12;
        int y = panelY + 52 + 54;

        if (inside(mouseX, mouseY, x, y, w, 28)) {
            selected.toggle();
            saveAll();
            return true;
        }
        y += 36;
        int sx = x + 10;
        int sy = y + 23;
        int sw = w - 20;
        if (inside(mouseX, mouseY, sx - 6, sy - 10, sw + 12, 24)) {
            scaleDragging = true;
            setScaleFromMouse(mouseX, sx, sw);
            saveAll();
            return true;
        }
        y += 48;
        if (inside(mouseX, mouseY, x, y, w, 28)) {
            selected.setPosition(8, 8);
            saveAll();
            toast("Position reset");
            return true;
        }
        y += 34;
        if (inside(mouseX, mouseY, x, y, w, 28)) {
            centerSelected();
            saveAll();
            toast("Widget centered");
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int widgetW = Math.max(46, Math.round(dragging.getWidth(mc) * scaleOf(dragging)));
            int widgetH = Math.max(20, Math.round(dragging.getHeight(mc) * scaleOf(dragging)));
            int newX = (int) click.x() - dragOffsetX;
            int newY = (int) click.y() - dragOffsetY;
            if (snapGrid) {
                newX = Math.round(newX / (float) GRID) * GRID;
                newY = Math.round(newY / (float) GRID) * GRID;
            }
            dragging.setPosition(clamp(newX, 0, Math.max(0, width - widgetW)), clamp(newY, 0, Math.max(0, height - widgetH)));
            return true;
        }
        if (scaleDragging && selected != null) {
            int panelW = 238;
            int panelX = width - panelW - 14;
            int x = panelX + 24;
            int sw = panelW - 44;
            setScaleFromMouse((int) click.x(), x, sw);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null || scaleDragging) {
            dragging = null;
            scaleDragging = false;
            saveAll();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selected != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int widgetW = Math.max(46, Math.round(selected.getWidth(mc) * scaleOf(selected)));
            int widgetH = Math.max(20, Math.round(selected.getHeight(mc) * scaleOf(selected)));
            if (inside(mouseX, mouseY, selected.getX(), selected.getY(), widgetW, widgetH)) {
                setScale(selected, scaleOf(selected) + (float) verticalAmount * 0.05F);
                saveAll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        int key = input.getKeycode();
        if (key == GLFW.GLFW_KEY_G) {
            showGrid = !showGrid;
            return true;
        }
        if (key == GLFW.GLFW_KEY_S) {
            snapGrid = !snapGrid;
            return true;
        }
        if (key == GLFW.GLFW_KEY_L) {
            showLines = !showLines;
            return true;
        }
        if (selected != null && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN)) {
            int step = 1;
            int dx = key == GLFW.GLFW_KEY_LEFT ? -step : key == GLFW.GLFW_KEY_RIGHT ? step : 0;
            int dy = key == GLFW.GLFW_KEY_UP ? -step : key == GLFW.GLFW_KEY_DOWN ? step : 0;
            selected.setPosition(clamp(selected.getX() + dx, 0, width), clamp(selected.getY() + dy, 0, height));
            saveAll();
            return true;
        }
        return super.keyPressed(input);
    }

    private void drawDotGrid(DrawContext context, int accent) {
        int color = ClientTheme.withAlpha(accent, 30);
        for (int x = GRID; x < width; x += GRID) {
            for (int y = GRID; y < height; y += GRID) {
                context.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private void drawScreenAlignment(DrawContext context, int accent) {
        int line = ClientTheme.withAlpha(accent, dragging == null ? 45 : 95);
        context.fill(width / 2, 0, width / 2 + 1, height, line);
        context.fill(0, height / 2, width, height / 2 + 1, line);
        if (dragging != null) {
            context.fill(dragging.getX(), 0, dragging.getX() + 1, height, ClientTheme.withAlpha(accent, 135));
            context.fill(0, dragging.getY(), width, dragging.getY() + 1, ClientTheme.withAlpha(accent, 135));
        }
    }

    private void drawResizeDots(DrawContext context, int x, int y, int w, int h, int accent) {
        int dot = ClientTheme.withAlpha(accent, 230);
        rect(context, x - 3, y - 3, 6, 6, 3, dot);
        rect(context, x + w - 3, y - 3, 6, 6, 3, dot);
        rect(context, x - 3, y + h - 3, 6, 6, 3, dot);
        rect(context, x + w - 3, y + h - 3, 6, 6, 3, dot);
    }

    private void renderTinyButton(DrawContext context, int x, int y, int w, int h, String label, boolean active, int accent) {
        rect(context, x, y, w, h, 4, active ? ClientTheme.withAlpha(accent, 145) : 0xAA101723);
        outline(context, x, y, w, h, 4, active ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 6, WHITE);
    }

    private void renderPanelButton(DrawContext context, int x, int y, int w, int h, String label, boolean hovered, int accent) {
        rect(context, x, y, w, h, 6, hovered ? ClientTheme.withAlpha(accent, 160) : 0xFF111722);
        outline(context, x, y, w, h, 6, hovered ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 10, hovered ? WHITE : MUTED);
    }

    private void renderSwitch(DrawContext context, int x, int y, boolean enabled, int accent) {
        rect(context, x, y, 36, 18, 9, enabled ? ClientTheme.withAlpha(accent, 220) : 0xFF333A4B);
        rect(context, enabled ? x + 20 : x + 3, y + 3, 12, 12, 6, WHITE);
    }

    private void renderStatus(DrawContext context) {
        if (System.currentTimeMillis() > statusUntil) return;
        int w = textRenderer.getWidth(status) + 26;
        int x = (width - w) / 2;
        int y = height - 34;
        rect(context, x, y, w, 22, 8, 0xCC090D14);
        outline(context, x, y, w, 22, 8, 0x66404A5D);
        context.drawTextWithShadow(textRenderer, Text.literal(status), x + 13, y + 8, WARN);
    }

    private void centerSelected() {
        if (selected == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int widgetW = Math.max(46, Math.round(selected.getWidth(mc) * scaleOf(selected)));
        int widgetH = Math.max(20, Math.round(selected.getHeight(mc) * scaleOf(selected)));
        selected.setPosition(Math.max(0, (width - widgetW) / 2), Math.max(0, (height - widgetH) / 2));
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

    private void saveAll() {
        exportJsonSilent();
        if (S9LabClientClient.getConfigManager() != null) {
            S9LabClientClient.getConfigManager().save();
        }
    }

    private void exportJson() {
        if (exportJsonSilent()) toast("HUD JSON saved"); else toast("JSON save failed");
    }

    private boolean exportJsonSilent() {
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
            return true;
        } catch (IOException e) {
            return false;
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
                if (obj.has("x") && obj.has("y")) module.setPosition(obj.get("x").getAsInt(), obj.get("y").getAsInt());
                if (obj.has("enabled")) module.setEnabled(obj.get("enabled").getAsBoolean());
                if (obj.has("scale")) setScale(module, obj.get("scale").getAsFloat());
            }
            if (S9LabClientClient.getConfigManager() != null) S9LabClientClient.getConfigManager().save();
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
