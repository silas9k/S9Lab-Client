package site.s9lab.s9labclient.client.module.impl.render;

import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public class ZoomModule extends Module {
    private final NumberSetting fov;

    public ZoomModule() {
        super("Zoom", "Hold the zoom key to reduce FOV for cleaner aim and screenshots.", ModuleCategory.RENDER, true);
        this.fov = addSetting(new NumberSetting("FOV", 28.0D, 10.0D, 70.0D, 2.0D));
    }

    public float getFov() {
        return fov.getValue().floatValue();
    }

    public void adjustFov(double scrollAmount) {
        double next = fov.getValue() - scrollAmount * fov.getStep();
        fov.setValue(next);
    }
}
