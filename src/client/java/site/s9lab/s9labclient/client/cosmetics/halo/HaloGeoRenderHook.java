package site.s9lab.s9labclient.client.cosmetics.halo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticManifest;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;

public final class HaloGeoRenderHook {
    private static final HaloGeoAnimatable ANIMATABLE = new HaloGeoAnimatable();
    private static final Map<String, HaloGeoObjectRenderer> RENDERERS = new ConcurrentHashMap<>();
    private static Method performRenderPassMethod;

    private HaloGeoRenderHook() {
    }

    public static void render(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            Object cameraState
    ) {
        if (state.invisible || !isEnabled(state.id)) {
            return;
        }

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.HALO).orElse(null);
        if (!(cosmetic instanceof HaloCosmetic haloCosmetic) || haloCosmetic.texture() == null) {
            return;
        }

        CosmeticManifest.HaloManifest halo = S9LabClientClient.getCosmeticRegistry()
                .manifest(haloCosmetic.id())
                .map(CosmeticManifest::halo)
                .orElse(new CosmeticManifest.HaloManifest(
                        haloCosmetic.scale(),
                        haloCosmetic.orbitRadius(),
                        haloCosmetic.orbitSpeed(),
                        haloCosmetic.bobAmplitude(),
                        haloCosmetic.spinSpeed(),
                        haloCosmetic.verticalOffset()
                ));

        String rendererKey = haloCosmetic.effectiveModel() + "|" + haloCosmetic.texture() + "|" + haloCosmetic.effectiveAnimation();
        HaloGeoObjectRenderer renderer = RENDERERS.computeIfAbsent(
                rendererKey,
                ignored -> new HaloGeoObjectRenderer(new HaloGeoModel(
                        haloCosmetic.effectiveModel(),
                        haloCosmetic.texture(),
                        haloCosmetic.effectiveAnimation()
                ))
        );

        matrices.push();
        applyHaloTransform(state, matrices, halo);

        try {
            Method method = getPerformRenderPassMethod(renderer);
            method.invoke(
                    renderer,
                    ANIMATABLE,
                    null,
                    matrices,
                    queue,
                    cameraState,
                    15728880,
                    0
            );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            matrices.pop();
        }
    }

    /**
     * Positioniert den Halo bewusst oberhalb des Kopfes und laesst ihn leicht
     * schweben/orbitieren. Die eigentliche Geometrie bleibt vollstaendig in
     * GeckoLib, Java steuert hier nur die globale Platzierung.
     */
    private static void applyHaloTransform(PlayerEntityRenderState state, MatrixStack matrices, CosmeticManifest.HaloManifest halo) {
        float time = state.age;
        float orbit = time * halo.orbitSpeed();
        float orbitX = MathHelper.cos(orbit) * halo.orbitRadius();
        float orbitZ = MathHelper.sin(orbit) * halo.orbitRadius();
        float bob = MathHelper.sin(time * 0.10F) * halo.bobAmplitude();
        float sneakLift = state.isInSneakingPose ? 0.14F : 0.0F;
        float spin = time * halo.spinSpeed();

        matrices.translate(orbitX, 1.56D + halo.verticalOffset() + bob + sneakLift, orbitZ);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotation(spin));
        matrices.scale(halo.scale(), -halo.scale(), halo.scale());
    }

    private static Method getPerformRenderPassMethod(HaloGeoObjectRenderer renderer) throws NoSuchMethodException {
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

    private static boolean isEnabled(int stateId) {
        if (CosmeticPreviewContext.activeForState(stateId)) {
            return true;
        }
        if (S9LabClientClient.getModuleManager() == null) {
            return false;
        }
        Module module = S9LabClientClient.getModuleManager().getModule("Halo").orElse(null);
        return module != null && module.isEnabled();
    }
}
