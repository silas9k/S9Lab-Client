package site.s9lab.s9labclient.client.cosmetics;

import java.util.Locale;
import java.util.Optional;

public enum CosmeticType {
    CAPE("cape", "Capes"),
    BANDANA("bandana", "Bandanas"),
    WINGS("wings", "Wings"),
    HAT("hat", "Hats"),
    HALO("halo", "Halos"),
    SHOULDER("shoulder", "Shoulder Buddies"),
    GLINT("glint", "Glints"),
    EMOTE("emote", "Emotes");

    private final String commandName;
    private final String displayName;

    CosmeticType(String commandName, String displayName) {
        this.commandName = commandName;
        this.displayName = displayName;
    }

    public String commandName() {
        return commandName;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<CosmeticType> byCommandName(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (CosmeticType type : values()) {
            if (type.commandName.equals(normalized) || type.name().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
