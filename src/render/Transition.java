package render;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author Alex
 */
public class Transition {

    public Transition(Block b, String property, int time, String start_value, String end_value) {
        this.block = b;
        this.property = property;
        this.time = time;
        startColor = b.parseColor(start_value);
        if (startColor != null) {
            value_type = "color";
            endColor = b.parseColor(end_value);
            return;
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

    public Transition(Block b, String property, int time, Color start_value, Color end_value) {
        this.block = b;
        this.property = property;
        this.time = time;
        value_type = "color";
        startColor = start_value;
        endColor = end_value;
    }

    public Transition(Block b, String property, int time, String start_value, String end_value, int timingFunc) {
        this(b, property, time, start_value, end_value);
        timingFunction = timingFunc;
    }

    public Transition(Block b, String property, int time, String start_value, String end_value, int timingFunc, int delay) {
        this(b, property, time, start_value, end_value);
        timingFunction = timingFunc;
        this.delay = delay;
    }

    public Transition(Block b, String property, int time, Color start_value, Color end_value, int timingFunc) {
        this(b, property, time, start_value, end_value);
        timingFunction = timingFunc;
    }

    public Transition(Block b, String property, int time, Color start_value, Color end_value, int timingFunc, int delay) {
        this(b, property, time, start_value, end_value);
        timingFunction = timingFunc;
        this.delay = delay;
    }

    public void start() {
        timer = new Timer(resolution, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double q = Math.max(0, Math.min(1, (double)(System.currentTimeMillis() - startedAt) / time));
                if (!value_type.equals("color")) {
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
                    if (end_width_auto && !block.auto_width) {
                        block.auto_width = true;
                        Block b = block.doIncrementLayout();
                        b.forceRepaint();
                        b.document.repaint();
                    }
                    if (end_height_auto && !block.auto_height) {
                        block.auto_height = true;
                        Block b = block.doIncrementLayout();
                        b.forceRepaint();
                        b.document.repaint();
                    }
                    timer.stop();
                }
            }
        });
        timer.setInitialDelay(delay + 50);
        timer.start();
        startedAt = System.currentTimeMillis();
    }

    public double interpolate(double from, double to, double q) {
        return from + (to - from) * getMultiplier(q);
    }

    private void updateProperty(double value) {
        String[] units = {"px", "%", "em", "rem"};
        block.setProp(property, value + units[value_units]);
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
            block.forceRepaint();
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
            block.forceRepaint();
            return;
        }

        if (prop.equals("background-color")) {
            block.background.bgcolor = color;
            for (Block part: block.parts) {
                part.background.bgcolor = block.background.bgcolor;
            }
            block.forceRepaint();
            return;
        }

        if (prop.equals("color")) {
            block.setTextColorRecursive(color);
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


    public Block block;
    public String property;
    public int timingFunction;
    public int delay = 0;

    public long startedAt = 0;
    public int time = 0;

    public double start_value;
    public double end_value;
    public boolean end_width_auto = false;
    public boolean end_height_auto = false;
    public String value_type = "length";
    public int value_units = Block.Units.px;

    public Color startColor;
    public Color endColor;

    public Timer timer;

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
