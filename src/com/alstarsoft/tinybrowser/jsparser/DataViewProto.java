package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class DataViewProto extends JSObject {

    class getUint8Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            return ((DataView)context).getUint8((int)args.get(0).asInt().getValue());
        }
    }

    class getUint16Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            return ((DataView)context).getUint16((int)args.get(0).asInt().getValue());
        }
    }

    class getUint32Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            return ((DataView)context).getUint32((int)args.get(0).asInt().getValue());
        }
    }

    class getFloat64Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            return ((DataView)context).getFloat64((int)args.get(0).asFloat().getValue());
        }
    }

    class setUint8Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments error: 2 arguments expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 2 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            ((DataView)context).setUint8((int)args.get(0).asInt().getValue(), args.get(1).asInt());
            
            return Undefined.getInstance();
        }
    }

    class setUint16Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments error: 2 arguments expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 2 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            ((DataView)context).setUint16((int)args.get(0).asInt().getValue(), args.get(1).asInt());

            return Undefined.getInstance();
        }
    }

    class setUint32Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments error: 2 arguments expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 2 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            ((DataView)context).setUint32((int)args.get(0).asInt().getValue(), args.get(1).asInt());

            return Undefined.getInstance();
        }
    }

    class setFloat64Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments error: 2 arguments expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(1).getType().equals("Float") && !args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 2 must be a number value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            
            ((DataView)context).setFloat64((int)args.get(0).asInt().getValue(), args.get(1).asFloat());

            return Undefined.getInstance();
        }
    }

    private DataViewProto() {
        items.put("__proto__", ArrayProto.getInstance());
        items.put("getUint8", new getUint8Function());
        items.put("getUint16", new getUint16Function());
        items.put("getUint32", new getUint32Function());
        items.put("getFloat64", new getFloat64Function());
        items.put("setUint8", new setUint8Function());
        items.put("setUint16", new setUint16Function());
        items.put("setUint32", new setUint32Function());
        items.put("setFloat64", new setFloat64Function());
    }

    public static DataViewProto getInstance() {
        if (instance == null) {
            instance = new DataViewProto();
        }
        return instance;
    }

    private String type = "Object";
    private static DataViewProto instance = null;

}
