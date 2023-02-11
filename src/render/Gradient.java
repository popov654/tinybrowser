package render;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *
 * @author Alex
 */
public class Gradient {

    public Gradient(double angle, ColorStop[] points) {
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

    public float[] getPositions(double angle, Point2D start, Point2D end) {
        float[] result = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = points[i].pos;
            if (points[i].units < 0) continue;
            double dx = end.getX() - start.getX();
            double dy = end.getY() - start.getY();
            double r = Math.sqrt(dx * dx + dy * dy);
            if (points[i].units == Block.Units.px) {
                result[i] = r > 0 ? (float)(points[i].pos / r) : 0;
            }
            else if (points[i].units == Block.Units.em) {
                result[i] = r > 0 ? (float)(points[i].pos * 16 / r) : 0;
            }
            else if (points[i].units == Block.Units.percent) {
                result[i] = points[i].pos / 100;
            }
        }
        return result;
    }

    public double getAngle() {
        return angle;
    }

    public ColorStop[] getPoints() {
        return points;
    }

    public static Point2D[] getPoints(double angle, int x, int y, int w, int h) {
        Point2D[] result = new Point2D[2];
        if (angle % 180 == 0) {
            int x1 = x;
            int y1 = y;
            int x2 = x;
            int y2 = y + h - 1;
            if (angle % 360 > 0) {
                result[0] = new Point2D.Float(x1, y1);
                result[1] = new Point2D.Float(x2, y2);
            } else {
                result[1] = new Point2D.Float(x1, y1);
                result[0] = new Point2D.Float(x2, y2);
            }
            return result;
        } else if (angle % 90 == 0) {
            int x1 = x;
            int y1 = y;
            int x2 = x + w - 1;
            int y2 = y;
            if (angle % 180 > 0) {
                result[0] = new Point2D.Float(x1, y1);
                result[1] = new Point2D.Float(x2, y2);
            } else {
                result[1] = new Point2D.Float(x1, y1);
                result[0] = new Point2D.Float(x2, y2);
            }
            return result;
        } else {

            boolean flag = true;

            if (angle % 360 > 180) {
                angle = 180 + angle % 360;
                flag = false;
            }

            double s = Math.sin((double) angle / 180 * Math.PI);
            double c = Math.cos((double) angle / 180 * Math.PI);
            int dx = (int) (Math.round(h * s * s));
            int dy = (int) (Math.round(h * c * s));

            w = h = Math.max(w, h);

            int x1 = 0;
            int y1 = 0;
            int x2 = (int) (x + w * 0.5 + dx - 2);
            int y2 = (int) (y + h * 0.5 - dy - 2);

            if (flag) {
                if (dx > 0 && dy < 0) {
                    result[0] = new Point2D.Float(0, 0);
                    result[1] = new Point2D.Float(x2, y2);
                } else {
                    result[0] = new Point2D.Float(x, y+h-1);
                    result[1] = new Point2D.Float(x2, y2);
                }
            } else {
                if (dx > 0 && dy < 0) {
                    result[0] = new Point2D.Float(x2, y2);
                    result[1] = new Point2D.Float(0, 0);
                } else {
                    result[0] = new Point2D.Float(x2, y2);
                    result[1] = new Point2D.Float(x, y+h-1);
                }
            }

            return result;
        }
    }

    ColorStop[] points;
    double angle;

    public static class ColorStop {

        public ColorStop(Color col, float p) {
            color = col;
            pos = p;
        }

         public ColorStop(Color col, float p, int u) {
            color = col;
            pos = p;
            units = u;
        }

        Color color;
        float pos;
        int units = -1;
    }
}
