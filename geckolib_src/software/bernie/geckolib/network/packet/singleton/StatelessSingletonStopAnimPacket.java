package software.bernie.geckolib.network.packet.singleton;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.stateless.StatelessGeoSingletonAnimatable;
import software.bernie.geckolib.cache.SyncedSingletonAnimatableCache;
import software.bernie.geckolib.network.packet.MultiloaderPacket;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record StatelessSingletonStopAnimPacket(String syncableId, long instanceId, String animation) implements MultiloaderPacket {
    public static final class_9154<StatelessSingletonStopAnimPacket> TYPE = new class_9154<>(GeckoLibConstants.id("stateless_singleton_stop_anim"));
    public static final class_9139<class_2540, StatelessSingletonStopAnimPacket> CODEC = class_9139.method_56436(
            class_9135.field_48554, StatelessSingletonStopAnimPacket::syncableId,
            class_9135.field_48551, StatelessSingletonStopAnimPacket::instanceId,
            class_9135.field_48554, StatelessSingletonStopAnimPacket::animation,
            StatelessSingletonStopAnimPacket::new);

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            GeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(this.syncableId);

            if (animatable instanceof StatelessGeoSingletonAnimatable statelessAnimatable)
                statelessAnimatable.handleClientAnimationStop(animatable, this.instanceId, this.animation);
        });
    }
}
