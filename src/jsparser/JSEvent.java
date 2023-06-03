package jsparser;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSEvent extends JSObject {

    public JSEvent(JSObject target, String eventType, HashMap<String, String> data) {
        items.put("type", new JSString(eventType));
        items.put("cancelBubble", new JSBool(false));
        items.put("defaultPrevented", new JSBool(false));
        items.put("target", target);
        items.put("stopPropagation", new stopPropagationFunction(this));
        items.put("preventDefault", new preventDefaultFunction(this));
        if (data == null) return;
        Set<String> keys = data.keySet();
        for (String key: keys) {
            String type = JSValue.getType(data.get(key));
            JSValue val = JSValue.create(type, data.get(key));
            items.put(key, val);
        }
    }

    public JSEvent(HTMLElement target, HTMLElement relatedTarget, HashMap<String, String> data) {
        items.put("cancelBubble", new JSBool(false));
        items.put("defaultPrevented", new JSBool(false));
        items.put("target", target);
        items.put("relatedTarget", relatedTarget != null ? relatedTarget : Null.getInstance());
        items.put("stopPropagation", new stopPropagationFunction(this));
        items.put("preventDefault", new preventDefaultFunction(this));
        if (data == null) return;
        Set<String> keys = data.keySet();
        for (String key: keys) {
            String type = JSValue.getType(data.get(key));
            JSValue val = JSValue.create(type, data.get(key));
            items.put(key, val);
        }
    }

    class stopPropagationFunction extends Function {

        stopPropagationFunction(JSEvent e) {
            event = e;
        }
        
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            event.items.put("cancelBubble", new JSBool(true));
            return new JSBool(true);
        }

        JSEvent event;
    }

    class preventDefaultFunction extends Function {

        preventDefaultFunction(JSEvent e) {
            event = e;
        }

        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            event.items.put("defaultPrevented", new JSBool(true));
            ((HTMLElement)event.items.get("target")).node.defaultPrevented = true;
            return new JSBool(true);
        }

        JSEvent event;
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    @Override
    public String toString() {
        return "Event " + super.toString();
    }

}
