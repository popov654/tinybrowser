package com.alstarsoft.tinybrowser.jsparser;

import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public abstract class JSValue {

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "null";
    }

    public static String getType(String value) {
        if (value.equals("[]")) {
            return "Array";
        }
        if (value.equals("{}")) {
            return "Object";
        }
        if (value.replaceAll("\n", "").matches("^([\"'`]).*\\1$")) {
            return "String";
        }
        if (value.matches("^[+-]?(0x[0-9a-fA-F]+|0?[0-9]+)$")) {
            return "Integer";
        }
        if (value.matches("^[+-]?[0-9]+(\\.[0-9]+([eE]-?[0-9]+)?)?$")) {
            return "Float";
        }
        if (value.toLowerCase().matches("^(true|false)$")) {
            return "Boolean";
        }
        if (value.matches("^[+-]?Infinity$")) {
            return "Number";
        }
        if (value.equals("NaN")) {
            return "NaN";
        }
        if (value.toLowerCase().equals("null")) {
            return "null";
        }
        if (value.toLowerCase().equals("undefined")) {
            return "undefined";
        }
        return "null";
    }

    public static JSValue create(String type, String value, Block block) {
        JSValue val = null;
        if (type.charAt(0) == 'S') {
            if (value.replaceAll("\n", "").matches("^\".*\"$")) {
                value = value.substring(1, value.length()-1);
            }
            val = new JSString(value);
        }
        if (type.charAt(0) == 'I') {
            if (value.toLowerCase().equals("true")) val = new JSInt(1);
            else if (value.toLowerCase().equals("false")) val = new JSInt(0);
            else val = new JSInt(value);
        }
        if (type.charAt(0) == 'F') {
            if (value.toLowerCase().equals("true")) val = new JSFloat(1);
            else if (value.toLowerCase().equals("false")) val = new JSFloat(0);
            else val = new JSFloat(value);
        }
        if (type.charAt(0) == 'B') {
            val = new JSBool(value);
        }
        if (type.equals("Number")) {
            if (value.equals("NaN")) val = NaN.getInstance();
            else val = Infinity.getInstance(value.charAt(0) != '-');
        }
        else if (type.charAt(0) == 'N') {
            val = NaN.getInstance();
        }
        if (value.matches("/.*/[a-z]*")) {
            String pattern = value.substring(1, value.lastIndexOf('/'));
            String flags = value.substring(value.lastIndexOf('/') + 1);
            RegExp regexp = new RegExp(pattern);
            regexp.setFlags(flags);
            val = regexp;
        }
        if (type.equals("null")) {
            val = Null.getInstance();
        }
        if (type.equals("undefined")) {
            val = Undefined.getInstance();
        }
        if (val != null) {
            val.block = block;
        }
        return val;
    }

    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        System.err.println("The instance is not callable");
        return Undefined.getInstance();
    }

    public JSValue call(JSObject context, Vector<JSValue> args) {
        return call(context, args, false);
    }

    public JSValue call(Vector<JSValue> args) {
        return call(null, args);
    }

    @Override
    public JSValue clone() {
        return create(toString(), null, null);
    }

    public void incrementRefCount() {
        if (this instanceof Null || this instanceof Undefined) {
            return;
        }
        ref_count++;
    }

    public void decrementRefCount() {
        if (this instanceof Null || this instanceof Undefined) {
            return;
        }
        ref_count--;
        if (ref_count <= 0) {
            if (this instanceof JSArray) {
                JSArray obj = (JSArray) this;
                Set<String> keys = obj._items.keySet();
                for (String key: keys) {
                    JSValue val = obj._items.get(key);
                    if (val != null) {
                        val.decrementRefCount();
                    }
                }
                for (int i = 0; i < obj.items.size(); i++) {
                    obj.items.get(i).decrementRefCount();
                }
                obj._items.clear();
                obj.items.clear();
            }
            if (this instanceof JSObject && !(this instanceof HTMLNode)) {
                JSObject obj = (JSObject) this;
                Set<String> keys = obj.items.keySet();
                for (String key: keys) {
                    if (key.matches("__proto__|constructor")) continue;
                    System.out.println(key);
                    JSValue val = obj.items.get(key);
                    if (val != null && val != this) {
                        val.decrementRefCount();
                    }
                }
                obj.items.clear();
            }
        }
    }

    public abstract JSString asString();
    public abstract JSInt asInt();
    public abstract JSFloat asFloat();
    public abstract JSBool asBool();

    public int ref_count = 0;
    public Block block;

    private String type;
}
