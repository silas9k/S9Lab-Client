package software.bernie.geckolib.network.packet.entity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record StopTriggeredEntityAnimPacket(int entityId, boolean isReplacedEntity, Optional<String> controllerName, Optional<String> animName) implements MultiloaderPacket {
    public static final class_9154<StopTriggeredEntityAnimPacket> TYPE = new class_9154<>(GeckoLibConstants.id("stop_triggered_entity_anim"));
    public static final class_9139<class_2540, StopTriggeredEntityAnimPacket> CODEC = class_9139.method_56905(
            class_9135.field_48550, StopTriggeredEntityAnimPacket::entityId,
            class_9135.field_48547, StopTriggeredEntityAnimPacket::isReplacedEntity,
            class_9135.field_48554.method_56433(class_9135::method_56382), StopTriggeredEntityAnimPacket::controllerName,
            class_9135.field_48554.method_56433(class_9135::method_56382), StopTriggeredEntityAnimPacket::animName,
            StopTriggeredEntityAnimPacket::new);

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
                    geoEntity.stopTriggeredAnim(this.controllerName.orElse(null), this.animName.orElse(null));

                return;
            }

            if (RenderUtil.getReplacedAnimatable(entity.method_5864()) instanceof GeoReplacedEntity replacedEntity)
                replacedEntity.stopTriggeredAnim(entity, this.controllerName.orElse(null), this.animName.orElse(null));
        });
    }
}
