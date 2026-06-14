package site.s9lab.s9labclient.mixin.client;

import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext.BaseVisibility;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.emote.EmoteManager.Emote;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void s9labclient$applyEmote(PlayerEntityRenderState state, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean localPlayer = client.player != null && state.id == client.player.getId();
        Emote previewEmote = CosmeticPreviewContext.emoteForState(state.id);
        Emote emote = previewEmote != null
                ? previewEmote
                : (localPlayer ? EmoteManager.activeEmote() : remoteEmote(state));

        PlayerEntityModel playerModel = (PlayerEntityModel) (Object) this;
        BipedEntityModel<?> model = playerModel;
        resetVisibility(playerModel, model);

        BaseVisibility previewVisibility = CosmeticPreviewContext.visibilityForState(state.id);
        if (previewVisibility == BaseVisibility.HIDDEN) {
            hideBasePlayer(playerModel, model);
        } else if (previewVisibility == BaseVisibility.HEAD_ONLY) {
            showHeadOnly(playerModel, model);
        }

        if (client.player == null || emote == null) {
            return;
        }

        float wave = EmoteManager.wave(state.age, 0.34F);
        float fast = EmoteManager.wave(state.age, 0.72F);
        float progress = previewEmote != null
                ? Math.floorMod((int) state.age, Math.max(1, previewEmote.durationTicks()))
                        / (float) Math.max(1, previewEmote.durationTicks())
                : (localPlayer ? EmoteManager.progress() : 0.5F);

        model.hat.pitch = model.head.pitch;

        switch (emote) {
            case LIGHTNING_WAVE -> {
                model.body.yaw += wave * 0.08F;
                model.rightArm.pitch = -2.35F + fast * 0.34F;
                model.rightArm.yaw = -0.35F + wave * 0.18F;
                model.rightArm.roll = 0.36F + fast * 0.18F;
                model.leftArm.pitch = -0.24F + wave * 0.08F;
            }

            case SPIN_FLEX -> {
                model.body.yaw += wave * 0.20F;
                model.rightArm.pitch = -1.48F;
                model.leftArm.pitch = -1.48F;
                model.rightArm.yaw = 0.66F;
                model.leftArm.yaw = -0.66F;
                model.rightArm.roll = 0.82F + fast * 0.10F;
                model.leftArm.roll = -0.82F - fast * 0.10F;
            }

            case CAPE_BOW -> {
                float bow = (float) Math.sin(Math.min(1.0F, progress * 2.0F) * Math.PI) * 0.55F;
                model.body.pitch += bow;
                model.head.pitch += bow * 0.45F;
                model.rightArm.pitch = -0.34F + bow * 0.25F;
                model.leftArm.pitch = -0.34F + bow * 0.25F;
            }

            case DRAGON_FLAP -> {
                model.body.pitch += 0.08F;
                model.rightArm.pitch = -0.95F + fast * 0.55F;
                model.leftArm.pitch = -0.95F - fast * 0.55F;
                model.rightArm.roll = 0.92F + wave * 0.22F;
                model.leftArm.roll = -0.92F + wave * 0.22F;
            }

            case DAB -> {
                model.head.yaw += 0.35F;
                model.rightArm.pitch = -1.82F;
                model.rightArm.yaw = -0.82F;
                model.rightArm.roll = 0.22F;
                model.leftArm.pitch = -0.35F;
                model.leftArm.yaw = 0.92F;
                model.leftArm.roll = -0.68F;
            }

            case T_POSE -> {
                model.rightArm.pitch = 0.0F;
                model.leftArm.pitch = 0.0F;
                model.rightArm.roll = 1.57F;
                model.leftArm.roll = -1.57F;
                model.body.yaw += wave * 0.05F;
            }

            case GRIDDY -> {
                float bounce = Math.abs(fast);

                model.body.yaw += wave * 0.25F;
                model.body.pitch += bounce * 0.08F;
                model.head.yaw += wave * 0.15F;

                model.rightArm.pitch = -0.65F + wave * 0.55F;
                model.leftArm.pitch = -0.65F - wave * 0.55F;
                model.rightArm.roll = 0.35F + wave * 0.25F;
                model.leftArm.roll = -0.35F - wave * 0.25F;

                model.rightLeg.pitch += fast * 0.65F;
                model.leftLeg.pitch -= fast * 0.65F;
                model.rightLeg.yaw += 0.10F;
                model.leftLeg.yaw -= 0.10F;
            }
            case BIG_HEAD -> {
                model.body.visible = false;
                model.rightArm.visible = false;
                model.leftArm.visible = false;
                model.rightLeg.visible = false;
                model.leftLeg.visible = false;

                playerModel.jacket.visible = false;
                playerModel.leftSleeve.visible = false;
                playerModel.rightSleeve.visible = false;
                playerModel.leftPants.visible = false;
                playerModel.rightPants.visible = false;

                model.head.visible = true;
                model.hat.visible = true;
                if (previewEmote != null) {
                    model.head.xScale = 2.35F;
                    model.head.yScale = 2.35F;
                    model.head.zScale = 2.35F;
                    model.hat.xScale = 2.35F;
                    model.hat.yScale = 2.35F;
                    model.hat.zScale = 2.35F;
                }

                model.head.pitch = wave * 0.03F;
                model.head.yaw = wave * 0.08F;
                model.head.roll = 0.0F;

                model.hat.pitch = model.head.pitch;
                model.hat.yaw = model.head.yaw;
                model.hat.roll = model.head.roll;
            }

            case BILLY_BOUNCE -> {
                float bounce = Math.abs(fast);

                model.body.pitch += bounce * 0.10F;
                model.body.yaw += wave * 0.12F;
                model.head.pitch += bounce * 0.05F;
                model.head.yaw += wave * 0.10F;

                model.rightArm.pitch = -0.55F + fast * 0.35F;
                model.leftArm.pitch = -0.55F - fast * 0.35F;
                model.rightArm.roll = 0.28F + wave * 0.20F;
                model.leftArm.roll = -0.28F - wave * 0.20F;
                model.rightArm.yaw = -0.18F;
                model.leftArm.yaw = 0.18F;

                model.rightLeg.pitch += fast * 0.45F;
                model.leftLeg.pitch -= fast * 0.45F;
                model.rightLeg.yaw += wave * 0.08F;
                model.leftLeg.yaw -= wave * 0.08F;
            }

            case ROBOT -> {
                float snap = Math.round(fast * 2.0F) / 2.0F;
                model.body.yaw += snap * 0.16F;
                model.rightArm.pitch = -1.22F + snap * 0.42F;
                model.leftArm.pitch = -0.72F - snap * 0.42F;
                model.rightArm.roll = 0.55F;
                model.leftArm.roll = -0.55F;
                model.head.yaw += snap * 0.18F;
            }

            case CHILL_BOUNCE -> {
                model.body.pitch += 0.08F + wave * 0.05F;
                model.rightArm.pitch = -0.72F + wave * 0.24F;
                model.leftArm.pitch = -0.72F - wave * 0.24F;
                model.rightLeg.pitch += fast * 0.16F;
                model.leftLeg.pitch -= fast * 0.16F;
            }

            case SKY_POINT -> {
                model.rightArm.pitch = -2.82F;
                model.rightArm.yaw = -0.18F;
                model.rightArm.roll = 0.12F;
                model.leftArm.pitch = -0.45F + wave * 0.12F;
                model.body.pitch += 0.04F;
                model.head.pitch -= 0.22F;
            }

            case HEART_BEAT -> {
                float pulse = Math.abs(fast);
                model.rightArm.pitch = -1.22F - pulse * 0.18F;
                model.leftArm.pitch = -1.22F - pulse * 0.18F;
                model.rightArm.yaw = -0.48F;
                model.leftArm.yaw = 0.48F;
                model.rightArm.roll = -0.25F;
                model.leftArm.roll = 0.25F;
                model.body.pitch += pulse * 0.07F;
            }
        }

        model.hat.pitch = model.head.pitch;
        model.hat.yaw = model.head.yaw;
        model.hat.roll = model.head.roll;
    }

    private static Emote remoteEmote(PlayerEntityRenderState state) {
        UUID uuid = CosmeticResolver.uuidForState(state.id);
        String emoteId = BackendState.remoteEmote(uuid);
        return emoteId.isBlank() ? null : EmoteManager.byIdOrName(emoteId);
    }



    private static void showHeadOnly(PlayerEntityModel playerModel, BipedEntityModel<?> model) {
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;

        playerModel.jacket.visible = false;
        playerModel.leftSleeve.visible = false;
        playerModel.rightSleeve.visible = false;
        playerModel.leftPants.visible = false;
        playerModel.rightPants.visible = false;
    }

    private static void hideBasePlayer(PlayerEntityModel playerModel, BipedEntityModel<?> model) {
        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;

        playerModel.jacket.visible = false;
        playerModel.leftSleeve.visible = false;
        playerModel.rightSleeve.visible = false;
        playerModel.leftPants.visible = false;
        playerModel.rightPants.visible = false;
    }

    private static void resetVisibility(PlayerEntityModel playerModel, BipedEntityModel<?> model) {
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = true;
        model.rightArm.visible = true;
        model.leftArm.visible = true;
        model.rightLeg.visible = true;
        model.leftLeg.visible = true;

        playerModel.jacket.visible = true;
        playerModel.leftSleeve.visible = true;
        playerModel.rightSleeve.visible = true;
        playerModel.leftPants.visible = true;
        playerModel.rightPants.visible = true;

        model.head.xScale = model.head.yScale = model.head.zScale = 1.0F;
        model.hat.xScale = model.hat.yScale = model.hat.zScale = 1.0F;
        model.body.xScale = model.body.yScale = model.body.zScale = 1.0F;
        model.rightArm.xScale = model.rightArm.yScale = model.rightArm.zScale = 1.0F;
        model.leftArm.xScale = model.leftArm.yScale = model.leftArm.zScale = 1.0F;
        model.rightLeg.xScale = model.rightLeg.yScale = model.rightLeg.zScale = 1.0F;
        model.leftLeg.xScale = model.leftLeg.yScale = model.leftLeg.zScale = 1.0F;
    }
}
