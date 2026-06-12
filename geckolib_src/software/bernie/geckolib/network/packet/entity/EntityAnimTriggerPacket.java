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

public record EntityAnimTriggerPacket(int entityId, boolean isReplacedEntity, Optional<String> controllerName, String animName) implements MultiloaderPacket {
    public static final class_8710.class_9154<EntityAnimTriggerPacket> TYPE = new class_9154<>(GeckoLibConstants.id("entity_anim_trigger"));
    public static final class_9139<class_2540, EntityAnimTriggerPacket> CODEC = class_9139.method_56905(
            class_9135.field_48550, EntityAnimTriggerPacket::entityId,
            class_9135.field_48547, EntityAnimTriggerPacket::isReplacedEntity,
            class_9135.field_48554.method_56433(class_9135::method_56382), EntityAnimTriggerPacket::controllerName,
            class_9135.field_48554, EntityAnimTriggerPacket::animName,
            EntityAnimTriggerPacket::new);

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
                    geoEntity.triggerAnim(this.controllerName.orElse(null), this.animName);

                return;
            }

            if (RenderUtil.getReplacedAnimatable(entity.method_5864()) instanceof GeoReplacedEntity replacedEntity)
                replacedEntity.triggerAnim(entity, this.controllerName.orElse(null), this.animName);
        });
    }
}
