package com.alstarsoft.tinybrowser.network;

import java.util.Vector;
import com.alstarsoft.tinybrowser.jsparser.Function;
import com.alstarsoft.tinybrowser.jsparser.JSObject;
import com.alstarsoft.tinybrowser.jsparser.JSValue;

/**
 *
 * @author Alex
 */
public class HttpsClientC extends Function {
    
    public HttpsClientC() {
        items.put("prototype", HttpsClientProto.getInstance());
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        return new HttpsClient();
    }

}
