package software.bernie.geckolib.loading.math;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1407;
import net.minecraft.class_1409;
import net.minecraft.class_1410;
import net.minecraft.class_1412;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1937;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2874;
import net.minecraft.class_3532;
import net.minecraft.class_5354;
import net.minecraft.class_5766;
import net.minecraft.class_6025;
import net.minecraft.class_7094;
import net.minecraft.class_9460;
import net.minecraft.class_9817;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.*;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.state.ControllerState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.value.Variable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.util.ClientUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/**
 * Helper class for the builtin <a href="https://learn.microsoft.com/en-us/minecraft/creator/reference/content/molangreference/examples/molangconcepts/molangintroduction?view=minecraft-bedrock-stable">Molang</a> query string constants for the {@link MathParser}.
 * <p>
 * These do not constitute a definitive list of queries; merely the default ones
 * <p>
 * Note that the implementations of the various queries in GeckoLib may not necessarily match its implementation in Bedrock
 */
public final class MolangQueries {
	public static final String ACTOR_COUNT = "query.actor_count";
	public static final String ANIM_TIME = "query.anim_time";
	public static final String BLOCK_STATE = "query.block_state";
	public static final String BLOCKING = "query.blocking";
	public static final String BODY_X_ROTATION = "query.body_x_rotation";
	public static final String BODY_Y_ROTATION = "query.body_y_rotation";
	public static final String CAN_CLIMB = "query.can_climb";
	public static final String CAN_FLY = "query.can_fly";
	public static final String CAN_SWIM = "query.can_swim";
	public static final String CAN_WALK = "query.can_walk";
	public static final String CARDINAL_FACING = "query.cardinal_facing";
	public static final String CARDINAL_FACING_2D = "query.cardinal_facing_2d";
	public static final String CARDINAL_PLAYER_FACING = "query.cardinal_player_facing";
	public static final String CONTROLLER_SPEED = "query.controller_speed";
	public static final String DAY = "query.day";
	public static final String DEATH_TICKS = "query.death_ticks";
	public static final String DISTANCE_FROM_CAMERA = "query.distance_from_camera";
	public static final String EQUIPMENT_COUNT = "query.equipment_count";
	public static final String FRAME_ALPHA = "query.frame_alpha";
	public static final String GET_ACTOR_INFO_ID = "query.get_actor_info_id";
	public static final String GROUND_SPEED = "query.ground_speed";
	public static final String HAS_CAPE = "query.has_cape";
	public static final String HAS_COLLISION = "query.has_collision";
	public static final String HAS_GRAVITY = "query.has_gravity";
	public static final String HAS_HEAD_GEAR = "query.has_head_gear";
	public static final String HAS_OWNER = "query.has_owner";
	public static final String HAS_PLAYER_RIDER = "query.has_player_rider";
	public static final String HAS_RIDER = "query.has_rider";
	public static final String HEAD_X_ROTATION = "query.head_x_rotation";
	public static final String HEAD_Y_ROTATION = "query.head_y_rotation";
	public static final String HEALTH = "query.health";
	public static final String HURT_TIME = "query.hurt_time";
	public static final String INVULNERABLE_TICKS = "query.invulnerable_ticks";
	public static final String IS_ALIVE = "query.is_alive";
	public static final String IS_ANGRY = "query.is_angry";
	public static final String IS_BABY = "query.is_baby";
	public static final String IS_BREATHING = "query.is_breathing";
	public static final String IS_ENCHANTED = "query.is_enchanted";
	public static final String IS_FIRE_IMMUNE = "query.is_fire_immune";
	public static final String IS_FIRST_PERSON = "query.is_first_person";
	public static final String IS_IN_CONTACT_WITH_WATER = "query.is_in_contact_with_water";
	public static final String IS_IN_LAVA = "query.is_in_lava";
	public static final String IS_IN_WATER = "query.is_in_water";
	public static final String IS_IN_WATER_OR_RAIN = "query.is_in_water_or_rain";
	public static final String IS_INVISIBLE = "query.is_invisible";
	public static final String IS_LEASHED = "query.is_leashed";
	public static final String IS_MOVING = "query.is_moving";
	public static final String IS_ON_FIRE = "query.is_on_fire";
	public static final String IS_ON_GROUND = "query.is_on_ground";
	public static final String IS_RIDING = "query.is_riding";
	public static final String IS_SADDLED = "query.is_saddled";
	public static final String IS_SILENT = "query.is_silent";
	public static final String IS_SLEEPING = "query.is_sleeping";
	public static final String IS_SNEAKING = "query.is_sneaking";
	public static final String IS_SPRINTING = "query.is_sprinting";
	public static final String IS_STACKABLE = "query.is_stackable";
	public static final String IS_SWIMMING = "query.is_swimming";
	public static final String IS_USING_ITEM = "query.is_using_item";
	public static final String IS_WALL_CLIMBING = "query.is_wall_climbing";
	public static final String ITEM_MAX_USE_DURATION = "query.item_max_use_duration";
	public static final String LIFE_TIME = "query.life_time";
	public static final String LIMB_SWING = "query.limb_swing";
	public static final String LIMB_SWING_AMOUNT = "query.limb_swing_amount";
	public static final String MAIN_HAND_ITEM_MAX_DURATION = "query.main_hand_item_max_duration";
	public static final String MAIN_HAND_ITEM_USE_DURATION = "query.main_hand_item_use_duration";
	public static final String MAX_DURABILITY = "query.max_durability";
	public static final String MAX_HEALTH = "query.max_health";
	public static final String MOON_BRIGHTNESS = "query.moon_brightness";
	public static final String MOON_PHASE = "query.moon_phase";
	public static final String MOVEMENT_DIRECTION = "query.movement_direction";
	public static final String PLAYER_LEVEL = "query.player_level";
	public static final String REMAINING_DURABILITY = "query.remaining_durability";
	public static final String RIDER_BODY_X_ROTATION = "query.rider_body_x_rotation";
	public static final String RIDER_BODY_Y_ROTATION = "query.rider_body_y_rotation";
	public static final String RIDER_HEAD_X_ROTATION = "query.rider_head_x_rotation";
	public static final String RIDER_HEAD_Y_ROTATION = "query.rider_head_y_rotation";
	public static final String SCALE = "query.scale";
	public static final String SLEEP_ROTATION = "query.sleep_rotation";
	public static final String TIME_OF_DAY = "query.time_of_day";
	public static final String TIME_STAMP = "query.time_stamp";
	public static final String VERTICAL_SPEED = "query.vertical_speed";
	public static final String YAW_SPEED = "query.yaw_speed";

	private static final Map<String, Variable> VARIABLES = new Object2ObjectOpenHashMap<>();
	private static final Map<Variable, ToDoubleFunction<Actor<? extends GeoAnimatable>>> ACTOR_VARIABLES = new Reference2ObjectOpenHashMap<>();

	static {
		setDefaultQueryValues();
	}

	/**
	 * Returns whether a variable under the given identifier has already been registered, without creating a new instance
	 */
	public static boolean isExistingVariable(String name) {
		return VARIABLES.containsKey(name);
	}

	/**
	 * Register a new {@link Variable} with the math parsing system
	 * <p>
	 * Technically supports overriding by matching keys, though you should try to update the existing variable instances instead if possible
	 *
	 * @see MathParser#registerVariable(Variable)
	 */
	static void registerVariable(Variable variable) {
		VARIABLES.put(variable.name(), variable);
	}

	/**
	 * @return The registered {@link Variable} instance for the given name
	 *
	 * @see MathParser#getVariableFor(String)
	 */
	static Variable getVariableFor(String name) {
		return VARIABLES.computeIfAbsent(applyPrefixAliases(name, "query.", "q."), key -> new Variable(key, 0));
	}

	/**
	 * Parse a given string formatted with a prefix, swapping out any potential aliases for the defined proper name
	 *
	 * @param text The base text to parse
	 * @param properName The "correct" prefix to apply
	 * @param aliases The available prefixes to check and replace
	 * @return The unaliased string, or the original string if no aliases match
	 */
	private static String applyPrefixAliases(String text, String properName, String... aliases) {
		for (String alias : aliases) {
			if (text.startsWith(alias))
				return properName + text.substring(alias.length());
		}

		return text;
	}

	/**
	 * Set a Molang variable that operates on data relevant to the {@link GeoAnimatable} or associated variables at the time of rendering.
	 * <p>
	 * Because of the state-based nature of the render pipeline, this has to be handled slightly differently
	 * to standard {@link Variable}s
	 * <p>
	 * You should only be doing this once, at mod construct
	 *
	 * @param <T> The animatable type your variable operates on
	 * @param valueFunction The function that generates the variable value based on the animatable and render state
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void setActorVariable(String name, ToDoubleFunction<Actor<T>> valueFunction) {
		Variable variable = getVariableFor(name);

		ACTOR_VARIABLES.put(variable, (ToDoubleFunction)valueFunction);
        variable.set(state -> state.getQueryValue(variable));
	}

	/**
	 * Set a Molang variable to a given value function based on an {@link class_7094}
	 * <p>
	 * Note that {@link #setActorVariable(String, ToDoubleFunction) actor variables} cannot be overridden here
	 *
	 * @param valueFunction The value function to set the variable to
	 */
	public static <T extends GeoAnimatable> void setVariableFunction(String name, ToDoubleFunction<ControllerState> valueFunction) {
		Variable variable = getVariableFor(name);

		if (ACTOR_VARIABLES.containsKey(variable))
			throw new IllegalArgumentException("Cannot replace actor variables");

		variable.set(valueFunction);
	}

	/**
	 * Set a Molang variable to a given value
	 * <p>
	 * Note that {@link #setActorVariable(String, ToDoubleFunction) actor variables} cannot be overridden here
	 *
	 * @param value The value to set the variable to
	 */
	public static void setVariableValue(String name, double value) {
		Variable variable = getVariableFor(name);

		if (ACTOR_VARIABLES.containsKey(variable))
			throw new IllegalArgumentException("Cannot replace actor variables");

		variable.set(value);
	}

	/**
	 * Compute and cache the provided variables into the provided value map, to be passed into a following render pass
	 *
	 * @param actor The actor instance for this render pass
	 * @param variables The list of variables to compute values for
	 * @param valueMap The map to store the computed values into
	 * @param <T> The lowest-common type of object your actor needs to be in order to evaluate this variable
	 */
	public static <T extends GeoAnimatable> void buildActorVariables(Actor<T> actor, Set<Variable> variables, Reference2DoubleMap<Variable> valueMap) {
		for (Variable variable : variables) {
            ToDoubleFunction<Actor<? extends GeoAnimatable>> function = ACTOR_VARIABLES.get(variable);

            if (function != null && !valueMap.containsKey(variable))
                valueMap.put(variable, function.applyAsDouble(actor));
		}
	}

	/**
	 * Holder object representing an animatable about to be rendered, along with some associated helper objects.<br>
	 * Used in {@link #setActorVariable(String, ToDoubleFunction) actor variables} for pre-computing variable values
 	 *
	 * @param animatable The animatable instance being prepared for render
	 * @param renderState The {@link GeoRenderState} being built for the render pass
	 * @param controller The {@link AnimationController} relevant to the actor at the time this actor is being used
	 * @param renderTime The amount of time (in ticks) this animatable has existed since the first time it rendered
	 * @param partialTick The fraction of a tick that has passed as of the upcoming render frame
	 * @param level The client's level
	 * @param clientPlayer The client player
	 * @param cameraPos The position of the client player's camera
	 */
	public record Actor<T>(T animatable, GeoRenderState renderState, AnimationController<?> controller, double renderTime, float partialTick, class_1937 level, class_1657 clientPlayer, class_243 cameraPos) {}

	private static void setDefaultQueryValues() {
		setVariableValue("PI", Math.PI);
		setVariableValue("E", Math.E);

		setActorVariable(ACTOR_COUNT, actor -> ClientUtil.getVisibleEntityCount());
		setActorVariable(ANIM_TIME, actor -> actor.controller.getCurrentAnimationTime());
		setActorVariable(CONTROLLER_SPEED, actor -> actor.controller.getAnimationSpeed());
		setActorVariable(CARDINAL_PLAYER_FACING, actor -> actor.clientPlayer.method_5735().ordinal());
		setActorVariable(DAY, actor -> actor.level.method_75260() / 24000d);
		setActorVariable(FRAME_ALPHA, actor -> actor.partialTick);
		setActorVariable(HAS_CAPE, actor -> ClientUtil.clientPlayerHasCape() ? 1 : 0);
		setActorVariable(IS_FIRST_PERSON, actor -> ClientUtil.isFirstPerson() ? 1 : 0);
		setActorVariable(LIFE_TIME, actor -> actor.renderTime / 20d);
		setActorVariable(MOON_BRIGHTNESS, actor -> class_2874.field_24752[ClientUtil.getClientMoonPhase().method_75261()]);
		setActorVariable(MOON_PHASE, actor -> ClientUtil.getClientMoonPhase().method_75261());
		setActorVariable(PLAYER_LEVEL, actor -> actor.clientPlayer.field_7520);
		setActorVariable(TIME_OF_DAY, actor -> actor.level.method_8532() / 24000d);
		setActorVariable(TIME_STAMP, actor -> actor.level.method_75260());

		setDefaultBlockEntityQueryValues();
		setDefaultEntityQueryValues();
		setDefaultLivingEntityQueryValues();
		setDefaultMobQueryValues();
		setDefaultItemQueryValues();
	}

	private static void setDefaultBlockEntityQueryValues() {
		MolangQueries.<class_2586>setActorVariable(BLOCK_STATE, actor -> actor.animatable.method_11010().method_26204().method_9595().method_11662().indexOf(actor.animatable.method_11010()));
	}

	private static void setDefaultEntityQueryValues() {
		MolangQueries.<class_1297>setActorVariable(BODY_X_ROTATION, actor -> actor.animatable instanceof class_1309 ? 0 : actor.animatable.method_5695(actor.partialTick));
		MolangQueries.<class_1297>setActorVariable(BODY_Y_ROTATION, actor -> actor.animatable instanceof class_1309 living ? class_3532.method_16439(actor.partialTick, living.field_6220, living.field_6283) : actor.animatable.method_5705(actor.partialTick));
		MolangQueries.<class_1297>setActorVariable(CARDINAL_FACING, actor -> actor.animatable.method_5735().method_10146());
		MolangQueries.<class_1297>setActorVariable(CARDINAL_FACING_2D, actor -> {
			int directionId = actor.animatable.method_5735().method_10146();

			return directionId < 2 ? 6 : directionId;
		});
		MolangQueries.<class_1297>setActorVariable(DISTANCE_FROM_CAMERA, actor -> actor.cameraPos.method_1022(actor.animatable.method_73189()));
		MolangQueries.<class_1297>setActorVariable(GET_ACTOR_INFO_ID, actor -> actor.animatable.method_5628());
		MolangQueries.<class_1297>setActorVariable(EQUIPMENT_COUNT, actor -> actor.animatable instanceof class_9460 equipmentUser ? Arrays.stream(class_1304.values()).filter(class_1304::method_46643).filter(slot -> !equipmentUser.method_6118(slot).method_7960()).count() : 0);
		MolangQueries.<class_1297>setActorVariable(HAS_COLLISION, actor -> !actor.animatable.field_5960 ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(HAS_GRAVITY, actor -> !actor.animatable.method_5740() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(HAS_OWNER, actor -> actor.animatable instanceof class_6025 ownable && ownable.method_66287() != null ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(HAS_PLAYER_RIDER, actor -> actor.animatable.method_5703(class_1657.class::isInstance) ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(HAS_RIDER, actor -> actor.animatable.method_5782() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_ALIVE, actor -> actor.animatable.method_5805() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_ANGRY, actor -> actor.animatable instanceof class_5354 neutralMob && neutralMob.method_29511() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_BREATHING, actor -> actor.animatable.method_5669() >= actor.animatable.method_5748() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_FIRE_IMMUNE, actor -> actor.animatable.method_5864().method_19946() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_INVISIBLE, actor -> actor.animatable.method_5767() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_IN_CONTACT_WITH_WATER, actor -> actor.animatable.method_5721() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_IN_LAVA, actor -> actor.animatable.method_5771() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_IN_WATER, actor -> actor.animatable.method_5799() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_IN_WATER_OR_RAIN, actor -> actor.animatable.method_5721() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_LEASHED, actor -> actor.animatable instanceof class_9817 leashable && leashable.method_60953() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_MOVING, actor -> actor.renderState.getOrDefaultGeckolibData(DataTickets.IS_MOVING, false) ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_ON_FIRE, actor -> actor.animatable.method_5809() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_ON_GROUND, actor -> actor.animatable.method_24828() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_RIDING, actor -> actor.animatable.method_5765() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_SADDLED, actor -> actor.animatable instanceof class_9460 equipmentUser && !equipmentUser.method_6118(class_1304.field_55946).method_7960() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_SILENT, actor -> actor.animatable.method_5701() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_SNEAKING, actor -> actor.animatable.method_18276() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_SPRINTING, actor -> actor.animatable.method_5624() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(IS_SWIMMING, actor -> actor.animatable.method_5681() ? 1 : 0);
		MolangQueries.<class_1297>setActorVariable(MOVEMENT_DIRECTION, actor -> actor.renderState.getOrDefaultGeckolibData(DataTickets.IS_MOVING, false) ? class_2350.method_58251(actor.animatable.method_18798()).method_10146() : 6);
		MolangQueries.<class_1297>setActorVariable(RIDER_BODY_X_ROTATION, actor -> actor.animatable.method_5782() ? actor.animatable.method_31483() instanceof class_1309 ? 0 : actor.animatable.method_31483().method_5695(actor.partialTick) : 0);
		MolangQueries.<class_1297>setActorVariable(RIDER_BODY_Y_ROTATION, actor -> actor.animatable.method_5782() ? actor.animatable.method_31483() instanceof class_1309 living ? class_3532.method_16439(actor.partialTick, living.field_6220, living.field_6283) : actor.animatable.method_31483().method_5705(actor.partialTick) : 0);
		MolangQueries.<class_1297>setActorVariable(RIDER_HEAD_X_ROTATION, actor -> actor.animatable.method_31483() instanceof class_1309 living ? living.method_5695(actor.partialTick) : 0);
		MolangQueries.<class_1297>setActorVariable(RIDER_HEAD_Y_ROTATION, actor -> actor.animatable.method_31483() instanceof class_1309 living ? living.method_5705(actor.partialTick) : 0);
		MolangQueries.<class_1297>setActorVariable(VERTICAL_SPEED, actor -> actor.animatable.method_18798().field_1351);
		MolangQueries.<class_1297>setActorVariable(YAW_SPEED, actor -> actor.animatable.method_36454() - actor.animatable.field_5982);
	}

	private static void setDefaultLivingEntityQueryValues() {
		MolangQueries.<class_1309>setActorVariable(BLOCKING, actor -> actor.animatable.method_6039() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(DEATH_TICKS, actor -> actor.animatable.field_6213 == 0 ? 0 : actor.animatable.field_6213 + actor.partialTick);
		MolangQueries.<class_1309>setActorVariable(GROUND_SPEED, actor -> actor.animatable.method_18798().method_37267());
		MolangQueries.<class_1309>setActorVariable(HAS_HEAD_GEAR, actor -> !actor.animatable.method_6118(class_1304.field_6169).method_7960() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(HEAD_X_ROTATION, actor -> actor.renderState.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, actor.animatable.method_5695(actor.partialTick)));
		MolangQueries.<class_1309>setActorVariable(HEAD_Y_ROTATION, actor -> actor.renderState.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, actor.animatable.method_5705(actor.partialTick)));
		MolangQueries.<class_1309>setActorVariable(HEALTH, actor -> actor.animatable.method_6032());
		MolangQueries.<class_1309>setActorVariable(HURT_TIME, actor -> actor.animatable.field_6235 == 0 ? 0 : actor.animatable.field_6235 - actor.partialTick);
		MolangQueries.<class_1309>setActorVariable(INVULNERABLE_TICKS, actor -> actor.animatable.field_6008 == 0 ? 0 : actor.animatable.field_6008 - actor.partialTick);
		MolangQueries.<class_1309>setActorVariable(IS_BABY, actor -> actor.animatable.method_6109() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(IS_SLEEPING, actor -> actor.animatable.method_6113() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(IS_USING_ITEM, actor -> actor.animatable.method_6115() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(IS_WALL_CLIMBING, actor -> actor.animatable.method_6101() ? 1 : 0);
		MolangQueries.<class_1309>setActorVariable(LIMB_SWING, actor -> actor.animatable.field_42108.method_48569());
		MolangQueries.<class_1309>setActorVariable(LIMB_SWING_AMOUNT, actor -> actor.animatable.field_42108.method_48570(actor.partialTick()));
		MolangQueries.<class_1309>setActorVariable(MAIN_HAND_ITEM_MAX_DURATION, actor -> actor.animatable.method_6047().method_7935(actor.animatable));
		MolangQueries.<class_1309>setActorVariable(MAIN_HAND_ITEM_USE_DURATION, actor -> actor.animatable.method_6058() == class_1268.field_5808 ? actor.animatable.method_6048() / 20d + actor.partialTick : 0);
		MolangQueries.<class_1309>setActorVariable(MAX_HEALTH, actor -> actor.animatable.method_6063());
		MolangQueries.<class_1309>setActorVariable(SCALE, actor -> actor.animatable.method_55693());
		MolangQueries.<class_1309>setActorVariable(SLEEP_ROTATION, actor -> Optional.ofNullable(actor.animatable.method_18401()).map(class_2350::method_10144).orElse(0f));
	}

	private static void setDefaultMobQueryValues() {
		MolangQueries.<class_1308>setActorVariable(CAN_CLIMB, actor -> !actor.animatable.method_5987() && actor.animatable.method_5942() instanceof class_1410 ? 1 : 0);
		MolangQueries.<class_1308>setActorVariable(CAN_FLY, actor -> !actor.animatable.method_5987() && actor.animatable.method_5942() instanceof class_1407 ? 1 : 0);
		MolangQueries.<class_1308>setActorVariable(CAN_SWIM, actor -> !actor.animatable.method_5987() && actor.animatable.method_5942() instanceof class_1412 || actor.animatable.method_5942() instanceof class_5766 ? 1 : 0);
		MolangQueries.<class_1308>setActorVariable(CAN_WALK, actor -> !actor.animatable.method_5987() && actor.animatable.method_5942() instanceof class_1409 || actor.animatable.method_5942() instanceof class_5766 ? 1 : 0);
	}

	private static void setDefaultItemQueryValues() {
		MolangQueries.<class_1792>setActorVariable(IS_ENCHANTED, actor -> actor.renderState.getGeckolibData(DataTickets.IS_ENCHANTED) ? 1 : 0);
		MolangQueries.<class_1792>setActorVariable(IS_STACKABLE, actor -> actor.renderState.getGeckolibData(DataTickets.IS_STACKABLE) ? 1 : 0);
		MolangQueries.<class_1792>setActorVariable(ITEM_MAX_USE_DURATION, actor -> actor.renderState.getGeckolibData(DataTickets.MAX_USE_DURATION));
		MolangQueries.<class_1792>setActorVariable(MAX_DURABILITY, actor -> actor.renderState.getGeckolibData(DataTickets.MAX_DURABILITY));
		MolangQueries.<class_1792>setActorVariable(REMAINING_DURABILITY, actor -> actor.renderState.getGeckolibData(DataTickets.REMAINING_DURABILITY));
	}
}
