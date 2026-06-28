package site.s9lab.s9labclient.client.emote.prop;

import site.s9lab.s9labclient.client.emote.EmotePropDefinition;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class EmotePropAnimatable implements GeoAnimatable {
    private final EmotePropDefinition definition;
    private final RawAnimation animation;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EmotePropAnimatable(EmotePropDefinition definition, boolean loop) {
        this.definition = definition;
        this.animation = loop
                ? RawAnimation.begin().thenLoop(definition.animationName())
                : RawAnimation.begin().thenPlay(definition.animationName());
    }

    public EmotePropDefinition definition() {
        return definition;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                "emote_prop_controller",
                0,
                state -> state.setAndContinue(animation)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
