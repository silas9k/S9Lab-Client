package site.s9lab.backend.cosmetics;

import java.util.Map;

public final class CosmeticIdAliases {
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("s9lab_cape", "black_edition_s9lab_cape"),
            Map.entry("s9lab_flux_cape", "blue_edition_s9lab_cape"),
            Map.entry("s9lab_gold_edition_cape", "gold_edition_s9lab_cape"),
            Map.entry("s9lab_void_cape", "white_edition_s9lab_cape"),
            Map.entry("s9lab_redstone_cape", "red_edition_s9lab_cape"),
            Map.entry("s9lab_bandana", "black_bandana"),
            Map.entry("s9lab_gold_edition_bandana", "gold_edition_bandana"),
            Map.entry("s9lab_pulse_bandana", "white_bandana"),
            Map.entry("s9lab_void_bandana", "blue_bandana"),
            Map.entry("s9lab_red_bandana", "red_bandana"),
            Map.entry("s9lab_gold_halo", "gold_edition_s9lab_halo"),
            Map.entry("s9lab_void_halo", "white_edition_s9lab_halo"),
            Map.entry("s9lab_emote_seife", "s9lab_emote_seife_emote")
    );

    private CosmeticIdAliases() {
    }

    public static String normalize(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        return ALIASES.getOrDefault(trimmed, trimmed);
    }

    public static Map<String, String> aliases() {
        return ALIASES;
    }
}
