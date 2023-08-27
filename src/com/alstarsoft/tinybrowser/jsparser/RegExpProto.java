package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class RegExpProto extends JSObject {

    private RegExpProto() {
        items.put("test", new testFunction());
        items.put("exec", new execFunction());
    }

    class testFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("String")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a string", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return new JSBool(((RegExp)context).test(args.get(0).asString().getValue()));
        }
    }

    class execFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("String")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a string", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return ((RegExp)context).exec(args.get(0).asString().getValue());
        }
    }

    public static RegExpProto getInstance() {
        if (instance == null) {
            instance = new RegExpProto();
        }
        return instance;
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getType() {
        return type;
    }

    private String type = "Object";
    private static RegExpProto instance = null;
}
