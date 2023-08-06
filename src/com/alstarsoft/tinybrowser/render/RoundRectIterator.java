package com.alstarsoft.tinybrowser.render;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class RoundRectIterator implements PathIterator {
    double x, y, w, h, aw, ah;
    AffineTransform affine;
    int index;
    RoundedRect r;
    HashMap<Integer, Integer> corners = new HashMap<Integer, Integer>();

    RoundRectIterator(RoundedRect rr, AffineTransform at) {
        this.x = rr.getX();
        this.y = rr.getY();
        this.w = rr.getWidth();
        this.h = rr.getHeight();
        this.affine = at;
        this.r = rr;

        if (r.arcs[0] > 0.0f && r.arcs[1] > 0.0f && r.arcs[2] > 0.0f && r.arcs[3] > 0.0f) {
            //return;
        }

        for (int i = 0; i < 4; i++) {
            corners.put(2 + i * 2, 3-i);
        }

        Vector<double[]> v = new Vector<double[]>();
        for (int i = 0; i < ctrlpts.length; i++) {
            v.add(ctrlpts[i]);
        }
        Vector<Integer> s = new Vector<Integer>();
        for (int i = 0; i < types.length; i++) {
            s.add(types[i]);
        }

        Vector<double[]> v1 = new Vector<double[]>();
        v1.add(v.get(2));
        Vector<Integer> s1 = new Vector<Integer>();
        s1.add(SEG_CUBICTO);

        Vector<double[]> v2 = new Vector<double[]>();
        v2.add(v.get(4));
        Vector<Integer> s2 = new Vector<Integer>();
        s2.add(SEG_CUBICTO);

        Vector<double[]> v3 = new Vector<double[]>();
        v3.add(v.get(6));
        Vector<Integer> s3 = new Vector<Integer>();
        s3.add(SEG_CUBICTO);

        Vector<double[]> v4 = new Vector<double[]>();
        v4.add(v.get(8));
        Vector<Integer> s4 = new Vector<Integer>();
        s4.add(SEG_CUBICTO);

        int n = 0;
        for (int i = 0; i < v.size(); i++) {
            if (s.get(i) == SEG_CUBICTO && r.arcs[3-n] == 0.0f) {
                if (n == 0 || n == 2) {
                    v.get(i-1)[3] = 0.0;
                } else {
                    v.get(i-1)[1] = 0.0;
                }
                if (n == 3) {
                    v.get(0)[3] = 0.0;
                }
                corners.put(i-1, 3-n);
                v.remove(i);
                s.remove(i);
                i--;
                n++;
            }
            else if (s.get(i) == SEG_CUBICTO) {
                corners.put(i, 3-n);
                n++;
            } else {
                corners.put(i, 3-n);
            }
        }

        ctrlpts = new double[v.size()][];
        types = new int[s.size()];

        for (int i = 0; i < v.size(); i++) {
            ctrlpts[i] = v.get(i);
            types[i] = s.get(i);
        }

    }

    /**
     * Return the winding rule for determining the insideness of the
     * path.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    @Override
    public int getWindingRule() {
        return java.awt.geom.PathIterator.WIND_NON_ZERO;
    }

    /**
     * Tests if there are more points to read.
     * @return true if there are more points to read
     */
    @Override
    public boolean isDone() {
	return index >= ctrlpts.length;
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    @Override
    public void next() {
	index++;
    }

    private static final double angle = Math.PI / 4.0;
    private static final double a = 1.0 - Math.cos(angle);
    private static final double b = Math.tan(angle);
    private static final double c = Math.sqrt(1.0 + b * b) - 1 + a;
    private static final double cv = 4.0 / 3.0 * a * b / c;
    private static final double acv = (1.0 - cv) / 2.0;

    // For each array:
    //     4 values for each point {v0, v1, v2, v3}:
    //         point = (x + v0 * w + v1 * arcWidth,
    //                  y + v2 * h + v3 * arcHeight);
    private double ctrlpts[][] = {
	{  0.0,  0.0,  0.0,  0.5 },
	{  0.0,  0.0,  1.0, -0.5 },
	{  0.0,  0.0,  1.0, -acv,
	   0.0,  acv,  1.0,  0.0,
	   0.0,  0.5,  1.0,  0.0 },
	{  1.0, -0.5,  1.0,  0.0 },
	{  1.0, -acv,  1.0,  0.0,
	   1.0,  0.0,  1.0, -acv,
	   1.0,  0.0,  1.0, -0.5 },
	{  1.0,  0.0,  0.0,  0.5 },
	{  1.0,  0.0,  0.0,  acv,
	   1.0, -acv,  0.0,  0.0,
	   1.0, -0.5,  0.0,  0.0 },
	{  0.0,  0.5,  0.0,  0.0 },
	{  0.0,  acv,  0.0,  0.0,
	   0.0,  0.0,  0.0,  acv,
	   0.0,  0.0,  0.0,  0.5 },
	{},
    };
    private int types[] = {
	SEG_MOVETO,
	SEG_LINETO, SEG_CUBICTO,
	SEG_LINETO, SEG_CUBICTO,
	SEG_LINETO, SEG_CUBICTO,
	SEG_LINETO, SEG_CUBICTO,
	SEG_CLOSE,
    };

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A float array of length 6 must be passed in and may be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types will return one point,
     * SEG_QUADTO will return two points,
     * SEG_CUBICTO will return 3 points
     * and SEG_CLOSE will not return any points.
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    @Override
    public int currentSegment(float[] coords) {
	if (isDone()) {
	    throw new NoSuchElementException("roundrect iterator out of bounds");
	}
	double ctrls[] = ctrlpts[index];
	int nc = 0;
        int n = corners.containsKey(index) ? corners.get(index) : 0;
        if (n < 0 || n > r.arcs.length-1) n = 0;
        aw = r.arcs[n];
        ah = r.arcs[n];
	for (int i = 0; i < ctrls.length; i += 4) {
	    coords[nc++] = (float) (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
	    coords[nc++] = (float) (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
	}
	if (affine != null) {
	    affine.transform(coords, 0, coords, 0, nc / 2);
	}
	return types[index];
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A double array of length 6 must be passed in and may be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types will return one point,
     * SEG_QUADTO will return two points,
     * SEG_CUBICTO will return 3 points
     * and SEG_CLOSE will not return any points.
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    @Override
    public int currentSegment(double[] coords) {
	if (isDone()) {
	    throw new NoSuchElementException("roundrect iterator out of bounds");
	}
	double ctrls[] = ctrlpts[index];
	int nc = 0;
        int n = corners.containsKey(index) ? corners.get(index) : 0;
        if (n < 0 || n > r.arcs.length-1) n = 0;
        aw = r.arcs[n];
        ah = r.arcs[n];
	for (int i = 0; i < ctrls.length; i += 4) {
	    coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
	    coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
	}
	if (affine != null) {
	    affine.transform(coords, 0, coords, 0, nc / 2);
	}
	return types[index];
    }
}
