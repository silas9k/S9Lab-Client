package site.s9lab.s9labclient.client.cosmetics.preview;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;

/** A non-mutating GUI entity renderer with true 360-degree yaw and explicit preview poses. */
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
        if (entity == null || x2 <= x1 || y2 <= y1) {
            return;
        }
        if (tryOn && cosmetic != null) {
            CosmeticPreviewContext.begin(cosmetic);
        }
        try {
            EntityRenderManager manager = MinecraftClient.getInstance().getEntityRenderDispatcher();
            EntityRenderState state = manager.getAndUpdateRenderState(entity, 1.0F);
            state.light = FULL_BRIGHT;
            state.shadowPieces.clear();
            state.outlineColor = EntityRenderState.NO_OUTLINE;
            state.displayName = null;
            state.nameLabelPos = null;

            if (state instanceof LivingEntityRenderState living) {
                living.bodyYaw = normalizeDegrees(yaw);
                living.relativeHeadYaw = 0.0F;
                living.pitch = pitch;
                if (living.baseScale != 0.0F) {
                    living.width /= living.baseScale;
                    living.height /= living.baseScale;
                }
                living.baseScale = 1.0F;
                applyPose(living, pose == null ? PreviewPose.IDLE : pose);
            }

            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch * 0.35F));
            rotation.mul(pitchRotation);
            Vector3f offset = new Vector3f(0.0F, state.height / 2.0F, 0.0F);
            context.addEntity(state, Math.max(1, scale), offset, rotation, pitchRotation, x1, y1, x2, y2);
        } finally {
            if (tryOn && cosmetic != null) {
                CosmeticPreviewContext.end();
            }
        }
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
