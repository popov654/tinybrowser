package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class PromiseC extends Function {

    public PromiseC() {
        items.put("prototype", PromiseProto.getInstance());
        items.put("resolve", new resolveFunction());
        items.put("all", new allFunction());
        items.put("race", new raceFunction());
        PromiseProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 0) return new Promise(null);

        Window w = (Window)Block.getVar("window", getCaller());
        if (w == null) {
            Block b = getCaller().parent_block;
            while (b.parent_block != null) {
                b = b.parent_block;
            }
            w = new Window(b);
            Expression.setVar("window", w, b, Block.var);
        }

        if (!args.get(0).getType().equals("Function")) {
            JSError e = new JSError(null, "Type error: argument is not a function", getCaller().getStack());
            getCaller().error = e;
            Promise p = new Promise(null);
            w.addPromise(p);
            return p;
        }
        Promise p = new Promise((Function)args.get(0));
        w.addPromise(p);
        return p;
    }

    class resolveFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Promise p = new Promise(null);
            if (args.size() > 0) p.setResult(args.get(0));
            p.setState(Promise.FULFILLED);
            Window w = (Window)Block.getVar("window", getCaller());
            w.addPromise(p);
            return p;
        }
    }

    class allFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !(args.get(0) instanceof JSArray)) {
                JSError e = new JSError(null, "Type error: argument 1 is not an array", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Vector<JSValue> promises = ((JSArray)args.get(0)).getItems();
            Promise p = new PromiseAdv(null, promises, 0);
            Window w = (Window)Block.getVar("window", getCaller());
            w.addPromise(p);
            return p;
        }
    }

    class raceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !(args.get(0) instanceof JSArray)) {
                JSError e = new JSError(null, "Type error: argument 1 is not an array", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Vector<JSValue> promises = ((JSArray)args.get(0)).getItems();
            Promise p = new PromiseAdv(null, promises, 1);
            Window w = (Window)Block.getVar("window", getCaller());
            w.addPromise(p);
            return p;
        }
    }
}
