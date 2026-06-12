package software.bernie.geckolib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.class_3518;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

/**
 * Container class for poly mesh information, only used in deserialization at startup
 */
public record PolyMesh(@Nullable Boolean normalizedUVs, double[] normals, @Nullable PolysUnion polysUnion, double[] positions, double[] uvs) {
	public static JsonDeserializer<PolyMesh> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Boolean normalizedUVs = JsonUtil.getOptionalBoolean(obj, "normalized_uvs");
			double[] normals = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "normals", null));
			PolysUnion polysUnion = class_3518.method_15283(obj, "polys", null, context, PolysUnion.class);
			double[] positions = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "positions", null));
			double[] uvs = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "uvs", null));

			return new PolyMesh(normalizedUVs, normals, polysUnion, positions, uvs);
		};
	}
}
