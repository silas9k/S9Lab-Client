package site.s9lab.s9labclient.client.ui;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendClient.ScreenshotPost;
import site.s9lab.s9labclient.client.dev.DevModeManager;
import site.s9lab.s9labclient.client.notification.S9ToastManager;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ScreenshotFeedScreen extends ResponsiveScreen {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM. HH:mm").withZone(ZoneId.systemDefault());
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    private final Screen parent;
    private final Map<String, FeedTexture> textures = new HashMap<>();
    private List<ScreenshotPost> posts = List.of();
    private TextFieldWidget commentField;
    private String selectedPostId = "";
    private String status = "Loading feed...";
    private FeedMode mode = FeedMode.FEED;
    private int scroll;
    private boolean loading;

    public ScreenshotFeedScreen(Screen parent) {
        super(Text.literal("S9Lab Screenshot Feed"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Panel panel = panel();
        commentField = new TextFieldWidget(textRenderer, panel.x + 18, panel.y + panel.height - 28, Math.max(120, panel.width - 150), 20, Text.literal("Comment"));
        commentField.setMaxLength(120);
        commentField.setPlaceholder(Text.literal("Select a post, write comment..."));
        addDrawableChild(commentField);
        refresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.shopBackdrop(context);
        Panel panel = panel();
        PremiumRender.shopPanel(context, panel.x, panel.y, panel.width, panel.height, 58, 34);
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB MOMENTS"), panel.x + 18, panel.y + 16, theme.textColor());
        context.drawTextWithShadow(textRenderer, Text.literal(mode.title + "  |  " + status), panel.x + 18, panel.y + 36, theme.mutedTextColor());
        int tabX = panel.x + 190;
        for (FeedMode feedMode : FeedMode.values()) {
            drawSmallButton(context, tabX, panel.y + 16, feedMode.width, 24, feedMode.label, mode == feedMode, mouseX, mouseY);
            tabX += feedMode.width + 6;
        }
        drawButton(context, panel.x + panel.width - 168, panel.y + 16, 72, 24, "Refresh", mouseX, mouseY);
        drawButton(context, panel.x + panel.width - 86, panel.y + 16, 68, 24, "Close", mouseX, mouseY);

        int contentX = panel.x + 18;
        int contentY = panel.y + 72 - scroll;
        int contentW = panel.width - 36;
        int columns = contentW >= 920 ? 2 : 1;
        int gap = 12;
        int cardW = (contentW - gap * (columns - 1)) / columns;
        cardW = Math.max(280, Math.min(520, cardW));
        int x = contentX + Math.max(0, (contentW - (cardW * columns + gap * (columns - 1))) / 2);
        int y = contentY;
        int column = 0;

        if (posts.isEmpty() && !loading) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Noch keine Moments gefunden."), panel.x + panel.width / 2, panel.y + 132, theme.mutedTextColor());
        }

        context.enableScissor(panel.x + 8, panel.y + 62, panel.x + panel.width - 8, panel.y + panel.height - 42);
        for (ScreenshotPost post : posts) {
            int cardH = Math.max(230, Math.min(330, panel.height - 120));
            drawPost(context, post, x + column * (cardW + gap), y, cardW, cardH, mouseX, mouseY);
            column++;
            if (column >= columns) {
                column = 0;
                y += cardH + 14;
            }
        }
        context.disableScissor();
        drawSmallButton(context, panel.x + panel.width - 122, panel.y + panel.height - 29, 104, 22, "Post Comment", false, mouseX, mouseY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void drawPost(DrawContext context, ScreenshotPost post, int x, int y, int width, int height, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean plusFrame = "s9lab_plus".equalsIgnoreCase(post.frame());
        int animated = plusFrame ? animatedFrameColor() : 0x667C8AA8;
        PremiumRender.card(context, x, y, width, height, 0, 0xCC10141D, animated);
        if (plusFrame) {
            PremiumRender.card(context, x + 2, y + 2, width - 4, height - 4, 0, 0x0010141D, animatedFrameColor((System.currentTimeMillis() + 500L) % 1800L));
        }
        context.drawTextWithShadow(textRenderer, Text.literal(post.uploaderName() == null || post.uploaderName().isBlank() ? "S9Lab User" : post.uploaderName()), x + 12, y + 10, theme.textColor());
        context.drawTextWithShadow(textRenderer, Text.literal(DATE_FORMAT.format(Instant.ofEpochMilli(post.uploadedAt()))), x + width - 12 - textRenderer.getWidth(DATE_FORMAT.format(Instant.ofEpochMilli(post.uploadedAt()))), y + 10, theme.mutedTextColor());
        if ("private".equalsIgnoreCase(post.visibility())) {
            context.drawTextWithShadow(textRenderer, Text.literal("PRIVATE"), x + 12, y + 24, 0xFFFFB547);
        } else if (plusFrame) {
            context.drawTextWithShadow(textRenderer, Text.literal("S9LAB+ FRAME"), x + 12, y + 24, animated);
        }
        int imageX = x + 12;
        int imageY = y + 38;
        int imageW = width - 24;
        int imageH = height - 126;
        PremiumRender.card(context, imageX, imageY, imageW, imageH, 0, 0xFF070A10, 0x55455770);
        FeedTexture texture = texture(post);
        if (texture.ready()) {
            double scale = Math.min(imageW / (double) texture.width, imageH / (double) texture.height);
            int drawW = Math.max(1, (int) Math.round(texture.width * scale));
            int drawH = Math.max(1, (int) Math.round(texture.height * scale));
            int drawX = imageX + (imageW - drawW) / 2;
            int drawY = imageY + (imageH - drawH) / 2;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture.id, drawX, drawY, 0, 0, drawW, drawH, texture.width, texture.height, texture.width, texture.height);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(texture.failed ? "Image unavailable" : "Loading preview..."), imageX + imageW / 2, imageY + imageH / 2 - 5, theme.mutedTextColor());
        }
        String caption = post.caption() == null || post.caption().isBlank() ? post.server() : post.caption();
        context.drawTextWithShadow(textRenderer, Text.literal(trim(caption, Math.max(20, width / 6))), x + 12, y + height - 82, theme.textColor());
        String meta = post.server() + " | Score " + post.score();
        context.drawTextWithShadow(textRenderer, Text.literal(trim(meta, Math.max(20, width / 6))), x + 12, y + height - 68, theme.mutedTextColor());
        if (post.comments() != null && !post.comments().isEmpty()) {
            BackendClient.ScreenshotComment comment = post.comments().get(0);
            context.drawTextWithShadow(textRenderer, Text.literal(trim(comment.name() + ": " + comment.message(), Math.max(18, width / 6))),
                    x + 12, y + height - 54, 0xFFE8EBF4);
        }
        int likeX = x + 12;
        int buttonY = y + height - 42;
        drawSmallButton(context, likeX, buttonY, 64, 22, "Like " + post.likes(), "like".equals(post.viewerReaction()), mouseX, mouseY);
        drawSmallButton(context, likeX + 72, buttonY, 78, 22, "Dislike " + post.dislikes(), "dislike".equals(post.viewerReaction()), mouseX, mouseY);
        if (DevModeManager.isEnabled()) {
            drawSmallButton(context, likeX + 158, buttonY, 58, 22, "Delete", false, mouseX, mouseY);
        }
        drawSmallButton(context, x + width - 92, buttonY, 80, 22, selectedPostId.equals(post.postId()) ? "Selected" : "Comment", selectedPostId.equals(post.postId()), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Panel panel = panel();
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int tabX = panel.x + 190;
        for (FeedMode feedMode : FeedMode.values()) {
            if (inside(mouseX, mouseY, tabX, panel.y + 16, feedMode.width, 24)) {
                mode = feedMode;
                scroll = 0;
                refresh();
                return true;
            }
            tabX += feedMode.width + 6;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 168, panel.y + 16, 72, 24)) {
            refresh();
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 86, panel.y + 16, 68, 24)) {
            close();
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 122, panel.y + panel.height - 29, 104, 22)) {
            ScreenshotPost selected = posts.stream().filter(post -> post.postId().equals(selectedPostId)).findFirst().orElse(null);
            if (selected == null) {
                S9ToastManager.warning("Comment", "Erst einen Screenshot auswaehlen.");
                return true;
            }
            comment(selected, commentField == null ? "" : commentField.getText());
            return true;
        }
        int contentW = panel.width - 36;
        int columns = contentW >= 920 ? 2 : 1;
        int gap = 12;
        int cardW = Math.max(280, Math.min(520, (contentW - gap * (columns - 1)) / columns));
        int x = panel.x + 18 + Math.max(0, (contentW - (cardW * columns + gap * (columns - 1))) / 2);
        int y = panel.y + 72 - scroll;
        int column = 0;
        for (ScreenshotPost post : posts) {
            int drawX = x + column * (cardW + gap);
            int cardH = Math.max(230, Math.min(330, panel.height - 120));
            int buttonY = y + cardH - 42;
            if (inside(mouseX, mouseY, drawX + 12, buttonY, 64, 22)) {
                react(post, "like");
                return true;
            }
            if (inside(mouseX, mouseY, drawX + 84, buttonY, 78, 22)) {
                react(post, "dislike");
                return true;
            }
            if (DevModeManager.isEnabled() && inside(mouseX, mouseY, drawX + 170, buttonY, 58, 22)) {
                delete(post);
                return true;
            }
            if (inside(mouseX, mouseY, drawX + cardW - 92, buttonY, 80, 22)) {
                selectedPostId = post.postId();
                status = "Kommentiere " + trim(post.fileName(), 32);
                return true;
            }
            column++;
            if (column >= columns) {
                column = 0;
                y += cardH + 14;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int columns = Math.max(1, (panel().width - 36) >= 920 ? 2 : 1);
        int rows = (posts.size() + columns - 1) / columns;
        int max = Math.max(0, rows * 344 - Math.max(220, panel().height - 120));
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.round(verticalAmount * 28)));
        return true;
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

    private void refresh() {
        loading = true;
        status = "Loading feed...";
        BackendClient.fetchScreenshotFeed(mode.apiMode, result -> {
            loading = false;
            posts = result;
            status = result.size() + (mode == FeedMode.FEED ? " posts" : " ranked posts");
            scroll = Math.min(scroll, Math.max(0, result.size() * 344));
        }, message -> {
            loading = false;
            status = message;
        });
    }

    private void react(ScreenshotPost post, String reaction) {
        BackendClient.reactScreenshot(post.postId(), reaction, result -> {
            posts = result;
            S9ToastManager.success("Reaction saved", reaction);
        }, message -> S9ToastManager.warning("Reaction failed", message));
    }

    private void comment(ScreenshotPost post, String message) {
        if (message == null || message.trim().isBlank()) {
            S9ToastManager.warning("Comment", "Text eingeben.");
            return;
        }
        BackendClient.commentScreenshot(post.postId(), message, result -> {
            posts = result;
            if (commentField != null) {
                commentField.setText("");
            }
            S9ToastManager.success("Comment posted", post.fileName());
        }, error -> S9ToastManager.warning("Comment failed", error));
    }

    private void delete(ScreenshotPost post) {
        if (!DevModeManager.isEnabled()) {
            S9ToastManager.warning("Dev mode", "Bitte zuerst /s9c dev verifizieren.");
            return;
        }
        BackendClient.deleteScreenshot(post.postId(), DevModeManager.adminSecret(), result -> {
            posts = result;
            textures.remove(post.postId());
            if (selectedPostId.equals(post.postId())) {
                selectedPostId = "";
            }
            S9ToastManager.success("Post deleted", trim(post.fileName(), 48));
        }, error -> S9ToastManager.warning("Delete failed", error));
    }

    private FeedTexture texture(ScreenshotPost post) {
        FeedTexture texture = textures.computeIfAbsent(post.postId(), ignored -> new FeedTexture());
        if (!texture.loading && !texture.ready() && !texture.failed) {
            texture.loading = true;
            CompletableFuture.runAsync(() -> loadTexture(post, texture));
        }
        return texture;
    }

    private void loadTexture(ScreenshotPost post, FeedTexture target) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BackendClient.screenshotImageUrl(post.imageUrl())))
                    .timeout(java.time.Duration.ofSeconds(8))
                    .GET()
                    .build();
            byte[] bytes = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
            MinecraftClient.getInstance().execute(() -> {
                target.width = image.getWidth();
                target.height = image.getHeight();
                target.id = Identifier.of("s9labclient", "feed_" + post.postId());
                target.texture = new NativeImageBackedTexture(() -> "s9lab feed " + post.postId(), image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(target.id, target.texture);
                target.loading = false;
            });
        } catch (Exception exception) {
            S9LabClient.LOGGER.debug("Failed to load screenshot feed image {}", post.imageUrl(), exception);
            target.failed = true;
            target.loading = false;
        }
    }

    private void drawButton(DrawContext context, int x, int y, int width, int height, String label, int mouseX, int mouseY) {
        drawSmallButton(context, x, y, width, height, label, false, mouseX, mouseY);
    }

    private void drawSmallButton(DrawContext context, int x, int y, int width, int height, String label, boolean active, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        int fill = active ? 0xCC2F5DCC : hovered ? PremiumRender.SHOP_BUTTON_HOVER : PremiumRender.SHOP_BUTTON;
        int border = active || hovered ? theme.accentColor() : 0x66505C75;
        PremiumRender.card(context, x, y, width, height, 0, fill, border);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + height / 2 - 4, theme.textColor());
    }

    private Panel panel() {
        ScreenLayout layout = centeredLayout(680, 420, 320, 260);
        return new Panel(layout.x(), layout.y(), layout.width(), layout.height());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static int animatedFrameColor() {
        return animatedFrameColor(System.currentTimeMillis() % 1800L);
    }

    private static int animatedFrameColor(long time) {
        float hue = (time % 1800L) / 1800.0F;
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.65F, 1.0F) & 0x00FFFFFF;
    }

    private enum FeedMode {
        FEED("Feed", "Latest public and private posts", "feed", 48),
        WEEK("Week", "Weekly top screenshots", "week", 50),
        MONTH("Month", "Monthly top screenshots", "month", 58),
        YEAR("Year", "Yearly top screenshots", "year", 50);

        private final String label;
        private final String title;
        private final String apiMode;
        private final int width;

        FeedMode(String label, String title, String apiMode, int width) {
            this.label = label;
            this.title = title;
            this.apiMode = apiMode;
            this.width = width;
        }
    }

    private record Panel(int x, int y, int width, int height) {
    }

    private static final class FeedTexture {
        private Identifier id;
        private NativeImageBackedTexture texture;
        private int width;
        private int height;
        private boolean loading;
        private boolean failed;

        private boolean ready() {
            return id != null && texture != null && width > 0 && height > 0;
        }
    }
}
