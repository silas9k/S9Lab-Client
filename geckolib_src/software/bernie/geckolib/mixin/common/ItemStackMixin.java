package software.bernie.geckolib.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1799;
import net.minecraft.class_9335;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Injection into ItemStack functionality to handle duplication and splitting with GeckoLib stack identifiers
 */
@Mixin(class_1799.class)
public class ItemStackMixin {
    /**
     * Remove the GeckoLib stack ID when splitting up a stack into two
     */
    @WrapOperation(method = "split", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"))
    public class_1799 geckolib$removeGeckolibIdOnCopy(class_1799 instance, int count, Operation<class_1799> original) {
        class_1799 copy = original.call(instance, count);

        if (count < instance.method_7947() && copy.method_57826(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get()))
            copy.method_57381(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());

        return copy;
    }

    /**
     * Consider ItemStacks equal if the only difference is their GeckoLib stack ID
     * <p>
     * We do this so that the game doesn't prevent combining stacks due solely to GeckoLib sync IDs.
     */
    @WrapOperation(method = "isSameItemSameComponents", at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    private static boolean geckolib$skipGeckolibIdOnCompare(Object a, Object b, Operation<Boolean> original) {
        if (original.call(a, b))
            return true;

        if (!(a instanceof class_9335 components) || !(b instanceof class_9335 components2))
            return false;

        return GeckoLibUtil.areComponentsMatchingIgnoringGeckoLibId(components, components2);
    }
}
