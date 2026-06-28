package site.s9lab.s9labclient.client.emote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.resource.Resource;
import site.s9lab.s9labclient.S9LabClient;
import software.bernie.geckolib.animation.state.EasingState;
import software.bernie.geckolib.cache.animation.Animation;
import software.bernie.geckolib.cache.animation.BoneAnimation;
import software.bernie.geckolib.cache.animation.Keyframe;
import software.bernie.geckolib.cache.animation.KeyframeStack;
import software.bernie.geckolib.loading.json.typeadapter.BakedAnimationsAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;

/** Applies GeckoLib's baked Blockbench keyframes to the vanilla player model. */
public final class GeckoPlayerAnimation {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BakedAnimations.class, BakedAnimationsAdapter.deserializer())
            .create();
    private static final Map<String, Animation> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    private GeckoPlayerAnimation() {
    }

    static void clear() {
        CACHE.clear();
        FAILED.clear();
    }

    public static boolean apply(BipedEntityModel<?> model, EmoteDefinition definition, double elapsedSeconds, float blend) {
        Animation animation = load(definition);
        if (animation == null) return false;
        double time = definition.loop() && animation.length() > 0
                ? elapsedSeconds % animation.length()
                : Math.min(elapsedSeconds, animation.length());
        for (BoneAnimation bone : animation.boneAnimations()) {
            BoneTarget target = target(model, bone.boneName());
            if (target == null) continue;
            applyRotation(target.part(), bone.rotationKeyFrames(), time, blend);
            applyPosition(target, bone.positionKeyFrames(), time, blend);
            applyScale(target.part(), bone.scaleKeyFrames(), time, blend);
        }
        return true;
    }

    private static Animation load(EmoteDefinition definition) {
        String key = definition.animationFile() + "#" + definition.animationName();
        if (FAILED.contains(key)) return null;
        return CACHE.computeIfAbsent(key, ignored -> {
            try {
                Resource resource = MinecraftClient.getInstance().getResourceManager()
                        .getResource(definition.animationFile()).orElseThrow();
                try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    BakedAnimations baked = GSON.fromJson(root.getAsJsonObject("animations"), BakedAnimations.class);
                    Animation animation = baked.getAnimation(definition.animationName());
                    if (animation == null && baked.animations().size() == 1) {
                        animation = baked.animations().values().iterator().next();
                        S9LabClient.LOGGER.warn(
                                "Animation {} was not found in {}; using the only animation {} instead",
                                definition.animationName(), definition.animationFile(), animation.name()
                        );
                    }
                    if (animation == null) throw new IllegalArgumentException("Missing animation " + definition.animationName());
                    return animation;
                }
            } catch (Exception exception) {
                if (FAILED.add(key)) S9LabClient.LOGGER.error("Could not load emote animation {}", key, exception);
                return null;
            }
        });
    }

    private static void applyRotation(ModelPart part, KeyframeStack stack, double time, float blend) {
        part.pitch = lerp(part.pitch, (float) sample(stack.xKeyframes(), time, 0), blend);
        part.yaw = lerp(part.yaw, (float) sample(stack.yKeyframes(), time, 0), blend);
        part.roll = lerp(part.roll, (float) sample(stack.zKeyframes(), time, 0), blend);
    }

    private static void applyPosition(BoneTarget target, KeyframeStack stack, double time, float blend) {
        ModelPart part = target.part();

        // Vanilla's player pivots already account for Blockbench's model-space axis conversion.
        // Animation offsets therefore map to (+X, -Y, +Z) on ModelPart origins.
        float x = target.originX() + (float) sample(stack.xKeyframes(), time, 0);
        float y = target.originY() - (float) sample(stack.yKeyframes(), time, 0);
        float z = target.originZ() + (float) sample(stack.zKeyframes(), time, 0);
        part.originX = lerp(part.originX, x, blend);
        part.originY = lerp(part.originY, y, blend);
        part.originZ = lerp(part.originZ, z, blend);
    }

    private static void applyScale(ModelPart part, KeyframeStack stack, double time, float blend) {
        part.xScale = lerp(part.xScale, (float) sample(stack.xKeyframes(), time, 1), blend);
        part.yScale = lerp(part.yScale, (float) sample(stack.yKeyframes(), time, 1), blend);
        part.zScale = lerp(part.zScale, (float) sample(stack.zKeyframes(), time, 1), blend);
    }

    private static double sample(Keyframe[] frames, double time, double defaultValue) {
        if (frames.length == 0) return defaultValue;
        Keyframe current = frames[0];
        Keyframe next = null;
        for (int i = 1; i < frames.length; i++) {
            if (frames[i].startTime() > time) {
                next = frames[i];
                break;
            }
            current = frames[i];
        }
        if (next == null) return current.endValue().get(null);
        double delta = next.length() <= 0 ? 1 : Math.max(0, Math.min(1, (time - current.startTime()) / next.length()));
        double from = current.endValue().get(null);
        double to = next.endValue().get(null);
        return next.easingType().apply(new EasingState(next.easingType(), next.easingArgs(), delta, from, to), null);
    }

    private static BoneTarget target(BipedEntityModel<?> model, String name) {
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "head" -> new BoneTarget(model.head, 0, 0, 0);
            // The vanilla hat is a child of head in 1.21.11 and follows it automatically.
            case "hat" -> null;
            case "body", "torso" -> new BoneTarget(model.body, 0, 0, 0);
            case "rightarm", "right_arm" -> new BoneTarget(model.rightArm, -5, 2, 0);
            case "leftarm", "left_arm" -> new BoneTarget(model.leftArm, 5, 2, 0);
            case "rightleg", "right_leg" -> new BoneTarget(model.rightLeg, -1.9F, 12, 0);
            case "leftleg", "left_leg" -> new BoneTarget(model.leftLeg, 1.9F, 12, 0);
            default -> null;
        };
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * Math.max(0, Math.min(1, amount));
    }

    private record BoneTarget(ModelPart part, float originX, float originY, float originZ) {
    }
}
