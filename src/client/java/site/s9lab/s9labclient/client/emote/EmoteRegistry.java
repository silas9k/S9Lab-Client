package site.s9lab.s9labclient.client.emote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.emote.prop.EmotePropRenderHook;
import site.s9lab.s9labclient.client.emote.render.EmotePlayerRenderHook;

/** Loads emote metadata from assets so new Blockbench animations need no Java changes. */
public final class EmoteRegistry {
    private static final Identifier REGISTRY = Identifier.of(S9LabClient.MOD_ID, "emotes/emotes.json");
    private static final Map<String, EmoteDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static volatile boolean loaded;

    private EmoteRegistry() {
    }

    public static synchronized void reload() {
        DEFINITIONS.clear();
        EmotePlayerRenderHook.clear();
        EmotePropRenderHook.clear();
        try {
            InputStream input = openRegistry();
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonArray entries = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("emotes");
                for (JsonElement element : entries) {
                    JsonObject emoteJson = element.getAsJsonObject();
                    if (!bool(emoteJson, "enabled", true)) {
                        continue;
                    }
                    EmoteDefinition definition = parse(emoteJson);
                    if (DEFINITIONS.putIfAbsent(definition.id(), definition) != null) {
                        S9LabClient.LOGGER.warn("Duplicate emote id {} ignored", definition.id());
                    }
                }
            }
            loaded = true;
            S9LabClient.LOGGER.info("Loaded {} data-driven emotes", DEFINITIONS.size());
        } catch (Exception exception) {
            loaded = true;
            S9LabClient.LOGGER.error("Could not load S9Lab emote registry", exception);
        }
    }

    private static InputStream openRegistry() throws Exception {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourceManager() != null) {
            Resource resource = client.getResourceManager().getResource(REGISTRY).orElse(null);
            if (resource != null) return resource.getInputStream();
        }
        InputStream bootstrap = EmoteRegistry.class.getClassLoader()
                .getResourceAsStream("assets/s9labclient/emotes/emotes.json");
        if (bootstrap == null) throw new IllegalStateException("Missing " + REGISTRY);
        return bootstrap;
    }

    public static Collection<EmoteDefinition> all() {
        ensureLoaded();
        return List.copyOf(DEFINITIONS.values());
    }

    public static EmoteDefinition byIdOrName(String value) {
        ensureLoaded();
        String normalized = normalize(value);
        EmoteDefinition direct = DEFINITIONS.get(normalized);
        if (direct != null) return direct;
        return DEFINITIONS.values().stream()
                .filter(emote -> normalize(emote.displayName()).equals(normalized))
                .findFirst().orElse(null);
    }

    public static List<String> categories() {
        ensureLoaded();
        List<String> categories = new ArrayList<>();
        for (EmoteDefinition definition : DEFINITIONS.values()) {
            if (!categories.contains(definition.category())) categories.add(definition.category());
        }
        return List.copyOf(categories);
    }

    private static void ensureLoaded() {
        if (!loaded) reload();
    }

    private static EmoteDefinition parse(JsonObject json) {
        String id = normalize(required(json, "id"));
        String animation = required(json, "animation");
        return new EmoteDefinition(
                id,
                required(json, "name"),
                string(json, "description", ""),
                string(json, "category", "Featured"),
                Identifier.of(animation),
                string(json, "animationName", "animation.player." + id),
                Math.max(1, integer(json, "durationTicks", 160)),
                parseColor(string(json, "accent", "#7EA1FF")),
                bool(json, "loop", true),
                Math.max(1.0F, decimal(json, "headScale", 1.0F)),
                bool(json, "headOnly", false),
                prop(json)
        );
    }

    private static EmotePropDefinition prop(JsonObject json) {
        if (!json.has("prop") || !json.get("prop").isJsonObject()) {
            return null;
        }
        JsonObject prop = json.getAsJsonObject("prop");
        String model = required(prop, "model");
        String texture = required(prop, "texture");
        String animation = required(prop, "animation");
        return new EmotePropDefinition(
                Identifier.of(model),
                Identifier.of(texture),
                Identifier.of(animation),
                string(prop, "animationName", "animation.prop." + normalize(required(json, "id"))),
                Math.max(0.01F, decimal(prop, "scale", 1.0F)),
                decimal(prop, "offsetX", 0.0F),
                decimal(prop, "offsetY", 0.0F),
                decimal(prop, "offsetZ", 0.0F),
                decimal(prop, "rotationX", 0.0F),
                decimal(prop, "rotationY", 0.0F),
                decimal(prop, "rotationZ", 0.0F)
        );
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).getAsString().isBlank()) {
            throw new IllegalArgumentException("Missing emote field: " + key);
        }
        return json.get(key).getAsString();
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static float decimal(JsonObject json, String key, float fallback) {
        return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static int parseColor(String value) {
        try {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            return 0xFF000000 | Integer.parseInt(hex, 16);
        } catch (RuntimeException ignored) {
            return 0xFF7EA1FF;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
