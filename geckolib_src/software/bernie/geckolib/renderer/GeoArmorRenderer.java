package software.bernie.geckolib.renderer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibClientServices;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayersContainer;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.class_10034;
import net.minecraft.class_10192;
import net.minecraft.class_11659;
import net.minecraft.class_11785;
import net.minecraft.class_12249;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_572;
import net.minecraft.class_630;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import net.minecraft.class_970;

/**
 * Base {@link GeoRenderer} for rendering in-world armor specifically
 * <p>
 * All custom armor added to be rendered in-world by GeckoLib should use an instance of this class
 *
 * @param <T> Item animatable class type
 * @param <R> RenderState class type. GeckoLib armor rendering requires {@link class_10034} as the minimum class type
 * @see GeoItem
 */
public class GeoArmorRenderer<T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState> implements GeoRenderer<T, GeoArmorRenderer.RenderData, R> {
    protected static final class_1304[] ARMOR_SLOTS = new class_1304[] {class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
	protected final GeoRenderLayersContainer<T, GeoArmorRenderer.RenderData, R> renderLayers = new GeoRenderLayersContainer<>(this);
	protected final GeoModel<T> model;

	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

    /**
     * Creates a new defaulted renderer instance, using the item's registered id as the file name for its assets
     */
	public <I extends T> GeoArmorRenderer(I armorItem) {
		this(new DefaultedGeoModel<>(class_7923.field_41178.method_10221(armorItem)) {
            @Override
            protected String subtype() {
                return "armor";
            }
        });
	}

	public GeoArmorRenderer(GeoModel<T> model) {
		this.model = model;
	}

    /**
     * Return the list of {@link ArmorSegment}s that should be rendered for the given {@link class_1304} for this render pass.
     * <p>
     * Override this if your armor piece renders different pieces than the default setup
     */
    public List<ArmorSegment> getSegmentsForSlot(R renderState, class_1304 slot) {
        return switch (slot) {
            case field_6169 -> List.of(ArmorSegment.HEAD);
            case field_6174 -> List.of(ArmorSegment.CHEST, ArmorSegment.LEFT_ARM, ArmorSegment.RIGHT_ARM);
            case field_6172 -> List.of(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG);
            case field_6166 -> List.of(ArmorSegment.LEFT_FOOT, ArmorSegment.RIGHT_FOOT);
            default -> List.of();
        };
    }

    /**
     * Return the equivalent bone name for the given {@link ArmorSegment} for this render pass.
     * <p>
     * Override this if your armor has different bone names for some reason.
     */
    public String getBoneNameForSegment(R renderState, ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> "armorHead";
            case CHEST -> "armorBody";
            case LEFT_ARM -> "armorLeftArm";
            case RIGHT_ARM -> "armorRightArm";
            case LEFT_LEG -> "armorLeftLeg";
            case RIGHT_LEG -> "armorRightLeg";
            case LEFT_FOOT -> "armorLeftBoot";
            case RIGHT_FOOT -> "armorRightBoot";
        };
    }

	/**
	 * Data container for additional render context information for creating the RenderState for this renderer
	 *
	 * @param itemStack The ItemStack about to be rendered
	 * @param slot The EquipmentSlot the ItemStack is in
	 * @param entity The entity wearing the item
     * @param baseModel The base vanilla model for the armor piece being rendered
	 */
	public record RenderData(class_1799 itemStack, class_1304 slot, class_1309 entity, class_572<?> baseModel) {}

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
    public List<GeoRenderLayer<T, RenderData, R>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    @SuppressWarnings("UnusedReturnValue")
    public GeoArmorRenderer<T, R> withRenderLayer(Function<? super GeoArmorRenderer<T, R>, GeoRenderLayer<T, GeoArmorRenderer.RenderData, R>> renderLayer) {
        return withRenderLayer(renderLayer.apply(this));
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    public GeoArmorRenderer<T, R> withRenderLayer(GeoRenderLayer<T, GeoArmorRenderer.RenderData, R> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoArmorRenderer<T, R> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoArmorRenderer<T, R> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    /**
     * Gets a tint-applying color to render the given animatable with
     * <p>
     * Returns opaque white by default, multiplied by any inherent vanilla item dye color
     */
    @Override
    public int getRenderColor(T animatable, @SuppressWarnings("NullableProblems") RenderData stackAndSlot, float partialTick) {
        return GeckoLibClientServices.ITEM_RENDERING.getDyedItemColor(stackAndSlot.itemStack(), 0xFFFFFFFF);
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
    @Override
    public @Nullable class_1921 getRenderType(R renderState, class_2960 texture) {
        return class_12249.method_75966(texture);
    }

    /**
     * Gets the id that represents the current animatable's instance for animation purposes.
     *
     * @param animatable The Animatable instance being renderer
     * @param stackAndSlot An object related to the render pass or null if not applicable.
     *                         (E.G., ItemStack for GeoItemRenderer, entity instance for GeoReplacedEntityRenderer).
     */
    @ApiStatus.OverrideOnly
    @Override
    public long getInstanceId(T animatable, @SuppressWarnings("NullableProblems") RenderData stackAndSlot) {
        long stackId = GeoItem.getId(stackAndSlot.itemStack());

        if (stackId == Long.MAX_VALUE) {
            int id = stackAndSlot.entity().method_5628() * 13;

            return (long)id * id * id * -(stackAndSlot.slot().ordinal() + 1);
        }

        return -stackId;
    }

    /**
     * Internal method for capturing the common RenderState data for all animatable objects
     */
    @ApiStatus.Internal
    @Override
    public void captureDefaultRenderState(T animatable, @SuppressWarnings("NullableProblems") RenderData renderData, R renderState, float partialTick) {
        GeoRenderer.super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);

        renderState.addGeckolibData(DataTickets.POSITION, renderData.entity().method_73189());
        renderState.addGeckolibData(DataTickets.IS_GECKOLIB_WEARER, renderData.entity() instanceof GeoAnimatable);
        renderState.addGeckolibData(DataTickets.EQUIPMENT_SLOT, renderData.slot());
        renderState.addGeckolibData(DataTickets.HAS_GLINT, renderData.itemStack().method_7958());
        renderState.addGeckolibData(DataTickets.HUMANOID_MODEL, renderData.baseModel);
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
        renderPassInfo.poseStack().method_46416(0, 24 / 16f, 0);
        renderPassInfo.poseStack().method_22905(-1, -1, 1);
    }

    /**
     * Perform any necessary adjustments of the model here, such as positioning/scaling/rotating or hiding bones.
     * <p>
     * No manipulation of the RenderState is permitted here
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        final R renderState = renderPassInfo.renderState();
        final class_1304 slot = Objects.requireNonNull(renderState.getGeckolibData(DataTickets.EQUIPMENT_SLOT));
        final class_572 baseModel = Objects.requireNonNull(renderState.getGeckolibData(DataTickets.HUMANOID_MODEL));
        final List<ArmorSegment> segments = getSegmentsForSlot(renderState, slot);

        if (!segments.isEmpty()) {
            baseModel.method_17087(renderState);

            for (ArmorSegment segment : getSegmentsForSlot(renderState, slot)) {
                snapshots.get(getBoneNameForSegment(renderState, segment)).ifPresent(snapshot -> {
                    final class_630 modelPart = segment.modelPartGetter.apply(baseModel);
                    final Vector3f bonePos = segment.modelPartMatcher.apply(new Vector3f(modelPart.field_3657, modelPart.field_3656, modelPart.field_3655));

                    snapshot.setRotX(-modelPart.field_3654)
                            .setRotY(-modelPart.field_3675)
                            .setRotZ(modelPart.field_3674)
                            .setTranslateX(bonePos.x)
                            .setTranslateY(bonePos.y)
                            .setTranslateZ(bonePos.z);
                });
            }
        }
    }

    /**
     * Build and submit the actual render task to the {@link class_11785} here.
     * <p>
     * Once the render task has been submitted here, no further manipulations of the render pass should be made.
     * <p>
     * If the provided {@link class_1921} is null, no submission will be made
     */
    @Override
    public void submitRenderTasks(RenderPassInfo<R> renderPassInfo, class_11785 renderTasks, @Nullable class_1921 renderType) {
        if (renderType == null)
            return;

        final int packedLight = renderPassInfo.packedLight();
        final int packedOverlay = renderPassInfo.packedOverlay();
        final int renderColor = renderPassInfo.renderColor();
        final R renderState = renderPassInfo.renderState();
        final class_1304 slot = Objects.requireNonNull(renderState.getGeckolibData(DataTickets.EQUIPMENT_SLOT));

        renderTasks.method_73483(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            final class_4587 poseStack = renderPassInfo.poseStack();

            poseStack.method_22903();
            poseStack.method_23760().method_66521(pose);
            renderPassInfo.renderPosed(() -> {
                for (ArmorSegment segment : getSegmentsForSlot(renderState, slot)) {
                    renderPassInfo.model().getBone(getBoneNameForSegment(renderState, segment))
                            .ifPresent(bone -> bone.positionAndRender(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
                }
            });
            poseStack.method_22909();
        });
    }

    /**
     * Create and fire the relevant {@code CompileLayers} event hook for this renderer
     */
    @Override
    public void fireCompileRenderLayersEvent() {
        GeckoLibClientServices.EVENTS.fireCompileArmorRenderLayers(this);
    }

    /**
     * Create and fire the relevant {@code CompileRenderState} event hook for this renderer
     */
    @Override
    public void fireCompileRenderStateEvent(T animatable, @SuppressWarnings("NullableProblems") RenderData relatedObject, R renderState, float partialTick) {
        GeckoLibClientServices.EVENTS.fireCompileArmorRenderState(this, renderState, animatable, relatedObject);
    }

    /**
     * Create and fire the relevant {@code Pre-Render} event hook for this renderer
     *
     * @return Whether the renderer should proceed based on the cancellation state of the event
     */
    @Override
    public boolean firePreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeckoLibClientServices.EVENTS.fireArmorPreRender(renderPassInfo, renderTasks);
    }

    /**
     * Enum representing the different parts of a humanoid armor GeckoLib handles for rendering.
     */
    public enum ArmorSegment {
        HEAD(class_1304.field_6169, model -> model.field_3398, pos -> pos.mul(1, -1, 1)),
        CHEST(class_1304.field_6174, model -> model.field_3391, pos -> pos.mul(1, -1, 1)),
        LEFT_ARM(class_1304.field_6174, model -> model.field_27433, pos -> pos.set(pos.x - 5, 2 - pos.y, pos.z)),
        RIGHT_ARM(class_1304.field_6174, model -> model.field_3401, pos -> pos.set(pos.x + 5, 2 - pos.y, pos.z)),
        LEFT_LEG(class_1304.field_6172, model -> model.field_3397, pos -> pos.set(pos.x - 2, 12 - pos.y, pos.z)),
        RIGHT_LEG(class_1304.field_6172, model -> model.field_3392, pos -> pos.set(pos.x + 2, 12 - pos.y, pos.z)),
        LEFT_FOOT(class_1304.field_6166, model -> model.field_3397, pos -> pos.set(pos.x - 2, 12 - pos.y, pos.z)),
        RIGHT_FOOT(class_1304.field_6166, model -> model.field_3392, pos -> pos.set(pos.x + 2, 12 - pos.y, pos.z));

        public final class_1304 equipmentSlot;
        public final Function<class_572<?>, class_630> modelPartGetter;
        public final UnaryOperator<Vector3f> modelPartMatcher;

        ArmorSegment(class_1304 slot, Function<class_572<?>, class_630> modelPartGetter, UnaryOperator<Vector3f> modelPartMatcher) {
            this.equipmentSlot = slot;
            this.modelPartGetter = modelPartGetter;
            this.modelPartMatcher = modelPartMatcher;
        }
    }

    /**
     * Helper class to consolidate the retrieval and validation of an individual slot for rendering
     */
    @SuppressWarnings("rawtypes")
    @ApiStatus.Internal
    private record StackForRender(class_1799 stack, class_1304 slot, GeoArmorRenderer renderer, class_572<?> baseModel) {
        private static <S extends class_10034, A extends class_572<S>> @Nullable StackForRender find(
                class_1799 stack, class_1304 slot, S entityRenderState, BiFunction<S, class_1304, A> modelFunction) {
            final class_10192 equippable = stack.method_58694(class_9334.field_54196);
            GeoRenderProvider geckolibRenderers;

            if (equippable == null || !class_970.method_64082(equippable, slot) || (geckolibRenderers = GeoRenderProvider.of(stack)) == GeoRenderProvider.DEFAULT)
                return null;

            final A baseModel = modelFunction.apply(entityRenderState, slot);
            final GeoArmorRenderer<?, ?> armorRenderer = geckolibRenderers.getGeoArmorRenderer(stack, slot);

            if (armorRenderer == null)
                return null;

            return new StackForRender(stack, slot, armorRenderer, baseModel);
        }
    }

    /**
     * Attempt to render a GeckoLib {@link GeoArmorRenderer armor piece} for the given slot
     * <p>
     * This is typically only called by an internal mixin
     *
     * @return true if the armor piece was a GeckoLib armor piece and was rendered
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static <R extends class_10034 & GeoRenderState, A extends class_572<R>> boolean tryRenderGeoArmorPiece(
            BiFunction<R, class_1304, A> modelFunction, class_4587 poseStack, class_11659 renderTasks, class_1799 stack, class_1304 slot, int packedLight, R entityRenderState) {
        final StackForRender stackForRender = StackForRender.find(stack, slot, entityRenderState, modelFunction);
        EnumMap<class_1304, R> perSlotData;

        if (stackForRender == null)
            return false;

        if ((perSlotData = entityRenderState.getGeckolibData(DataTickets.PER_SLOT_RENDER_DATA)) == null || !perSlotData.containsKey(slot))
            return false;

        R perSlotRenderState = perSlotData.get(slot);
        stackForRender.renderer.performRenderPass(perSlotRenderState, poseStack, renderTasks, class_310.method_1551().field_1769.field_61737.field_63082, null);

        return true;
    }

	/**
	 * Capture and assign RenderState data for each GeckoLib-relevant equipment slot for rendering
     * <p>
     * Called internally by a mixin
	 */
	@SuppressWarnings("unchecked")
    @ApiStatus.Internal
	public static <R extends class_10034 & GeoRenderState, A extends class_572<R>> void captureRenderStates(
            R baseRenderState, class_1309 entity, float partialTick, BiFunction<R, class_1304, A> modelFunction, Function<class_1304, R> renderStateSupplier) {
		final List<StackForRender> relevantSlots = getRelevantSlotsForRendering(entity, baseRenderState, modelFunction);

		if (relevantSlots == null)
			return;

		final EnumMap<class_1304, R> slotRenderData = new EnumMap<>(class_1304.class);

        for (StackForRender entry : relevantSlots) {
            RenderData renderData = new RenderData(entry.stack, entry.slot, entity, entry.baseModel);
            R slotRenderState = renderStateSupplier.apply(entry.slot);

            entry.renderer.fillRenderState((GeoAnimatable)entry.stack.method_7909(), renderData, slotRenderState, partialTick);
            slotRenderData.put(entry.slot, slotRenderState);
        }

		baseRenderState.addGeckolibData(DataTickets.PER_SLOT_RENDER_DATA, slotRenderData);
	}

    /**
     * Compile an array of GeckoLib-relevant equipment pieces for rendering
     */
    @ApiStatus.Internal
	private static <R extends class_10034 & GeoRenderState, A extends class_572<R>> @Nullable List<StackForRender> getRelevantSlotsForRendering(
            class_1309 entity, R entityRenderState, BiFunction<R, class_1304, A> modelFunction) {
		List<StackForRender> relevantSlots = null;

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            final class_1304 slot = ARMOR_SLOTS[i];
            final StackForRender stackForRender = StackForRender.find(entity.method_6118(slot), slot, entityRenderState, modelFunction);

            if (stackForRender == null)
                continue;

            if (relevantSlots == null)
                relevantSlots = new ObjectArrayList<>(ARMOR_SLOTS.length - i);

            relevantSlots.add(stackForRender);
        }

		return relevantSlots;
	}

    /**
     * Disabled because we use the {@link class_10034} that vanilla compiles for the entity
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    @Deprecated
    @Override
    public R createRenderState(T animatable, @SuppressWarnings("NullableProblems") RenderData relatedObject) {
        return (R)new class_10034();
    }

    //</editor-fold>
}