package software.bernie.geckolib.animatable.stateless;

import net.minecraft.class_1297;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.SyncedSingletonAnimatableCache;
import software.bernie.geckolib.network.packet.singleton.StatelessSingletonPlayAnimPacket;
import software.bernie.geckolib.network.packet.singleton.StatelessSingletonStopAnimPacket;

/**
 * Extension of {@link StatelessAnimatable} for {@link SingletonGeoAnimatable} animatables.
 * <p>
 * Animatables <b><u>MUST</u></b> be registered with {@link SingletonGeoAnimatable#registerSyncedAnimatable} to use this interface
 */
public non-sealed interface StatelessGeoSingletonAnimatable extends StatelessAnimatable, SingletonGeoAnimatable {
    /**
     * Start or continue an animation, letting its pre-defined loop type determine whether it should loop or not
     */
    default void playAnimation(String animation, class_1297 relatedEntity, long instanceId) {
        playAnimation(RawAnimation.begin().thenPlay(animation), relatedEntity, instanceId);
    }

    /**
     * Start or continue an animation, forcing it to loop continuously until stopped
     */
    default void playLoopingAnimation(String animation, class_1297 relatedEntity, long instanceId) {
        playAnimation(RawAnimation.begin().thenLoop(animation), relatedEntity, instanceId);
    }

    /**
     * Start or continue an animation, then hold the pose at the end of the animation until otherwise stopped
     */
    default void playAndHoldAnimation(String animation, class_1297 relatedEntity, long instanceId) {
        playAnimation(RawAnimation.begin().thenPlayAndHold(animation), relatedEntity, instanceId);
    }

    /**
     * Stop an already-playing animation
     */
    default void stopAnimation(RawAnimation animation, class_1297 relatedEntity, long instanceId) {
        stopAnimation(animation.getStageCount() == 1 ? animation.getAnimationStages().getFirst().animationName() : animation.toString(), relatedEntity, instanceId);
    }

    /**
     * Start or continue a pre-defined animation
     */
    default void playAnimation(RawAnimation animation, class_1297 relatedEntity, long instanceId) {
        if (relatedEntity.method_73183().method_8608()) {
            handleClientAnimationPlay(this, instanceId, animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessSingletonPlayAnimPacket(SyncedSingletonAnimatableCache.getOrCreateId(this), instanceId, animation), relatedEntity);
        }
    }

    /**
     * Stop an already-playing animation
     */
    default void stopAnimation(String animation, class_1297 relatedEntity, long instanceId) {
        if (relatedEntity.method_73183().method_8608()) {
            handleClientAnimationStop(this, instanceId, animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessSingletonStopAnimPacket(SyncedSingletonAnimatableCache.getOrCreateId(this), instanceId, animation), relatedEntity);
        }
    }

    // Unsupported method handlers below; do not use

    /**
     * @deprecated Use {@link #playAnimation(String, class_1297, long)} instead.
     */
    @Deprecated
    default void playAnimation(String animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }

    /**
     * @deprecated Use {@link #playLoopingAnimation(String, class_1297, long)} instead.
     */
    @Deprecated
    default void playLoopingAnimation(String animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }

    /**
     * @deprecated Use {@link #playAndHoldAnimation(String, class_1297, long)} instead.
     */
    @Deprecated
    default void playAndHoldAnimation(String animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }

    /**
     * @deprecated Use {@link #stopAnimation(RawAnimation, class_1297, long)} instead.
     */
    @Deprecated
    default void stopAnimation(RawAnimation animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }

    /**
     * @deprecated Use {@link #playAnimation(RawAnimation, class_1297, long)} instead.
     */
    @Deprecated
    default void playAnimation(RawAnimation animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }

    /**
     * @deprecated Use {@link #stopAnimation(String, class_1297, long)} instead.
     */
    @Deprecated
    default void stopAnimation(String animation) {
        throw new IllegalStateException("Cannot use non-level method handlers on StatelessSingletonGeoAnimatable");
    }
}
