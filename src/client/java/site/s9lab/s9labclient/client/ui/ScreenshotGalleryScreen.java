package site.s9lab.s9labclient.client.ui;

import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.screenshot.ScreenshotManager;
import site.s9lab.s9labclient.client.screenshot.ScreenshotMetadata;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ScreenshotGalleryScreen extends ResponsiveScreen {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int CARD_MIN_W = 158;
    private static final int CARD_H = 124;
    private static final int CARD_GAP = 10;

    private final Screen parent;
    private final Map<String, PreviewTexture> textureCache = new HashMap<>();

    private String search = "";
    private boolean searchFocused;
    private int scroll;
    private ScreenshotMetadata preview;
    private String status = "";

    public ScreenshotGalleryScreen(Screen parent) {
        super(Text.literal("S9Lab Screenshots"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x77000000);

        Layout layout = layout();
        context.fill(layout.x + 3, layout.y + 3, layout.x + layout.width + 3, layout.y + layout.height + 3, 0x77000000);
        PremiumRender.roundedRect(context, layout.x, layout.y, layout.width, layout.height, 2, 0xE914161A);
        PremiumRender.outline(context, layout.x, layout.y, layout.width, layout.height, 2, 0xFF2D3138);
        context.fill(layout.x, layout.y, layout.x + layout.width, layout.y + 48, 0xDD1A1C21);
        drawHeader(context, layout, theme);
        drawSearch(context, layout.searchX(), layout.searchY(), layout.searchW, mouseX, mouseY);

        int gridX = layout.x + layout.pad;
        int gridY = layout.gridY();
        int gridW = layout.width - layout.pad * 2;
        int gridH = layout.gridHeight();
        List<ScreenshotMetadata> entries = ScreenshotManager.search(search);
        int columns = ResponsiveLayout.columns(gridW, CARD_MIN_W, 4);
        int cardW = Math.max(1, (gridW - CARD_GAP * (columns - 1)) / columns);

        context.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        for (int i = 0; i < entries.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = gridX + col * (cardW + CARD_GAP);
            int y = gridY + row * (CARD_H + CARD_GAP) - scroll;
            if (y + CARD_H < gridY || y > gridY + gridH) {
                continue;
            }
            drawScreenshotCard(context, entries.get(i), x, y, cardW, CARD_H, mouseX, mouseY);
        }
        context.disableScissor();

        if (entries.isEmpty()) {
            drawEmptyState(context, gridY, gridH, theme);
        }

        drawFooter(context, layout, entries.size());
        if (preview != null) {
            drawPreviewModal(context, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (preview != null) {
            return handlePreviewClick(mouseX, mouseY) || super.mouseClicked(click, doubled);
        }

        Layout layout = layout();
        if (inside(mouseX, mouseY, layout.searchX(), layout.searchY(), layout.searchW, 24)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (inside(mouseX, mouseY, layout.x + layout.width - layout.pad - 70, layout.y + layout.height - 30, 70, 22)) {
            close();
            return true;
        }

        int gridX = layout.x + layout.pad;
        int gridY = layout.gridY();
        int gridW = layout.width - layout.pad * 2;
        int gridH = layout.gridHeight();
        if (!inside(mouseX, mouseY, gridX, gridY, gridW, gridH)) {
            return super.mouseClicked(click, doubled);
        }

        List<ScreenshotMetadata> entries = ScreenshotManager.search(search);
        int columns = ResponsiveLayout.columns(gridW, CARD_MIN_W, 4);
        int cardW = Math.max(1, (gridW - CARD_GAP * (columns - 1)) / columns);
        for (int i = 0; i < entries.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = gridX + col * (cardW + CARD_GAP);
            int y = gridY + row * (CARD_H + CARD_GAP) - scroll;
            if (inside(mouseX, mouseY, x, y, cardW, CARD_H)) {
                preview = entries.get(i);
                status = "";
                getTexture(preview);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = layout();
        int gridW = layout.width - layout.pad * 2;
        int gridH = layout.gridHeight();
        if (!inside(mouseX, mouseY, layout.x + layout.pad, layout.gridY(), gridW, gridH)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scroll = ResponsiveLayout.scroll(scroll, verticalAmount, maxScroll(gridW, gridH));
        return true;
    }

    @Override
    protected void onResponsiveResize() {
        Layout layout = layout();
        scroll = clamp(scroll, 0, maxScroll(layout.width - layout.pad * 2, layout.gridHeight()));
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!searchFocused || !input.isValidChar()) {
            return super.charTyped(input);
        }
        if (search.length() < 48) {
            search += input.asString();
            scroll = 0;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (preview != null && input.isEscape()) {
            preview = null;
            return true;
        }
        if (searchFocused) {
            if (input.isEscape() || input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void drawHeader(DrawContext context, Layout layout, ClientTheme theme) {
        context.drawTextWithShadow(this.textRenderer, Text.literal("§lScreenshot Gallery"), layout.x + layout.pad, layout.y + 13, theme.textColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal("Browse, search, preview and share your captures"), layout.x + layout.pad, layout.y + 30, theme.mutedTextColor());
        int pillX = layout.x + layout.pad;
        int pillY = layout.y + 47;
        PremiumRender.card(context, pillX, pillY, 108, 17, 2, 0x66141B2A, 0x667CFFB2);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("PNG previews"), pillX + 54, pillY + 5, theme.accentColor());
    }

    private void drawScreenshotCard(DrawContext context, ScreenshotMetadata metadata, int x, int y, int width, int height, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        int border = hovered ? theme.accentColor() : 0x662C3344;
        int background = hovered ? 0xD91A2030 : 0xB9141822;
        PremiumRender.card(context, x, y, width, height, 2, background, border);

        int imageX = x + 8;
        int imageY = y + 8;
        int imageW = width - 16;
        int imageH = 68;
        PremiumRender.roundedRect(context, imageX, imageY, imageW, imageH, 2, 0xFF070A10);
        drawPreviewImage(context, metadata, imageX + 2, imageY + 2, imageW - 4, imageH - 4, false);
        if (hovered) {
            context.fill(imageX, imageY + imageH - 15, imageX + imageW, imageY + imageH, 0xAA000000);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Click to inspect"), imageX + imageW / 2, imageY + imageH - 12, 0xFFFFFFFF);
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal(trim(metadata.server(), Math.max(8, (width - 16) / 6))), x + 8, y + 84, theme.textColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal(shortDate(metadata) + "  •  " + metadata.resolution()), x + 8, y + 99, theme.mutedTextColor());
    }

    private void drawPreviewImage(DrawContext context, ScreenshotMetadata metadata, int x, int y, int width, int height, boolean large) {
        ClientTheme theme = ThemeManager.theme();
        PreviewTexture texture = getTexture(metadata);
        if (texture == null || texture.id == null || texture.width <= 0 || texture.height <= 0) {
            context.fill(x, y, x + width, y + height, 0xFF101522);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Preview not available"), x + width / 2, y + height / 2 - 4, theme.mutedTextColor());
            return;
        }

        double scale = Math.min(width / (double) texture.width, height / (double) texture.height);
        int drawW = Math.max(1, (int) Math.round(texture.width * scale));
        int drawH = Math.max(1, (int) Math.round(texture.height * scale));
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;

        context.fill(x, y, x + width, y + height, 0xFF070A10);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture.id, drawX, drawY, 0.0F, 0.0F, drawW, drawH, texture.width, texture.height, texture.width, texture.height);

        if (large) {
            context.fill(x, y + height - 20, x + width, y + height, 0xAA000000);
            context.drawTextWithShadow(this.textRenderer, Text.literal(metadata.resolution()), x + 8, y + height - 15, 0xFFFFFFFF);
        }
    }

    private PreviewTexture getTexture(ScreenshotMetadata metadata) {
        String key = metadata.absolutePath();
        PreviewTexture cached = textureCache.get(key);
        if (cached != null) {
            return cached;
        }

        PreviewTexture texture = PreviewTexture.empty();
        textureCache.put(key, texture);
        if (metadata.path() == null || !Files.exists(metadata.path())) {
            return texture;
        }

        try (InputStream input = Files.newInputStream(metadata.path())) {
            NativeImage image = NativeImage.read(input);
            texture.width = image.getWidth();
            texture.height = image.getHeight();
            texture.id = Identifier.of("s9labclient", "gallery_preview_" + Integer.toUnsignedString(key.hashCode()));
            texture.texture = new NativeImageBackedTexture(() -> "s9lab gallery preview " + metadata.fileName(), image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(texture.id, texture.texture);
        } catch (Exception ignored) {
            texture.id = null;
            texture.width = 0;
            texture.height = 0;
            texture.texture = null;
        }
        return texture;
    }

    private void drawSearch(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean active = searchFocused || inside(mouseX, mouseY, x, y, width, 24);
        PremiumRender.card(context, x, y, width, 24, 2, 0xB9141822, active ? theme.accentColor() : 0x662C3344);
        String value = search.isBlank() && !searchFocused ? "Search screenshots..." : search + (searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.drawTextWithShadow(this.textRenderer, Text.literal(value), x + 9, y + 8, search.isBlank() && !searchFocused ? theme.mutedTextColor() : theme.textColor());
    }

    private void drawFooter(DrawContext context, Layout layout, int count) {
        ClientTheme theme = ThemeManager.theme();
        int max = maxScroll(layout.width - layout.pad * 2, layout.gridHeight());
        String scrollText = max > 0 ? " | scroll " + (int) ((scroll / (double) max) * 100.0D) + "%" : "";
        context.drawTextWithShadow(this.textRenderer, Text.literal(count + " screenshots | /s9c screenshots" + scrollText), layout.x + layout.pad, layout.y + layout.height - 23, theme.mutedTextColor());
        drawSmallButton(context, layout.x + layout.width - layout.pad - 70, layout.y + layout.height - 30, 70, 22, "Done", false);
    }

    private void drawEmptyState(DrawContext context, int gridY, int gridH, ClientTheme theme) {
        int boxW = Math.min(310, this.width - 60);
        int boxX = (this.width - boxW) / 2;
        int boxY = gridY + gridH / 2 - 38;
        PremiumRender.card(context, boxX, boxY, boxW, 76, 2, 0xAA0A0D15, 0x662C3344);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No screenshots found"), this.width / 2, boxY + 17, theme.textColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press F2 in game, then reopen this gallery."), this.width / 2, boxY + 36, theme.mutedTextColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Search filters file, server, world and date."), this.width / 2, boxY + 52, theme.mutedTextColor());
    }

    private void drawPreviewModal(DrawContext context, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        context.fill(0, 0, this.width, this.height, 0xB6000000);
        ScreenLayout modal = centeredLayout(620, 410, 280, 250);
        int modalW = modal.width();
        int modalH = modal.height();
        int x = (this.width - modalW) / 2;
        int y = (this.height - modalH) / 2;
        PremiumRender.card(context, x, y, modalW, modalH, 2, 0xE914161A, theme.accentColor());

        context.drawTextWithShadow(this.textRenderer, Text.literal("§l" + trim(preview.fileName(), Math.max(18, (modalW - 90) / 6))), x + 18, y + 16, theme.textColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal(preview.server() + "  •  " + fullDate(preview)), x + 18, y + 32, theme.mutedTextColor());
        drawSmallButton(context, x + modalW - 42, y + 12, 24, 24, "X", inside(mouseX, mouseY, x + modalW - 42, y + 12, 24, 24));

        int previewX = x + 18;
        int previewY = y + 56;
        int previewW = modalW - 36;
        int previewH = Math.max(72, modalH - 170);
        PremiumRender.card(context, previewX, previewY, previewW, previewH, 2, 0xFF070A10, 0x662C3344);
        drawPreviewImage(context, preview, previewX + 3, previewY + 3, previewW - 6, previewH - 6, true);

        int metaY = previewY + previewH + 10;
        context.drawTextWithShadow(this.textRenderer, Text.literal("World: " + trim(preview.world(), Math.max(18, (modalW - 36) / 6))), x + 18, metaY, theme.mutedTextColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal("Path: " + trim(preview.absolutePath(), Math.max(24, (modalW - 36) / 6))), x + 18, metaY + 14, theme.mutedTextColor());

        int buttonY = y + modalH - 34;
        ButtonGrid buttons = ButtonGrid.create(x + 18, buttonY, modalW - 36, modalW < 420);
        drawSmallButton(context, buttons.x(0), buttons.y(0), buttons.buttonWidth(), 22, "Open", inside(mouseX, mouseY, buttons.x(0), buttons.y(0), buttons.buttonWidth(), 22));
        drawSmallButton(context, buttons.x(1), buttons.y(1), buttons.buttonWidth(), 22, "Copy", inside(mouseX, mouseY, buttons.x(1), buttons.y(1), buttons.buttonWidth(), 22));
        drawSmallButton(context, buttons.x(2), buttons.y(2), buttons.buttonWidth(), 22, "Discord", inside(mouseX, mouseY, buttons.x(2), buttons.y(2), buttons.buttonWidth(), 22));
        drawSmallButton(context, buttons.x(3), buttons.y(3), buttons.buttonWidth(), 22, "Delete", inside(mouseX, mouseY, buttons.x(3), buttons.y(3), buttons.buttonWidth(), 22));
        if (!status.isBlank()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(status), x + 18, y + modalH - 50, theme.accentColor());
        }
    }

    private boolean handlePreviewClick(int mouseX, int mouseY) {
        ScreenLayout modal = centeredLayout(620, 410, 280, 250);
        int modalW = modal.width();
        int modalH = modal.height();
        int x = (this.width - modalW) / 2;
        int y = (this.height - modalH) / 2;
        if (inside(mouseX, mouseY, x + modalW - 42, y + 12, 24, 24)) {
            preview = null;
            return true;
        }
        int buttonY = y + modalH - 34;
        ButtonGrid buttons = ButtonGrid.create(x + 18, buttonY, modalW - 36, modalW < 420);
        if (inside(mouseX, mouseY, buttons.x(0), buttons.y(0), buttons.buttonWidth(), 22)) {
            ScreenshotManager.openFile(preview);
            //status = "Opened screenshot.";
            return true;
        }
        if (inside(mouseX, mouseY, buttons.x(1), buttons.y(1), buttons.buttonWidth(), 22)) {
            ScreenshotManager.copyToClipboard(preview);
            //status = "Copied image or path.";
            return true;
        }
        if (inside(mouseX, mouseY, buttons.x(2), buttons.y(2), buttons.buttonWidth(), 22)) {
            ScreenshotManager.shareOnDiscord(preview);
            //status = "Copied path and opened Discord.";
            return true;
        }
        if (inside(mouseX, mouseY, buttons.x(3), buttons.y(3), buttons.buttonWidth(), 22)) {
            if (ScreenshotManager.delete(preview)) {
                textureCache.remove(preview.absolutePath());
                preview = null;
                status = "Deleted screenshot.";
            } else {
                status = "Delete failed.";
            }
            return true;
        }
        return false;
    }

    private void drawSmallButton(DrawContext context, int x, int y, int width, int height, String label, boolean hovered) {
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, x, y, width, height, 2, hovered ? 0xD91A2030 : 0xB9141822, hovered ? theme.accentColor() : 0x662C3344);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + width / 2, y + (height - 8) / 2, theme.textColor());
    }

    private int maxScroll(int gridW, int gridH) {
        int columns = ResponsiveLayout.columns(gridW, CARD_MIN_W, 4);
        int rows = (ScreenshotManager.search(search).size() + columns - 1) / columns;
        int content = rows * (CARD_H + CARD_GAP);
        return Math.max(0, content - gridH);
    }

    private Layout layout() {
        ScreenLayout screen = centeredLayout(880, 560, 280, 240);
        int pad = screen.padding();
        int searchW = screen.width() < 540 ? Math.max(100, screen.width() - pad * 2) : 250;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), pad, searchW);
    }

    private static String shortDate(ScreenshotMetadata metadata) {
        try {
            return DISPLAY_DATE.format(Instant.parse(metadata.date()).atZone(ZoneId.systemDefault())).substring(0, 10);
        } catch (Exception exception) {
            return metadata.date();
        }
    }

    private static String fullDate(ScreenshotMetadata metadata) {
        try {
            return DISPLAY_DATE.format(Instant.parse(metadata.date()).atZone(ZoneId.systemDefault()));
        } catch (Exception exception) {
            return metadata.date();
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private record Layout(int x, int y, int width, int height, int pad, int searchW) {
        private int gridY() {
            return y + (width < 540 ? 94 : 74);
        }

        private int gridHeight() {
            return Math.max(44, y + height - 40 - gridY());
        }

        private int searchX() {
            return width < 540 ? x + pad : x + width - searchW - pad;
        }

        private int searchY() {
            return width < 540 ? y + 48 : y + 14;
        }
    }

    private record ButtonGrid(int x, int y, int width, int columns, int gap) {
        private static ButtonGrid create(int x, int y, int width, boolean compact) {
            return new ButtonGrid(x, y, width, compact ? 2 : 4, 4);
        }

        private int buttonWidth() {
            return Math.max(48, (width - gap * (columns - 1)) / columns);
        }

        private int x(int index) {
            return x + (index % columns) * (buttonWidth() + gap);
        }

        private int y(int index) {
            return y + (index / columns) * 28;
        }
    }

    private static final class PreviewTexture {
        private Identifier id;
        private NativeImageBackedTexture texture;
        private int width;
        private int height;

        private static PreviewTexture empty() {
            return new PreviewTexture();
        }
    }
}
