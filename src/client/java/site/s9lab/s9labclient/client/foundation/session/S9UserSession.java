package site.s9lab.s9labclient.client.foundation.session;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

public final class S9UserSession {
    private static volatile Identity identity;
    private static volatile String backendToken = "";
    private static volatile Instant backendTokenUpdatedAt = Instant.EPOCH;

    private S9UserSession() {
    }

    public static Optional<Identity> current() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) {
            return Optional.empty();
        }
        Session session = client.getSession();
        UUID uuid = session.getUuidOrNull();
        if (uuid == null && client.player != null) {
            uuid = client.player.getUuid();
        }
        if (uuid == null) {
            return Optional.empty();
        }
        Identity next = new Identity(uuid, session.getUsername());
        identity = next;
        return Optional.of(next);
    }

    public static Optional<Identity> cachedIdentity() {
        return Optional.ofNullable(identity);
    }

    public static void setBackendToken(String token) {
        backendToken = token == null ? "" : token;
        backendTokenUpdatedAt = Instant.now();
    }

    public static String backendToken() {
        return backendToken;
    }

    public static void clearBackendToken() {
        setBackendToken("");
    }

    public static Instant backendTokenUpdatedAt() {
        return backendTokenUpdatedAt;
    }

    public record Identity(UUID uuid, String name) {
        public String uuidString() {
            return uuid.toString();
        }
    }
}
