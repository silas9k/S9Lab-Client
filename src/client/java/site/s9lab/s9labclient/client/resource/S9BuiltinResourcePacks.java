package site.s9lab.s9labclient.client.resource;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.s9lab.s9labclient.S9LabClient;

public final class S9BuiltinResourcePacks {
    private static final Logger LOGGER = LoggerFactory.getLogger("S9Lab Built-in Packs");
    private static boolean registered;

    private S9BuiltinResourcePacks() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        ModContainer container = FabricLoader.getInstance()
                .getModContainer(S9LabClient.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Missing mod container for " + S9LabClient.MOD_ID));

        boolean success = ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(S9LabClient.MOD_ID, "s9lab_client_resourcepack"),
                container,
                Text.literal("S9Lab Client Resourcepack"),
                ResourcePackActivationType.ALWAYS_ENABLED
        );

        if (!success) {
            LOGGER.error("Could not register built-in S9Lab Client Resourcepack. Expected path: resourcepacks/s9lab_client_resourcepack");
            return;
        }

        registered = true;
        LOGGER.info("Registered built-in S9Lab Client Resourcepack.");
    }
}
