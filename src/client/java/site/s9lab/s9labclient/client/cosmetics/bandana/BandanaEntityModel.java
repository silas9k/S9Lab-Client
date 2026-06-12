package site.s9lab.s9labclient.client.cosmetics.bandana;

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

public class BandanaEntityModel extends EntityModel<PlayerEntityRenderState> {
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0F);

    private final ModelPart bandana;

    public BandanaEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.bandana = root.getChild("bandana");
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();

        ModelPartData bandana = modelData.getRoot().addChild(
                "bandana",
                ModelPartBuilder.create(),
                ModelTransform.NONE
        );

        /*
         * SAFE 512x256 VERSION
         *
         * Alle Teile nutzen erstmal denselben bemalten UV-Bereich oben links.
         * Dadurch werden keine Teile schwarz/leer, solange deine PNG hauptsächlich dort bemalt ist.
         *
         * Wenn du später eine saubere vollständige UV-Map malst,
         * können wir die UVs wieder trennen.
         */

        bandana.addChild(
                "front",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -4.45F,
                                -7.20F,
                                -4.72F,
                                8.9F,
                                1.25F,
                                0.62F,
                                new Dilation(0.06F)
                        ),
                ModelTransform.NONE
        );

        bandana.addChild(
                "back",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -4.45F,
                                -7.20F,
                                4.10F,
                                8.9F,
                                1.25F,
                                0.62F,
                                new Dilation(0.06F)
                        ),
                ModelTransform.NONE
        );

        bandana.addChild(
                "left",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -4.72F,
                                -7.20F,
                                -4.10F,
                                0.62F,
                                1.25F,
                                8.2F,
                                new Dilation(0.06F)
                        ),
                ModelTransform.NONE
        );

        bandana.addChild(
                "right",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                4.10F,
                                -7.20F,
                                -4.10F,
                                0.62F,
                                1.25F,
                                8.2F,
                                new Dilation(0.06F)
                        ),
                ModelTransform.NONE
        );

        bandana.addChild(
                "knot",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -1.05F,
                                -7.28F,
                                4.58F,
                                2.1F,
                                1.45F,
                                1.05F,
                                new Dilation(0.08F)
                        ),
                ModelTransform.NONE
        );

        /*
         * SAFE TAILS:
         *
         * Wichtig:
         * Die Cuboids sind lokal um ihren eigenen Pivot gebaut.
         * Dadurch rotieren sie nicht mehr komisch um den globalen Ursprung.
         */

        bandana.addChild(
                "left_tail",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -0.45F,
                                0.0F,
                                -0.18F,
                                0.90F,
                                2.35F,
                                0.36F,
                                new Dilation(0.03F)
                        ),
                ModelTransform.of(
                        -0.72F,
                        -5.92F,
                        5.08F,
                        -8.0F * DEGREES_TO_RADIANS,
                        0.0F,
                        7.0F * DEGREES_TO_RADIANS
                )
        );

        bandana.addChild(
                "right_tail",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -0.45F,
                                0.0F,
                                -0.18F,
                                0.90F,
                                2.05F,
                                0.36F,
                                new Dilation(0.03F)
                        ),
                ModelTransform.of(
                        0.72F,
                        -5.92F,
                        5.08F,
                        -6.0F * DEGREES_TO_RADIANS,
                        0.0F,
                        -6.0F * DEGREES_TO_RADIANS
                )
        );

        return TexturedModelData.of(modelData, 512, 256).createModel();
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        this.bandana.yaw = state.relativeHeadYaw * DEGREES_TO_RADIANS;
        this.bandana.pitch = state.pitch * DEGREES_TO_RADIANS;
        this.bandana.roll = 0.0F;
    }
}