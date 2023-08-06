package com.alstarsoft.tinybrowser.render;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 *
 * @author Alex
 */
public class RoundedRect extends RectangularShape {

    public RoundedRect(double x, double y, double w, double h, double arc1, double arc2, double arc3, double arc4) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        arcs[0] = Math.max(arc1*2, 0);
        arcs[1] = Math.max(arc2*2, 0);
        arcs[2] = Math.max(arc3*2, 0);
        arcs[3] = Math.max(arc4*2, 0);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
	if (isEmpty() || w <= 0 || h <= 0) {
	    return false;
	}
	double rrx0 = getX();
	double rry0 = getY();
	double rrx1 = rrx0 + getWidth();
	double rry1 = rry0 + getHeight();
	// Check for trivial rejection - bounding rectangles do not intersect
	if (x + w < rrx0 || x > rrx1 || y + h < rry0 || y > rry1) {
	    return false;
	}

        double r1 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[0]))) / 2.0;
	double r2 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[1]))) / 2.0;
        double r3 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[2]))) / 2.0;
        double r4 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[3]))) / 2.0;

        double xx = 0, yy = 0;

        if (x + w < rrx0 + r1 && y + h < rry0 + r1) {
            xx = (x + w - (rrx0 + r1)) / r1;
            yy = (y + h - (rry0 + r1)) / r1;
            if (xx * xx + yy * yy > 1.0) return false;
        }

        if (x >= rrx1 - r2 && y + h < rry0 + r2) {
            x = (x - (rrx1 - r2)) / r2;
            y = (y - (rry0 + r2)) / r2;
            if (xx * xx + yy * yy > 1.0) return false;
        }

        if (x >= rrx1 - r3 && y >= rry1 - r3) {
            x = (x - (rrx1 - r3)) / r3;
            y = (y - (rry1 - r3)) / r3;
            if (xx * xx + yy * yy > 1.0) return false;
        }

        if (x + w < rrx0 + r4 && y >= rry1 - r4) {
            x = (x + w - (rrx0 + r4)) / r4;
            y = (y - (rry1 - r4)) / r4;
            if (xx * xx + yy * yy > 1.0) return false;
        }

        return true;
    }

    @Override
    public boolean contains(double x, double y) {
	if (isEmpty()) {
	    return false;
	}
	double rrx0 = getX();
	double rry0 = getY();
	double rrx1 = rrx0 + getWidth();
	double rry1 = rry0 + getHeight();
	// Check for trivial rejection - point is outside bounding rectangle
	if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
	    return false;
	}
	double r1 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[0]))) / 2.0;
	double r2 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[1]))) / 2.0;
        double r3 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[2]))) / 2.0;
        double r4 = Math.min(getHeight(), Math.min(getWidth(), Math.abs(arcs[3]))) / 2.0;

        double sw, sh;
	// Check which corner point is in and do circular containment
	// test - otherwise simple acceptance

        double x1 = rrx0 + Math.max(r1, r4);
        double y1 = rry0 + Math.max(r1, r2);
        double w1 = rrx1 - Math.max(r2, r3);
        double h1 = rry1 - Math.max(r3, r4);

        if (x >= x1 && y >= y1 && x < w1 && y < h1) {
            return true;
        }

        sh = Math.max(r1, r2);
	if (x >= r1 && x < rrx1 - r2 && y < rry0 + sh) {
	    return true;
	}
        sw = Math.max(r1, r4);
	if (y >= rry0 + r1 && y < rry1 - r4 && x < rrx0 + sw) {
	    return true;
	}
        sh = Math.max(r3, r4);
	if (x >= rrx0 + r4 && x < rrx1 - r3 && y >= rry1 - sh) {
	    return true;
	}
        sw = Math.max(r2, r3);
	if (y >= rry0 + r2 && y < rry1 - r3 && x >= rrx1 - sw) {
	    return true;
	}
        if (x < rrx0 + r1 && y < rry0 + r1) {
            x = (x - (rrx0 + r1)) / r1;
            y = (y - (rry0 + r1)) / r1;
        } else if (x >= rrx1 - r2 && y < rry0 + r2) {
            x = (x - (rrx1 - r2)) / r2;
            y = (y - (rry0 + r2)) / r2;
        } else if (x >= rrx1 - r3 && y >= rry1 - r3) {
            x = (x - (rrx1 - r3)) / r3;
            y = (y - (rry1 - r3)) / r3;
        } else if (x < rrx0 + r4 && y >= rry1 - r4) {
            x = (x - (rrx0 + r4)) / r4;
            y = (y - (rry1 - r4)) / r4;
        }
	return (x * x + y * y <= 1.0);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
	if (isEmpty() || w <= 0 || h <= 0) {
	    return false;
	}
	return (contains(x, y) &&
		contains(x + w, y) &&
		contains(x, y + h) &&
		contains(x + w, y + h));
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new RoundRectIterator(this, at);
    }

    public double x;
    public double y;
    public double width;
    public double height;

    double[] arcs = new double[4];

    @Override
    public double getX() {
        return (double) x;
    }

    @Override
    public double getY() {
        return (double) y;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public boolean isEmpty() {
        return (width <= 0.0f) || (height <= 0.0f);
    }

    @Override
    public void setFrame(double x, double y, double w, double h) {
        
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RoundedRect) {
            RoundedRect rr = (RoundedRect) obj;
            return ((getX() == rr.getX()) &&
                    (getY() == rr.getY()) &&
                    (getWidth() == rr.getWidth()) &&
                    (getHeight() == rr.getHeight()) &&
                    (arcs[0] == rr.arcs[0]) &&
                    (arcs[1] == rr.arcs[1]) &&
                    (arcs[2] == rr.arcs[2]) &&
                    (arcs[3] == rr.arcs[3]));
        }
        return false;
    }

    @Override
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        bits += java.lang.Double.doubleToLongBits(arcs[0]) * 53;
        bits += java.lang.Double.doubleToLongBits(arcs[1]) * 59;
        bits += java.lang.Double.doubleToLongBits(arcs[2]) * 61;
        bits += java.lang.Double.doubleToLongBits(arcs[3]) * 67;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
    
}
