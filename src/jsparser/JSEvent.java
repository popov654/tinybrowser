package jsparser;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSEvent extends JSObject {

    public JSEvent(JSElement target, HashMap<String, String> data) {
        this.data = new TreeMap<String, JSValue>();
        this.data.put("cancelBubble", new JSBool(false));
        this.data.put("defaultPrevented", new JSBool(false));
        this.data.put("target", target);
        Set<String> keys = data.keySet();
        for (String key: keys) {
            String type = JSValue.getType(data.get(key));
            JSValue val = JSValue.create(type, data.get(key));
            this.data.put(key, val);
        }
    }

    class stopPropagationFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            data.put("cancelBubble", new JSBool(true));
            return new JSBool(true);
        }
    }

    class preventDefaultFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            data.put("defaultPrevented", new JSBool(true));
            return new JSBool(true);
        }
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    TreeMap<String, JSValue> data;
}
