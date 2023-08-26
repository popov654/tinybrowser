package com.alstarsoft.tinybrowser.jsparser;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSObjectProto extends JSObject {

    private JSObjectProto() {
        items.put("entries", new entriesFunction());
        items.put("propertyIsEnumerable", new propertyIsEnumerableFunction());
        items.put("hasOwnProperty", new hasOwnPropertyFunction());
        items.put("toString", new toStringFunction());
    }

    public static JSObjectProto getInstance() {
        return instance;
    }

    @Override
    public JSValue get(JSString str) {
        return get(str.getValue());
    }

    @Override
    public JSValue get(String str) {
        JSValue val = items.get(str);
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public String toString() {
        String result = "";
        Set keys = items.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (!print_proto && !print_protos && str.equals("__proto__")) continue;
            if (result.length() > 0) result += ", ";
            result += str + ": " + (!str.equals("__proto__") ? items.get(str).toString() : "ObjectPrototype");
        }
        return "{" + result + "}";
    }

    @Override
    public String getType() {
        return type;
    }

    class entriesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Vector<JSValue> v = new Vector<JSValue>();
            Set<Entry<String, JSValue>> entries = context.items.entrySet();
            for (Entry<String, JSValue> entry: entries) {
                if (!context.hasOwnProperty(entry.getKey()) || entry.getKey().equals("__proto__")) {
                    continue;
                }
                JSArray a = new JSArray();
                a.push(new JSString(entry.getKey()));
                a.push(entry.getValue());
                v.add(a);
            }
            return new JSArray(v);
        }
    }

    class propertyIsEnumerableFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Function f = (Function) ((JSObject) Expression.getVar("Object", getCaller())).get("propertyIsEnumerable");
            args.add(0, context);
            return f.call(args);
        }
    }

    class hasOwnPropertyFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSBool(((JSObject)context).hasOwnProperty(args.get(0).asString()));
        }
    }

    class toStringFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSString(((JSObject)context).toString());
        }
    }

    private String type = "Object";
    private static JSObjectProto instance = new JSObjectProto();
}
