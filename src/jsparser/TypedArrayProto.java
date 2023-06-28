package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TypedArrayProto extends JSObject {

    class subarrayFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: at least 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (args.size() > 1 && !args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "Type error: argument 2 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            int from = (int) args.get(0).asInt().getValue();
            int to = args.size() > 1 ? (int) args.get(1).asInt().getValue() : ((TypedArray)context).buffer.end;

            return ((TypedArray)context).subArray(new JSInt(from), new JSInt(to));
        }
    }

    private TypedArrayProto() {
        items.put("__proto__", ArrayProto.getInstance());
        items.put("subarray", new subarrayFunction());
    }

    public static TypedArrayProto getInstance() {
        if (instance == null) {
            instance = new TypedArrayProto();
        }
        return instance;
    }

    private String type = "Object";
    private static TypedArrayProto instance = null;

}
