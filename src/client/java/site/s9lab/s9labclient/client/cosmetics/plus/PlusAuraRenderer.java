package site.s9lab.s9labclient.client.cosmetics.plus;

import java.util.UUID;
import net.minecraft.client.MinecraftClient;
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
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public final class PlusAuraRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier GOLD = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/gold_glint.png");
    private static final Identifier ICE = Identifier.of(S9LabClient.MOD_ID, "textures/cosmetics/glint/ice_glint.png");
    private final PlusAuraEntityModel model = new PlusAuraEntityModel(PlusAuraEntityModel.createModelPart());

    public PlusAuraRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
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
        Module module = module();
        if (state.invisible || module == null || !module.isEnabled()) {
            return;
        }

        UUID uuid = uuidForState(state);
        if (uuid == null || !isPlus(uuid)) {
            return;
        }

        boolean ownPlayer = isOwnPlayer(uuid);
        if (ownPlayer && !bool(module, "Show My Aura", true)) {
            return;
        }
        if (!ownPlayer && !bool(module, "Show Other Plus Auras", true)) {
            return;
        }

        float size = (float) (number(module, "Size", 100.0D) / 100.0D);
        float speed = (float) (number(module, "Speed", 100.0D) / 100.0D);
        float pulse = (float) (number(module, "Pulse", 100.0D) / 100.0D);
        model.configure(speed, pulse);

        int overlay = LivingEntityRenderer.getOverlay(state, 0.0F);
        matrices.push();
        matrices.scale(size, size, size);
        matrices.translate(0.0F, state.isInSneakingPose ? 0.08F : 0.0F, 0.0F);
        submit(queue, matrices, state, RenderLayers.energySwirl(GOLD, state.age * 0.018F * speed, state.age * 0.006F * speed), light, overlay);
        submit(queue, matrices, state, RenderLayers.energySwirl(ICE, -state.age * 0.012F * speed, state.age * 0.015F * speed), light, overlay);
        matrices.pop();
    }

    private void submit(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            PlayerEntityRenderState state,
            RenderLayer layer,
            int light,
            int overlay
    ) {
        queue.submitModel(
                model,
                state,
                matrices,
                layer,
                15728880,
                overlay,
                state.outlineColor,
                (ModelCommandRenderer.CrumblingOverlayCommand) null
        );
    }

    private static Module module() {
        if (S9LabClientClient.getModuleManager() == null) {
            return null;
        }
        return S9LabClientClient.getModuleManager().getModule("S9C+ Aura").orElse(null);
    }

    private static UUID uuidForState(PlayerEntityRenderState state) {
        UUID uuid = CosmeticResolver.uuidForState(state.id);
        MinecraftClient client = MinecraftClient.getInstance();
        if (uuid == null && client.player != null && state.id == client.player.getId() && client.getSession() != null) {
            uuid = client.getSession().getUuidOrNull();
        }
        return uuid;
    }

    private static boolean isOwnPlayer(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownUuid = client.getSession() == null ? null : client.getSession().getUuidOrNull();
        return ownUuid != null && ownUuid.equals(uuid);
    }

    private static boolean isPlus(UUID uuid) {
        return BackendState.plusIcon(uuid) || (isOwnPlayer(uuid) && BackendState.plusActive());
    }

    private static boolean bool(Module module, String name, boolean fallback) {
        return module.getSettings()
                .stream()
                .filter(setting -> setting instanceof BooleanSetting && setting.getName().equalsIgnoreCase(name))
                .map(setting -> ((BooleanSetting) setting).getValue())
                .findFirst()
                .orElse(fallback);
    }

    private static double number(Module module, String name, double fallback) {
        return module.getSettings()
                .stream()
                .filter(setting -> setting instanceof NumberSetting && setting.getName().equalsIgnoreCase(name))
                .map(setting -> ((NumberSetting) setting).getValue())
                .findFirst()
                .orElse(fallback);
    }
}
