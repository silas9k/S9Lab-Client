package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.util.S9BadgeText;

@Mixin(DrawContext.class)
public abstract class DrawContextNameEffectsMixin {
    private static final ThreadLocal<Boolean> S9LAB_REPLACING_TEXT = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void s9labclient$replaceKnownNamesWithShadow(
            TextRenderer textRenderer,
            Text text,
            int x,
            int y,
            int color,
            CallbackInfo ci
    ) {
        if (!shouldReplace()) {
            return;
        }
        Text replaced = S9BadgeText.replaceKnownNames(text);
        if (replaced == text) {
            return;
        }
        S9LAB_REPLACING_TEXT.set(true);
        try {
            ((DrawContext) (Object) this).drawTextWithShadow(textRenderer, replaced, x, y, color);
            ci.cancel();
        } finally {
            S9LAB_REPLACING_TEXT.set(false);
        }
    }

    @Inject(
            method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void s9labclient$replaceKnownStringNamesWithShadow(
            TextRenderer textRenderer,
            String text,
            int x,
            int y,
            int color,
            CallbackInfo ci
    ) {
        if (!shouldReplace() || text == null || text.isBlank()) {
            return;
        }
        Text original = Text.literal(text);
        Text replaced = S9BadgeText.replaceKnownNames(original);
        if (replaced == original) {
            return;
        }
        S9LAB_REPLACING_TEXT.set(true);
        try {
            ((DrawContext) (Object) this).drawTextWithShadow(textRenderer, replaced, x, y, color);
            ci.cancel();
        } finally {
            S9LAB_REPLACING_TEXT.set(false);
        }
    }

    @Inject(
            method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void s9labclient$replaceKnownNames(
            TextRenderer textRenderer,
            Text text,
            int x,
            int y,
            int color,
            boolean shadow,
            CallbackInfo ci
    ) {
        if (!shouldReplace()) {
            return;
        }
        Text replaced = S9BadgeText.replaceKnownNames(text);
        if (replaced == text) {
            return;
        }
        S9LAB_REPLACING_TEXT.set(true);
        try {
            ((DrawContext) (Object) this).drawText(textRenderer, replaced, x, y, color, shadow);
            ci.cancel();
        } finally {
            S9LAB_REPLACING_TEXT.set(false);
        }
    }

    @Inject(
            method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void s9labclient$replaceKnownStringNames(
            TextRenderer textRenderer,
            String text,
            int x,
            int y,
            int color,
            boolean shadow,
            CallbackInfo ci
    ) {
        if (!shouldReplace() || text == null || text.isBlank()) {
            return;
        }
        Text original = Text.literal(text);
        Text replaced = S9BadgeText.replaceKnownNames(original);
        if (replaced == original) {
            return;
        }
        S9LAB_REPLACING_TEXT.set(true);
        try {
            ((DrawContext) (Object) this).drawText(textRenderer, replaced, x, y, color, shadow);
            ci.cancel();
        } finally {
            S9LAB_REPLACING_TEXT.set(false);
        }
    }

    private static boolean shouldReplace() {
        if (S9LAB_REPLACING_TEXT.get() || S9LabClientClient.getModuleManager() == null) {
            return false;
        }
        Module module = S9LabClientClient.getModuleManager().getModule("Tablist Badge").orElse(null);
        return module != null && module.isEnabled();
    }
}
