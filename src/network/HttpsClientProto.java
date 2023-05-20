package network;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import jsparser.Function;
import jsparser.JSObject;
import jsparser.JSString;
import jsparser.JSValue;
import jsparser.Null;
import jsparser.Undefined;

/**
 *
 * @author Alex
 */
public class HttpsClientProto extends JSObject {
    
    class openFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 3) {
                return Undefined.getInstance();
            }
            ((HttpsClient)context).setUrl(args.get(1).asString().getValue());
            ((HttpsClient)context).setMethod(args.get(0).asString().getValue());
            ((HttpsClient)context).setAsync(args.get(2).asBool().getValue());
            return Undefined.getInstance();
        }
    }

    class sendFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0 && !args.get(0).equals(Null.getInstance())) {
                ((HttpsClient)context).setData(args.get(0).asString().getValue());
            }
            ((HttpsClient)context).start();
            return Undefined.getInstance();
        }
    }

    private HttpsClientProto() {
        items.put("open", new openFunction());
        items.put("send", new sendFunction());
    }

    public static HttpsClientProto getInstance() {
        if (instance == null) {
            instance = new HttpsClientProto();
        }
        return instance;
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
        return "XMLHttpRequest {" + result + "}";
    }

    @Override
    public String getType() {
        return type;
    }

    private String type = "Object";
    private static HttpsClientProto instance = null;
    
}
