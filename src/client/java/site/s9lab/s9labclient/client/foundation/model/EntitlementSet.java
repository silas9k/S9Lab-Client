package site.s9lab.s9labclient.client.foundation.model;

import java.util.EnumMap;
import java.util.Map;

public final class EntitlementSet {
    private final EnumMap<EntitlementType, Integer> levels = new EnumMap<>(EntitlementType.class);

    public EntitlementSet with(EntitlementType type, int level) {
        if (type != null && level > 0) {
            levels.put(type, level);
        }
        return this;
    }

    public int level(EntitlementType type) {
        return levels.getOrDefault(type, 0);
    }

    public boolean has(EntitlementType type) {
        return level(type) > 0;
    }

    public Map<EntitlementType, Integer> snapshot() {
        return Map.copyOf(levels);
    }
}
