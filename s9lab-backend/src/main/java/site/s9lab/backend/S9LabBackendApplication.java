package site.s9lab.backend;

import java.util.logging.Level;
import java.util.logging.Logger;
import site.s9lab.backend.config.BackendConfig;
import site.s9lab.backend.cosmetics.CosmeticCatalog;
import site.s9lab.backend.network.ApiServer;
import site.s9lab.backend.profiles.ProfileService;
import site.s9lab.backend.security.SessionManager;
import site.s9lab.backend.shop.ShopService;
import site.s9lab.backend.storage.BackupManager;
import site.s9lab.backend.storage.DatabaseManager;
import site.s9lab.backend.websocket.BackendWebSocketServer;

public final class S9LabBackendApplication {
    private static final Logger LOGGER = Logger.getLogger("S9LabBackend");

    private S9LabBackendApplication() {
    }

    public static void main(String[] args) {
        try {
            BackendConfig config = BackendConfig.load();
            DatabaseManager database = new DatabaseManager(config.databasePath());
            BackupManager.backupOnStartup(config, database.databaseFile());
            database.connect();
            database.migrate();
            database.seedCosmetics(CosmeticCatalog.defaults());
            SessionManager sessions = new SessionManager();

            BackendWebSocketServer webSocketServer = null;
            if (config.enableWebsocket()) {
                webSocketServer = new BackendWebSocketServer(config.resolvedWebsocketPort(), database, sessions);
                webSocketServer.start();
                LOGGER.info(() -> "websocket_started port=" + config.resolvedWebsocketPort());
            }

            ProfileService profiles = new ProfileService(database, sessions);
            ShopService shop = new ShopService(database);
            ApiServer api = new ApiServer(config, database, profiles, shop, sessions, webSocketServer);
            api.start();

            BackendWebSocketServer finalWebSocketServer = webSocketServer;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("shutdown_started");
                api.stop();
                if (finalWebSocketServer != null) {
                    try {
                        finalWebSocketServer.stop(1000);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                }
                database.close();
                LOGGER.info("shutdown_complete");
            }, "s9lab-backend-shutdown"));

            LOGGER.info(() -> "s9lab_backend_ready http_port=" + config.resolvedPort());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "startup_failed", exception);
            System.exit(1);
        }
    }
}
