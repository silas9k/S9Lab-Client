package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class GlintModule extends Module {
    public GlintModule(CosmeticRegistry cosmeticRegistry) {
        super("Glint", "Adds animated body glints to your player.", ModuleCategory.COSMETICS, false);
        addSetting(CosmeticModeSettings.create("Glint", cosmeticRegistry, CosmeticType.GLINT, "s9lab_gold_glint"));
    }
}
