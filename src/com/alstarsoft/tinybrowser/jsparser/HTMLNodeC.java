package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLNodeC extends Function {

    public HTMLNodeC() {
        items.put("prototype", HTMLNodeProto.getInstance());
        HTMLNodeProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        JSError e = new JSError(null, "Invocation error: cannot create nodes directly", getCaller().getStack());
        getCaller().error = e;
        return Undefined.getInstance();
    }

    public static HTMLNodeC getInstance() {
        if (instance == null) {
            instance = new HTMLNodeC();
        }
        return instance;
    }

    private static HTMLNodeC instance;

}

