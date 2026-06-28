package site.s9lab.s9labclient.client.cosmetics.shield;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public final class ShieldEntityModel extends EntityModel<PlayerEntityRenderState> {
    private final ModelPart shield;

    public ShieldEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.shield = root.getChild("shield");
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();
        ModelPartData shield = modelData.getRoot().addChild("shield", ModelPartBuilder.create(), ModelTransform.NONE);

        // Arm-mounted kite shield. It is intentionally neutral so texture swaps
        // can create Void/S9Lab/Gold variants without changing the geometry.
        shield.addChild(
                "plate",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-0.50F, -6.05F, -4.0F, 1.0F, 10.0F, 8.0F, new Dilation(0.02F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "top_rail",
                ModelPartBuilder.create()
                        .uv(0, 14)
                        .cuboid(-0.65F, -6.6F, -4.55F, 1.3F, 0.7F, 9.1F, new Dilation(0.03F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "left_rail",
                ModelPartBuilder.create()
                        .uv(0, 22)
                        .cuboid(-0.68F, -5.9F, -4.85F, 1.36F, 8.4F, 0.65F, new Dilation(0.03F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "right_rail",
                ModelPartBuilder.create()
                        .uv(0, 22)
                        .cuboid(-0.68F, -5.9F, 4.2F, 1.36F, 8.4F, 0.65F, new Dilation(0.03F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "point",
                ModelPartBuilder.create()
                        .uv(14, 22)
                        .cuboid(-0.58F, 2.15F, -2.55F, 1.16F, 4.35F, 5.1F, new Dilation(0.02F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "core",
                ModelPartBuilder.create()
                        .uv(24, 0)
                        .cuboid(-0.72F, -3.8F, -2.35F, 0.34F, 7.7F, 4.7F, new Dilation(0.01F)),
                ModelTransform.NONE
        );
        shield.addChild(
                "handle",
                ModelPartBuilder.create()
                        .uv(24, 8)
                        .cuboid(-1.02F, -1.85F, -1.25F, 0.48F, 3.7F, 2.5F, new Dilation(0.0F)),
                ModelTransform.NONE
        );

        return TexturedModelData.of(modelData, 64, 64).createModel();
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        this.shield.pitch = 0.0F;
        this.shield.yaw = 0.0F;
        this.shield.roll = 0.0F;
    }
}
