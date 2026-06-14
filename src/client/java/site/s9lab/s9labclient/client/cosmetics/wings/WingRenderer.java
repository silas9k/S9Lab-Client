package site.s9lab.s9labclient.client.cosmetics.wings;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public final class WingRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier DRAGON_TEXTURE =
            Identifier.of("minecraft", "textures/entity/enderdragon/dragon.png");

    private final WingEntityModel model = new WingEntityModel(WingEntityModel.createModelPart());

    public WingRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
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

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.WINGS)
                .orElse(null);

        if (cosmetic == null) {
            return;
        }

        if (!cosmetic.id().equals("s9lab_dragon_wings")
                && !cosmetic.id().equals("s9lab_blue_energy_wings")
                && !cosmetic.id().equals("s9lab_color_dragon_wings")) {
            return;
        }

        Module module = S9LabClientClient.getModuleManager().getModule("Wings").orElse(null);

        if (cosmetic.id().equals("s9lab_blue_energy_wings")) {
            Identifier texture = BlueEnergyWingTextureManager.getTexture(state.age, DRAGON_TEXTURE);
            renderDragonWings(matrices, queue, 15728880, state, texture, module, RenderLayers.entityCutoutNoCull(texture));
            renderDragonWings(matrices, queue, 15728880, state, texture, module, RenderLayers.energySwirl(texture, state.age * 0.006F, state.age * 0.011F));
            return;
        }

        Identifier texture = cosmetic.id().equals("s9lab_color_dragon_wings")
                ? WingColorTextureManager.getTexture(module, state.age, DRAGON_TEXTURE)
                : DRAGON_TEXTURE;

        renderDragonWings(matrices, queue, light, state, texture, module, RenderLayers.entityCutoutNoCull(texture));
    }

    private void renderDragonWings(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            PlayerEntityRenderState state,
            Identifier texture,
            Module module,
            RenderLayer layer
    ) {
        int overlay = LivingEntityRenderer.getOverlay(state, 0.0F);

        float scale = (float) (number(module, "Scale", 100.0D) / 100.0D);
        float height = (float) (number(module, "Height", 0.0D) / 100.0D);
        float sneakOffset = state.isInSneakingPose ? 0.05F : 0.0F;

        matrices.push();

        matrices.translate(0.0F, 0.1F + height + sneakOffset, 0.12F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.scale(0.11F * scale, 0.11F * scale, 0.11F * scale);

        queue.submitModel(
                model,
                state,
                matrices,
                layer,
                light,
                overlay,
                state.outlineColor,
                (ModelCommandRenderer.CrumblingOverlayCommand) null
        );

        matrices.pop();
    }

    private static boolean isEnabled(int stateId) {
        if (CosmeticPreviewContext.activeForState(stateId)) {
            return true;
        }

        if (S9LabClientClient.getModuleManager() == null) {
            return false;
        }

        Module module = S9LabClientClient.getModuleManager().getModule("Wings").orElse(null);
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
