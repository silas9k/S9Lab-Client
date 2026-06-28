package site.s9lab.s9labclient.client.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class ModDetectionManager {
    private static final Map<String, List<String>> MOD_ID_ALIASES = Map.of(
            "dynamic_fps", List.of("dynamicfps")
    );

    public static final List<TrackedMod> TRACKED_MODS = List.of(
            new TrackedMod("sodium", "Sodium", true, "modern renderer and large FPS gains"),
            new TrackedMod("lithium", "Lithium", true, "server/client logic optimizations"),
            new TrackedMod("ferritecore", "FerriteCore", true, "lower memory usage"),
            new TrackedMod("entityculling", "EntityCulling", true, "skips hidden entity rendering"),
            new TrackedMod("moreculling", "MoreCulling", false, "extra block and item culling"),
            new TrackedMod("immediatelyfast", "ImmediatelyFast", true, "faster GUI and rendering paths"),
            new TrackedMod("dynamic_fps", "DynamicFPS", false, "saves resources while unfocused")
    );

    private final Map<String, DetectedMod> detected = new LinkedHashMap<>();

    public ModDetectionManager() {
        refresh();
    }

    public synchronized void refresh() {
        FabricLoader loader = FabricLoader.getInstance();
        detected.clear();
        for (TrackedMod tracked : TRACKED_MODS) {
            String loadedModId = loadedModId(loader, tracked.modId());
            boolean installed = loadedModId != null;
            String version = installed
                    ? loader.getModContainer(loadedModId)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown")
                    : "";
            detected.put(tracked.modId(), new DetectedMod(
                    tracked.modId(),
                    tracked.displayName(),
                    tracked.important(),
                    tracked.benefit(),
                    installed,
                    version
            ));
        }
    }

    private static String loadedModId(FabricLoader loader, String primaryModId) {
        if (loader.isModLoaded(primaryModId)) {
            return primaryModId;
        }
        for (String alias : MOD_ID_ALIASES.getOrDefault(primaryModId, List.of())) {
            if (loader.isModLoaded(alias)) {
                return alias;
            }
        }
        return null;
    }

    public synchronized List<DetectedMod> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(detected.values()));
    }

    public synchronized boolean isLoaded(String modId) {
        String normalized = modId.toLowerCase(Locale.ROOT);
        DetectedMod mod = detected.get(normalized);
        return mod != null && mod.installed();
    }

    public synchronized List<DetectedMod> missingImportantMods() {
        return detected.values().stream()
                .filter(DetectedMod::important)
                .filter(mod -> !mod.installed())
                .toList();
    }

    public record TrackedMod(String modId, String displayName, boolean important, String benefit) {
    }

    public record DetectedMod(
            String modId,
            String displayName,
            boolean important,
            String benefit,
            boolean installed,
            String version
    ) {
    }
}
