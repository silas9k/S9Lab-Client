package software.bernie.geckolib.util;

import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_10426;
import net.minecraft.client.renderer.entity.state.*;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Helper class for RenderState-related functionality.
 * <p>
 * Primarily used for cloning RenderStates
 */
public final class RenderStateUtil {
    /**
     * Create a fully cloned copy of an existing {@link class_10017}.
     */
    public static class_10017 cloneEntityState(class_10017 existingState) {
        final class_10017 newState = new class_10017();

        fullCopyEntityState(newState, existingState);

        return newState;
    }

    /**
     * Fully copy an existing {@link class_10017} to a new one.
     */
    public static void fullCopyEntityState(class_10017 newRenderState, class_10017 oldRenderState) {
        newRenderState.field_58171 = oldRenderState.field_58171;
        newRenderState.field_53325 = oldRenderState.field_53325;
        newRenderState.field_53326 = oldRenderState.field_53326;
        newRenderState.field_53327 = oldRenderState.field_53327;
        newRenderState.field_53328 = oldRenderState.field_53328;
        newRenderState.field_53329 = oldRenderState.field_53329;
        newRenderState.field_53330 = oldRenderState.field_53330;
        newRenderState.field_53331 = oldRenderState.field_53331;
        newRenderState.field_53332 = oldRenderState.field_53332;
        newRenderState.field_53333 = oldRenderState.field_53333;
        newRenderState.field_53334 = oldRenderState.field_53334;
        newRenderState.field_53335 = oldRenderState.field_53335;
        newRenderState.field_61820 = oldRenderState.field_61820;
        newRenderState.field_61821 = oldRenderState.field_61821;
        newRenderState.field_53336 = oldRenderState.field_53336;
        newRenderState.field_53337 = oldRenderState.field_53337;
        newRenderState.field_53338 = oldRenderState.field_53338;
        newRenderState.field_60160 = oldRenderState.field_60160;
        newRenderState.field_61822 = oldRenderState.field_61822;

        newRenderState.field_61823.addAll(oldRenderState.field_61823);
        ((GeoRenderState)newRenderState).getDataMap().putAll(((GeoRenderState)oldRenderState).getDataMap());
    }

    /**
     * Create a fully cloned copy of an existing {@link class_10042}.
     */
    public static class_10042 cloneLivingEntityState(class_10042 existingState) {
        final class_10042 newState = new class_10042();

        fullCopyLivingEntityState(newState, existingState);

        return newState;
    }

    /**
     * Fully copy an existing {@link class_10042} to a new one.
     */
    public static void fullCopyLivingEntityState(class_10042 newRenderState, class_10042 oldRenderState) {
        fullCopyEntityState(newRenderState, oldRenderState);

        newRenderState.field_53446 = oldRenderState.field_53446;
        newRenderState.field_53447 = oldRenderState.field_53447;
        newRenderState.field_53448 = oldRenderState.field_53448;
        newRenderState.field_53449 = oldRenderState.field_53449;
        newRenderState.field_53450 = oldRenderState.field_53450;
        newRenderState.field_53451 = oldRenderState.field_53451;
        newRenderState.field_53453 = oldRenderState.field_53453;
        newRenderState.field_53454 = oldRenderState.field_53454;
        newRenderState.field_53455 = oldRenderState.field_53455;
        newRenderState.field_53456 = oldRenderState.field_53456;
        newRenderState.field_53457 = oldRenderState.field_53457;
        newRenderState.field_53458 = oldRenderState.field_53458;
        newRenderState.field_53459 = oldRenderState.field_53459;
        newRenderState.field_53460 = oldRenderState.field_53460;
        newRenderState.field_53461 = oldRenderState.field_53461;
        newRenderState.field_53463 = oldRenderState.field_53463;
        newRenderState.field_53465 = oldRenderState.field_53465;
        newRenderState.field_53467 = oldRenderState.field_53467;
        newRenderState.field_53452 = oldRenderState.field_53452;
        newRenderState.field_55315 = oldRenderState.field_55315;
        newRenderState.field_55316 = oldRenderState.field_55316;
    }

    /**
     * Create a fully cloned copy of an existing {@link class_10426}.
     */
    public static class_10426 cloneArmedEntityState(class_10426 existingState) {
        final class_10426 newState = new class_10426();

        fullCopyArmedEntityState(newState, existingState);

        return newState;
    }

    /**
     * Fully copy an existing {@link class_10426} to a new one.
     */
    public static void fullCopyArmedEntityState(class_10426 newRenderState, class_10426 oldRenderState) {
        fullCopyLivingEntityState(newRenderState, oldRenderState);

        newRenderState.field_55303 = oldRenderState.field_55303;
        newRenderState.field_55304 = oldRenderState.field_55304;
        newRenderState.field_55305 = oldRenderState.field_55305;
        newRenderState.field_55306 = oldRenderState.field_55306;
        newRenderState.field_55307 = oldRenderState.field_55307;
    }

    /**
     * Create a fully cloned copy of an existing {@link class_10034}.
     */
    public static class_10034 cloneHumanoidEntityState(class_10034 existingState) {
        final class_10034 newState = new class_10034();

        fullCopyHumanoidEntityState(newState, existingState);

        return newState;
    }

    /**
     * Fully copy an existing {@link class_10034} to a new one.
     */
    public static void fullCopyHumanoidEntityState(class_10034 newRenderState, class_10034 oldRenderState) {
        fullCopyArmedEntityState(newRenderState, oldRenderState);

        newRenderState.field_53403 = oldRenderState.field_53403;
        newRenderState.field_63604 = oldRenderState.field_63604;
        newRenderState.field_53405 = oldRenderState.field_53405;
        newRenderState.field_53406 = oldRenderState.field_53406;
        newRenderState.field_53407 = oldRenderState.field_53407;
        newRenderState.field_53408 = oldRenderState.field_53408;
        newRenderState.field_53409 = oldRenderState.field_53409;
        newRenderState.field_53410 = oldRenderState.field_53410;
        newRenderState.field_53411 = oldRenderState.field_53411;
        newRenderState.field_53412 = oldRenderState.field_53412;
        newRenderState.field_53413 = oldRenderState.field_53413;
        newRenderState.field_53414 = oldRenderState.field_53414;
        newRenderState.field_53415 = oldRenderState.field_53415;
        newRenderState.field_53416 = oldRenderState.field_53416;
        newRenderState.field_53417 = oldRenderState.field_53417;
        newRenderState.field_55309 = oldRenderState.field_55309;
        newRenderState.field_53418 = oldRenderState.field_53418;
        newRenderState.field_53419 = oldRenderState.field_53419;
        newRenderState.field_53420 = oldRenderState.field_53420;
    }

    /**
     * Create a fully cloned copy of an existing {@link class_10055}.
     */
    public static class_10055 cloneAvatarEntityState(class_10055 existingState) {
        final class_10055 newState = new class_10055();

        fullCopyAvatarEntityState(newState, existingState);

        return newState;
    }

    /**
     * Fully copy an existing {@link class_10055} to a new one.
     */
    public static void fullCopyAvatarEntityState(class_10055 newRenderState, class_10055 oldRenderState) {
        fullCopyHumanoidEntityState(newRenderState, oldRenderState);

        newRenderState.field_53520 = oldRenderState.field_53520;
        newRenderState.field_53536 = oldRenderState.field_53536;
        newRenderState.field_53537 = oldRenderState.field_53537;
        newRenderState.field_53538 = oldRenderState.field_53538;
        newRenderState.field_53539 = oldRenderState.field_53539;
        newRenderState.field_53540 = oldRenderState.field_53540;
        newRenderState.field_53542 = oldRenderState.field_53542;
        newRenderState.field_53543 = oldRenderState.field_53543;
        newRenderState.field_53544 = oldRenderState.field_53544;
        newRenderState.field_53545 = oldRenderState.field_53545;
        newRenderState.field_53546 = oldRenderState.field_53546;
        newRenderState.field_53530 = oldRenderState.field_53530;
        newRenderState.field_53531 = oldRenderState.field_53531;
        newRenderState.field_53532 = oldRenderState.field_53532;
        newRenderState.field_53534 = oldRenderState.field_53534;
        newRenderState.field_53535 = oldRenderState.field_53535;
        newRenderState.field_53521 = oldRenderState.field_53521;
        newRenderState.field_53525 = oldRenderState.field_53525;
        newRenderState.field_53526 = oldRenderState.field_53526;
        newRenderState.field_53527 = oldRenderState.field_53527;
        newRenderState.field_53528 = oldRenderState.field_53528;
        newRenderState.field_62758 = oldRenderState.field_62758;
        newRenderState.field_55317 = oldRenderState.field_55317;
    }

    /**
     * Create a partial-clone of an existing unknown RenderState into a new {@link class_10034} for the purpose of
     * armor rendering, which explicitly requires an {@code HumanoidRenderState}
     * <p>
     * Because this is only being used for armor rendering, we don't need an exhaustive copy of the renderstate and instead focus
     * solely on the data points we know are needed.
     * <p>
     * If you are doing custom modeling and a data point here is missing and causing you issues, let me know in Discord and I'll add it
     */
    public static class_10034 makeMinimalArmorRenderingClone(final class_10034 newRenderState, final class_10017 oldRenderState) {
        ((GeoRenderState)newRenderState).getDataMap().putAll(((GeoRenderState)oldRenderState).getDataMap());

        newRenderState.field_58171 = oldRenderState.field_58171; // Optional
        newRenderState.field_53325 = oldRenderState.field_53325; // Optional
        newRenderState.field_53326 = oldRenderState.field_53326; // Optional
        newRenderState.field_53327 = oldRenderState.field_53327; // Optional
        newRenderState.field_53328 = oldRenderState.field_53328;
        newRenderState.field_53331 = oldRenderState.field_53331; // Optional
        newRenderState.field_53332 = oldRenderState.field_53332; // Optional
        newRenderState.field_53333 = oldRenderState.field_53333; // Optional
        newRenderState.field_53334 = oldRenderState.field_53334; // Optional
        newRenderState.field_53335 = oldRenderState.field_53335; // Optional
        newRenderState.field_61820 = oldRenderState.field_61820; // Optional
        newRenderState.field_61821 = oldRenderState.field_61821; // Optional

        if (oldRenderState instanceof class_10042 livingEntityState) {
            newRenderState.field_53446 = livingEntityState.field_53446; // Optional
            newRenderState.field_53447 = livingEntityState.field_53447;
            newRenderState.field_53448 = livingEntityState.field_53448;
            newRenderState.field_53449 = livingEntityState.field_53449; // Optional
            newRenderState.field_53450 = livingEntityState.field_53450;
            newRenderState.field_53451 = livingEntityState.field_53451;
            newRenderState.field_53453 = livingEntityState.field_53453; // Optional
            newRenderState.field_53454 = livingEntityState.field_53454;
            newRenderState.field_53455 = livingEntityState.field_53455; // Optional
            newRenderState.field_53456 = livingEntityState.field_53456; // Optional
            newRenderState.field_53457 = livingEntityState.field_53457; // Optional
            newRenderState.field_53458 = livingEntityState.field_53458; // Optional
            newRenderState.field_53459 = livingEntityState.field_53459; // Optional
            newRenderState.field_53460 = livingEntityState.field_53460; // Optional
            newRenderState.field_53461 = livingEntityState.field_53461; // Optional
            newRenderState.field_53463 = livingEntityState.field_53463; // Optional
            newRenderState.field_53465 = livingEntityState.field_53465; // Optional

            if (livingEntityState instanceof class_10426 armedState) {
                newRenderState.field_55303 = armedState.field_55303;
                newRenderState.field_55304 = armedState.field_55304;
                newRenderState.field_55306 = armedState.field_55306;

                if (armedState instanceof class_10034 humanoidState) {
                    newRenderState.field_53403 = humanoidState.field_53403;
                    newRenderState.field_63604 = humanoidState.field_63604;
                    newRenderState.field_53405 = humanoidState.field_53405;
                    newRenderState.field_53406 = humanoidState.field_53406;
                    newRenderState.field_53407 = humanoidState.field_53407;
                    newRenderState.field_53408 = humanoidState.field_53408;
                    newRenderState.field_53409 = humanoidState.field_53409;
                    newRenderState.field_53410 = humanoidState.field_53410;
                    newRenderState.field_53411 = humanoidState.field_53411;
                    newRenderState.field_53412 = humanoidState.field_53412; // Optional
                    newRenderState.field_53413 = humanoidState.field_53413;
                    newRenderState.field_53414 = humanoidState.field_53414;
                    newRenderState.field_53415 = humanoidState.field_53415; // Optional
                    newRenderState.field_53416 = humanoidState.field_53416; // Optional
                    newRenderState.field_53417 = humanoidState.field_53417; // Optional
                    newRenderState.field_55309 = humanoidState.field_55309; // Optional
                    newRenderState.field_53418 = humanoidState.field_53418; // Optional
                    newRenderState.field_53419 = humanoidState.field_53419; // Optional
                    newRenderState.field_53420 = humanoidState.field_53420; // Optional
                }
            }
        }

        return newRenderState;
    }

    private RenderStateUtil() {}
}
