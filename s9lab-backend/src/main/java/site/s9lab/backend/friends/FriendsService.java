package site.s9lab.backend.friends;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.storage.DatabaseManager;

public final class FriendsService {
    private final DatabaseManager database;

    public FriendsService(DatabaseManager database) {
        this.database = database;
    }

    public Dtos.FriendsResponse snapshot(String ownerUuid) throws SQLException {
        return database.friends(requireUuid(ownerUuid));
    }

    public AddResult add(String ownerUuid, String targetUuid, String targetName) throws SQLException {
        String owner = requireUuid(ownerUuid);
        String target = resolveTarget(targetUuid, targetName);
        boolean accepted = database.addFriendRequest(owner, target);
        return new AddResult(target, accepted, database.friends(owner));
    }

    public Dtos.FriendsResponse respond(String ownerUuid, String requesterUuid, boolean accept) throws SQLException {
        String owner = requireUuid(ownerUuid);
        String requester = requireUuid(requesterUuid);
        database.respondFriendRequest(owner, requester, accept);
        return database.friends(owner);
    }

    public Dtos.FriendsResponse remove(String ownerUuid, String friendUuid) throws SQLException {
        String owner = requireUuid(ownerUuid);
        database.removeFriend(owner, requireUuid(friendUuid));
        return database.friends(owner);
    }

    public Dtos.FriendsResponse favorite(String ownerUuid, String friendUuid, boolean favorite) throws SQLException {
        String owner = requireUuid(ownerUuid);
        database.setFriendFavorite(owner, requireUuid(friendUuid), favorite);
        return database.friends(owner);
    }

    public Dtos.FriendMessageDto sendMessage(String ownerUuid, String friendUuid, String message) throws SQLException {
        return database.sendFriendMessage(requireUuid(ownerUuid), requireUuid(friendUuid), message);
    }

    public Dtos.FriendMessagesResponse messages(String ownerUuid, String friendUuid) throws SQLException {
        String owner = requireUuid(ownerUuid);
        String friend = requireUuid(friendUuid);
        List<Dtos.FriendMessageDto> messages = database.friendConversation(owner, friend);
        return new Dtos.FriendMessagesResponse(true, friend, messages);
    }

    private String resolveTarget(String targetUuid, String targetName) throws SQLException {
        if (targetUuid != null && !targetUuid.isBlank()) {
            String uuid = requireUuid(targetUuid);
            if (database.profile(uuid) == null) {
                throw new IllegalArgumentException("player_not_found");
            }
            return uuid;
        }
        Dtos.PlayerAdminResponse profile = database.profileByName(targetName);
        if (profile == null) {
            throw new IllegalArgumentException("player_not_found");
        }
        return profile.uuid();
    }

    private static String requireUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("missing_uuid");
        }
        try {
            return UUID.fromString(uuid.trim()).toString();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid_uuid");
        }
    }

    public record AddResult(String targetUuid, boolean accepted, Dtos.FriendsResponse response) {
    }
}
