package site.s9lab.s9labclient.client.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClientConfig {
    public LocalConfig local = new LocalConfig();

    public static class ModuleConfig {
        public boolean enabled;
        public Map<String, Object> settings = new LinkedHashMap<>();
    }

    public static class LocalConfig {
        public boolean showOfflineWarnings = true;
    }
}
