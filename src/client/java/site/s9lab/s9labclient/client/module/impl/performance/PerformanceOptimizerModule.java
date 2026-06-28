package site.s9lab.s9labclient.client.module.impl.performance;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;

public final class PerformanceOptimizerModule extends Module {
    private final ModeSetting preset;
    private final BooleanSetting lightweightUi;
    private final BooleanSetting missingModWarnings;

    public PerformanceOptimizerModule() {
        super("Performance Optimizer", "Built-in S9Lab performance presets, cached metrics and UI-friendly rendering.", ModuleCategory.PERFORMANCE, true);
        this.preset = addSetting(new ModeSetting("Preset", "Balanced", "Quality", "Balanced", "Max FPS"));
        this.lightweightUi = addSetting(new BooleanSetting("Lightweight UI", true));
        this.missingModWarnings = addSetting(new BooleanSetting("Missing Mod Warnings", true));
    }

    public String preset() {
        return preset.getValue();
    }

    public boolean lightweightUi() {
        return isEnabled() && lightweightUi.getValue();
    }

    public boolean missingModWarnings() {
        return isEnabled() && missingModWarnings.getValue();
    }
}
