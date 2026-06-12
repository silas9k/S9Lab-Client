package software.bernie.geckolib.renderer.layer.builtin;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibClientServices;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.cache.model.cuboid.CuboidGeoBone;
import software.bernie.geckolib.cache.model.cuboid.GeoCube;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.PerBoneRender;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.service.GeckoLibClient;
import software.bernie.geckolib.util.RenderStateUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_10186;
import net.minecraft.class_10192;
import net.minecraft.class_10197;
import net.minecraft.class_10201;
import net.minecraft.class_10394;
import net.minecraft.class_11659;
import net.minecraft.class_11786;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2190;
import net.minecraft.class_243;
import net.minecraft.class_2484;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_5321;
import net.minecraft.class_5598;
import net.minecraft.class_5617;
import net.minecraft.class_563;
import net.minecraft.class_572;
import net.minecraft.class_630;
import net.minecraft.class_630.class_628;
import net.minecraft.class_836;
import net.minecraft.class_9296;
import net.minecraft.class_9334;

/**
 * Builtin class for handling dynamic armor rendering on GeckoLib entities
 * <p>
 * Supports both {@link GeoItem GeckoLib} and vanilla armor models
 * <p>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering
 *
 * @param <T> Animatable class type. Inherited from the renderer this layer is attached to
 * @param <O> Associated object class type, or {@link Void} if none. Inherited from the renderer this layer is attached to
 * @param <R> RenderState class type. Inherited from the renderer this layer is attached to
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ItemArmorGeoLayer<T extends class_1309 & GeoAnimatable, O, R extends class_10017 & GeoRenderState> extends GeoRenderLayer<T, O, R> {
	protected final class_10197 equipmentRenderer;
	protected final class_10201 equipmentAssets;
	protected final Function<class_2484.class_2485, @Nullable class_5598> skullModels;
    protected final class_11786 skinCache;

	public ItemArmorGeoLayer(GeoRenderer<T, O, R> geoRenderer, class_5617.class_5618 context) {
		super(geoRenderer);

		this.equipmentRenderer = context.method_64072();
		this.equipmentAssets = context.method_64071();
		this.skullModels = class_156.method_34866(type -> class_836.method_32160(context.method_32170(), type));
        this.skinCache = context.method_73540();
	}

	/**
	 * Return a list of the bone names that this layer will render for.
	 * <p>
	 * Ideally, you would cache this list in a static field if you don't need any data from the input renderState or model
	 */
	protected abstract List<RenderData> getRelevantBones(RenderPassInfo<R> renderPassInfo);

	/**
	 * Container for data needed to render an armor piece for a bone.
	 *
	 * @param boneName The name of the bone to render the armor piece for
	 * @param armorSegment The armor segment to render
	 */
	public record RenderData(String boneName, GeoArmorRenderer.ArmorSegment armorSegment) {
		public static RenderData head(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.HEAD);
		}

		public static RenderData body(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.CHEST);
		}

		public static RenderData leftArm(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.LEFT_ARM);
		}

		public static RenderData rightArm(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.RIGHT_ARM);
		}

		public static RenderData leftLeg(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.LEFT_LEG);
		}

		public static RenderData rightLeg(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.RIGHT_LEG);
		}

		public static RenderData leftFoot(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.LEFT_FOOT);
		}

		public static RenderData rightFoot(String boneName) {
			return new RenderData(boneName, GeoArmorRenderer.ArmorSegment.RIGHT_FOOT);
		}
	}

	/**
	 * Override to add any custom {@link DataTicket}s you need to capture for rendering.
	 * <p>
	 * The animatable is discarded from the rendering context after this, so any data needed
	 * for rendering should be captured in the renderState provided
	 *
	 * @param animatable The animatable instance being rendered
	 * @param relatedObject An object related to the render pass or null if not applicable.
	 *                         (E.G. ItemStack for GeoItemRenderer, entity instance for GeoReplacedEntityRenderer).
	 * @param renderState The GeckoLib RenderState to add data to, will be passed through the rest of rendering
	 */
	@Override
	public void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {
		final EnumMap<class_1304, class_1799> equipment = renderState.getOrDefaultGeckolibData(DataTickets.EQUIPMENT_BY_SLOT, new EnumMap<>(class_1304.class));

        collectArmorData(renderState, animatable, partialTick, equipment);

		if (!equipment.get(class_1304.field_6174).method_7960()) {
			renderState.addGeckolibData(DataTickets.ELYTRA_ROTATION, new class_243(animatable.field_52447.method_61404(partialTick),
																			  animatable.field_52447.method_61405(partialTick),
																			  animatable.field_52447.method_61406(partialTick)));
		}
	}

    protected <S extends class_10034 & GeoRenderState, A extends class_572<S>> void collectArmorData(
            R baseRenderState, T animatable, float partialTick, EnumMap<class_1304, class_1799> equipment) {
        S headRenderState = getOrCreateHumanoidRenderState(baseRenderState, false);

        equipment.put(class_1304.field_6169, headRenderState.field_55309 = animatable.method_6118(class_1304.field_6169));
        equipment.put(class_1304.field_6174, headRenderState.field_53418 = animatable.method_6118(class_1304.field_6174));
        equipment.put(class_1304.field_6172, headRenderState.field_53419 = animatable.method_6118(class_1304.field_6172));
        equipment.put(class_1304.field_6166, headRenderState.field_53420 = animatable.method_6118(class_1304.field_6166));

        GeoArmorRenderer.captureRenderStates(headRenderState, animatable, partialTick,
                                             (renderState, slot) -> (A)GeckoLibClient.HUMANOID_ARMOR_MODEL.get().method_72959(slot),
                                             slot -> slot == class_1304.field_6169 ? headRenderState : getOrCreateHumanoidRenderState(baseRenderState, true));

        baseRenderState.addGeckolibData(DataTickets.EQUIPMENT_BY_SLOT, equipment);

        if (headRenderState != baseRenderState && headRenderState.hasGeckolibData(DataTickets.PER_SLOT_RENDER_DATA))
            baseRenderState.addGeckolibData(DataTickets.PER_SLOT_RENDER_DATA, headRenderState.getGeckolibData(DataTickets.PER_SLOT_RENDER_DATA));
    }

	/**
	 * Register per-bone render operations, to be rendered after the main model is done.
	 * <p>
	 * Even though the task is called after the main model renders, the {@link class_4587} provided will be posed as if the bone
	 * is currently rendering.
	 *
	 * @param consumer The registrar to accept the per-bone render tasks
	 */
    @Override
    public void addPerBoneRender(RenderPassInfo<R> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
		for (RenderData renderData : getRelevantBones(renderPassInfo)) {
            renderPassInfo.model().getBone(renderData.boneName).filter(CuboidGeoBone.class::isInstance)
                    .ifPresentOrElse(bone -> createPerBoneRender(renderPassInfo, bone, renderData, consumer),
                                     () -> GeckoLibConstants.LOGGER.error("Unable to find bone for ItemArmorGeoLayer: {}, skipping", renderData.boneName));
		}
	}

	private void createPerBoneRender(RenderPassInfo<R> renderPassInfo, GeoBone bone, RenderData renderData, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        GeoArmorRenderer.ArmorSegment armorSegment = renderData.armorSegment;
		class_1799 stack = getEquipmentStack(renderPassInfo, bone, armorSegment.equipmentSlot);

		if (!stack.method_7960()) {
			consumer.accept(bone, (renderPassInfo2, bone2, renderTasks) ->
					buildRenderTask(renderPassInfo2, armorSegment.equipmentSlot, armorSegment.modelPartGetter, stack, (CuboidGeoBone)bone2, renderTasks));
		}
	}

	/**
	 * Perform the actual rendering operation for the given bone and equipment
	 */
	protected void buildRenderTask(RenderPassInfo<R> renderPassInfo, class_1304 slot, Function<class_572<?>, class_630> modelPartFactory, class_1799 equipmentStack, CuboidGeoBone bone,
                                   class_11659 renderTasks) {
		// TODO Rewrite this Geo layer to make work :(
		if (true || equipmentStack.method_7960())
			return;

        final class_4587 poseStack = renderPassInfo.poseStack();
        final R renderState = renderPassInfo.renderState();

		if (equipmentStack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2190 skullBlock) {
            renderSkullAsArmor(renderPassInfo, bone, equipmentStack, skullBlock, renderTasks);
		}
        else if (RenderUtil.getGeckoLibArmorRenderer(equipmentStack, slot) instanceof GeoArmorRenderer geoArmorRenderer) {
            EnumMap<class_1304, R> perSlotData = renderState.getGeckolibData(DataTickets.PER_SLOT_RENDER_DATA);

            if (perSlotData != null) {
                R slotRenderState = perSlotData.get(slot);

                if (slotRenderState != null) {
                    poseStack.method_22903();
                    poseStack.method_22905(-1, -1, 1);

                    GeoRenderState humanoidRenderState = getOrCreateHumanoidRenderState(slotRenderState, false);
                    RenderPassInfo.BoneUpdater<R> boneUpdater = positionModelPartFromBone(poseStack, bone, modelPartFactory.apply(slotRenderState.getGeckolibData(DataTickets.HUMANOID_MODEL)));

                    renderPassInfo.addBoneUpdater(boneUpdater);

                    geoArmorRenderer.performRenderPass(humanoidRenderState, poseStack, renderTasks, renderPassInfo.cameraState());
                    poseStack.method_22909();
                }
            }
        }
		else {
			class_3879<?> vanillaModel = getArmorModelForRender(bone, slot, equipmentStack, renderState);
			class_630 modelPart = vanillaModel instanceof class_572<?> humanoidModel ? modelPartFactory.apply(humanoidModel) : vanillaModel.method_63512();

			if (!modelPart.field_3663.isEmpty()) {
				poseStack.method_22903();
				poseStack.method_22905(-1, -1, 1);

                class_10192 equippable = equipmentStack.method_58694(class_9334.field_54196);

                if (equippable != null) {
                    equippable.comp_3176().ifPresent(assetId -> {
                        positionModelPartFromBone(poseStack, bone, modelPart);
                        renderVanillaArmorPiece(renderPassInfo, poseStack, bone, slot, equipmentStack, equippable, assetId, vanillaModel, modelPart, renderTasks);
                    });
                }

				poseStack.method_22909();
			}
		}
	}

	/**
	 * Helper method to retrieve a stored held or worn ItemStack by the slot it's in, as computed in {@link GeoRenderLayer#addRenderData(GeoAnimatable, Object, GeoRenderState, float)}
	 */
	protected class_1799 getEquipmentStack(RenderPassInfo<R> renderPassInfo, GeoBone bone, class_1304 slot) {
		return (class_1799)renderPassInfo.getGeckolibData(DataTickets.EQUIPMENT_BY_SLOT).getOrDefault(slot, class_1799.field_8037);
	}

	/**
	 * Get the LayerType for the given armor piece. This defines the asset type to use in rendering a vanilla armor piece.
	 */
	protected class_10186.class_10190 getEquipmentLayerType(RenderPassInfo<R> renderPassInfo, GeoBone bone, class_1304 slot, class_1799 armorStack, class_5321<class_10394> assetId) {
		if (slot == class_1304.field_6172)
			return class_10186.class_10190.field_54126;

		if (slot == class_1304.field_6174 && !this.equipmentAssets.method_64087(assetId).method_63996(class_10186.class_10190.field_54127).isEmpty())
			return class_10186.class_10190.field_54127;

		return class_10186.class_10190.field_54125;
	}

	/**
	 * Renders an individual armor piece base on the given {@link GeoBone} and {@link class_1799}
	 */
	protected void renderVanillaArmorPiece(RenderPassInfo<R> renderPassInfo, class_4587 poseStack, GeoBone bone, class_1304 slot, class_1799 armorStack,
										   class_10192 equippable, class_5321<class_10394> assetId, class_3879<?> model, class_630 modelPart, class_11659 renderTasks) {
		class_10186.class_10190 layerType = getEquipmentLayerType(renderPassInfo, bone, slot, armorStack, assetId);
		class_3879 modelToRender = model;

		if (layerType == class_10186.class_10190.field_54127) {
			if (model instanceof class_572 humanoidModel && modelPart != humanoidModel.field_3391)
				return;

			modelToRender = checkForElytraModel(renderPassInfo, layerType, bone, poseStack);
		}

		this.equipmentRenderer.method_64077(layerType, assetId, modelToRender, renderPassInfo.renderState(), armorStack, poseStack, renderTasks, renderPassInfo.packedLight(), renderPassInfo.renderState().field_61821);
	}

	/**
	 * Check for the presence of {@link class_563 Elytra} wings, and adjust the model as necessary
	 */
	protected class_3879 checkForElytraModel(RenderPassInfo<R> renderPassInfo, class_10186.class_10190 layerType, GeoBone bone, class_4587 poseStack) {
		class_563 model = GeckoLibClient.GENERIC_ELYTRA_MODEL.get();
		class_10034 humanoidRenderState = new class_10034();
        R renderState = renderPassInfo.renderState();
		class_243 elytraRotation = renderState.getOrDefaultGeckolibData(DataTickets.ELYTRA_ROTATION, class_243.field_1353);
		humanoidRenderState.field_53410 = renderState.getOrDefaultGeckolibData(DataTickets.IS_CROUCHING, false);
		humanoidRenderState.field_53415 = (float)elytraRotation.field_1352;
		humanoidRenderState.field_53416 = (float)elytraRotation.field_1351;
		humanoidRenderState.field_53417 = (float)elytraRotation.field_1350;

		model.method_17079(humanoidRenderState);
		poseStack.method_46416(0, -1.5f, 0.125f);

		return model;
	}

	/**
	 * Returns a cached instance of a base HumanoidModel that is used for rendering/modelling the provided {@link class_1799}
	 */
	@ApiStatus.Internal
	protected <S extends class_10034 & GeoRenderState> class_3879<?> getArmorModelForRender(GeoBone bone, class_1304 slot, class_1799 stack, R renderState) {
		final S humanoidRenderState = renderState instanceof class_10034 humanoidRenderState1 ? (S)humanoidRenderState1 : (S)new class_10034();
		final class_10186.class_10190 layerType = slot == class_1304.field_6172 ? class_10186.class_10190.field_54126 : class_10186.class_10190.field_54125;
		final class_572 defaultModel = GeckoLibClient.HUMANOID_ARMOR_MODEL.get().method_72959(slot);

		return GeckoLibClientServices.ITEM_RENDERING.getArmorModelForItem(humanoidRenderState, stack, slot, layerType, defaultModel);
	}

	/**
	 * Render a given {@link class_2190} as a worn armor piece in relation to a given {@link GeoBone}
	 */
	protected void renderSkullAsArmor(RenderPassInfo<R> renderPassInfo, GeoBone bone, class_1799 stack, class_2190 skullBlock, class_11659 renderTasks) {
		class_2484.class_2485 type = skullBlock.method_9327();
		class_5598 model = this.skullModels.apply(type);

		if (model == null)
			return;

		class_9296 profile = stack.method_58694(class_9334.field_49617);
		class_1921 renderType = profile == null ? class_11786.field_62215 : this.skinCache.method_73495(profile).method_73504();
		class_4587 poseStack = renderPassInfo.poseStack();

		poseStack.method_22903();
		RenderUtil.translateAndRotateMatrixForBone(poseStack, bone);
		poseStack.method_22905(1.1875f, 1.1875f, 1.1875f);
		poseStack.method_46416(-0.5f, 0, -0.5f);

		class_836.method_72958(null, 0, 0, poseStack, renderTasks, renderPassInfo.packedLight(), model, renderType, renderPassInfo.renderState().field_61821, null);
		poseStack.method_22909();
	}

	/**
	 * Prepares the given {@link class_630} for render by setting its translation, position, and rotation values based on the provided {@link GeoBone}
	 * <p>
	 * This implementation uses the <b><u>FIRST</u></b> cube in the source part
	 * to determine the scale and position of the GeoArmor to be rendered
	 *
	 * @param poseStack The PoseStack being used for rendering
	 * @param bone The GeoBone to base the translations on
	 * @param sourcePart The ModelPart to translate
	 */
	protected RenderPassInfo.BoneUpdater<R> positionModelPartFromBone(class_4587 poseStack, CuboidGeoBone bone, class_630 sourcePart) {
		final GeoCube firstCube = bone.cubes[0];
		final class_628 armorCube = sourcePart.field_3663.getFirst();
		final double armorBoneSizeX = firstCube.size().method_10216();
		final double armorBoneSizeY = firstCube.size().method_10214();
		final double armorBoneSizeZ = firstCube.size().method_10215();
		final double actualArmorSizeX = Math.abs(armorCube.field_3648 - armorCube.field_3645);
		final double actualArmorSizeY = Math.abs(armorCube.field_3647 - armorCube.field_3644);
		final double actualArmorSizeZ = Math.abs(armorCube.field_3646 - armorCube.field_3643);
		float scaleX = (float)(armorBoneSizeX / actualArmorSizeX);
		float scaleY = (float)(armorBoneSizeY / actualArmorSizeY);
		float scaleZ = (float)(armorBoneSizeZ / actualArmorSizeZ);
        final RenderPassInfo.BoneUpdater<R> modelPositioner = (renderPassInfo, snapshots) -> {
            final BoneSnapshot snapshot = snapshots.get(bone);

            sourcePart.method_2851(-(bone.pivotX() - ((bone.pivotX() * scaleX) - bone.pivotX()) / scaleX),
                              -(bone.pivotY() - ((bone.pivotY() * scaleY) - bone.pivotY()) / scaleY),
                              (bone.pivotZ() - ((bone.pivotZ() * scaleZ) - bone.pivotZ()) / scaleZ));

            sourcePart.field_3654 = -snapshot.getRotX();
            sourcePart.field_3675 = -snapshot.getRotY();
            sourcePart.field_3674 = snapshot.getRotZ();
        };

		poseStack.method_22905(scaleX, scaleY, scaleZ);

        return modelPositioner;
	}

    /**
     * Convert an existing RenderState to a HumanoidRenderState, either by casting or creating a new one, for the purposes of RenderState filling
     */
    protected <S extends class_10034 & GeoRenderState> S getOrCreateHumanoidRenderState(R renderState, boolean forceNew) {
        S newState = (S)(!forceNew && renderState instanceof class_10034 state ? state : new class_10034());

        if (newState != renderState)
            RenderStateUtil.makeMinimalArmorRenderingClone(newState, renderState);

        return newState;
    }
}
