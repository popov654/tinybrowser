package com.alstarsoft.tinybrowser.jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class ArrayProto extends JSObject {

    class pushFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSArray)context).get((int)((JSArray)context).length().getValue()-1);
            } else {
                for (JSValue value: args) {
                    ((JSArray)context).push(value);
                }
                return ((JSArray)context).length();
            }
        }
    }

    class popFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue v = ((JSArray)context).get((int)((JSArray)context).length().getValue()-1);
            ((JSArray)context).pop();
            return v;
        }
    }

    class shiftFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue v = ((JSArray)context).get(0);
            ((JSArray)context).shift();
            return v;
        }
    }

    class unshiftFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSArray)context).length();
            }
            for (JSValue value: args) {
                ((JSArray)context).unshift(value);
            }
            return ((JSArray)context).length();
        }
    }

    class fillFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                int len = (int)((JSArray)context).length().getValue();
                for (int i = 0; i < len; i++) {
                    ((JSArray)context).set(i, args.get(0));
                }
            }
            return context;
        }
    }

    class concatFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0 && args.get(0).getType().equals("Array")) {
                return ((JSArray)context).concat((JSArray)args.get(0));
            }
            return ((JSArray)context).slice();
        }
    }

    class sliceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSArray)context).slice();
            } else if (args.size() == 1) {
                return ((JSArray)context).slice(args.get(0).asInt());
            } else {
                return ((JSArray)context).slice(args.get(0).asInt(), args.get(1).asInt());
            }
        }
    }

    class spliceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSArray)context).slice();
            } else if (args.size() == 1) {
                JSInt count = new JSInt(((JSArray)context).length().getValue() - args.get(0).asInt().getValue());
                return ((JSArray)context).splice(args.get(0).asInt(), count, new Vector<JSValue>());
            } else {
                Vector<JSValue> ins = new Vector<JSValue>(args.subList(2, args.size()));
                return ((JSArray)context).splice(args.get(0).asInt(), args.get(1).asInt(), ins);
            }
        }
    }

    class sortFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((JSArray)context).sort();
        }
    }

    class reverseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray a = (JSArray)context;
            JSArray b = new JSArray();
            for (int i = (int)a.length().getValue()-1; i >= 0; i--) {
                b.push(a.get(i));
            }
            return b;
        }
    }

    class joinFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSArray)context).join();
            } else {
                return ((JSArray)context).join(args.get(0).asString());
            }
        }
    }

    class mapFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !args.get(0).getType().equals("Function")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            } else {
                JSArray a = ((JSArray)context).clone();
                Function f = (Function)args.get(0);
                for (int i = 0; i < a.length().getValue(); i++) {
                    Vector<JSValue> p = new Vector<JSValue>();
                    p.add(a.get(i));
                    p.add(new JSInt(i));
                    p.add(context);
                    JSObject ctx = args.size() > 1 && args.get(1).getType().equals("Object") ?
                                      (JSObject)args.get(1) : null;
                    a.set(i, f.call(ctx, p));
                }
                return a;
            }
        }
    }

    class filterFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !args.get(0).getType().equals("Function")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            } else {
                JSArray a = (JSArray)context;
                JSArray b = new JSArray();
                Function f = (Function)args.get(0);
                for (int i = 0; i < a.length().getValue(); i++) {
                    Vector<JSValue> p = new Vector<JSValue>();
                    p.add(a.get(i));
                    p.add(new JSInt(i));
                    p.add(context);
                    JSObject ctx = args.size() > 1 && args.get(1).getType().equals("Object") ?
                                      (JSObject)args.get(1) : null;
                    if (f.call(ctx, p).asBool().getValue()) {
                        b.push(a.get(i));
                    }
                }
                return b;
            }
        }
    }

    class forEachFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !args.get(0).getType().equals("Function")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
            } else {
                JSArray a = (JSArray)context;
                Function f = (Function)args.get(0);
                for (int i = 0; i < a.length().getValue(); i++) {
                    Vector<JSValue> p = new Vector<JSValue>();
                    p.add(a.get(i));
                    p.add(new JSInt(i));
                    p.add(context);
                    JSObject ctx = args.size() > 1 && args.get(1).getType().equals("Object") ?
                                      (JSObject)args.get(1) : null;
                    f.call(ctx, p);
                }
            }
            return Undefined.getInstance();
        }
    }

    class reduceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !args.get(0).getType().equals("Function")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            } else {
                JSArray a = (JSArray)context;
                Function f = (Function)args.get(0);
                JSValue result = Undefined.getInstance();
                if (a.length().getValue() == 0 && args.size() == 1) {
                    JSError e = new JSError(null, "Type error: reduce of empty array with no initial value", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                int i = 0;
                if (args.size() == 1) {
                    result = a.get(i);
                    i++;
                } else {
                    result = args.get(1);
                }
                for ( ; i < a.length().getValue(); i++) {
                    if (a.get(i) == Undefined.getInstance()) continue;
                    Vector<JSValue> p = new Vector<JSValue>();
                    p.add(result);
                    p.add(a.get(i));
                    p.add(new JSInt(i));
                    p.add(context);
                    result = f.call(null, p);
                }
                return result;
            }
        }
    }

    class reduceRightFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !args.get(0).getType().equals("Function")) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            } else {
                JSArray a = (JSArray)context;
                Function f = (Function)args.get(0);
                JSValue result = Undefined.getInstance();
                if (a.length().getValue() == 0 && args.size() == 1) {
                    JSError e = new JSError(null, "Type error: reduce of empty array with no initial value", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                int i = (int)a.length().getValue()-1;
                if (args.size() == 1) {
                    result = a.get(i);
                    i--;
                } else {
                    result = args.get(1);
                }
                for ( ; i >= 0; i--) {
                    if (a.get(i) == Undefined.getInstance()) continue;
                    Vector<JSValue> p = new Vector<JSValue>();
                    p.add(result);
                    p.add(a.get(i));
                    p.add(new JSInt(i));
                    p.add(context);
                    result = f.call(null, p);
                }
                return result;
            }
        }
    }

    private ArrayProto() {
        items.put("push", new pushFunction());
        items.put("pop", new popFunction());
        items.put("shift", new shiftFunction());
        items.put("unshift", new unshiftFunction());
        items.put("fill", new fillFunction());
        items.put("concat", new concatFunction());
        items.put("slice", new sliceFunction());
        items.put("splice", new spliceFunction());
        items.put("sort", new sortFunction());
        items.put("reverse", new reverseFunction());
        items.put("join", new joinFunction());
        items.put("map", new mapFunction());
        items.put("filter", new filterFunction());
        items.put("reduce", new reduceFunction());
        items.put("reduceRight", new reduceRightFunction());
    }

    public static ArrayProto getInstance() {
        if (instance == null) {
            instance = new ArrayProto();
        }
        return instance;
    }

    @Override
    public void set(JSString str, JSValue value) {
        set(str.getValue(), value);
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.equals("constructor")) {
            super.set(str, value);
        }
    }

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
    private static ArrayProto instance = null;
}
