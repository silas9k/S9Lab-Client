package site.s9lab.s9labclient.client.cosmetics.halo;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

/**
 * UNUSED FALLBACK MODEL
 *
 * Wird aktuell nicht mehr gerendert.
 * Bleibt nur drin, falls irgendwo im Projekt noch eine Referenz darauf existiert.
 */
public class HaloEntityModel extends EntityModel<PlayerEntityRenderState> {
    public HaloEntityModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
    }

    public static ModelPart createModelPart() {
        ModelData modelData = new ModelData();
        return TexturedModelData.of(modelData, 64, 32).createModel();
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        // Unused.
    }
}