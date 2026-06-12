package software.bernie.geckolib.util;

import net.minecraft.class_2350;
import net.minecraft.class_3532;

/**
 * Helper class for miscellaneous functions that don't fit into the other util classes
 */
public final class MiscUtil {
    public static final float WORLD_TO_MODEL_SIZE = 1 / 16f;
    public static final float MODEL_TO_WORLD_SIZE = 16f;

    /**
     * Converts a {@link class_2350} to a rotational float for rotation purposes
     */
    public static float getDirectionAngle(class_2350 direction) {
        return switch(direction) {
            case field_11035 -> 90f;
            case field_11043 -> 270f;
            case field_11034 -> 180f;
            default -> 0f;
        };
    }

    /**
     * Return whether the two floating point values should be considered numerically equal
     * <p>
     * This is important because of the way floating point values work, there may not necessarily be
     * 1:1 equality between two functionally equal floating point values
     */
    public static boolean areFloatsEqual(double a, double b) {
        return Math.abs(a - b) < class_3532.field_29849;
    }

    /**
     * Special helper function for interpolating yaw.
     * <p>
     * This exists because yaw in Minecraft handles its yaw a bit strangely; and can cause incorrect results if interpolated without accounting for special cases
     */
    public static double lerpYaw(double delta, double start, double end) {
        start = class_3532.method_15338(start);
        end = class_3532.method_15338(end);
        double diff = start - end;
        end = diff > 180 || diff < -180 ? start + Math.copySign(360 - Math.abs(diff), diff) : end;

        return class_3532.method_16436(delta, start, end);
    }

    private MiscUtil() {}
}
