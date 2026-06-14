package site.s9lab.backend.cosmetics;

import site.s9lab.backend.api.dto.Dtos;

public record CosmeticDefinition(
        String id,
        String type,
        String name,
        String description,
        long price,
        boolean enabled,
        String rarity,
        boolean limited,
        long availableFrom,
        long availableUntil,
        boolean plusExclusive,
        String limitedText,
        String previewAsset
) {
    public CosmeticDefinition(String id, String type, String name, String description, long price, boolean enabled) {
        this(id, type, name, description, price, enabled, "COMMON", false, 0L, 0L, false, "", "");
    }

    public Dtos.CosmeticDto dto() {
        return new Dtos.CosmeticDto(id, type, name, description, price, enabled, rarity, limited, availableFrom, availableUntil, plusExclusive, limitedText, previewAsset, java.util.Map.of());
    }
}
