package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

public final class MonkeyHatModelFactory {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0F);

    private MonkeyHatModelFactory() {
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();

        ModelPartData hat = modelData.getRoot().addChild(
                "hat",
                ModelPartBuilder.create(),
                ModelTransform.NONE
        );

        /*
         * STEHENDER AFFE AUF DEM KOPF
         *
         * Ziel:
         * - steht auf dem Kopf des Spielers
         * - Füße unten auf Höhe des Kopfes
         * - Affe schaut nach vorne (-Z)
         * - Körper vertikal, nicht liegend
         *
         * Player Head Space:
         * X -4 bis 4
         * Y -8 bis 0
         * Z -4 bis 4
         */

        // Füße / Kontaktfläche auf dem Kopf
        addCube(hat, "foot_left", 0, 44, -1.10F, -8.20F, -0.20F, 0.90F, 0.35F, 1.10F);
        addCube(hat, "foot_right", 6, 44,  0.20F, -8.20F, -0.20F, 0.90F, 0.35F, 1.10F);

        // Beine
        addCube(hat, "leg_left", 0, 32, -1.00F, -11.20F, 0.00F, 0.75F, 3.05F, 0.90F);
        addCube(hat, "leg_right", 7, 32, 0.25F, -11.20F, 0.00F, 0.75F, 3.05F, 0.90F);

        // Körper
        addCube(hat, "body", 0, 0, -1.70F, -14.30F, -0.80F, 3.40F, 3.80F, 1.95F);

        // Kleiner Brust-/Bauchblock
        addCube(hat, "belly", 10, 0, -1.20F, -13.40F, -1.15F, 2.40F, 2.30F, 0.55F);

        // Kopf
        addCube(hat, "head", 0, 16, -1.55F, -16.85F, -1.65F, 3.10F, 2.85F, 2.55F);

        // Schnauze
        addCube(hat, "muzzle", 20, 16, -0.80F, -15.80F, -2.15F, 1.60F, 0.90F, 0.70F);

        // Ohren
        addCube(hat, "ear_left", 34, 0, -1.75F, -17.15F, -0.80F, 0.55F, 0.85F, 0.55F);
        addCube(hat, "ear_right", 40, 0, 1.20F, -17.15F, -0.80F, 0.55F, 0.85F, 0.55F);

        // Arme seitlich am Körper
        addCube(hat, "arm_left", 14, 32, -2.20F, -14.00F, -0.35F, 0.65F, 2.85F, 0.75F);
        addCube(hat, "arm_right", 21, 32,  1.55F, -14.00F, -0.35F, 0.65F, 2.85F, 0.75F);

        // Augen
        addCube(hat, "eye_left", 48, 0, -0.65F, -15.95F, -2.18F, 0.18F, 0.18F, 0.08F);
        addCube(hat, "eye_right", 48, 0, 0.47F, -15.95F, -2.18F, 0.18F, 0.18F, 0.08F);

        // Nase / Mund
        addCube(hat, "nose", 50, 0, -0.15F, -15.25F, -2.20F, 0.30F, 0.16F, 0.08F);

        // Schwanzansatz
        ModelPartData tailBase = hat.addChild(
                "tail_base",
                ModelPartBuilder.create()
                        .uv(30, 32)
                        .cuboid(
                                -0.20F,
                                -0.20F,
                                0.0F,
                                0.40F,
                                0.40F,
                                2.30F,
                                new Dilation(0.01F)
                        ),
                ModelTransform.of(
                        0.0F,
                        -13.10F,
                        1.05F,
                        -28.0F * DEG_TO_RAD,
                        0.0F,
                        0.0F
                )
        );

        tailBase.addChild(
                "tail_mid",
                ModelPartBuilder.create()
                        .uv(38, 32)
                        .cuboid(
                                -0.18F,
                                -0.18F,
                                0.0F,
                                0.36F,
                                0.36F,
                                2.10F,
                                new Dilation(0.01F)
                        ),
                ModelTransform.of(
                        0.0F,
                        0.0F,
                        2.10F,
                        -22.0F * DEG_TO_RAD,
                        0.0F,
                        0.0F
                )
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