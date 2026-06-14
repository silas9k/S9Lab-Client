package site.s9lab.s9labclient.client.cosmetics.hat;

import java.util.HashMap;
import java.util.Map;

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
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;

public class HatRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final Map<String, HatEntityModel> modelCache = new HashMap<>();

    public HatRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
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

        HatCosmetic cosmetic = getSelectedHat(state);
        if (cosmetic == null) {
            return;
        }
        HatEntityModel model = getModel(cosmetic);

        int overlay = LivingEntityRenderer.getOverlay(state, 0.0F);

        matrices.push();

        /*
         * Der Hat hängt direkt am echten Minecraft-Head.
         * Kein extra Monkey-Scale.
         * Kein extra Monkey-Translate.
         */
        this.getContextModel().head.applyTransform(matrices);

        /*
         * Minimale Anti-Clipping-Korrektur.
         * Diese bleibt für alle Hats gleich.
         */
        matrices.translate(0.0F, -0.005F, 0.0F);

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

    private HatEntityModel getModel(HatCosmetic cosmetic) {
        return modelCache.computeIfAbsent(
                cosmetic.id(),
                ignored -> new HatEntityModel(cosmetic.modelFactory().get())
        );
    }

    private static HatCosmetic getSelectedHat(PlayerEntityRenderState state) {
        return CosmeticResolver.equippedForState(state, CosmeticType.HAT)
                .filter(cosmetic -> cosmetic instanceof HatCosmetic)
                .map(cosmetic -> (HatCosmetic) cosmetic)
                .orElse(null);
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
