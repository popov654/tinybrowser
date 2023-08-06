package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FunctionC extends Function {

    public FunctionC() {
        items.put("prototype", FunctionProto.getInstance());
        FunctionProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        if (args.size() >= 1 && args.lastElement().getType().equals("String")) {
            JSParser jp = new JSParser(((JSString)args.lastElement()).getValue());
            Block b = (Block)Expression.create(jp.getHead());
            b.parent_block = ((Window)context).getRoot();
            Vector<String> p = new Vector<String>();
            for (int i = 0; i < args.size()-1; i++) {
                if (args.get(i).getType().equals("String")) {
                    p.add(((JSString)args.get(i)).getValue());
                }
            }
            return new Function(p, b, "anonymous");
        }
        Block b = new Block();
        b.parent_block = ((Window)context).getRoot();
        return new Function(new Vector<String>(), b, "anonymous");
    }
}
