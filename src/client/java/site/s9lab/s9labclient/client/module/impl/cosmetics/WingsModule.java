package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public class WingsModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public WingsModule(CosmeticRegistry cosmeticRegistry) {
        super("Wings", "Renders the selected S9Lab wing cosmetic on your player.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;

        addSetting(CosmeticModeSettings.create("Wings", cosmeticRegistry, CosmeticType.WINGS, "s9lab_dragon_wings"));

        addSetting(new NumberSetting("Scale", 100.0D, 60.0D, 160.0D, 10.0D));
        addSetting(new NumberSetting("Height", 0.0D, -20.0D, 20.0D, 5.0D));

        addSetting(new ModeSetting("Color Mode", "Normal", "Normal", "Gradient", "Rainbow"));

        addSetting(new NumberSetting("Color 1 Red", 120.0D, 0.0D, 255.0D, 1.0D));
        addSetting(new NumberSetting("Color 1 Green", 0.0D, 0.0D, 255.0D, 1.0D));
        addSetting(new NumberSetting("Color 1 Blue", 255.0D, 0.0D, 255.0D, 1.0D));

        addSetting(new NumberSetting("Color 2 Red", 255.0D, 0.0D, 255.0D, 1.0D));
        addSetting(new NumberSetting("Color 2 Green", 0.0D, 0.0D, 255.0D, 1.0D));
        addSetting(new NumberSetting("Color 2 Blue", 80.0D, 0.0D, 255.0D, 1.0D));

        addSetting(new NumberSetting("Rainbow Speed", 1.0D, 0.1D, 5.0D, 0.1D));
        addSetting(new NumberSetting("Rainbow Saturation", 1.0D, 0.0D, 1.0D, 0.05D));
        addSetting(new NumberSetting("Rainbow Brightness", 1.0D, 0.1D, 1.0D, 0.05D));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}