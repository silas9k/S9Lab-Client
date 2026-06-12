package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 239))
    private static int s9labclient$darkRedR(int value) {
        return 7;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 50))
    private static int s9labclient$darkRedG(int value) {
        return 9;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 61))
    private static int s9labclient$darkRedB(int value) {
        return 13;
    }
}
