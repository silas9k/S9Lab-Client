package site.s9lab.backend.cosmetics;

import site.s9lab.backend.api.dto.Dtos;

public record CosmeticDefinition(
        String id,
        String type,
        String name,
        String description,
        long price,
        boolean enabled
) {
    public Dtos.CosmeticDto dto() {
        return new Dtos.CosmeticDto(id, type, name, description, price, enabled);
    }
}
