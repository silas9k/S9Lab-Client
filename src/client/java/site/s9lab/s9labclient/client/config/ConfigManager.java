package site.s9lab.s9labclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleManager;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.KeybindSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_BACKEND_BASE_URL = "http://31.70.89.55:25614/api/v1";
    private static final String DEFAULT_BACKEND_WEBSOCKET_URL = "ws://31.70.89.55:8789";

    private final ModuleManager moduleManager;
    private final CosmeticRegistry cosmeticRegistry;
    private final Path configPath;
    private boolean discordRpcEnabled = true;
    private boolean discordRpcShowServer = true;
    private boolean backendShowOfflineWarnings = true;
    private int uiAccentColor = 0xFF5E7CE2;
    private boolean uiBlurEnabled = true;
    private String[] emoteWheelSlots = new String[] {"", "", "", ""};

    public ConfigManager(ModuleManager moduleManager, CosmeticRegistry cosmeticRegistry) {
        this.moduleManager = moduleManager;
        this.cosmeticRegistry = cosmeticRegistry;
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(S9LabClient.MOD_ID + ".json");
    }

    public void load() {
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            ClientConfig config = GSON.fromJson(reader, ClientConfig.class);
            if (config == null) {
                save();
                return;
            }

            apply(config);
            save();
        } catch (IOException | JsonSyntaxException exception) {
            save();
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(createSnapshot(), writer);
            }
            BackendClient.pushSettingsAsync();
        } catch (IOException ignored) {
        }
    }

    private void apply(ClientConfig config) {
        if (config.local != null) {
            backendShowOfflineWarnings = config.local.showOfflineWarnings;
        }
    }

    private void applySetting(Setting<?> setting, Object value) {
        if (value == null) {
            return;
        }

        if (setting instanceof BooleanSetting booleanSetting && value instanceof Boolean booleanValue) {
            booleanSetting.setValue(booleanValue);
        } else if (setting instanceof NumberSetting numberSetting && value instanceof Number numberValue) {
            numberSetting.setValue(numberValue.doubleValue());
        } else if (setting instanceof ModeSetting modeSetting && value instanceof String stringValue) {
            modeSetting.setValue(stringValue);
        } else if (setting instanceof KeybindSetting keybindSetting && value instanceof Number numberValue) {
            keybindSetting.setValue(numberValue.intValue());
        }
    }

    private ClientConfig createSnapshot() {
        ClientConfig config = new ClientConfig();
        config.local.showOfflineWarnings = backendShowOfflineWarnings;
        return config;
    }

    public List<String> getUnlockedCosmetics() {
        return new ArrayList<>(BackendState.ownedSnapshot());
    }

    public boolean isUnlocked(String cosmeticId) {
        return BackendState.owned(cosmeticId);
    }

    public boolean equipCosmetic(CosmeticType type, String cosmeticId) {
        Optional<Cosmetic> cosmetic = cosmeticRegistry.get(cosmeticId);
        if (cosmetic.isEmpty() || cosmetic.get().type() != type) {
            return false;
        }
        if (!isUnlocked(cosmeticId)) {
            return false;
        }

        return true;
    }

    public void unequipCosmetic(CosmeticType type) {
    }

    public boolean applyBackendProfile(java.util.Collection<String> owned, Map<String, String> equipped) {
        return false;
    }

    public Optional<Cosmetic> getEquippedCosmetic(CosmeticType type) {
        String id = BackendState.equippedId(type);
        return id == null ? Optional.empty() : cosmeticRegistry.get(id).filter(cosmetic -> cosmetic.type() == type);
    }

    public String getEquippedCosmeticId(CosmeticType type) {
        return getEquippedCosmetic(type).map(Cosmetic::id).orElse("");
    }

    public boolean isDiscordRpcEnabled() {
        return discordRpcEnabled;
    }

    public boolean shouldDiscordRpcShowServer() {
        return discordRpcShowServer;
    }

    public void setDiscordRpcEnabled(boolean enabled) {
        discordRpcEnabled = enabled;
    }

    public void setDiscordRpcShowServer(boolean showServer) {
        discordRpcShowServer = showServer;
    }

    public boolean isBackendEnabled() {
        return true;
    }

    public void setBackendEnabled(boolean backendEnabled) {
    }

    public String getBackendBaseUrl() {
        return normalizeUrl(System.getProperty("s9lab.backend.baseUrl",
                System.getenv().getOrDefault("S9LAB_BACKEND_BASE_URL", DEFAULT_BACKEND_BASE_URL)), DEFAULT_BACKEND_BASE_URL);
    }

    public String getBackendWebsocketUrl() {
        return normalizeUrl(System.getProperty("s9lab.backend.websocketUrl",
                System.getenv().getOrDefault("S9LAB_BACKEND_WEBSOCKET_URL", DEFAULT_BACKEND_WEBSOCKET_URL)), DEFAULT_BACKEND_WEBSOCKET_URL);
    }

    public boolean shouldBackendShowOfflineWarnings() {
        return backendShowOfflineWarnings;
    }

    public int getUiAccentColor() {
        return uiAccentColor;
    }

    public void setUiAccentColor(int accentColor) {
        uiAccentColor = 0xFF000000 | (accentColor & 0x00FFFFFF);
    }

    public boolean isUiBlurEnabled() {
        return uiBlurEnabled;
    }

    public void setUiBlurEnabled(boolean uiBlurEnabled) {
        this.uiBlurEnabled = uiBlurEnabled;
    }

    public String getEmoteWheelSlot(int slot) {
        if (slot < 0 || slot >= 4) {
            return "";
        }
        emoteWheelSlots = normalizeEmoteSlots(emoteWheelSlots);
        return emoteWheelSlots[slot];
    }

    public String[] getEmoteWheelSlots() {
        emoteWheelSlots = normalizeEmoteSlots(emoteWheelSlots);
        return emoteWheelSlots.clone();
    }

    public void setEmoteWheelSlot(int slot, String emoteId) {
        if (slot < 0 || slot >= 4) {
            return;
        }
        emoteWheelSlots = normalizeEmoteSlots(emoteWheelSlots);
        emoteWheelSlots[slot] = emoteId == null ? "" : emoteId;
    }

    public Map<String, Object> backendSettingsSnapshot() {
        Map<String, Object> root = new LinkedHashMap<>();
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
        root.put("modules", modules);
        root.put("discordRpc", Map.of(
                "enabled", discordRpcEnabled,
                "showServer", discordRpcShowServer
        ));
        root.put("ui", Map.of(
                "accentColor", uiAccentColor,
                "blurEnabled", uiBlurEnabled
        ));
        root.put("emoteWheelSlots", List.of(normalizeEmoteSlots(emoteWheelSlots)));
        return root;
    }

    public void applyBackendSettings(Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }

        Map<?, ?> modules = asMap(settings.get("modules"));
        if (modules != null) {
            for (Module module : moduleManager.getModules()) {
                Map<?, ?> moduleData = asMap(modules.get(module.getName()));
                if (moduleData == null) {
                    continue;
                }
                Object enabled = moduleData.get("enabled");
                if (enabled instanceof Boolean booleanValue) {
                    module.setEnabled(booleanValue);
                }
                Map<?, ?> moduleSettings = asMap(moduleData.get("settings"));
                if (moduleSettings != null) {
                    for (Setting<?> setting : module.getSettings()) {
                        applySetting(setting, moduleSettings.get(setting.getName()));
                    }
                }
            }
        }

        Map<?, ?> discord = asMap(settings.get("discordRpc"));
        if (discord != null) {
            Object enabled = discord.get("enabled");
            Object showServer = discord.get("showServer");
            if (enabled instanceof Boolean booleanValue) {
                discordRpcEnabled = booleanValue;
            }
            if (showServer instanceof Boolean booleanValue) {
                discordRpcShowServer = booleanValue;
            }
        }

        Map<?, ?> ui = asMap(settings.get("ui"));
        if (ui != null) {
            Object accent = ui.get("accentColor");
            Object blur = ui.get("blurEnabled");
            if (accent instanceof Number number) {
                uiAccentColor = 0xFF000000 | (number.intValue() & 0x00FFFFFF);
            }
            if (blur instanceof Boolean booleanValue) {
                uiBlurEnabled = booleanValue;
            }
            ThemeManager.loadFromConfig();
        }

        Object slots = settings.get("emoteWheelSlots");
        if (slots instanceof List<?> list) {
            String[] loaded = new String[] {"", "", "", ""};
            for (int i = 0; i < Math.min(4, list.size()); i++) {
                Object value = list.get(i);
                loaded[i] = value == null ? "" : String.valueOf(value);
            }
            emoteWheelSlots = normalizeEmoteSlots(loaded);
        }
    }

    private static String[] normalizeEmoteSlots(String[] slots) {
        String[] normalized = new String[] {"", "", "", ""};
        if (slots == null) {
            return normalized;
        }
        for (int i = 0; i < Math.min(4, slots.length); i++) {
            normalized[i] = slots[i] == null ? "" : slots[i];
        }
        return normalized;
    }

    private static Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : null;
    }

    private static String normalizeUrl(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
