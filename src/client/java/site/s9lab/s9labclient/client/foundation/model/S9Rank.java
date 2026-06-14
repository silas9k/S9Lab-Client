package site.s9lab.s9labclient.client.foundation.model;

import java.util.Locale;

public enum S9Rank {
    USER("User", 0xFFE7EAF2, 0),
    PLUS("S9Lab Client+", 0xFF5E7CE2, 10);

    private final String displayName;
    private final int color;
    private final int priority;

    S9Rank(String displayName, int color, int priority) {
        this.displayName = displayName;
        this.color = color;
        this.priority = priority;
    }

    public String displayName() {
        return displayName;
    }

    public int color() {
        return color;
    }

    public int priority() {
        return priority;
    }

    public String iconKey() {
        return this == PLUS ? "s9_icon_plus" : "s9_icon";
    }

    public static S9Rank fromWire(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        try {
            return S9Rank.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return USER;
        }
    }
}
