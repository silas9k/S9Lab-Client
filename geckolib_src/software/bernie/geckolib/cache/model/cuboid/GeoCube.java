package software.bernie.geckolib.cache.model.cuboid;

import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.cache.model.GeoQuad;
import software.bernie.geckolib.util.RenderUtil;

/**
 * Baked cuboid for a {@link GeoBone}
 *
 * @param quads The quad array for this cube, pre-sorted to render in correct order
 * @param pivot The pivot point of this cube
 * @param rotation The baked rotation value of this cube
 * @param size The x/y/z dimensions of this cube
 */
public record GeoCube(@Nullable GeoQuad[] quads, class_243 pivot, class_243 rotation, class_243 size) {
    /**
     * Submit this cuboid's quads to the vertex consumer
     */
    public void render(class_4587 poseStack, class_4588 vertexConsumer, int packedLight, int packedOverlay, int renderColor) {
        translateToPivotPoint(poseStack);
        rotate(poseStack);
        translateAwayFromPivotPoint(poseStack);

        Matrix3f normalisedPoseState = poseStack.method_23760().method_23762();
        Matrix4f poseState = new Matrix4f(poseStack.method_23760().method_23761());

        for (GeoQuad quad : this.quads) {
            if (quad == null)
                continue;

            Vector3f normal = normalisedPoseState.transform(quad.normalVec());

            RenderUtil.fixInvertedFlatCube(this, normal);
            quad.render(poseState, normal, vertexConsumer, packedLight, packedOverlay, renderColor);
        }

    }

    /**
     * Apply a rotation to the provided PoseStack by this cube's rotation values
     */
    public void rotate(class_4587 poseStack) {
        final class_243 rotation = rotation();

        poseStack.method_22907(new Quaternionf().rotationXYZ(0, 0, (float)rotation.method_10215()));
        poseStack.method_22907(new Quaternionf().rotationXYZ(0, (float)rotation.method_10214(), 0));
        poseStack.method_22907(new Quaternionf().rotationXYZ((float)rotation.method_10216(), 0, 0));
    }

    /**
     * Apply a translation to the provided PoseStack to this cube's pivot point
     */
    public void translateToPivotPoint(class_4587 poseStack) {
        poseStack.method_22904(pivot().method_10216() / 16f, pivot().method_10214() / 16f, pivot().method_10215() / 16f);
    }

    /**
     * Apply a translation to the provided PoseStack away from this cube's pivot point
     */
    public void translateAwayFromPivotPoint(class_4587 poseStack) {
        poseStack.method_22904(-pivot().method_10216() / 16f, -pivot().method_10214() / 16f, -pivot().method_10215() / 16f);
    }
}
