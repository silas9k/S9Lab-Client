package site.s9lab.s9labclient.client.cosmetics.hat;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HatGeoObjectRenderer extends GeoObjectRenderer<HatGeoAnimatable, Void, GeoRenderState> {
    public HatGeoObjectRenderer(HatGeoModel model) {
        super(model);
    }
}