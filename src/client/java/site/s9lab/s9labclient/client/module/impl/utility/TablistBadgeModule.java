package site.s9lab.s9labclient.client.module.impl.utility;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class TablistBadgeModule extends Module {
    public TablistBadgeModule() {
        super("Tablist Badge", "Adds an S9 badge before your own name in the player list.", ModuleCategory.UTILITY, true);
    }
}
