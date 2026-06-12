package software.bernie.geckolib;

import com.mojang.serialization.Codec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;
import net.minecraft.class_2960;
import net.minecraft.class_9135;
import net.minecraft.class_9331;

/**
 * Holder class for several properties and/or handlers inherent to GeckoLib
 */
public final class GeckoLibConstants {
    public static final Logger LOGGER = LogManager.getLogger("GeckoLib");
    public static final String MODID = "geckolib";

    public static final Supplier<class_9331<Long>> STACK_ANIMATABLE_ID_COMPONENT = GeckoLibServices.PLATFORM.registerDataComponent("stack_animatable_id", builder -> builder.method_57881(Codec.LONG).method_57882(class_9135.field_48551));

    public static void init() {}

    /**
     * Helper method to create an Identifier predefined with GeckoLib's {@link #MODID}
     */
    public static class_2960 id(String path) {
        return class_2960.method_60655(GeckoLibConstants.MODID, path);
    }

    /**
     * Throw an exception pertaining to a specific resource
     * <p>
     * This mostly serves as a helper for consistent formatting of exceptions
     *
     * @param resource The location or id of the resource the error pertains to
     * @param message The error message to display
     */
    public static RuntimeException exception(class_2960 resource, String message) {
        return new RuntimeException(resource + ": " + message);
    }

    /**
     * Throw an exception pertaining to a specific resource
     * <p>
     * This mostly serves as a helper for consistent formatting of exceptions
     *
     * @param resource The location or id of the resource the error pertains to
     * @param message The error message to display
     * @param exception The exception to throw
     */
    public static RuntimeException exception(class_2960 resource, String message, Throwable exception) {
        return new RuntimeException(resource + ": " + message, exception);
    }
}
