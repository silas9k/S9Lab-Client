package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class HatModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public HatModule(CosmeticRegistry cosmeticRegistry) {
        super("Hat", "Renders the selected hat cosmetic.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;
        addSetting(CosmeticModeSettings.create("Hat", cosmeticRegistry, CosmeticType.HAT, "s9lab_black_crown"));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}
