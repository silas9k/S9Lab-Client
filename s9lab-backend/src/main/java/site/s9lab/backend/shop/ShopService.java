package site.s9lab.backend.shop;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.storage.DatabaseManager;

public final class ShopService {
    private static final List<Dtos.PlusPlanDto> PLUS_PLANS = List.of(
            new Dtos.PlusPlanDto("plus_1m", "S9Lab Client+ - 1 Month", 1, 750),
            new Dtos.PlusPlanDto("plus_3m", "S9Lab Client+ - 3 Months", 3, 1900)
    );
    private final DatabaseManager database;

    public ShopService(DatabaseManager database) {
        this.database = database;
    }

    public Dtos.PlayerAdminResponse buy(String uuid, String cosmeticId) throws SQLException {
        database.buy(uuid, cosmeticId);
        return database.profile(uuid);
    }

    public Dtos.PlayerAdminResponse buyPlus(String uuid, String planId) throws SQLException {
        Dtos.PlusPlanDto plan = plusPlan(planId);
        database.buyPlus(uuid, plan.months(), plan.price());
        return database.profile(uuid);
    }

    public Dtos.PlusGiftResult giftPlus(String senderUuid, String receiverUuid, String receiverName, String planId) throws SQLException {
        String resolvedReceiver = receiverUuid == null || receiverUuid.isBlank()
                ? resolveReceiverByName(receiverName)
                : receiverUuid;
        Dtos.PlusPlanDto plan = plusPlan(planId);
        database.giftPlus(senderUuid, resolvedReceiver, plan.months(), plan.price());
        return new Dtos.PlusGiftResult(database.profile(senderUuid), resolvedReceiver);
    }

    public List<Dtos.PlusPlanDto> plusPlans() {
        return PLUS_PLANS;
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

    private static Dtos.PlusPlanDto plusPlan(String planId) {
        return PLUS_PLANS.stream()
                .filter(plan -> plan.id().equalsIgnoreCase(planId == null ? "" : planId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid_plus_plan"));
    }
}
