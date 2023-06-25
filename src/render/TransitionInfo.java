package render;

/**
 *
 * @author Alex
 */
public class TransitionInfo {

    public TransitionInfo(Block block, String prop, int duration, int timingFunction, int delay) {
        this.block = block;
        this.timingFunction = timingFunction;
        this.property = prop;
        this.duration = duration;
        this.delay = delay;
    }

    public TransitionInfo(Block block, String prop, int duration, double[] coeffs, int delay) {
        this.block = block;
        this.timingFunction = Transition.TimingFunction.BEZIER;
        this.bezierCoeficients = java.util.Arrays.copyOf(coeffs, 4);
        this.property = prop;
        this.duration = duration;
        this.delay = delay;
    }

    public double[] bezierCoeficients = {0, 0, 1, 1};

    public Block block;
    public String property;
    public int duration;
    public int timingFunction;
    public int delay;
}
