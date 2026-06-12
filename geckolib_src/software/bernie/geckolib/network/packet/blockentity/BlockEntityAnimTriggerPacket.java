package software.bernie.geckolib.network.packet.blockentity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record BlockEntityAnimTriggerPacket(class_2338 pos, Optional<String> controllerName, String animName) implements MultiloaderPacket {
    public static final class_8710.class_9154<BlockEntityAnimTriggerPacket> TYPE = new class_9154<>(GeckoLibConstants.id("blockentity_anim_trigger"));
    public static final class_9139<class_2540, BlockEntityAnimTriggerPacket> CODEC = class_9139.method_56436(
            class_2338.field_48404, BlockEntityAnimTriggerPacket::pos,
            class_9135.field_48554.method_56433(class_9135::method_56382), BlockEntityAnimTriggerPacket::controllerName,
            class_9135.field_48554, BlockEntityAnimTriggerPacket::animName,
            BlockEntityAnimTriggerPacket::new);

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            final class_1937 level = ClientUtil.getLevel();

            if (level != null && level.method_8321(this.pos) instanceof GeoBlockEntity blockEntity)
                blockEntity.triggerAnim(this.controllerName.orElse(null), this.animName);
        });
    }
}
