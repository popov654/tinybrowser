package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSFloat extends JSObject implements Comparable {
    public JSFloat(String val) {
        items.put("__proto__", NumberProto.getInstance());
        try {
            value = Double.parseDouble(val);
        } catch(NumberFormatException e) {
            value = 0;
        }
    }
    public JSFloat(int val) {
        items.put("__proto__", ObjectProto.getInstance());
        value = (float)val;
    }
    public JSFloat(float val) {
        items.put("__proto__", ObjectProto.getInstance());
        value = val;
    }
    public JSFloat(double val) {
        items.put("__proto__", ObjectProto.getInstance());
        value = val;
    }

    public boolean isNaN() {
        return false;
    }

    @Override
    public JSBool asBool() {
        return new JSBool(value != 0);
    }

    @Override
    public JSInt asInt() {
        return new JSInt(value);
    }

    @Override
    public JSFloat asFloat() {
        return new JSFloat(value);
    }

    @Override
    public JSString asString() {
        return new JSString(String.valueOf(value));
    }

    public double getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj.equals(Infinity.getInstance(true))) {
            return -1;
        }
        if (obj.equals(Infinity.getInstance(false))) {
            return 1;
        }
        JSFloat f = (obj instanceof JSFloat) ? (JSFloat)obj : ((JSValue)obj).asFloat();
        if (value - f.getValue() < 0.00001) return 0;
        return value - f.getValue() > 0 ? 1 : -1;
    }

    @Override
    public JSFloat clone() {
        return new JSFloat(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private double value;
    private String type = "Float";
}
