package site.s9lab.s9labclient.client.cosmetics.halo;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HaloGeoObjectRenderer extends GeoObjectRenderer<HaloGeoAnimatable, Void, GeoRenderState> {
    public HaloGeoObjectRenderer(HaloGeoModel model) {
        super(model);
    }
}