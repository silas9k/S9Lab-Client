package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

public final class DuckHatEntityModel {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0F);

    private DuckHatEntityModel() {
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();

        ModelPartData hat = modelData.getRoot().addChild(
                "hat",
                ModelPartBuilder.create(),
                ModelTransform.NONE
        );

        /*
         * Duck liegt flach auf dem Kopf.
         */

        addCube(
                hat,
                "body",
                0, 0,
                -3.75F, -9.95F, -2.55F,
                6.90F, 2.10F, 4.60F,
                0.0F, -8.95F, -0.20F,
                0.0F, 0.0F, -4.0F
        );

        addCube(
                hat,
                "head",
                0, 8,
                2.30F, -10.15F, -1.75F,
                2.20F, 2.20F, 3.00F,
                3.35F, -9.05F, -0.25F,
                0.0F, 0.0F, -6.0F
        );

        addCube(
                hat,
                "beak",
                16, 8,
                4.28F, -9.40F, -1.20F,
                1.35F, 0.65F, 1.90F,
                4.95F, -9.05F, -0.25F,
                0.0F, 0.0F, -6.0F
        );

        addCube(
                hat,
                "tail",
                24, 0,
                -4.25F, -9.80F, -1.00F,
                1.10F, 1.10F, 1.60F,
                -3.70F, -9.25F, -0.20F,
                0.0F, 0.0F, 18.0F
        );

        addCube(
                hat,
                "left_wing",
                28, 0,
                -1.35F, -9.55F, -2.95F,
                3.25F, 0.55F, 0.70F,
                0.20F, -9.25F, -2.60F,
                0.0F, 0.0F, -8.0F
        );

        addCube(
                hat,
                "right_wing",
                28, 4,
                -1.35F, -9.55F, 2.00F,
                3.25F, 0.55F, 0.70F,
                0.20F, -9.25F, 2.35F,
                0.0F, 0.0F, 8.0F
        );

        addCube(
                hat,
                "left_eye",
                44, 0,
                4.50F, -9.85F, -0.95F,
                0.12F, 0.32F, 0.32F,
                0.0F, 0.0F, 0.0F,
                0.0F, 0.0F, 0.0F
        );

        addCube(
                hat,
                "right_eye",
                44, 2,
                4.50F, -9.85F, 0.35F,
                0.12F, 0.32F, 0.32F,
                0.0F, 0.0F, 0.0F,
                0.0F, 0.0F, 0.0F
        );

        addCube(
                hat,
                "hair_1",
                48, 0,
                2.85F, -10.85F, -0.35F,
                0.25F, 0.90F, 0.25F,
                2.98F, -10.35F, -0.22F,
                0.0F, 0.0F, -12.0F
        );

        addCube(
                hat,
                "hair_2",
                48, 2,
                3.30F, -10.78F, -0.20F,
                0.25F, 0.75F, 0.25F,
                3.42F, -10.35F, -0.08F,
                0.0F, 0.0F, 10.0F
        );

        return TexturedModelData.of(modelData, 64, 64).createModel();
    }

    private static void addCube(
            ModelPartData parent,
            String name,
            int uvX,
            int uvY,
            float originX,
            float originY,
            float originZ,
            float sizeX,
            float sizeY,
            float sizeZ,
            float pivotX,
            float pivotY,
            float pivotZ,
            float rotX,
            float rotY,
            float rotZ
    ) {
        parent.addChild(
                name,
                ModelPartBuilder.create()
                        .uv(uvX, uvY)
                        .cuboid(
                                originX - pivotX,
                                originY - pivotY,
                                originZ - pivotZ,
                                sizeX,
                                sizeY,
                                sizeZ,
                                new Dilation(0.02F)
                        ),
                ModelTransform.of(
                        pivotX,
                        pivotY,
                        pivotZ,
                        rotX * DEG_TO_RAD,
                        rotY * DEG_TO_RAD,
                        rotZ * DEG_TO_RAD
                )
        );
    }
}