package site.s9lab.s9labclient.client.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleManager;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.KeybindSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.notification.S9ToastManager;

/** Maintains one backend-synced module snapshot per multiplayer server and one for singleplayer. */
public final class ServerModuleProfileManager {
    public static final String SETTINGS_KEY = "serverModuleProfiles";
    private static final String SINGLEPLAYER_ID = "singleplayer";
    private static final Map<String, StoredProfile> PROFILES = new LinkedHashMap<>();

    private static ModuleManager moduleManager;
    private static String activeProfileId;
    private static String activeProfileName;
    private static boolean initialized;

    private ServerModuleProfileManager() {
    }

    public static synchronized void initialize(ModuleManager manager) {
        if (initialized) {
            return;
        }
        initialized = true;
        moduleManager = manager;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(() -> activate(context(client))));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                client.execute(ServerModuleProfileManager::deactivate));
    }

    public static synchronized Map<String, Object> exportSettings() {
        captureActiveProfile();
        Map<String, Object> exported = new LinkedHashMap<>();
        for (Map.Entry<String, StoredProfile> entry : PROFILES.entrySet()) {
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("name", entry.getValue().name());
            profile.put("modules", deepCopy(entry.getValue().modules()));
            exported.put(entry.getKey(), profile);
        }
        return exported;
    }

    public static synchronized void importSettings(Object value) {
        if (!(value instanceof Map<?, ?> profiles)) {
            return;
        }

        Map<String, StoredProfile> imported = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : profiles.entrySet()) {
            if (imported.size() >= 96 || !(entry.getValue() instanceof Map<?, ?> data)) {
                continue;
            }
            String id = String.valueOf(entry.getKey());
            Object modulesValue = data.get("modules");
            if (id.isBlank() || !(modulesValue instanceof Map<?, ?> modules)) {
                continue;
            }
            String name = data.get("name") == null ? id : String.valueOf(data.get("name"));
            imported.put(id, new StoredProfile(limit(name, 160), stringMap(modules)));
        }
        PROFILES.clear();
        PROFILES.putAll(imported);
    }

    public static synchronized Map<String, Object> activeProfileModules() {
        StoredProfile profile = activeProfileId == null ? null : PROFILES.get(activeProfileId);
        return profile == null ? null : deepCopy(profile.modules());
    }

    public static synchronized void captureActiveProfile() {
        if (activeProfileId == null || moduleManager == null) {
            return;
        }
        PROFILES.put(activeProfileId, new StoredProfile(activeProfileName, snapshotModules()));
    }

    private static synchronized void activate(Context context) {
        if (context == null || context.id().equals(activeProfileId) || moduleManager == null) {
            return;
        }

        captureActiveProfile();
        StoredProfile target = PROFILES.get(context.id());
        activeProfileId = context.id();
        activeProfileName = context.name();

        if (target == null) {
            PROFILES.put(context.id(), new StoredProfile(context.name(), snapshotModules()));
            S9ToastManager.success("Module profile", "Created for " + context.name());
        } else {
            S9ToastManager.success("Module profile", "Switching to " + context.name());
            applyModules(target.modules());
        }
        persistOnce();
    }

    private static synchronized void deactivate() {
        if (activeProfileId == null) {
            return;
        }
        captureActiveProfile();
        activeProfileId = null;
        activeProfileName = null;
        persistOnce();
    }

    private static Context context(MinecraftClient client) {
        if (client.getServer() != null) {
            return new Context(SINGLEPLAYER_ID, "Singleplayer");
        }
        ServerInfo server = client.getCurrentServerEntry();
        if (server == null || server.address == null || server.address.isBlank()) {
            return null;
        }
        String address = normalizeAddress(server.address);
        String name = server.name == null || server.name.isBlank() ? address : server.name;
        return new Context("server_" + shortHash(address), limit(name, 160));
    }

    private static Map<String, Object> snapshotModules() {
        Map<String, Object> modules = new LinkedHashMap<>();
        for (Module module : moduleManager.getModules()) {
            Map<String, Object> moduleData = new LinkedHashMap<>();
            Map<String, Object> settings = new LinkedHashMap<>();
            moduleData.put("enabled", module.isEnabled());
            for (Setting<?> setting : module.getSettings()) {
                settings.put(setting.getName(), setting.getValue());
            }
            moduleData.put("settings", settings);
            modules.put(module.getName(), moduleData);
        }
        return modules;
    }

    private static void applyModules(Map<String, Object> modules) {
        for (Module module : moduleManager.getModules()) {
            if (!(modules.get(module.getName()) instanceof Map<?, ?> moduleData)) {
                continue;
            }
            if (moduleData.get("enabled") instanceof Boolean enabled) {
                module.setEnabled(enabled);
            }
            if (!(moduleData.get("settings") instanceof Map<?, ?> settings)) {
                continue;
            }
            for (Setting<?> setting : module.getSettings()) {
                applySetting(setting, settings.get(setting.getName()));
            }
        }
    }

    private static void applySetting(Setting<?> setting, Object value) {
        if (setting instanceof BooleanSetting target && value instanceof Boolean booleanValue) {
            target.setValue(booleanValue);
        } else if (setting instanceof NumberSetting target && value instanceof Number numberValue) {
            target.setValue(numberValue.doubleValue());
        } else if (setting instanceof ModeSetting target && value instanceof String stringValue) {
            target.setValue(stringValue);
        } else if (setting instanceof KeybindSetting target && value instanceof Number numberValue) {
            target.setValue(numberValue.intValue());
        }
    }

    private static void persistOnce() {
        if (S9LabClientClient.getConfigManager() != null) {
            S9LabClientClient.getConfigManager().save();
        }
    }

    private static String normalizeAddress(String address) {
        String normalized = address.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String shortHash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(24);
            for (int i = 0; i < 12; i++) {
                result.append(String.format(Locale.ROOT, "%02x", digest[i]));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static Map<String, Object> stringMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(String.valueOf(entry.getKey()), copyValue(entry.getValue()));
        }
        return result;
    }

    private static Map<String, Object> deepCopy(Map<String, Object> source) {
        return stringMap(source);
    }

    private static Object copyValue(Object value) {
        return value instanceof Map<?, ?> map ? stringMap(map) : value;
    }

    private static String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record Context(String id, String name) {
    }

    private record StoredProfile(String name, Map<String, Object> modules) {
    }
}
