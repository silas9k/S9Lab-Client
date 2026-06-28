package site.s9lab.s9labclient.client.cosmetics;

import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.Identifier;

/**
 * Zentrales, datengetriebenes Manifest fuer Cosmetics.
 *
 * <p>Die eigentlichen Renderer sollen moeglichst wenig hart codieren.
 * Stattdessen haengt alles Wichtige an diesem Manifest:
 * Typ, Variantengruppe, Preview-Defaults, optionale GeckoLib-Resourcen
 * und typ-spezifische Zusatzdaten wie Halo-Motion.</p>
 */
public record CosmeticManifest(
        String id,
        String displayName,
        CosmeticType type,
        Identifier texture,
        Identifier model,
        Identifier animation,
        boolean animated,
        String category,
        String variantGroup,
        int sortOrder,
        PreviewManifest preview,
        HaloManifest halo
) {
    public CosmeticManifest {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(type, "type");
        preview = preview == null ? PreviewManifest.DEFAULT : preview;
        category = normalize(category);
        variantGroup = normalize(variantGroup);
        halo = halo == null ? HaloManifest.DEFAULT : halo;
    }

    public static CosmeticManifest fromCosmetic(Cosmetic cosmetic) {
        return new CosmeticManifest(
                cosmetic.id(),
                cosmetic.displayName(),
                cosmetic.type(),
                cosmetic.texture(),
                null,
                null,
                cosmetic.animated(),
                cosmetic.type().commandName(),
                cosmetic.type().commandName(),
                0,
                PreviewManifest.DEFAULT,
                HaloManifest.DEFAULT
        );
    }

    public boolean matchesVariantGroup(CosmeticManifest other) {
        if (other == null) {
            return false;
        }
        if (!variantGroup.isBlank() && !other.variantGroup.isBlank()) {
            return variantGroup.equals(other.variantGroup);
        }
        return type == other.type;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Vorschau-Defaults fuer Katalog, Detailseite und Studio.
     */
    public record PreviewManifest(
            String pose,
            float yaw,
            float pitch,
            int zoom
    ) {
        public static final PreviewManifest DEFAULT = new PreviewManifest("idle", 180.0F, 8.0F, 78);
    }

    /**
     * Typ-spezifische Bewegungsdaten fuer Halos.
     */
    public record HaloManifest(
            float scale,
            float orbitRadius,
            float orbitSpeed,
            float bobAmplitude,
            float spinSpeed,
            float verticalOffset
    ) {
        public static final HaloManifest DEFAULT = new HaloManifest(0.82F, 0.0F, 0.0F, 0.018F, 0.20F, 0.08F);
    }
}
