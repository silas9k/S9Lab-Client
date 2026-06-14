package site.s9lab.s9labclient.client.foundation.model;

import java.util.Locale;

public enum PresenceStatus {
    ONLINE,
    IN_GAME,
    IDLE,
    DO_NOT_DISTURB,
    OFFLINE;

    public static PresenceStatus fromWire(String value) {
        if (value == null || value.isBlank()) {
            return OFFLINE;
        }
        try {
            return PresenceStatus.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            return OFFLINE;
        }
    }
}
