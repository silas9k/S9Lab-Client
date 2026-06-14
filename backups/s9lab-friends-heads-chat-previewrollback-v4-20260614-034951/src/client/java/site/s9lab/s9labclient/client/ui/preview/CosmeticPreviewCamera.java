package site.s9lab.s9labclient.client.ui.preview;

/**
 * Smooth, inertial camera controller for cosmetic player previews.
 * Keeps input and rendered values separate so the model never snaps while dragging or zooming.
 */
public final class CosmeticPreviewCamera {
    private static final float MIN_PITCH = -24.0F;
    private static final float MAX_PITCH = 28.0F;
    private static final float MIN_ZOOM = 42.0F;
    private static final float MAX_ZOOM = 140.0F;

    private float yaw = 180.0F;
    private float pitch = 8.0F;
    private float zoom = 78.0F;

    private float targetYaw = yaw;
    private float targetPitch = pitch;
    private float targetZoom = zoom;

    private float yawVelocity;
    private float pitchVelocity;
    private boolean dragging;

    public void update(float deltaTicks, boolean hovered, double mouseX, double mouseY,
                       int x, int y, int width, int height) {
        float dt = Math.min(3.0F, Math.max(0.0F, deltaTicks));

        if (!dragging) {
            yawVelocity *= pow(0.82F, dt);
            pitchVelocity *= pow(0.76F, dt);
            targetYaw += yawVelocity * dt;
            targetPitch = clamp(targetPitch + pitchVelocity * dt, MIN_PITCH, MAX_PITCH);

            // Subtle mouse-follow, similar to polished client previews. It never overrides rotation.
            if (hovered && width > 0 && height > 0) {
                float nx = clamp((float) ((mouseX - (x + width * 0.5D)) / (width * 0.5D)), -1.0F, 1.0F);
                float ny = clamp((float) ((mouseY - (y + height * 0.5D)) / (height * 0.5D)), -1.0F, 1.0F);
                targetYaw += nx * 0.055F * dt;
                targetPitch = clamp(targetPitch - ny * 0.025F * dt, MIN_PITCH, MAX_PITCH);
            }
        }

        float rotationBlend = 1.0F - pow(0.60F, dt);
        float zoomBlend = 1.0F - pow(0.48F, dt);
        yaw = lerpAngle(yaw, targetYaw, rotationBlend);
        pitch = lerp(pitch, targetPitch, rotationBlend);
        zoom = lerp(zoom, targetZoom, zoomBlend);
    }

    public void beginDrag() {
        dragging = true;
        yawVelocity = 0.0F;
        pitchVelocity = 0.0F;
    }

    public void drag(double deltaX, double deltaY) {
        if (!dragging) {
            return;
        }
        float yawDelta = (float) deltaX * 0.78F;
        float pitchDelta = (float) -deltaY * 0.48F;
        targetYaw += yawDelta;
        targetPitch = clamp(targetPitch + pitchDelta, MIN_PITCH, MAX_PITCH);
        yawVelocity = yawDelta * 0.42F;
        pitchVelocity = pitchDelta * 0.26F;
    }

    public void endDrag() {
        dragging = false;
    }

    public void scroll(double amount) {
        targetZoom = clamp(targetZoom + (float) amount * 6.0F, MIN_ZOOM, MAX_ZOOM);
    }

    public void reset(float yaw, float pitch, float zoom) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.zoom = zoom;
        this.targetYaw = yaw;
        this.targetPitch = clamp(pitch, MIN_PITCH, MAX_PITCH);
        this.targetZoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);
        this.yawVelocity = 0.0F;
        this.pitchVelocity = 0.0F;
        this.dragging = false;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public int zoom() {
        return Math.round(zoom);
    }

    public boolean dragging() {
        return dragging;
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * clamp(amount, 0.0F, 1.0F);
    }

    private static float lerpAngle(float from, float to, float amount) {
        float delta = wrapDegrees(to - from);
        return from + delta * clamp(amount, 0.0F, 1.0F);
    }

    private static float wrapDegrees(float value) {
        value %= 360.0F;
        if (value >= 180.0F) value -= 360.0F;
        if (value < -180.0F) value += 360.0F;
        return value;
    }

    private static float pow(float value, float exponent) {
        return (float) Math.pow(value, exponent);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
