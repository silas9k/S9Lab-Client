package site.s9lab.s9labclient.client.module.impl.cosmetics;

import java.util.List;
import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;

final class CosmeticModeSettings {
    private CosmeticModeSettings() {
    }

    static ModeSetting create(String settingName, CosmeticRegistry registry, CosmeticType type, String preferredDefault) {
        List<String> modes = registry.idsByType(type);
        if (modes.isEmpty()) {
            return new ModeSetting(settingName, preferredDefault, preferredDefault);
        }

        String selected = modes.contains(preferredDefault) ? preferredDefault : modes.get(0);
        return new ModeSetting(settingName, selected, modes.toArray(String[]::new));
    }
}
