package site.s9lab.s9labclient.client.cosmetics.shoulder;

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

public class ShoulderBuddyEntityModel extends EntityModel<PlayerEntityRenderState> {
    private final ModelPart buddy;

    public ShoulderBuddyEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.buddy = root.getChild("buddy");
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();
        ModelPartData buddy = modelData.getRoot().addChild("buddy", ModelPartBuilder.create(), ModelTransform.NONE);
        buddy.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.01F)), ModelTransform.NONE);
        buddy.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, -16.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.01F)), ModelTransform.NONE);
        buddy.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(4.0F, -15.0F, -2.0F, 4.0F, 11.0F, 4.0F, new Dilation(0.01F)), ModelTransform.rotation(0.0F, 0.0F, -0.25F));
        buddy.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-8.0F, -15.0F, -2.0F, 4.0F, 11.0F, 4.0F, new Dilation(0.01F)), ModelTransform.rotation(0.0F, 0.0F, 0.25F));
        buddy.addChild("left_leg", ModelPartBuilder.create().uv(16, 48).cuboid(0.0F, -4.0F, -2.0F, 4.0F, 10.0F, 4.0F, new Dilation(0.01F)), ModelTransform.NONE);
        buddy.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, -4.0F, -2.0F, 4.0F, 10.0F, 4.0F, new Dilation(0.01F)), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64).createModel();
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        this.buddy.yaw = (float) Math.sin(state.age * 0.04F) * 0.12F;
        this.buddy.pitch = (float) Math.sin(state.age * 0.07F) * 0.04F;
    }
}
