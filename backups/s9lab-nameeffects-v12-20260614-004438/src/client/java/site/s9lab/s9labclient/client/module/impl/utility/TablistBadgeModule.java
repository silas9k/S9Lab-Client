package site.s9lab.s9labclient.client.module.impl.utility;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;

public class TablistBadgeModule extends Module {
    private static final String[] EFFECTS = {
            "none", "rainbow", "wave", "shake", "spin", "bounce", "pulse", "blink", "fade", "iterate", "glitch",
            "color_white", "color_red", "color_orange", "color_yellow", "color_green", "color_cyan", "color_blue", "color_purple", "color_pink"
    };
    private final BooleanSetting plusRainbowName;
    private final BooleanSetting plusAnimatedName;
    private final BooleanSetting plusNameEffectsEnabled;
    private final BooleanSetting showOtherPlayersNameEffects;
    private final ModeSetting plusEffect1;
    private final ModeSetting plusEffect2;
    private final ModeSetting plusEffect3;

    public TablistBadgeModule() {
        super("Tablist Badge", "Shows S9/S9C+ icons and synchronized player-name shader effects.", ModuleCategory.UTILITY, true);
        this.plusRainbowName = addSetting(new BooleanSetting("Plus Rainbow Name", true));
        this.plusAnimatedName = addSetting(new BooleanSetting("Plus Animated Name", true));
        this.plusNameEffectsEnabled = addSetting(new BooleanSetting("Plus Name Effects Enabled", true));
        this.showOtherPlayersNameEffects = addSetting(new BooleanSetting("Show Other Plus Name Effects", true));
        this.plusEffect1 = addSetting(new ModeSetting("Plus Effect 1", "rainbow", EFFECTS));
        this.plusEffect2 = addSetting(new ModeSetting("Plus Effect 2", "wave", EFFECTS));
        this.plusEffect3 = addSetting(new ModeSetting("Plus Effect 3", "none", EFFECTS));
    }

    public boolean plusRainbowName() {
        return plusRainbowName.getValue();
    }

    public boolean plusAnimatedName() {
        return plusAnimatedName.getValue();
    }

    public boolean plusNameEffectsEnabled() {
        return plusNameEffectsEnabled.getValue();
    }

    public boolean showOtherPlayersNameEffects() {
        return showOtherPlayersNameEffects.getValue();
    }

    public java.util.List<String> plusNameEffects() {
        return java.util.List.of(plusEffect1.getValue(), plusEffect2.getValue(), plusEffect3.getValue());
    }
}
