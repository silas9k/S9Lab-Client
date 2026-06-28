package site.s9lab.s9labclient.client.cosmetics.shield;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

public record ShieldCosmetic(
        String id,
        String displayName,
        Identifier texture,
        Identifier model,
        Identifier animation,
        boolean animated
) implements Cosmetic {
    @Override
    public CosmeticType type() {
        return CosmeticType.SHIELD;
    }
}
