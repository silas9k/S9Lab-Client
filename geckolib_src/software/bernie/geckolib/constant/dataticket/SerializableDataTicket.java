package software.bernie.geckolib.constant.dataticket;

import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.Pair;
import software.bernie.geckolib.constant.DataTickets;

import java.lang.reflect.Type;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;
import net.minecraft.class_9334;

/**
 * Network-compatible {@link DataTicket} implementation
 * <p>
 * Used for sending data from {@code server -> client} in an easy manner
 *
 * @param <D> Data type for this ticket
 * @deprecated These are not considered safe to use since they do not maintain state tracking and will cause client desync. Use {@link class_2945} or {@link class_9334} instead
 */
@Deprecated(forRemoval = true)
public final class SerializableDataTicket<D> extends DataTicket<D> {
	public static final class_9139<class_9129, SerializableDataTicket<?>> STREAM_CODEC = class_9139.method_56434(
			class_2960.field_48267,
			SerializableDataTicket::getRegisteredId,
            SerializableDataTicket::enforceValidTicket);

	private final class_9139<? super class_9129, D> streamCodec;
	private final class_2960 registeredId;

	private SerializableDataTicket(class_2960 id, Class<? extends D> objectType, Type dataType, class_9139<? super class_9129, D> streamCodec) {
		super(id.toString(), objectType, dataType);

		this.streamCodec = streamCodec;
		this.registeredId = id;
	}

	/**
	 * Create a new network-syncable DataTicket for a given name and object type
	 * <p>
	 * <b><u>MUST</u></b> be created during mod construct
	 * <p>
	 * This DataTicket should then be stored statically somewhere and re-used.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
    public static <D> SerializableDataTicket<D> create(class_2960 id, Class<? extends D> objectType, class_9139<? super class_9129, D> streamCodec) {
		return create(id, objectType, (TypeToken)TypeToken.of(objectType), streamCodec);
	}

	/**
	 * Create a new network-syncable DataTicket for a given name and object type
	 * <p>
	 * <b><u>MUST</u></b> be created during mod construct
	 * <p>
	 * This DataTicket should then be stored statically somewhere and re-used.
	 */
	@SuppressWarnings("unchecked")
    public static <D> SerializableDataTicket<D> create(class_2960 id, Class<? extends D> objectType, TypeToken<D> typeToken, class_9139<? super class_9129, D> streamCodec) {
		return (SerializableDataTicket<D>)IDENTITY_CACHE.computeIfAbsent(Pair.of(objectType, id.toString()), pair ->
				DataTickets.registerSerializable(new SerializableDataTicket<>(id, objectType, typeToken.getType(), streamCodec)));
	}

	/**
	 * Get the registered ID for this ticket
	 */
	public class_2960 getRegisteredId() {
		return this.registeredId;
	}

	/**
	 * @return The {@link class_9139} for the given SerializableDataTicket
	 */
	public class_9139<? super class_9129, D> streamCodec() {
		return this.streamCodec;
	}

	// Pre-defined common types for use

	/**
	 * Generate a new {@code SerializableDataTicket<Double>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Double> ofDouble(class_2960 id) {
		return SerializableDataTicket.create(id, Double.class, class_9135.field_48553);
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Float>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Float> ofFloat(class_2960 id) {
		return SerializableDataTicket.create(id, Float.class, class_9135.field_48552);
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Boolean>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Boolean> ofBoolean(class_2960 id) {
		return SerializableDataTicket.create(id, Boolean.class, class_9135.field_48547);
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Integer>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Integer> ofInt(class_2960 id) {
		return SerializableDataTicket.create(id, Integer.class, class_9135.field_49675);
	}

	/**
	 * Generate a new {@code SerializableDataTicket<String>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<String> ofString(class_2960 id) {
		return SerializableDataTicket.create(id, String.class, class_9135.field_48554);
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Enum>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static <E extends Enum<E>> SerializableDataTicket<E> ofEnum(class_2960 id, Class<E> enumClass) {
		return SerializableDataTicket.create(id, enumClass, new class_9139<>() {
			@Override
			public E decode(class_9129 buf) {
				return Enum.valueOf(enumClass, buf.method_19772());
			}

			@Override
			public void encode(class_9129 buf, E data) {
				buf.method_10814(data.toString());
			}
		});
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Vec3>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<class_243> ofVec3(class_2960 id) {
		return SerializableDataTicket.create(id, class_243.class, class_9135.field_48558.method_56432(class_243::new, class_243::method_46409));
	}

	/**
	 * Generate a new {@code SerializableDataTicket<BlockPos>} for the given id
	 *
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<class_2338> ofBlockPos(class_2960 id) {
		return SerializableDataTicket.create(id, class_2338.class, new class_9139<>() {
			@Override
			public class_2338 decode(class_9129 buf) {
				return buf.method_10811();
			}

			@Override
			public void encode(class_9129 buf, class_2338 blockPos) {
				buf.method_10807(blockPos);
			}
		});
	}

	/**
	 * Retrieve a SerializableDataTicket by its registered ID, throwing an exception if not found
	 */
	public static SerializableDataTicket<?> enforceValidTicket(class_2960 name) throws IllegalStateException {
		final SerializableDataTicket<?> ticket = DataTickets.byName(name);

		if (ticket == null)
			throw new IllegalStateException("Attempted to retrieve a SerializableDataTicket that does not exist! Likely didn't register the ticket properly: " + name);

		return ticket;
	}
}
