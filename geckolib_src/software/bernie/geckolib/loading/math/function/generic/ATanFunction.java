package software.bernie.geckolib.loading.math.function.generic;

import net.minecraft.class_3532;
import software.bernie.geckolib.animation.state.ControllerState;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.function.MathFunction;

/**
 * {@link MathFunction} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the arc-tangent of the input value angle, with the input angle converted to radians
 */
public final class ATanFunction extends MathFunction {
    private final MathValue value;

    public ATanFunction(MathValue... values) {
        super(values);

        this.value = values[0];
    }

    @Override
    public String getName() {
        return "math.atan";
    }

    @Override
    public double compute(ControllerState controllerState) {
        return Math.atan(this.value.get(controllerState) * class_3532.field_29847);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public MathValue[] getArgs() {
        return new MathValue[] {this.value};
    }
}
