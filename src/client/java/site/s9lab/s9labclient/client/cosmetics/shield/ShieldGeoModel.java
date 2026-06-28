package site.s9lab.s9labclient.client.cosmetics.shield;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class ShieldGeoModel extends GeoModel<ShieldGeoAnimatable> {
    private final Identifier texture;

    public ShieldGeoModel(Identifier texture) {
        this.texture = texture == null
                ? Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/shields/void_shield.png")
                : texture;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.of(S9LabClient.MOD_ID, "s9lab_shield");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(ShieldGeoAnimatable animatable) {
        return Identifier.of(S9LabClient.MOD_ID, "s9lab_shield");
    }
}
