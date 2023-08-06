package com.alstarsoft.tinybrowser.network;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import com.alstarsoft.tinybrowser.jsparser.Function;
import com.alstarsoft.tinybrowser.jsparser.JSObject;
import com.alstarsoft.tinybrowser.jsparser.JSString;
import com.alstarsoft.tinybrowser.jsparser.JSValue;
import com.alstarsoft.tinybrowser.jsparser.FormData;
import com.alstarsoft.tinybrowser.jsparser.Null;
import com.alstarsoft.tinybrowser.jsparser.Undefined;

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
                if (!(args.get(0) instanceof FormData)) {
                    ((HttpsClient)context).setData(args.get(0).asString().getValue());
                } else {
                    ((HttpsClient)context).setData((FormData) args.get(0));
                }
            }
            ((HttpsClient)context).start();
            return Undefined.getInstance();
        }
    }

    private HttpsClientProto() {
        items.put("onloadstart", Null.getInstance());
        items.put("onprogress", Null.getInstance());
        items.put("onabort", Null.getInstance());
        items.put("onerror", Null.getInstance());
        items.put("onload", Null.getInstance());
        items.put("ontimeout", Null.getInstance());
        items.put("onloadend", Null.getInstance());
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
