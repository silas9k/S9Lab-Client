package site.s9lab.s9labclient.client.cosmetics.preview;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext.BaseVisibility;

/**
 * Non-mutating GUI player renderer.
 *
 * Every preview uses a fresh PlayerEntityRenderState and a stable negative id.
 * This prevents queued card renders from overwriting each other and makes the
 * preview cosmetic independent from ownership/equipped backend state.
 */
public final class CosmeticPreviewRenderer {
    private static final int FULL_BRIGHT = 0x00F000F0;

    private CosmeticPreviewRenderer() {
    }

    public static void draw(
            DrawContext context,
            LivingEntity entity,
            Cosmetic cosmetic,
            boolean tryOn,
            PreviewPose pose,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yaw,
            float pitch
    ) {
        int previewKey = CosmeticPreviewContext.stableKey(
                "preview:" + x1 + ':' + y1 + ':' + x2 + ':' + y2,
                cosmetic
        );
        draw(context, entity, cosmetic, tryOn, pose, x1, y1, x2, y2, scale, yaw, pitch,
                previewKey, BaseVisibility.FULL);
    }

    /** Compatibility overload for older call sites. */
    public static void draw(
            DrawContext context,
            LivingEntity entity,
            Cosmetic cosmetic,
            boolean tryOn,
            PreviewPose pose,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yaw,
            float pitch,
            boolean hideBasePlayer
    ) {
        int previewKey = CosmeticPreviewContext.stableKey(
                "preview:" + x1 + ':' + y1 + ':' + x2 + ':' + y2,
                cosmetic
        );
        draw(context, entity, cosmetic, tryOn, pose, x1, y1, x2, y2, scale, yaw, pitch,
                previewKey, hideBasePlayer ? BaseVisibility.HIDDEN : BaseVisibility.FULL);
    }

    public static void draw(
            DrawContext context,
            LivingEntity entity,
            Cosmetic cosmetic,
            boolean tryOn,
            PreviewPose pose,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yaw,
            float pitch,
            int previewKey,
            BaseVisibility visibility
    ) {
        if (!(entity instanceof AbstractClientPlayerEntity player) || x2 <= x1 || y2 <= y1) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderManager manager = client.getEntityRenderDispatcher();
        PlayerEntityRenderer<AbstractClientPlayerEntity> renderer = manager.getPlayerRenderer(player);
        PlayerEntityRenderState state = renderer.createRenderState();

        if (tryOn && cosmetic != null) {
            CosmeticPreviewContext.begin(cosmetic);
        }
        try {
            renderer.updateRenderState(player, state, 1.0F);
        } finally {
            if (tryOn && cosmetic != null) {
                CosmeticPreviewContext.end();
            }
        }

        if (tryOn && cosmetic != null) {
            state.id = previewKey;
            CosmeticPreviewContext.bindState(previewKey, cosmetic, visibility);
            // Preview states are isolated from every real equipped cosmetic.
            // Preview capes are rendered by the dedicated CapeRenderer feature,
            // so the vanilla SkinTextures cape is always removed to avoid
            // duplicate or skipped vanilla cape passes.
            removeCape(state);
        }

        // Keep the preview mannequin clean and deterministic.
        state.equippedHeadStack = ItemStack.EMPTY;
        state.equippedChestStack = ItemStack.EMPTY;
        state.equippedLegsStack = ItemStack.EMPTY;
        state.equippedFeetStack = ItemStack.EMPTY;
        state.rightHandItem = ItemStack.EMPTY;
        state.leftHandItem = ItemStack.EMPTY;
        state.rightHandItemState.clear();
        state.leftHandItemState.clear();
        state.spyglassState.clear();
        state.leftShoulderParrotVariant = null;
        state.rightShoulderParrotVariant = null;
        state.stuckArrowCount = 0;
        state.stingerCount = 0;
        state.extraEars = false;

        state.light = FULL_BRIGHT;
        state.shadowPieces.clear();
        state.outlineColor = EntityRenderState.NO_OUTLINE;
        state.displayName = null;
        state.nameLabelPos = null;
        state.invisible = false;

        state.bodyYaw = normalizeDegrees(yaw);
        state.relativeHeadYaw = 0.0F;
        state.pitch = pitch;
        if (state.baseScale != 0.0F) {
            state.width /= state.baseScale;
            state.height /= state.baseScale;
        }
        state.baseScale = 1.0F;
        applyPose(state, pose == null ? PreviewPose.IDLE : pose);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch * 0.35F));
        rotation.mul(pitchRotation);

        float focusY = visibility == BaseVisibility.HEAD_ONLY
                ? Math.max(0.1F, state.height - 0.28F)
                : state.height / 2.0F;
        Vector3f offset = new Vector3f(0.0F, focusY, 0.0F);

        context.addEntity(
                state,
                Math.max(1, scale),
                offset,
                rotation,
                pitchRotation,
                x1,
                y1,
                x2,
                y2
        );
    }


    private static void removeCape(PlayerEntityRenderState state) {
        SkinTextures original = state.skinTextures;
        if (original != null && original.cape() != null) {
            state.skinTextures = new SkinTextures(
                    original.body(),
                    null,
                    original.elytra(),
                    original.model(),
                    original.secure()
            );
        }
        state.capeVisible = false;
    }

    private static void applyPose(LivingEntityRenderState state, PreviewPose pose) {
        state.sneaking = false;
        state.pose = EntityPose.STANDING;
        state.limbSwingAnimationProgress = 0.0F;
        state.limbSwingAmplitude = 0.0F;
        if (state instanceof BipedEntityRenderState biped) {
            biped.isInSneakingPose = false;
            biped.isGliding = false;
            biped.isSwimming = false;
            biped.leaningPitch = 0.0F;
            biped.hasVehicle = false;
        }

        switch (pose) {
            case WALK -> {
                state.limbSwingAnimationProgress = (System.currentTimeMillis() % 2400L) / 110.0F;
                state.limbSwingAmplitude = 0.72F;
            }
            case CROUCH -> {
                state.sneaking = true;
                state.pose = EntityPose.CROUCHING;
                if (state instanceof BipedEntityRenderState biped) {
                    biped.isInSneakingPose = true;
                }
            }
            case IDLE -> {
            }
        }
    }

    private static float normalizeDegrees(float value) {
        float normalized = value % 360.0F;
        return normalized < 0.0F ? normalized + 360.0F : normalized;
    }
}
