package software.bernie.geckolib.constant;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.state.ControllerState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.constant.dataticket.OverridingDataTicket;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_4050;
import net.minecraft.class_572;
import net.minecraft.class_811;

/**
 * Stores the default (builtin) {@link DataTicket DataTickets} used in GeckoLib
 * <p>
 * Additionally handles registration of {@link SerializableDataTicket SerializableDataTickets}
 * <p>
 * This class should only be used on the client side
 */
public final class DataTickets {
	private static final Map<String, SerializableDataTicket<?>> SERIALIZABLE_TICKETS = new ConcurrentHashMap<>();

	// TODO 26.1 - drop class and properly generify the constants
	// Builtin tickets
	// These tickets are used by GeckoLib by default, usually added in by the GeoRenderer for use in animations
	public static final DataTicket<Class> ANIMATABLE_CLASS = DataTicket.create("animatable_class", Class.class, new TypeToken<>() {});
	public static final DataTicket<Float> PARTIAL_TICK = DataTicket.create("partial_tick", Float.class, new TypeToken<>() {});
	public static final DataTicket<Integer> RENDER_COLOR = DataTicket.create("render_color", Integer.class, new TypeToken<>() {});
	public static final DataTicket<Long> ANIMATABLE_INSTANCE_ID = DataTicket.create("animatable_instance_id", Long.class, new TypeToken<>() {});
	public static final DataTicket<Integer> PACKED_OVERLAY = DataTicket.create("packed_overlay", Integer.class, new TypeToken<>() {});
	public static final DataTicket<Integer> PACKED_LIGHT = DataTicket.create("packed_light", Integer.class, new TypeToken<>() {});
	public static final DataTicket<Integer> GLOW_COLOUR = DataTicket.create("glow_colour", Integer.class, new TypeToken<>() {});
	public static final DataTicket<class_243> VELOCITY = DataTicket.create("velocity", class_243.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_MOVING = DataTicket.create("is_moving", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<class_2680> BLOCKSTATE = DataTicket.create("blockstate", class_2680.class, new TypeToken<>() {});
	public static final DataTicket<class_243> POSITION = DataTicket.create("position", class_243.class, new TypeToken<>() {});
	public static final DataTicket<class_2338> BLOCKPOS = DataTicket.create("blockpos", class_2338.class, new TypeToken<>() {});
	public static final DataTicket<class_2350> BLOCK_FACING = DataTicket.create("block_facing", class_2350.class, new TypeToken<>() {});
	public static final DataTicket<class_811> ITEM_RENDER_PERSPECTIVE = DataTicket.create("item_render_perspective", class_811.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> HAS_GLINT = DataTicket.create("has_glint", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<class_1792> ITEM = DataTicket.create("item", class_1792.class, new TypeToken<>() {});
	public static final DataTicket<AnimatableManager> ANIMATABLE_MANAGER = DataTicket.create("animatable_manager", AnimatableManager.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> SWINGING_ARM = DataTicket.create("swinging_arm", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> SPRINTING = DataTicket.create("sprinting", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_DEAD_OR_DYING = DataTicket.create("is_dead_or_dying", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_ENCHANTED = DataTicket.create("is_enchanted", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_STACKABLE = DataTicket.create("is_stackable", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Integer> MAX_USE_DURATION = DataTicket.create("max_use_duration", Integer.class, new TypeToken<>() {});
	public static final DataTicket<Integer> MAX_DURABILITY = DataTicket.create("max_durability", Integer.class, new TypeToken<>() {});
	public static final DataTicket<Integer> REMAINING_DURABILITY = DataTicket.create("remaining_durability", Integer.class, new TypeToken<>() {});
	public static final DataTicket<class_1304> EQUIPMENT_SLOT = DataTicket.create("equipment_slot", class_1304.class, new TypeToken<>() {});
	public static final DataTicket<class_572> HUMANOID_MODEL = DataTicket.create("humanoid_model", class_572.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_GECKOLIB_WEARER = DataTicket.create("is_geckolib_wearer", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<EnumMap> EQUIPMENT_BY_SLOT = DataTicket.create("equipment_by_slot", EnumMap.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_LEFT_HANDED = DataTicket.create("is_left_handed", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<Boolean> IS_CROUCHING = DataTicket.create("is_crouching", Boolean.class, new TypeToken<>() {});
	public static final DataTicket<class_243> ELYTRA_ROTATION = DataTicket.create("elytra_rotation", class_243.class, new TypeToken<>() {});

	// Default-value overriding DataTickets
	public static final OverridingDataTicket<Double, class_10017> TICK = OverridingDataTicket.create("tick", Double.class, new TypeToken<>() {}, class_10017.class, state -> (double)state.field_53328);
	public static final OverridingDataTicket<Boolean, class_10042> INVISIBLE_TO_PLAYER = OverridingDataTicket.create("invisible_to_player", Boolean.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53461);
	public static final OverridingDataTicket<Boolean, class_10042> IS_SHAKING = OverridingDataTicket.create("is_shaking", Boolean.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53456);
	public static final OverridingDataTicket<class_4050, class_10042> ENTITY_POSE = OverridingDataTicket.create("entity_pose", class_4050.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53465);
	public static final OverridingDataTicket<Float, class_10042> ENTITY_PITCH = OverridingDataTicket.create("entity_pitch", Float.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53448);
	public static final OverridingDataTicket<Float, class_10042> ENTITY_YAW = OverridingDataTicket.create("entity_yaw", Float.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53447);
	public static final OverridingDataTicket<Float, class_10042> ENTITY_BODY_YAW = OverridingDataTicket.create("entity_body_yaw", Float.class, new TypeToken<>() {}, class_10042.class, state -> state.field_53446);

	@ApiStatus.Internal
	public static final DataTicket<EnumMap> PER_SLOT_RENDER_DATA = DataTicket.create("per_slot_render_data", EnumMap.class, new TypeToken<>() {});
	@ApiStatus.Internal
	public static final DataTicket<ControllerState[]> ANIMATION_CONTROLLER_STATES = DataTicket.create("animation_controller_states", ControllerState[].class, new TypeToken<>() {});

	// Builtin serializable tickets
	// These are not used anywhere by default, but are provided as examples and for ease of use
	public static final SerializableDataTicket<Integer> ANIM_STATE = SerializableDataTicket.ofInt(GeckoLibConstants.id("anim_state"));
	public static final SerializableDataTicket<String> ANIM = SerializableDataTicket.ofString(GeckoLibConstants.id("anim"));
	public static final SerializableDataTicket<Integer> USE_TICKS = SerializableDataTicket.ofInt(GeckoLibConstants.id("use_ticks"));
	public static final SerializableDataTicket<Boolean> ACTIVE = SerializableDataTicket.ofBoolean(GeckoLibConstants.id("active"));
	public static final SerializableDataTicket<Boolean> OPEN = SerializableDataTicket.ofBoolean(GeckoLibConstants.id("open"));
	public static final SerializableDataTicket<Boolean> CLOSED = SerializableDataTicket.ofBoolean(GeckoLibConstants.id("closed"));
	public static final SerializableDataTicket<class_2350> DIRECTION = SerializableDataTicket.ofEnum(GeckoLibConstants.id("direction"), class_2350.class);

	@SuppressWarnings("DataFlowIssue")
    public static @Nullable SerializableDataTicket<?> byName(class_2960 id) {
		return SERIALIZABLE_TICKETS.getOrDefault(id.toString(), null);
	}

	/**
	 * Internal only. You should NOT be using this
	 */
	@ApiStatus.Internal
	public static <D> SerializableDataTicket<D> registerSerializable(SerializableDataTicket<D> ticket) {
		SerializableDataTicket<?> existingTicket = SERIALIZABLE_TICKETS.putIfAbsent(ticket.id(), ticket);

		if (existingTicket != null)
            GeckoLibConstants.LOGGER.error("Duplicate SerializableDataTicket registered! This will cause issues. Existing: {}, New: {}", existingTicket.id(), ticket.id());

		return ticket;
	}
}
