package render;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *
 * @author Alex
 */
public class Gradient {

    public Gradient(int angle, ColorStop[] points) {
        this.angle = angle;
        this.points = points;
    }

    public Color[] getColors() {
        Color[] result = new Color[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = points[i].color;
        }
        return result;
    }

    public float[] getPositions() {
        float[] result = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = points[i].pos;
        }
        return result;
    }

    public int getAngle() {
        return angle;
    }

    public ColorStop[] getPoints() {
        return points;
    }

    public static Point2D[] getPoints(float[] pos, int angle, int x, int y, int w, int h) {
        Point2D[] result = new Point2D[2];
        if (angle % 90 == 0 && angle % 180 > 0) {
            int x1 = x;
            int y1 = y;
            int x2 = x;
            int y2 = y + h;
            result[0] = new Point2D.Float(x1, y1);
            result[1] = new Point2D.Float(x2, y2);
            return result;
        } else if (angle % 180 == 0) {
            int x1 = x;
            int y1 = y;
            int x2 = x + w;
            int y2 = y;
            result[0] = new Point2D.Float(x1, y1);
            result[1] = new Point2D.Float(x2, y2);
            return result;
        } else {
            int d = w / 2;
            if (Math.abs(angle) > 60 && Math.abs(angle) < 120) d = (int)Math.round(w * 0.18);
            if (Math.abs(angle) > 80 && Math.abs(angle) < 100) d = (int)Math.round(w * 0.08);
            int x1 = x + w / 2 - d;
            int y1 = y + (int)Math.round(h / 2 + d * Math.tan(Math.PI * (1f / 180 * (float)angle + 1)));
            int x2 = x + w / 2 + d;
            int y2 = y + h - (y1 - y);
            result[0] = new Point2D.Float(x1, y1);
            result[1] = new Point2D.Float(x2, y2);
            return result;
        }
    }

    ColorStop[] points;
    int angle;

    public static class ColorStop {

        public ColorStop(Color col, float p) {
            color = col;
            pos = p;
        }

        Color color;
        float pos;
    }
}
