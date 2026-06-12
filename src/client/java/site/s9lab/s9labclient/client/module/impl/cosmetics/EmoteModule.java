package site.s9lab.s9labclient.client.module.impl.cosmetics;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.emote.EmoteManager;

public class EmoteModule extends Module {
    public EmoteModule() {
        super("S9 Emote", "Client-side S9 emote preset selector.", ModuleCategory.COSMETICS, true);
        addSetting(new ModeSetting("Emote", "t_pose", EmoteManager.ids().toArray(String[]::new)));
    }
}
