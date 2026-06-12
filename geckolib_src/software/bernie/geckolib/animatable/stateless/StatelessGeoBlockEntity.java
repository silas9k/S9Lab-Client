package software.bernie.geckolib.animatable.stateless;

import net.minecraft.class_2586;
import net.minecraft.class_3218;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.network.packet.blockentity.StatelessBlockEntityPlayAnimPacket;
import software.bernie.geckolib.network.packet.blockentity.StatelessBlockEntityStopAnimPacket;

/**
 * Extension of {@link StatelessAnimatable} for {@link GeoBlockEntity} animatables
 */
public non-sealed interface StatelessGeoBlockEntity extends StatelessAnimatable, GeoBlockEntity {
    /**
     * Start or continue a pre-defined animation
     */
    @Override
    default void playAnimation(RawAnimation animation) {
        if (!(this instanceof class_2586 self))
            throw new ClassCastException("Cannot use StatelessGeoBlockEntity on a non-BlockEntity animatable!");

        if (self.method_10997() instanceof class_3218 level) {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingBlock(new StatelessBlockEntityPlayAnimPacket(self.method_11016(), animation), level, self.method_11016());
        }
        else {
            handleClientAnimationPlay(this, 0, animation);
        }
    }

    /**
     * Stop an already-playing animation
     */
    @Override
    default void stopAnimation(String animation) {
        if (!(this instanceof class_2586 self))
            throw new ClassCastException("Cannot use StatelessGeoBlockEntity on a non-BlockEntity animatable!");

        if (self.method_10997() instanceof class_3218 level) {
            GeckoLibServices.NETWORK.sendToAllPlayersTrackingBlock(new StatelessBlockEntityStopAnimPacket(self.method_11016(), animation), level, self.method_11016());
        }
        else {
            handleClientAnimationStop(this, 0, animation);
        }
    }
}
