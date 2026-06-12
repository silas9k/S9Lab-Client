package software.bernie.geckolib.network.packet.singleton;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.cache.SyncedSingletonAnimatableCache;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record SingletonDataSyncPacket<D>(String syncableId, long instanceId, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final class_8710.class_9154<SingletonDataSyncPacket<?>> TYPE = new class_9154<>(GeckoLibConstants.id("singleton_data_sync"));
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final class_9139<class_9129, SingletonDataSyncPacket<?>> CODEC = class_9139.method_56437((buf, packet) -> {
        SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
        buf.method_10814(packet.syncableId);
        buf.method_10791(packet.instanceId);
        ((class_9139)packet.dataTicket.streamCodec()).encode(buf, packet.data);
    }, buf -> {
        final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

        return new SingletonDataSyncPacket<>(buf.method_19772(), buf.method_10792(), dataTicket, dataTicket.streamCodec().decode(buf));
    });

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            final class_1657 player = ClientUtil.getClientPlayer();
            final GeoAnimatable animatable;

            if (player == null || (animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(this.syncableId)) == null)
                return;

            if (animatable instanceof SingletonGeoAnimatable singleton)
                singleton.setAnimData(player, this.instanceId, this.dataTicket, this.data);
        });
    }
}
