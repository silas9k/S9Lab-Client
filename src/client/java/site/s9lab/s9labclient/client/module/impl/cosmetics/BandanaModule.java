package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public class BandanaModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public BandanaModule(CosmeticRegistry cosmeticRegistry) {
        super("Bandana", "Enables the S9Lab head bandana cosmetic.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;
        addSetting(CosmeticModeSettings.create("Bandana", cosmeticRegistry, CosmeticType.BANDANA, "s9lab_bandana"));
        addSetting(new NumberSetting("Head Offset", 0.0D, -10.0D, 10.0D, 1.0D));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}
