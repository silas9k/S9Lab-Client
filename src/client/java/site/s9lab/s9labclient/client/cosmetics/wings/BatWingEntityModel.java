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

public final class BatWingEntityModel extends EntityModel<PlayerEntityRenderState> {
    private static final float WING_SCALE = 1.20F;

    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;

    public BatWingEntityModel(ModelPart root) {
        super(root, RenderLayers::entityTranslucent);

        this.body = root.getChild("body");

        this.rightWing = this.body.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");

        this.leftWing = this.body.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
    }

    public static ModelPart createModelPart() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData body = root.addChild("body", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 10.0F, -0.5F));

        ModelPartData right = body.addChild(
                "right_wing",
                ModelPartBuilder.create().uv(12, 0).cuboid(-2.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F),
                ModelTransform.origin(-0.4F, 0.0F, 0.0F)
        );
        right.addChild(
                "right_wing_tip",
                ModelPartBuilder.create().uv(16, 0).cuboid(-6.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F),
                ModelTransform.origin(-2.0F, 0.0F, 0.0F)
        );

        ModelPartData left = body.addChild(
                "left_wing",
                ModelPartBuilder.create().uv(12, 7).cuboid(0.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F),
                ModelTransform.origin(0.4F, 0.0F, 0.0F)
        );
        left.addChild(
                "left_wing_tip",
                ModelPartBuilder.create().uv(16, 8).cuboid(0.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F),
                ModelTransform.origin(2.0F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(data, 32, 32).createModel();
    }

@Override
public void setAngles(PlayerEntityRenderState state) {
    this.body.xScale = WING_SCALE;
    this.body.yScale = WING_SCALE;
    this.body.zScale = WING_SCALE;

    float movement = Math.min(1.0F, state.limbSwingAmplitude * 1.6F);

    float time = state.age * 0.10F;

    float flap = MathHelper.sin(time) * (0.18F + movement * 0.10F);
    float spread = 0.48F + flap;

    this.rightWing.yaw = spread;
    this.leftWing.yaw = -spread;

    this.rightWingTip.yaw = spread * 0.72F;
    this.leftWingTip.yaw = -spread * 0.72F;

    this.rightWing.roll = 0.08F;
    this.leftWing.roll = -0.08F;
}
}