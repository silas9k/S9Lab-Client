package site.s9lab.backend.cosmetics;

import java.util.List;

public final class CosmeticCatalog {
    private CosmeticCatalog() {
    }

    public static List<CosmeticDefinition> defaults() {
        return List.of(
                cosmetic("s9lab_cape", "cape", "S9Lab Cape", "Classic S9Lab cape.", 150),
                cosmetic("s9lab_flux_cape", "cape", "Flux Cape", "Animated electric cape.", 350),
                cosmetic("s9lab_void_cape", "cape", "Void Cape", "Dark clean cape.", 250),
                cosmetic("s9lab_redstone_cape", "cape", "Redstone Cape", "Animated redstone cape.", 300),
                cosmetic("s9lab_bandana", "bandana", "Black Bandana", "Clean black bandana.", 120),
                cosmetic("s9lab_pulse_bandana", "bandana", "Pulse Bandana", "Animated pulse bandana.", 250),
                cosmetic("s9lab_void_bandana", "bandana", "Void Bandana", "Void themed bandana.", 180),
                cosmetic("s9lab_red_bandana", "bandana", "Redstone Bandana", "Redstone themed bandana.", 180),
                cosmetic("s9lab_dragon_wings", "wings", "Dragon Wings", "Scaled ender dragon style wings.", 700),
                cosmetic("s9lab_color_dragon_wings", "wings", "Color Dragon Wings", "Configurable dragon wings.", 900),
                cosmetic("s9lab_blue_energy_wings", "wings", "Blue Energy Wings", "Premium cyan-blue energy wings with a soft pulse.", 950),
                cosmetic("s9lab_pirate_hat", "hat", "Pirate Hat", "A pirate hat cosmetic.", 220),
                cosmetic("s9lab_guinea_pig_hat", "hat", "Guinea Pig Hat", "Small animal hat cosmetic.", 250),
                cosmetic("s9lab_duck_hat", "hat", "Duck Hat", "Duck hat cosmetic.", 250),
                cosmetic("s9lab_monkey_hat", "hat", "Monkey Hat", "Monkey hat cosmetic.", 250),
                cosmetic("s9lab_gold_halo", "halo", "Gold Halo", "Golden halo.", 300),
                cosmetic("s9lab_void_halo", "halo", "Void Halo", "Dark halo.", 300),
                cosmetic("s9lab_mini_me", "shoulder", "Mini Me", "Tiny player on your shoulder.", 450),
                cosmetic("s9lab_shadow_buddy", "shoulder", "Shadow Buddy", "Small shadow buddy.", 350),
                cosmetic("s9lab_gold_glint", "glint", "Gold Glint", "Golden body enchantment glint.", 260),
                cosmetic("s9lab_ice_glint", "glint", "Ice Glint", "Cool blue body enchantment glint.", 260),
                cosmetic("s9lab_emerald_glint", "glint", "Emerald Glint", "Emerald body enchantment glint.", 260),
                cosmetic("s9lab_shadow_glint", "glint", "Shadow Glint", "Dark body enchantment glint.", 260),
                cosmetic("s9lab_creeper_glint", "glint", "Creeper Glint", "Creeper armor energy swirl.", 320),
                cosmetic("s9lab_rainbow_glint", "glint", "Rainbow Glint", "Animated rainbow body glint.", 450),
                cosmetic("s9lab_emote_t_pose", "emote", "T-Pose", "Classic T-Pose emote.", 180),
                cosmetic("s9lab_emote_griddy", "emote", "Griddy", "Griddy dance emote.", 350),
                cosmetic("s9lab_emote_big_head", "emote", "Big Head", "Big Head pose emote.", 260),
                cosmetic("s9lab_emote_billy_bounce", "emote", "Billy Bounce", "Bouncy dance emote.", 350)
        );
    }

    private static CosmeticDefinition cosmetic(String id, String type, String name, String description, long price) {
        return new CosmeticDefinition(id, type, name, description, price, true);
    }
}
