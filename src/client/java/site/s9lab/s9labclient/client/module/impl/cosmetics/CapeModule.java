package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class CapeModule extends Module {
    private final CosmeticRegistry cosmeticRegistry;

    public CapeModule(CosmeticRegistry cosmeticRegistry) {
        super("Cape", "Enables S9Lab cosmetic capes.", ModuleCategory.COSMETICS, true);
        this.cosmeticRegistry = cosmeticRegistry;
        addSetting(CosmeticModeSettings.create("Cape", cosmeticRegistry, CosmeticType.CAPE, "s9lab_cape"));
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }
}
