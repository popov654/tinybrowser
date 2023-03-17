package render;

import java.awt.Color;
import javax.swing.Timer;

/**
 *
 * @author Alex
 */
public class Transition {

    public Transition(Block b, TransitionInfo info, String property, String start_value, String end_value) {
        this(b, property, info.duration, start_value, end_value, info.timingFunction, info.delay);
    }

    public Transition(Block b, String property, int duration, String start_value, String end_value) {
        this.block = b;
        this.property = property;
        this.duration = duration;
        passiveMode = block.passiveTransitionMode;

        if (start_value != null || property.endsWith("color") ||
                end_value != null && b.parseColor(end_value) != null) {
            if (start_value != null) {
                startColor = b.parseColor(start_value);
            }
            if (startColor == null) {
                startColor = property.equals("color") ? b.color : (b.target_background != null ? b.target_background.bgcolor :
                    (b.background != null ? b.background.bgcolor : new Color(0, 0, 0, 0)));
            }
            if (startColor != null) {
                value_type = "color";
                endColor = b.parseColor(end_value);
                if (endColor == null) {
                    endColor = new Color(0, 0, 0, 0);
                }
            }
        }
        if (property.startsWith("background")) {

            if ((property.equals("background-color") || property.equals("background")) && end_value != null && block.background.gradient == null && block.background.bgImage == null &&
                  (block.target_background == null || block.target_background.gradient == null && block.target_background.bgImage == null)) {
                
                value_type = "color";

                startColor = start_value != null ? b.parseColor(start_value) : (block.background.bgcolor != null ? block.background.bgcolor : new Color(0, 0, 0, 0));
                if (startColor == null) {
                    startColor = block.background != null && block.background.bgcolor != null ? block.background.bgcolor : new Color(0, 0, 0, 0);
                }
                endColor = b.parseColor(end_value);
                if (endColor == null) {
                    endColor = new Color(0, 0, 0, 0);
                }

                block.target_background = new Background();
                block.target_background.bgcolor = endColor;

                return;
            }

            value_type = "background";

            block.document.ready = false;
            if (block.background == null) {
                block.background = new Background();
            }
            Background old_background = block.background;
            block.background = block.background.clone();
            block.background.gradient = null;
            block.setProp(property, end_value);
            block.target_background = block.background;
            block.background = old_background;
            block.backgroundState = 0;
            block.document.ready = true;
        }
        if (startColor != null) return;

        if (start_value == null) {
            start_value = block.cssStyles.get(property);
            if (start_value == null) return;
        }

        if (block.display_type == Block.Display.BLOCK && block.auto_width == true && property.equals("width")) {
            block.auto_width = false;
            if (start_value.equals("auto")) {
                double realPercent = (double)block.width / (block.parent.viewport_width - block.parent.paddings[3] - block.parent.paddings[1]) * 100;
                start_value = realPercent + "%";
            }
            if (end_value.equals("auto")) {
                end_value = "100%";
                end_width_auto = true;
            }
        }
        if (property.equals("height")) {
            block.auto_height = false;
            if (start_value.equals("auto")) {
                start_value = block.orig_height + "px";
            }
            if (end_value.equals("auto")) {
                if (start_value.equals("auto")) {
                    end_value = block.orig_height + "px";
                } else {
                    if (!block.auto_height) {
                        block.auto_height = true;
                        block.performLayout();
                        end_value = block.orig_height + "px";
                        block.auto_height = false;
                        block.performLayout();
                    }
                }
                end_height_auto = true;
            }
        }
        int[] val_start = b.parseValueStringToArray(start_value);
        int[] val_end = b.parseValueStringToArray(end_value);
        if (val_start[1] != val_end[1]) {
            this.start_value = b.getValueInCssPixels(start_value);
            this.end_value = b.getValueInCssPixels(end_value);
        } else {
            this.start_value = val_start[0];
            this.end_value = val_end[0];
            value_units = val_start[1];
        }
    }

    public Transition(Block b, String property, int duration, Color start_value, Color end_value) {
        this.block = b;
        this.property = property;
        this.duration = duration;
        passiveMode = block.passiveTransitionMode;
        value_type = "color";
        startColor = start_value;
        endColor = end_value;
    }

    public Transition(Block b, String property, int duration, String start_value, String end_value, int timingFunc) {
        this(b, property, duration, start_value, end_value);
        timingFunction = timingFunc;
    }

    public Transition(Block b, String property, int duration, String start_value, String end_value, int timingFunc, int delay) {
        this(b, property, duration, start_value, end_value);
        timingFunction = timingFunc;
        this.delay = delay;
    }

    public Transition(Block b, String property, int duration, Color start_value, Color end_value, int timingFunc) {
        this(b, property, duration, start_value, end_value);
        timingFunction = timingFunc;
    }

    public Transition(Block b, String property, int duration, Color start_value, Color end_value, int timingFunc, int delay) {
        this(b, property, duration, start_value, end_value);
        timingFunction = timingFunc;
        this.delay = delay;
    }

    public void start() {
        for (Transition t: block.activeTransitions) {
            if (t.block == block && t.property.equals(property)) {
                block.activeTransitions.remove(t);
                t.joinAndStart(this);
                return;
            }
        }
        block.activeTransitions.add(this);
        startedAt = System.currentTimeMillis();
        if (block.animator == null) {
            block.animator = new Timer(resolution, new Animator(block));
            block.animator.setInitialDelay(delay + 50);
            block.animator.start();
        }
    }

    public void stop() {
        update(1);
        block.activeTransitions.remove(this);
        if (value_type.equals("background")) {
            block.background = block.target_background;
        }
    }

    public void joinAndStart(Transition transition) {
        stop();
        try {
            Thread.sleep(50);
        } catch (Exception ex) {}
        transition.start();
        if (!property.equals(transition.property) || !value_type.equals(transition.value_type)) {
            return;
        }
        if (value_type.equals("background")) {
            transition.startedAt = System.currentTimeMillis() - startedAt + 100;
            block.backgroundState = Math.max(0, (System.currentTimeMillis() - startedAt - 100) / duration);
        }
    }

    public void update() {
        double q = Math.max(0, Math.min(1, (double)(System.currentTimeMillis() - startedAt) / duration));
        update(q);
    }

    public void update(double q) {
        if (value_type.equals("background")) {
            block.backgroundState = getMultiplier(q);
            if (!passiveMode) {
                block.forceRepaint();
                block.document.repaint();
            }
        } else if (!value_type.equals("color")) {
            double value = interpolate(start_value, end_value, q);
            //System.out.println(value);
            updateProperty(value);
        } else {
            if (startColor == null || endColor == null) {
                q = 1;
            } else {
                int r = (int) interpolate(startColor.getRed(), endColor.getRed(), q);
                int g = (int) interpolate(startColor.getGreen(), endColor.getGreen(), q);
                int b = (int) interpolate(startColor.getBlue(), endColor.getBlue(), q);
                int a = (int) interpolate(startColor.getAlpha(), endColor.getAlpha(), q);
                updateProperty(new Color(r, g, b, a));
                //System.out.println("color(" + r + ", " + g + ", " + b + ", " + a + ")");
            }
        }
        if (q >= 1) {
            if (value_type.equals("background") && block.target_background != null) {
                block.background = block.target_background;
            }
            boolean flag = false;
            if (end_width_auto && !block.auto_width) {
                block.auto_width = true;
                flag = true;
            }
            if (end_height_auto && !block.auto_height) {
                block.auto_height = true;
                flag = true;
            }
            block.backgroundState = 0;
            if (!passiveMode && flag) {
                Block b = block.doIncrementLayout();
                b.forceRepaint();
                b.document.repaint();
            }
            block.activeTransitions.remove(this);
        }
    }

    public double interpolate(double from, double to, double q) {
        return from + (to - from) * getMultiplier(q);
    }

    private void updateProperty(double value) {
        boolean old_value = block.document.ready;
        if (passiveMode) block.document.ready = false;
        String[] units = {"px", "%", "em", "rem"};
        block.setProp(property, value + units[value_units]);
        if (passiveMode) block.document.ready = old_value;
    }

    private void updateProperty(Color color) {
        String prop = property;
        if (prop.matches("border(-left|-right|-top|-bottom)-color")) {
            if (prop.contains("-left")) block.borderColor[3] = color;
            if (prop.contains("-right")) block.borderColor[1] = color;
            if (prop.contains("-top")) block.borderColor[0] = color;
            if (prop.contains("-bottom")) block.borderColor[2] = color;
            for (Block part: block.parts) {
                part.borderColor[0] = block.borderColor[0];
                part.borderColor[1] = block.borderColor[1];
                part.borderColor[2] = block.borderColor[2];
                part.borderColor[3] = block.borderColor[3];
            }
            if (!passiveMode) {
                block.forceRepaint();
            }
            return;
        }

        if (prop.equals("border-color")) {
            block.borderColor[0] = color;
            block.borderColor[1] = color;
            block.borderColor[2] = color;
            block.borderColor[3] = color;
            for (Block part: block.parts) {
                part.borderColor[0] = block.borderColor[0];
                part.borderColor[1] = block.borderColor[1];
                part.borderColor[2] = block.borderColor[2];
                part.borderColor[3] = block.borderColor[3];
            }
            if (!passiveMode) {
                block.forceRepaint();
            }
            return;
        }

        if (prop.equals("background-color") && block.background.gradient == null && block.background.bgImage == null &&
                block.target_background.gradient == null && block.target_background.bgImage == null) {
            block.background.bgcolor = color;
            for (Block part: block.parts) {
                part.background.bgcolor = block.background.bgcolor;
            }
            if (!passiveMode) {
                block.forceRepaint();
            }
            return;
        }

        if (prop.equals("color")) {
            boolean old_value = block.document.ready;
            if (passiveMode) block.document.ready = false;
            block.setTextColorRecursive(color);
            if (passiveMode) block.document.ready = old_value;
            return;
        }
    }

    private double getMultiplier(double q) {
        switch (timingFunction) {
            case TimingFunction.LINEAR:
                return q;
            case TimingFunction.EASE_IN:
                return q * q;
            case TimingFunction.EASE_OUT:
                return q * (2 - q);
            case TimingFunction.EASE_IN_OUT:
                return 3 * q * q - 2 * q * q * q;
            case TimingFunction.EASE:
                return (double)-2 / (110 * Math.pow(q + 0.15, 8) + 2) + 1;
            case TimingFunction.BOUNCE:
                return 5 * q * q - 4 * q * q * q;

        }
        return q;
    }


    public final Block block;
    public String property;
    public int timingFunction;
    public int delay = 0;

    public long startedAt = 0;
    public int duration = 0;

    public double start_value;
    public double end_value;
    public boolean end_width_auto = false;
    public boolean end_height_auto = false;
    public String value_type = "length";
    public int value_units = Block.Units.px;

    public Color startColor;
    public Color endColor;

    public Timer timer;
    public boolean passiveMode = true;

    public static int resolution = 10;
    

    public static class TimingFunction {
        public static final int LINEAR = 0;
        public static final int EASE_IN = 1;
        public static final int EASE_OUT = 2;
        public static final int EASE_IN_OUT = 3;
        public static final int EASE = 4;
        public static final int BOUNCE = 5;
    }
}
