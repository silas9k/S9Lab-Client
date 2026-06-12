package software.bernie.geckolib.network.packet.blockentity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.stateless.StatelessGeoBlockEntity;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record StatelessBlockEntityStopAnimPacket(class_2338 blockPos, String animation) implements MultiloaderPacket {
    public static final class_9154<StatelessBlockEntityStopAnimPacket> TYPE = new class_9154<>(GeckoLibConstants.id("stateless_block_entity_stop_anim"));
    public static final class_9139<class_2540, StatelessBlockEntityStopAnimPacket> CODEC = class_9139.method_56435(
            class_2338.field_48404, StatelessBlockEntityStopAnimPacket::blockPos,
            class_9135.field_48554, StatelessBlockEntityStopAnimPacket::animation,
            StatelessBlockEntityStopAnimPacket::new);

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            final class_1937 level = ClientUtil.getLevel();

            if (level != null && level.method_8321(this.blockPos) instanceof GeoBlockEntity blockEntity && blockEntity instanceof StatelessGeoBlockEntity statelessAnimatable)
                statelessAnimatable.handleClientAnimationStop(blockEntity, 0, this.animation);
        });
    }
}
