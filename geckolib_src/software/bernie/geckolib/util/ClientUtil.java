package software.bernie.geckolib.util;

import net.minecraft.class_12131;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3673;
import net.minecraft.class_746;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtil {
	/**
	 * Get the player on the client
	 */
	public static @Nullable class_1657 getClientPlayer() {
		return class_310.method_1551().field_1724;
	}

	/**
	 * Gets the current level on the client
	 */
	public static @Nullable class_1937 getLevel() {
		return class_310.method_1551().field_1687;
	}

	/**
	 * Whether the local (client) player has a cape
	 */
	public static boolean clientPlayerHasCape() {
		final class_746 player = class_310.method_1551().field_1724;

		return player != null && player.method_52814().comp_1627() != null;
	}

	/**
	 * Get the current camera position
	 */
	public static class_243 getCameraPos() {
		return class_310.method_1551().field_1773.method_19418().method_71156();
	}

	/**
	 * Helper method to check for first-person camera mode
	 * <p>
	 * Split off to preserve side-agnosticism of the Molang system
	 */
	public static boolean isFirstPerson() {
		return class_310.method_1551().field_1690.method_31044().method_31034();
	}

	/**
	 * Get the current phase of the moon on the client world
	 */
	public static class_12131 getClientMoonPhase() {
		return class_310.method_1551().field_1769.field_61737.field_63087.field_63096;
	}

    /**
     * Get the game time for the client world, or a global game time if no world is loaded<br>
     * Returned value is in ticks
	 * <p>
	 * Note that due to vanilla desync issues, the level will occasionally go backwards 1 tick
     */
    public static double getCurrentTick() {
        final class_310 mc = class_310.method_1551();

        return mc.field_1687 != null ?
               mc.field_1687.method_75260() + mc.method_61966().method_60637(false) :
               class_3673.method_15974() * 20d;
    }

    @ApiStatus.Internal
	public static int getVisibleEntityCount() {
        final class_310 mc = class_310.method_1551();

        if (mc.field_1687 == null)
            return 0;

        return mc.field_1769.field_61737.field_61735.size();
	}

    private ClientUtil() {}
}
