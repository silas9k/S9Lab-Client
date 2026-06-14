package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.emote.EmoteManager.Emote;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.bandana.BandanaRenderer;
import site.s9lab.s9labclient.client.cosmetics.glint.BodyGlintRenderer;
import site.s9lab.s9labclient.client.cosmetics.halo.HaloRenderer;
import site.s9lab.s9labclient.client.cosmetics.hat.HatRenderer;
import site.s9lab.s9labclient.client.cosmetics.shoulder.ShoulderBuddyRenderer;
import site.s9lab.s9labclient.client.cosmetics.wings.WingRenderer;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.impl.utility.TablistBadgeModule;
import site.s9lab.s9labclient.client.util.S9BadgeText;

import java.util.UUID;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void s9labclient$addCosmeticFeatures(
            EntityRendererFactory.Context context,
            boolean slim,
            CallbackInfo ci
    ) {
        FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> featureContext =
                (FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) (Object) this;

        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new BandanaRenderer(featureContext));
        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new HatRenderer(featureContext));

        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new HaloRenderer(featureContext));

        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new WingRenderer(featureContext));
        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new ShoulderBuddyRenderer(featureContext));
        ((LivingEntityRendererAccessor) this).s9labclient$addFeature(new BodyGlintRenderer(featureContext));
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/PlayerLikeEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void s9labclient$showOwnName(
            PlayerLikeEntity entity,
            double squaredDistance,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (S9LabClientClient.getModuleManager() == null || MinecraftClient.getInstance().player == null) {
            return;
        }

        Module module = S9LabClientClient.getModuleManager()
                .getModule("Own Name")
                .orElse(null);

        if (module != null && module.isEnabled() && entity == MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void s9labclient$capturePlayerUuid(
            PlayerLikeEntity player,
            PlayerEntityRenderState state,
            float tickProgress,
            CallbackInfo ci
    ) {
        UUID uuid = player.getUuid();
        CosmeticResolver.remember(state.id, uuid);
        if (!shouldRenderBadge(uuid, state)) {
            return;
        }

        if (state.playerName != null) {
            state.playerName = withBadgeOnce(state.playerName, player.getName().getString(), uuid);
        }
        if (state.displayName != null) {
            state.displayName = withBadgeOnce(state.displayName, player.getName().getString(), uuid);
        }
    }

    @Inject(
            method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("TAIL")
    )
    private void s9labclient$scaleBigHead(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || state.id != client.player.getId()) {
            return;
        }

        if (EmoteManager.activeEmote() != Emote.BIG_HEAD) {
            return;
        }

        matrices.translate(0.0F, 4.00F, 0.0F);
        matrices.scale(4.0F, 4.0F, 4.0F);
    }

    private static boolean shouldShowNameEffects(UUID uuid) {
        Module module = S9LabClientClient.getModuleManager().getModule("Tablist Badge").orElse(null);
        if (!(module instanceof TablistBadgeModule badgeModule)) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownUuid = client.getSession() == null ? null : client.getSession().getUuidOrNull();
        return ownUuid != null && ownUuid.equals(uuid) || badgeModule.showOtherPlayersNameEffects();
    }

    private static boolean isBadgeEnabled() {
        Module module = S9LabClientClient.getModuleManager().getModule("Tablist Badge").orElse(null);
        return module != null && module.isEnabled();
    }

    private static boolean shouldRenderBadge(UUID uuid, PlayerEntityRenderState state) {
        if (S9LabClientClient.getModuleManager() == null || !isBadgeEnabled()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && state.id == client.player.getId()) {
            return true;
        }

        UUID ownUuid = client.getSession() == null ? null : client.getSession().getUuidOrNull();
        return (ownUuid != null && ownUuid.equals(uuid)) || BackendState.isS9Player(uuid);
    }

    private static Text withBadgeOnce(Text text, String playerName, UUID uuid) {
        if (text.getString().startsWith("\uE000") || text.getString().startsWith("\uE001")) {
            return text;
        }
        boolean plus = BackendState.plusIcon(uuid);
        BackendState.NameEffects effects = BackendState.nameEffects(uuid);
        if (plus && effects.enabled() && shouldShowNameEffects(uuid)) {
            return S9BadgeText.withBadgeForPlayer(text, playerName, true, effects.effects());
        }
        return S9BadgeText.withBadge(text, plus);
    }
}
