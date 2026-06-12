package site.s9lab.s9labclient.client.module.impl.hud;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.util.ColorUtil;
import site.s9lab.s9labclient.client.util.RenderUtil;

public class ClockHudElement extends HudModule {
    private static final DateTimeFormatter TIME_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
    private static final DateTimeFormatter TIME_MINUTES = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);
    private static final int PADDING_X = 7;
    private static final int PADDING_Y = 5;
    private final ClockSettings clockSettings;

    public ClockHudElement() {
        super("Clock", "Shows your local or Berlin time as a draggable HUD widget.", true);
        this.clockSettings = new ClockSettings(
                addSetting(new ModeSetting("Style", ClockSettings.STYLE_GLASS, ClockSettings.STYLE_MINIMAL, ClockSettings.STYLE_CARD, ClockSettings.STYLE_GLASS)),
                addSetting(new ModeSetting("Timezone", ClockSettings.TIMEZONE_SYSTEM, ClockSettings.TIMEZONE_SYSTEM, ClockSettings.TIMEZONE_BERLIN)),
                addSetting(new BooleanSetting("Date", true)),
                addSetting(new BooleanSetting("Seconds", true))
        );
    }

    @Override
    protected int defaultY() {
        return 42;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        String time = timeText();
        String date = dateText();
        String style = clockSettings.style().getValue();
        int x = getX();
        int y = getY();

        if (ClockSettings.STYLE_MINIMAL.equals(style)) {
            renderMinimal(context, client, x, y, time, date);
            return;
        }

        int width = getWidth(client);
        int height = getHeight(client);
        if (ClockSettings.STYLE_CARD.equals(style)) {
            PremiumRender.card(context, x - PADDING_X, y - PADDING_Y, width, height, 7, 0xD911141D, 0xAA4C7DFF);
            PremiumRender.roundedRect(context, x - PADDING_X, y - PADDING_Y, 3, height, 2, 0xFF4C7DFF);
        } else {
            PremiumRender.card(context, x - PADDING_X, y - PADDING_Y, width, height, 8, 0x77101724, 0x6688A8FF);
            context.fill(x - PADDING_X + 2, y - PADDING_Y + 2, x - PADDING_X + width - 2, y - PADDING_Y + 3, 0x33FFFFFF);
        }

        RenderUtil.drawText(context, client, time, x, y, ColorUtil.WHITE);
        if (clockSettings.showDate().getValue()) {
            RenderUtil.drawText(context, client, date, x, y + 11, 0xFFB9C4DA);
        }
    }

    @Override
    public int getWidth(MinecraftClient client) {
        int textWidth = client.textRenderer.getWidth(timeText());
        if (clockSettings.showDate().getValue()) {
            textWidth = Math.max(textWidth, client.textRenderer.getWidth(dateText()));
        }
        if (ClockSettings.STYLE_MINIMAL.equals(clockSettings.style().getValue())) {
            return textWidth;
        }
        return textWidth + PADDING_X * 2;
    }

    @Override
    public int getHeight(MinecraftClient client) {
        int textHeight = clockSettings.showDate().getValue() ? 20 : 9;
        if (ClockSettings.STYLE_MINIMAL.equals(clockSettings.style().getValue())) {
            return textHeight;
        }
        return textHeight + PADDING_Y * 2;
    }

    private void renderMinimal(DrawContext context, MinecraftClient client, int x, int y, String time, String date) {
        if (hasBackground()) {
            drawBackground(context, client);
        }
        RenderUtil.drawText(context, client, time, x, y, ColorUtil.WHITE);
        if (clockSettings.showDate().getValue()) {
            RenderUtil.drawText(context, client, date, x, y + 11, 0xFFB9C4DA);
        }
    }

    private String timeText() {
        ZonedDateTime now = ZonedDateTime.now(clockSettings.zoneId());
        return now.format(clockSettings.showSeconds().getValue() ? TIME_SECONDS : TIME_MINUTES);
    }

    private String dateText() {
        return ZonedDateTime.now(clockSettings.zoneId()).format(DATE);
    }
}
