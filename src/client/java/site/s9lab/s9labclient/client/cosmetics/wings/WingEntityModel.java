package site.s9lab.s9labclient.client.cosmetics.wings;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.MathHelper;

public class WingEntityModel extends EntityModel<PlayerEntityRenderState> {
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;

    public WingEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.leftWing = root.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.rightWing = root.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData leftWing = root.addChild(
                "left_wing",
                ModelPartBuilder.create()
                        .mirrored()
                        .cuboid("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88)
                        .cuboid("upper_finger", 8.0F, -2.0F, -1.5F, 42, 3, 3, 112, 100)
                        .cuboid("lower_finger", 10.0F, 8.0F, 0.5F, 38, 3, 3, 112, 108)
                        .cuboid("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
                ModelTransform.origin(0.0F, 0.0F, 0.0F)
        );
        leftWing.addChild(
                "left_wing_tip",
                ModelPartBuilder.create()
                        .mirrored()
                        .cuboid("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136)
                        .cuboid("tip_finger", 10.0F, 7.0F, -1.0F, 42, 3, 3, 112, 152)
                        .cuboid("tip_claw", 52.0F, -3.0F, -3.0F, 6, 6, 6, 176, 136)
                        .cuboid("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
                ModelTransform.origin(56.0F, 0.0F, 0.0F)
        );
        ModelPartData rightWing = root.addChild(
                "right_wing",
                ModelPartBuilder.create()
                        .cuboid("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88)
                        .cuboid("upper_finger", -50.0F, -2.0F, -1.5F, 42, 3, 3, 112, 100)
                        .cuboid("lower_finger", -48.0F, 8.0F, 0.5F, 38, 3, 3, 112, 108)
                        .cuboid("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
                ModelTransform.origin(0.0F, 0.0F, 0.0F)
        );
        rightWing.addChild(
                "right_wing_tip",
                ModelPartBuilder.create()
                        .cuboid("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136)
                        .cuboid("tip_finger", -52.0F, 7.0F, -1.0F, 42, 3, 3, 112, 152)
                        .cuboid("tip_claw", -58.0F, -3.0F, -3.0F, 6, 6, 6, 176, 136)
                        .cuboid("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
                ModelTransform.origin(-56.0F, 0.0F, 0.0F)
        );
        return TexturedModelData.of(modelData, 256, 256).createModel();
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        float t = state.age * 0.010F * MathHelper.TAU;
        float idle = MathHelper.sin(t) * 0.22F;
        float pulse = MathHelper.sin(t * 1.7F + 0.8F) * 0.07F;

        float roll = -0.64F + idle + pulse;
        float tipRoll = 0.55F + MathHelper.sin(t + 1.15F) * 0.24F;
        float pitch = 0.09F + MathHelper.cos(t * 0.8F) * 0.05F;

        this.leftWing.pitch  = pitch;
        this.leftWing.yaw    = -0.28F;
        this.leftWing.roll   = roll;
        this.leftWingTip.roll = tipRoll;

        this.rightWing.pitch  = pitch;
        this.rightWing.yaw    =  0.28F;
        this.rightWing.roll   = -roll;
        this.rightWingTip.roll = -tipRoll;
    }
}
