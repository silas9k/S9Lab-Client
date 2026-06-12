package site.s9lab.s9labclient.client.module.impl.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.music.MusicInfo;
import site.s9lab.s9labclient.client.music.MusicManager;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class MusicHudElement extends HudModule {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 62;
    private static final int COVER = 42;
    private final ModeSetting style;
    private final BooleanSetting blurBackground;

    public MusicHudElement() {
        super("Music Display", "Shows a Spotify-like music widget with cover, progress and player source.", true);
        this.style = addSetting(new ModeSetting("Style", "Card", "Card", "Glass", "Minimal"));
        this.blurBackground = addSetting(new BooleanSetting("Blur", true));
    }

    @Override
    protected int defaultY() {
        return 76;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        MusicInfo music = MusicManager.current();
        int x = getX();
        int y = getY();
        int accent = ThemeManager.theme().accentColor();

        if ("Minimal".equals(style.getValue())) {
            renderMinimal(context, client, music, x, y, accent);
            return;
        }

        int panel = "Glass".equals(style.getValue()) ? (blurBackground.getValue() ? 0x88101524 : 0xBB101524) : 0xE111141D;
        int border = "Glass".equals(style.getValue()) ? 0x6688A8FF : 0x884C7DFF;
        PremiumRender.card(context, x, y, WIDTH, HEIGHT, 9, panel, border);
        if ("Glass".equals(style.getValue())) {
            context.fill(x + 3, y + 3, x + WIDTH - 3, y + 4, 0x33FFFFFF);
        }

        renderCover(context, x + 10, y + 10, accent, music.playing());
        int textX = x + 60;
        renderScrollingText(context, client, music.title(), textX, y + 10, 102, 0xFFFFFFFF);
        context.drawText(client.textRenderer, trim(music.artist(), client, 102), textX, y + 24, 0xFFB9C4DA, true);
        context.drawText(client.textRenderer, music.formattedPosition(), textX, y + 39, 0xFF8F98AC, true);
        String duration = music.formattedDuration();
        context.drawText(client.textRenderer, duration, x + WIDTH - 10 - client.textRenderer.getWidth(duration), y + 39, 0xFF8F98AC, true);
        renderProgress(context, textX, y + 52, WIDTH - 70, music.progress(), accent);
        renderPlayPause(context, x + 40, y + 39, music.playing());
    }

    @Override
    public int getWidth(MinecraftClient client) {
        return "Minimal".equals(style.getValue()) ? Math.min(180, client.textRenderer.getWidth(MusicManager.current().title())) : WIDTH;
    }

    @Override
    public int getHeight(MinecraftClient client) {
        return "Minimal".equals(style.getValue()) ? 14 : HEIGHT;
    }

    private void renderMinimal(DrawContext context, MinecraftClient client, MusicInfo music, int x, int y, int accent) {
        if (hasBackground()) {
            drawBackground(context, client);
        }
        String text = music.title() + " - " + music.artist() + " " + music.formattedPosition() + "/" + music.formattedDuration();
        context.drawText(client.textRenderer, trim(text, client, 180), x, y, 0xFFFFFFFF, true);
        renderProgress(context, x, y + 10, Math.min(180, Math.max(70, client.textRenderer.getWidth(text))), music.progress(), accent);
    }

    private void renderCover(DrawContext context, int x, int y, int accent, boolean playing) {
        PremiumRender.card(context, x, y, COVER, COVER, 7, 0xFF080A10, 0x554C7DFF);
        PremiumRender.roundedRect(context, x + 5, y + 5, COVER - 10, COVER - 10, 6, 0xFF151B2A);
        PremiumRender.roundedRect(context, x + 11, y + 11, COVER - 22, COVER - 22, 10, accent);
        if (playing) {
            context.fill(x + 20, y + 17, x + 24, y + 29, 0xDD000000);
            context.fill(x + 27, y + 17, x + 31, y + 29, 0xDD000000);
        } else {
            context.fill(x + 20, y + 16, x + 23, y + 30, 0xDD000000);
            context.fill(x + 23, y + 19, x + 30, y + 27, 0xDD000000);
            context.fill(x + 30, y + 22, x + 33, y + 24, 0xDD000000);
        }
    }

    private void renderPlayPause(DrawContext context, int x, int y, boolean playing) {
        int color = 0xCCFFFFFF;
        if (playing) {
            context.fill(x, y, x + 3, y + 9, color);
            context.fill(x + 5, y, x + 8, y + 9, color);
        } else {
            context.fill(x, y, x + 3, y + 9, color);
            context.fill(x + 3, y + 2, x + 6, y + 7, color);
            context.fill(x + 6, y + 4, x + 9, y + 5, color);
        }
    }

    private void renderProgress(DrawContext context, int x, int y, int width, double progress, int accent) {
        PremiumRender.roundedRect(context, x, y, width, 4, 2, 0x66313A4F);
        int filled = Math.max(2, (int) Math.round(width * progress));
        PremiumRender.roundedRect(context, x, y, filled, 4, 2, accent);
    }

    private void renderScrollingText(DrawContext context, MinecraftClient client, String text, int x, int y, int maxWidth, int color) {
        int textWidth = client.textRenderer.getWidth(text);
        if (textWidth <= maxWidth) {
            context.drawText(client.textRenderer, text, x, y, color, true);
            return;
        }

        int overflow = textWidth - maxWidth + 18;
        int offset = (int) ((System.currentTimeMillis() / 45L) % (overflow * 2L));
        if (offset > overflow) {
            offset = overflow * 2 - offset;
        }
        context.enableScissor(x, y - 1, x + maxWidth, y + 10);
        context.drawText(client.textRenderer, text, x - offset, y, color, true);
        context.disableScissor();
    }

    private static String trim(String text, MinecraftClient client, int maxWidth) {
        if (client.textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String trimmed = text;
        while (trimmed.length() > 3 && client.textRenderer.getWidth(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }
}
