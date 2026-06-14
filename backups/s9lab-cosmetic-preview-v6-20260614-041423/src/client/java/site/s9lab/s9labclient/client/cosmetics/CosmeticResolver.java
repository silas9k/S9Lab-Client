package site.s9lab.s9labclient.client.cosmetics;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendState;

public final class CosmeticResolver {
    private static final Map<Integer, UUID> STATE_UUIDS = new ConcurrentHashMap<>();

    private CosmeticResolver() {
    }

    public static void remember(int entityId, UUID uuid) {
        if (uuid != null) {
            STATE_UUIDS.put(entityId, uuid);
        }
    }

    public static UUID uuidForState(int entityId) {
        return STATE_UUIDS.get(entityId);
    }

    public static Optional<Cosmetic> equippedForState(PlayerEntityRenderState state, CosmeticType type) {
        if (CosmeticPreviewContext.active()) {
            return CosmeticPreviewContext.get(type);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = STATE_UUIDS.get(state.id);
        if (uuid == null && client.player != null && state.id == client.player.getId() && client.getSession() != null) {
            uuid = client.getSession().getUuidOrNull();
        }
        return equippedForPlayer(uuid, type);
    }

    public static Optional<Cosmetic> equippedForPlayer(UUID uuid, CosmeticType type) {
        if (CosmeticPreviewContext.active()) {
            return CosmeticPreviewContext.get(type);
        }
        if (uuid == null || S9LabClientClient.getCosmeticRegistry() == null) {
            return Optional.empty();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownUuid = client.getSession() == null ? null : client.getSession().getUuidOrNull();
        if (ownUuid != null && ownUuid.equals(uuid)) {
            String cosmeticId = BackendState.equippedId(type);
            return S9LabClientClient.getCosmeticRegistry().get(cosmeticId)
                    .filter(cosmetic -> cosmetic.type() == type);
        }

        String cosmeticId = BackendState.remoteEquipped(uuid, type);
        if (cosmeticId == null || cosmeticId.isBlank()) {
            return Optional.empty();
        }
        return S9LabClientClient.getCosmeticRegistry().get(cosmeticId)
                .filter(cosmetic -> cosmetic.type() == type);
    }
}
