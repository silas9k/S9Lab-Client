package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

public final class GuineaPigHatEntityModel {
    private GuineaPigHatEntityModel() {
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();

        ModelPartData hat = modelData.getRoot().addChild(
                "hat",
                ModelPartBuilder.create(),
                ModelTransform.NONE
        );

        /*
         * Meerschweinchen direkt auf dem Kopf.
         *
         * Player schaut nach -Z.
         * Meerschweinchen schaut auch nach -Z.
         *
         * Kein Renderer-Scale mehr.
         * Größe und Sitz sind hier direkt eingebaut.
         *
         * Kopf oben: ungefähr Y -8.
         * Füße berühren den Kopf bei Y ca. -8.02.
         */

        addCube(
                hat,
                "body",
                0, 0,
                -2.35F, -10.05F, -1.65F,
                4.70F, 1.85F, 5.40F
        );

        addCube(
                hat,
                "head",
                0, 14,
                -2.05F, -10.12F, -4.05F,
                4.10F, 1.95F, 2.55F
        );

        addCube(
                hat,
                "snout",
                20, 14,
                -0.95F, -9.38F, -4.95F,
                1.90F, 0.78F, 1.00F
        );

        addCube(
                hat,
                "left_ear",
                34, 0,
                -2.10F, -10.95F, -3.50F,
                0.72F, 0.95F, 0.58F
        );

        addCube(
                hat,
                "right_ear",
                34, 4,
                1.38F, -10.95F, -3.50F,
                0.72F, 0.95F, 0.58F
        );

        addCube(
                hat,
                "front_left_foot",
                42, 0,
                -1.60F, -8.18F, -3.45F,
                0.62F, 0.28F, 0.62F
        );

        addCube(
                hat,
                "front_right_foot",
                42, 4,
                0.98F, -8.18F, -3.45F,
                0.62F, 0.28F, 0.62F
        );

        addCube(
                hat,
                "back_left_foot",
                48, 0,
                -1.60F, -8.18F, 2.60F,
                0.62F, 0.28F, 0.62F
        );

        addCube(
                hat,
                "back_right_foot",
                48, 4,
                0.98F, -8.18F, 2.60F,
                0.62F, 0.28F, 0.62F
        );

        addCube(
                hat,
                "left_eye",
                56, 0,
                -1.05F, -9.72F, -4.98F,
                0.28F, 0.28F, 0.08F
        );

        addCube(
                hat,
                "right_eye",
                56, 2,
                0.77F, -9.72F, -4.98F,
                0.28F, 0.28F, 0.08F
        );

        addCube(
                hat,
                "nose",
                58, 0,
                -0.22F, -9.30F, -5.02F,
                0.44F, 0.22F, 0.08F
        );

        addCube(
                hat,
                "head_patch",
                0, 24,
                -0.95F, -10.16F, -4.08F,
                1.90F, 0.08F, 1.55F
        );

        addCube(
                hat,
                "back_patch",
                8, 24,
                -1.65F, -10.09F, -0.80F,
                3.30F, 0.08F, 2.70F
        );

        return TexturedModelData.of(modelData, 64, 64).createModel();
    }

    private static void addCube(
            ModelPartData parent,
            String name,
            int uvX,
            int uvY,
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
                        .uv(uvX, uvY)
                        .cuboid(
                                x,
                                y,
                                z,
                                sizeX,
                                sizeY,
                                sizeZ,
                                new Dilation(0.01F)
                        ),
                ModelTransform.NONE
        );
    }
}