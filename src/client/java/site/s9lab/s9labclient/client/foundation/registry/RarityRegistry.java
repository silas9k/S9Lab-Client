package site.s9lab.s9labclient.client.foundation.registry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import site.s9lab.s9labclient.client.foundation.model.CosmeticRarity;

public final class RarityRegistry {
    private static final List<CosmeticRarity> ORDERED = Arrays.stream(CosmeticRarity.values())
            .sorted(Comparator.comparingInt(CosmeticRarity::sortOrder))
            .toList();

    private RarityRegistry() {
    }

    public static CosmeticRarity resolve(String value) {
        return CosmeticRarity.fromWire(value);
    }

    public static List<CosmeticRarity> ordered() {
        return ORDERED;
    }
}
