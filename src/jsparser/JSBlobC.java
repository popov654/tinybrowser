package jsparser;

import java.util.Vector;
import network.Blob;

/**
 *
 * @author Alex
 */
public class JSBlobC extends Function {

    public JSBlobC() {
        items.put("prototype", JSBlobProto.getInstance());
        JSBlobProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() < 1) {
            JSError e = new JSError(null, "ArgumentsError: at least 1 argument expected", getCaller().getStack());
            getCaller().error = e;
            return Undefined.getInstance();
        }
        if (!args.get(0).getType().equals("Array")) {
            JSError e = new JSError(null, "ArgumentsError: argument 1 must be an array", getCaller().getStack());
            getCaller().error = e;
            return Undefined.getInstance();
        }
        JSArray list = (JSArray) args.get(0);

        if (list.items.size() == 0) {
            Blob b = new Blob(new byte[0]);
            if (args.size() > 1) {
                if (args.get(1).getType().equals("String")) {
                    b.mimeType = args.get(1).asString().getValue();
                } else if (args.get(1).getType().equals("Object") && ((JSObject)args.get(1)).get("type").getType().equals("String")) {
                    b.mimeType = ((JSObject)args.get(1)).get("type").asString().getValue();
                }
            }
            return new JSBlob(b);
        }

        String mimeType = "";

        Vector<Blob> blobs = new Vector<Blob>();
        for (int i = 0; i < list.items.size(); i++) {
            if (list.items.get(i) instanceof JSBlob) {
                blobs.add(((JSBlob)list.items.get(i)).blob);
                if (mimeType.isEmpty()) mimeType = ((JSBlob)list.items.get(i)).blob.getType();
            } else if (list.items.get(i) instanceof JSString) {
                blobs.add(new Blob(list.items.get(i).asString().getValue().getBytes(), "text/plain"));
                if (mimeType.isEmpty()) mimeType = "text/plain";
            } else if (list.items.get(i) instanceof File) {
                File file = (File) list.items.get(i);
                if (file.blob != null) {
                    blobs.add(file.blob.blob);
                    if (mimeType.isEmpty()) mimeType = "application/octet-stream";
                } else {
                    blobs.add(new Blob(file.file));
                    if (mimeType.isEmpty()) mimeType = network.Request.getMimeType(file.file.getName());
                }
            }
        }

        if (mimeType.isEmpty()) mimeType = "application/octet-stream";

        return new JSBlob(new Blob(blobs, mimeType));
    }

}