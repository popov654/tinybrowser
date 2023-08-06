package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLElementC extends Function {

    public HTMLElementC() {
        items.put("prototype", HTMLElementProto.getInstance());
        HTMLElementProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        JSError e = new JSError(null, "Invocation error: cannot create elements directly", getCaller().getStack());
        getCaller().error = e;
        return Undefined.getInstance();
    }

}
