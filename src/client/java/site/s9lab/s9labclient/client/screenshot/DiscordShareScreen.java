package site.s9lab.s9labclient.client.screenshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

/**
 * In-game Discord share screen.
 *
 * Flow:
 *  1. User sees their saved webhook targets (DMs / channels)
 *  2. User picks one (or adds a new one by pasting a webhook URL + giving it a label)
 *  3. Optionally types a caption
 *  4. Hits "Send" → multipart upload via {@link ScreenshotManager#uploadToDiscordWebhook}
 *
 * Webhooks are stored in .minecraft/s9lab_discord_targets.json so the list
 * survives relaunches.  A webhook URL looks like:
 *   https://discord.com/api/webhooks/<id>/<token>
 *
 * Users create them in:  Server/Channel Settings → Integrations → Webhooks
 * or for DMs: a bot must create them — we guide users in the UI.
 */
public class DiscordShareScreen extends Screen {

    // ─── Persistent webhook targets ───────────────────────────────────────────

    public record WebhookTarget(String label, String url) {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TARGET_LIST_TYPE = new TypeToken<List<WebhookTarget>>() {}.getType();
    private static List<WebhookTarget> savedTargets = new ArrayList<>();
    private static Path targetsPath;

    public static void initStorage(MinecraftClient client) {
        targetsPath = client.runDirectory.toPath().resolve("s9lab_discord_targets.json");
        loadTargets();
    }

    private static void loadTargets() {
        savedTargets = new ArrayList<>();
        if (targetsPath == null || !Files.exists(targetsPath)) return;
        try (Reader r = Files.newBufferedReader(targetsPath)) {
            List<WebhookTarget> loaded = GSON.fromJson(r, TARGET_LIST_TYPE);
            if (loaded != null) savedTargets.addAll(loaded);
        } catch (IOException e) {
            S9LabClient.LOGGER.warn("Failed to load Discord targets.", e);
        }
    }

    private static void saveTargets() {
        if (targetsPath == null) return;
        try {
            Files.createDirectories(targetsPath.getParent());
            try (Writer w = Files.newBufferedWriter(targetsPath)) {
                GSON.toJson(savedTargets, TARGET_LIST_TYPE, w);
            }
        } catch (IOException e) {
            S9LabClient.LOGGER.warn("Failed to save Discord targets.", e);
        }
    }

    // ─── Screen state ─────────────────────────────────────────────────────────

    private enum View { TARGET_LIST, ADD_TARGET }

    private final Screen parent;
    private final ScreenshotMetadata metadata;

    private View currentView = View.TARGET_LIST;
    private int selectedIndex = -1;

    // widgets
    private TextFieldWidget captionField;
    private TextFieldWidget newLabelField;
    private TextFieldWidget newUrlField;
    private ButtonWidget sendButton;

    // screenshot preview texture
    private Identifier previewTextureId;
    private NativeImageBackedTexture previewTexture;
    private int previewTextureWidth;
    private int previewTextureHeight;
    private boolean previewLoadAttempted;

    // upload state
    private boolean uploading   = false;
    private boolean uploadDone  = false;
    private boolean uploadOk    = false;
    // scroll
    private int scrollOffset = 0;
    private static final int ROW_H     = 28;
    private static final int LIST_ROWS = 4;

    public DiscordShareScreen(Screen parent, ScreenshotMetadata metadata) {
        super(Text.literal("Share on Discord"));
        this.parent   = parent;
        this.metadata = metadata;
        loadTargets(); // always refresh
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        loadPreviewTexture();
        rebuildWidgets();
    }

    private int panelWidth() {
        return Math.min(420, width - 40);
    }

    private int panelHeight() {
        return Math.min(340, height - 24);
    }

    private void rebuildWidgets() {
        clearChildren();

        int cx = width / 2;
        int cy = height / 2;
        int panelW = panelWidth();
        int panelH = panelHeight();
        int px = cx - panelW / 2;
        int py = cy - panelH / 2;

        if (currentView == View.TARGET_LIST) {
            buildListView(px, py, panelW, panelH);
        } else {
            buildAddView(px, py, panelW, panelH);
        }
    }

    private void buildListView(int px, int py, int panelW, int panelH) {
        int cx = px + panelW / 2;

        // Caption
        captionField = new TextFieldWidget(textRenderer, px + 8, py + 136, panelW - 16, 18,
                Text.literal("Caption (optional)"));
        captionField.setMaxLength(200);
        captionField.setPlaceholder(Text.literal("Add a caption…"));
        addSelectableChild(captionField);

        // "+ Add target" button
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Add"), btn -> {
            currentView = View.ADD_TARGET;
            rebuildWidgets();
        }).dimensions(px + panelW - 64, py + 162, 56, 16).build());

        // Send button
        sendButton = ButtonWidget.builder(Text.literal("Send"), btn -> doUpload())
                .dimensions(cx - 80, py + panelH - 30, 76, 20).build();
        sendButton.active = selectedIndex >= 0 && selectedIndex < savedTargets.size() && !uploading;
        addDrawableChild(sendButton);

        // Cancel
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> close())
                .dimensions(cx + 4, py + panelH - 30, 76, 20).build());
    }

    private void buildAddView(int px, int py, int panelW, int panelH) {
        int cx = px + panelW / 2;

        newLabelField = new TextFieldWidget(textRenderer, px + 8, py + 50, panelW - 16, 18,
                Text.literal("Label"));
        newLabelField.setMaxLength(32);
        newLabelField.setPlaceholder(Text.literal("e.g. my-server-screenshots"));
        addSelectableChild(newLabelField);

        newUrlField = new TextFieldWidget(textRenderer, px + 8, py + 90, panelW - 16, 18,
                Text.literal("Webhook URL"));
        newUrlField.setMaxLength(256);
        newUrlField.setPlaceholder(Text.literal("https://discord.com/api/webhooks/…"));
        addSelectableChild(newUrlField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), btn -> {
            String label = newLabelField.getText().trim();
            String url   = newUrlField.getText().trim();
            if (!label.isEmpty() && url.startsWith("https://discord.com/api/webhooks/")) {
                savedTargets.add(new WebhookTarget(label, url));
                saveTargets();
                currentView = View.TARGET_LIST;
                selectedIndex = savedTargets.size() - 1;
                rebuildWidgets();
            }
        }).dimensions(cx - 80, py + panelH - 30, 76, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> {
            currentView = View.TARGET_LIST;
            rebuildWidgets();
        }).dimensions(cx + 4, py + panelH - 30, 76, 20).build());
    }

    // ─── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Minecraft 1.21.11: do not call renderBackground() here.
        // It can trigger a second blur in the same frame via Fabric Screen API.
        context.fill(0, 0, width, height, 0x99000000);

        int cx = width / 2;
        int cy = height / 2;
        int panelW = panelWidth();
        int panelH = panelHeight();
        int px = cx - panelW / 2;
        int py = cy - panelH / 2;

        // Panel background — dark slate with subtle purple tint (Discord brand nod)
        context.fill(px, py, px + panelW, py + panelH, 0xF0161B25);
        // Top accent bar (Discord blurple)
        context.fill(px, py, px + panelW, py + 4, 0xFF5865F2);
        // Border
        drawBorder(context, px, py, panelW, panelH, 0xFF5865F2);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§9§l⬡ §r§bShare on Discord"),
                cx, py + 10, 0xFFFFFF);

        if (currentView == View.TARGET_LIST) {
            renderListView(context, mouseX, mouseY, delta, px, py, panelW, panelH);
        } else {
            renderAddView(context, mouseX, mouseY, delta, px, py, panelW, panelH);
        }

        // Feedback overlay
        if (uploadDone) {
            String msg = uploadOk ? "§a✔ Sent!" : "§c✗ Failed";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(msg), cx, py + panelH / 2, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderListView(DrawContext ctx, int mx, int my, float delta,
                                 int px, int py, int panelW, int panelH) {
        // Screenshot preview
        renderPreview(ctx, px + 8, py + 30, panelW - 16, 88);

        // Caption label
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Caption"), px + 8, py + 126, 0xFFFFFF);
        captionField.render(ctx, mx, my, delta);

        // "Send to" label + add-button row
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Send to"), px + 8, py + 163, 0xFFFFFF);

        // Webhook list
        int listTop = py + 178;
        int listH   = ROW_H * LIST_ROWS;
        ctx.enableScissor(px + 4, listTop, px + panelW - 4, listTop + listH);

        for (int i = 0; i < savedTargets.size(); i++) {
            int ry = listTop + (i - scrollOffset) * ROW_H;
            if (ry + ROW_H < listTop || ry > listTop + listH) continue;

            WebhookTarget t = savedTargets.get(i);
            boolean hovered  = mx >= px + 4 && mx <= px + panelW - 68 && my >= ry && my <= ry + ROW_H - 2;
            boolean selected = i == selectedIndex;

            int rowBg = selected ? 0xFF3D3F8F : (hovered ? 0xFF2A2D3E : 0xFF1E2130);
            ctx.fill(px + 4, ry, px + panelW - 68, ry + ROW_H - 2, rowBg);
            if (selected) drawBorder(ctx, px + 4, ry, panelW - 72, ROW_H - 2, 0xFF5865F2);

            // Discord icon placeholder (blurple square)
            ctx.fill(px + 10, ry + 7, px + 22, ry + 19, 0xFF5865F2);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§b" + t.label()), px + 26, ry + 5, 0xFFFFFF);
            String urlShort = t.url().replaceFirst("https://discord.com/api/webhooks/", "#");
            if (urlShort.length() > 30) urlShort = urlShort.substring(0, 28) + "…";
            ctx.drawTextWithShadow(textRenderer, Text.literal("§8" + urlShort), px + 26, ry + 15, 0xFFFFFF);

            // Delete button per row
            int delX = px + panelW - 64;
            boolean delHov = mx >= delX && mx <= delX + 18 && my >= ry + 4 && my <= ry + ROW_H - 6;
            ctx.fill(delX, ry + 4, delX + 18, ry + ROW_H - 6, delHov ? 0xFFAA2222 : 0xFF662222);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§c✗"), delX + 9, ry + 10, 0xFFFFFF);
        }

        ctx.disableScissor();

        if (savedTargets.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§8No targets yet — click §7+ Add§8 to add one"),
                    px + panelW / 2, listTop + 20, 0xFFFFFF);
        }

        // Loading spinner or send status
        if (uploading) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§eUploading…"), px + panelW / 2, py + panelH - 40, 0xFFFFFF);
        }

        // Update send button
        if (sendButton != null) {
            sendButton.active = selectedIndex >= 0 && selectedIndex < savedTargets.size() && !uploading;
        }
    }

    private void renderAddView(DrawContext ctx, int mx, int my, float delta,
                                int px, int py, int panelW, int panelH) {
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Label"), px + 8, py + 41, 0xFFFFFF);
        newLabelField.render(ctx, mx, my, delta);

        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Webhook URL"), px + 8, py + 81, 0xFFFFFF);
        newUrlField.render(ctx, mx, my, delta);

        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§8Channel Settings → Integrations → Webhooks"),
                px + 8, py + 116, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§8to create a webhook for a server channel."),
                px + 8, py + 126, 0xFFFFFF);

        // Validate URL live
        if (newUrlField != null && !newUrlField.getText().isBlank()) {
            boolean valid = newUrlField.getText().trim().startsWith("https://discord.com/api/webhooks/");
            String hint = valid ? "§a✔ valid URL" : "§c✗ must start with discord.com/api/webhooks/";
            ctx.drawTextWithShadow(textRenderer, Text.literal(hint), px + 8, py + 145, 0xFFFFFF);
        }
    }

    private void loadPreviewTexture() {
        if (previewLoadAttempted) return;
        previewLoadAttempted = true;

        if (metadata == null || metadata.path() == null || !Files.exists(metadata.path())) {
            return;
        }

        try (InputStream in = Files.newInputStream(metadata.path())) {
            NativeImage image = NativeImage.read(in);
            previewTextureWidth = image.getWidth();
            previewTextureHeight = image.getHeight();

            previewTexture = new NativeImageBackedTexture(
                    () -> "s9lab screenshot preview " + metadata.fileName(), image);
            previewTextureId = Identifier.of(
                    "s9labclient",
                    "screenshot_preview_" + Math.abs(metadata.absolutePath().hashCode())
            );
            MinecraftClient.getInstance()
                    .getTextureManager()
                    .registerTexture(previewTextureId, previewTexture);
        } catch (Exception e) {
            S9LabClient.LOGGER.warn("Failed to load screenshot preview: {}", metadata.absolutePath(), e);
            previewTextureId = null;
            previewTexture = null;
            previewTextureWidth = 0;
            previewTextureHeight = 0;
        }
    }

    private void renderPreview(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF0B0E16);
        drawBorder(ctx, x, y, w, h, 0xFF2E3548);

        if (previewTextureId == null || previewTextureWidth <= 0 || previewTextureHeight <= 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§8Preview not available"),
                    x + w / 2, y + h / 2 - 4, 0xFFFFFF);
            return;
        }

        int innerW = Math.max(1, w - 6);
        int innerH = Math.max(1, h - 6);
        double scale = Math.min(innerW / (double) previewTextureWidth, innerH / (double) previewTextureHeight);
        int drawW = Math.max(1, (int) Math.round(previewTextureWidth * scale));
        int drawH = Math.max(1, (int) Math.round(previewTextureHeight * scale));
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;

        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, previewTextureId,
                drawX, drawY,
                0.0F, 0.0F,
                drawW, drawH,
                previewTextureWidth, previewTextureHeight,
                previewTextureWidth, previewTextureHeight);
    }

    // ─── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        if (currentView == View.TARGET_LIST) {
            int cx = width / 2;
            int panelW = panelWidth();
            int panelH = panelHeight();
            int px = cx - panelW / 2;
            int py = height / 2 - panelH / 2;
            int listTop = py + 178;

            for (int i = 0; i < savedTargets.size(); i++) {
                int ry = listTop + (i - scrollOffset) * ROW_H;

                // delete button click
                int delX = px + panelW - 64;
                if (mx >= delX && mx <= delX + 18 && my >= ry + 4 && my <= ry + ROW_H - 6) {
                    savedTargets.remove(i);
                    saveTargets();
                    if (selectedIndex >= savedTargets.size()) selectedIndex = savedTargets.size() - 1;
                    rebuildWidgets();
                    return true;
                }

                // row click = select
                if (mx >= px + 4 && mx <= px + panelW - 68 && my >= ry && my <= ry + ROW_H - 2) {
                    selectedIndex = i;
                    if (sendButton != null) sendButton.active = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hx, double vy) {
        if (currentView == View.TARGET_LIST) {
            scrollOffset = Math.max(0, Math.min(
                    Math.max(0, savedTargets.size() - LIST_ROWS),
                    scrollOffset - (int) Math.signum(vy)));
            return true;
        }
        return super.mouseScrolled(mx, my, hx, vy);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    // ─── Upload logic ─────────────────────────────────────────────────────────

    private void doUpload() {
        if (selectedIndex < 0 || selectedIndex >= savedTargets.size()) return;
        WebhookTarget target = savedTargets.get(selectedIndex);
        String caption = captionField != null ? captionField.getText().trim() : "";

        uploading  = true;
        uploadDone = false;
        if (sendButton != null) sendButton.active = false;

        ScreenshotManager.uploadToDiscordWebhook(target.url(), metadata, caption, success -> {
            uploading    = false;
            uploadDone   = true;
            uploadOk     = success;
            if (success) {
                // Show success for 2s then close
                MinecraftClient.getInstance().execute(() -> {
                    // schedule close after 2 s (40 ticks at 20 TPS)
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        MinecraftClient.getInstance().execute(this::close);
                    }, "s9lab-discord-close").start();
                });
            } else {
                if (sendButton != null) sendButton.active = true;
            }
        });
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ─── Draw helpers ─────────────────────────────────────────────────────────

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,         y,         x + w, y + 1,     color); // top
        ctx.fill(x,         y + h - 1, x + w, y + h,     color); // bottom
        ctx.fill(x,         y,         x + 1, y + h,     color); // left
        ctx.fill(x + w - 1, y,         x + w, y + h,     color); // right
    }
}
