package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public final class S9PlusAuraModule extends Module {
    public S9PlusAuraModule() {
        super("S9C+ Aura", "Exclusive animated aura around active S9Lab+ players.", ModuleCategory.COSMETICS, true);
        addSetting(new BooleanSetting("Show My Aura", true));
        addSetting(new BooleanSetting("Show Other Plus Auras", true));
        addSetting(new NumberSetting("Size", 100.0D, 70.0D, 140.0D, 5.0D));
        addSetting(new NumberSetting("Speed", 100.0D, 40.0D, 180.0D, 5.0D));
        addSetting(new NumberSetting("Pulse", 100.0D, 0.0D, 180.0D, 5.0D));
    }
}
