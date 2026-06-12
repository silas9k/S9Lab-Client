package site.s9lab.s9labclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import site.s9lab.s9labclient.S9LabClient;
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

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ModuleManager moduleManager;
    private final CosmeticRegistry cosmeticRegistry;
    private final Path configPath;
    private final Set<String> unlockedCosmetics = new LinkedHashSet<>();
    private final Map<CosmeticType, String> equippedCosmetics = new LinkedHashMap<>();
    private boolean discordRpcEnabled = true;
    private boolean discordRpcShowServer = true;
    private int uiAccentColor = 0xFF5E7CE2;
    private boolean uiBlurEnabled = true;

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
            if (config == null || config.modules == null) {
                save();
                return;
            }

            apply(config);
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
        } catch (IOException ignored) {
        }
    }

    private void apply(ClientConfig config) {
        for (Module module : moduleManager.getModules()) {
            ClientConfig.ModuleConfig moduleConfig = config.modules.get(module.getName());
            if (moduleConfig == null) {
                continue;
            }

            module.setEnabled(moduleConfig.enabled);
            if (moduleConfig.settings == null) {
                continue;
            }

            for (Setting<?> setting : module.getSettings()) {
                Object value = moduleConfig.settings.get(setting.getName());
                applySetting(setting, value);
            }
        }

        unlockedCosmetics.clear();
        if (config.unlockedCosmetics != null) {
            unlockedCosmetics.addAll(config.unlockedCosmetics);
        }

        equippedCosmetics.clear();
        if (config.equippedCosmetics != null) {
            for (Map.Entry<String, String> entry : config.equippedCosmetics.entrySet()) {
                CosmeticType.byCommandName(entry.getKey()).ifPresent(type -> equippedCosmetics.put(type, entry.getValue()));
            }
        }

        if (config.discordRpc != null) {
            discordRpcEnabled = config.discordRpc.enabled;
            discordRpcShowServer = config.discordRpc.showServer;
        }
        if (config.ui != null) {
            uiAccentColor = 0xFF000000 | (config.ui.accentColor & 0x00FFFFFF);
            uiBlurEnabled = config.ui.blurEnabled;
        }

        ensureCosmeticDefaults();
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
        ensureCosmeticDefaults();
        ClientConfig config = new ClientConfig();
        for (Module module : moduleManager.getModules()) {
            ClientConfig.ModuleConfig moduleConfig = new ClientConfig.ModuleConfig();
            moduleConfig.enabled = module.isEnabled();

            for (Setting<?> setting : module.getSettings()) {
                moduleConfig.settings.put(setting.getName(), setting.getValue());
            }

            config.modules.put(module.getName(), moduleConfig);
        }
        config.unlockedCosmetics.addAll(unlockedCosmetics);
        for (Map.Entry<CosmeticType, String> entry : equippedCosmetics.entrySet()) {
            config.equippedCosmetics.put(entry.getKey().commandName(), entry.getValue());
        }
        config.discordRpc.enabled = discordRpcEnabled;
        config.discordRpc.showServer = discordRpcShowServer;
        config.ui.accentColor = uiAccentColor;
        config.ui.blurEnabled = uiBlurEnabled;
        return config;
    }

    public Collection<String> getUnlockedCosmetics() {
        ensureCosmeticDefaults();
        return cosmeticRegistry.all().stream().map(Cosmetic::id).toList();
    }

    public boolean isUnlocked(String cosmeticId) {
        ensureCosmeticDefaults();
        return cosmeticRegistry.get(cosmeticId).isPresent();
    }

    public boolean unlockCosmetic(String cosmeticId) {
        Optional<Cosmetic> cosmetic = cosmeticRegistry.get(cosmeticId);
        if (cosmetic.isEmpty()) {
            return false;
        }

        unlockedCosmetics.add(cosmeticId);
        equippedCosmetics.putIfAbsent(cosmetic.get().type(), cosmeticId);
        return true;
    }

    public boolean equipCosmetic(CosmeticType type, String cosmeticId) {
        Optional<Cosmetic> cosmetic = cosmeticRegistry.get(cosmeticId);
        if (cosmetic.isEmpty() || cosmetic.get().type() != type) {
            return false;
        }

        equippedCosmetics.put(type, cosmeticId);
        return true;
    }

    public Optional<Cosmetic> getEquippedCosmetic(CosmeticType type) {
        ensureCosmeticDefaults();
        String id = equippedCosmetics.get(type);
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

    private void ensureCosmeticDefaults() {
        for (CosmeticType type : CosmeticType.values()) {
            Optional<Cosmetic> first = cosmeticRegistry.firstByType(type);
            if (first.isEmpty()) {
                continue;
            }

            String firstId = first.get().id();
            cosmeticRegistry.byType(type).forEach(cosmetic -> unlockedCosmetics.add(cosmetic.id()));
            String selected = equippedCosmetics.get(type);
            if (selected == null || cosmeticRegistry.get(selected).filter(cosmetic -> cosmetic.type() == type).isEmpty()) {
                equippedCosmetics.put(type, firstId);
            }
        }
    }
}
