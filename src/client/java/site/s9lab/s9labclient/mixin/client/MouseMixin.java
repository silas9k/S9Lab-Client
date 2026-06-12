package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.zoom.ZoomController;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void s9labclient$adjustZoomWithScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomController.handleMouseScroll(vertical)) {
            ci.cancel();
        }
    }
}
