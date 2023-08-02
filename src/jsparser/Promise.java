package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Promise extends JSObject {

    public Promise(Function f) {
        items.put("__proto__", PromiseProto.getInstance());
        func = f;
        onFulfilled = new PromiseHandleWrapper(this, null, Promise.FULFILLED);
        onRejected = new PromiseHandleWrapper(this, null, Promise.REJECTED);
        created_on = System.currentTimeMillis();
    }

    public void run() {
        if (func != null) {
            Vector<JSValue> args = new Vector<JSValue>();
            args.add(onFulfilled);
            args.add(onRejected);
            func.call(null, args, false);
        }
    }

    public Promise then(Function f1, Function f2) {
        Promise p = new Promise(null);
        p.setHandlers(f1, f2);
        next.add(p);
        if (state == Promise.FULFILLED) {
            Vector<JSValue> args = new Vector<JSValue>();
            if (result != null) args.add(result);
            p.onFulfilled.call(null, args, false);
        }
        return p;
    }

    public Promise then(Function f) {
        return then(f, null);
    }

    public Promise _catch(Function f) {
        return then(null, f);
    }

    public void setHandlers(Function f1, Function f2) {
        onFulfilled = new PromiseHandleWrapper(this, f1, Promise.FULFILLED);
        onRejected = new PromiseHandleWrapper(this, f2, Promise.REJECTED);
    }

    public void setState(int value) {
        if (this.state > 0) return;
        this.state = value;
        Vector<JSValue> args = new Vector<JSValue>();
        if (result != null) args.add(result);
        if (value == Promise.FULFILLED) {
            if (onFulfilled.getError() == null) {
                if (result != null && result instanceof Promise) {
                    ((Promise)result).setChildren(next);
                } else {
                    for (int i = 0; i < next.size(); i++) {
                        next.get(i).onFulfilled.call(null, args, false);
                    }
                    next.clear();
                }
            } else {
                Block b = onFulfilled.getCaller();
                while (b != null) {
                    b.error = null;
                    b = b.func != null ? b.func.getCaller() : b.parent_block;
                }
                args = new Vector<JSValue>();
                args.add(onFulfilled.getError().getValue());
                for (int i = 0; i < next.size(); i++) {
                    next.get(i).onRejected.call(null, args, false);
                }
                if (next.size() == 0 && containers.size() == 0) {
                    JSError error = onFulfilled.getError();
                    System.err.println("Uncaught exception in promise: " + error.getText());
                    String stack = error.printStack();
                    if (!stack.isEmpty()) {
                        System.err.println(error.printStack());
                    }
                }
            }
        }
        if (value == Promise.REJECTED) {
            args = new Vector<JSValue>();
            if (result != null) args.add(result);
            for (int i = 0; i < next.size(); i++) {
                next.get(i).onRejected.call(null, args, false);
            }
            next.clear();
        }
        for (int i = 0; i < containers.size(); i++) {
            containers.get(i).notify(this, result, state);
        }
    }

    public void setChildren(Vector<Promise> v) {
        next = v;
    }

    public JSValue getResult() {
        return result;
    }

    public void setResult(JSValue value) {
        result = value;
    }

    public void attachTo(PromiseAdv p) {
        containers.add(p);
    }

    public void detachFrom(PromiseAdv p) {
        containers.remove(p);
    }

    @Override
    public String toString() {
        String status = (state == PENDING ? "pending" : (state == FULFILLED ? "fulfilled" : "rejected"));
        return "Promise<" + status + ">";
    }

    public static int PENDING = 0;
    public static int FULFILLED = 1;
    public static int REJECTED = 2;

    public Function onFulfilled = null;
    public Function onRejected = null;

    protected Vector<PromiseAdv> containers = new Vector<PromiseAdv>();

    protected JSValue result = null;

    protected Vector<Promise> next = new Vector<Promise>();

    protected long created_on = 0;

    protected int state = 0;
    protected Function func;
}
