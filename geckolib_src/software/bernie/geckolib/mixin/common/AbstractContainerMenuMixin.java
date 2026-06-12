package software.bernie.geckolib.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.GeckoLibConstants;

/**
 * Injection into the base container functionality to handle ItemStack duplication and splitting with GeckoLib stack identifiers
 */
@Mixin(class_1703.class)
public class AbstractContainerMenuMixin {
    /**
     * Remove the GeckoLib stack ID from a stack when copying it with middle-click
     */
    @WrapOperation(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;", ordinal = 1))
    public class_1799 geckolib$removeGeckolibIdOnCopy(class_1799 instance, int count, Operation<class_1799> original) {
        class_1799 copy = original.call(instance, count);

        if (copy.method_57826(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get()))
            copy.method_57381(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());

        return copy;
    }

    /**
     * In {@code ItemStackMixin#geckolib$skipGeckolibIdOnCompare}, we tell Minecraft to ignore the contents of GeckoLib
     * stack ids for the purposes of ItemStack parity.
     * <p>
     * We temporarily reinstate it here so that the game syncs changes to this specific component
     */
    @WrapOperation(method = "triggerSlotListeners", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean geckolib$allowLazyStackIdParity(class_1799 stack, class_1799 other, Operation<Boolean> original) {
        return original.call(stack, other) && stack.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), Integer.MIN_VALUE).equals(other.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), Integer.MIN_VALUE));
    }
}
