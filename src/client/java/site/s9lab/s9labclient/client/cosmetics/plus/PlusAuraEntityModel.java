package site.s9lab.s9labclient.client.cosmetics.plus;

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

public final class PlusAuraEntityModel extends EntityModel<PlayerEntityRenderState> {
    private final ModelPart lowerRing;
    private final ModelPart upperRing;
    private final ModelPart sparks;
    private float speed = 1.0F;
    private float pulse = 1.0F;

    public PlusAuraEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.lowerRing = root.getChild("lower_ring");
        this.upperRing = root.getChild("upper_ring");
        this.sparks = root.getChild("sparks");
    }

    public void configure(float speed, float pulse) {
        this.speed = Math.max(0.15F, speed);
        this.pulse = Math.max(0.0F, pulse);
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData lower = root.addChild("lower_ring", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartData upper = root.addChild("upper_ring", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartData sparks = root.addChild("sparks", ModelPartBuilder.create(), ModelTransform.NONE);

        addRing(lower, 8.2F, 5.6F);
        addRing(upper, 4.8F, 7.2F);
        addSpark(sparks, "spark_front", -0.6F, 2.3F, -7.0F);
        addSpark(sparks, "spark_back", -0.6F, 12.4F, 6.7F);
        addSpark(sparks, "spark_left", -7.0F, 8.4F, -0.6F);
        addSpark(sparks, "spark_right", 6.7F, 4.8F, -0.6F);

        return TexturedModelData.of(modelData, 32, 32).createModel();
    }

    private static void addRing(ModelPartData parent, float radius, float y) {
        float thickness = 0.34F;
        addSegment(parent, "front_" + y, -radius, y, -radius - thickness, radius * 2.0F, thickness, thickness);
        addSegment(parent, "back_" + y, -radius, y, radius, radius * 2.0F, thickness, thickness);
        addSegment(parent, "left_" + y, -radius - thickness, y, -radius, thickness, thickness, radius * 2.0F);
        addSegment(parent, "right_" + y, radius, y, -radius, thickness, thickness, radius * 2.0F);
    }

    private static void addSegment(
            ModelPartData parent,
            String name,
            float x,
            float y,
            float z,
            float sizeX,
            float sizeY,
            float sizeZ
    ) {
        parent.addChild(
                name,
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(x, y, z, sizeX, sizeY, sizeZ, new Dilation(0.04F)),
                ModelTransform.NONE
        );
    }

    private static void addSpark(ModelPartData parent, String name, float x, float y, float z) {
        parent.addChild(
                name,
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(x, y, z, 1.2F, 1.2F, 1.2F, new Dilation(0.02F)),
                ModelTransform.NONE
        );
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        float time = state.age * 0.055F * speed;
        float wave = (float) Math.sin(state.age * 0.08F * speed) * 0.18F * pulse;
        lowerRing.yaw = time;
        lowerRing.pitch = 0.08F + wave * 0.14F;
        lowerRing.roll = wave * 0.06F;

        upperRing.yaw = -time * 1.35F;
        upperRing.pitch = 0.74F + wave * 0.18F;
        upperRing.roll = -0.42F + wave * 0.1F;

        sparks.yaw = time * 2.2F;
        sparks.pitch = wave * 0.08F;
        sparks.roll = (float) Math.cos(state.age * 0.06F * speed) * 0.12F * pulse;
    }
}
