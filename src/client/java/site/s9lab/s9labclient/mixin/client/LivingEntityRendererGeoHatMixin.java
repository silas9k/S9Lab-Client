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
import site.s9lab.s9labclient.client.cosmetics.hat.HatGeoRenderHook;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererGeoHatMixin {
    @Inject(
            method = "render",
            at = @At("TAIL"),
            require = 0
    )
    private void s9labclient$renderGeoHat(
            @Coerce Object state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            @Coerce Object cameraState,
            CallbackInfo ci
    ) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        HatGeoRenderHook.render(playerState, matrices, queue, cameraState);
    }
}