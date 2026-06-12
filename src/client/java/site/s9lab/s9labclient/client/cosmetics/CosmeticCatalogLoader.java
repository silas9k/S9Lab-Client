package site.s9lab.s9labclient.client.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.cosmetics.bandana.BandanaCosmetic;
import site.s9lab.s9labclient.client.cosmetics.cape.CapeCosmetic;
import site.s9lab.s9labclient.client.cosmetics.hat.HatCosmetic;
import site.s9lab.s9labclient.client.cosmetics.wings.WingCosmetic;

public final class CosmeticCatalogLoader {
    private static final Gson GSON = new Gson();
    private static final String CATALOG_PATH = "assets/" + S9LabClient.MOD_ID + "/s9lab_cosmetics.json";

    private CosmeticCatalogLoader() {
    }

    public static void loadInto(CosmeticRegistry registry) {
        try (InputStream stream = CosmeticCatalogLoader.class.getClassLoader().getResourceAsStream(CATALOG_PATH)) {
            if (stream == null) {
                registerFallback(registry);
                return;
            }

            Catalog catalog = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Catalog.class);
            if (catalog == null || catalog.cosmetics == null || catalog.cosmetics.isEmpty()) {
                registerFallback(registry);
                return;
            }

            for (Entry entry : catalog.cosmetics) {
                registerEntry(registry, entry);
            }
        } catch (IOException | JsonSyntaxException exception) {
            registerFallback(registry);
        }
    }

    private static void registerEntry(CosmeticRegistry registry, Entry entry) {
        if (entry == null || entry.id == null || entry.name == null || entry.type == null || entry.texture == null) {
            return;
        }

        CosmeticType.byCommandName(entry.type).ifPresent(type -> registry.register(createCosmetic(type, entry)));
    }

    private static Cosmetic createCosmetic(CosmeticType type, Entry entry) {
        Identifier texture = id(entry.texture);
        boolean animated = entry.animated;

        return switch (type) {
            case CAPE -> new CapeCosmetic(
                    entry.id,
                    entry.name,
                    texture,
                    id(defaulted(entry.model, "geo/s9lab_cape.geo.json")),
                    id(defaulted(entry.animation, "animations/s9lab_cape.animation.json")),
                    animated
            );

            case BANDANA -> new BandanaCosmetic(
                    entry.id,
                    entry.name,
                    texture,
                    id(defaulted(entry.model, "geo/s9lab_bandana.geo.json")),
                    id(defaulted(entry.animation, "animations/s9lab_bandana.animation.json")),
                    animated
            );

            case WINGS -> new WingCosmetic(
                    entry.id,
                    entry.name,
                    texture,
                    animated
            );

            case HAT -> createHatCosmetic(entry, texture);

            case HALO, SHOULDER, GLINT, EMOTE -> new SimpleCosmetic(
                    entry.id,
                    entry.name,
                    type,
                    texture,
                    animated
            );
        };
    }

    private static Cosmetic createHatCosmetic(Entry entry, Identifier texture) {
        return switch (entry.id) {
            case "s9lab_pirate_hat" -> new HatCosmetic(
                    entry.id,
                    entry.name,
                    texture,
                    site.s9lab.s9labclient.client.cosmetics.hat.HatEntityModel::createModelPart
            );

            case "s9lab_guinea_pig_hat" -> new HatCosmetic(
            entry.id,
            entry.name,
            texture,
            site.s9lab.s9labclient.client.cosmetics.hat.GuineaPigHatEntityModel::createModelPart
            );

            case "s9lab_duck_hat" -> new HatCosmetic(
                    entry.id,
                    entry.name,
                    texture,
                    site.s9lab.s9labclient.client.cosmetics.hat.DuckHatEntityModel::createModelPart
            );

            default -> {
                S9LabClient.LOGGER.warn("Unknown hat cosmetic id '{}'. Falling back to Pirate Hat model.", entry.id);
                yield new HatCosmetic(
                        entry.id,
                        entry.name,
                        texture,
                        site.s9lab.s9labclient.client.cosmetics.hat.HatEntityModel::createModelPart
                );
            }
        };
    }

    private static void registerFallback(CosmeticRegistry registry) {
        registry.register(new CapeCosmetic(
                "s9lab_cape",
                "S9Lab Cape",
                id("textures/cosmetics/capes/s9lab_cape.png"),
                id("geo/s9lab_cape.geo.json"),
                id("animations/s9lab_cape.animation.json"),
                false
        ));

        registry.register(new CapeCosmetic(
                "s9lab_flux_cape",
                "Flux Cape",
                id("textures/cosmetics/capes/s9lab_flux_cape.png"),
                id("geo/s9lab_cape.geo.json"),
                id("animations/s9lab_cape.animation.json"),
                true
        ));

        registry.register(new BandanaCosmetic(
                "s9lab_bandana",
                "Black Bandana",
                id("textures/cosmetics/bandanas/s9lab_bandana.png"),
                id("geo/s9lab_bandana.geo.json"),
                id("animations/s9lab_bandana.animation.json"),
                false
        ));

        registry.register(new WingCosmetic(
                "s9lab_color_dragon_wings",
                "Color Dragon Wings",
                id("minecraft:textures/entity/enderdragon/dragon.png"),
                true
        ));
        
        registry.register(new WingCosmetic(
                "s9lab_dragon_wings",
                "Dragon Wings",
                id("minecraft:textures/entity/enderdragon/dragon.png"),
                true
        ));

        registry.register(new WingCosmetic(
                "s9lab_blue_energy_wings",
                "Blue Energy Wings",
                id("minecraft:textures/entity/enderdragon/dragon.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_gold_glint",
                "Gold Glint",
                CosmeticType.GLINT,
                id("textures/cosmetics/glint/gold_glint.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_ice_glint",
                "Ice Glint",
                CosmeticType.GLINT,
                id("textures/cosmetics/glint/ice_glint.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_shadow_glint",
                "Shadow Glint",
                CosmeticType.GLINT,
                id("textures/cosmetics/glint/shadow_glint.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_emerald_glint",
                "Emerald Glint",
                CosmeticType.GLINT,
                id("textures/cosmetics/glint/emerald_glint.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_creeper_glint",
                "Creeper Glint",
                CosmeticType.GLINT,
                id("minecraft:textures/entity/creeper/creeper_armor.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_rainbow_glint",
                "Rainbow Glint",
                CosmeticType.GLINT,
                id("textures/cosmetics/glint/gold_glint.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_emote_t_pose",
                "T-Pose",
                CosmeticType.EMOTE,
                id("minecraft:textures/item/armor_stand.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_emote_griddy",
                "Griddy",
                CosmeticType.EMOTE,
                id("minecraft:textures/item/diamond_boots.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_emote_big_head",
                "Big Head",
                CosmeticType.EMOTE,
                id("minecraft:textures/item/player_head.png"),
                true
        ));

        registry.register(new SimpleCosmetic(
                "s9lab_emote_billy_bounce",
                "Billy Bounce",
                CosmeticType.EMOTE,
                id("minecraft:textures/item/slime_ball.png"),
                true
        ));

        registry.register(HatCosmetic.pirateHat());
        registry.register(HatCosmetic.guineaPigHat());
        registry.register(HatCosmetic.duckHat());
    }

    private static String defaulted(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Identifier id(String value) {
        return value.contains(":") ? Identifier.of(value) : Identifier.of(S9LabClient.MOD_ID, value);
    }

    private static final class Catalog {
        private List<Entry> cosmetics;
    }

    private static final class Entry {
        private String id;
        private String name;
        private String type;
        private String texture;
        private String model;
        private String animation;
        private boolean animated;
    }
}
