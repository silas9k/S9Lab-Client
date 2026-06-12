package software.bernie.geckolib.network.packet;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_8710;

/**
 * Multiloader implementation of a packet base, for loader-agnostic packet handling
 */
public interface MultiloaderPacket extends class_8710 {
    /**
     * Handle the message after being received and decoded
     * <p>
     * This method is side-agnostic, so make sure you call out to client proxies as needed
     * <p>
     * The player may be null if the packet is being sent before the player loads in
     */
    void receiveMessage(@Nullable class_1657 sender, Consumer<Runnable> workQueue);
}
