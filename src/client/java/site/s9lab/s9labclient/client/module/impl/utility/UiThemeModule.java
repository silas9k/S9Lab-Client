package site.s9lab.s9labclient.client.module.impl.utility;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ColorSetting;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public final class UiThemeModule extends Module {
    public static final String MODULE_NAME = "UI Theme";
    public static final String ACCENT_SETTING = "Main Color";
    public static final String BLUR_SETTING = "Blur Background";

    public UiThemeModule() {
        super(MODULE_NAME, "Change the client UI main color and background feel.", ModuleCategory.UTILITY, true);
        addSetting(new ColorSetting(ACCENT_SETTING, ClientTheme.DEFAULT_ACCENT));
        addSetting(new BooleanSetting(BLUR_SETTING, true));
    }
}
