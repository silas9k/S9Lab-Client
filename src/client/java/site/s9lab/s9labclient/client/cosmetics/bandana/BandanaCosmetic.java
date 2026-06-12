package site.s9lab.s9labclient.client.cosmetics.bandana;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

public record BandanaCosmetic(
        String id,
        String displayName,
        Identifier texture,
        Identifier model,
        Identifier animation,
        boolean animated
) implements Cosmetic {

    @Override
    public CosmeticType type() {
        return CosmeticType.BANDANA;
    }

    public static BandanaCosmetic s9LabBandana() {
        return new BandanaCosmetic(
                "s9lab_bandana",
                "Black Bandana",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/bandanas/s9lab_bandana.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_bandana.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_bandana.animation.json"),
                false
        );
    }

    public static BandanaCosmetic pulseBandana() {
        return new BandanaCosmetic(
                "s9lab_pulse_bandana",
                "Pulse Bandana",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/bandanas/s9lab_pulse_bandana.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_bandana.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_bandana.animation.json"),
                true
        );
    }

    public static BandanaCosmetic voidBandana() {
        return new BandanaCosmetic(
                "s9lab_void_bandana",
                "Void Bandana",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/bandanas/s9lab_void_bandana.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_bandana.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_bandana.animation.json"),
                false
        );
    }

    public static BandanaCosmetic redBandana() {
        return new BandanaCosmetic(
                "s9lab_red_bandana",
                "Redstone Bandana",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/bandanas/s9lab_red_bandana.png"),
                Identifier.of(S9LabClient.MOD_ID, "geo/s9lab_bandana.geo.json"),
                Identifier.of(S9LabClient.MOD_ID, "animations/s9lab_bandana.animation.json"),
                false
        );
    }
}