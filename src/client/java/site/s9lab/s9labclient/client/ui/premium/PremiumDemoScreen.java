package site.s9lab.s9labclient.client.ui.premium;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.ui.ResponsiveLayout;
import site.s9lab.s9labclient.client.ui.ResponsiveScreen;
import site.s9lab.s9labclient.client.ui.ScreenLayout;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.component.ButtonComponent;
import site.s9lab.s9labclient.client.ui.premium.component.CardComponent;
import site.s9lab.s9labclient.client.ui.premium.component.Component;
import site.s9lab.s9labclient.client.ui.premium.component.DropdownComponent;
import site.s9lab.s9labclient.client.ui.premium.component.SearchBarComponent;
import site.s9lab.s9labclient.client.ui.premium.component.SliderComponent;
import site.s9lab.s9labclient.client.ui.premium.component.ToggleComponent;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class PremiumDemoScreen extends ResponsiveScreen {
    private static final int MIN_PANEL_WIDTH = 240;
    private static final int MAX_PANEL_WIDTH = 900;
    private static final int PANEL_MARGIN = 14;
    private static final int HEADER_HEIGHT = 52;
    private static final int COLUMN_MIN_WIDTH = 250;
    private static final int MIN_PANEL_HEIGHT = 190;
    private static final int MAX_PANEL_HEIGHT = 520;
    private static final int SCROLL_STEP = 24;
    private static final int COLOR_MAX = 255;

    private final Screen parent;
    private final AnimationManager animations = new AnimationManager();
    private final List<Component> cards = new ArrayList<>();
    private final List<Component> controls = new ArrayList<>();
    private String search = "";
    private String dropdownValue = "Balanced";
    private double red;
    private double green;
    private double blue;
    private boolean hexFocused;
    private String hexInput;
    private boolean hexReplaceOnType;
    private int scrollOffset;
    private int maxScroll;
    private Rect contentViewport = Rect.empty();
    private Rect hexBounds = Rect.empty();

    public PremiumDemoScreen(Screen parent) {
        super(Text.literal("S9Lab Premium UI"));
        this.parent = parent;
        int accent = ThemeManager.theme().accentColor();
        red = (accent >>> 16) & 255;
        green = (accent >>> 8) & 255;
        blue = accent & 255;
        hexInput = ThemeManager.accentHex();
    }

    @Override
    protected void init() {
        cards.clear();
        controls.clear();
        cards.add(new CardComponent("card.modules", Text.literal("Modules"), Text.literal("Rounded cards with hover motion"), () -> search = "modules"));
        cards.add(new CardComponent("card.cosmetics", Text.literal("Cosmetics"), Text.literal("Preview-ready premium layout"), () -> search = "cosmetics"));
        cards.add(new CardComponent("card.accounts", Text.literal("Accounts"), Text.literal("Clean account manager panels"), () -> search = "accounts"));
        cards.add(new CardComponent("card.settings", Text.literal("Settings"), Text.literal("Persistent accent and blur"), () -> search = "settings"));

        controls.add(new SearchBarComponent("search.main", () -> search, value -> search = value));
        controls.add(new ToggleComponent("toggle.blur", Text.literal("Blur / dark overlay"), () -> ThemeManager.theme().blurBackground(), ThemeManager::setBlurBackground));
        controls.add(new DropdownComponent("dropdown.profile", Text.literal("Animation Profile"), List.of("Fast", "Balanced", "Smooth"), () -> dropdownValue, value -> dropdownValue = value));
        controls.add(new SliderComponent("slider.red", Text.literal("Accent Red"), 0, COLOR_MAX, () -> red, value -> updateAccent(value, green, blue)));
        controls.add(new SliderComponent("slider.green", Text.literal("Accent Green"), 0, COLOR_MAX, () -> green, value -> updateAccent(red, value, blue)));
        controls.add(new SliderComponent("slider.blue", Text.literal("Accent Blue"), 0, COLOR_MAX, () -> blue, value -> updateAccent(red, green, value)));
        controls.add(new ButtonComponent("button.reset", Text.literal("Reset Theme"), this::resetTheme));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.overlay(context, theme);

        Layout layout = layout();
        clampScroll();
        float intro = animations.animate("screen.intro", 1.0F, 6.0F, deltaTicks);
        int lift = Math.round((1.0F - intro) * 18.0F);
        PremiumRender.shopPanel(context, layout.x, layout.y + lift, layout.width, layout.height, HEADER_HEIGHT, 0);
        renderHeader(context, layout, theme);
        renderColumns(context, layout, mouseX, mouseY, deltaTicks, theme);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!contentViewport.contains(click.x(), click.y())) {
            hexFocused = false;
            return super.mouseClicked(click, doubled);
        }
        if (handleHexClick(click)) {
            return true;
        }
        for (Component component : allComponents()) {
            if (component.mouseClicked(click)) {
                return true;
            }
        }
        hexFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (Component component : allComponents()) {
            if (component.mouseReleased(click)) {
                return true;
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (!contentViewport.contains(click.x(), click.y())) {
            return super.mouseDragged(click, offsetX, offsetY);
        }
        for (Component component : allComponents()) {
            if (component.mouseDragged(click, offsetX, offsetY)) {
                return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!contentViewport.contains(mouseX, mouseY) || maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scrollOffset = clamp(scrollOffset - (int) Math.round(verticalAmount * SCROLL_STEP), 0, maxScroll);
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (hexFocused && input.isValidChar()) {
            String value = input.asString();
            if (hexReplaceOnType) {
                hexInput = value.equals("#") ? "#" : "";
                hexReplaceOnType = false;
                if (value.equals("#")) {
                    return true;
                }
            }
            if (hexInput.length() < 7 && value.matches("[0-9a-fA-F#]")) {
                hexInput += value;
                applyHexIfValid();
            }
            return true;
        }
        for (Component component : allComponents()) {
            if (component.charTyped(input)) {
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (hexFocused) {
            if (input.isEscape() || input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                hexFocused = false;
                applyHexIfValid();
                return true;
            }
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !hexInput.isEmpty()) {
                if (hexReplaceOnType) {
                    hexInput = "";
                    hexReplaceOnType = false;
                    return true;
                }
                hexInput = hexInput.substring(0, hexInput.length() - 1);
                applyHexIfValid();
                return true;
            }
        }
        for (Component component : allComponents()) {
            if (component.keyPressed(input)) {
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    protected void onResponsiveResize() {
        clampScroll();
    }

    private void renderHeader(DrawContext context, Layout layout, ClientTheme theme) {
        MinecraftClient client = MinecraftClient.getInstance();
        int x = layout.x + layout.innerPadding;
        int y = layout.y + 13;
        context.drawTextWithShadow(client.textRenderer, Text.literal("S9LAB PREMIUM UI"), x, y, theme.textColor());
        if (layout.width >= 360) {
            context.drawTextWithShadow(client.textRenderer, Text.literal("Reusable components, smooth animation, persistent theme"), x, y + 15, theme.mutedTextColor());
        } else {
            context.drawTextWithShadow(client.textRenderer, Text.literal("Responsive UI system"), x, y + 15, theme.mutedTextColor());
        }
        int chipWidth = 68;
        if (layout.width >= chipWidth + layout.innerPadding * 2 + 130) {
            PremiumRender.roundedRect(context, layout.x + layout.width - layout.innerPadding - chipWidth, layout.y + 15, chipWidth, 22, 0, PremiumRender.SHOP_BUTTON_ACTIVE);
            PremiumRender.centeredText(context, Text.literal(ThemeManager.accentHex()), layout.x + layout.width - layout.innerPadding - chipWidth / 2, layout.y + 22, theme.textColor());
        }
    }

    private void renderColumns(DrawContext context, Layout layout, int mouseX, int mouseY, float deltaTicks, ClientTheme theme) {
        int contentX = layout.x + layout.innerPadding;
        int contentY = layout.y + HEADER_HEIGHT;
        int contentW = layout.width - layout.innerPadding * 2;
        int contentH = layout.height - HEADER_HEIGHT - layout.innerPadding;
        contentViewport = new Rect(contentX, contentY, contentW, contentH);
        boolean twoColumns = contentW >= COLUMN_MIN_WIDTH * 2 + theme.gap();
        int leftW = twoColumns ? (contentW - theme.gap()) / 2 : contentW;
        int rightX = twoColumns ? contentX + leftW + theme.gap() : contentX;
        int rightY = twoColumns ? contentY : contentY + cardsHeight(theme) + theme.gap();
        int rightW = twoColumns ? contentW - leftW - theme.gap() : contentW;
        int totalHeight = twoColumns ? Math.max(cardsHeight(theme), controlsHeight(theme)) : cardsHeight(theme) + theme.gap() + controlsHeight(theme);
        maxScroll = Math.max(0, totalHeight - contentH);
        clampScroll();

        context.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        renderComponentList(context, cards, contentX, contentY - scrollOffset, leftW, contentH + scrollOffset, mouseX, mouseY, deltaTicks, theme);
        renderControls(context, rightX, rightY - scrollOffset, rightW, controlsHeight(theme), mouseX, mouseY, deltaTicks, theme);
        context.disableScissor();
        renderScrollbar(context, contentX + contentW - 4, contentY, contentH, theme);
    }

    private void renderControls(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float deltaTicks, ClientTheme theme) {
        MinecraftClient client = MinecraftClient.getInstance();
        PremiumRender.card(context, x, y, width, height, 0, PremiumRender.SHOP_CARD, PremiumRender.SHOP_SOFT_BORDER);
        context.drawTextWithShadow(client.textRenderer, Text.literal("Theme Controls"), x + 12, y + 10, theme.textColor());
        context.drawTextWithShadow(client.textRenderer, Text.literal("RGB + HEX accent support"), x + 12, y + 24, theme.mutedTextColor());

        int cursorY = y + 45;
        for (Component component : controls) {
            component.bounds(x + 10, cursorY, width - 20, component.preferredHeight());
            component.render(context, mouseX, mouseY, deltaTicks, theme, animations);
            cursorY += component.preferredHeight() + theme.gap();
        }
        renderHexInput(context, x + 10, cursorY, width - 20, theme);
    }

    private void renderComponentList(DrawContext context, List<Component> components, int x, int y, int width, int height, int mouseX, int mouseY, float deltaTicks, ClientTheme theme) {
        int cursorY = y;
        for (Component component : components) {
            component.bounds(x, cursorY, width, component.preferredHeight());
            component.render(context, mouseX, mouseY, deltaTicks, theme, animations);
            cursorY += component.preferredHeight() + theme.gap();
            if (cursorY > y + height) {
                break;
            }
        }
    }

    private void renderHexInput(DrawContext context, int x, int y, int width, ClientTheme theme) {
        MinecraftClient client = MinecraftClient.getInstance();
        hexBounds = new Rect(x, y, width, 28);
        int border = hexFocused ? ClientTheme.withAlpha(theme.accentColor(), 190) : theme.borderColor();
        PremiumRender.card(context, x, y, width, 28, 0, PremiumRender.SHOP_INPUT, border);
        context.drawTextWithShadow(client.textRenderer, Text.literal("HEX"), x + LABEL_PAD(), y + 10, theme.mutedTextColor());
        String value = hexInput + (hexFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(client.textRenderer, Text.literal(value), x + width - LABEL_PAD() - client.textRenderer.getWidth(value), y + 10, theme.textColor());
    }

    private boolean handleHexClick(Click click) {
        hexFocused = contentViewport.contains(click.x(), click.y()) && hexBounds.contains(click.x(), click.y());
        if (hexFocused) {
            hexReplaceOnType = true;
        }
        return hexFocused;
    }

    private List<Component> allComponents() {
        List<Component> all = new ArrayList<>(cards);
        all.addAll(controls);
        return all;
    }

    private int cardsHeight(ClientTheme theme) {
        return cards.stream().mapToInt(Component::preferredHeight).sum() + Math.max(0, cards.size() - 1) * theme.gap();
    }

    private int controlsHeight(ClientTheme theme) {
        int content = 45 + controls.stream().mapToInt(Component::preferredHeight).sum()
                + Math.max(0, controls.size()) * theme.gap() + 28 + 10;
        return Math.max(140, content);
    }

    private void renderScrollbar(DrawContext context, int x, int y, int height, ClientTheme theme) {
        if (maxScroll <= 0) {
            return;
        }
        int trackHeight = Math.max(24, height);
        int thumbHeight = Math.max(18, Math.round(trackHeight * (trackHeight / (float) (trackHeight + maxScroll))));
        int thumbY = y + Math.round((trackHeight - thumbHeight) * (scrollOffset / (float) maxScroll));
        PremiumRender.roundedRect(context, x, y, 3, trackHeight, 2, 0x55313748);
        PremiumRender.roundedRect(context, x, thumbY, 3, thumbHeight, 2, ClientTheme.withAlpha(theme.accentColor(), 190));
    }

    private void updateAccent(double red, double green, double blue) {
        this.red = clampColor(red);
        this.green = clampColor(green);
        this.blue = clampColor(blue);
        int color = 0xFF000000 | ((int) this.red << 16) | ((int) this.green << 8) | (int) this.blue;
        ThemeManager.setAccentColor(color);
        hexInput = ThemeManager.accentHex();
    }

    private void resetTheme() {
        red = (ClientTheme.DEFAULT_ACCENT >>> 16) & 255;
        green = (ClientTheme.DEFAULT_ACCENT >>> 8) & 255;
        blue = ClientTheme.DEFAULT_ACCENT & 255;
        ThemeManager.setAccentColor(ClientTheme.DEFAULT_ACCENT);
        ThemeManager.setBlurBackground(true);
        hexInput = ThemeManager.accentHex();
    }

    private void applyHexIfValid() {
        String value = hexInput.startsWith("#") ? hexInput.substring(1) : hexInput;
        if (!value.matches("[0-9a-fA-F]{6}")) {
            return;
        }
        int color = Integer.parseInt(value, 16);
        updateAccent((color >>> 16) & 255, (color >>> 8) & 255, color & 255);
    }

    private Layout layout() {
        ScreenLayout layout = centeredLayout(MAX_PANEL_WIDTH, MAX_PANEL_HEIGHT, MIN_PANEL_WIDTH, MIN_PANEL_HEIGHT);
        int innerPadding = ResponsiveLayout.adaptivePadding(layout.width(), layout.height());
        return new Layout(layout.x(), layout.y(), layout.width(), layout.height(), innerPadding);
    }

    private double clampColor(double value) {
        return Math.max(0.0D, Math.min(COLOR_MAX, value));
    }

    private int LABEL_PAD() {
        return 9;
    }

    private void clampScroll() {
        scrollOffset = clamp(scrollOffset, 0, maxScroll);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Layout(int x, int y, int width, int height, int innerPadding) {
    }

    private record Rect(int x, int y, int width, int height) {
        private static Rect empty() {
            return new Rect(0, 0, 0, 0);
        }

        private boolean contains(double mouseX, double mouseY) {
            return PremiumRender.inside(mouseX, mouseY, x, y, width, height);
        }
    }
}
