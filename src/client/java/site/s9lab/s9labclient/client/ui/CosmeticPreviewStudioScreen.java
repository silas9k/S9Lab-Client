package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext.BaseVisibility;
import site.s9lab.s9labclient.client.cosmetics.preview.CosmeticPreviewRenderer;
import site.s9lab.s9labclient.client.cosmetics.preview.PreviewPose;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

/** Dedicated interactive 3D try-on studio, independent from the vanilla inventory preview. */
public final class CosmeticPreviewStudioScreen extends ResponsiveScreen {
    private final net.minecraft.client.gui.screen.Screen parent;
    private final Cosmetic cosmetic;
    private float yaw = 180.0F;
    private float pitch = 8.0F;
    private int zoom = 105;
    private PreviewPose pose = PreviewPose.IDLE;
    private boolean tryOn = true;
    private boolean dragging;

    public CosmeticPreviewStudioScreen(net.minecraft.client.gui.screen.Screen parent, Cosmetic cosmetic) {
        super(Text.literal("Cosmetic Preview Studio"));
        this.parent = parent;
        this.cosmetic = cosmetic;
        reset();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.shopBackdrop(context);
        int margin = Math.max(16, Math.min(42, width / 18));
        int x = margin;
        int y = margin;
        int w = width - margin * 2;
        int h = height - margin * 2;
        PremiumRender.shopPanel(context, x, y, w, h, 52, 58);

        context.drawTextWithShadow(textRenderer, Text.literal("COSMETIC PREVIEW STUDIO"), x + 20, y + 15, 0xFFFFFFFF);
        String name = cosmetic == null ? "No cosmetic selected" : cosmetic.displayName();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, name, w - 180)), x + 20, y + 32, 0xFF9AA1B2);
        button(context, x + w - 42, y + 14, 26, 24, "×", inside(mouseX, mouseY, x + w - 42, y + 14, 26, 24), 0xFFFF6B77);

        int stageX = x + 18;
        int stageY = y + 60;
        int stageW = w - 36;
        int stageH = h - 130;
        PremiumRender.card(context, stageX, stageY, stageW, stageH, 0, 0xFF090D14, 0x66505B6D);
        drawGrid(context, stageX, stageY, stageW, stageH, theme.accentColor());

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            CosmeticPreviewRenderer.draw(
                    context,
                    client.player,
                    cosmetic,
                    tryOn,
                    pose,
                    stageX + stageW / 4,
                    stageY + 12,
                    stageX + stageW * 3 / 4,
                    stageY + stageH - 12,
                    zoom,
                    yaw,
                    pitch,
                    CosmeticPreviewContext.stableKey("studio", cosmetic),
                    BaseVisibility.FULL
            );
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Join a world to render your player"), stageX + stageW / 2, stageY + stageH / 2, 0xFF9AA1B2);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag to rotate  •  Mouse wheel to zoom  •  Double-click to reset"), stageX + stageW / 2, stageY + stageH - 18, 0xFF8E98AA);

        int buttonY = y + h - 43;
        int gap = 8;
        int bw = Math.max(70, Math.min(150, (w - 52 - gap * 3) / 4));
        int total = bw * 4 + gap * 3;
        int bx = x + (w - total) / 2;
        button(context, bx, buttonY, bw, 28, tryOn ? "TRY ON: ON" : "TRY ON: OFF", inside(mouseX, mouseY, bx, buttonY, bw, 28), tryOn ? theme.accentColor() : 0xFF687083);
        bx += bw + gap;
        button(context, bx, buttonY, bw, 28, "POSE: " + pose.label().toUpperCase(), inside(mouseX, mouseY, bx, buttonY, bw, 28), theme.accentColor());
        bx += bw + gap;
        button(context, bx, buttonY, bw, 28, "RESET VIEW", inside(mouseX, mouseY, bx, buttonY, bw, 28), theme.accentColor());
        bx += bw + gap;
        button(context, bx, buttonY, bw, 28, "DONE", inside(mouseX, mouseY, bx, buttonY, bw, 28), 0xFF49F26F);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int margin = Math.max(16, Math.min(42, width / 18));
        int x = margin;
        int y = margin;
        int w = width - margin * 2;
        int h = height - margin * 2;
        if (inside(mx, my, x + w - 42, y + 14, 26, 24)) {
            close();
            return true;
        }
        int buttonY = y + h - 43;
        int gap = 8;
        int bw = Math.max(70, Math.min(150, (w - 52 - gap * 3) / 4));
        int total = bw * 4 + gap * 3;
        int bx = x + (w - total) / 2;
        if (inside(mx, my, bx, buttonY, bw, 28)) {
            tryOn = !tryOn;
            return true;
        }
        bx += bw + gap;
        if (inside(mx, my, bx, buttonY, bw, 28)) {
            pose = pose.next();
            return true;
        }
        bx += bw + gap;
        if (inside(mx, my, bx, buttonY, bw, 28)) {
            reset();
            return true;
        }
        bx += bw + gap;
        if (inside(mx, my, bx, buttonY, bw, 28)) {
            close();
            return true;
        }
        int stageX = x + 18;
        int stageY = y + 60;
        int stageW = w - 36;
        int stageH = h - 130;
        if (inside(mx, my, stageX, stageY, stageW, stageH)) {
            if (doubled) {
                reset();
            } else {
                dragging = true;
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging) {
            yaw += (float) offsetX * 1.5F;
            pitch = Math.max(-65.0F, Math.min(65.0F, pitch - (float) offsetY * 0.9F));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        zoom = Math.max(45, Math.min(180, zoom + (int) Math.round(verticalAmount * 8.0D)));
        return true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void reset() {
        if (cosmetic == null) {
            yaw = 180.0F;
            pitch = 8.0F;
            zoom = 105;
        } else {
            yaw = switch (cosmetic.type()) {
                case CAPE, WINGS -> 0.0F;
                default -> 180.0F;
            };
            pitch = switch (cosmetic.type()) {
                case HAT, HALO, BANDANA -> -6.0F;
                default -> 8.0F;
            };
            zoom = switch (cosmetic.type()) {
                case HAT, HALO, BANDANA -> 125;
                case CAPE, WINGS -> 100;
                default -> 105;
            };
        }
        pose = PreviewPose.IDLE;
    }

    private void drawGrid(DrawContext context, int x, int y, int w, int h, int accent) {
        for (int gx = x + 20; gx < x + w; gx += 24) {
            context.fill(gx, y + 1, gx + 1, y + h - 1, 0x182F65C8);
        }
        for (int gy = y + 20; gy < y + h; gy += 24) {
            context.fill(x + 1, gy, x + w - 1, gy + 1, 0x182F65C8);
        }
        context.fill(x + w / 2, y + 1, x + w / 2 + 1, y + h - 1, ClientTheme.withAlpha(accent, 80));
    }

    private void button(DrawContext context, int x, int y, int w, int h, String label, boolean hovered, int accent) {
        PremiumRender.card(context, x, y, w, h, 0, hovered ? 0xFF243149 : 0xFF151C29, hovered ? accent : 0x66505B6D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, w - 10)), x + w / 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFD7DFED);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
