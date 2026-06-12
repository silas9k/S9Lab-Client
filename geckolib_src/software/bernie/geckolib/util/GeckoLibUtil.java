package software.bernie.geckolib.util;

import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.object.EasingType;
import software.bernie.geckolib.animation.object.LoopType;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.loading.object.BakedModelFactory;

import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_2586;
import net.minecraft.class_9331;
import net.minecraft.class_9335;

/**
 * Helper class for various GeckoLib-specific functions.
 */
public final class GeckoLibUtil {
	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object
	 *
	 * @param animatable The animatable object
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
		AnimatableInstanceCache cache = animatable.animatableCacheOverride();

		return cache != null ? cache : createInstanceCache(animatable, !(animatable instanceof class_1297) && !(animatable instanceof class_2586));
	}

	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object
	 * <p>
	 * Recommended to use {@link GeckoLibUtil#createInstanceCache(GeoAnimatable)} unless you know what you're doing
	 *
	 * @param animatable The animatable object
	 * @param singletonObject Whether the object is a singleton/flyweight object, and uses ints to differentiate animatable instances
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean singletonObject) {
		AnimatableInstanceCache cache = animatable.animatableCacheOverride();

		if (cache != null)
			return cache;

		return singletonObject ? new SingletonAnimatableInstanceCache(animatable) : new InstancedAnimatableInstanceCache(animatable);
	}

	/**
	 * Register a custom {@link LoopType} with GeckoLib, allowing for dynamic handling of post-animation looping
	 * <p>
	 * <b><u>MUST be called during mod construct</u></b>
	 *
	 * @param name The name of the {@code LoopType} handler
	 * @param loopType The {@code LoopType} implementation to use for the given name
	 */
	synchronized public static LoopType addCustomLoopType(String name, LoopType loopType) {
		return LoopType.register(name, loopType);
	}

	/**
	 * Register a custom {@link EasingType} with GeckoLib allowing for dynamic handling of animation transitions and curves
	 * <p>
	 * <b><u>MUST be called during mod construct</u></b>
	 *
	 * @param name The name of the {@code EasingType} handler
	 * @param easingType The {@code EasingType} implementation to use for the given name
	 */
	synchronized public static EasingType addCustomEasingType(String name, EasingType easingType) {
		return EasingType.register(name, easingType);
	}

	/**
	 * Register a custom {@link BakedModelFactory} with GeckoLib, allowing for dynamic handling of geo model loading
	 * <p>
	 * <b><u>MUST be called during mod construct</u></b>
	 *
	 * @param namespace The namespace (modid) to register the factory for
	 * @param factory The factory responsible for model loading under the given namespace
	 */
	synchronized public static void addCustomBakedModelFactory(String namespace, BakedModelFactory factory) {
		BakedModelFactory.register(namespace, factory);
	}

	/**
	 * Register a custom {@link SerializableDataTicket} with GeckoLib for handling custom data transmission
	 * <p>
	 * NOTE: You do not need to register non-serializable {@link DataTicket DataTickets}.
	 * <p>
	 * <b><u>MUST be called during mod construct</u></b>
	 *
	 * @param dataTicket The SerializableDataTicket to register
	 * @return The dataTicket you passed in
	 */
	synchronized public static <D> SerializableDataTicket<D> addDataTicket(SerializableDataTicket<D> dataTicket) {
		return DataTickets.registerSerializable(dataTicket);
	}

	/**
	 * Perform an {@link Object#equals(Object)} check on two {@link class_9335}s,
	 * ignoring any GeckoLib stack ids that may be present.
	 * <p>
	 * This is typically only called by an internal mixin
	 */
	@ApiStatus.Internal
	public static boolean areComponentsMatchingIgnoringGeckoLibId(class_9335 map1, class_9335 map2) {
		final class_9331<Long> stackId = GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get();
		boolean patched = false;

		if (map1.method_57832(stackId)) {
			class_9335 prevMap = map1;
			boolean copyOnWrite = prevMap.field_49656;
			(map1 = map1.method_57941()).method_57939(stackId);
			map1.field_49656 = copyOnWrite;
			patched = true;
		}

		if (map2.method_57832(stackId)) {
			class_9335 prevMap = map2;
			boolean copyOnWrite = prevMap.field_49656;
			(map2 = map2.method_57941()).method_57939(stackId);
			map2.field_49656 = copyOnWrite;
			patched = true;
		}

		return patched && Objects.equals(map1, map2);
	}

    private GeckoLibUtil() {}
}
