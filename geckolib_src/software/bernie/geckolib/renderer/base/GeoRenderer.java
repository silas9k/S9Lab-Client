package software.bernie.geckolib.renderer.base;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.value.Variable;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.ToDoubleFunction;
import net.minecraft.class_11659;
import net.minecraft.class_11785;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_8080;

/**
 * Base interface for all GeckoLib renderers.
 *
 * @param <T> The type of animatable this renderer is for
 * @param <O> The associated object this renderer takes when extracting data, or void if not applicable
 * @param <R> The type of render state this renderer uses
 * @see GeoRendererInternals
 */
public non-sealed interface GeoRenderer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRendererInternals<T, O, R> {
	/**
	 * Gets a tint-applying color to render the given animatable with
	 * <p>
	 * Returns opaque white by default
	 */
    @Override
	default int getRenderColor(T animatable, @Nullable O relatedObject, float partialTick) {
		return 0xFFFFFFFF;
	}

	/**
	 * Gets a packed overlay coordinate pair for rendering
	 * <p>
	 * Mostly just used for the red tint when an entity is hurt,
	 * but can be used for other things like the {@link net.minecraft.class_1548}
	 * white tint when exploding.
	 */
    @Override
	default int getPackedOverlay(T animatable, @Nullable O relatedObject, float u, float partialTick) {
		return class_4608.field_21444;
	}

	/**
	 * Determines the threshold value before the animatable should be considered moving for animation purposes
	 * <p>
	 * The default value and usage for this varies depending on the renderer
	 * <ul>
	 *     <li>For entities, it represents the lateral velocity of the object or the current speed of the {@link class_8080}</li>
	 *     <li>For {@link software.bernie.geckolib.animatable.GeoBlockEntity Tile Entities} and {@link software.bernie.geckolib.animatable.GeoItem Items}, it's currently unused</li>
	 *</ul>
	 *
	 *
	 * The lower the value, the more sensitive the {@link AnimationTest#isMoving()} check will be.
	 * <p>
	 * Particularly low values may have adverse effects
	 */
	default float getMotionAnimThreshold(T animatable) {
		return 0.015f;
	}

	/**
	 * Gets the {@link class_1921} to render the current render pass with
	 * <p>
	 * Uses the {@link class_12249#method_75994} {@code RenderType} by default
	 * <p>
	 * Override this to change the way a model will render (such as translucent models, etc.)
	 *
	 * @return Return the RenderType to use, or null to prevent the model rendering. Returning null will not prevent animation functions from taking place
	 */
	default @Nullable class_1921 getRenderType(R renderState, class_2960 texture) {
		return class_12249.method_75994(texture);
	}

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
	@ApiStatus.OverrideOnly
    default void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {}

    /**
     * This method is called once per render-frame for each {@link GeoAnimatable} being rendered
     * <p>
     * Use this method to set custom {@link Variable Variable} values via
     * {@link MathParser#setVariable(String, ToDoubleFunction) MathParser.setVariable}
     */
    @Override
    default void setMolangQueryValues(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {}

    /**
     * Initial access point for performing a single render pass; it all begins here.<br>
     * The {@link GeoRenderState} should have already been filled by this stage.
     * <p>
     * All GeckoLib renderers should immediately defer their respective default {@code submit} calls to this, for consistent handling
     * @see #performRenderPass(GeoRenderState, class_4587, class_11659, class_12075, RenderPassInfo.BoneUpdater)
     */
    default void performRenderPass(R renderState, class_4587 poseStack, class_11659 renderTasks, class_12075 cameraState) {
        performRenderPass(renderState, poseStack, renderTasks, cameraState, null);
    }

	/**
	 * Initial access point for performing a single render pass, with an optional pre-defined {@link RenderPassInfo.BoneUpdater} to allow for pre-positioning models from outside the renderer
	 */
	default void performRenderPass(R renderState, class_4587 poseStack, class_11659 renderTasks, class_12075 cameraState, RenderPassInfo.@Nullable BoneUpdater<R> boneUpdater) {
		poseStack.method_22903();

        final class_1921 renderType = getRenderType(renderState, getTextureLocation(renderState));
        final RenderPassInfo<R> renderPassInfo = RenderPassInfo.create(this, renderState, poseStack, cameraState, renderType != null);

        if (boneUpdater != null)
            renderPassInfo.addBoneUpdater(boneUpdater);

		if (firePreRenderEvent(renderPassInfo, renderTasks)) {
            preRenderPass(renderPassInfo, renderTasks);
            scaleModelForRender(renderPassInfo, 1, 1);
            adjustRenderPose(renderPassInfo);
			preApplyRenderLayers(renderPassInfo, renderTasks);
            renderPassInfo.captureModelRenderPose();
			submitRenderTasks(renderPassInfo, renderTasks, renderType);
            submitPerBoneRenderTasks(renderPassInfo, renderTasks);
			applyRenderLayers(renderPassInfo, renderTasks);
		}

		poseStack.method_22909();

		postRenderPass(renderPassInfo, renderTasks);
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
	default void preRenderPass(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {}

    /**
     * Scales the {@link class_4587} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call
     * <p>
     * Override and call {@code super} with modified scale values as needed to further modify the scale of the model
     */
    default void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        if (widthScale != 1 || heightScale != 1)
            renderPassInfo.poseStack().method_22905(widthScale, heightScale, widthScale);
    }

    /**
     * Transform the {@link class_4587} in preparation for rendering the model.
     * <p>
     * This is called after {@link #scaleModelForRender}, and so any transformations here will be scaled appropriately.
     * If you need to do pre-scale translations, use {@link #preRenderPass}
     * <p>
     * PoseStack translations made here are kept until the end of the render process
     */
    default void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {}

    /**
     * Perform any necessary adjustments of the model here, such as positioning/scaling/rotating or hiding bones.
     * <p>
     * No manipulation of the RenderState is permitted here
     */
    default void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {}

    /**
     * Build and submit the actual render task to the {@link class_11785} here.
     * <p>
     * Once the render task has been submitted here, no further manipulations of the render pass should be made.
     * <p>
     * If the provided {@link class_1921} is null, no submission will be made
     */
    default void submitRenderTasks(RenderPassInfo<R> renderPassInfo, class_11785 renderTasks, @Nullable class_1921 renderType) {
        if (renderType == null)
            return;

        final int packedLight = renderPassInfo.packedLight();
        final int packedOverlay = renderPassInfo.packedOverlay();
        final int renderColor = renderPassInfo.renderColor();

        renderTasks.method_73483(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            final class_4587 poseStack = renderPassInfo.poseStack();

            poseStack.method_22903();
            poseStack.method_23760().method_66521(pose);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
            poseStack.method_22909();
        });
    }

	/**
	 * Called after the rest of the render pass has completed, including discarding the PoseStack's pose.
     * <p>
     * The actual rendering of the object has not yet taken place, as that is done in a deferred {@link #performRenderPass submission}
	 */
	default void postRenderPass(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {}
}
