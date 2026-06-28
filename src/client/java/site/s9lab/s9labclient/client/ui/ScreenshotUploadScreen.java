package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.dev.DevModeManager;
import site.s9lab.s9labclient.client.notification.S9ToastManager;
import site.s9lab.s9labclient.client.screenshot.ScreenshotMetadata;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ScreenshotUploadScreen extends ResponsiveScreen {
    private final Screen parent;
    private final ScreenshotMetadata metadata;
    private TextFieldWidget captionField;
    private String status = DevModeManager.isEnabled() ? "Ready to upload as WebP." : "Enable /s9c dev first.";
    private boolean uploading;
    private boolean publicPost = true;

    public ScreenshotUploadScreen(Screen parent, ScreenshotMetadata metadata) {
        super(Text.literal("S9Lab Screenshot Upload"));
        this.parent = parent;
        this.metadata = metadata;
    }

    @Override
    protected void init() {
        Panel panel = panel();
        captionField = new TextFieldWidget(textRenderer, panel.x + 24, panel.y + 116, panel.width - 48, 22, Text.literal("Caption"));
        captionField.setMaxLength(160);
        captionField.setPlaceholder(Text.literal("Caption fuer den Feed..."));
        addDrawableChild(captionField);
        setInitialFocus(captionField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.shopBackdrop(context);
        Panel panel = panel();
        PremiumRender.shopPanel(context, panel.x, panel.y, panel.width, panel.height, 74, 0);
        context.drawTextWithShadow(textRenderer, Text.literal("UPLOAD SCREENSHOT"), panel.x + 24, panel.y + 20, theme.textColor());
        context.drawTextWithShadow(textRenderer, Text.literal(metadata.fileName()), panel.x + 24, panel.y + 42, theme.mutedTextColor());
        context.drawTextWithShadow(textRenderer, Text.literal(metadata.server() + " | " + metadata.resolution()), panel.x + 24, panel.y + 62, theme.mutedTextColor());
        context.drawTextWithShadow(textRenderer, Text.literal(status), panel.x + 24, panel.y + panel.height - 32, uploading ? theme.accentColor() : theme.mutedTextColor());
        drawButton(context, panel.x + 24, panel.y + 150, 118, 26, publicPost ? "Public" : "Private", mouseX, mouseY);
        context.drawTextWithShadow(textRenderer, Text.literal(publicPost ? "Visible in feed and contests" : "Only visible for you"),
                panel.x + 154, panel.y + 158, theme.mutedTextColor());
        drawButton(context, panel.x + 24, panel.y + 190, 150, 28, uploading ? "Uploading..." : "Upload WebP", mouseX, mouseY);
        drawButton(context, panel.x + 184, panel.y + 190, 120, 28, "Feed", mouseX, mouseY);
        drawButton(context, panel.x + panel.width - 144, panel.y + 190, 120, 28, "Cancel", mouseX, mouseY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Panel panel = panel();
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (inside(mouseX, mouseY, panel.x + 24, panel.y + 150, 118, 26)) {
            publicPost = !publicPost;
            status = publicPost ? "Public post selected." : "Private post selected.";
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + 24, panel.y + 190, 150, 28)) {
            upload();
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + 184, panel.y + 190, 120, 28)) {
            MinecraftClient.getInstance().setScreen(new ScreenshotFeedScreen(this));
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 144, panel.y + 190, 120, 28)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void upload() {
        if (uploading) {
            return;
        }
        if (!DevModeManager.isEnabled()) {
            status = "Dev Mode fehlt. Nutze /s9c dev.";
            return;
        }
        uploading = true;
        status = "Konvertiere und lade hoch...";
        BackendClient.uploadScreenshot(metadata, captionField.getText(), publicPost ? "public" : "private", DevModeManager.adminSecret(), post -> {
            uploading = false;
            status = "Upload fertig: " + post.postId();
            S9ToastManager.success("Screenshot uploaded", (publicPost ? "Public: " : "Private: ") + post.fileName());
        }, message -> {
            uploading = false;
            status = message;
            S9ToastManager.warning("Upload failed", message);
        });
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

    private void drawButton(DrawContext context, int x, int y, int width, int height, String label, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 0, hovered ? PremiumRender.SHOP_BUTTON_HOVER : PremiumRender.SHOP_BUTTON,
                hovered ? theme.accentColor() : 0x66505C75);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + height / 2 - 4, theme.textColor());
    }

    private Panel panel() {
        ScreenLayout layout = centeredLayout(560, 300, 340, 280);
        return new Panel(layout.x(), layout.y(), layout.width(), layout.height());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private record Panel(int x, int y, int width, int height) {
    }
}
