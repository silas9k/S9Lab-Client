package site.s9lab.s9labclient.client.cosmetics.hat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.module.Module;

import java.lang.reflect.Method;

public final class HatGeoRenderHook {
    private static final Identifier MODEL_FILE = Identifier.of(
            S9LabClient.MOD_ID,
            "geckolib/models/s9lab_pirate_hat.geo.json"
    );

    private static final Identifier TEXTURE_FILE = Identifier.of(
            S9LabClient.MOD_ID,
            "textures/cosmetics/hats/s9lab_pirate_hat.png"
    );

    private static final HatGeoAnimatable ANIMATABLE = new HatGeoAnimatable();
    private static final HatGeoObjectRenderer RENDERER = new HatGeoObjectRenderer(new HatGeoModel());

    private static Method performRenderPassMethod;
    private static boolean missingResourcesLogged;

    private HatGeoRenderHook() {
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

        if (!hasRequiredResources()) {
            logMissingResourcesOnce();
            return;
        }

        matrices.push();

        /*
        * GeckoLib-Model an Spieler-Kopf ausrichten.
        * Erst hoch zum Kopf, dann Modellrichtung korrigieren.
        */
        matrices.translate(0.0D, 1.58D, 0.0D);
        matrices.scale(1.0F, -1.0F, 1.0F);
        matrices.translate(0.0D, 0.0D, 0.0D);

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

    private static boolean hasRequiredResources() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getResourceManager() == null) {
            return false;
        }

        return client.getResourceManager().getResource(MODEL_FILE).isPresent()
                && client.getResourceManager().getResource(TEXTURE_FILE).isPresent();
    }

    private static void logMissingResourcesOnce() {
        if (missingResourcesLogged) {
            return;
        }

        missingResourcesLogged = true;

        System.out.println("[S9Lab Hat] Missing pirate hat resources.");
        System.out.println("[S9Lab Hat] Required model: " + MODEL_FILE);
        System.out.println("[S9Lab Hat] Required texture: " + TEXTURE_FILE);
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

    private static boolean isEnabled(int stateId) {
        if (S9LabClientClient.getModuleManager() == null) {
            return false;
        }

        Module module = S9LabClientClient.getModuleManager()
                .getModule("Hat")
                .orElse(null);

        return CosmeticPreviewContext.activeForState(stateId) || (module != null && module.isEnabled());
    }
}