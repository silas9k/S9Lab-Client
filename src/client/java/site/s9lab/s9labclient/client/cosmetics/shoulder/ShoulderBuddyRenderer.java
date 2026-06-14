package site.s9lab.s9labclient.client.cosmetics.shoulder;

import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public class ShoulderBuddyRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final ShoulderBuddyEntityModel model = new ShoulderBuddyEntityModel(ShoulderBuddyEntityModel.createModelPart());

    public ShoulderBuddyRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (state.invisible || !isEnabled(state.id)) {
            return;
        }

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.SHOULDER)
                .orElse(null);
        if (cosmetic == null) {
            return;
        }
        var texture = "s9lab_mini_me".equals(cosmetic.id()) && state.skinTextures != null && state.skinTextures.body() != null
                ? state.skinTextures.body().texturePath()
                : cosmetic.texture();

        matrices.push();
        Module module = S9LabClientClient.getModuleManager().getModule("Shoulder Buddy").orElse(null);
        float scale = (float) (number(module, "Scale", 100.0D) / 100.0D);
        float height = (float) (number(module, "Height", 0.0D) / 100.0D);
        matrices.translate(0.32F, -0.21F + height + (state.isInSneakingPose ? 0.20F : 0.0F), 0.03F);
        matrices.scale(0.18F * scale, 0.18F * scale, 0.18F * scale);
        queue.submitModel(model, state, matrices, RenderLayers.entityCutoutNoCull(texture), light, LivingEntityRenderer.getOverlay(state, 0.0F), state.outlineColor, (ModelCommandRenderer.CrumblingOverlayCommand) null);
        matrices.pop();
    }

    private static boolean isEnabled(int stateId) {
        if (CosmeticPreviewContext.activeForState(stateId)) {
            return true;
        }

        Module module = S9LabClientClient.getModuleManager().getModule("Shoulder Buddy").orElse(null);
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
}
