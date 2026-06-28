package site.s9lab.s9labclient.client.emote.render;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class EmotePlayerGeoModel extends GeoModel<EmotePlayerAnimatable> {
    private static final Identifier WIDE_MODEL = Identifier.of(S9LabClient.MOD_ID, "emotes/player_wide");
    private static final Identifier SLIM_MODEL = Identifier.of(S9LabClient.MOD_ID, "emotes/player_slim");
    private final EmotePlayerAnimatable animatable;

    public EmotePlayerGeoModel(EmotePlayerAnimatable animatable) {
        this.animatable = animatable;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return animatable.slim() ? SLIM_MODEL : WIDE_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return animatable.texture();
    }

    @Override
    public Identifier getAnimationResource(EmotePlayerAnimatable animatable) {
        return animatable.animationResource();
    }
}
