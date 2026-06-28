package site.s9lab.s9labclient.client.cosmetics.shield;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public final class ShieldRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final ShieldGeoAnimatable ANIMATABLE = new ShieldGeoAnimatable();
    private static final Map<Identifier, ShieldGeoObjectRenderer> RENDERERS = new ConcurrentHashMap<>();
    private static final float BASE_SCALE = 1F;
    private static final float ARM_OUTWARD_OFFSET = 0.28F;
    private static final float ARM_DOWN_OFFSET = 0.56F;
    private static final float ARM_FORWARD_OFFSET = 0.00F;
    private static Method performRenderPassMethod;
    private static boolean renderFailureLogged;

    public ShieldRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            PlayerEntityRenderState state,
            float limbAngle,
            float limbDistance
    ) {
        if (state.invisible || !isEnabled(state.id)) {
            return;
        }
        if (!CosmeticPreviewContext.activeForState(state.id)) {
            return;
        }

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.SHIELD).orElse(null);
        if (cosmetic == null || cosmetic.texture() == null) {
            return;
        }

        Module module = S9LabClientClient.getModuleManager() == null
                ? null
                : S9LabClientClient.getModuleManager().getModule("Shield").orElse(null);

        String visibility = mode(module, "Visibility", "Always show");
        if (!CosmeticPreviewContext.activeForState(state.id)
                && visibility.equalsIgnoreCase("Only with shield item")
                && !CosmeticResolver.hasShieldItem(state.id)) {
            return;
        }

        float scale = (float) (number(module, "Scale", 100.0D) / 100.0D);
        float x = (float) (number(module, "X Offset", 0.0D) / 100.0D);
        float y = (float) (number(module, "Y Offset", 0.0D) / 100.0D);
        float z = (float) (number(module, "Z Offset", 0.0D) / 100.0D);
        float rotation = (float) number(module, "Rotation", 0.0D);

        ShieldGeoObjectRenderer renderer = RENDERERS.computeIfAbsent(
                cosmetic.texture(),
                texture -> new ShieldGeoObjectRenderer(new ShieldGeoModel(texture))
        );

        matrices.push();
        this.getContextModel().setArmAngle(state, Arm.LEFT, matrices);

        // Left-arm local space. GeckoLib reads the model from Blockbench JSON,
        // while this transform pins the root to the player's left forearm.
        matrices.translate(ARM_OUTWARD_OFFSET + x, ARM_DOWN_OFFSET + y, ARM_FORWARD_OFFSET + z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.scale(BASE_SCALE * scale, BASE_SCALE * scale, BASE_SCALE * scale);

        try {
            Method method = getPerformRenderPassMethod(renderer);
            method.invoke(
                    renderer,
                    ANIMATABLE,
                    null,
                    matrices,
                    queue,
                    ShieldGeoRenderHook.cameraState(),
                    light,
                    0
            );
        } catch (Throwable throwable) {
            if (!renderFailureLogged) {
                renderFailureLogged = true;
                S9LabClient.LOGGER.warn("Failed to render GeckoLib shield cosmetic on player arm.", throwable);
            }
        } finally {
            matrices.pop();
        }
    }

    private static Method getPerformRenderPassMethod(ShieldGeoObjectRenderer renderer) throws NoSuchMethodException {
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
        Module module = S9LabClientClient.getModuleManager().getModule("Shield").orElse(null);
        return module != null && module.isEnabled();
    }

    private static double number(Module module, String name, double fallback) {
        if (module == null) {
            return fallback;
        }
        return module.getSettings().stream()
                .filter(setting -> setting instanceof NumberSetting && setting.getName().equalsIgnoreCase(name))
                .map(setting -> ((NumberSetting) setting).getValue())
                .findFirst()
                .orElse(fallback);
    }

    private static String mode(Module module, String name, String fallback) {
        if (module == null) {
            return fallback;
        }
        return module.getSettings().stream()
                .filter(setting -> setting instanceof ModeSetting && setting.getName().equalsIgnoreCase(name))
                .map(setting -> ((ModeSetting) setting).getValue())
                .findFirst()
                .orElse(fallback);
    }
}
