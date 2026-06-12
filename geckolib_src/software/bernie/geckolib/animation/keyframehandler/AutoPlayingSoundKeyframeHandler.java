package software.bernie.geckolib.animation.keyframehandler;

import net.minecraft.class_10017;
import net.minecraft.class_1569;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2960;
import net.minecraft.class_3419;
import net.minecraft.class_7923;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.state.KeyFrameEvent;
import software.bernie.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.ClientUtil;

/**
 * Built-in helper for a {@link AnimationController.KeyframeEventHandler SoundKeyframeHandler} that automatically plays the sound defined in the keyframe data
 * <p>
 * Due to an inability to determine the position of the sound for all animatables, this handler only supports {@link software.bernie.geckolib.animatable.GeoEntity GeoEntity} and {@link software.bernie.geckolib.animatable.GeoBlockEntity GeoBlockEntity}
 * <p>
 * The expected keyframe data format is one of the below:
 * <pre>{@code
 * namespace:soundid
 * namespace:soundid|volume|pitch
 * }</pre>
 *
 * @param <A> Animatable class type
 */
public class AutoPlayingSoundKeyframeHandler<A extends GeoAnimatable> implements AnimationController.KeyframeEventHandler<A, SoundKeyframeData> {
    @Override
    public void handle(KeyFrameEvent<A, SoundKeyframeData> event) {
        final class_1937 level = ClientUtil.getLevel();

        if (level == null)
            return;

        String[] segments = event.keyframeData().getSound().split("\\|");

        class_7923.field_41172.method_10223(class_2960.method_29186(segments[0]).getOrThrow()).ifPresent(sound -> {
            class_243 position = event.renderState().getOrDefaultGeckolibData(DataTickets.POSITION, event.renderState() instanceof class_10017 entityState ?
                                                                                          new class_243(entityState.field_53325, entityState.field_53326, entityState.field_53327) : null);
            Class<?> animatableClass = event.renderState().getOrDefaultGeckolibData(DataTickets.ANIMATABLE_CLASS, Object.class);

            if (position != null) {
                float volume = segments.length > 1 ? Float.parseFloat(segments[1]) : 1;
                float pitch = segments.length > 2 ? Float.parseFloat(segments[2]) : 1;
                class_3419 source = animatableClass.isAssignableFrom(class_2586.class) ? class_3419.field_15245 :
                                     animatableClass.isAssignableFrom(class_1569.class) ? class_3419.field_15251 : class_3419.field_15254;

                level.method_8486(position.field_1352, position.field_1351, position.field_1350, sound.comp_349(), source, volume, pitch, false);
            }
            else {
                GeckoLibConstants.LOGGER.warn("Found sound keyframe handler, but AnimationState had no position data for animatable: {}", animatableClass.getName());
            }
        });
    }
}
