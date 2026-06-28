package site.s9lab.s9labclient.client.emote.render;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.emote.EmoteDefinition;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A real GeckoLib player instance. No vanilla ModelPart conversion is involved. */
public final class EmotePlayerAnimatable implements GeoAnimatable {
    private final EmoteDefinition definition;
    private final Identifier texture;
    private final boolean slim;
    private final Identifier animationResource;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EmotePlayerAnimatable(EmoteDefinition definition, Identifier texture, boolean slim) {
        this.definition = definition;
        this.texture = texture;
        this.slim = slim;
        this.animationResource = logicalAnimationResource(definition.animationFile());
    }

    public EmoteDefinition definition() {
        return definition;
    }

    public Identifier texture() {
        return texture;
    }

    public boolean slim() {
        return slim;
    }

    public Identifier animationResource() {
        return animationResource;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        RawAnimation animation = definition.loop()
                ? RawAnimation.begin().thenLoop(definition.animationName())
                : RawAnimation.begin().thenPlayAndHold(definition.animationName());
        controllers.add(new AnimationController<>(
                "s9lab_emote_player",
                0,
                state -> state.setAndContinue(animation)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static Identifier logicalAnimationResource(Identifier file) {
        String path = file.getPath().replace('\\', '/');
        if (path.startsWith("geckolib/animations/")) {
            path = path.substring("geckolib/animations/".length());
        } else if (path.startsWith("animations/")) {
            path = path.substring("animations/".length());
        }
        if (path.endsWith(".animation.json")) {
            path = path.substring(0, path.length() - ".animation.json".length());
        }
        return Identifier.of(file.getNamespace(), path);
    }
}
