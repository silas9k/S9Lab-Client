package software.bernie.geckolib.loading.definition.geometry;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import net.minecraft.class_243;
import net.minecraft.class_3518;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

/**
 * Container class for a single geometry bone locator, only used for intermediary steps between .json deserialization and GeckoLib object creation
 * <p>
 * This information isn't used by GeckoLib natively
 *
 * @param offset The position of this locator, relative to the bone it belongs to
 * @param rotation The rotation of this locator, in degrees
 * @see <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/schemasreference/schemas/minecraftschema_geometry_1.21.0?view=minecraft-bedrock-experimental">Bedrock Geometry Spec 1.21.0</a>
 */
@ApiStatus.Internal
public record GeometryLocator(@Nullable class_243 offset, @Nullable class_243 rotation) {
    /**
     * Parse a GeometryLocators instance from raw .json input via {@link Gson}
     */
    public static JsonDeserializer<GeometryLocator> gsonDeserializer() throws JsonParseException {
        return (json, type, context) -> {
            final boolean isArray = json.isJsonArray();
            final class_243 offset = JsonUtil.jsonToVec3(isArray ? json.getAsJsonArray() : class_3518.method_15261(json.getAsJsonObject(), "offset"));
            final class_243 rotation = isArray ? null : JsonUtil.jsonToVec3(class_3518.method_15261(json.getAsJsonObject(), "rotation"));

            return new GeometryLocator(offset, rotation);
        };
    }
}
