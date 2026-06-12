package software.bernie.geckolib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.class_3264;
import net.minecraft.class_8710;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.cache.GeckoLibResources;
import software.bernie.geckolib.network.packet.MultiloaderPacket;

/**
 * Main GeckoLib client entrypoint
 */
public class GeckoLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceLoader.get(class_3264.field_14188).registerReloader(GeckoLibResources.RELOAD_LISTENER_ID, GeckoLibResources::reload);
    }

    @ApiStatus.Internal
    public static <P extends MultiloaderPacket> void registerPacket(class_8710.class_9154<P> packetType) {
        ClientPlayNetworking.registerGlobalReceiver(packetType, (packet, context) -> packet.receiveMessage(context.player(), context.client()::execute));
    }
}
