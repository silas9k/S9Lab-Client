package site.s9lab.s9labclient.client.foundation.profile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import site.s9lab.s9labclient.client.foundation.model.UserProfile;

public final class ProfileCache {
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(2);
    private final Map<UUID, CacheEntry> byUuid = new ConcurrentHashMap<>();
    private final Map<String, UUID> byName = new ConcurrentHashMap<>();

    public void put(UserProfile profile) {
        if (profile == null || profile.minecraftUuid() == null) {
            return;
        }
        CacheEntry entry = new CacheEntry(profile, Instant.now());
        byUuid.put(profile.minecraftUuid(), entry);
        byName.put(profile.minecraftName().toLowerCase(), profile.minecraftUuid());
    }

    public Optional<UserProfile> get(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        CacheEntry entry = byUuid.get(uuid);
        if (entry == null || entry.expired(DEFAULT_TTL)) {
            byUuid.remove(uuid);
            return Optional.empty();
        }
        return Optional.of(entry.profile);
    }

    public Optional<UserProfile> getByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return get(byName.get(name.trim().toLowerCase()));
    }

    public void clear() {
        byUuid.clear();
        byName.clear();
    }

    private record CacheEntry(UserProfile profile, Instant cachedAt) {
        private boolean expired(Duration ttl) {
            return cachedAt.plus(ttl).isBefore(Instant.now());
        }
    }
}
