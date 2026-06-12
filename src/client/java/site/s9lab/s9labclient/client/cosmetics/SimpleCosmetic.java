package site.s9lab.s9labclient.client.cosmetics;

import net.minecraft.util.Identifier;

public record SimpleCosmetic(String id, String displayName, CosmeticType type, Identifier texture, boolean animated) implements Cosmetic {
}
