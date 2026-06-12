package site.s9lab.s9labclient.client.cosmetics.bandana;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BandanaGeoModel extends GeoModel<BandanaGeoAnimatable> {
    private Identifier animation = Identifier.of(
            S9LabClient.MOD_ID,
            "animations/s9lab_bandana.animation.json"
    );

    public void setTexture(Identifier texture) {
    }

    public void setModel(Identifier model) {
    }

    public void setAnimation(Identifier animation) {
        this.animation = animation;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return null;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return null;
    }

    @Override
    public Identifier getAnimationResource(BandanaGeoAnimatable animatable) {
        return this.animation;
    }
}