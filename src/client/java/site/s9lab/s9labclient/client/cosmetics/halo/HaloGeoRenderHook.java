package site.s9lab.s9labclient.client.cosmetics.halo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;

public final class HaloGeoRenderHook {
    private static final HaloGeoAnimatable ANIMATABLE = new HaloGeoAnimatable();
    private static final Map<Identifier, HaloGeoObjectRenderer> RENDERERS = new ConcurrentHashMap<>();
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
        if (cosmetic == null || cosmetic.texture() == null) {
            return;
        }

        HaloGeoObjectRenderer renderer = RENDERERS.computeIfAbsent(
                cosmetic.texture(),
                texture -> new HaloGeoObjectRenderer(new HaloGeoModel(texture))
        );

        matrices.push();
        matrices.translate(0.0D, 1.88D, 0.0D);
        matrices.scale(0.78F, -0.78F, 0.78F);

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
