package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileWriterC extends Function {

    public FileWriterC() {
        items.put("prototype", FileWriterProto.getInstance());
        FileWriterProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        return new FileWriter();
    }

}

