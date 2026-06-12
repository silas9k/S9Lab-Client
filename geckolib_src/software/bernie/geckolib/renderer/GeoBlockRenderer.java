package software.bernie.geckolib.renderer;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibClientServices;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayersContainer;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_12075;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2754;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_827;

/**
 * Base {@link GeoRenderer} class for rendering {@link class_2586 Blocks} specifically
 * <p>
 * All blocks added to be rendered by GeckoLib should use an instance of this class.
 *
 * @param <T> BlockEntity animatable class type
 * @param <R> RenderState class type
 */
public class GeoBlockRenderer<T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState> implements GeoRenderer<T, Void, R>, class_827<T, R> {
	protected final GeoRenderLayersContainer<T, Void, R> renderLayers = new GeoRenderLayersContainer<>(this);
	protected final GeoModel<T> model;

	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

    /**
     * Creates a new defaulted renderer instance, using the BlockEntity's registered id as the file name for its assets
     */
    public GeoBlockRenderer(class_2591<? extends T> blockEntityType) {
        this(new DefaultedBlockGeoModel<>(Objects.requireNonNull(class_7923.field_41181.method_10221(blockEntityType))));
    }

	public GeoBlockRenderer(GeoModel<T> model) {
		this.model = model;
	}

    /**
     * Attempt to extract a direction from the block so that the model can be oriented correctly
     */
    @SuppressWarnings("unchecked")
    protected class_2350 getBlockStateDirection(T blockEntity) {
        class_2680 blockState = blockEntity.method_11010();

        for (class_2754<class_2350> property : new class_2754[] {class_2741.field_12525, class_2741.field_12481, class_2741.field_28062 , class_2741.field_12545 }) {
            if (blockState.method_28498(property))
                return blockState.method_11654(property);
        }

        return class_2350.field_11043;
    }

    /**
     * Rotate the {@link class_4587} based on the determined {@link class_2350} the block is facing
     */
    protected void tryRotateByBlockstate(RenderPassInfo<R> renderPassInfo, class_4587 poseStack) {
        final class_2350 facing = renderPassInfo.getOrDefaultGeckolibData(DataTickets.BLOCK_FACING, class_2350.field_11043);

        switch (facing) {
            case field_11035 -> poseStack.method_22907(class_7833.field_40716.rotationDegrees(180));
            case field_11039 -> poseStack.method_22907(class_7833.field_40716.rotationDegrees(90));
            case field_11034 -> poseStack.method_22907(class_7833.field_40715.rotationDegrees(90));
            case field_11036 -> poseStack.method_22907(class_7833.field_40714.rotationDegrees(90));
            case field_11033 -> poseStack.method_22907(class_7833.field_40713.rotationDegrees(90));
            default -> {}
        }
    }

    //<editor-fold defaultstate="collapsed" desc="<Internal Methods>">
    /**
     * Gets the model instance for this renderer
     */
    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    /**
     * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
     */
    @Override
    public List<GeoRenderLayer<T, Void, R>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    @SuppressWarnings("UnusedReturnValue")
    public GeoBlockRenderer<T, R> withRenderLayer(Function<? super GeoBlockRenderer<T, R>, GeoRenderLayer<T, Void, R>> renderLayer) {
        return withRenderLayer(renderLayer.apply(this));
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    public GeoBlockRenderer<T, R> withRenderLayer(GeoRenderLayer<T, Void, R> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoBlockRenderer<T, R> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoBlockRenderer<T, R> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    /**
     * Gets the id that represents the current animatable's instance for animation purposes.
     *
     * @param animatable The Animatable instance being renderer
     */
    @ApiStatus.OverrideOnly
    @Override
    public long getInstanceId(T animatable, @Nullable Void ignored) {
        return animatable.method_11016().hashCode();
    }

    /**
     * Internal method for capturing the common RenderState data for all animatable objects
     */
    @ApiStatus.Internal
    @Override
    public void captureDefaultRenderState(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        GeoRenderer.super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(DataTickets.BLOCKSTATE, animatable.method_11010());
        renderState.addGeckolibData(DataTickets.POSITION, class_243.method_24953(animatable.method_11016()));
        renderState.addGeckolibData(DataTickets.BLOCK_FACING, getBlockStateDirection(animatable));
    }

    /**
     * Called at the start of the render compilation pass. PoseState manipulations have not yet taken place and typically should not be made here.
     * <p>
     * Manipulation of the model's bones is not permitted here
     * <p>
     * Use this method to handle any preparation or pre-work required for the render submission.
     *
     * @see #scaleModelForRender
     * @see #adjustRenderPose
     * @see #adjustModelBonesForRender
     */
    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        renderPassInfo.poseStack().method_22904(0.5d, 0, 0.5d);
    }

    /**
     * Scales the {@link class_4587} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call
     * <p>
     * Override and call {@code super} with modified scale values as needed to further modify the scale of the model
     */
    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        GeoRenderer.super.scaleModelForRender(renderPassInfo, this.scaleWidth * widthScale, this.scaleHeight * heightScale);
    }

    /**
     * Transform the {@link class_4587} in preparation for rendering the model.
     * <p>
     * This is called after {@link #scaleModelForRender}, and so any transformations here will be scaled appropriately.
     * If you need to do pre-scale translations, use {@link #preRenderPass}
     * <p>
     * PoseStack translations made here are kept until the end of the render process
     */
    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        tryRotateByBlockstate(renderPassInfo, renderPassInfo.poseStack());
    }

    /**
     * Initial access point for vanilla's {@link class_827} interface<br>
     * Immediately defers to {@link GeoRenderer#performRenderPass(GeoRenderState, class_4587, class_11659, class_12075)}
     */
    @ApiStatus.Internal
    @Override
    public void method_3569(R renderState, class_4587 poseStack, class_11659 renderTasks, class_12075 cameraRenderState) {
        GeoRenderer.super.performRenderPass(renderState, poseStack, renderTasks, cameraRenderState);
    }

    /**
     * Default return for creating the {@link class_11954}.
     * <p>
     * You generally shouldn't need to override or use this
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    @Override
    public R method_74335() {
        return (R)new class_11954();
    }

    /**
     * Create the contextually relevant {@link class_11954} for the current render pass
     */
    @Override
    public void method_74331(T blockEntity, R renderState, float partialTick, class_243 cameraPos, class_11683.@Nullable class_11792 damageOverlayState) {
        class_827.super.method_74331(blockEntity, renderState, partialTick, cameraPos, damageOverlayState);
        fillRenderState(blockEntity, null, renderState, partialTick);
    }

    /**
     * Create and fire the relevant {@code CompileLayers} event hook for this renderer
     */
    @Override
    public void fireCompileRenderLayersEvent() {
        GeckoLibClientServices.EVENTS.fireCompileBlockRenderLayers(this);
    }

    /**
     * Create and fire the relevant {@code CompileRenderState} event hook for this renderer
     */
    @Override
    public void fireCompileRenderStateEvent(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        GeckoLibClientServices.EVENTS.fireCompileBlockRenderState(this, renderState, animatable);
    }

    /**
     * Create and fire the relevant {@code Pre-Render} event hook for this renderer
     *
     * @return Whether the renderer should proceed based on the cancellation state of the event
     */
    @Override
    public boolean firePreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeckoLibClientServices.EVENTS.fireBlockPreRender(renderPassInfo, renderTasks);
    }

    /**
     * @deprecated Unusable because of vanilla implementation. Use {@link #method_74335()}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    @ApiStatus.Internal
    @Override
    public final R createRenderState(T animatable, @Nullable Void relatedObject) {
        return (R)new class_11954();
    }
    //</editor-fold>
}
