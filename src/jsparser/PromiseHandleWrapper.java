package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class PromiseHandleWrapper extends Function {
    public PromiseHandleWrapper(Promise p, Function func, int type) {
        this.promise = p;
        this.func = func;
        this.to_state = type;
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        JSValue result = args.size() > 0 ? args.get(0) : Undefined.getInstance();
        if (func != null) {
            Block b = getCaller();
            if (b == null) {
                b = func.getParentBlock();
                while (b.parent_block != null) {
                    b = b.parent_block;
                }
            }
            func.setCaller(b);
            result = func.call(context, args, false);
        }
        promise.setResult(result);
        promise.setState(to_state);
        return promise.getResult();
    }

    @Override
    public Block getCaller() {
        return func != null ? func.getCaller() : null;
    }

    @Override
    public JSError getError() {
        return func != null ? func.getError() : null;
    }

    private Promise promise;
    private Function func;
    private int to_state = 0;
}
