package site.s9lab.s9labclient.client.ui.premium.animation;

import java.util.HashMap;
import java.util.Map;

public final class AnimationManager {
    public static final float DEFAULT_SPEED = 12.0F;
    private final Map<String, Float> values = new HashMap<>();

    public float animate(String key, boolean target, float deltaTicks) {
        return animate(key, target ? 1.0F : 0.0F, DEFAULT_SPEED, deltaTicks);
    }

    public float animate(String key, float target, float speed, float deltaTicks) {
        float current = values.getOrDefault(key, target);
        float frame = Math.max(0.0F, Math.min(1.0F, deltaTicks / 20.0F));
        float factor = 1.0F - (float) Math.pow(0.001F, frame * Math.max(0.1F, speed));
        float next = current + (target - current) * factor;
        if (Math.abs(next - target) < 0.001F) {
            next = target;
        }
        values.put(key, next);
        return next;
    }

    public void set(String key, float value) {
        values.put(key, value);
    }

    public float get(String key) {
        return values.getOrDefault(key, 0.0F);
    }
}
