package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HatGeoModel extends GeoModel<HatGeoAnimatable> {
    private static final Identifier MODEL = Identifier.of(S9LabClient.MOD_ID, "s9lab_pirate_hat");
    private static final Identifier TEXTURE = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/hats/s9lab_pirate_hat.png");
    private static final Identifier ANIMATION = Identifier.of(S9LabClient.MOD_ID, "s9lab_pirate_hat.animation");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HatGeoAnimatable animatable) {
        return ANIMATION;
    }
}