package jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FunctionProto extends JSObject {

    class callFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue ctx = args.remove(0);
            if (!(ctx instanceof JSObject)) {
                ctx = new JSObject();
            }
            return context.call((JSObject)ctx, args);
        }
    }

    class applyFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue ctx = args.remove(0);
            Vector<JSValue> args2 = new Vector<JSValue>();
            if (!args.isEmpty() && args.get(0) instanceof JSArray) {
                args2 = ((JSArray)args.get(0)).getItems();
            }
            if (!(ctx instanceof JSObject)) {
                ctx = new JSObject();
            }
            return context.call((JSObject)ctx, args2);
        }
    }

    class bindFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return context;
            JSValue ctx = args.remove(0);
            return ((Function)context).bind((JSObject)ctx, args);
        }
    }

    private FunctionProto() {
        items.put("call", new callFunction());
        items.put("apply", new applyFunction());
        items.put("bind", new bindFunction());
    }

    public static FunctionProto getInstance() {
        if (instance == null) {
            instance = new FunctionProto();
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
    private static FunctionProto instance = null;
}
