package software.bernie.geckolib.cache;

import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.cache.model.BakedGeoModel;

import java.util.Map;
import net.minecraft.class_2960;

/**
 * Container record for GeckoLib's baked model cache
 *
 * @param cache The baked models map as loaded from the resource files
 */
public record BakedModelCache(Map<class_2960, BakedGeoModel> cache) {
    /**
     * Get a {@link BakedGeoModel} from the model cache by its file id
     *
     * @param modelFile The file identifier of the animations file - (E.G. {@code mymod:entity/my_mob})
     */
    public BakedGeoModel getModel(class_2960 modelFile) {
        BakedGeoModel model = this.cache.get(modelFile);

        if (model == null) {
            class_2960 strippedPath = stripLegacyPath(modelFile);

            if (!modelFile.equals(strippedPath)) {
                GeckoLibConstants.LOGGER.error("Superfluous prefix or suffix found in model resource path: '{}'. Should be '{}'", modelFile, strippedPath);

                model = this.cache.get(strippedPath);
            }

            if (model == null)
                throw new IllegalArgumentException("Unable to find model file: " + modelFile);
        }

        return model;
    }

    /**
     * Strips out unnecessary prefix/suffix components of a model resource path.<br>
     * Typically these are leftovers from previous versions of GeckoLib.
     *
     * @deprecated To be removed once a sufficient time has passed to allow devs to fix their paths
     */
    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    private static class_2960 stripLegacyPath(class_2960 legacyPath) {
        String path = legacyPath.method_12832();

        if (path.startsWith("geckolib/"))
            path = path.substring(9);

        if (path.startsWith("models/"))
            path = path.substring(7);

        if (path.endsWith(".json"))
            path = path.substring(0, path.length() - 5);

        if (path.endsWith(".geo"))
            path = path.substring(0, path.length() - 4);

        return !path.equals(legacyPath.method_12832()) ? legacyPath.method_45136(path) : legacyPath;
    }
}
