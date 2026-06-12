package site.s9lab.s9labclient.client.cosmetics.hat;

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

public class HatEntityModel extends EntityModel<PlayerEntityRenderState> {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0F);

    private final ModelPart hat;

    public HatEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.hat = root.getChild("hat");
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();

        ModelPartData hat = modelData.getRoot().addChild(
                "hat",
                ModelPartBuilder.create(),
                ModelTransform.NONE
        );

        /*
         * Pirate Hat im Minecraft Head-Space.
         *
         * Kopf:
         * X -4 bis 4
         * Y -8 bis 0
         * Z -4 bis 4
         */

        addCube(
                hat,
                "front_brim",
                0, 9,
                -6.0F, -8.65F, -5.0F,
                12.0F, 1.0F, 2.0F,
                0.0F, -8.15F, -4.0F,
                -20.0F, 0.0F, 0.0F
        );

        addCube(
                hat,
                "back_brim",
                0, 12,
                -6.0F, -8.65F, 3.0F,
                12.0F, 1.0F, 2.0F,
                0.0F, -8.15F, 4.0F,
                20.0F, 0.0F, 0.0F
        );

        addCube(
                hat,
                "right_brim",
                0, 20,
                4.0F, -8.65F, -3.0F,
                2.0F, 1.0F, 6.0F,
                5.0F, -8.15F, 0.0F,
                0.0F, 0.0F, -20.0F
        );

        addCube(
                hat,
                "left_brim",
                20, 15,
                -6.0F, -8.65F, -3.0F,
                2.0F, 1.0F, 6.0F,
                -5.0F, -8.15F, 0.0F,
                0.0F, 0.0F, 20.0F
        );

        addCube(
                hat,
                "main",
                0, 0,
                -4.0F, -10.65F, -3.0F,
                8.0F, 3.0F, 6.0F,
                0.0F, 0.0F, 0.0F,
                0.0F, 0.0F, 0.0F
        );

        addCube(
                hat,
                "top",
                0, 15,
                -3.0F, -11.65F, -2.0F,
                6.0F, 1.0F, 4.0F,
                0.0F, 0.0F, 0.0F,
                0.0F, 0.0F, 0.0F
        );

        /*
         * FIXED FEATHER:
         *
         * Keine alten GeckoLib-Pivots mehr.
         * Die Feder ist jetzt ein kompakter Child-Part direkt am Hut.
         */
        ModelPartData feather = hat.addChild(
                "feather",
                ModelPartBuilder.create(),
                ModelTransform.of(
                        -4.65F,
                        -9.25F,
                        -4.20F,
                        0.0F,
                        0.0F,
                        -24.0F * DEG_TO_RAD
                )
        );

        /*
         * Goldener Schaft.
         */
        feather.addChild(
                "stem",
                ModelPartBuilder.create()
                        .uv(16, 20)
                        .cuboid(
                                -0.13F,
                                -4.10F,
                                -0.12F,
                                0.26F,
                                4.55F,
                                0.24F,
                                new Dilation(0.01F)
                        ),
                ModelTransform.NONE
        );

        /*
         * Violette Federblätter links.
         * Alle starten am Schaft, dadurch fliegt nichts mehr auseinander.
         */
        addFeatherPiece(
                feather,
                "left_1",
                20, 22,
                0.0F, -3.65F, 0.0F,
                -1.15F, 0.36F,
                -24.0F
        );

        addFeatherPiece(
                feather,
                "left_2",
                20, 23,
                0.0F, -2.95F, 0.0F,
                -1.25F, 0.38F,
                -18.0F
        );

        addFeatherPiece(
                feather,
                "left_3",
                20, 24,
                0.0F, -2.25F, 0.0F,
                -1.15F, 0.36F,
                -12.0F
        );

        addFeatherPiece(
                feather,
                "left_4",
                20, 25,
                0.0F, -1.55F, 0.0F,
                -0.95F, 0.34F,
                -6.0F
        );

        /*
         * Violette Federblätter rechts.
         */
        addFeatherPiece(
                feather,
                "right_1",
                25, 23,
                0.0F, -3.45F, 0.0F,
                0.95F, 0.36F,
                24.0F
        );

        addFeatherPiece(
                feather,
                "right_2",
                25, 24,
                0.0F, -2.75F, 0.0F,
                1.10F, 0.38F,
                18.0F
        );

        addFeatherPiece(
                feather,
                "right_3",
                25, 25,
                0.0F, -2.05F, 0.0F,
                1.00F, 0.36F,
                12.0F
        );

        addFeatherPiece(
                feather,
                "right_4",
                25, 26,
                0.0F, -1.35F, 0.0F,
                0.80F, 0.34F,
                6.0F
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

    private static void addFeatherPiece(
            ModelPartData parent,
            String name,
            int uvX,
            int uvY,
            float pivotX,
            float pivotY,
            float pivotZ,
            float length,
            float height,
            float rotZ
    ) {
        float x = length < 0.0F ? length : 0.0F;
        float width = Math.abs(length);

        parent.addChild(
                name,
                ModelPartBuilder.create()
                        .uv(uvX, uvY)
                        .cuboid(
                                x,
                                -height * 0.5F,
                                -0.13F,
                                width,
                                height,
                                0.26F,
                                new Dilation(0.01F)
                        ),
                ModelTransform.of(
                        pivotX,
                        pivotY,
                        pivotZ,
                        0.0F,
                        0.0F,
                        rotZ * DEG_TO_RAD
                )
        );
    }

@Override
public void setAngles(PlayerEntityRenderState state) {
    this.hat.yaw = 0.0F;
    this.hat.pitch = 0.0F;
    this.hat.roll = 0.0F;
}

public static ModelPart createDuckHatModelPart() {
    ModelData modelData = new ModelData();

    ModelPartData hat = modelData.getRoot().addChild(
            "hat",
            ModelPartBuilder.create(),
            ModelTransform.NONE
    );

    /*
     * Duck sitzt direkt auf dem Kopf.
     * Negative Y = höher über dem Kopf.
     * Forward im Head-Space = Richtung -Z.
     *
     * Ente schaut also nach vorne mit dem Spieler mit.
     */
    ModelPartData duck = hat.addChild(
            "duck",
            ModelPartBuilder.create(),
            ModelTransform.of(
                    0.0F,
                    -8.85F,
                    0.05F,
                    -8.0F * DEG_TO_RAD,
                    0.0F,
                    0.0F
            )
    );

    // Hauptkörper
    duck.addChild(
            "body",
            ModelPartBuilder.create()
                    .uv(0, 0)
                    .cuboid(
                            -3.6F, -1.55F, -1.35F,
                            7.2F, 2.25F, 4.8F,
                            new Dilation(0.02F)
                    ),
            ModelTransform.NONE
    );

    // Kopf vorne
    duck.addChild(
            "head",
            ModelPartBuilder.create()
                    .uv(0, 12)
                    .cuboid(
                            -2.25F, -1.75F, -4.25F,
                            4.5F, 2.35F, 3.0F,
                            new Dilation(0.02F)
                    ),
            ModelTransform.NONE
    );

    // Schnabel
    duck.addChild(
            "beak",
            ModelPartBuilder.create()
                    .uv(32, 0)
                    .cuboid(
                            -1.15F, -0.95F, -5.25F,
                            2.3F, 0.8F, 1.2F,
                            new Dilation(0.01F)
                    ),
            ModelTransform.NONE
    );

    // Schwanz
    duck.addChild(
            "tail",
            ModelPartBuilder.create()
                    .uv(8, 22)
                    .cuboid(
                            -0.8F, -0.45F, 0.0F,
                            1.6F, 0.85F, 1.35F,
                            new Dilation(0.01F)
                    ),
            ModelTransform.of(
                    0.0F,
                    -0.20F,
                    3.35F,
                    -14.0F * DEG_TO_RAD,
                    0.0F,
                    0.0F
            )
    );

    // Linker Flügel
    duck.addChild(
            "wing_left",
            ModelPartBuilder.create()
                    .uv(0, 26)
                    .cuboid(
                            0.0F, -0.35F, -1.25F,
                            0.9F, 0.95F, 2.65F,
                            new Dilation(0.01F)
                    ),
            ModelTransform.of(
                    3.05F,
                    -0.55F,
                    0.65F,
                    0.0F,
                    0.0F,
                    14.0F * DEG_TO_RAD
            )
    );

    // Rechter Flügel
    duck.addChild(
            "wing_right",
            ModelPartBuilder.create()
                    .uv(10, 26)
                    .cuboid(
                            -0.9F, -0.35F, -1.25F,
                            0.9F, 0.95F, 2.65F,
                            new Dilation(0.01F)
                    ),
            ModelTransform.of(
                    -3.05F,
                    -0.55F,
                    0.65F,
                    0.0F,
                    0.0F,
                    -14.0F * DEG_TO_RAD
            )
    );

    // Füße
    duck.addChild(
            "foot_left",
            ModelPartBuilder.create()
                    .uv(32, 8)
                    .cuboid(
                            -0.55F, 0.0F, -0.55F,
                            1.1F, 0.55F, 1.0F,
                            new Dilation(0.0F)
                    ),
            ModelTransform.of(
                    1.15F,
                    0.65F,
                    0.35F,
                    0.0F,
                    0.0F,
                    0.0F
            )
    );

    duck.addChild(
            "foot_right",
            ModelPartBuilder.create()
                    .uv(32, 8)
                    .cuboid(
                            -0.55F, 0.0F, -0.55F,
                            1.1F, 0.55F, 1.0F,
                            new Dilation(0.0F)
                    ),
            ModelTransform.of(
                    -1.15F,
                    0.65F,
                    0.35F,
                    0.0F,
                    0.0F,
                    0.0F
            )
    );

    // Augen als kleine Cubes vorne
    duck.addChild(
            "eye_left",
            ModelPartBuilder.create()
                    .uv(48, 0)
                    .cuboid(
                            -0.18F, -0.18F, -0.08F,
                            0.36F, 0.36F, 0.08F,
                            new Dilation(0.0F)
                    ),
            ModelTransform.of(
                    0.95F,
                    -0.62F,
                    -4.28F,
                    0.0F,
                    0.0F,
                    0.0F
            )
    );

    duck.addChild(
            "eye_right",
            ModelPartBuilder.create()
                    .uv(56, 0)
                    .cuboid(
                            -0.18F, -0.18F, -0.08F,
                            0.36F, 0.36F, 0.08F,
                            new Dilation(0.0F)
                    ),
            ModelTransform.of(
                    -0.95F,
                    -0.62F,
                    -4.28F,
                    0.0F,
                    0.0F,
                    0.0F
            )
    );

    return TexturedModelData.of(modelData, 64, 64).createModel();
}
}