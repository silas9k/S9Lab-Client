package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HaloGeoModel extends GeoModel<HaloGeoAnimatable> {
    public static final Identifier DEFAULT_MODEL = Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo");
    public static final Identifier DEFAULT_TEXTURE = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/halos/s9lab_gold_halo.png");
    public static final Identifier DEFAULT_ANIMATION = Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo.animation");

    private final Identifier model;
    private final Identifier texture;
    private final Identifier animation;

    public HaloGeoModel() {
        this(DEFAULT_MODEL, DEFAULT_TEXTURE, DEFAULT_ANIMATION);
    }

    public HaloGeoModel(Identifier texture) {
        this(DEFAULT_MODEL, texture, DEFAULT_ANIMATION);
    }

    public HaloGeoModel(Identifier model, Identifier texture, Identifier animation) {
        this.model = model == null ? DEFAULT_MODEL : model;
        this.texture = texture == null ? DEFAULT_TEXTURE : texture;
        this.animation = animation == null ? DEFAULT_ANIMATION : animation;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(HaloGeoAnimatable animatable) {
        return animation;
    }
}
