package site.s9lab.s9labclient.client.dev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.notification.S9ToastManager;

public final class DevModeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("s9labclient-dev-mode.json");
    private static volatile boolean enabled;
    private static volatile String adminSecret = "";
    private static volatile String loadedUuid = "";

    private DevModeManager() {
    }

    public static boolean isEnabled() {
        loadForCurrentAccount();
        return enabled && !adminSecret.isBlank();
    }

    public static String adminSecret() {
        loadForCurrentAccount();
        return adminSecret;
    }

    public static String currentAccountUuid() {
        return currentUuid();
    }

    public static boolean hasStoredSecretForCurrentAccount() {
        loadForCurrentAccount();
        return !adminSecret.isBlank();
    }

    public static void disable() {
        loadForCurrentAccount();
        String uuid = currentUuid();
        enabled = false;
        adminSecret = "";
        save(uuid);
        S9ToastManager.warning("Dev mode disabled", "Screenshot uploads are hidden again.");
    }

    public static void verify(String secret, Consumer<Boolean> success, Consumer<String> failure) {
        String trimmed = secret == null ? "" : secret.trim();
        if (trimmed.isBlank()) {
            failure.accept("Bitte Admin Secret eingeben.");
            return;
        }
        BackendClient.verifyAdminSecret(trimmed, ignored -> {
            String uuid = currentUuid();
            enabled = true;
            adminSecret = trimmed;
            save(uuid);
            S9ToastManager.success("Dev mode enabled", "Screenshot uploads are now visible.");
            success.accept(true);
        }, message -> {
            enabled = false;
            adminSecret = "";
            failure.accept(message);
        });
    }

    private static void loadForCurrentAccount() {
        String uuid = currentUuid();
        if (uuid.equals(loadedUuid)) {
            return;
        }
        loadedUuid = uuid;
        enabled = false;
        adminSecret = "";
        if (uuid.isBlank() || !Files.isRegularFile(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            Store store = GSON.fromJson(reader, Store.class);
            Entry entry = store == null || store.accounts == null ? null : store.accounts.get(uuid);
            if (entry != null && entry.enabled && entry.adminSecret != null && !entry.adminSecret.isBlank()) {
                enabled = true;
                adminSecret = entry.adminSecret;
            }
        } catch (Exception exception) {
            S9LabClient.LOGGER.warn("Could not load S9Lab dev mode config", exception);
        }
    }

    private static void save(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return;
        }
        Store store = new Store();
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                Store loaded = GSON.fromJson(reader, Store.class);
                if (loaded != null && loaded.accounts != null) {
                    store.accounts.putAll(loaded.accounts);
                }
            } catch (Exception exception) {
                S9LabClient.LOGGER.warn("Could not read S9Lab dev mode config before save", exception);
            }
        }
        if (enabled && !adminSecret.isBlank()) {
            store.accounts.put(uuid, new Entry(true, adminSecret));
        } else {
            store.accounts.remove(uuid);
        }
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(store, writer);
            }
        } catch (Exception exception) {
            S9LabClient.LOGGER.warn("Could not save S9Lab dev mode config", exception);
        }
    }

    private static String currentUuid() {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = client == null || client.getSession() == null ? null : client.getSession().getUuidOrNull();
        if (uuid == null && client != null && client.player != null) {
            uuid = client.player.getUuid();
        }
        return uuid == null ? "" : uuid.toString();
    }

    private static final class Store {
        private Map<String, Entry> accounts = new LinkedHashMap<>();
    }

    private record Entry(boolean enabled, String adminSecret) {
    }
}
