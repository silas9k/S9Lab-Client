package site.s9lab.s9labclient.client.cosmetics;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import site.s9lab.s9labclient.client.cosmetics.hat.HatCosmetic;

public class CosmeticRegistry {
    private final Map<String, Cosmetic> cosmetics = new LinkedHashMap<>();
    private final Map<String, CosmeticManifest> manifests = new LinkedHashMap<>();

    public void registerDefaults() {
        cosmetics.clear();
        manifests.clear();
        CosmeticCatalogLoader.loadInto(this);

        register(HatCosmetic.pirateHat());
        register(HatCosmetic.guineaPigHat());
        register(HatCosmetic.duckHat());
        register(HatCosmetic.monkeyHat());
    }

    public void register(Cosmetic cosmetic) {
        register(cosmetic, CosmeticManifest.fromCosmetic(cosmetic));
    }

    public void register(Cosmetic cosmetic, CosmeticManifest manifest) {
        cosmetics.put(cosmetic.id(), cosmetic);
        manifests.put(cosmetic.id(), manifest == null ? CosmeticManifest.fromCosmetic(cosmetic) : manifest);
    }

    public Optional<Cosmetic> get(String id) {
        return Optional.ofNullable(cosmetics.get(id));
    }

    public Optional<CosmeticManifest> manifest(String id) {
        return Optional.ofNullable(manifests.get(id));
    }

    public Collection<Cosmetic> all() {
        return cosmetics.values();
    }

    public Collection<CosmeticManifest> allManifests() {
        return manifests.values();
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

    /**
     * Laedt den kompletten Cosmetic-Bestand neu aus den Datenquellen.
     *
     * <p>Diese Methode ist die kuenftige Basis fuer Admin-Reloads und
     * Backend-gestuetzte Content-Aktualisierungen, ohne den Client
     * neu starten zu muessen.</p>
     */
    public void reload() {
        registerDefaults();
    }
}
