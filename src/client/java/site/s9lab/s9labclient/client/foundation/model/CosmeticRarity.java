package site.s9lab.s9labclient.client.foundation.model;

import java.util.Locale;

public enum CosmeticRarity {
    COMMON("Common", 0xFF9AA1B2, 0, false),
    RARE("Rare", 0xFF3FA7FF, 10, false),
    EPIC("Epic", 0xFFC45CFF, 20, true),
    LEGENDARY("Legendary", 0xFFFFC857, 30, true),
    EXCLUSIVE("Exclusive", 0xFFFF4D8D, 40, true);

    private final String displayName;
    private final int color;
    private final int sortOrder;
    private final boolean glow;

    CosmeticRarity(String displayName, int color, int sortOrder, boolean glow) {
        this.displayName = displayName;
        this.color = color;
        this.sortOrder = sortOrder;
        this.glow = glow;
    }

    public String displayName() {
        return displayName;
    }

    public int color() {
        return color;
    }

    public int sortOrder() {
        return sortOrder;
    }

    public boolean glow() {
        return glow;
    }

    public String translationKey() {
        return "rarity.s9labclient." + name().toLowerCase(Locale.ROOT);
    }

    public static CosmeticRarity fromWire(String value) {
        if (value == null || value.isBlank()) {
            return COMMON;
        }
        try {
            return CosmeticRarity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return COMMON;
        }
    }
}
