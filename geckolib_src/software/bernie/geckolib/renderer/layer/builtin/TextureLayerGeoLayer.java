package software.bernie.geckolib.renderer.layer.builtin;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Function;
import net.minecraft.class_1047;
import net.minecraft.class_11659;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

/**
 * Built-in GeoLayer for quickly performing another render pass for the same model after the main render pass has completed.
 * <p>
 * This should only be used if the additional render pass isn't specific to any bones, as this re-renders the entire model.
 * If you are using this to use custom textures/rendertypes on specific bones, use {@link CustomBoneTextureGeoLayer} instead.
 *
 * @param <T> Animatable class type. Inherited from the renderer this layer is attached to
 * @param <O> Associated object class type, or {@link Void} if none. Inherited from the renderer this layer is attached to
 * @param <R> RenderState class type. Inherited from the renderer this layer is attached to
 */
public class TextureLayerGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    protected final class_2960 texture;
    protected final @Nullable Function<class_2960, class_1921> renderType;

    TextureLayerGeoLayer(GeoRenderer<T, O, R> renderer) {
        this(renderer, class_1047.method_4539(), null);
    }

    public TextureLayerGeoLayer(GeoRenderer<T, O, R> renderer, class_2960 texture) {
        this(renderer, texture, null);
    }

    public TextureLayerGeoLayer(GeoRenderer<T, O, R> renderer, class_2960 texture, @Nullable Function<class_2960, class_1921> renderTypeFunction) {
        super(renderer);

        this.texture = texture;
        this.renderType = renderTypeFunction;
    }

    /**
     * Get the texture resource path for the given {@link GeoRenderState}
     */
    @Override
    protected class_2960 getTextureResource(R renderState) {
        return this.texture;
    }

    /**
     * Get the render type for the render pass
     */
    protected @Nullable class_1921 getRenderType(R renderState) {
        final class_2960 texture = getTextureResource(renderState);

        if (this.renderType == null)
            return this.renderer.getRenderType(renderState, texture);

        return this.renderType.apply(texture);
    }

    /**
     * This is the method that is actually called by the render for your render layer to function
     * <p>
     * This is called <i>after</i> the animatable has been submitted for rendering, but before supplementary rendering submissions like nametags
     */
    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        if (!renderPassInfo.willRender())
            return;

        class_1921 renderType = getRenderType(renderPassInfo.renderState());

        if (renderType != null)
            this.renderer.submitRenderTasks(renderPassInfo, renderTasks.method_73529(1), renderType);
    }
}
