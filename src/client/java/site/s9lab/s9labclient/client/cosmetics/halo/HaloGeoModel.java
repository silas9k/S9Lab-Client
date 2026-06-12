package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class HaloGeoModel extends GeoModel<HaloGeoAnimatable> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        /*
         * GeckoLib sucht daraus:
         * assets/s9labclient/geo/s9lab_gold_halo.geo.json
         */
        return Identifier.of(
                S9LabClient.MOD_ID,
                "s9lab_gold_halo"
        );
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.of(
                S9LabClient.MOD_ID,
                "textures/cosmetics/halos/s9lab_gold_halo.png"
        );
    }

    @Override
    public Identifier getAnimationResource(HaloGeoAnimatable animatable) {
        /*
         * Aktuell egal, weil HaloGeoAnimatable keine Controller registriert.
         * Später aktivieren wir die Animation wieder.
         */
        return Identifier.of(
                S9LabClient.MOD_ID,
                "s9lab_gold_halo.animation"
        );
    }
}