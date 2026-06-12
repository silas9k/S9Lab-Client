package site.s9lab.s9labclient.client.cosmetics.bandana;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BandanaGeoObjectRenderer extends GeoObjectRenderer<BandanaGeoAnimatable, Void, GeoRenderState> {
    public BandanaGeoObjectRenderer(BandanaGeoModel model) {
        super(model);
    }
}