package software.bernie.geckolib.loading.definition.geometry;

import com.google.gson.*;
import net.minecraft.class_243;
import net.minecraft.class_3518;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

/**
 * Container class for a single geometry bone's polygon mesh, only used for intermediary steps between .json deserialization and GeckoLib object creation
 * <p>
 * This information isn't used by GeckoLib natively
 *
 * @param normalizedUvs Whether the UV coordinates are normalized (0->1) or otherwise, 0->texture size
 * @param positions The vertex positions for this mesh
 * @param normals The vertex normals for this mesh
 * @param uvs The UV coordinates for this mesh
 * @param polys The polygon indices for this mesh
 * @param polysFormat The format of the polygon indices for this mesh
 * @see <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/schemasreference/schemas/minecraftschema_geometry_1.21.0?view=minecraft-bedrock-experimental">Bedrock Geometry Spec 1.21.0</a>
 */
@ApiStatus.Internal
public record GeometryPolyMesh(boolean normalizedUvs, class_243 @Nullable[] positions, class_243 @Nullable[] normals, GeometryUv @Nullable [] uvs, GeometryPolyIndices polys, String polysFormat) {
    /**
     * Parse a GeometryTextureMesh instance from raw .json input via {@link Gson}
     */
    public static JsonDeserializer<GeometryPolyMesh> gsonDeserializer() throws JsonParseException {
        return (json, type, context) -> {
            final JsonObject obj = json.getAsJsonObject();
            final boolean normalizedUvs = class_3518.method_15258(obj, "normalized_uvs", true);
            final class_243[] positions = JsonUtil.jsonArrayToObjectArray(class_3518.method_15292(obj, "positions", null), JsonUtil::jsonToVec3);
            final class_243[] normals = JsonUtil.jsonArrayToObjectArray(class_3518.method_15292(obj, "normals", null), JsonUtil::jsonToVec3);
            final GeometryUv[] uvs = JsonUtil.jsonArrayToObjectArray(class_3518.method_15292(obj, "uvs", null), context, GeometryUv.class);
            final GeometryPolyIndices polys = class_3518.method_15272(obj, "polys", context, GeometryPolyIndices.class);
            final String polysFormat = class_3518.method_15253(obj, "polys_format", "polys");

            return new GeometryPolyMesh(normalizedUvs, positions, normals, uvs, polys, polysFormat);
        };
    }
}
