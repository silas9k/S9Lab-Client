package software.bernie.geckolib.animatable.stateless;

import net.minecraft.class_1297;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.network.packet.entity.StatelessEntityPlayAnimPacket;
import software.bernie.geckolib.network.packet.entity.StatelessEntityStopAnimPacket;

/**
 * Extension of {@link StatelessAnimatable} for {@link GeoReplacedEntity} animatables
 */
public interface StatelessGeoReplacedEntity extends StatelessGeoSingletonAnimatable, GeoReplacedEntity {
    /**
     * Start or continue an animation, letting its pre-defined loop type determine whether it should loop or not
     */
    default void playAnimation(String animation, class_1297 relatedEntity) {
        playAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    /**
     * Start or continue an animation, forcing it to loop continuously until stopped
     */
    default void playLoopingAnimation(String animation, class_1297 relatedEntity) {
        playLoopingAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    /**
     * Start or continue an animation, then hold the pose at the end of the animation until otherwise stopped
     */
    default void playAndHoldAnimation(String animation, class_1297 relatedEntity) {
        playAndHoldAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    /**
     * Stop an already-playing animation
     */
    default void stopAnimation(RawAnimation animation, class_1297 relatedEntity) {
        stopAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    /**
     * Start or continue a pre-defined animation
     */
    default void playAnimation(RawAnimation animation, class_1297 relatedEntity) {
        playAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    /**
     * Stop an already-playing animation
     */
    default void stopAnimation(String animation, class_1297 relatedEntity) {
        stopAnimation(animation, relatedEntity, relatedEntity.method_5628());
    }

    @Override
    default void playAnimation(RawAnimation animation, class_1297 relatedEntity, long instanceId) {
        if (relatedEntity.method_73183().method_8608()) {
            handleClientAnimationPlay(this, instanceId, animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessEntityPlayAnimPacket((int)instanceId, true, animation), relatedEntity);
        }
    }

    @Override
    default void stopAnimation(String animation, class_1297 relatedEntity, long instanceId) {
        if (relatedEntity.method_73183().method_8608()) {
            handleClientAnimationStop(this, instanceId, animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessEntityStopAnimPacket((int)instanceId, true, animation), relatedEntity);
        }
    }
}
