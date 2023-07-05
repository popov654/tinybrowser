package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class ObjectC extends Function {

    public ObjectC() {
        items.put("prototype", ObjectProto.getInstance());
        ObjectProto.getInstance().set("constructor", this);
        addMethods();
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() >= 1) {
            JSValue val = args.get(0);
            String type = val.getType();
            if (type.equals("Float")) return new JSFloat(((JSFloat)val).getValue());
            if (type.equals("Integer")) return new JSInt(((JSInt)val).getValue());
            if (type.matches("Array|String|Object|Function")) return val;
            else if (val instanceof JSObject) {
                JSObject obj = (JSObject)val;
                while (obj != null && obj.hasOwnProperty("_proto_") && !type.equals("Object")) {
                    obj = (JSObject)obj.get("_proto_");
                    if (obj != null) {
                        type = obj.getType();
                    }
                }
                if (type.equals("Object")) return val;
            } else {
                return new JSObject();
            }
        }
        return new JSObject();
    }

    private void addMethods() {
        final JSObject instance = this;
        items.put("freeze", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() == 0 || !(args.get(0) instanceof JSObject)) {
                    return Undefined.getInstance();
                }
                ((JSObject)args.get(0)).isFrozen = true;
                return Undefined.getInstance();
            }
        });
        items.put("isFrozen", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() == 0 || !(args.get(0) instanceof JSObject)) {
                    new JSBool(false);
                }
                return new JSBool(context.isFrozen);
            }
        });
    }

}
