package software.bernie.geckolib.service;

import com.google.gson.Gson;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import software.bernie.geckolib.cache.animation.Animation;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.loading.definition.animation.ActorAnimations;
import software.bernie.geckolib.loading.definition.geometry.Geometry;

/**
 * Replaceable SPI for alternate deserialization schemes, replacing GeckoLib's default {@link Gson} handling
 * <p>
 * Not currently used by GeckoLib
 */
public interface GeckoLibLoader {
    /**
     * Deserialize an identified resource into an unbaked {@link Geometry} instance, ready for
     * transposition into a usable {@link BakedGeoModel}
     *
     * @param id The resource path of the resource to load
     * @param resource The resource reference to load
     * @return An unbaked Geometry instance
     */
    Geometry deserializeGeckoLibModelFile(class_2960 id, class_3298 resource);

    /**
     * Deserialize an identified resource into an unbaked {@link ActorAnimations} instance, ready for
     * transposition into a usable {@link Animation}
     *
     * @param id The resource path of the resource to load
     * @param resource The resource reference to load
     * @return An unbaked ActorAnimations instance
     */
    ActorAnimations deserializeGeckoLibAnimationFile(class_2960 id, class_3298 resource);
}
