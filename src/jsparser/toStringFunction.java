package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class toStringFunction extends Function {
    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return new JSString(((JSObject)context).toString());
    }
}