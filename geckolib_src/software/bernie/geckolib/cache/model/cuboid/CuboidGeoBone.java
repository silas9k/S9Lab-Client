package software.bernie.geckolib.cache.model.cuboid;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Implementation of GeoBone for cuboid rendering
 */
public final class CuboidGeoBone extends GeoBone {
    public final GeoCube[] cubes;

    public CuboidGeoBone(@Nullable GeoBone parent, String name, GeoBone[] children, GeoCube[] cubes, float pivotX, float pivotY, float pivotZ, float rotX, float rotY, float rotZ) {
        super(parent, name, children, pivotX, pivotY, pivotZ, rotX, rotY, rotZ);

        this.cubes = cubes;
    }

    @Override
    public <R extends GeoRenderState> void render(RenderPassInfo<R> renderPassInfo, class_4587 poseStack, class_4588 vertexConsumer, int packedLight, int packedOverlay, int renderColor) {
        if (this.frameSnapshot == null || !this.frameSnapshot.isHidden()) {
            for (GeoCube cube : this.cubes) {
                poseStack.method_22903();
                cube.render(poseStack, vertexConsumer, packedLight, packedOverlay, renderColor);
                poseStack.method_22909();
            }
        }
    }
}
