package site.s9lab.backend.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.cosmetics.CosmeticDefinition;

public final class DatabaseManager implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger("S9LabBackend");
    private static final long MAX_COINS = 1_000_000_000_000L;
    private final Path databaseFile;
    private Connection connection;

    public DatabaseManager(String databasePath) {
        this.databaseFile = Path.of(databasePath);
    }

    public Path databaseFile() {
        return databaseFile;
    }

    public void connect() throws Exception {
        Path parent = databaseFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
        }
    }

    public synchronized void migrate() throws SQLException {
        execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    coins INTEGER NOT NULL DEFAULT 0,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL,
                    total_playtime_seconds INTEGER NOT NULL DEFAULT 0,
                    online INTEGER NOT NULL DEFAULT 0,
                    rank TEXT NOT NULL DEFAULT 'USER',
                    plus_expires_at INTEGER NOT NULL DEFAULT 0
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS cosmetics (
                    id TEXT PRIMARY KEY,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    price INTEGER NOT NULL,
                    enabled INTEGER NOT NULL DEFAULT 1,
                    rarity TEXT NOT NULL DEFAULT 'COMMON',
                    limited INTEGER NOT NULL DEFAULT 0,
                    available_from INTEGER NOT NULL DEFAULT 0,
                    available_until INTEGER NOT NULL DEFAULT 0,
                    plus_exclusive INTEGER NOT NULL DEFAULT 0,
                    limited_text TEXT NOT NULL DEFAULT '',
                    preview_asset TEXT NOT NULL DEFAULT ''
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS owned_cosmetics (
                    uuid TEXT NOT NULL,
                    cosmetic_id TEXT NOT NULL,
                    purchased_at INTEGER NOT NULL,
                    PRIMARY KEY (uuid, cosmetic_id),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS equipped_cosmetics (
                    uuid TEXT NOT NULL,
                    type TEXT NOT NULL,
                    cosmetic_id TEXT NOT NULL,
                    PRIMARY KEY (uuid, type),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS active_emotes (
                    uuid TEXT PRIMARY KEY,
                    emote_id TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS user_settings (
                    uuid TEXT PRIMARY KEY,
                    settings_json TEXT NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS cosmetic_gifts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender_uuid TEXT NOT NULL,
                    receiver_uuid TEXT NOT NULL,
                    cosmetic_id TEXT NOT NULL,
                    gifted_at INTEGER NOT NULL,
                    FOREIGN KEY (sender_uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (receiver_uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    receiver_uuid TEXT NOT NULL,
                    sender_uuid TEXT,
                    sender_name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    cosmetic_id TEXT,
                    cosmetic_name TEXT,
                    message TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    read INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (receiver_uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (sender_uuid) REFERENCES players(uuid) ON DELETE SET NULL
                )
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at INTEGER NOT NULL,
                    action TEXT NOT NULL,
                    actor TEXT NOT NULL,
                    target_uuid TEXT,
                    detail TEXT NOT NULL
                )
                """);
        execute("CREATE INDEX IF NOT EXISTS idx_owned_cosmetics_uuid ON owned_cosmetics(uuid)");
        execute("CREATE INDEX IF NOT EXISTS idx_equipped_cosmetics_uuid ON equipped_cosmetics(uuid)");
        execute("CREATE INDEX IF NOT EXISTS idx_players_online ON players(online)");
        execute("CREATE INDEX IF NOT EXISTS idx_players_name_lower ON players(LOWER(name))");
        execute("CREATE INDEX IF NOT EXISTS idx_cosmetic_gifts_sender ON cosmetic_gifts(sender_uuid)");
        execute("CREATE INDEX IF NOT EXISTS idx_cosmetic_gifts_receiver ON cosmetic_gifts(receiver_uuid)");
        execute("CREATE INDEX IF NOT EXISTS idx_notifications_receiver_read ON notifications(receiver_uuid, read, created_at)");
        execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at)");
        migrateColumn("players", "rank", "TEXT NOT NULL DEFAULT 'USER'");
        migrateColumn("players", "plus_expires_at", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("players", "name_effects_enabled", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("players", "name_effects", "TEXT NOT NULL DEFAULT ''");
        migrateColumn("cosmetics", "rarity", "TEXT NOT NULL DEFAULT 'COMMON'");
        migrateColumn("cosmetics", "limited", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("cosmetics", "available_from", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("cosmetics", "available_until", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("cosmetics", "plus_exclusive", "INTEGER NOT NULL DEFAULT 0");
        migrateColumn("cosmetics", "limited_text", "TEXT NOT NULL DEFAULT ''");
        migrateColumn("cosmetics", "preview_asset", "TEXT NOT NULL DEFAULT ''");
    }

    public synchronized void seedCosmetics(List<CosmeticDefinition> definitions) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO cosmetics (id, type, name, description, price, enabled, rarity, limited, available_from, available_until, plus_exclusive, limited_text, preview_asset)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    type = excluded.type,
                    name = excluded.name,
                    description = excluded.description,
                    price = excluded.price,
                    enabled = excluded.enabled,
                    rarity = excluded.rarity,
                    limited = excluded.limited,
                    available_from = excluded.available_from,
                    available_until = excluded.available_until,
                    plus_exclusive = excluded.plus_exclusive,
                    limited_text = excluded.limited_text,
                    preview_asset = excluded.preview_asset
                """)) {
            for (CosmeticDefinition definition : definitions) {
                statement.setString(1, definition.id());
                statement.setString(2, definition.type());
                statement.setString(3, definition.name());
                statement.setString(4, definition.description());
                statement.setLong(5, definition.price());
                statement.setInt(6, definition.enabled() ? 1 : 0);
                statement.setString(7, definition.rarity());
                statement.setInt(8, definition.limited() ? 1 : 0);
                statement.setLong(9, definition.availableFrom());
                statement.setLong(10, definition.availableUntil());
                statement.setInt(11, definition.plusExclusive() ? 1 : 0);
                statement.setString(12, definition.limitedText());
                statement.setString(13, definition.previewAsset());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public synchronized void ensurePlayer(String uuid, String name) throws SQLException {
        long now = now();
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO players (uuid, name, coins, first_seen, last_seen, online)
                VALUES (?, ?, 0, ?, ?, 1)
                ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, last_seen = excluded.last_seen, online = 1
                """)) {
            statement.setString(1, uuid);
            statement.setString(2, name == null || name.isBlank() ? "Unknown" : name);
            statement.setLong(3, now);
            statement.setLong(4, now);
            statement.executeUpdate();
        }
    }

    public synchronized void heartbeat(String uuid, String name, long playtimeSeconds, boolean online) throws SQLException {
        ensurePlayer(uuid, name);
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE players
                SET last_seen = ?, total_playtime_seconds = MAX(total_playtime_seconds, ?), online = ?
                WHERE uuid = ?
                """)) {
            statement.setLong(1, now());
            statement.setLong(2, Math.max(0, playtimeSeconds));
            statement.setInt(3, online ? 1 : 0);
            statement.setString(4, uuid);
            statement.executeUpdate();
        }
    }

    public synchronized Dtos.PlayerAdminResponse profile(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                long plusExpiresAt = result.getLong("plus_expires_at");
                boolean plusActive = plusExpiresAt > now();
                String rank = plusActive ? "PLUS" : "USER";
                boolean effectsEnabled = plusActive && result.getInt("name_effects_enabled") == 1;
                return new Dtos.PlayerAdminResponse(
                        true,
                        uuid,
                        result.getString("name"),
                        result.getLong("coins"),
                        ownedCosmetics(uuid),
                        equippedCosmetics(uuid),
                        activeEmote(uuid).orElse(""),
                        result.getLong("first_seen"),
                        result.getLong("last_seen"),
                        result.getLong("total_playtime_seconds"),
                        result.getInt("online") == 1,
                        rank,
                        List.of(),
                        plusActive,
                        plusExpiresAt,
                        effectsEnabled,
                        parseNameEffects(result.getString("name_effects"))
                );
            }
        }
    }

    public synchronized Dtos.PlayerAdminResponse profileByName(String name) throws SQLException {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            return null;
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM players WHERE LOWER(name) = LOWER(?) ORDER BY last_seen DESC LIMIT 1")) {
            statement.setString(1, normalized);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? profile(result.getString("uuid")) : null;
            }
        }
    }

    public synchronized List<Dtos.CosmeticDto> cosmetics() throws SQLException {
        List<Dtos.CosmeticDto> cosmetics = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cosmetics ORDER BY type, price, name");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                cosmetics.add(new Dtos.CosmeticDto(
                        result.getString("id"),
                        result.getString("type"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getLong("price"),
                        result.getInt("enabled") == 1,
                        blank(result.getString("rarity"), "COMMON"),
                        result.getInt("limited") == 1,
                        result.getLong("available_from"),
                        result.getLong("available_until"),
                        result.getInt("plus_exclusive") == 1,
                        blank(result.getString("limited_text"), ""),
                        blank(result.getString("preview_asset"), ""),
                        Map.of()
                ));
            }
        }
        return cosmetics;
    }

    public synchronized Optional<Dtos.CosmeticDto> cosmetic(String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cosmetics WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Dtos.CosmeticDto(
                        result.getString("id"),
                        result.getString("type"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getLong("price"),
                        result.getInt("enabled") == 1,
                        blank(result.getString("rarity"), "COMMON"),
                        result.getInt("limited") == 1,
                        result.getLong("available_from"),
                        result.getLong("available_until"),
                        result.getInt("plus_exclusive") == 1,
                        blank(result.getString("limited_text"), ""),
                        blank(result.getString("preview_asset"), ""),
                        Map.of()
                ));
            }
        }
    }

    public synchronized void setCoins(String uuid, long coins, String actor) throws SQLException {
        ensurePlayerForAdmin(uuid);
        long normalized = clampCoins(coins);
        try (PreparedStatement statement = connection.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?")) {
            statement.setLong(1, normalized);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
        audit(actor, uuid, "coins_set", "amount=" + normalized);
    }

    public synchronized void addCoins(String uuid, long amount, String actor) throws SQLException {
        ensurePlayerForAdmin(uuid);
        long current = coins(uuid);
        long next = addClamped(current, amount);
        try (PreparedStatement statement = connection.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?")) {
            statement.setLong(1, next);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
        audit(actor, uuid, amount >= 0 ? "coins_add" : "coins_remove", "amount=" + amount + " result=" + next);
    }

    public synchronized boolean owns(String uuid, String cosmeticId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM owned_cosmetics WHERE uuid = ? AND cosmetic_id = ?")) {
            statement.setString(1, uuid);
            statement.setString(2, cosmeticId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public synchronized Dtos.NotificationDto gift(String senderUuid, String receiverUuid, String cosmeticId) throws SQLException {
        if (senderUuid.equals(receiverUuid)) {
            throw new IllegalArgumentException("cannot_gift_self");
        }
        Dtos.PlayerAdminResponse sender = profile(senderUuid);
        if (sender == null) {
            throw new IllegalArgumentException("sender_not_found");
        }
        Dtos.PlayerAdminResponse receiver = profile(receiverUuid);
        if (receiver == null) {
            throw new IllegalArgumentException("receiver_not_found");
        }
        Optional<Dtos.CosmeticDto> cosmetic = cosmetic(cosmeticId);
        if (cosmetic.isEmpty() || !cosmetic.get().enabled()) {
            throw new IllegalArgumentException("cosmetic_not_available");
        }
        if (!owns(senderUuid, cosmeticId)) {
            throw new IllegalArgumentException("sender_not_owner");
        }
        if (owns(receiverUuid, cosmeticId)) {
            throw new IllegalArgumentException("receiver_already_owns");
        }

        connection.setAutoCommit(false);
        try {
            long createdAt = now();
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO owned_cosmetics (uuid, cosmetic_id, purchased_at) VALUES (?, ?, ?)")) {
                insert.setString(1, receiverUuid);
                insert.setString(2, cosmeticId);
                insert.setLong(3, createdAt);
                insert.executeUpdate();
            }
            try (PreparedStatement gift = connection.prepareStatement("""
                    INSERT INTO cosmetic_gifts (sender_uuid, receiver_uuid, cosmetic_id, gifted_at)
                    VALUES (?, ?, ?, ?)
                    """)) {
                gift.setString(1, senderUuid);
                gift.setString(2, receiverUuid);
                gift.setString(3, cosmeticId);
                gift.setLong(4, createdAt);
                gift.executeUpdate();
            }
            Dtos.NotificationDto notification = insertNotification(
                    receiverUuid,
                    senderUuid,
                    sender.name(),
                    "gift_received",
                    cosmeticId,
                    cosmetic.get().name(),
                    "You received " + cosmetic.get().name() + " from " + sender.name(),
                    createdAt
            );
            connection.commit();
            audit(senderUuid, receiverUuid, "cosmetic_gift", "cosmetic=" + cosmeticId + " sender=" + senderUuid);
            return notification;
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Dtos.NotificationDto insertNotification(
            String receiverUuid,
            String senderUuid,
            String senderName,
            String type,
            String cosmeticId,
            String cosmeticName,
            String message,
            long createdAt
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO notifications (receiver_uuid, sender_uuid, sender_name, type, cosmetic_id, cosmetic_name, message, created_at, read)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, receiverUuid);
            statement.setString(2, senderUuid);
            statement.setString(3, blank(senderName, "Unknown"));
            statement.setString(4, blank(type, "info"));
            statement.setString(5, cosmeticId);
            statement.setString(6, blank(cosmeticName, ""));
            statement.setString(7, blank(message, ""));
            statement.setLong(8, createdAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0L;
                return new Dtos.NotificationDto(
                        id,
                        receiverUuid,
                        senderUuid,
                        blank(senderName, "Unknown"),
                        blank(type, "info"),
                        blank(cosmeticId, ""),
                        blank(cosmeticName, ""),
                        blank(message, ""),
                        createdAt,
                        false
                );
            }
        }
    }

    public synchronized void buy(String uuid, String cosmeticId) throws SQLException {
        Optional<Dtos.CosmeticDto> cosmetic = cosmetic(cosmeticId);
        if (cosmetic.isEmpty() || !cosmetic.get().enabled()) {
            throw new IllegalArgumentException("cosmetic_not_available");
        }
        if (owns(uuid, cosmeticId)) {
            throw new IllegalArgumentException("already_owned");
        }
        if (cosmetic.get().price() < 0 || cosmetic.get().price() > MAX_COINS) {
            throw new IllegalArgumentException("invalid_cosmetic_price");
        }
        if (cosmetic.get().plusExclusive() && !plusActive(uuid)) {
            throw new IllegalArgumentException("plus_required");
        }

        connection.setAutoCommit(false);
        try {
            long coins = coins(uuid);
            if (coins < cosmetic.get().price()) {
                throw new IllegalArgumentException("not_enough_coins");
            }
            try (PreparedStatement update = connection.prepareStatement("UPDATE players SET coins = coins - ? WHERE uuid = ?")) {
                update.setLong(1, cosmetic.get().price());
                update.setString(2, uuid);
                update.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO owned_cosmetics (uuid, cosmetic_id, purchased_at) VALUES (?, ?, ?)")) {
                insert.setString(1, uuid);
                insert.setString(2, cosmeticId);
                insert.setLong(3, now());
                insert.executeUpdate();
            }
            connection.commit();
            audit("shop", uuid, "cosmetic_buy", cosmeticId);
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public synchronized void buyPlus(String uuid, int months, long price) throws SQLException {
        if (months != 1 && months != 3) {
            throw new IllegalArgumentException("invalid_plus_plan");
        }
        if (price < 0 || price > MAX_COINS) {
            throw new IllegalArgumentException("invalid_plus_price");
        }
        ensurePlayerForAdmin(uuid);
        if (plusActive(uuid)) {
            throw new IllegalArgumentException("plus_already_active");
        }

        connection.setAutoCommit(false);
        try {
            long currentCoins = coins(uuid);
            if (currentCoins < price) {
                throw new IllegalArgumentException("not_enough_coins");
            }
            long currentExpiry = 0L;
            try (PreparedStatement select = connection.prepareStatement("SELECT plus_expires_at FROM players WHERE uuid = ?")) {
                select.setString(1, uuid);
                try (ResultSet result = select.executeQuery()) {
                    if (result.next()) {
                        currentExpiry = result.getLong("plus_expires_at");
                    }
                }
            }
            long now = now();
            long base = Math.max(now, currentExpiry);
            long extensionSeconds = months * 30L * 24L * 60L * 60L;
            long newExpiry = base > Long.MAX_VALUE - extensionSeconds ? Long.MAX_VALUE : base + extensionSeconds;
            try (PreparedStatement update = connection.prepareStatement("""
                    UPDATE players
                    SET coins = coins - ?, rank = 'PLUS', plus_expires_at = ?
                    WHERE uuid = ?
                    """)) {
                update.setLong(1, price);
                update.setLong(2, newExpiry);
                update.setString(3, uuid);
                update.executeUpdate();
            }
            connection.commit();
            audit("shop", uuid, "plus_buy", "months=" + months + " price=" + price + " expires=" + newExpiry);
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public synchronized void giftPlus(String senderUuid, String receiverUuid, int months, long price) throws SQLException {
        if (senderUuid.equals(receiverUuid)) {
            throw new IllegalArgumentException("cannot_gift_self");
        }
        if (months != 1 && months != 3) {
            throw new IllegalArgumentException("invalid_plus_plan");
        }
        if (price < 0 || price > MAX_COINS) {
            throw new IllegalArgumentException("invalid_plus_price");
        }
        ensurePlayerForAdmin(senderUuid);
        ensurePlayerForAdmin(receiverUuid);
        if (plusActive(receiverUuid)) {
            throw new IllegalArgumentException("receiver_plus_active");
        }
        connection.setAutoCommit(false);
        try {
            long currentCoins = coins(senderUuid);
            if (currentCoins < price) {
                throw new IllegalArgumentException("not_enough_coins");
            }
            long createdAt = now();
            long newExpiry = createdAt + months * 30L * 24L * 60L * 60L;
            try (PreparedStatement updateSender = connection.prepareStatement("UPDATE players SET coins = coins - ? WHERE uuid = ?")) {
                updateSender.setLong(1, price);
                updateSender.setString(2, senderUuid);
                updateSender.executeUpdate();
            }
            try (PreparedStatement updateReceiver = connection.prepareStatement("""
                    UPDATE players
                    SET rank = 'PLUS', plus_expires_at = ?, name_effects_enabled = 0
                    WHERE uuid = ?
                    """)) {
                updateReceiver.setLong(1, newExpiry);
                updateReceiver.setString(2, receiverUuid);
                updateReceiver.executeUpdate();
            }
            connection.commit();
            audit("shop", receiverUuid, "plus_gift", "sender=" + senderUuid + " months=" + months + " price=" + price + " expires=" + newExpiry);
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public synchronized void saveNameEffects(String uuid, boolean enabled, List<String> effects) throws SQLException {
        ensurePlayerForAdmin(uuid);
        List<String> normalized = normalizeNameEffects(effects);
        boolean active = plusActive(uuid) && enabled && !normalized.isEmpty();
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE players
                SET name_effects_enabled = ?, name_effects = ?
                WHERE uuid = ?
                """)) {
            statement.setInt(1, active ? 1 : 0);
            statement.setString(2, String.join(",", normalized));
            statement.setString(3, uuid);
            statement.executeUpdate();
        }
    }

    public synchronized void equip(String uuid, String cosmeticId) throws SQLException {
        Optional<Dtos.CosmeticDto> cosmetic = cosmetic(cosmeticId);
        if (cosmetic.isEmpty() || !cosmetic.get().enabled()) {
            throw new IllegalArgumentException("cosmetic_not_available");
        }
        if (!owns(uuid, cosmeticId)) {
            throw new IllegalArgumentException("not_owned");
        }
        if (cosmetic.get().plusExclusive() && !plusActive(uuid)) {
            throw new IllegalArgumentException("plus_required");
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO equipped_cosmetics (uuid, type, cosmetic_id)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid, type) DO UPDATE SET cosmetic_id = excluded.cosmetic_id
                """)) {
            statement.setString(1, uuid);
            statement.setString(2, cosmetic.get().type());
            statement.setString(3, cosmeticId);
            statement.executeUpdate();
        }
        audit("shop", uuid, "cosmetic_equip", cosmeticId);
    }

    public synchronized void unequip(String uuid, String type) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM equipped_cosmetics WHERE uuid = ? AND type = ?")) {
            statement.setString(1, uuid);
            statement.setString(2, type);
            statement.executeUpdate();
        }
        audit("shop", uuid, "cosmetic_unequip", type);
    }

    public synchronized void startEmote(String uuid, String emoteId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO active_emotes (uuid, emote_id, started_at)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET emote_id = excluded.emote_id, started_at = excluded.started_at
                """)) {
            statement.setString(1, uuid);
            statement.setString(2, emoteId);
            statement.setLong(3, now());
            statement.executeUpdate();
        }
        audit("client", uuid, "emote_start", emoteId);
    }

    public synchronized void stopEmote(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM active_emotes WHERE uuid = ?")) {
            statement.setString(1, uuid);
            statement.executeUpdate();
        }
        audit("client", uuid, "emote_stop", "");
    }

    public synchronized Map<String, String> equippedCosmetics(String uuid) throws SQLException {
        Map<String, String> equipped = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT type, cosmetic_id FROM equipped_cosmetics WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    equipped.put(result.getString("type"), result.getString("cosmetic_id"));
                }
            }
        }
        return equipped;
    }

    public synchronized List<Dtos.NotificationDto> unreadNotifications(String uuid) throws SQLException {
        return notifications(uuid, true);
    }

    public synchronized List<Dtos.NotificationDto> notifications(String uuid, boolean unreadOnly) throws SQLException {
        List<Dtos.NotificationDto> notifications = new ArrayList<>();
        String sql = unreadOnly
                ? "SELECT * FROM notifications WHERE receiver_uuid = ? AND read = 0 ORDER BY created_at DESC, id DESC LIMIT 25"
                : "SELECT * FROM notifications WHERE receiver_uuid = ? ORDER BY created_at DESC, id DESC LIMIT 50";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    notifications.add(notification(result));
                }
            }
        }
        return notifications;
    }

    public synchronized int markNotificationsRead(String uuid, List<Long> notificationIds) throws SQLException {
        if (notificationIds == null || notificationIds.isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE notifications SET read = 1 WHERE receiver_uuid = ? AND read = 0")) {
                statement.setString(1, uuid);
                return statement.executeUpdate();
            }
        }

        int changed = 0;
        try (PreparedStatement statement = connection.prepareStatement("UPDATE notifications SET read = 1 WHERE receiver_uuid = ? AND id = ?")) {
            for (Long id : notificationIds) {
                if (id == null || id <= 0) {
                    continue;
                }
                statement.setString(1, uuid);
                statement.setLong(2, id);
                changed += statement.executeUpdate();
            }
        }
        return changed;
    }

    private static Dtos.NotificationDto notification(ResultSet result) throws SQLException {
        return new Dtos.NotificationDto(
                result.getLong("id"),
                result.getString("receiver_uuid"),
                blank(result.getString("sender_uuid"), ""),
                blank(result.getString("sender_name"), "Unknown"),
                blank(result.getString("type"), "info"),
                blank(result.getString("cosmetic_id"), ""),
                blank(result.getString("cosmetic_name"), ""),
                blank(result.getString("message"), ""),
                result.getLong("created_at"),
                result.getInt("read") == 1
        );
    }

    public synchronized String userSettingsJson(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT settings_json FROM user_settings WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("settings_json") : "{}";
            }
        }
    }

    public synchronized void saveUserSettingsJson(String uuid, String settingsJson) throws SQLException {
        ensurePlayerForAdmin(uuid);
        String json = settingsJson == null || settingsJson.isBlank() ? "{}" : settingsJson;
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO user_settings (uuid, settings_json, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET settings_json = excluded.settings_json, updated_at = excluded.updated_at
                """)) {
            statement.setString(1, uuid);
            statement.setString(2, json);
            statement.setLong(3, now());
            statement.executeUpdate();
        }
    }

    public synchronized Set<String> onlinePlayers() throws SQLException {
        Set<String> players = new LinkedHashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM players WHERE online = 1");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                players.add(result.getString("uuid"));
            }
        }
        return players;
    }

    public synchronized void setOnline(String uuid, boolean online) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE players SET online = ?, last_seen = ? WHERE uuid = ?")) {
            statement.setInt(1, online ? 1 : 0);
            statement.setLong(2, now());
            statement.setString(3, uuid);
            statement.executeUpdate();
        }
    }

    public synchronized long coins(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT coins FROM players WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong("coins") : 0;
            }
        }
    }

    public synchronized int ownedCosmeticsCount(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM owned_cosmetics WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    private List<String> ownedCosmetics(String uuid) throws SQLException {
        List<String> owned = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT cosmetic_id FROM owned_cosmetics WHERE uuid = ? ORDER BY cosmetic_id")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    owned.add(result.getString("cosmetic_id"));
                }
            }
        }
        return owned;
    }

    public synchronized Optional<String> activeEmote(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT emote_id FROM active_emotes WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(result.getString("emote_id")) : Optional.empty();
            }
        }
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void migrateColumn(String table, String column, String definition) throws SQLException {
        if (columnExists(table, column)) {
            return;
        }
        execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensurePlayerForAdmin(String uuid) throws SQLException {
        long now = now();
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO players (uuid, name, coins, first_seen, last_seen, online)
                VALUES (?, 'Unknown', 0, ?, ?, 0)
                ON CONFLICT(uuid) DO NOTHING
                """)) {
            statement.setString(1, uuid);
            statement.setLong(2, now);
            statement.setLong(3, now);
            statement.executeUpdate();
        }
    }

    private boolean plusActive(String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT plus_expires_at FROM players WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getLong("plus_expires_at") > now();
            }
        }
    }

    private static List<String> parseNameEffects(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return normalizeNameEffects(List.of(csv.split(",")));
    }

    private static List<String> normalizeNameEffects(List<String> effects) {
        if (effects == null || effects.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String effect : effects) {
            String value = effect == null ? "" : effect.trim().toLowerCase();
            if (value.equals("none") || value.isBlank()) {
                continue;
            }
            if (List.of("shake", "wave", "rainbow", "bounce", "blink", "pulse", "spin", "sequential_spin", "fade", "iterate", "glitch", "scale", "offset", "gradient", "dynamic_gradient_red_blue", "dynamic_gradient_green_yellow", "lava").contains(value)
                    && !normalized.contains(value)) {
                normalized.add(value);
            }
            if (normalized.size() >= 3) {
                break;
            }
        }
        return normalized;
    }

    private static long addClamped(long current, long amount) {
        if (amount > 0 && current > MAX_COINS - amount) {
            return MAX_COINS;
        }
        if (amount < 0 && current < -amount) {
            return 0;
        }
        return clampCoins(current + amount);
    }

    private static long clampCoins(long coins) {
        return Math.max(0, Math.min(MAX_COINS, coins));
    }

    private void audit(String actor, String targetUuid, String action, String detail) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO audit_logs (created_at, action, actor, target_uuid, detail) VALUES (?, ?, ?, ?, ?)")) {
            statement.setLong(1, now());
            statement.setString(2, action);
            statement.setString(3, actor);
            statement.setString(4, targetUuid);
            statement.setString(5, detail);
            statement.executeUpdate();
        }
        LOGGER.info(() -> "audit action=" + action + " target=" + targetUuid + " detail=" + detail);
    }

    private static long now() {
        return Instant.now().getEpochSecond();
    }

    private static String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @Override
    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
