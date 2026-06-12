package software.bernie.geckolib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.class_3518;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.util.JsonUtil;

/**
 * Container class for texture mesh information, only used in deserialization at startup
 */
public record TextureMesh(double[] localPivot, double[] position, double[] rotation, double[] scale, @Nullable String texture) {
	public static JsonDeserializer<TextureMesh> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			double[] pivot = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "local_pivot", null));
			double[] position = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "position", null));
			double[] rotation = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "rotation", null));
			double[] scale = JsonUtil.jsonArrayToDoubleArray(class_3518.method_15292(obj, "scale", null));
			String texture = class_3518.method_15253(obj, "texture", null);

			return new TextureMesh(pivot, position, rotation, scale, texture);
		};
	}
}
