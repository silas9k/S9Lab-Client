package site.s9lab.s9labclient.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.module.impl.cosmetics.BandanaModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.CapeModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.EmoteModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.GlintModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.HaloModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.HatModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.ShoulderBuddyModule;
import site.s9lab.s9labclient.client.module.impl.cosmetics.WingsModule;
import site.s9lab.s9labclient.client.module.impl.hud.ClockHudElement;
import site.s9lab.s9labclient.client.module.impl.hud.CoordinatesModule;
import site.s9lab.s9labclient.client.module.impl.hud.FpsModule;
import site.s9lab.s9labclient.client.module.impl.hud.KeystrokesModule;
import site.s9lab.s9labclient.client.module.impl.hud.PingModule;
import site.s9lab.s9labclient.client.module.impl.render.OwnNameModule;
import site.s9lab.s9labclient.client.module.impl.render.ZoomModule;
import site.s9lab.s9labclient.client.module.impl.utility.TablistBadgeModule;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void registerDefaults(CosmeticRegistry cosmeticRegistry) {
        register(new FpsModule());
        register(new CoordinatesModule());
        register(new PingModule());
        register(new ClockHudElement());
        // Music Display is disabled for now because Windows media detection is unreliable.
        // register(new MusicHudElement());
        register(new KeystrokesModule());
        register(new CapeModule(cosmeticRegistry));
        register(new BandanaModule(cosmeticRegistry));
        register(new WingsModule(cosmeticRegistry));
        register(new HatModule(cosmeticRegistry));
        register(new HaloModule(cosmeticRegistry));
        register(new ShoulderBuddyModule(cosmeticRegistry));
        register(new GlintModule(cosmeticRegistry));
        register(new EmoteModule());
        register(new OwnNameModule());
        register(new ZoomModule());
        register(new TablistBadgeModule());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<HudModule> getHudModules() {
        return modules.stream()
                .filter(HudModule.class::isInstance)
                .map(HudModule.class::cast)
                .toList();
    }

    public Optional<Module> getModule(String name) {
        String normalized = normalize(name);
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name)
                        || normalize(module.getName()).equals(normalized)
                        || normalize(module.getName()).contains(normalized))
                .findFirst();
    }

    private static String normalize(String name) {
        return name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}

