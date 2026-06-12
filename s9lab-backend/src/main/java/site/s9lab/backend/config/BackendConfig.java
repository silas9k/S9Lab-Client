package site.s9lab.backend.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public record BackendConfig(
        String host,
        int port,
        int websocketPort,
        String databasePath,
        String backupDirectory,
        String adminSecret,
        String apiVersion,
        boolean enableWebsocket,
        int rateLimitPerMinute
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "config.json");
    private static final String LEGACY_DEFAULT_SECRET = "315e0791-f27f-42bd-8879-0003a2cb2386-2c584a8e-f13d-4ea1-8cab-25c0e8d97720";

    public static BackendConfig load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            Files.createDirectories(CONFIG_PATH.getParent());
            BackendConfig defaults = defaults();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            BackendConfig loaded = GSON.fromJson(reader, BackendConfig.class);
            BackendConfig normalized = loaded == null ? defaults() : loaded.normalized();
            if (loaded == null || loaded.adminSecret() == null || loaded.adminSecret().isBlank()
                    || LEGACY_DEFAULT_SECRET.equals(loaded.adminSecret())) {
                normalized = normalized.withAdminSecret(generateSecret());
                save(normalized);
            }
            return normalized;
        }
    }

    public static BackendConfig defaults() {
        return new BackendConfig(
                "0.0.0.0",
                8788,
                8789,
                "database/s9lab.db",
                "backups",
                generateSecret(),
                "v1",
                true,
                120
        );
    }

    public int resolvedPort() {
        String envPort = System.getenv("PORT");
        if (envPort == null || envPort.isBlank()) {
            envPort = System.getenv("SERVER_PORT");
        }
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return port;
    }

    public int resolvedWebsocketPort() {
        String envPort = System.getenv("WEBSOCKET_PORT");
        if (envPort == null || envPort.isBlank()) {
            envPort = System.getenv("WS_PORT");
        }
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return websocketPort;
    }

    private BackendConfig normalized() {
        BackendConfig fallback = defaults();
        String secret = env("S9LAB_ADMIN_SECRET", env("ADMIN_SECRET", blank(adminSecret, fallback.adminSecret)));
        return new BackendConfig(
                blank(host, fallback.host),
                port <= 0 ? fallback.port : port,
                websocketPort <= 0 ? fallback.websocketPort : websocketPort,
                blank(databasePath, fallback.databasePath),
                blank(backupDirectory, fallback.backupDirectory),
                secret,
                blank(apiVersion, fallback.apiVersion),
                enableWebsocket,
                rateLimitPerMinute <= 0 ? fallback.rateLimitPerMinute : rateLimitPerMinute
        );
    }

    private BackendConfig withAdminSecret(String secret) {
        return new BackendConfig(host, port, websocketPort, databasePath, backupDirectory, secret, apiVersion, enableWebsocket, rateLimitPerMinute);
    }

    private static void save(BackendConfig config) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        }
    }

    private static String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String generateSecret() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }
}
