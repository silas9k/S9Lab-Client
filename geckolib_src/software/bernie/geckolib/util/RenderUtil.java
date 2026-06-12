package software.bernie.geckolib.util;

import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.cache.model.GeoQuad;
import software.bernie.geckolib.cache.model.cuboid.GeoCube;
import software.bernie.geckolib.renderer.*;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

import java.util.List;
import net.minecraft.class_10192;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2591;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9334;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtil {
    /**
     * Transform a PoseStack to match a bone's render position.
     * <p>
     * Can only be used inside a {@link RenderPassInfo#renderPosed} call
     */
    public static void transformToBone(class_4587 poseStack, GeoBone bone) {
        final List<GeoBone> boneQueue = new ObjectArrayList<>();
        GeoBone parent = bone;

        boneQueue.add(bone);

        while ((parent = parent.parent()) != null) {
            boneQueue.add(parent);
        }

        for (GeoBone bone2 : boneQueue.reversed()) {
            prepMatrixForBone(poseStack, bone2);
        }

        bone.translateToPivotPoint(poseStack);
    }

	public static void translateAndRotateMatrixForBone(class_4587 poseStack, GeoBone bone) {
        bone.translateToPivotPoint(poseStack);

        float xRot = bone.baseRotX();
        float yRot = bone.baseRotY();
        float zRot = bone.baseRotZ();

        if (bone.frameSnapshot != null) {
            xRot += bone.frameSnapshot.getRotX();
            yRot += bone.frameSnapshot.getRotY();
            zRot += bone.frameSnapshot.getRotZ();
        }

        if (zRot != 0)
            poseStack.method_22907(class_7833.field_40718.rotation(zRot));

        if (yRot != 0)
            poseStack.method_22907(class_7833.field_40716.rotation(yRot));

        if (xRot != 0)
            poseStack.method_22907(class_7833.field_40714.rotation(xRot));
	}

    /**
     * Make the necessarily manipulations of the {@link class_4587} to position a bone based on its current snapshot and state
     */
	public static void prepMatrixForBone(class_4587 poseStack, GeoBone bone) {
        prepMatrixForBoneAndUpdateListeners(poseStack, bone, null);
	}

    /**
     * Make the necessarily manipulations of the {@link class_4587} to position a bone based on its current snapshot and state
     * <p>
     * Additionally update the RenderPassInfo's {@link RenderPassInfo.BonePositionListener}s, if applicable
     */
	public static void prepMatrixForBoneAndUpdateListeners(class_4587 poseStack, GeoBone bone, @Nullable RenderPassInfo<?> renderPassInfo) {
        if (bone.frameSnapshot != null)
            bone.frameSnapshot.translate(poseStack);

        translateAndRotateMatrixForBone(poseStack, bone);

        if (bone.frameSnapshot != null)
            bone.frameSnapshot.scale(poseStack);

        if (renderPassInfo != null)
            bone.updateBonePositionListeners(poseStack, renderPassInfo);

        bone.translateAwayFromPivotPoint(poseStack);
	}

    /**
     * Convert a {@link Matrix4fc} pose to a three-dimensional vector position, multiplying it by input values
     * to allow for inline transformations
     */
    public static class_243 renderPoseToPosition(Matrix4fc pose, float xScale, float yScale, float zScale) {
        final Vector4f position = pose.transform(new Vector4f(0, 0, 0, 1));

        return new class_243(position.x() * xScale, position.y() * yScale, position.z() * zScale);
    }

    /**
     * Extract the relative pose of an input matrix from a base matrix
     */
	public static Matrix4f extractPoseFromRoot(Matrix4fc baseMatrix, Matrix4f inputMatrix) {
		inputMatrix = new Matrix4f(inputMatrix);
		
		inputMatrix.invert();
		inputMatrix.mul(baseMatrix);

		return inputMatrix;
	}

    /**
     * Directly translate a Matrix pose by a given position
     */
    public static Matrix4f addPosToMatrix(Matrix4f baseMatrix, class_243 pos) {
        baseMatrix.m30(baseMatrix.m30() + (float)pos.field_1352)
                .m31(baseMatrix.m31() + (float)pos.field_1351)
                .m32(baseMatrix.m32() + (float)pos.field_1350);

        return baseMatrix;
    }
	
	/**
     * Translates the provided {@link class_4587} to face towards the given {@link class_1297}'s rotation
	 * <p>
     * Usually used for rotating projectiles towards their trajectory, in an {@link GeoRenderer#preRenderPass} override
	 */
	public static void faceRotation(class_4587 poseStack, class_1297 animatable, float partialTick) {
		poseStack.method_22907(class_7833.field_40716.rotationDegrees(class_3532.method_16439(partialTick, animatable.field_5982, animatable.method_36454()) - 90));
		poseStack.method_22907(class_7833.field_40718.rotationDegrees(class_3532.method_16439(partialTick, animatable.field_6004, animatable.method_36455())));
	}

	/**
	 * Add a positional vector to a matrix
	 * <p>
	 * This is specifically implemented to act as a translation of an x/y/z coordinate triplet to a render matrix
	 */
	public static Matrix4f translateMatrix(Matrix4f matrix, Vector3f vector) {
		return matrix.add(new Matrix4f().m30(vector.x).m31(vector.y).m32(vector.z));
	}
	
	/**
	 * Gets the actual dimensions of a texture resource from a given path
	 *
	 * @param texture The path of the texture resource to check
	 * @return The dimensions (width x height) of the texture
	 */
	public static IntIntPair getTextureDimensions(class_2960 texture) {
		GpuTexture gpuTexture = class_310.method_1551().method_1531().method_4619(texture).method_68004();

		return IntIntPair.of(gpuTexture.getWidth(0), gpuTexture.getHeight(0));
	}

	/**
	 * If a {@link GeoCube} is a 2d plane and the {@link GeoQuad Quad's} normal is inverted on an intersecting plane,
     * it can cause issues with shaders and other lighting tasks
	 * <p>
	 * This performs a pseudo-ABS function to help resolve some of those issues
	 */
	public static void fixInvertedFlatCube(GeoCube cube, Vector3f normal) {
		if (normal.x() < 0 && (cube.size().method_10214() == 0 || cube.size().method_10215() == 0))
			normal.mul(-1, 1, 1);

		if (normal.y() < 0 && (cube.size().method_10216() == 0 || cube.size().method_10215() == 0))
			normal.mul(1, -1, 1);

		if (normal.z() < 0 && (cube.size().method_10216() == 0 || cube.size().method_10214() == 0))
			normal.mul(1, 1, -1);
	}

	/**
	 * Helper method to create the glowmask resource location for a given input texture
	 */
	public static class_2960 getEmissiveResource(class_2960 textureLocation) {
		return textureLocation.method_45134(path -> path.replace(".png", "_glowmask.png"));
	}

    /**
     * Gets a registered {@link GeoReplacedEntityRenderer} for a given {@link class_1297} if it has had its renderer replaced
     *
     * @param entityType The {@link class_1299} to retrieve the replaced renderer for
     * @return The GeckoLib replaced renderer for the given entity, or null if not applicable
     */
    public static @Nullable GeoReplacedEntityRenderer<?, ?, ?> getReplacedEntityRenderer(class_1299<?> entityType) {
        return class_310.method_1551().method_1561().field_4696.get(entityType) instanceof GeoReplacedEntityRenderer<?, ?, ?> replacedEntityRenderer ? replacedEntityRenderer : null;
    }

    /**
     * Gets a registered {@link GeoItemRenderer} for a given {@link class_1792}, if applicable
     *
     * @param item The item to retrieve the renderer for
     * @return The GeoItemRenderer instance, or null if not applicable
     */
    public static @Nullable GeoItemRenderer<?> getGeckoLibItemRenderer(class_1792 item) {
        return GeoRenderProvider.of(item).getGeoItemRenderer();
    }

    /**
     * Gets a registered {@link GeoEntityRenderer} for a given {@link class_1299}, if applicable
     *
     * @param entityType The {@code EntityType} to retrieve the renderer for
     * @return The {@code GeoEntityRenderer} instance, or null if not applicable
     */
    public static @Nullable GeoEntityRenderer<?, ?> getGeckoLibEntityRenderer(class_1299<?> entityType) {
        return class_310.method_1551().method_1561().field_4696.get(entityType) instanceof GeoEntityRenderer<?, ?> geoEntityRenderer ? geoEntityRenderer : null;
    }

    /**
     * Gets a registered {@link GeoBlockRenderer} for a given {@link class_2591}, if applicable
     *
     * @param blockEntityType The {@code BlockEntityType} to retrieve the renderer for
     * @return The {@code GeoBlockRenderer} instance, or null if not applicable
     */
    public static @Nullable GeoBlockRenderer<?, ?> getGeckoLibBlockRenderer(class_2591<?> blockEntityType) {
        return class_310.method_1551().method_31975().field_4345.get(blockEntityType) instanceof GeoBlockRenderer<?, ?> geoBlockRenderer ? geoBlockRenderer : null;
    }

    /**
     * Gets a registered {@link GeoArmorRenderer} for a given {@link class_1792}, if applicable
     *
     * @param item The {@code Item} to retrieve the renderer for
     * @return The {@code GeoArmorRenderer} instance, or null if not applicable
     * @see #getGeckoLibArmorRenderer(class_1799, class_1304)
     */
    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    public static @Nullable GeoArmorRenderer<?, ?> getGeckoLibArmorRenderer(class_1792 item) {
        final class_1799 stack = item.method_7854();
        final class_10192 equippable = stack.method_58695(class_9334.field_54196, null);

        if (equippable == null)
            return null;

        return getGeckoLibArmorRenderer(stack, equippable.comp_3174());
    }

    /**
     * Gets a registered {@link GeoArmorRenderer} for a given {@link class_1799}, if applicable
     *
     * @param stack The {@code ItemStack} to retrieve the renderer for
     * @param slot The {@link class_1304} to retrieve the renderer for
     * @return The {@code GeoArmorRenderer} instance, or null if not applicable
     */
    public static @Nullable GeoArmorRenderer<?, ?> getGeckoLibArmorRenderer(class_1799 stack, class_1304 slot) {
        return GeoRenderProvider.of(stack).getGeoArmorRenderer(stack, slot);
    }

    /**
     * Gets a GeoAnimatable instance that has been registered as the replacement renderer for a given {@link class_1299}
     *
     * @param entityType The {@code EntityType} to retrieve the replaced {@link GeoAnimatable} for
     * @return The {@code GeoAnimatable} instance, or null if one isn't found
     */
    public static @Nullable GeoAnimatable getReplacedAnimatable(class_1299<?> entityType) {
        final GeoReplacedEntityRenderer<?, ?, ?> replacedEntityRenderer = getReplacedEntityRenderer(entityType);

        return replacedEntityRenderer == null ? null : replacedEntityRenderer.getAnimatable();
    }

    private RenderUtil() {}
}
