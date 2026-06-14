package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HaloGeoModel extends GeoModel<HaloGeoAnimatable> {
    private final Identifier texture;

    public HaloGeoModel() {
        this(Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/halos/s9lab_gold_halo.png"));
    }

    public HaloGeoModel(Identifier texture) {
        this.texture = texture == null
                ? Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/halos/s9lab_gold_halo.png")
                : texture;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(HaloGeoAnimatable animatable) {
        return Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo.animation");
    }
}
