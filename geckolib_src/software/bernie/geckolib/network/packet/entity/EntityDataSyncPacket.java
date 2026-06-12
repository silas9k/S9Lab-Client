package software.bernie.geckolib.network.packet.entity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record EntityDataSyncPacket<D>(int entityId, boolean isReplacedEntity, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final class_8710.class_9154<EntityDataSyncPacket<?>> TYPE = new class_9154<>(GeckoLibConstants.id("entity_data_sync"));
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final class_9139<class_9129, EntityDataSyncPacket<?>> CODEC = class_9139.method_56437((buf, packet) -> {
        SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
        buf.method_10804(packet.entityId);
        buf.method_52964(packet.isReplacedEntity);
        ((class_9139)packet.dataTicket.streamCodec()).encode(buf, packet.data);
    }, buf -> {
        final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

        return new EntityDataSyncPacket<>(buf.method_10816(), buf.readBoolean(), dataTicket, dataTicket.streamCodec().decode(buf));
    });

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            final class_1937 level = ClientUtil.getLevel();
            final class_1297 entity;

            if (level == null || (entity = level.method_8469(this.entityId)) == null)
                return;

            if (!this.isReplacedEntity) {
                if (entity instanceof GeoEntity geoEntity)
                    geoEntity.setAnimData(this.dataTicket, this.data);

                return;
            }

            if (RenderUtil.getReplacedAnimatable(entity.method_5864()) instanceof GeoReplacedEntity replacedEntity)
                replacedEntity.setAnimData(entity, this.dataTicket, this.data);
        });
    }
}
