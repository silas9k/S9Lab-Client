package software.bernie.geckolib.network.packet.entity;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.stateless.StatelessAnimatable;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record StatelessEntityStopAnimPacket(int entityId, boolean isReplacedEntity, String animation) implements MultiloaderPacket {
    public static final class_9154<StatelessEntityStopAnimPacket> TYPE = new class_9154<>(GeckoLibConstants.id("stateless_entity_stop_anim"));
    public static final class_9139<class_2540, StatelessEntityStopAnimPacket> CODEC = class_9139.method_56436(
            class_9135.field_48550, StatelessEntityStopAnimPacket::entityId,
            class_9135.field_48547, StatelessEntityStopAnimPacket::isReplacedEntity,
            class_9135.field_48554, StatelessEntityStopAnimPacket::animation,
            StatelessEntityStopAnimPacket::new);

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

            GeoAnimatable animatable = this.isReplacedEntity ?
                                       RenderUtil.getReplacedAnimatable(entity.method_5864()) :
                                       entity instanceof GeoAnimatable entityAnimatable ? entityAnimatable : null;

            if (animatable instanceof StatelessAnimatable statelessAnimatable)
                statelessAnimatable.handleClientAnimationStop(animatable, this.entityId, this.animation);
        });
    }
}
