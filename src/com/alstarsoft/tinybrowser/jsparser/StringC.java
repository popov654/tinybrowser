package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class StringC extends Function {

    public StringC() {
        items.put("prototype", StringProto.getInstance());
        StringProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 0) return new JSString("");
        String str = args.get(0).getType().equals("String") ?
            ((JSString)args.get(0)).getValue() : args.get(0).toString();
        return new JSString(str);
    }

}
