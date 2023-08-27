package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class RegExpC extends Function {

    public RegExpC() {
        items.put("prototype", RegExpProto.getInstance());
        RegExpProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() < 1 && args.get(0).getType().equals("Integer")) {
            return new RegExp("");
        } else {
            RegExp result = new RegExp(args.get(0).asString().getValue());
            if (args.size() > 1) {
                result.setFlags(args.get(1).asString().getValue().toLowerCase());
            }
            return result;
        }
    }
}
