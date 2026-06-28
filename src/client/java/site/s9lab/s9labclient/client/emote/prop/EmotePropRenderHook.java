package site.s9lab.s9labclient.client.emote.prop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.emote.EmoteDefinition;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.emote.EmotePropDefinition;

/** Renders optional data-driven GeckoLib props for local, remote and preview emotes. */
public final class EmotePropRenderHook {
    private static final Map<String, RenderBundle> RENDERERS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED_MISSING = ConcurrentHashMap.newKeySet();
    private static final Set<String> WARNED_RENDER = ConcurrentHashMap.newKeySet();
    private static Method performRenderPassMethod;

    private EmotePropRenderHook() {
    }

    public static void render(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            Object cameraState,
            int light
    ) {
        if (state.invisible) {
            return;
        }
        ActiveEmote active = activeEmote(state);
        EmoteDefinition emote = active == null ? null : active.definition();
        EmotePropDefinition prop = emote == null ? null : emote.prop();
        if (prop == null || !resourcesAvailable(emote.id(), prop)) {
            return;
        }

        String instanceKey = state.id + ":" + emote.id() + ":" + active.startedAt();
        if (RENDERERS.size() > 512) {
            RENDERERS.clear();
        }
        RenderBundle bundle = RENDERERS.computeIfAbsent(instanceKey, ignored -> {
            EmotePropAnimatable animatable = new EmotePropAnimatable(prop, emote.loop());
            return new RenderBundle(animatable, new EmotePropObjectRenderer(new EmotePropGeoModel(animatable)));
        });

        matrices.push();
        matrices.scale(1.0F, -1.0F, 1.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.translate(0.0D, -1.501D, 0.0D);
        matrices.translate(prop.offsetX(), prop.offsetY(), prop.offsetZ());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(prop.rotationX()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(prop.rotationY()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(prop.rotationZ()));
        matrices.scale(prop.scale(), prop.scale(), prop.scale());
        try {
            Method method = performRenderPass(bundle.renderer());
            method.invoke(bundle.renderer(), bundle.animatable(), null, matrices, queue, cameraState, light, 0);
        } catch (Throwable throwable) {
            if (WARNED_RENDER.add(emote.id())) {
                S9LabClient.LOGGER.error("Could not render emote prop for {}", emote.id(), throwable);
            }
        } finally {
            matrices.pop();
        }
    }

    public static void clear() {
        RENDERERS.clear();
        WARNED_MISSING.clear();
        WARNED_RENDER.clear();
    }

    private static ActiveEmote activeEmote(PlayerEntityRenderState state) {
        EmoteDefinition preview = CosmeticPreviewContext.emoteForState(state.id);
        if (preview != null) {
            return new ActiveEmote(preview, 0L);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && state.id == client.player.getId()) {
            EmoteDefinition local = EmoteManager.activeEmote();
            return local == null ? null : new ActiveEmote(local, EmoteManager.startedAt());
        }
        UUID uuid = CosmeticResolver.uuidForState(state.id);
        BackendState.RemoteEmote remote = BackendState.remoteEmoteState(uuid);
        EmoteDefinition definition = EmoteManager.byIdOrName(remote.id());
        return definition == null ? null : new ActiveEmote(definition, remote.startedAt());
    }

    private static boolean resourcesAvailable(String emoteId, EmotePropDefinition prop) {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourceManager resources = client.getResourceManager();
        Identifier modelFile = asset(prop.model(), "geckolib/models/", ".geo.json");
        Identifier animationFile = asset(prop.animation(), "geckolib/animations/", ".animation.json");
        boolean available = resources.getResource(modelFile).isPresent()
                && resources.getResource(animationFile).isPresent()
                && resources.getResource(prop.texture()).isPresent();
        if (available) {
            WARNED_MISSING.remove(emoteId);
            return true;
        }
        if (WARNED_MISSING.add(emoteId)) {
            S9LabClient.LOGGER.warn(
                    "Emote prop {} is missing resources: model={}, animation={}, texture={}",
                    emoteId, modelFile, animationFile, prop.texture()
            );
        }
        return false;
    }

    private static Identifier asset(Identifier id, String prefix, String suffix) {
        String path = id.getPath();
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(suffix)) {
            path = path.substring(0, path.length() - suffix.length());
        }
        return Identifier.of(id.getNamespace(), prefix + path + suffix);
    }

    private static Method performRenderPass(EmotePropObjectRenderer renderer) throws NoSuchMethodException {
        if (performRenderPassMethod != null) {
            return performRenderPassMethod;
        }
        for (Method method : renderer.getClass().getMethods()) {
            if (method.getName().equals("performRenderPass") && method.getParameterCount() == 7) {
                method.setAccessible(true);
                performRenderPassMethod = method;
                return method;
            }
        }
        throw new NoSuchMethodException("Could not find GeckoLib performRenderPass method");
    }

    private record RenderBundle(EmotePropAnimatable animatable, EmotePropObjectRenderer renderer) {
    }

    private record ActiveEmote(EmoteDefinition definition, long startedAt) {
    }
}
