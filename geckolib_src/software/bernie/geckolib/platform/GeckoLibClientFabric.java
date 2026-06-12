package software.bernie.geckolib.platform;

import net.minecraft.class_10034;
import net.minecraft.class_10186;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_3489;
import net.minecraft.class_3879;
import net.minecraft.class_572;
import net.minecraft.class_9282;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.service.GeckoLibClient;

/**
 * Fabric service implementation for clientside functionalities
 */
public class GeckoLibClientFabric implements GeckoLibClient {
    /**
     * Helper method for retrieving an (ideally) cached instance of the armor model for a given Item
     * <p>
     * If no custom model applies to this item, the {@code defaultModel} is returned
     */
    @Override
    public <S extends class_10034 & GeoRenderState> class_3879<?> getArmorModelForItem(S renderState, class_1799 stack, class_1304 slot, class_10186.class_10190 type, class_572<S> defaultModel) {
        return defaultModel;
    }

    /**
     * Return the dye value for a given ItemStack, or the defaul value if not present.
     * <p>
     * This is split off to allow for handling of loader-specific handling for dyed items
     */
    @Override
    public int getDyedItemColor(class_1799 itemStack, int defaultColor) {
        return itemStack.method_31573(class_3489.field_48803) ? class_9282.method_57470(itemStack, defaultColor) : defaultColor;
    }
}
