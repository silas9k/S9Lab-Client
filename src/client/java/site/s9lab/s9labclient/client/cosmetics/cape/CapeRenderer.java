package site.s9lab.s9labclient.client.cosmetics.cape;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerCapeModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;

/**
 * Preview-only cape renderer.
 *
 * Vanilla cape rendering depends on SkinTextures and can be skipped for GUI
 * preview states. This renderer reads the selected preview cosmetic directly
 * from CosmeticPreviewContext, so locked/unowned capes are still visible in
 * cards, the side preview and the studio without changing the real player.
 */
public final class CapeRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final PlayerCapeModel model;

    public CapeRenderer(
            FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
            LoadedEntityModels models
    ) {
        super(context);
        this.model = new PlayerCapeModel(models.getModelPart(EntityModelLayers.PLAYER_CAPE));
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
        Cosmetic cosmetic = CosmeticPreviewContext.getForState(state.id, CosmeticType.CAPE).orElse(null);
        if (cosmetic == null || cosmetic.texture() == null) {
            return;
        }

        matrices.push();
        queue.submitModel(
                model,
                state,
                matrices,
                RenderLayers.entitySolid(cosmetic.texture()),
                light,
                OverlayTexture.DEFAULT_UV,
                state.outlineColor,
                null
        );
        matrices.pop();
    }
}
