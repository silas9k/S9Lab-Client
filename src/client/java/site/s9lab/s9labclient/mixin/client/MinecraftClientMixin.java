package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void s9labclient$getWindowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("S9Lab Client 1.21.11 (BETA) | Minecraft 1.21.11");
    }
}
