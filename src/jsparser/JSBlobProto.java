package jsparser;

import htmlparser.Node;
import java.util.Vector;
import network.Blob;

/**
 *
 * @author Alex
 */
public class JSBlobProto extends JSObject {

    private JSBlobProto() {
        items.put("slice", new sliceFunction());
    }


    class sliceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "ArgumentsError: at least 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (args.size() > 0 && !args.get(0).getType().equals("Integer")) {
                JSError e = new JSError(null, "ArgumentsError: argument 1 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (args.size() > 1 && !args.get(1).getType().equals("Integer")) {
                JSError e = new JSError(null, "ArgumentsError: argument 2 must be an integer value", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            long start = args.get(0).asInt().getValue();
            long end = args.size() > 1 ? args.get(1).asInt().getValue() : ((JSBlob)context).blob.getSize();
            
            Blob b = ((JSBlob)context).blob.getSlice(start, end);
            if (args.size() > 2 && !(args.get(2) instanceof JSString)) {
                b.mimeType = args.get(2).asString().getValue();
            }

            return new JSBlob(b);
        }
    }

    public static JSBlobProto getInstance() {
        if (instance == null) {
            instance = new JSBlobProto();
        }
        return instance;
    }

    private String type = "Object";
    private static JSBlobProto instance = null;
}