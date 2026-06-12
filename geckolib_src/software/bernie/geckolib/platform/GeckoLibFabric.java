package software.bernie.geckolib.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2378;
import net.minecraft.class_7923;
import net.minecraft.class_9331;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.service.GeckoLibPlatform;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Fabric service for general loader-specific functions
 */
public final class GeckoLibFabric implements GeckoLibPlatform {
    /**
     * @return Whether the current runtime is an in-dev (non-production) environment, for running debug-only tasks
     */
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /**
     * @return The root game directory (./run)
     */
    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    /**
     * @return Whether the current runtime is on the client side regardless of logical context
     */
    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    /**
     * Register a {@link class_9331}
     * <p>
     * This is mostly just used for storing the animatable ID on ItemStacks
     */
    @Override
    public <T> Supplier<class_9331<T>> registerDataComponent(String id, UnaryOperator<class_9331.class_9332<T>> builder) {
        final class_9331<T> componentType = class_2378.method_10226(class_7923.field_49658, GeckoLibConstants.id(id).toString(), builder.apply(class_9331.method_57873()).method_57880());

        return () -> componentType;
    }
}
