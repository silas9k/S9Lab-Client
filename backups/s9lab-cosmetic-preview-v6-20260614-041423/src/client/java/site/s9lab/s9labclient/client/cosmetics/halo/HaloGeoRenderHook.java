package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.module.Module;

import java.lang.reflect.Method;

public final class HaloGeoRenderHook {
    private static final HaloGeoAnimatable ANIMATABLE = new HaloGeoAnimatable();
    private static final HaloGeoObjectRenderer RENDERER = new HaloGeoObjectRenderer(new HaloGeoModel());

    private static Method performRenderPassMethod;

    private HaloGeoRenderHook() {
    }

    public static void render(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            Object cameraState
    ) {
        if (state.invisible || !isEnabled()) {
            return;
        }

        matrices.push();

        try {
            Method method = getPerformRenderPassMethod();

            int fullBright = 15728880;
            int partialTick = 0;

            method.invoke(
                    RENDERER,
                    ANIMATABLE,
                    null,
                    matrices,
                    queue,
                    cameraState,
                    fullBright,
                    partialTick
            );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            matrices.pop();
        }
    }

    private static Method getPerformRenderPassMethod() throws NoSuchMethodException {
        if (performRenderPassMethod != null) {
            return performRenderPassMethod;
        }

        for (Method method : RENDERER.getClass().getMethods()) {
            if (!method.getName().equals("performRenderPass")) {
                continue;
            }

            if (method.getParameterCount() == 7) {
                method.setAccessible(true);
                performRenderPassMethod = method;
                return method;
            }
        }

        throw new NoSuchMethodException("Could not find GeckoLib performRenderPass method");
    }

    private static boolean isEnabled() {
        if (S9LabClientClient.getModuleManager() == null) {
            return false;
        }

        Module module = S9LabClientClient.getModuleManager()
                .getModule("Halo")
                .orElse(null);

        return CosmeticPreviewContext.active() || (module != null && module.isEnabled());
    }
}