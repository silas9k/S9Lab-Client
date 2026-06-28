package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public final class ShieldModule extends Module {
    public ShieldModule(CosmeticRegistry cosmeticRegistry) {
        super("Shield", "Renders the selected custom shield cosmetic on your left arm.", ModuleCategory.COSMETICS, true);

        addSetting(CosmeticModeSettings.create("Shield", cosmeticRegistry, CosmeticType.SHIELD, "void_shield"));
        addSetting(new ModeSetting("Visibility", "Always show", "Always show", "Only with shield item"));
        addSetting(new NumberSetting("Scale", 100.0D, 50.0D, 240.0D, 5.0D));
        addSetting(new NumberSetting("X Offset", 0.0D, -80.0D, 80.0D, 1.0D));
        addSetting(new NumberSetting("Y Offset", 0.0D, -80.0D, 80.0D, 1.0D));
        addSetting(new NumberSetting("Z Offset", 0.0D, -80.0D, 80.0D, 1.0D));
        addSetting(new NumberSetting("Rotation", 0.0D, -180.0D, 180.0D, 5.0D));
    }
}
