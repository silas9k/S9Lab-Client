package software.bernie.geckolib.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.class_10515;
import net.minecraft.class_10517;
import net.minecraft.class_2960;
import net.minecraft.class_5699;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.renderer.internal.GeckolibItemSpecialRenderer;

@Mixin(class_10517.class)
public class SpecialModelRenderersMixin {
    @Shadow
    @Final
    private static class_5699.class_10388<class_2960, MapCodec<? extends class_10515.class_10516>> ID_MAPPER;

    /**
     * Inject GeckoLib's custom item model renderer into the vanilla map of special renderers
     */
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void geckolib$addSpecialRenderer(CallbackInfo ci) {
        ID_MAPPER.method_65325(GeckoLibConstants.id("geckolib"), GeckolibItemSpecialRenderer.Unbaked.MAP_CODEC);
    }
}