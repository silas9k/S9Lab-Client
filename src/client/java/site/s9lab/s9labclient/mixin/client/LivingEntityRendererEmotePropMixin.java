package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.emote.prop.EmotePropRenderHook;
import site.s9lab.s9labclient.client.emote.render.EmotePlayerRenderHook;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererEmotePropMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            require = 0
    )
    private void s9labclient$renderEmotePlayer(
            @Coerce Object state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            @Coerce Object cameraState,
            CallbackInfo ci
    ) {
        if (state instanceof PlayerEntityRenderState playerState) {
            EmotePlayerRenderHook.render(playerState, matrices, queue, cameraState, playerState.light);
            EmotePropRenderHook.render(playerState, matrices, queue, cameraState, 15728880);
        }
    }
}
