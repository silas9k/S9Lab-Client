package software.bernie.geckolib.loading.json.raw;

import net.minecraft.class_243;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.loading.definition.geometry.GeometryDescription;

/**
 * Container class for model property information
 */
public record ModelProperties(String identifier, @Nullable Float visibleBoundsWidth, @Nullable Float visibleBoundsHeight, @Nullable class_243 visibleBoundsOffset,
							  int textureWidth, int textureHeight) {
	/**
	 * Temporary conversion method, until the new geometry loading is fully in place
	 */
	@Deprecated(forRemoval = true)
	public static ModelProperties fromDescription(GeometryDescription description) {
		return new ModelProperties(description.identifier(), description.visibleBoundsWidth(), description.visibleBoundsHeight(), description.visibleBoundsOffset(), description.textureWidth(), description.textureHeight());
	}
}