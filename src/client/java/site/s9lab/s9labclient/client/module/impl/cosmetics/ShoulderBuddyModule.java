package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public class ShoulderBuddyModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public ShoulderBuddyModule(CosmeticRegistry cosmeticRegistry) {
        super("Shoulder Buddy", "Renders a tiny buddy cosmetic on your shoulder.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;
        addSetting(CosmeticModeSettings.create("Buddy", cosmeticRegistry, CosmeticType.SHOULDER, "s9lab_mini_me"));
        addSetting(new NumberSetting("Scale", 100.0D, 60.0D, 130.0D, 10.0D));
        addSetting(new NumberSetting("Height", 0.0D, -20.0D, 20.0D, 5.0D));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}
