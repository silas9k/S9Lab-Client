package software.bernie.geckolib.loading.json.typeadapter;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.cache.GeckoLibResources;
import software.bernie.geckolib.cache.animation.Animation;
import software.bernie.geckolib.cache.animation.keyframeevent.CustomInstructionKeyframeData;
import software.bernie.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import software.bernie.geckolib.cache.animation.keyframeevent.SoundKeyframeData;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.class_3518;

/**
 * {@link Gson} {@link JsonDeserializer} for {@link Animation.KeyframeMarkers}
 */
@ApiStatus.Internal
public final class KeyFrameMarkersAdapter {
	/**
	 * Create a GSON {@link JsonDeserializer} for {@link Animation.KeyframeMarkers}
	 */
	public static JsonDeserializer<Animation.KeyframeMarkers> deserializer() throws JsonParseException {
		return KeyFrameMarkersAdapter::fromJson;
	}

	/**
	 * Deserialize a {@link Animation.KeyframeMarkers} from a {@link JsonElement}.
	 */
	private static Animation.KeyframeMarkers fromJson(JsonElement json, Type type, JsonDeserializationContext context) {
		JsonObject obj = json.getAsJsonObject();
		SoundKeyframeData[] sounds = buildSoundFrameData(obj);
		ParticleKeyframeData[] particles = buildParticleFrameData(obj);
		CustomInstructionKeyframeData[] customInstructions = buildCustomFrameData(obj);

		return new Animation.KeyframeMarkers(sounds, particles, customInstructions);
	}

	private static SoundKeyframeData[] buildSoundFrameData(JsonObject rootObj) {
		JsonObject soundsObj = class_3518.method_15281(rootObj, "sound_effects", new JsonObject());
        List<SoundKeyframeData> sounds = new ObjectArrayList<>(soundsObj.size());

		for (Map.Entry<String, JsonElement> entry : soundsObj.entrySet()) {
			sounds.add(new SoundKeyframeData(Double.parseDouble(entry.getKey()), class_3518.method_15265(entry.getValue().getAsJsonObject(), "effect")));
		}

        sounds.sort(Comparator.comparing(SoundKeyframeData::getTime));

		return sounds.toArray(new SoundKeyframeData[0]);
	}

	private static ParticleKeyframeData[] buildParticleFrameData(JsonObject rootObj) {
		JsonObject particlesObj = class_3518.method_15281(rootObj, "particle_effects", new JsonObject());
        List<ParticleKeyframeData> particles = new ObjectArrayList<>(particlesObj.size());

		for (Map.Entry<String, JsonElement> entry : particlesObj.entrySet()) {
			JsonObject obj = entry.getValue().getAsJsonObject();
			String effect = class_3518.method_15253(obj, "effect", "");
			String locator = class_3518.method_15253(obj, "locator", "");
			String script = class_3518.method_15253(obj, "pre_effect_script", "");

			particles.add(new ParticleKeyframeData(Double.parseDouble(entry.getKey()), effect, locator, script));
		}

        particles.sort(Comparator.comparing(ParticleKeyframeData::getTime));

        return particles.toArray(new ParticleKeyframeData[0]);
	}

	private static CustomInstructionKeyframeData[] buildCustomFrameData(JsonObject rootObj) {
		JsonObject customInstructionsObj = class_3518.method_15281(rootObj, "timeline", new JsonObject());
		List<CustomInstructionKeyframeData> customInstructions = new ObjectArrayList<>(customInstructionsObj.size());

		for (Map.Entry<String, JsonElement> entry : customInstructionsObj.entrySet()) {
			String instructions;

			if (entry.getValue() instanceof JsonArray array) {
				instructions = GeckoLibResources.GSON.fromJson(array, ObjectArrayList.class).toString();
			}
			else if (entry.getValue() instanceof JsonPrimitive primitive) {
				instructions = primitive.getAsString();
			}
            else {
                instructions = "";
            }

			customInstructions.add(new CustomInstructionKeyframeData(Double.parseDouble(entry.getKey()), instructions));
		}

        customInstructions.sort(Comparator.comparing(CustomInstructionKeyframeData::getTime));

		return customInstructions.toArray(new CustomInstructionKeyframeData[0]);
	}
}
