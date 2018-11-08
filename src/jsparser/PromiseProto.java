package jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class PromiseProto extends JSObject {

    class thenFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 1 && args.get(0).getType().equals("Function")) {
                return ((Promise)context).then((Function)args.get(0));
            } else if (args.size() > 1 && args.get(0).getType().equals("Function") &&
                       args.get(1).getType().equals("Function")) {
                return ((Promise)context).then((Function)args.get(0), (Function)args.get(1));
            } else if (args.size() > 1 && args.get(0).getType().equals("null") &&
                       args.get(1).getType().equals("Function")) {
                return ((Promise)context)._catch((Function)args.get(1));
            }
            return context;
        }
    }

    class catchFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0 && args.get(0).getType().equals("Function")) {
                return ((Promise)context)._catch((Function)args.get(0));
            }
            return context;
        }
    }

    private PromiseProto() {
        items.put("then", new thenFunction());
        items.put("catch", new catchFunction());
    }

    public static PromiseProto getInstance() {
        if (instance == null) {
            instance = new PromiseProto();
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
    private static PromiseProto instance = null;
}
