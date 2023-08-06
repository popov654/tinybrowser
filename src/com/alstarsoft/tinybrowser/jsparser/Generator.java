package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Generator extends JSObject {

    class nextFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                block.yt_value = args.get(0);
            } else {
                Expression last_exp = block.last > -1 ? block.children.get(block.last) : null;
                if (last_exp != null && last_exp.yt != null && last_exp.yt != last_exp.start && !(last_exp.yt.val instanceof Generator)) {
                    block.yt_value = last_value;
                    last_exp.yt = null;
                } else if (last_exp != null && last_exp.yt != null && last_exp.yt.prev == last_exp.start) {
                    block.yt_value = null;
                }
            }
            block.eval();
            last_value = block.return_value;
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", block.return_value);
            }
            result.set("done", new JSBool(block.done));
            done = block.done;
            return result;
        }
    }

    class throwFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                block.error = new JSError(args.get(0), "Error", null);
            }
            block.eval();
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", block.return_value);
            }
            result.set("done", new JSBool(block.done));
            done = block.done;
            return result;
        }
    }

    public Generator(Block b) {
        b.is_func = true;
        b.is_gen = true;
        block = b;
        items.put("next", new nextFunction());
        items.put("throw", new throwFunction());
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void set(JSString str, JSValue value) {
        set(str.getValue(), value);
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.equals("constructor")) {
            super.set(str, value);
        }
    }

    @Override
    public String toString() {
        if (block.func == null) return "{Generator}";
        return block.func.toPaddedString(0);
    }

    @Override
    public String getType() {
        return type;
    }

    private JSValue last_value;
    private Block block = null;
    private boolean done = false;
    private String type = "Object";
}
