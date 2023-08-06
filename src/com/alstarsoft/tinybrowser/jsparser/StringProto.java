package com.alstarsoft.tinybrowser.jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class StringProto  extends JSObject {

    class sliceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSString)context).slice();
            } else if (args.size() == 1) {
                return ((JSString)context).slice(args.get(0).asInt());
            } else {
                return ((JSString)context).slice(args.get(0).asInt(), args.get(1).asInt());
            }
        }
    }

    class splitFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSString)context).split();
            } else {
                return ((JSString)context).split(args.get(0).asString());
            }
        }
    }

    class toUpperCaseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((JSString)context).toUpperCase();
        }
    }

    class toLowerCaseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((JSString)context).toLowerCase();
        }
    }

    class charAtFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSInt index = new JSInt(0);
            if (args.size() > 0) index = args.get(0).asInt();
            return ((JSString)context).charAt(index);
        }
    }

    private StringProto() {
        items.put("slice", new sliceFunction());
        items.put("split", new splitFunction());
        items.put("toUpperCase", new toUpperCaseFunction());
        items.put("toLowerCase", new toLowerCaseFunction());
        items.put("charAt", new charAtFunction());
    }

    public static StringProto getInstance() {
        if (instance == null) {
            instance = new StringProto();
        }
        return instance;
    }

    @Override
    public JSValue get(JSString str) {
        JSValue val = items.get(str.getValue());
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public JSValue get(String str) {
        JSValue val = items.get(str);
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    @Override
    public String toString() {
        String result = "";
        Set keys = items.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            if (result.length() > 0) result += ", ";
            String str = (String)it.next();
            result += str + ": " + items.get(str).toString();
        }
        return "{" + result + "}";
    }

    @Override
    public String getType() {
        return type;
    }

    private String type = "Object";
    private static StringProto instance = null;
}

