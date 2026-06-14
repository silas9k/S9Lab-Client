package site.s9lab.s9labclient.client.foundation.model;

import java.time.Instant;

public record PlusStatus(boolean active, long expiresAt) {
    public static final PlusStatus INACTIVE = new PlusStatus(false, 0L);

    public static PlusStatus of(long expiresAt) {
        return new PlusStatus(expiresAt > Instant.now().getEpochSecond(), Math.max(0L, expiresAt));
    }
}
