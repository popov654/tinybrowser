package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileProto extends JSObject {

    private FileProto() {
        items.put("asBlob", new asBlobFunction());
    }

    class asBlobFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((File)context).blob;
        }
    }

    public static FileProto getInstance() {
        if (instance == null) {
            instance = new FileProto();
        }
        return instance;
    }

    private String type = "Object";
    private static FileProto instance = null;
}
