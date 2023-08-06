/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.alstarsoft.tinybrowser.jsparser;

/**
 *
 * @author Alex
 */
public class Infinity extends JSObject implements Comparable {

    private Infinity(boolean sign) {
        items.put("__proto__", NumberProto.getInstance());
        positive = sign;
    }

    public boolean isNaN() {
        return false;
    }

    @Override
    public JSBool asBool() {
        return new JSBool(true);
    }

    @Override
    public JSInt asInt() {
        return null;
    }

    @Override
    public JSFloat asFloat() {
        return null;
    }

    @Override
    public JSString asString() {
        return new JSString((positive ? "+" : "-") + String.valueOf(value));
    }

    @Override
    public String toString() {
        return (positive ? "+" : "-") + String.valueOf(value);
    }

    @Override
    public String getType() {
        return type;
    }

    public int compareTo(Object obj) {
        if (obj.equals(Infinity.getInstance(positive))) {
            return 0;
        }
        return positive ? 1 : -1;
    }

    public boolean isPositive() {
        return positive;
    }

    public static Infinity getInstance(boolean sign) {
        return sign ? infPlus : infMinus;
    }

    private static Infinity infPlus = new Infinity(true);
    private static Infinity infMinus = new Infinity(false);

    private boolean positive = true;
    private String value = "Infinity";
    private String type = "Number";

}
