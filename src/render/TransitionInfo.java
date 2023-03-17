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

    public Block block;
    public String property;
    public int duration;
    public int timingFunction;
    public int delay;
}
