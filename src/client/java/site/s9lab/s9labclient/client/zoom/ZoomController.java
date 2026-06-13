package site.s9lab.s9labclient.client.zoom;

import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.impl.render.ZoomModule;

public final class ZoomController {
    private static boolean zooming;
    private static float smoothedFov = -1.0F;
    private static boolean pendingSave;

    private ZoomController() {
    }

    public static void setZooming(boolean zooming) {
        boolean wasZooming = ZoomController.zooming;
        ZoomController.zooming = zooming;
        if (wasZooming && !zooming) {
            savePending();
            smoothedFov = -1.0F;
        }
    }

    public static boolean shouldZoom() {
        ZoomModule module = getModule();
        return zooming && module != null && module.isEnabled();
    }

    public static float getFov(float original) {
        ZoomModule module = getModule();
        if (module == null || !shouldZoom()) {
            smoothedFov = original;
            return original;
        }
        float target = module.getFov();
        if (smoothedFov < 0.0F) {
            smoothedFov = original;
        }
        smoothedFov += (target - smoothedFov) * 0.42F;
        return smoothedFov;
    }

    public static boolean handleMouseScroll(double verticalAmount) {
        ZoomModule module = getModule();
        if (!zooming || module == null || !module.isEnabled()) {
            return false;
        }
        module.adjustFov(verticalAmount);
        pendingSave = true;
        return true;
    }

    private static void savePending() {
        if (!pendingSave || S9LabClientClient.getConfigManager() == null) {
            return;
        }
        pendingSave = false;
        S9LabClientClient.getConfigManager().save();
    }

    private static ZoomModule getModule() {
        if (S9LabClientClient.getModuleManager() == null) {
            return null;
        }

        Module module = S9LabClientClient.getModuleManager().getModule("Zoom").orElse(null);
        return module instanceof ZoomModule zoomModule ? zoomModule : null;
    }
}
