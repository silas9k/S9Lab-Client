package site.s9lab.s9labclient.client.cosmetics.wings;

import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

public record WingCosmetic(String id, String displayName, Identifier texture, boolean animated) implements Cosmetic {
    @Override
    public CosmeticType type() {
        return CosmeticType.WINGS;
    }

    public static WingCosmetic dragonWings() {
        return new WingCosmetic(
                "s9lab_dragon_wings",
                "Dragon Wings",
                Identifier.of("minecraft", "textures/entity/enderdragon/dragon.png"),
                true
        );
    }

    public static WingCosmetic colorDragonWings() {
        return new WingCosmetic(
                "s9lab_color_dragon_wings",
                "Color Dragon Wings",
                Identifier.of("minecraft", "textures/entity/enderdragon/dragon.png"),
                true
        );
    }
}