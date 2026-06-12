package software.bernie.geckolib.renderer.base;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.object.DeferredCache;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;
import java.util.Map;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4608;

/**
 * Container class holding all the common information relevant for a single render pass in GeckoLib.
 * <p>
 * {@link GeoRenderer} builds an instance of this at the start of a render pass, and uses it until completion, then discards it.
 * <p>
 * This allows for a significant aggregation of the various objects passed to render methods, as well as
 * allowing extensibility where it may be wanted
 * <p>
 * This should hopefully make it easier to organize and manage data for rendering for end-users
 * <p>
 * <b><u>NOTE:</u></b> All objects contained by this instance should be considered functionally immutable.
 *
 * @param <R> RenderState class type
 */
public class RenderPassInfo<R extends GeoRenderState> {
    protected final GeoRenderer<?, ?, R> renderer;
    protected final R renderState;
    protected final class_4587 poseStack;
    protected final BakedGeoModel model;
    protected final class_12075 cameraState;
    protected final boolean willRender;
    protected final class_4587.class_4665 objectRenderPose;
    protected final class_4587.class_4665 modelRenderPose;

    protected final DeferredCache<List<BoneUpdater<R>>, BoneSnapshot[]> boneUpdates = new DeferredCache<>(new ObjectArrayList<>(), this::compileBoneUpdates);
    protected final Map<GeoBone, List<PerBoneRender<R>>> boneRenderTasks = new Reference2ObjectArrayMap<>();
    protected final Map<GeoBone, List<BonePositionListener>> bonePositionListeners = new Reference2ObjectArrayMap<>();

    /**
     * @see #create
     * @param renderer The GeoRenderer instance this instance is for
     * @param renderState The RenderState instance for this render pass
     * @param poseStack The PoseStack instance for this render pass
     * @param model The BakedGeoModel instance for this render pass
     * @param cameraState The CameraRenderState instance for this render pass
     * @param willRender Whether the model should actually render in this render pass. Typically false if {@link GeoRenderer#getRenderType} returns null.
     *                   This does not guarantee that the model will render, only that it should.
     */
    protected RenderPassInfo(GeoRenderer<?, ?, R> renderer, R renderState, class_4587 poseStack, BakedGeoModel model, class_12075 cameraState, boolean willRender) {
        this.renderer = renderer;
        this.renderState = renderState;
        this.poseStack = poseStack;
        this.model = model;
        this.cameraState = cameraState;
        this.willRender = willRender;
        this.objectRenderPose = new class_4587.class_4665();
        this.modelRenderPose = new class_4587.class_4665();

        this.objectRenderPose.method_66521(poseStack.method_23760());
    }

    /**
     * @return The GeoRenderer instance this instance is for
     */
    public GeoRenderer<?, ?, R> renderer() {
        return this.renderer;
    }

    /**
     * @return The GeoRenderState instance for this render pass
     */
    public R renderState() {
        return this.renderState;
    }

    /**
     * @return The PoseStack instance for this render pass
     */
    public class_4587 poseStack() {
        return this.poseStack;
    }

    /**
     * @return The BakedGeoModel instance for this render pass
     */
    public BakedGeoModel model() {
        return this.model;
    }

    /**
     * @return The CameraRenderState instance for this render pass
     */
    public class_12075 cameraState() {
        return this.cameraState;
    }

    /**
     * @return The packed light value for this render pass
     */
    public int packedLight() {
        return this.renderState.getPackedLight();
    }

    /**
     * @return The packed overlay coordinates for this render pass
     */
    public int packedOverlay() {
        return this.renderState.getOrDefaultGeckolibData(DataTickets.PACKED_OVERLAY, class_4608.field_21444);
    }

    /**
     * @return The packed (ARGB) color/tint value for this render pass
     */
    public int renderColor() {
    	return this.renderState.getOrDefaultGeckolibData(DataTickets.RENDER_COLOR, 0xFFFFFFFF);
    }

    /**
     * @return Whether the model should actually render in this render pass.<br>
     * Typically false if {@link GeoRenderer#getRenderType} returns null.
     */
    public boolean willRender() {
        return this.willRender;
    }

    /**
     * Shortcut method for retrieving render data from the {@link GeoRenderState}
     *
     * @see #renderState()
     */
    public <D> @Nullable D getGeckolibData(DataTicket<D> dataTicket) {
        return this.renderState().getGeckolibData(dataTicket);
    }

    /**
     * Shortcut method for retrieving render data from the {@link GeoRenderState}
     *
     * @see #renderState()
     */
    @Contract("_,null->null;_,!null->!null")
    public <D> @Nullable D getOrDefaultGeckolibData(DataTicket<D> dataTicket, @Nullable D fallback) {
        return this.renderState().getOrDefaultGeckolibData(dataTicket, fallback);
    }

    /**
     * Get the {@link class_4587.class_4665} for the current render pass representing the state
     * of the PoseStack prior to any renderer-specific manipulations
     */
    public class_4587.class_4665 getPreRenderMatrixPose() {
        return this.objectRenderPose;
    }

    /**
     * Get the {@link Matrix4f} for the current render pass representing
     * the state of the {@link class_4587} prior to any renderer-specific manipulations
     */
    public Matrix4f getPreRenderMatrixState() {
        return this.objectRenderPose.method_23761();
    }

    /**
     * Get the {@link class_4587.class_4665} for the current render pass representing the state
     * of the PoseStack prior to any renderer-specific manipulations
     */
    public class_4587.class_4665 getModelRenderMatrixPose() {
        return this.modelRenderPose;
    }

    /**
     * Get the {@link class_4587.class_4665#method_23761()} for the current render pass representing
     * the state of the PoseStack immediately prior to submitting the render task
     * <p>
     * <b><u>NOTE:</u></b> Must not be called prior to {@link GeoRenderer#submitRenderTasks}
     */
    public Matrix4f getModelRenderMatrixState() {
        if ((this.modelRenderPose.method_23761().properties() & Matrix4fc.PROPERTY_IDENTITY) != 0)
            throw new IllegalStateException("Attempting to access model render matrix state before it has been set");

        return this.modelRenderPose.method_23761();
    }

    /**
     * Add a {@link PerBoneRender} task to be executed for a specific bone in the model.
     * <p>
     * Typically should only be called from {@link GeoRenderLayer#addPerBoneRender}
     */
    public void addPerBoneRender(GeoBone bone, PerBoneRender<R> render) {
        this.boneRenderTasks.computeIfAbsent(bone, key -> new ObjectArrayList<>()).add(render);
    }

    /**
     * Add a BoneUpdater for this render pass
     * <p>
     * Can only be called prior to the renderer submitting this pass for rendering<br>
     * Updaters added after that point will be ignored
     */
    public void addBoneUpdater(BoneUpdater<R> updater) {
        try {
            this.boneUpdates.getInput().add(updater);
        }
        catch (IllegalStateException ex) {
            GeckoLibConstants.LOGGER.error("BoneUpdater added after render pass submission", ex);
        }
    }

    /**
     * Add a BonePositionListener for this render pass
     * <p>
     * Use this to capture bone matrix positions at the time of render, which is the only time they actually have a position of any kind
     */
    public void addBonePositionListener(String boneName, BonePositionListener listener) {
        this.model.getBone(boneName).ifPresent(bone -> addBonePositionListener(bone, listener));
    }

    /**
     * Add a BonePositionListener for this render pass
     * <p>
     * Use this to capture bone matrix positions at the time of render, which is the only time they actually have a position of any kind
     */
    public void addBonePositionListener(GeoBone bone, BonePositionListener listener) {
        this.bonePositionListeners.computeIfAbsent(bone, key -> new ObjectArrayList<>()).add(listener);
    }

    /**
     * Wrap a render task, posing the model using this RenderPassInfo's bone updates
     * <p>
     * All bone
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void renderPosed(Runnable renderTask) {
        final BoneSnapshot[] updates = this.boneUpdates.compute();

        for (int i = 0; i < updates.length; i++) {
            updates[i].apply();
        }

        if (!this.bonePositionListeners.isEmpty()) {
            for (Map.Entry<GeoBone, List<BonePositionListener>> boneListeners : this.bonePositionListeners.entrySet()) {
                boneListeners.getKey().positionListeners = boneListeners.getValue().toArray(new BonePositionListener[0]);
            }
        }

        try {
            renderTask.run();
        }
        catch (Exception ex) {
            GeckoLibConstants.LOGGER.error("Error while rendering GeckoLib model", ex);
        }
        finally {
            for (int i = 0; i < updates.length; i++) {
                updates[i].cleanup();
            }

            if (!this.bonePositionListeners.isEmpty()) {
                for (GeoBone bone : this.bonePositionListeners.keySet()) {
                    bone.positionListeners = null;
                }
            }
        }
    }

    /**
     * Singular GeoBone-positioning callback to run immediately before rendering a model
     *
     * @param <R> RenderState class type
     */
    @FunctionalInterface
    public interface BoneUpdater<R extends GeoRenderState> {
        /**
         * Run this BoneUpdate, adjusting one or more GeoBones for a single render pass
         *
         * @param renderPassInfo The collated render-related data for this render pass. GeoBone instances can be retrieved from the BakedGeoModel contained in this
         * @param snapshots Function to retrieve a BoneSnapshot for a given bone by its name. Use this to transform a bone for this render pass
         */
        void run(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots);
    }

    /**
     * Functional interface for a listener of bone render positions
     */
    @FunctionalInterface
    public interface BonePositionListener {
        void accept(@Nullable class_243 worldPos, @Nullable class_243 modelPos, @Nullable class_243 localPos);
    }

    //<editor-fold defaultstate="collapsed" desc="<Internal Methods>">

    /**
     * Create a new RenderPassInfo instance
     */
    @ApiStatus.Internal
    public static <R extends GeoRenderState> RenderPassInfo<R> create(GeoRenderer<?, ?, R> renderer, R renderState, class_4587 poseStack, class_12075 cameraState, boolean willRender) {
        final GeoModel<?> geoModel = renderer.getGeoModel();
        final BakedGeoModel model = geoModel.getBakedModel(geoModel.getModelResource(renderState));
        final RenderPassInfo<R> renderPassInfo = new RenderPassInfo<>(renderer, renderState, poseStack, model, cameraState, willRender);

        renderPassInfo.addBoneUpdater(renderer::applyAnimationControllers);
        renderPassInfo.addBoneUpdater(renderer::adjustModelBonesForRender);

        return renderPassInfo;
    }

    /**
     * Run through {@link #boneUpdates} and create update snapshots for each bone
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @ApiStatus.Internal
    protected BoneSnapshot[] compileBoneUpdates(List<BoneUpdater<R>> boneUpdaters) {
        final List<BoneSnapshot> snapshots = new ObjectArrayList<>(boneUpdaters.size());

        for (BoneUpdater<R> updater : boneUpdaters) {
            updater.run(this, boneName -> this.model.getBone(boneName).map(bone -> {
                if (bone.frameSnapshot == null)
                    snapshots.add(bone.frameSnapshot = BoneSnapshot.create(bone));

                return bone.frameSnapshot;
            }));
        }

        final BoneSnapshot[] array = snapshots.toArray(new BoneSnapshot[0]);

        for (int i = 0; i < array.length; i++) {
            array[i].cleanup();
        }

        return array;
    }

    /**
     * @return The {@link PerBoneRender} collection for this render pass
     */
    @ApiStatus.Internal
    public Map<GeoBone, List<PerBoneRender<R>>> getBoneRenderTasks() {
        return this.boneRenderTasks;
    }

    /**
     * Tell the {@link #poseStack} to cache its current state in the {@link #modelRenderPose}, for re-use later
     */
    @ApiStatus.Internal
    public void captureModelRenderPose() {
        this.modelRenderPose.method_66521(this.poseStack.method_23760());
    }

    //</editor-fold>
}
