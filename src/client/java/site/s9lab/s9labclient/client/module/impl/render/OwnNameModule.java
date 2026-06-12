package site.s9lab.s9labclient.client.module.impl.render;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;

public class OwnNameModule extends Module {
    public OwnNameModule() {
        super("Own Name", "Shows your own nametag in third person/F5.", ModuleCategory.RENDER, true);
    }
}
