package software.bernie.geckolib.network.packet.singleton;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.cache.SyncedSingletonAnimatableCache;
import software.bernie.geckolib.network.packet.MultiloaderPacket;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record SingletonAnimTriggerPacket(String syncableId, long instanceId, Optional<String> controllerName, String animName) implements MultiloaderPacket {
    public static final class_8710.class_9154<SingletonAnimTriggerPacket> TYPE = new class_9154<>(GeckoLibConstants.id("singleton_anim_trigger"));
    public static final class_9139<class_2540, SingletonAnimTriggerPacket> CODEC = class_9139.method_56905(
            class_9135.field_48554, SingletonAnimTriggerPacket::syncableId,
            class_9135.field_48551, SingletonAnimTriggerPacket::instanceId,
            class_9135.field_48554.method_56433(class_9135::method_56382), SingletonAnimTriggerPacket::controllerName,
            class_9135.field_48554, SingletonAnimTriggerPacket::animName,
            SingletonAnimTriggerPacket::new);

    @Override
    public class_9154<? extends class_8710> method_56479() {
        return TYPE;
    }

    @Override
    public void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            GeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(this.syncableId);

            if (animatable != null) {
                AnimatableManager<GeoAnimatable> animatableManager = animatable.getAnimatableInstanceCache().getManagerForId(this.instanceId);

                if (this.controllerName.isPresent()) {
                    animatableManager.tryTriggerAnimation(this.controllerName.get(), this.animName);
                }
                else {
                    animatableManager.tryTriggerAnimation(this.animName);
                }
            }
        });
    }
}
