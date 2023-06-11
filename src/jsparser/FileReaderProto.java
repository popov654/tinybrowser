package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileReaderProto extends JSObject {

    private FileReaderProto() {
        items.put("readAsText", new readAsTextFunction());
        items.put("readAsDataURL", new readAsDataURLFunction());
    }

    class readAsTextFunction extends Function {
        @Override
        public JSValue call(final JSObject context, Vector<JSValue> args, boolean as_constr) {

            if (!checkArguments(this, args)) {
                return Undefined.getInstance();
            }

            final JSBlob blob = (JSBlob) args.get(0);

            final JSValue callback = ((FileReader)context).get("onload");
            if (callback instanceof Function) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSValue result = new JSString(blob.blob.asTextString());
                        ((JSObject)context).set("result", result);

                        JSEvent event = new JSEvent(context, "load", null);
                        event.items.put("loaded", new JSInt(blob.blob.getSize()));
                        event.items.put("total", new JSInt(blob.blob.getSize()));
                        Vector<JSValue> arguments = new Vector<JSValue>();
                        arguments.add(event);

                        ((Function)callback).call(context, arguments);
                    }
                });
                t.start();
            }
            return Undefined.getInstance();
        }
    }

    class readAsDataURLFunction extends Function {
        @Override
        public JSValue call(final JSObject context, Vector<JSValue> args, boolean as_constr) {

            if (!checkArguments(this, args)) {
                return Undefined.getInstance();
            }

            final JSBlob blob = (JSBlob) args.get(0);

            final JSValue callback = ((FileReader)context).get("onload");
            if (callback instanceof Function) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSValue result = new JSString(blob.blob.asDataURL());
                        ((JSObject)context).set("result", result);
                        
                        JSEvent event = new JSEvent(context, "load", null);
                        event.items.put("loaded", new JSInt(blob.blob.getSize()));
                        event.items.put("total", new JSInt(blob.blob.getSize()));
                        Vector<JSValue> arguments = new Vector<JSValue>();
                        arguments.add(event);

                        ((Function)callback).call(context, arguments);
                    }
                });
                t.start();
            }
            return Undefined.getInstance();
        }
    }

    private boolean checkArguments(Function func, Vector<JSValue> args) {
        if (args.size() == 0) {
            JSError e = new JSError(null, "Arguments error: 1 argument required", func.getCaller().getStack());
            func.getCaller().error = e;
            return false;
        }

        if (!(args.get(0) instanceof File || args.get(0) instanceof JSBlob)) {
            JSError e = new JSError(null, "Type error: argument 1 must be a File or a Blob", func.getCaller().getStack());
            func.getCaller().error = e;
            return false;
        }

        return true;
    }

    public static FileReaderProto getInstance() {
        if (instance == null) {
            instance = new FileReaderProto();
        }
        return instance;
    }

    private String type = "Object";
    private static FileReaderProto instance = null;
}
