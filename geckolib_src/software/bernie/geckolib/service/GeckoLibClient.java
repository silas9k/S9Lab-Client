package software.bernie.geckolib.service;

import com.google.common.base.Suppliers;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.function.Supplier;
import net.minecraft.class_10034;
import net.minecraft.class_10186;
import net.minecraft.class_11677;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_5602;
import net.minecraft.class_563;
import net.minecraft.class_572;

/**
 * Loader-agnostic service interface for clientside functionalities
 */
public interface GeckoLibClient {
    Supplier<class_11677<class_572<?>>> HUMANOID_ARMOR_MODEL = Suppliers.memoize(() -> class_11677.method_72961(class_5602.field_61701, class_310.method_1551().method_31974(), class_572::new));
    Supplier<class_563> GENERIC_ELYTRA_MODEL = Suppliers.memoize(() -> new class_563(class_310.method_1551().method_31974().method_32072(class_5602.field_27559)));

    /**
     * Helper method for retrieving an (ideally) cached instance of the armor model for a given Item
     * <p>
     * If no custom model applies to this item, the {@code defaultModel} is returned
     */
    <S extends class_10034 & GeoRenderState> class_3879<?> getArmorModelForItem(S entityRenderState, class_1799 stack, class_1304 slot, class_10186.class_10190 type, class_572<S> defaultModel);

    /**
     * Return the dye value for a given ItemStack, or the default value if not present.
     * <p>
     * This is split off to allow for handling of loader-specific handling for dyed items
     */
    int getDyedItemColor(class_1799 itemStack, int defaultColor);
}
