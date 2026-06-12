package software.bernie.geckolib.service;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.class_1297;
import net.minecraft.class_9331;

/**
 * Loader-agnostic service interface for general loader-specific functions
 */
public interface GeckoLibPlatform {
    /**
     * @return Whether the current runtime is an in-dev (non-production) environment, for running debug-only tasks
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return Whether the current runtime is on the client side regardless of logical context
     */
    boolean isPhysicalClient();

    /**
     * @return The root game directory (./run)
     */
    Path getGameDir();

    /**
     * Helper method to account for Forge/NeoForge's custom fluid implementation in relation to swimming in fluids
     *
     * @return Whether the entity is in a swimmable fluid or not
     */
    default boolean isInSwimmableFluid(class_1297 entity) {
        return entity.method_5799();
    }

    /**
     * Register a {@link class_9331}
     * <p>
     * This is mostly just used for storing the animatable ID on ItemStacks
     */
    <T> Supplier<class_9331<T>> registerDataComponent(String id, UnaryOperator<class_9331.class_9332<T>> builder);
}
