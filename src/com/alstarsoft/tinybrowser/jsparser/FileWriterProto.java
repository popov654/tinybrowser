package com.alstarsoft.tinybrowser.jsparser;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Vector;
import com.alstarsoft.tinybrowser.network.Blob;
import com.alstarsoft.tinybrowser.render.WebDocument;

/**
 *
 * @author Alex
 */
public class FileWriterProto extends JSObject {

    private FileWriterProto() {
        items.put("write", new writeFunction());
        items.put("writeText", new writeTextFunction());
    }

    class writeFunction extends Function {
        @Override
        public JSValue call(final JSObject context, Vector<JSValue> args, boolean as_constr) {

            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: at least 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            JSBlob blob;

            if (args.get(0) instanceof TypedArray) {
                blob = new JSBlob(new Blob(((TypedArray)args.get(0)).buffer.data));
            } else if (args.get(0) instanceof JSBlob) {
                blob = (JSBlob) args.get(0);
            } else {
                JSError e = new JSError(null, "Type error: argument 1 must be a File, a Blob or a TypedArray", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            if (args.size() >= 3) {
                blob.getBlob().mimeType = args.get(2).asString().getValue();
            }

            String filename;

            if (args.size() >= 2) {
                filename = args.get(1).asString().getValue();
            } else {
                filename = blob instanceof File ? ((File) blob).name : (blob.getMimeType().equals("text/plain") ? "file.txt" : "file");
            }

            com.alstarsoft.tinybrowser.render.Util.downloadFile(WebDocument.active_document, blob.getBlob(), filename);
            
            return Undefined.getInstance();
        }
    }

    class writeTextFunction extends Function {
        @Override
        public JSValue call(final JSObject context, Vector<JSValue> args, boolean as_constr) {

            if (args.size() == 0) {
                JSError e = new JSError(null, "Type error: argument 1 must be a File or a Blob", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }

            String text = args.get(0).asString().getValue();
            byte[] bytes = text.getBytes();

            if (args.size() >= 3) {
                bytes = new String(text.getBytes(), Charset.forName(args.get(2).asString().getValue())).getBytes();
            }

            String filename;

            if (args.size() >= 2 && args.get(1).asBool().getValue()) {
                filename = args.get(1).asString().getValue();
            } else {
                filename = "file.txt";
            }

            com.alstarsoft.tinybrowser.render.Util.downloadFile(WebDocument.active_document, new Blob(bytes), filename);

            return Undefined.getInstance();
        }
    }

    public static FileWriterProto getInstance() {
        if (instance == null) {
            instance = new FileWriterProto();
        }
        return instance;
    }

    private String type = "Object";
    private static FileWriterProto instance = null;
}
