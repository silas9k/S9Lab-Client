package software.bernie.geckolib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_10455;
import net.minecraft.class_10515;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_638;
import net.minecraft.class_811;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.internal.GeckolibItemSpecialRenderer;

@Mixin(class_10455.class)
public class SpecialModelWrapperMixin {
    /**
     * Expand the data points available for GeckoLib item rendering, since vanilla ignores most of it
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;extractArgument(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Object;"))
    public <T> @Nullable T geckolib$extractAllArguments(class_10515<T> instance, class_1799 itemStack, Operation<T> original,
                                              class_10444 renderState, class_1799 itemStack2, class_10442 modelResolver,
                                              class_811 displayContext, @Nullable class_638 level, @Nullable class_11566 itemOwner, int layerIndex) {
        return instance instanceof GeckolibItemSpecialRenderer geckolibRenderer ?
               (T)geckolibRenderer.extractArgument(itemStack, renderState, displayContext, level, itemOwner) :
               original.call(instance, itemStack);
    }
}
