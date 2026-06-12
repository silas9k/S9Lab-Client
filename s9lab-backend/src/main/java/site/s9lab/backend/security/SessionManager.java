package site.s9lab.backend.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private static final long SESSION_TTL_SECONDS = 6 * 60 * 60L;
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public String create(String uuid) {
        cleanupExpired();
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new SessionInfo(uuid, expiresAt()));
        return token;
    }

    public boolean validate(String uuid, String token) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        return authenticate(token).filter(uuid::equals).isPresent();
    }

    public Optional<String> authenticate(String token) {
        cleanupExpired();
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SessionInfo info = sessions.get(token);
        if (info == null) {
            return Optional.empty();
        }
        long now = Instant.now().getEpochSecond();
        if (info.expiresAt < now) {
            sessions.remove(token);
            return Optional.empty();
        }
        info.expiresAt = expiresAt();
        return Optional.of(info.uuid);
    }

    private void cleanupExpired() {
        long now = Instant.now().getEpochSecond();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
    }

    private static long expiresAt() {
        return Instant.now().getEpochSecond() + SESSION_TTL_SECONDS;
    }

    private static final class SessionInfo {
        private final String uuid;
        private volatile long expiresAt;

        private SessionInfo(String uuid, long expiresAt) {
            this.uuid = uuid;
            this.expiresAt = expiresAt;
        }
    }
}
