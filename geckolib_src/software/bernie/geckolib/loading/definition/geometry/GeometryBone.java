package software.bernie.geckolib.loading.definition.geometry;

import com.google.gson.*;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

import java.util.Map;
import net.minecraft.class_243;
import net.minecraft.class_3518;

/**
 * Container class for a single geometry bone, only used for intermediary steps between .json deserialization and GeckoLib object creation
 *
 * @param name The name of this bone
 * @param parent The parent bone of this bone, if any
 * @param pivot The pivot point for this bone, or null if not defined
 * @param rotation The rotation of this bone, in degrees, or null if not defined
 * @param debug An optional debug marker for this bone. Not used by GeckoLib
 * @param mirror An optional mirror toggle for this bone
 * @param inflate An optional inflation value for this bone
 * @param renderGroupId The numerical group index this bone belongs to. Not used by GeckoLib
 * @param cubes The array of cube definitions for this bone
 * @param binding An optional binding for this bone, defining its parental relationship. Not used by GeckoLib
 * @param locators An optional map of locator markers for this bone
 * @param polyMesh An optional poly mesh definition for this bone. Not used by GeckoLib
 * @param textureMeshes An optional array of texture mesh definitions for this bone. Not used by GeckoLib
 * @see <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/schemasreference/schemas/minecraftschema_geometry_1.21.0?view=minecraft-bedrock-experimental">Bedrock Geometry Spec 1.21.0</a>
 */
@ApiStatus.Internal
public record GeometryBone(String name, @Nullable String parent, @Nullable class_243 pivot, @Nullable class_243 rotation, boolean debug,
                           boolean mirror, float inflate, int renderGroupId, GeometryCube @Nullable [] cubes,
                           @Nullable String binding, @Nullable Map<String, GeometryLocator> locators, @Nullable GeometryPolyMesh polyMesh,
                           GeometryTextureMesh @Nullable[] textureMeshes) {
    /**
     * Parse a GeometryBone instance from raw .json input via {@link Gson}
     */
    public static JsonDeserializer<GeometryBone> gsonDeserializer() throws JsonParseException {
        return (json, type, context) -> {
            final JsonObject obj = json.getAsJsonObject();
            final String name = class_3518.method_15265(obj, "name");
            final String parent = class_3518.method_15253(obj, "parent", null);
            final class_243 pivot = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "pivot", null));
            final class_243 rotation = JsonUtil.jsonToVec3(class_3518.method_15292(obj, "rotation", null));
            final boolean debug = class_3518.method_15258(obj, "debug", false);
            final boolean mirror = class_3518.method_15258(obj, "mirror", false);
            final float inflate = class_3518.method_15277(obj, "inflate", 0f);
            final int renderGroupId = class_3518.method_15282(obj, "render_group_id", 0);
            final GeometryCube[] cubes = JsonUtil.jsonArrayToObjectArray(class_3518.method_15292(obj, "cubes", new JsonArray()), context, GeometryCube.class);
            final String binding = class_3518.method_15253(obj, "binding", null);
            final Map<String, GeometryLocator> locators = JsonUtil.jsonObjToMap(class_3518.method_15281(obj, "locators", null), context, GeometryLocator.class);
            final GeometryPolyMesh polyMesh = class_3518.method_15283(obj, "poly_mesh", null, context, GeometryPolyMesh.class);
            final GeometryTextureMesh[] textureMeshes = JsonUtil.jsonArrayToObjectArray(class_3518.method_15292(obj, "texture_meshes", null), context, GeometryTextureMesh.class);

            return new GeometryBone(name, parent, pivot, rotation, debug, mirror, inflate, renderGroupId, cubes, binding, locators, polyMesh, textureMeshes);
        };
    }
}
