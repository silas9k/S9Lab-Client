package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

import java.util.function.Supplier;

public record HatCosmetic(
        String id,
        String displayName,
        Identifier texture,
        Supplier<ModelPart> modelFactory
) implements Cosmetic {

    @Override
    public CosmeticType type() {
        return CosmeticType.HAT;
    }

    @Override
    public boolean animated() {
        return false;
    }

    public static HatCosmetic pirateHat() {
        return new HatCosmetic(
                "s9lab_pirate_hat",
                "Pirate Hat",
                Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/hats/s9lab_pirate_hat.png"),
                HatEntityModel::createModelPart
        );
    }

public static HatCosmetic guineaPigHat() {
    return new HatCosmetic(
            "s9lab_guinea_pig_hat",
            "Guinea Pig Hat",
            Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/hats/s9lab_guinea_pig_hat.png"),
            GuineaPigHatEntityModel::createModelPart
    );
}

public static HatCosmetic duckHat() {
    return new HatCosmetic(
            "s9lab_duck_hat",
            "Duck Hat",
            Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/hats/s9lab_duck_hat.png"),
            HatEntityModel::createDuckHatModelPart
    );
}

public static HatCosmetic monkeyHat() {
    return new HatCosmetic(
            "s9lab_monkey_hat",
            "Monkey Hat",
            net.minecraft.util.Identifier.of(
                    site.s9lab.s9labclient.S9LabClient.MOD_ID,
                    "textures/cosmetics/hats/s9lab_monkey_hat.png"
            ),
            MonkeyHatModelFactory::createModelPart
    );
}

}