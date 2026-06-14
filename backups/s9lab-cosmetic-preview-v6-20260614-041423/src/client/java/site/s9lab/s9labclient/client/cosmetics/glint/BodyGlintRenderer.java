package site.s9lab.s9labclient.client.cosmetics.glint;

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
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.impl.cosmetics.GlintModule;

public class BodyGlintRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier CREEPER_ARMOR = Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
    private static final Identifier GOLD_GLINT = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/gold_glint.png");
    private static final Identifier ICE_GLINT = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/ice_glint.png");
    private static final Identifier EMERALD_GLINT = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/emerald_glint.png");
    private static final Identifier SHADOW_GLINT = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/shadow_glint.png");

    public BodyGlintRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        GlintModule glintModule = getGlintModule();

        if (glintModule == null || state.invisible || (!CosmeticPreviewContext.active() && !glintModule.isEnabled())) {
            return;
        }

        Cosmetic cosmetic = CosmeticResolver.equippedForState(state, CosmeticType.GLINT).orElse(null);
        if (cosmetic == null) {
            return;
        }

        RenderLayer layer = layerFor(cosmetic.id(), state.age);

        int overlay = LivingEntityRenderer.getOverlay(state, 0.0F);

        queue.submitModel(
                getContextModel(),
                state,
                matrices,
                layer,
                light,
                overlay,
                state.outlineColor,
                (ModelCommandRenderer.CrumblingOverlayCommand) null
        );
    }

    private static GlintModule getGlintModule() {
        if (S9LabClientClient.getModuleManager() == null) {
            return null;
        }

        Module module = S9LabClientClient.getModuleManager().getModule("Glint").orElse(null);
        return module instanceof GlintModule glintModule ? glintModule : null;
    }

    private static RenderLayer layerFor(String cosmeticId, float age) {
        return switch (cosmeticId) {
            case "s9lab_creeper_glint" -> RenderLayers.energySwirl(CREEPER_ARMOR, age * 0.01F, age * 0.01F);
            case "s9lab_gold_glint" -> RenderLayers.energySwirl(GOLD_GLINT, age * 0.012F, age * 0.006F);
            case "s9lab_ice_glint" -> RenderLayers.energySwirl(ICE_GLINT, age * 0.008F, age * 0.014F);
            case "s9lab_emerald_glint" -> RenderLayers.energySwirl(EMERALD_GLINT, age * 0.014F, age * 0.01F);
            case "s9lab_shadow_glint" -> RenderLayers.energySwirl(SHADOW_GLINT, age * 0.006F, age * 0.018F);
            case "s9lab_rainbow_glint" -> RenderLayers.energySwirl(RainbowGlintTextureManager.get(age), age * 0.010F, age * 0.018F);
            default -> RenderLayers.entityGlint();
        };
    }
}
