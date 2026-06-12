package software.bernie.geckolib.renderer.layer.builtin;

import com.mojang.datafixers.util.Either;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.PerBoneRender;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.class_10017;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_811;

/**
 * {@link GeoRenderLayer} for rendering {@link class_2680 BlockStates}
 * or {@link class_1799 ItemStacks} on a given {@link GeoAnimatable}
 *
 * @param <T> Animatable class type. Inherited from the renderer this layer is attached to
 * @param <O> Associated object class type, or {@link Void} if none. Inherited from the renderer this layer is attached to
 * @param <R> RenderState class type. Inherited from the renderer this layer is attached to
 */
public abstract class BlockAndItemGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public BlockAndItemGeoLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    /**
     * Return a list of the bone names that this layer will render for.
     * <p>
     * Ideally, you would cache this list in a class-field if you don't need any data from the input renderState or model
     */
    protected abstract List<RenderData<R>> getRelevantBones(R renderState, BakedGeoModel model);

    /**
     * Override to add any custom {@link DataTicket}s you need to capture for rendering.
     * <p>
     * The animatable is discarded from the rendering context after this, so any data needed
     * for rendering should be captured in the renderState provided
     *
     * @param animatable The animatable instance being rendered
     * @param relatedObject An object related to the render pass or null if not applicable.
     *                         (E.G., ItemStack for GeoItemRenderer, entity instance for GeoReplacedEntityRenderer).
     * @param renderState The GeckoLib RenderState to add data to, will be passed through the rest of rendering
     * @param partialTick The fraction of a tick that has elapsed as of the current render pass
     */
    @Override
    public abstract void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick);

    /**
     * Container for data needed to render an item or block for a bone.
     *
     * @param boneName The name of the bone to render the armor piece for
     * @param displayContext The {@link class_811} to use when rendering the item
     * @param retrievalFunction The function to retrieve the {@link class_1799} or {@link class_2680} to render. You probably need to override
     * {@link GeoRenderLayer#addRenderData(GeoAnimatable, Object, GeoRenderState, float)} as well
     */
    public record RenderData<R extends GeoRenderState>(String boneName, class_811 displayContext, BiFunction<GeoBone, R, Either<class_1799, class_2680>> retrievalFunction) {}

    /**
     * Register per-bone render operations, to be rendered after the main model is done.
     * <p>
     * Even though the task is called after the main model renders, the {@link class_4587} provided will be posed as if the bone
     * is currently rendering.
     *
     * @param renderPassInfo The collated render-related data for this render pass
     * @param consumer The registrar to accept the per-bone render tasks
     */
    @Override
    public void addPerBoneRender(RenderPassInfo<R> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        if (!renderPassInfo.willRender())
            return;

        final R renderState = renderPassInfo.renderState();
        final BakedGeoModel model = renderPassInfo.model();

        for (RenderData<R> renderData : getRelevantBones(renderState, model)) {
            model.getBone(renderData.boneName)
                    .ifPresentOrElse(bone -> createPerBoneRender(bone, renderData, consumer, renderState),
                                     () -> GeckoLibConstants.LOGGER.error("Unable to find bone for ItemArmorGeoLayer: {}, skipping", renderData.boneName));
        }

    }

    private void createPerBoneRender(GeoBone bone, RenderData<R> renderData, BiConsumer<GeoBone, PerBoneRender<R>> consumer, R renderState) {
        Either<class_1799, class_2680> renderObject = renderData.retrievalFunction().apply(bone, renderState);

        renderObject.ifLeft(stack -> {
            if (!stack.method_7960()) {
                consumer.accept(bone, (renderPassInfo, bone2, renderTasks) -> {
                    //RenderUtil.translateAndRotateMatrixForBone(poseStack, bone);
                    submitItemStackRender(renderPassInfo.poseStack(), bone2, stack, renderData.displayContext, renderPassInfo.renderState(), renderTasks,
                                          renderPassInfo.cameraState(), renderPassInfo.packedLight(), renderPassInfo.packedOverlay(), renderPassInfo.renderColor());
                });
            }
        }).ifRight(blockState -> {
            if (!blockState.method_26215()) {
                consumer.accept(bone, (renderPassInfo, bone2, renderTasks) -> {
                    //RenderUtil.translateAndRotateMatrixForBone(poseStack, bone);
                    submitBlockRender(renderPassInfo.poseStack(), bone2, blockState, renderPassInfo.renderState(), renderTasks,
                                      renderPassInfo.cameraState(), renderPassInfo.packedLight(), renderPassInfo.packedOverlay(), renderPassInfo.renderColor());
                });
            }
        });
    }

    /**
     * Render the given {@link class_1799} for the provided {@link GeoBone}.
     */
    protected void submitItemStackRender(class_4587 poseStack, GeoBone bone, class_1799 stack, class_811 displayContext, R renderState, class_11659 renderTasks,
                                         class_12075 cameraState, int packedLight, int packedOverlay, int renderColor) {
        final class_10444 stackRenderState = new class_10444();
        final class_310 mc = class_310.method_1551();

        mc.method_65386().method_65598(stackRenderState, stack, displayContext, mc.field_1687, null, (int)(long)renderState.getOrDefaultGeckolibData(DataTickets.ANIMATABLE_INSTANCE_ID, 0L) + displayContext.ordinal());
        stackRenderState.method_65604(poseStack, renderTasks, packedLight, class_4608.field_21444, 0);
    }

    /**
     * Render the given {@link class_2680} for the provided {@link GeoBone}.
     */
    protected void submitBlockRender(class_4587 poseStack, GeoBone bone, class_2680 state, R renderState, class_11659 renderTasks,
                                     class_12075 cameraState, int packedLight, int packedOverlay, int renderColor) {
        poseStack.method_22903();
        poseStack.method_46416(-0.25f, -0.25f, -0.25f);
        poseStack.method_22905(0.5f, 0.5f, 0.5f);
        renderTasks.method_73481(poseStack, state, packedLight, class_4608.field_21444, renderState instanceof class_10017 entityState ? entityState.field_61821 : 0);
        poseStack.method_22909();
    }
}
