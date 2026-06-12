package software.bernie.geckolib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.class_2350;
import net.minecraft.class_3518;
import org.jspecify.annotations.Nullable;

/**
 * Container class for UV face information, only used in deserialization at startup
 */
public record UVFaces(@Nullable FaceUV north, @Nullable FaceUV south, @Nullable FaceUV east, @Nullable FaceUV west, @Nullable FaceUV up, @Nullable FaceUV down) {
	public static JsonDeserializer<UVFaces> deserializer() {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			FaceUV north = class_3518.method_15283(obj, "north", null, context, FaceUV.class);
			FaceUV south = class_3518.method_15283(obj, "south", null, context, FaceUV.class);
			FaceUV east = class_3518.method_15283(obj, "east", null, context, FaceUV.class);
			FaceUV west = class_3518.method_15283(obj, "west", null, context, FaceUV.class);
			FaceUV up = class_3518.method_15283(obj, "up", null, context, FaceUV.class);
			FaceUV down = class_3518.method_15283(obj, "down", null, context, FaceUV.class);

			return new UVFaces(north, south, east, west, up, down);
		};
	}

	public @Nullable FaceUV fromDirection(class_2350 direction) {
		return switch(direction) {
			case field_11043 -> north;
			case field_11035 -> south;
			case field_11034 -> east;
			case field_11039 -> west;
			case field_11036 -> up;
			case field_11033 -> down;
		};
	}
}
