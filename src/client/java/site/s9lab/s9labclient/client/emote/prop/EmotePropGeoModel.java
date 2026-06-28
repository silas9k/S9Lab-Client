package site.s9lab.s9labclient.client.emote.prop;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class EmotePropGeoModel extends GeoModel<EmotePropAnimatable> {
    private final EmotePropAnimatable animatable;

    public EmotePropGeoModel(EmotePropAnimatable animatable) {
        this.animatable = animatable;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return logical(animatable.definition().model(), "geckolib/models/", ".geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return animatable.definition().texture();
    }

    @Override
    public Identifier getAnimationResource(EmotePropAnimatable animatable) {
        return logical(animatable.definition().animation(), "geckolib/animations/", ".animation.json");
    }

    private static Identifier logical(Identifier id, String prefix, String suffix) {
        String path = id.getPath();
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(suffix)) {
            path = path.substring(0, path.length() - suffix.length());
        }
        return Identifier.of(id.getNamespace(), path);
    }
}
