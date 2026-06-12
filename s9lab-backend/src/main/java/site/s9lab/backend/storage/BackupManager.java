package site.s9lab.backend.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import site.s9lab.backend.config.BackendConfig;

public final class BackupManager {
    private static final Logger LOGGER = Logger.getLogger("S9LabBackend");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private BackupManager() {
    }

    public static void backupOnStartup(BackendConfig config, Path databaseFile) {
        if (!Files.exists(databaseFile)) {
            return;
        }

        try {
            Path backupDir = Path.of(config.backupDirectory());
            Files.createDirectories(backupDir);
            Path target = backupDir.resolve("s9lab-db-" + FORMAT.format(LocalDateTime.now()) + ".sqlite");
            Files.copy(databaseFile, target);
            LOGGER.info(() -> "database_backup_created path=" + target);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "database_backup_failed", exception);
        }
    }
}
