package jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Generator  extends JSObject {

    class nextFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                block.yt_value = args.get(0);
            }
            block.eval();
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", block.return_value);
            }
            result.set("done", new JSBool(block.done));
            done = block.done;
            return result;
        }
    }

    class throwFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                block.error = new JSError(args.get(0), "Error", null);
            }
            block.eval();
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", block.return_value);
            }
            result.set("done", new JSBool(block.done));
            done = block.done;
            return result;
        }
    }

    public Generator(Block b) {
        b.is_func = true;
        b.is_gen = true;
        block = b;
        items.put("next", new nextFunction());
        items.put("throw", new throwFunction());
    }

    public boolean isDone() {
        return done;
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

    private Block block = null;
    private boolean done = false;
    private String type = "Object";
}
