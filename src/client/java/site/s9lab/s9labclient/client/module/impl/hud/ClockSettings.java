package site.s9lab.s9labclient.client.module.impl.hud;

import java.time.ZoneId;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;

public record ClockSettings(
        ModeSetting style,
        ModeSetting timezone,
        BooleanSetting showDate,
        BooleanSetting showSeconds
) {
    public static final String STYLE_MINIMAL = "Minimal";
    public static final String STYLE_CARD = "Card";
    public static final String STYLE_GLASS = "Glass";
    public static final String TIMEZONE_SYSTEM = "System";
    public static final String TIMEZONE_BERLIN = "Berlin";

    public ZoneId zoneId() {
        return TIMEZONE_BERLIN.equals(timezone.getValue()) ? ZoneId.of("Europe/Berlin") : ZoneId.systemDefault();
    }
}
