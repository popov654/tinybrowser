package jsparser;

import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Function extends JSObject {

    public Function() {}

    public Function(Vector<String> p, Block b, String name) {
        params = p;
        body = b;
        display_name = name;
        b.setReusable(true);
        b.is_func = true;
        items.put("__proto__", FunctionProto.getInstance());
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        if (is_generator) {
            Block b = (Block)body.clone();
            b.setReusable(false);
            b.func = this;
            return new Generator(b);
        }
        JSValue r1 = body.scope.get("resolve");
        JSValue r2 = body.scope.get("reject");
        if (body == null) return Undefined.getInstance();
        body.scope = new HashMap<String, JSValue>();
        if (!is_lambda) {
            body.scope.put("arguments", new JSArray(args));
        }
        int j = 0;
        while (j < Math.min(params.size(), bound_args.size())) {
            body.scope.put(params.get(j), bound_args.get(j));
            j++;
        }
        for (int i = j; i < params.size(); i++) {
            body.scope.put(params.get(i), i-j < args.size() ? args.get(i-j) : Undefined.getInstance());
        }
        body.func = this;
        Block b = body;
        while (b.parent_block != null) b = b.parent_block;
        if (!is_lambda) {
            if (context == null) context = (JSObject)Block.getVar("window", b);
            if (bound_ctx != null) context = (JSObject)bound_ctx;
            body.scope.put("this", context);
        }
        if (as_constr) {
            JSObject obj = new JSObject();
            JSObject proto = items.containsKey("prototype") ?
                (JSObject)items.get("prototype") : ObjectProto.getInstance();
            proto.set("constructor", this);
            obj.set("__proto__", proto);
            body.scope.put("this", obj);
            body.eval();
            return obj;
        }
        return ((Block)body.eval()).return_value;
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        return call(context, args, false);
    }

    @Override
    public JSValue call(Vector<JSValue> args) {
        return call(null, args, false);
    }

    public Block getCaller() {
        return caller;
    }

    public void setCaller(Block b) {
        caller = b;
    }

    public void setAsLambda() {
        is_lambda = true;
    }

    public void setAsGenerator() {
        is_generator = true;
    }

    public JSError getError() {
        return body.error;
    }

    public Block getParentBlock() {
        return body.parent_block;
    }

    public JSValue getCallerFunction() {
        Block b = caller;
        while (b != null && !b.is_func) {
            b = b.parent_block;
        }
        return b != null ? b.func : Null.getInstance();
    }

    public String getName() {
        return display_name;
    }

    public void setName(String value) {
        display_name = value;
    }

    public void injectVar(String name, JSValue value) {
        body.scope.put(name, value);
    }

    public void removeVar(String name) {
        body.scope.remove(name);
    }

    public String getParamName(int index) {
        return params.get(index);
    }

    public int getParamsCount() {
        return params.size();
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block block) {
        body = block;
    }

    public Function bind(JSObject context, Vector<JSValue> args) {
        if (bound_ctx == null) {
            bound_ctx = context;
        }
        bound_args.addAll(args);
        return this;
    }

    public void setSilent(boolean val) {
        if (this.body != null) {
            body.setSilent(val);
        }
    }

    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(params.size());
        }
        if (str.equals("displayName") || str.equals("name")) {
            return new JSString(display_name);
        }
        if (str.equals("caller")) {
            return getCallerFunction();
        }
        return super.get(str);
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.equals("length") || str.equals("displayName") ||
                str.equals("name") || str.equals("caller")) return;
        super.set(str, value);
    }
    
    @Override
    public void incrementRefCount() {
        if (body != null) {
            body.incrementRefCount();
        }
        super.incrementRefCount();
    }

    @Override
    public void decrementRefCount() {
        if (body != null) {
            body.decrementRefCount();
            body.func = null;
            body = null;
        }
        super.decrementRefCount();
    }

    @Override
    public String getType() {
        return type;
    }

    public String getArguments() {
        String result = "";
        if (params == null) return result;
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) result += ", ";
            result += params.get(i);
        }
        return result;
    }

    @Override
    public Function clone() {
       return new Function((Vector<String>)params.clone(), (Block)body.clone(), display_name);
    }

    @Override
    public String toString() {
        String func_body = "{}";
        if (body != null) {
            func_body = body.toString(0).replaceAll("\\s+$", "");
            if (func_body.matches("\\{\\s+\\}")) {
                func_body = "{}";
            }
        }
        String signature = is_lambda ? "(" + getArguments() + ") => " : "function" + (is_generator ? "*" : "") + " (" + getArguments() + ")";
        return signature + (body != null ? " " + func_body : "");
    }

    public String toPaddedString(int level) {
        String pad = "";
        for (int i = 0; i < level; i++) {
            pad += "  ";
        }
        String signature = is_lambda ? "(" + getArguments() + ") => " : "function" + (is_generator ? "*" : "") + " (" + getArguments() + ")";
        return pad + signature + (body != null ? " " + body.toString(level) : "");
    }

    public String toPaddedString() {
        return toPaddedString(0);
    }

    private Vector<String> params;
    private String display_name;
    private JSValue bound_ctx = null;
    private Vector<JSValue> bound_args = new Vector<JSValue>();
    private Block body;
    private Block caller = null;
    private boolean is_lambda;
    private boolean is_generator;
    protected String type = "Function";
}
