package site.s9lab.s9labclient.mixin.client;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.screenshot.ScreenshotManager;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
    private static final ThreadLocal<Boolean> S9LAB_RENAMING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private static void s9labclient$customScreenshotName(File gameDirectory, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (S9LAB_RENAMING.get()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String fileName = ScreenshotManager.customFileName(client);
        Consumer<Text> wrappedReceiver = ScreenshotManager.wrapScreenshotCallback(client, fileName, messageReceiver);
        S9LAB_RENAMING.set(true);
        try {
            ScreenshotRecorder.saveScreenshot(gameDirectory, fileName, framebuffer, 1, wrappedReceiver);
        } finally {
            S9LAB_RENAMING.set(false);
        }
        ci.cancel();
    }
}
