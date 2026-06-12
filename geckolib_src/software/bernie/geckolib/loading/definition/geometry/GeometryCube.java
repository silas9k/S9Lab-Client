package software.bernie.geckolib.loading.definition.geometry;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.class_243;
import net.minecraft.class_3518;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;
import software.bernie.geckolib.util.MiscUtil;

/**
 * Container class for a single geometry cube, only used for intermediary steps between .json deserialization and GeckoLib object creation
 *
 * @param origin The unrotated lower corner position of the cube, or null if not defined
 * @param size The size of the cube (in {@link MiscUtil#MODEL_TO_WORLD_SIZE model units)}, or null if not defined
 * @param rotation The rotation of the cube, in degrees, or null if not defined
 * @param pivot The pivot point of the cube, defaults to the cube's center, or null if not defined
 * @param inflate An optional inflation value for this cube
 * @param mirror An optional mirror toggle for this cube
 * @param uv The UV coordinate assignments for this cube
 * @see <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/schemasreference/schemas/minecraftschema_geometry_1.21.0?view=minecraft-bedrock-experimental">Bedrock Geometry Spec 1.21.0</a>
 */
@ApiStatus.Internal
public record GeometryCube(@Nullable class_243 origin, @Nullable class_243 size, @Nullable class_243 rotation, @Nullable class_243 pivot, float inflate, boolean mirror, GeometryUv uv) {
    /**
     * Parse a GeometryBone instance from raw .json input via {@link Gson}
     */
    public static JsonDeserializer<GeometryCube> gsonDeserializer() throws JsonParseException {
        return (json, type, context) -> {
            final JsonObject obj = json.getAsJsonObject();
            final class_243 origin = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "origin", null));
            final class_243 size = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "size", null));
            final class_243 rotation = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "rotation", null));
            final class_243 pivot = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "pivot", null));
            final float inflate = class_3518.method_15277(obj, "inflate", 0f);
            final boolean mirror = class_3518.method_15258(obj, "mirror", false);
            final GeometryUv uv = class_3518.method_15283(obj, "uv", null, context, GeometryUv.class);

            return new GeometryCube(origin, size, rotation, pivot, inflate, mirror, uv);
        };
    }
}
