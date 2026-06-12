package software.bernie.geckolib.animatable.stateless;

import net.minecraft.class_1297;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.network.packet.entity.StatelessEntityPlayAnimPacket;
import software.bernie.geckolib.network.packet.entity.StatelessEntityStopAnimPacket;

/**
 * Extension of {@link StatelessAnimatable} for {@link GeoEntity} animatables
 */
public non-sealed interface StatelessGeoEntity extends StatelessAnimatable, GeoEntity {
    /**
     * Start or continue a pre-defined animation
     */
    @Override
    default void playAnimation(RawAnimation animation) {
        if (!(this instanceof class_1297 self))
            throw new ClassCastException("Cannot use StatelessGeoEntity on a non-entity animatable!");

        if (self.method_73183().method_8608()) {
            handleClientAnimationPlay(this, self.method_5628(), animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessEntityPlayAnimPacket(self.method_5628(), false, animation), self);
        }
    }

    /**
     * Stop an already-playing animation
     */
    @Override
    default void stopAnimation(String animation) {
        if (!(this instanceof class_1297 self))
            throw new ClassCastException("Cannot use StatelessGeoEntity on a non-entity animatable!");

        if (self.method_73183().method_8608()) {
            handleClientAnimationStop(this, self.method_5628(), animation);
        }
        else {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingEntity(new StatelessEntityStopAnimPacket(self.method_5628(), false, animation), self);
        }
    }
}
