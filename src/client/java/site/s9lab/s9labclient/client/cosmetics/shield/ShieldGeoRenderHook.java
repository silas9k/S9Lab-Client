package site.s9lab.s9labclient.client.cosmetics.shield;

/**
 * GeckoLib's object renderer needs the current render camera state. The normal
 * player feature renderer owns the actual shield transform; this bridge only
 * carries the camera state through the surrounding LivingEntityRenderer call.
 */
public final class ShieldGeoRenderHook {
    private static final ThreadLocal<Object> CAMERA_STATE = new ThreadLocal<>();

    private ShieldGeoRenderHook() {
    }

    public static void setCameraState(Object cameraState) {
        if (cameraState == null) {
            CAMERA_STATE.remove();
        } else {
            CAMERA_STATE.set(cameraState);
        }
    }

    public static Object cameraState() {
        return CAMERA_STATE.get();
    }

    public static void clearCameraState() {
        CAMERA_STATE.remove();
    }
}
