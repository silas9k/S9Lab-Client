package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

/**
 * Halo-Cosmetic mit vollständigen GeckoLib-Resourcen und Bewegungswerten.
 *
 * <p>Der Halo soll nicht mehr wie ein hart codierter Sonderfall wirken,
 * sondern wie ein normales, datengetriebenes Cosmetic: Texture, Model,
 * Animation und die kleine Orbit-Bewegung kommen aus den Cosmetic-Daten.</p>
 */
public record HaloCosmetic(
        String id,
        String displayName,
        Identifier texture,
        Identifier model,
        Identifier animation,
        boolean animated,
        float scale,
        float orbitRadius,
        float orbitSpeed,
        float bobAmplitude,
        float spinSpeed,
        float verticalOffset
) implements Cosmetic {
    public static final Identifier DEFAULT_MODEL =
            Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo");

    public static final Identifier DEFAULT_TEXTURE =
            Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/halos/s9lab_gold_halo.png");

    public static final Identifier DEFAULT_ANIMATION =
            Identifier.of(S9LabClient.MOD_ID, "s9lab_gold_halo.animation");

    public HaloCosmetic {
        // Defensive Defaults: Wenn ein JSON-Eintrag etwas vergisst, soll der Halo
        // trotzdem sauber rendern statt im Renderer zu explodieren.
        texture = texture == null ? DEFAULT_TEXTURE : texture;
        model = model == null ? DEFAULT_MODEL : model;
        animation = animation == null ? DEFAULT_ANIMATION : animation;
    }

    @Override
    public CosmeticType type() {
        return CosmeticType.HALO;
    }

    public Identifier effectiveModel() {
        return model == null ? DEFAULT_MODEL : model;
    }

    public Identifier effectiveAnimation() {
        return animation == null ? DEFAULT_ANIMATION : animation;
    }

    public static HaloCosmetic goldHalo() {
        return halo(
                "s9lab_gold_halo",
                "Gold Halo",
                "textures/cosmetics/halos/s9lab_gold_halo.png",
                true,
                0.88F,
                0.10F,
                0.80F,
                0.028F,
                0.58F,
                0.02F
        );
    }

    public static HaloCosmetic voidHalo() {
        return halo(
                "s9lab_void_halo",
                "Void Halo",
                "textures/cosmetics/halos/s9lab_void_halo.png",
                true,
                0.90F,
                0.11F,
                0.92F,
                0.030F,
                0.68F,
                0.03F
        );
    }

    public static HaloCosmetic plusHalo() {
        return halo(
                "s9lab_plus_halo",
                "S9Lab+ Halo",
                "textures/cosmetics/halos/plus_edition_s9lab_halo.png",
                true,
                0.92F,
                0.12F,
                0.88F,
                0.034F,
                0.72F,
                0.03F
        );
    }

    public static HaloCosmetic halo(
            String id,
            String displayName,
            String texturePath,
            boolean animated,
            float scale,
            float orbitRadius,
            float orbitSpeed,
            float bobAmplitude,
            float spinSpeed,
            float verticalOffset
    ) {
        return new HaloCosmetic(
                id,
                displayName,
                Identifier.of(S9LabClient.MOD_ID, texturePath),
                DEFAULT_MODEL,
                DEFAULT_ANIMATION,
                animated,
                scale,
                orbitRadius,
                orbitSpeed,
                bobAmplitude,
                spinSpeed,
                verticalOffset
        );
    }
}
