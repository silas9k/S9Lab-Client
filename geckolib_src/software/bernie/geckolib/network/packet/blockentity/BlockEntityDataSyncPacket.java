package software.bernie.geckolib.network.packet.blockentity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record BlockEntityDataSyncPacket<D>(class_2338 pos, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final class_8710.class_9154<BlockEntityDataSyncPacket<?>> TYPE = new class_9154<>(GeckoLibConstants.id("blockentity_data_sync"));
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final class_9139<class_9129, BlockEntityDataSyncPacket<?>> CODEC = class_9139.method_56437((buf, packet) -> {
        SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
        buf.method_10807(packet.pos);
        ((class_9139)packet.dataTicket.streamCodec()).encode(buf, packet.data);
    }, buf -> {
        final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

        return new BlockEntityDataSyncPacket<>(buf.method_10811(), dataTicket, dataTicket.streamCodec().decode(buf));
    });

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            final class_1937 level = ClientUtil.getLevel();

            if (level != null && level.method_8321(this.pos) instanceof GeoBlockEntity blockEntity)
                blockEntity.setAnimData(this.dataTicket, this.data);
        });
    }
}
