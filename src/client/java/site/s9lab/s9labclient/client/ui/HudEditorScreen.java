package site.s9lab.s9labclient.client.ui;

import java.util.List;
import java.util.Locale;
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
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class HudEditorScreen extends ResponsiveScreen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF9AA4B7;
    private static final int DIM = 0xFF697386;
    private static final int CARD = 0xC80B1018;
    private static final int CARD_HOVER = 0xE2121A27;
    private static final int LINE = 0x66394358;
    private static final int GRID = 10;
    private static final int SNAP_DISTANCE = 5;
    private static final int X_SIZE = 13;
    private static final int HANDLE = 10;

    private final net.minecraft.client.gui.screen.Screen parent;
    private HudModule selected;
    private HudModule dragging;
    private HudModule resizing;
    private int dragOffsetX;
    private int dragOffsetY;
    private int resizeStartMouseX;
    private int resizeStartMouseY;
    private float resizeStartScale;
    private boolean showGrid = true;
    private boolean snap = true;
    private boolean showHidden = false;
    private int guideX = Integer.MIN_VALUE;
    private int guideY = Integer.MIN_VALUE;
    private String status = "Drag HUD elements directly on the Minecraft screen";
    private long statusUntil = System.currentTimeMillis() + 2200L;

    public HudEditorScreen(net.minecraft.client.gui.screen.Screen parent) {
        super(Text.literal("S9Lab HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (selected == null) {
            for (HudModule module : hudModules()) {
                if (module.isEnabled()) {
                    selected = module;
                    break;
                }
            }
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
        int accent = ThemeManager.theme().accentColor();
        PremiumRender.shopBackdrop(context);

        if (showGrid) drawDotGrid(context, accent);
        renderTopBar(context, accent);

        guideX = Integer.MIN_VALUE;
        guideY = Integer.MIN_VALUE;
        if (dragging != null) computeGuides(dragging);
        renderGuides(context, accent);

        for (HudModule module : hudModules()) {
            if (!module.isEnabled() && !showHidden) continue;
            renderWidget(context, module, mouseX, mouseY, accent);
        }

        renderStatus(context);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderTopBar(DrawContext context, int accent) {
        int x = 12;
        int y = 10;
        int w = 445;
        rect(context, x, y, w, 31, 0, PremiumRender.SHOP_HEADER);
        outline(context, x, y, w, 31, 0, PremiumRender.SHOP_BORDER);
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB HUD EDITOR"), x + 12, y + 7, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Drag • wheel/handle scale • X hide • R reset • ESC save"), x + 132, y + 7, MUTED);
        tinyToggle(context, x + w + 8, y, 70, 31, snap ? "Snap" : "Free", snap, accent);
        tinyToggle(context, x + w + 84, y, 70, 31, showGrid ? "Grid" : "Clean", showGrid, accent);
        tinyToggle(context, x + w + 160, y, 86, 31, showHidden ? "Hidden" : "Visible", showHidden, accent);
    }

    private void renderWidget(DrawContext context, HudModule module, int mouseX, int mouseY, int accent) {
        Bounds b = bounds(module);
        boolean active = module == selected;
        boolean hovered = inside(mouseX, mouseY, b.x, b.y, b.w, b.h);
        boolean hidden = !module.isEnabled();
        int fill = hidden ? 0x66202634 : active ? ClientTheme.withAlpha(accent, 105) : hovered ? CARD_HOVER : CARD;
        int border = active ? accent : hovered ? 0xAA59657E : 0x77394358;

        shadow(context, b.x, b.y, b.w, b.h, 0);
        rect(context, b.x, b.y, b.w, b.h, 0, fill);
        outline(context, b.x, b.y, b.w, b.h, 0, border);

        if (active || hovered) {
            renderCloseX(context, b, accent, hoveredClose(mouseX, mouseY, b));
            renderScaleHandle(context, b, accent, hoveredHandle(mouseX, mouseY, b));
        }

        String name = TextLayout.ellipsize(textRenderer, module.getName(), Math.max(18, b.w - 26));
        int textY = b.y + Math.max(6, (b.h - 8) / 2);
        context.drawTextWithShadow(textRenderer, Text.literal(name), b.x + 8, textY, hidden ? DIM : WHITE);
        if (hidden) context.drawTextWithShadow(textRenderer, Text.literal("hidden"), b.x + 8, textY + 11, DIM);
    }

    private void renderCloseX(DrawContext context, Bounds b, int accent, boolean hovered) {
        int x = b.x + b.w - X_SIZE + 4;
        int y = b.y - 7;
        rect(context, x, y, X_SIZE, X_SIZE, 0, hovered ? 0xFFE65252 : PremiumRender.SHOP_BUTTON);
        outline(context, x, y, X_SIZE, X_SIZE, 0, hovered ? 0xFFFF9D9D : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("×"), x + X_SIZE / 2, y + 3, WHITE);
    }

    private void renderScaleHandle(DrawContext context, Bounds b, int accent, boolean hovered) {
        int x = b.x + b.w - HANDLE / 2;
        int y = b.y + b.h - HANDLE / 2;
        rect(context, x, y, HANDLE, HANDLE, 0, hovered ? accent : ClientTheme.withAlpha(accent, 210));
        outline(context, x, y, HANDLE, HANDLE, 0, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (handleTopClick(mouseX, mouseY)) return true;

        for (int i = hudModules().size() - 1; i >= 0; i--) {
            HudModule module = hudModules().get(i);
            if (!module.isEnabled() && !showHidden) continue;
            Bounds b = bounds(module);

            if (hoveredClose(mouseX, mouseY, b)) {
                selected = module;
                module.setEnabled(false);
                playClick();
                saveConfig();
                toast(module.getName() + " hidden");
                return true;
            }

            if (hoveredHandle(mouseX, mouseY, b)) {
                selected = module;
                resizing = module;
                resizeStartMouseX = mouseX;
                resizeStartMouseY = mouseY;
                resizeStartScale = scaleOf(module);
                playClick();
                return true;
            }

            if (inside(mouseX, mouseY, b.x, b.y, b.w, b.h)) {
                selected = module;
                dragging = module;
                dragOffsetX = mouseX - b.x;
                dragOffsetY = mouseY - b.y;
                playClick();
                return true;
            }
        }
        selected = null;
        return super.mouseClicked(click, doubled);
    }

    private boolean handleTopClick(int mouseX, int mouseY) {
        int x = 12;
        int y = 10;
        int w = 445;
        if (inside(mouseX, mouseY, x + w + 8, y, 70, 31)) {
            snap = !snap;
            playClick();
            toast(snap ? "Snap enabled" : "Free movement");
            return true;
        }
        if (inside(mouseX, mouseY, x + w + 84, y, 70, 31)) {
            showGrid = !showGrid;
            playClick();
            return true;
        }
        if (inside(mouseX, mouseY, x + w + 160, y, 86, 31)) {
            showHidden = !showHidden;
            playClick();
            toast(showHidden ? "Hidden widgets shown" : "Only visible widgets");
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (dragging != null) {
            Bounds old = bounds(dragging);
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            Bounds moved = moveBounds(old, newX, newY);
            if (snap) moved = smartSnap(dragging, moved);
            moved = clampBounds(moved);
            if (!collides(dragging, moved)) {
                dragging.setPosition(moved.x, moved.y);
            } else {
                toast("Blocked: widgets cannot overlap");
            }
            return true;
        }
        if (resizing != null) {
            float delta = (mouseX - resizeStartMouseX + mouseY - resizeStartMouseY) / 140.0F;
            float newScale = clamp(resizeStartScale + delta, 0.55F, 2.25F);
            float previous = scaleOf(resizing);
            setScale(resizing, newScale);
            Bounds b = clampBounds(bounds(resizing));
            if (collides(resizing, b)) {
                setScale(resizing, previous);
                toast("Blocked: not enough space");
            } else {
                resizing.setPosition(b.x, b.y);
            }
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null || resizing != null) {
            dragging = null;
            resizing = null;
            saveConfig();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        HudModule target = widgetAt(mouseX, mouseY);
        if (target != null) {
            selected = target;
            float previous = scaleOf(target);
            setScale(target, previous + (float) verticalAmount * 0.08F);
            Bounds b = clampBounds(bounds(target));
            if (collides(target, b)) {
                setScale(target, previous);
                toast("Blocked: not enough space");
            } else {
                target.setPosition(b.x, b.y);
                saveConfig();
            }
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
        int key = input.getKeycode();
        if (key == GLFW.GLFW_KEY_G) {
            showGrid = !showGrid;
            playClick();
            return true;
        }
        if (key == GLFW.GLFW_KEY_S) {
            snap = !snap;
            playClick();
            return true;
        }
        if (key == GLFW.GLFW_KEY_H) {
            showHidden = !showHidden;
            playClick();
            return true;
        }
        if (key == GLFW.GLFW_KEY_R && selected != null) {
            Bounds b = bounds(selected);
            selected.setPosition(12, 54 + hudModules().indexOf(selected) * (b.h + 8));
            setScale(selected, 1.0F);
            saveConfig();
            playClick();
            return true;
        }
        if (selected != null && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN)) {
            Bounds old = bounds(selected);
            int dx = key == GLFW.GLFW_KEY_LEFT ? -1 : key == GLFW.GLFW_KEY_RIGHT ? 1 : 0;
            int dy = key == GLFW.GLFW_KEY_UP ? -1 : key == GLFW.GLFW_KEY_DOWN ? 1 : 0;
            Bounds moved = clampBounds(moveBounds(old, old.x + dx, old.y + dy));
            if (!collides(selected, moved)) selected.setPosition(moved.x, moved.y);
            saveConfig();
            return true;
        }
        return super.keyPressed(input);
    }

    private HudModule widgetAt(double mouseX, double mouseY) {
        for (int i = hudModules().size() - 1; i >= 0; i--) {
            HudModule module = hudModules().get(i);
            if (!module.isEnabled() && !showHidden) continue;
            Bounds b = bounds(module);
            if (inside(mouseX, mouseY, b.x, b.y, b.w, b.h)) return module;
        }
        return null;
    }

    private Bounds smartSnap(HudModule moving, Bounds b) {
        int x = Math.round(b.x / (float) GRID) * GRID;
        int y = Math.round(b.y / (float) GRID) * GRID;
        Bounds snapped = new Bounds(x, y, b.w, b.h);

        int[] movingXs = {snapped.x, snapped.x + snapped.w / 2, snapped.x + snapped.w};
        int[] movingYs = {snapped.y, snapped.y + snapped.h / 2, snapped.y + snapped.h};
        int[] screenXs = {0, width / 2, width};
        int[] screenYs = {0, height / 2, height};

        for (int sx : screenXs) {
            for (int i = 0; i < movingXs.length; i++) {
                if (Math.abs(movingXs[i] - sx) <= SNAP_DISTANCE) {
                    snapped = new Bounds(snapped.x + sx - movingXs[i], snapped.y, snapped.w, snapped.h);
                    guideX = sx;
                    break;
                }
            }
        }
        for (int sy : screenYs) {
            for (int i = 0; i < movingYs.length; i++) {
                if (Math.abs(movingYs[i] - sy) <= SNAP_DISTANCE) {
                    snapped = new Bounds(snapped.x, snapped.y + sy - movingYs[i], snapped.w, snapped.h);
                    guideY = sy;
                    break;
                }
            }
        }

        for (HudModule other : hudModules()) {
            if (other == moving || !other.isEnabled()) continue;
            Bounds o = bounds(other);
            int[] oxs = {o.x, o.x + o.w / 2, o.x + o.w};
            int[] oys = {o.y, o.y + o.h / 2, o.y + o.h};
            movingXs = new int[]{snapped.x, snapped.x + snapped.w / 2, snapped.x + snapped.w};
            movingYs = new int[]{snapped.y, snapped.y + snapped.h / 2, snapped.y + snapped.h};
            for (int ox : oxs) {
                for (int i = 0; i < movingXs.length; i++) {
                    if (Math.abs(movingXs[i] - ox) <= SNAP_DISTANCE) {
                        snapped = new Bounds(snapped.x + ox - movingXs[i], snapped.y, snapped.w, snapped.h);
                        guideX = ox;
                    }
                }
            }
            for (int oy : oys) {
                for (int i = 0; i < movingYs.length; i++) {
                    if (Math.abs(movingYs[i] - oy) <= SNAP_DISTANCE) {
                        snapped = new Bounds(snapped.x, snapped.y + oy - movingYs[i], snapped.w, snapped.h);
                        guideY = oy;
                    }
                }
            }
        }
        return snapped;
    }

    private void computeGuides(HudModule module) {
        Bounds b = bounds(module);
        smartSnap(module, b);
    }

    private boolean collides(HudModule moving, Bounds b) {
        for (HudModule other : hudModules()) {
            if (other == moving || !other.isEnabled()) continue;
            Bounds o = bounds(other);
            if (intersects(b, o)) return true;
        }
        return false;
    }

    private Bounds bounds(HudModule module) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float scale = scaleOf(module);
        int w = Math.max(54, Math.round(module.getWidth(mc) * scale));
        int h = Math.max(22, Math.round(module.getHeight(mc) * scale));
        int x = clamp(module.getX(), 0, Math.max(0, width - w));
        int y = clamp(module.getY(), 0, Math.max(0, height - h));
        return new Bounds(x, y, w, h);
    }

    private Bounds moveBounds(Bounds b, int x, int y) {
        return new Bounds(x, y, b.w, b.h);
    }

    private Bounds clampBounds(Bounds b) {
        return new Bounds(clamp(b.x, 0, Math.max(0, width - b.w)), clamp(b.y, 0, Math.max(0, height - b.h)), b.w, b.h);
    }

    private boolean hoveredClose(int mouseX, int mouseY, Bounds b) {
        return inside(mouseX, mouseY, b.x + b.w - X_SIZE + 4, b.y - 7, X_SIZE, X_SIZE);
    }

    private boolean hoveredHandle(int mouseX, int mouseY, Bounds b) {
        return inside(mouseX, mouseY, b.x + b.w - HANDLE / 2, b.y + b.h - HANDLE / 2, HANDLE, HANDLE);
    }

    private void drawDotGrid(DrawContext context, int accent) {
        int color = ClientTheme.withAlpha(accent, 34);
        for (int x = GRID; x < width; x += GRID) {
            for (int y = GRID; y < height; y += GRID) {
                context.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private void renderGuides(DrawContext context, int accent) {
        if (guideX != Integer.MIN_VALUE) context.fill(guideX, 0, guideX + 1, height, ClientTheme.withAlpha(accent, 150));
        if (guideY != Integer.MIN_VALUE) context.fill(0, guideY, width, guideY + 1, ClientTheme.withAlpha(accent, 150));
    }

    private void tinyToggle(DrawContext context, int x, int y, int w, int h, String label, boolean active, int accent) {
        rect(context, x, y, w, h, 0, active ? PremiumRender.SHOP_BUTTON_ACTIVE : PremiumRender.SHOP_BUTTON);
        outline(context, x, y, w, h, 0, active ? accent : LINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 12, WHITE);
    }

    private void renderStatus(DrawContext context) {
        if (System.currentTimeMillis() > statusUntil) return;
        int w = textRenderer.getWidth(status) + 26;
        int x = (width - w) / 2;
        int y = height - 34;
        rect(context, x, y, w, 22, 0, PremiumRender.SHOP_HEADER);
        outline(context, x, y, w, 22, 0, LINE);
        context.drawTextWithShadow(textRenderer, Text.literal(status), x + 13, y + 8, MUTED);
    }

    private float scaleOf(HudModule module) {
        NumberSetting scale = scaleSetting(module);
        if (scale != null) return clamp(scale.getValue().floatValue(), 0.55F, 2.25F);
        return 1.0F;
    }

    private void setScale(HudModule module, float scale) {
        NumberSetting setting = scaleSetting(module);
        if (setting != null) setting.setValue((double) clamp(scale, 0.55F, 2.25F));
    }

    private NumberSetting scaleSetting(HudModule module) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting numberSetting && setting.getName().toLowerCase(Locale.ROOT).contains("scale")) return numberSetting;
        }
        return null;
    }

    private List<HudModule> hudModules() {
        return S9LabClientClient.getModuleManager().getHudModules();
    }

    private void saveConfig() {
        if (S9LabClientClient.getConfigManager() != null) S9LabClientClient.getConfigManager().save();
    }

    private void toast(String text) {
        status = text;
        statusUntil = System.currentTimeMillis() + 1400L;
    }

    private void playClick() {
        // TODO: add 1.21.11-compatible click sound
        // MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return w > 0 && h > 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static boolean intersects(Bounds a, Bounds b) {
        return a.x < b.x + b.w && a.x + a.w > b.x && a.y < b.y + b.h && a.y + a.h > b.y;
    }

    private static void shadow(DrawContext context, int x, int y, int w, int h, int radius) {
        rect(context, x + 2, y + 2, w, h, radius, 0x65000000);
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

    private record Bounds(int x, int y, int w, int h) {}
}
