package site.s9lab.s9labclient.client.cosmetics.wings;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;

public final class BatWingTextureManager {
    private static final Identifier WHITE_WINGS = Identifier.of(
            S9LabClient.MOD_ID,
            "textures/cosmetics/wings/white_wings.png"
    );
    private static final Identifier BLACK_WINGS = Identifier.of(
            S9LabClient.MOD_ID,
            "textures/cosmetics/wings/black_wings.png"
    );

    private BatWingTextureManager() {
    }

    public static Identifier texture(String style) {
        return "Black".equalsIgnoreCase(style) ? BLACK_WINGS : WHITE_WINGS;
    }
}
