package site.s9lab.s9labclient.client.ui;

import java.io.InputStream;
import java.nio.file.Files;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.screenshot.ScreenshotManager;
import site.s9lab.s9labclient.client.screenshot.ScreenshotMetadata;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ScreenshotOverlayScreen extends ResponsiveScreen {
    private final Screen parent;
    private final ScreenshotMetadata metadata;
    private String status = "Screenshot saved.";

    private Identifier previewTextureId;
    private NativeImageBackedTexture previewTexture;
    private int previewTextureWidth;
    private int previewTextureHeight;
    private boolean previewLoadAttempted;

    public ScreenshotOverlayScreen(Screen parent, ScreenshotMetadata metadata) {
        super(Text.literal("S9Lab Screenshot"));
        this.parent = parent;
        this.metadata = metadata;
    }

    @Override
    protected void init() {
        loadPreviewTexture();
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

        Panel panel = panel();
        int panelW = panel.width();
        int panelH = panel.height();
        int x = panel.x();
        int y = panel.y();

        PremiumRender.card(context, x, y, panelW, panelH, theme.radius(), theme.panelColor(), theme.accentColor());
        drawHeader(context, x, y, panelW, theme);

        int previewX = x + 18;
        int previewY = y + 72;
        int previewW = panelW - 36;
        int previewH = Math.max(78, panelH - 162);
        PremiumRender.card(context, previewX, previewY, previewW, previewH, theme.radius(), 0xFF070A10, theme.borderColor());
        renderPreview(context, previewX + 3, previewY + 3, previewW - 6, previewH - 6);

        int buttonY = y + panelH - 72;
        int columns = panelW < 360 ? 2 : 4;
        int gap = 8;
        int buttonW = (panelW - 36 - gap * (columns - 1)) / columns;
        drawButton(context, x + 18, buttonY, buttonW, 24, "Open", mouseX, mouseY);
        drawButton(context, x + 18 + (buttonW + gap), buttonY, buttonW, 24, "Copy", mouseX, mouseY);
        drawButton(context, x + 18 + (buttonW + gap) * (columns == 2 ? 0 : 2), buttonY + (columns == 2 ? 32 : 0), buttonW, 24, "Discord", mouseX, mouseY);
        drawButton(context, x + 18 + (buttonW + gap) * (columns == 2 ? 1 : 3), buttonY + (columns == 2 ? 32 : 0), buttonW, 24, "Delete", mouseX, mouseY);

        int pillW = Math.min(panelW - 36, Math.max(120, this.textRenderer.getWidth(status) + 18));
        PremiumRender.card(context, x + 18, y + panelH - 23, pillW, 17, theme.radius(), 0x66141B2A, 0x667CFFB2);
        context.drawTextWithShadow(this.textRenderer, Text.literal(status), x + 27, y + panelH - 18, theme.mutedTextColor());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Panel panel = panel();
        int panelW = panel.width();
        int panelH = panel.height();
        int x = panel.x();
        int y = panel.y();
        int buttonY = y + panelH - 72;
        int columns = panelW < 360 ? 2 : 4;
        int gap = 8;
        int buttonW = (panelW - 36 - gap * (columns - 1)) / columns;
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        if (inside(mouseX, mouseY, x + 18, buttonY, buttonW, 24)) {
            ScreenshotManager.openFile(metadata);
            status = "Opened screenshot.";
            return true;
        }
        if (inside(mouseX, mouseY, x + 18 + buttonW + gap, buttonY, buttonW, 24)) {
            ScreenshotManager.copyToClipboard(metadata);
            status = "Copied image or path.";
            return true;
        }
        if (inside(mouseX, mouseY, x + 18 + (buttonW + gap) * (columns == 2 ? 0 : 2), buttonY + (columns == 2 ? 32 : 0), buttonW, 24)) {
            ScreenshotManager.shareOnDiscord(metadata);
            status = "Copied path and opened Discord.";
            return true;
        }
        if (inside(mouseX, mouseY, x + 18 + (buttonW + gap) * (columns == 2 ? 1 : 3), buttonY + (columns == 2 ? 32 : 0), buttonW, 24)) {
            status = ScreenshotManager.delete(metadata) ? "Deleted screenshot." : "Delete failed.";
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void drawHeader(DrawContext context, int x, int y, int panelW, ClientTheme theme) {
        context.drawTextWithShadow(this.textRenderer, Text.literal("§lS9Lab Screenshot"), x + 18, y + 14, theme.textColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal(trim(metadata.fileName(), Math.max(18, (panelW - 36) / 6))), x + 18, y + 32, theme.mutedTextColor());
        int badgeY = y + 51;
        int serverW = Math.min(panelW - 36, this.textRenderer.getWidth(metadata.server()) + 20);
        PremiumRender.card(context, x + 18, badgeY, serverW, 17, theme.radius(), 0x66141B2A, 0x667CFFB2);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(metadata.server()), x + 18 + serverW / 2, badgeY + 5, 0xFF7CFFB2);
        context.drawTextWithShadow(this.textRenderer, Text.literal(metadata.resolution()), x + panelW - 18 - this.textRenderer.getWidth(metadata.resolution()), badgeY + 5, theme.mutedTextColor());
    }

    private void loadPreviewTexture() {
        if (previewLoadAttempted) return;
        previewLoadAttempted = true;

        if (metadata == null || metadata.path() == null || !Files.exists(metadata.path())) {
            return;
        }

        try (InputStream input = Files.newInputStream(metadata.path())) {
            NativeImage image = NativeImage.read(input);
            previewTextureWidth = image.getWidth();
            previewTextureHeight = image.getHeight();
            previewTexture = new NativeImageBackedTexture(() -> "s9lab overlay preview " + metadata.fileName(), image);
            previewTextureId = Identifier.of("s9labclient", "overlay_preview_" + Integer.toUnsignedString(metadata.absolutePath().hashCode()));
            MinecraftClient.getInstance().getTextureManager().registerTexture(previewTextureId, previewTexture);
        } catch (Exception ignored) {
            previewTextureId = null;
            previewTexture = null;
            previewTextureWidth = 0;
            previewTextureHeight = 0;
        }
    }

    private void renderPreview(DrawContext context, int x, int y, int width, int height) {
        ClientTheme theme = ThemeManager.theme();
        if (previewTextureId == null || previewTextureWidth <= 0 || previewTextureHeight <= 0) {
            context.fill(x, y, x + width, y + height, 0xFF101522);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Preview not available"), x + width / 2, y + height / 2 - 5, theme.mutedTextColor());
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(trim(metadata.absolutePath(), Math.max(18, width / 6))), x + width / 2, y + height / 2 + 10, theme.mutedTextColor());
            return;
        }

        double scale = Math.min(width / (double) previewTextureWidth, height / (double) previewTextureHeight);
        int drawW = Math.max(1, (int) Math.round(previewTextureWidth * scale));
        int drawH = Math.max(1, (int) Math.round(previewTextureHeight * scale));
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;

        context.fill(x, y, x + width, y + height, 0xFF070A10);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, previewTextureId, drawX, drawY, 0.0F, 0.0F, drawW, drawH, previewTextureWidth, previewTextureHeight, previewTextureWidth, previewTextureHeight);
        context.fill(x, y + height - 20, x + width, y + height, 0xAA000000);
        context.drawTextWithShadow(this.textRenderer, Text.literal(metadata.resolution()), x + 8, y + height - 15, 0xFFFFFFFF);
    }

    private void drawButton(DrawContext context, int x, int y, int width, int height, String label, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, theme.radius(), hovered ? theme.cardHoverColor() : theme.cardColor(), hovered ? theme.accentColor() : theme.borderColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + width / 2, y + 8, theme.textColor());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private Panel panel() {
        ScreenLayout layout = centeredLayout(460, 310, 280, 230);
        int y = Math.min(layout.y(), Math.max(layout.margin(), this.height - layout.height() - layout.margin() - 16));
        return new Panel(layout.x(), y, layout.width(), layout.height());
    }

    private record Panel(int x, int y, int width, int height) {
    }
}
