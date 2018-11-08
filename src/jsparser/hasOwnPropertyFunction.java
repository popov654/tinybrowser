package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class hasOwnPropertyFunction extends Function {
    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return new JSBool(((JSObject)context).hasOwnProperty(args.get(0).asString()));
    }
}
