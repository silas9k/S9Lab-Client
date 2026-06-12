package site.s9lab.s9labclient.client.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ClientConfig {
    public Map<String, ModuleConfig> modules = new LinkedHashMap<>();
    public Set<String> unlockedCosmetics = new LinkedHashSet<>();
    public Map<String, String> equippedCosmetics = new LinkedHashMap<>();
    public DiscordRpcConfig discordRpc = new DiscordRpcConfig();
    public UiConfig ui = new UiConfig();

    public static class ModuleConfig {
        public boolean enabled;
        public Map<String, Object> settings = new LinkedHashMap<>();
    }

    public static class DiscordRpcConfig {
        public boolean enabled = true;
        public boolean showServer = true;
    }

    public static class UiConfig {
        public int accentColor = 0xFF5E7CE2;
        public boolean blurEnabled = true;
    }
}
