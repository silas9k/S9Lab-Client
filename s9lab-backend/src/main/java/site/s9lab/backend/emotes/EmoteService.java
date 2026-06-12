package site.s9lab.backend.emotes;

import java.sql.SQLException;
import java.util.Set;
import site.s9lab.backend.storage.DatabaseManager;

public final class EmoteService {
    private static final Set<String> ALLOWED = Set.of(
            "t_pose",
            "griddy",
            "big_head",
            "billy_bounce",
            "lightning_wave",
            "spin_flex",
            "cape_bow",
            "dragon_flap",
            "dab",
            "robot",
            "chill_bounce",
            "sky_point",
            "heart_beat"
    );

    private final DatabaseManager database;

    public EmoteService(DatabaseManager database) {
        this.database = database;
    }

    public void start(String uuid, String emoteId) throws SQLException {
        if (emoteId == null || !ALLOWED.contains(emoteId)) {
            throw new IllegalArgumentException("invalid_emote");
        }
        if (!database.owns(uuid, cosmeticId(emoteId))) {
            throw new IllegalArgumentException("not_owned");
        }
        database.startEmote(uuid, emoteId);
    }

    public void stop(String uuid) throws SQLException {
        database.stopEmote(uuid);
    }

    private static String cosmeticId(String emoteId) {
        return "s9lab_emote_" + emoteId;
    }
}
