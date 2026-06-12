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
import site.s9lab.s9labclient.client.cosmetics.halo.HaloGeoRenderHook;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererGeoHaloMixin {
    /*
     * GeckoLib Halo Hook.
     *
     * In deinem Mapping heißt die Render-Methode NICHT method_3936,
     * sondern einfach render.
     *
     * cameraState bleibt Object + @Coerce, weil dein Mapping dafür keinen
     * sauber importierbaren Java-Namen hat.
     */
    @Inject(
            method = "render",
            at = @At("TAIL"),
            require = 0
    )
    private void s9labclient$renderGeoHalo(
            @Coerce Object state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            @Coerce Object cameraState,
            CallbackInfo ci
    ) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        HaloGeoRenderHook.render(playerState, matrices, queue, cameraState);
    }
}