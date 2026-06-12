package site.s9lab.s9labclient.client.cosmetics.cape;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

public record CapeCosmetic(String id, String displayName, Identifier texture, Identifier model, Identifier animation, boolean animated) implements Cosmetic {
    @Override
    public CosmeticType type() {
        return CosmeticType.CAPE;
    }

    public static CapeCosmetic s9LabCape() {
        return new CapeCosmetic(
                "s9lab_cape",
                "S9Lab Cape",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/capes/s9lab_cape.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_cape.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_cape.animation.json"),
                false
        );
    }

    public static CapeCosmetic animatedFluxCape() {
        return new CapeCosmetic(
                "s9lab_flux_cape",
                "Flux Cape",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/capes/s9lab_flux_cape.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_cape.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_cape.animation.json"),
                true
        );
    }

    public static CapeCosmetic voidCape() {
        return new CapeCosmetic(
                "s9lab_void_cape",
                "Void Cape",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/capes/s9lab_void_cape.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_cape.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_cape.animation.json"),
                false
        );
    }

    public static CapeCosmetic redstoneCape() {
        return new CapeCosmetic(
                "s9lab_redstone_cape",
                "Redstone Cape",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/capes/s9lab_redstone_cape.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_cape.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_cape.animation.json"),
                true
        );
    }
}
