package site.s9lab.s9labclient.client.cosmetics;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import site.s9lab.s9labclient.client.cosmetics.hat.HatCosmetic;

public class CosmeticRegistry {
    private final Map<String, Cosmetic> cosmetics = new LinkedHashMap<>();

    public void registerDefaults() {
        cosmetics.clear();
        CosmeticCatalogLoader.loadInto(this);

        register(HatCosmetic.pirateHat());
        register(HatCosmetic.guineaPigHat());
        register(HatCosmetic.duckHat());
        register(HatCosmetic.monkeyHat());
    }

    public void register(Cosmetic cosmetic) {
        cosmetics.put(cosmetic.id(), cosmetic);
    }

    public Optional<Cosmetic> get(String id) {
        return Optional.ofNullable(cosmetics.get(id));
    }

    public Collection<Cosmetic> all() {
        return cosmetics.values();
    }

    public List<Cosmetic> byType(CosmeticType type) {
        return cosmetics.values().stream()
                .filter(cosmetic -> cosmetic.type() == type)
                .toList();
    }

    public Optional<Cosmetic> firstByType(CosmeticType type) {
        return byType(type).stream().findFirst();
    }

    public List<String> idsByType(CosmeticType type) {
        return byType(type).stream().map(Cosmetic::id).toList();
    }
}