package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class HaloModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public HaloModule(CosmeticRegistry cosmeticRegistry) {
        super("Halo", "Renders the selected floating halo cosmetic.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;
        addSetting(CosmeticModeSettings.create("Halo", cosmeticRegistry, CosmeticType.HALO, "s9lab_gold_halo"));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}
