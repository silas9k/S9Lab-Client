package software.bernie.geckolib.model;

import net.minecraft.class_2960;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Defaulted model class for GeckoLib models
 * <p>
 * This class allows for minimal boilerplate when implementing basic models, and saves on new classes
 * <p>
 * Additionally, it encourages consistency and sorting of asset paths.
 */
public abstract class DefaultedGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
	private class_2960 modelPath;
	private class_2960 texturePath;
	private class_2960 animationsPath;

	/**
	 * Create a new instance of this model class
	 * <p>
	 * The asset path should be the truncated relative path from the base folder
	 * <p>
	 * E.G.
	 * <pre>{@code new Identifier("myMod", "animals/red_fish")}</pre>
	 */
	public DefaultedGeoModel(class_2960 assetSubpath) {
		this.modelPath = buildFormattedModelPath(assetSubpath);
		this.texturePath = buildFormattedTexturePath(assetSubpath);
		this.animationsPath = buildFormattedAnimationPath(assetSubpath);
	}

	/**
	 * Changes the constructor-defined model path for this model to an alternate
	 * <p>
	 * This is useful if your animatable shares a model path with another animatable that differs in path to the texture and animations for this model
	 */
	public DefaultedGeoModel<T> withAltModel(class_2960 altPath) {
		this.modelPath = buildFormattedModelPath(altPath);

		return this;
	}

	/**
	 * Changes the constructor-defined animations path for this model to an alternate
	 * <p>
	 * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
	 */
	public DefaultedGeoModel<T> withAltAnimations(class_2960 altPath) {
		this.animationsPath = buildFormattedAnimationPath(altPath);

		return this;
	}

	/**
	 * Changes the constructor-defined texture path for this model to an alternate
	 * <p>
	 * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
	 */
	public DefaultedGeoModel<T> withAltTexture(class_2960 altPath) {
		this.texturePath = buildFormattedTexturePath(altPath);

		return this;
	}

	/**
	 * Constructs a defaulted resource path for a geo.json file based on the input namespace and subpath, automatically using the {@link DefaultedGeoModel#subtype() subtype}
	 *
	 * @param basePath The base path of your resource. E.G. <pre>{@code new Identifier(MyMod.MOD_ID, "animal/goat")}</pre>
	 * @return The formatted model resource path based on recommended defaults. E.G. <pre>{@code "mymod:entity/animal/goat"}</pre>
	 */
	public class_2960 buildFormattedModelPath(class_2960 basePath) {
		return basePath.method_45138(subtype() + "/");
	}

	/**
	 * Constructs a defaulted resource path for a animation.json file based on the input namespace and subpath, automatically using the {@link DefaultedGeoModel#subtype() subtype}
	 *
	 * @param basePath The base path of your resource. E.G. <pre>{@code new Identifier(MyMod.MOD_ID, "animal/goat")}</pre>
	 * @return The formatted animation resource path based on recommended defaults. E.G. <pre>{@code "mymod:entity/animal/goat"}</pre>
	 */
	public class_2960 buildFormattedAnimationPath(class_2960 basePath) {
		return basePath.method_45138(subtype() + "/");
	}

	/**
	 * Constructs a defaulted resource path for a geo.json file based on the input namespace and subpath, automatically using the {@link DefaultedGeoModel#subtype() subtype}
	 *
	 * @param basePath The base path of your resource. E.G. <pre>{@code new Identifier(MyMod.MOD_ID, "animal/goat")}</pre>
	 * @return The formatted texture resource path based on recommended defaults. E.G. <pre>{@code "mymod:textures/entity/animal/goat.png"}</pre>
	 */
	public class_2960 buildFormattedTexturePath(class_2960 basePath) {
		return basePath.method_45136("textures/" + subtype() + "/" + basePath.method_12832() + ".png");
	}

	/**
	 * Returns the subtype string for this type of model
	 * <p>
	 * This allows for sorting of asset files into neat subdirectories for clean management
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>"entity"</li>
	 *     <li>"block"</li>
	 *     <li>"item"</li>
	 * </ul>
	 */
	protected abstract String subtype();

	@Override
	public class_2960 getModelResource(GeoRenderState renderState) {
		return this.modelPath;
	}

	@Override
	public class_2960 getTextureResource(GeoRenderState renderState) {
		return this.texturePath;
	}

	@Override
	public class_2960 getAnimationResource(T animatable) {
		return this.animationsPath;
	}
}
