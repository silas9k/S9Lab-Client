package software.bernie.geckolib.network.packet.singleton;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.stateless.StatelessGeoSingletonAnimatable;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.SyncedSingletonAnimatableCache;
import software.bernie.geckolib.network.packet.MultiloaderPacket;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record StatelessSingletonPlayAnimPacket(String syncableId, long instanceId, RawAnimation animation) implements MultiloaderPacket {
    public static final class_9154<StatelessSingletonPlayAnimPacket> TYPE = new class_9154<>(GeckoLibConstants.id("stateless_singleton_play_anim"));
    public static final class_9139<class_2540, StatelessSingletonPlayAnimPacket> CODEC = class_9139.method_56436(
            class_9135.field_48554, StatelessSingletonPlayAnimPacket::syncableId,
            class_9135.field_48551, StatelessSingletonPlayAnimPacket::instanceId,
            RawAnimation.STREAM_CODEC, StatelessSingletonPlayAnimPacket::animation,
            StatelessSingletonPlayAnimPacket::new);

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            GeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(this.syncableId);

            if (animatable instanceof StatelessGeoSingletonAnimatable statelessAnimatable)
                statelessAnimatable.handleClientAnimationPlay(animatable, this.instanceId, this.animation);
        });
    }
}
