package software.bernie.geckolib.animatable;

import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.manager.ContextAwareAnimatableManager;
import software.bernie.geckolib.cache.AnimatableIdCache;
import software.bernie.geckolib.constant.DataTickets;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_2520;
import net.minecraft.class_3218;
import net.minecraft.class_811;
import net.minecraft.class_9335;

/**
 * The {@link GeoAnimatable GeoAnimatable} interface specific to {@link net.minecraft.class_1792 Items}
 * <p>
 * This also applies to armor, as they are just items too.
 *
 * @see <a href="https://github.com/bernie-g/geckolib/wiki/Item-Animations">GeckoLib Wiki - Item Animations</a>
 * @see <a href="https://github.com/bernie-g/geckolib/wiki/Armor-Animations">GeckoLib Wiki - Armor Animations</a>
 */
public interface GeoItem extends SingletonGeoAnimatable {
	/**
	 * Register this as a synched {@code GeoAnimatable} instance with GeckoLib's networking functions
	 * <p>
	 * This should be called inside the constructor of your object.
	 */
	static void registerSyncedAnimatable(SingletonGeoAnimatable animatable) {
		SingletonGeoAnimatable.registerSyncedAnimatable(animatable);
	}

	/**
	 * Gets the unique identifying number from this ItemStack's {@link class_2520 NBT},
	 * or {@link Long#MAX_VALUE} if one hasn't been assigned
	 */
	static long getId(class_1799 stack) {
		return Optional.ofNullable(stack.method_57380().method_57845(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get()))
				.filter(Optional::isPresent)
				.<Long>map(Optional::get)
				.orElse((long)stack.hashCode());
	}

	/**
	 * Gets the unique identifying number from this ItemStack's {@link class_2520 NBT}
	 * <p>
	 * If no ID has been reserved for this stack yet, it will reserve a new id and assign it
	 */
	static long getOrAssignId(class_1799 stack, class_3218 level) {
		if (!(stack.method_57353() instanceof class_9335 components))
			return Long.MAX_VALUE;

		Long id = components.method_58694(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());

		if (id == null)
			components.method_57938(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), id = AnimatableIdCache.getFreeId(level));

		return id;
	}

	/**
	 * Whether this item animatable is perspective-aware, handling animations differently depending on the {@link class_811 render perspective}
	 */
	default boolean isPerspectiveAware() {
		return false;
	}

	/**
	 * Replaces the default AnimatableInstanceCache for GeoItems if {@link GeoItem#isPerspectiveAware()} is true, for perspective-dependent handling
	 */
	@Override
	default @Nullable AnimatableInstanceCache animatableCacheOverride() {
		if (isPerspectiveAware())
			return new ContextBasedAnimatableInstanceCache(this);

		return SingletonGeoAnimatable.super.animatableCacheOverride();
	}

	/**
	 * AnimatableInstanceCache specific to GeoItems, for doing render perspective-based animations
	 * <p>
	 * You should <b><u>NOT</u></b> be instantiating this directly unless you know what you are doing.
	 * Use {@link software.bernie.geckolib.util.GeckoLibUtil#createInstanceCache GeckoLibUtil.createInstanceCache} instead
	 */
	class ContextBasedAnimatableInstanceCache extends SingletonAnimatableInstanceCache {
		public ContextBasedAnimatableInstanceCache(GeoAnimatable animatable) {
			super(animatable);
		}

		/**
		 * Gets an {@link AnimatableManager} instance from this cache, cached under the id provided, or a new one if one doesn't already exist
		 * <p>
		 * This subclass assumes that all animatable instances will be sharing this cache instance, and so differentiates data by ids
		 */
		@SuppressWarnings("unchecked")
        @Override
		public AnimatableManager<GeoItem> getManagerForId(long uniqueId) {
			if (!this.managers.containsKey(uniqueId))
				this.managers.put(uniqueId, new ContextAwareAnimatableManager<GeoItem, class_811>(this.animatable) {
					@Override
					protected Map<class_811, AnimatableManager<GeoItem>> buildContextOptions(GeoAnimatable animatable) {
						Map<class_811, AnimatableManager<GeoItem>> map = new EnumMap<>(class_811.class);

						for (class_811 context : class_811.values()) {
							map.put(context, new AnimatableManager<>(animatable));
						}

						return map;
					}

					@Override
					public class_811 getCurrentContext() {
						class_811 context = getAnimatableData(DataTickets.ITEM_RENDER_PERSPECTIVE);

						return context == null ? class_811.field_4315 : context;
					}
				});

			return (AnimatableManager<GeoItem>)this.managers.get(uniqueId);
		}
	}
}
