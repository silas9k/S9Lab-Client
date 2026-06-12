package site.s9lab.backend.shop;

import java.sql.SQLException;
import java.util.Map;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.storage.DatabaseManager;

public final class ShopService {
    private final DatabaseManager database;

    public ShopService(DatabaseManager database) {
        this.database = database;
    }

    public Dtos.PlayerAdminResponse buy(String uuid, String cosmeticId) throws SQLException {
        database.buy(uuid, cosmeticId);
        return database.profile(uuid);
    }

    public Dtos.PlayerAdminResponse equip(String uuid, String cosmeticId) throws SQLException {
        database.equip(uuid, cosmeticId);
        return database.profile(uuid);
    }

    public Dtos.PlayerAdminResponse unequip(String uuid, String type) throws SQLException {
        database.unequip(uuid, type);
        return database.profile(uuid);
    }

    public Dtos.GiftResult gift(String senderUuid, String receiverUuid, String receiverName, String cosmeticId) throws SQLException {
        String resolvedReceiver = receiverUuid == null || receiverUuid.isBlank()
                ? resolveReceiverByName(receiverName)
                : receiverUuid;
        Dtos.NotificationDto notification = database.gift(senderUuid, resolvedReceiver, cosmeticId);
        return new Dtos.GiftResult(database.profile(senderUuid), resolvedReceiver, notification);
    }

    public Map<String, String> equipped(String uuid) throws SQLException {
        return database.equippedCosmetics(uuid);
    }

    private String resolveReceiverByName(String receiverName) throws SQLException {
        Dtos.PlayerAdminResponse receiver = database.profileByName(receiverName);
        if (receiver == null) {
            throw new IllegalArgumentException("receiver_not_found");
        }
        return receiver.uuid();
    }
}
