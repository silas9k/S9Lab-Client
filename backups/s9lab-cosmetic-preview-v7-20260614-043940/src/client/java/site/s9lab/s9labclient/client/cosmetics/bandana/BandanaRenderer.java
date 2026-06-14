package site.s9lab.s9labclient.client.cosmetics.bandana;

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

public class BandanaRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final BandanaEntityModel model = new BandanaEntityModel(BandanaEntityModel.createModelPart());

    public BandanaRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
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

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.BANDANA)
                .orElse(null);
        if (cosmetic == null) {
            return;
        }

        int overlay = LivingEntityRenderer.getOverlay(state, 0.0F);

        Module module = S9LabClientClient.getModuleManager()
                .getModule("Bandana")
                .orElse(null);

        float headOffset = (float) (number(module, "Head Offset", 0.0D) / 100.0D);

        matrices.push();

        matrices.translate(
                0.0F,
                (state.isInSneakingPose ? 0.24F : 0.0F) + headOffset,
                0.0F
        );

        queue.submitModel(
                model,
                state,
                matrices,
                RenderLayers.entityCutoutNoCull(cosmetic.texture()),
                light,
                overlay,
                state.outlineColor,
                (ModelCommandRenderer.CrumblingOverlayCommand) null
        );

        matrices.pop();
    }

    private static boolean isEnabled(int stateId) {
        if (S9LabClientClient.getModuleManager() == null) {
            return false;
        }

        Module module = S9LabClientClient.getModuleManager()
                .getModule("Bandana")
                .orElse(null);

        return CosmeticPreviewContext.activeForState(stateId) || (module != null && module.isEnabled());
    }

    private static double number(Module module, String name, double fallback) {
        if (module == null) {
            return fallback;
        }

        return module.getSettings()
                .stream()
                .filter(setting -> setting instanceof NumberSetting && setting.getName().equalsIgnoreCase(name))
                .map(setting -> ((NumberSetting) setting).getValue())
                .findFirst()
                .orElse(fallback);
    }
}
