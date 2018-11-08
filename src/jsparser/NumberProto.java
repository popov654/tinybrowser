package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class NumberProto extends JSObject {

    class toPrecisionFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (context.getType().equals("Float") &&
                   args.size() > 0 && args.get(0).getType().equals("Integer")) {
                double d = ((JSFloat)context).getValue();
                int p = (int)((JSInt)args.get(0)).getValue();
                for (int i = 0; i < p; i++) {
                    d = d * 10;
                }
                d = Math.round(d);
                for (int i = 0; i < p; i++) {
                    d = d / 10;
                }
                return new JSFloat(d);
            }
            return context;
        }
    }

    class isFiniteFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSBool(!(((JSValue)context) instanceof Infinity));
        }
    }

    private NumberProto() {
        items.put("toPrecision", new toPrecisionFunction());
        items.put("isFinite", new isFiniteFunction());
    }

    public static NumberProto getInstance() {
        return instance;
    }

    @Override
    public String getType() {
        return type;
    }

    private String type = "Object";
    private static NumberProto instance = new NumberProto();
}
