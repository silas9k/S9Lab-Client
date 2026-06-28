package site.s9lab.s9labclient.client.emote.render;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

public final class EmotePlayerObjectRenderer
        extends GeoObjectRenderer<EmotePlayerAnimatable, Void, GeoRenderState> {
    public EmotePlayerObjectRenderer(EmotePlayerGeoModel model) {
        super(model);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        // GeoObjectRenderer's default half-block translation is for standalone objects.
        // Player geometry is authored directly around the entity origin.
    }
}
