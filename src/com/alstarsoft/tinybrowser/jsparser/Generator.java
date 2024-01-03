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
                f_block.yt_value = args.get(0);
            } else {
                Expression last_exp = f_block.last > -1 ? f_block.children.get(f_block.last) : null;
                if (last_exp != null && last_exp.yt != null && last_exp.yt != last_exp.start && !(last_exp.yt.val instanceof Generator)) {
                    f_block.yt_value = last_value;
                    last_exp.yt = null;
                } else if (last_exp != null && last_exp.yt != null && last_exp.yt.prev == last_exp.start) {
                    f_block.yt_value = null;
                }
            }
            f_block.eval();
            last_value = f_block.return_value;
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", f_block.return_value);
            }
            result.set("done", new JSBool(f_block.done));
            done = f_block.done;
            return result;
        }
    }

    class throwFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                f_block.error = new JSError(args.get(0), "Error", null);
            }
            f_block.eval();
            JSObject result = new JSObject();
            if (!done) {
                result.set("value", f_block.return_value);
            }
            result.set("done", new JSBool(f_block.done));
            done = f_block.done;
            return result;
        }
    }

    public Generator(Block b) {
        b.is_func = true;
        b.is_gen = true;
        f_block = b;
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
        if (f_block.func == null) return "{Generator}";
        return f_block.func.toPaddedString(0);
    }

    @Override
    public String getType() {
        return type;
    }

    private JSValue last_value;
    private Block f_block = null;
    private boolean done = false;
    private String type = "Object";
}
