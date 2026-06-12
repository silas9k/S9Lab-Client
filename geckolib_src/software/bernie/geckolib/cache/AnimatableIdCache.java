package software.bernie.geckolib.cache;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_10741;
import net.minecraft.class_18;
import net.minecraft.class_3218;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;

/**
 * Storage class that keeps track of the last animatable id used, and provides new ones on request
 * <p>
 * Generally only used for {@link net.minecraft.class_1792 Items}, but any {@link SingletonAnimatableInstanceCache singleton} will likely use this.
 */
public final class AnimatableIdCache extends class_18 {
	private static final Codec<AnimatableIdCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Codec.LONG.fieldOf("last_id").forGetter(cache -> cache.lastId)
	).apply(builder, AnimatableIdCache::new));
	@SuppressWarnings("DataFlowIssue")
    public static final class_10741<AnimatableIdCache> TYPE = new class_10741<>(GeckoLibConstants.MODID + "_id_cache", AnimatableIdCache::new, CODEC, null);

	private long lastId;

	private AnimatableIdCache() {
		this(0);
	}

	private AnimatableIdCache(long lastId) {
		this.lastId = lastId;
	}

	/**
	 * Get the next free id from the id cache
	 *
	 * @param level An arbitrary ServerLevel. It doesn't matter which one
	 * @return The next free ID, which is immediately reserved for use after calling this method
	 */
	public static long getFreeId(class_3218 level) {
		return getCache(level.method_8503().method_30002()).getNextId();
	}

	private long getNextId() {
		method_80();

		return ++this.lastId;
	}

	private static AnimatableIdCache getCache(class_3218 level) {
		return level.method_8503().method_30002().method_17983().method_17924(TYPE);
	}
}
