package site.s9lab.s9labclient.client.emote.render;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.emote.EmoteDefinition;
import site.s9lab.s9labclient.client.emote.EmoteManager;

/** Renders the whole animated player through GeckoLib using the live Minecraft skin. */
public final class EmotePlayerRenderHook {
    private static final Map<String, RenderBundle> RENDERERS = new ConcurrentHashMap<>();
    private static Method performRenderPassMethod;

    private EmotePlayerRenderHook() {
    }

    public static void render(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            Object cameraState,
            int light
    ) {
        ActiveEmote active = activeEmote(state);
        if (active == null || state.invisible || state.skinTextures == null || state.skinTextures.body() == null) {
            return;
        }

        Identifier texture = state.skinTextures.body().texturePath();
        boolean slim = state.skinTextures.model() == PlayerSkinType.SLIM;
        String instanceKey = state.id + ":" + active.definition().id() + ":" + active.startedAt()
                + ":" + texture + ":" + slim;
        if (RENDERERS.size() > 256) {
            RENDERERS.clear();
        }
        RenderBundle bundle = RENDERERS.computeIfAbsent(instanceKey, ignored -> {
            EmotePlayerAnimatable animatable = new EmotePlayerAnimatable(active.definition(), texture, slim);
            return new RenderBundle(animatable, new EmotePlayerObjectRenderer(new EmotePlayerGeoModel(animatable)));
        });

        matrices.push();
        // This hook runs before LivingEntityRenderer pops its matrix, so the
        // exact vanilla player translation and body rotation are active.
        // Vanilla's player model is authored Y-down while Bedrock geometry is
        // authored Y-up. Mirror Y, then rotate the Bedrock player into the
        // same facing direction Blockbench previews use.
        matrices.scale(1.0F, -1.0F, 1.0F);
        // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        // Cancelling Vanilla's Y mirror also reverses its 1.501 block model
        // origin offset. Move the Bedrock model back down so its feet remain
        // anchored to the entity position.
        matrices.translate(0.0D, -1.501D, 0.0D);
        try {
            Method method = performRenderPass(bundle.renderer());
            method.invoke(bundle.renderer(), bundle.animatable(), null, matrices, queue, cameraState, light, 0);
        } catch (Throwable throwable) {
            S9LabClient.LOGGER.error("Could not render GeckoLib player emote {}", active.definition().id(), throwable);
            RENDERERS.remove(instanceKey);
        } finally {
            matrices.pop();
        }
    }

    public static boolean hasActiveEmote(PlayerEntityRenderState state) {
        return activeEmote(state) != null;
    }

    public static void clear() {
        RENDERERS.clear();
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

    private static Method performRenderPass(EmotePlayerObjectRenderer renderer) throws NoSuchMethodException {
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

    private record RenderBundle(EmotePlayerAnimatable animatable, EmotePlayerObjectRenderer renderer) {
    }

    private record ActiveEmote(EmoteDefinition definition, long startedAt) {
    }
}
