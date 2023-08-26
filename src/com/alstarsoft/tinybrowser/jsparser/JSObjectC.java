package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSObjectC extends Function {

    public JSObjectC() {
        items.put("prototype", JSObjectProto.getInstance());
        JSObjectProto.getInstance().set("constructor", this);
        addMethods();
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() >= 1) {
            JSValue val = args.get(0);
            String type = val.getType();
            if (type.equals("Float")) return new JSFloat(((JSFloat)val).getValue());
            if (type.equals("Integer")) return new JSInt(((JSInt)val).getValue());
            if (type.matches("Array|String|Object|Function")) return val;
            else if (val instanceof JSObject) {
                JSObject obj = (JSObject)val;
                while (obj != null && obj.hasOwnProperty("_proto_") && !type.equals("Object")) {
                    obj = (JSObject)obj.get("_proto_");
                    if (obj != null) {
                        type = obj.getType();
                    }
                }
                if (type.equals("Object")) return val;
            } else {
                return new JSObject();
            }
        }
        return new JSObject();
    }

    private void addMethods() {
        final JSObject instance = this;
        items.put("freeze", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() == 0 || !(args.get(0) instanceof JSObject)) {
                    return Undefined.getInstance();
                }
                ((JSObject)args.get(0)).isFrozen = true;
                return Undefined.getInstance();
            }
        });
        items.put("isFrozen", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() == 0 || !(args.get(0) instanceof JSObject)) {
                    new JSBool(false);
                }
                return new JSBool(context.isFrozen);
            }
        });
        items.put("defineProperty", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() < 2) {
                    JSError e = new JSError(null, "Arguments error: at least 2 arguments expected", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!(args.get(0) instanceof JSObject)) {
                    JSError e = new JSError(null, "Type error: argument 1 must be an Object", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!args.get(1).getType().equals("String")) {
                    JSError e = new JSError(null, "Type error: argument 2 must be a String", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (args.size() > 2 && !(args.get(2) instanceof JSObject)) {
                    JSError e = new JSError(null, "Type error: argument 3 must be an Object", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }

                JSObject ctx = (JSObject) args.get(0);
                JSObject descriptor = args.size() > 2 ? (JSObject)args.get(2) : new JSObject();
                ctx.defineCustomProperty(args.get(1).asString().getValue(), descriptor);
                
                return Undefined.getInstance();
            }
        });
        items.put("removeProperty", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() < 2) {
                    JSError e = new JSError(null, "Arguments error: at least 2 arguments expected", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!(args.get(0) instanceof JSObject)) {
                    JSError e = new JSError(null, "Type error: argument 1 must be an Object", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!args.get(1).getType().equals("String")) {
                    JSError e = new JSError(null, "Type error: argument 2 must be a String", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }

                JSObject ctx = (JSObject) args.get(0);
                ctx.removeProperty(args.get(1).asString().getValue());

                return Undefined.getInstance();
            }
        });
        items.put("propertyIsEnumerable", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() < 2) {
                    JSError e = new JSError(null, "Arguments error: at least 2 arguments expected", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!(args.get(0) instanceof JSObject)) {
                    JSError e = new JSError(null, "Type error: argument 1 must be an Object", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!args.get(1).getType().equals("String")) {
                    JSError e = new JSError(null, "Type error: argument 2 must be a String", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }

                JSObject ctx = (JSObject) args.get(0);
                CustomProperty p = ctx.customProperties.get(args.get(1).asString().getValue());

                return new JSBool(p.enumerable);
            }
        });
    }

}
