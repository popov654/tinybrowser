package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileC extends Function {

    public FileC() {
        items.put("prototype", FileProto.getInstance());
        FileProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        JSBlobC constr = new JSBlobC();

        JSBlob blob = (JSBlob) constr.call(context, args);

        /* Vector<Blob> blobs = blob.blob.parts;
        Vector<JSBlob> jsblobs = new Vector<JSBlob>();
        for (Blob b: blobs) {
            jsblobs.add(new JSBlob(b));
        } */

        if (args.size() > 1 && args.get(1).getType().equals("String")) {
            return new File(blob, args.get(1).asString().getValue(), blob.getType());
        }

        return new File(blob, blob.getType());
    }
    
}
