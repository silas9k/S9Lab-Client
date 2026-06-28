package site.s9lab.s9labclient.client.performance;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.impl.performance.PerformanceOptimizerModule;
import site.s9lab.s9labclient.client.notification.S9ToastManager;

public final class PerformanceManager {
    private static final int MAX_FPS_SAMPLES = 240;
    private static final long SAMPLE_INTERVAL_MILLIS = 250L;
    private static final long METRIC_REFRESH_MILLIS = 1_000L;
    private static final long AVERAGE_REFRESH_MILLIS = 2_000L;
    private static final long STARTUP_WARNING_DELAY_MILLIS = 10_000L;

    private final ModDetectionManager modDetectionManager;
    private final Deque<Integer> fpsSamples = new ArrayDeque<>();
    private long lastSampleMillis;
    private long lastMetricRefreshMillis;
    private long lastAverageRefreshMillis;
    private Object sampledWorld;
    private final long createdAtMillis = System.currentTimeMillis();
    private boolean startupWarningShown;
    private PerformancePreset activePreset = PerformancePreset.BALANCED;
    private Metrics cachedMetrics = Metrics.empty();
    private int stableAverageFps;

    public PerformanceManager(ModDetectionManager modDetectionManager) {
        this.modDetectionManager = modDetectionManager;
    }

    public void tick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (client != null && client.world != sampledWorld) {
            sampledWorld = client.world;
            fpsSamples.clear();
            stableAverageFps = 0;
            lastSampleMillis = now;
            lastAverageRefreshMillis = now;
        }
        if (client != null && client.world != null && client.currentScreen == null
                && now - lastSampleMillis >= SAMPLE_INTERVAL_MILLIS) {
            lastSampleMillis = now;
            sampleFps(client);
        }
        if (now - lastAverageRefreshMillis >= AVERAGE_REFRESH_MILLIS) {
            lastAverageRefreshMillis = now;
            stableAverageFps = averageFps();
        }
        if (now - lastMetricRefreshMillis >= METRIC_REFRESH_MILLIS) {
            lastMetricRefreshMillis = now;
            cachedMetrics = computeMetrics(client);
        }

        if (!startupWarningShown && now - createdAtMillis >= STARTUP_WARNING_DELAY_MILLIS) {
            startupWarningShown = true;
            if (missingModWarningsEnabled()) {
                warnAboutMissingImportantMods();
            }
        }
    }

    public ModDetectionManager modDetectionManager() {
        return modDetectionManager;
    }

    public PerformancePreset activePreset() {
        return activePreset;
    }

    public Metrics metrics() {
        return cachedMetrics;
    }

    public Metrics computeMetrics(MinecraftClient client) {
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L;
        long maxMb = runtime.maxMemory() / 1024L / 1024L;
        return new Metrics(
                client == null ? 0 : client.getCurrentFps(),
                stableAverageFps,
                usedMb,
                maxMb,
                loadedChunks(client),
                entityCount(client),
                renderDistance(client)
        );
    }

    public void refreshModDetection() {
        modDetectionManager.refresh();
    }

    public void applyPreset(PerformancePreset preset, MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }
        activePreset = preset;
        Object options = client.options;
        switch (preset) {
            case QUALITY -> {
                setSimpleOption(options, "getViewDistance", 12);
                setSimpleOption(options, "getSimulationDistance", 8);
                setSimpleOption(options, "getEntityShadows", true);
                setEnumOption(options, "getCloudRenderMode", "FANCY", "FAST");
                setEnumOption(options, "getParticles", "ALL");
                setEnumOption(options, "getGraphicsMode", "FANCY");
            }
            case BALANCED -> {
                setSimpleOption(options, "getViewDistance", 8);
                setSimpleOption(options, "getSimulationDistance", 6);
                setSimpleOption(options, "getEntityShadows", true);
                setEnumOption(options, "getCloudRenderMode", "FAST", "OFF");
                setEnumOption(options, "getParticles", "DECREASED", "ALL");
                setEnumOption(options, "getGraphicsMode", "FAST", "FANCY");
            }
            case MAX_FPS -> {
                setSimpleOption(options, "getViewDistance", 6);
                setSimpleOption(options, "getSimulationDistance", 5);
                setSimpleOption(options, "getEntityShadows", false);
                setEnumOption(options, "getCloudRenderMode", "OFF");
                setEnumOption(options, "getParticles", "MINIMAL", "DECREASED");
                setEnumOption(options, "getGraphicsMode", "FAST");
            }
        }
        writeOptions(options);
        S9ToastManager.success("Performance preset", preset.displayName() + " applied");
    }

    public boolean lightweightUiEnabled() {
        return performanceModule().map(PerformanceOptimizerModule::lightweightUi).orElse(true);
    }

    public boolean missingModWarningsEnabled() {
        return performanceModule().map(PerformanceOptimizerModule::missingModWarnings).orElse(true);
    }

    private static java.util.Optional<PerformanceOptimizerModule> performanceModule() {
        if (S9LabClientClient.getModuleManager() == null) {
            return java.util.Optional.empty();
        }
        return S9LabClientClient.getModuleManager().getModule("Performance Optimizer")
                .filter(PerformanceOptimizerModule.class::isInstance)
                .map(PerformanceOptimizerModule.class::cast);
    }

    private void warnAboutMissingImportantMods() {
        int missing = modDetectionManager.missingImportantMods().size();
        if (missing <= 0) {
            return;
        }
        S9ToastManager.warning("Performance mods missing", missing + " recommended mod" + (missing == 1 ? "" : "s") + " not detected");
    }

    private void sampleFps(MinecraftClient client) {
        if (client == null) {
            return;
        }
        int fps = Math.max(0, client.getCurrentFps());
        fpsSamples.addLast(fps);
        while (fpsSamples.size() > MAX_FPS_SAMPLES) {
            fpsSamples.removeFirst();
        }
    }

    private int averageFps() {
        if (fpsSamples.isEmpty()) {
            return 0;
        }
        long total = 0L;
        for (int fps : fpsSamples) {
            total += fps;
        }
        return Math.round((float) total / fpsSamples.size());
    }

    private static int loadedChunks(MinecraftClient client) {
        if (client == null || client.world == null) {
            return -1;
        }
        Object chunkManager = invokeNoArg(client.world, "getChunkManager");
        Object count = firstNoArg(chunkManager, "getLoadedChunkCount", "getLoadedChunksCount", "getChunksLoadedCount");
        return numberOrMinusOne(count);
    }

    private static int entityCount(MinecraftClient client) {
        if (client == null || client.world == null) {
            return -1;
        }
        Object entities = invokeNoArg(client.world, "getEntities");
        if (entities instanceof Iterable<?> iterable) {
            int count = 0;
            for (Object ignored : iterable) {
                count++;
            }
            return count;
        }
        Object players = invokeNoArg(client.world, "getPlayers");
        if (players instanceof Iterable<?> iterable) {
            int count = 0;
            for (Object ignored : iterable) {
                count++;
            }
            return count;
        }
        return -1;
    }

    private static int renderDistance(MinecraftClient client) {
        if (client == null || client.options == null) {
            return -1;
        }
        return numberOrMinusOne(simpleOptionValue(client.options, "getViewDistance"));
    }

    private static int numberOrMinusOne(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string.replaceAll("[^0-9-]", ""));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    private static Object simpleOptionValue(Object options, String getterName) {
        Object option = invokeNoArg(options, getterName);
        return invokeNoArg(option, "getValue");
    }

    private static void setSimpleOption(Object options, String getterName, Object value) {
        Object option = invokeNoArg(options, getterName);
        if (option == null) {
            return;
        }
        invokeOneArg(option, "setValue", value);
    }

    private static void setEnumOption(Object options, String getterName, String... preferredNames) {
        Object option = invokeNoArg(options, getterName);
        Object current = invokeNoArg(option, "getValue");
        if (!(current instanceof Enum<?> currentEnum)) {
            return;
        }
        Class<?> enumClass = currentEnum.getDeclaringClass();
        for (String preferredName : preferredNames) {
            Object constant = enumConstant(enumClass, preferredName);
            if (constant != null) {
                invokeOneArg(option, "setValue", constant);
                return;
            }
        }
    }

    private static Object enumConstant(Class<?> enumClass, String name) {
        if (!enumClass.isEnum()) {
            return null;
        }
        for (Object constant : enumClass.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
        return null;
    }

    private static Object firstNoArg(Object target, String... names) {
        for (String name : names) {
            Object value = invokeNoArg(target, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static void invokeOneArg(Object target, String methodName, Object value) {
        if (target == null) {
            return;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            try {
                method.invoke(target, value);
                return;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return;
            }
        }
    }

    private static void writeOptions(Object options) {
        for (String methodName : List.of("write", "writeChanges", "save")) {
            try {
                Method method = options.getClass().getMethod(methodName);
                method.invoke(options);
                return;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        S9LabClient.LOGGER.debug("No writable options method found after performance preset application.");
    }

    public enum PerformancePreset {
        QUALITY("Quality"),
        BALANCED("Balanced"),
        MAX_FPS("Max FPS");

        private final String displayName;

        PerformancePreset(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public String commandName() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }

    public record Metrics(
            int fps,
            int averageFps,
            long usedRamMb,
            long maxRamMb,
            int loadedChunks,
            int entityCount,
            int renderDistance
    ) {
        private static Metrics empty() {
            return new Metrics(0, 0, 0L, 0L, -1, -1, -1);
        }
    }
}
