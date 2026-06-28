package site.s9lab.s9labclient.client.emote.prop;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class EmotePropObjectRenderer extends GeoObjectRenderer<EmotePropAnimatable, Void, GeoRenderState> {
    public EmotePropObjectRenderer(EmotePropGeoModel model) {
        super(model);
    }
}
