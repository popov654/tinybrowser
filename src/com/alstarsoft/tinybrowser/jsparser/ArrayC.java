package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class ArrayC extends Function {

    public ArrayC() {
        items.put("prototype", ArrayProto.getInstance());
        ArrayProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            Vector<JSValue> v = new Vector<JSValue>();
            int length = (int)((JSInt)args.get(0)).getValue();
            for (int i = 0; i < length; i++) {
                v.add(Undefined.getInstance());
            }
            return new JSArray(v);
        } else {
            return new JSArray(args);
        }
    }

}
