package site.s9lab.s9labclient.client.foundation.notification;

import java.util.Locale;

public enum NotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR,
    FRIEND,
    REWARD,
    SHOP;

    public static NotificationType fromWire(String value) {
        if (value == null || value.isBlank()) {
            return INFO;
        }
        try {
            return NotificationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return INFO;
        }
    }
}
