package software.bernie.geckolib.loading.definition.geometry;

import com.google.gson.*;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3518;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

/**
 * Container class for a single geometry bone's texture mesh details, only used for intermediary steps between .json deserialization and GeckoLib object creation
 * <p>
 * This information isn't used by GeckoLib natively
 *
 * @param texture The texture resource location for this mesh
 * @param position The optional position of the pivot point for this mesh <i>after</i> rotation
 * @param localPivot The optional position of the pivot point for this mesh
 * @param rotation The optional rotation of this mesh (in degrees)
 * @param scale The optional scale of the texture on this mesh
 * @see <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/schemasreference/schemas/minecraftschema_geometry_1.21.0?view=minecraft-bedrock-experimental">Bedrock Geometry Spec 1.21.0</a>
 */
@ApiStatus.Internal
public record GeometryTextureMesh(class_2960 texture, @Nullable class_243 position, @Nullable class_243 localPivot, @Nullable class_243 rotation, @Nullable class_243 scale) {
    /**
     * Parse a GeometryTextureMesh instance from raw .json input via {@link Gson}
     */
    public static JsonDeserializer<GeometryTextureMesh> gsonDeserializer() throws JsonParseException {
        return (json, type, context) -> {
            final JsonObject obj = json.getAsJsonObject();
            final class_2960 texture = class_2960.method_60654(class_3518.method_15265(obj, "texture"));
            final JsonArray position = class_3518.method_15292(obj, "position", null);
            final JsonArray localPivot = class_3518.method_15292(obj, "local_pivot", null);
            final JsonArray rotation = class_3518.method_15292(obj, "rotation", null);
            final JsonArray scale = class_3518.method_15292(obj, "scale", null);

            return new GeometryTextureMesh(texture,
                                           position == null ? null : JsonUtil.arrayToVec(JsonUtil.jsonArrayToDoubleArray(position)),
                                           localPivot == null ? null : JsonUtil.arrayToVec(JsonUtil.jsonArrayToDoubleArray(localPivot)),
                                           rotation == null ? null : JsonUtil.arrayToVec(JsonUtil.jsonArrayToDoubleArray(rotation)),
                                           scale == null ? null : JsonUtil.arrayToVec(JsonUtil.jsonArrayToDoubleArray(scale)));
        };
    }
}
