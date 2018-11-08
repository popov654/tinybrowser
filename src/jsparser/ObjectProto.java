package jsparser;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Alex
 */
public class ObjectProto extends JSObject {

    private ObjectProto() {
        items.put("hasOwnProperty", new hasOwnPropertyFunction());
        items.put("toString", new toStringFunction());
    }

    public static ObjectProto getInstance() {
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

    private String type = "Object";
    private static ObjectProto instance = new ObjectProto();
}
