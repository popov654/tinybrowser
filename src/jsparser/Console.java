package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Console extends JSObject {

    class logFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            String str = args.get(0).asString().getValue();
            data.add(str);
            System.out.println(str);
            for (Listener listener: listeners) {
                listener.log(str);
            }
            return Undefined.getInstance();
        }
    }

    class clearFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            data.clear();
            for (Listener listener: listeners) {
                listener.clear();
            }
            return Undefined.getInstance();
        }
    }

    private Console() {
        items.put("log", new logFunction());
        items.put("clear", new clearFunction());
    }

    public static Console getInstance() {
        if (instance == null) {
            instance = new Console();
        }
        return instance;
    }

    public Vector<String> getData() {
        return data;
    }

    public void addListener(Listener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public interface Listener {
        public void log(String message);
        public void clear();
    }

    private Vector<String> data = new Vector<String>();
    private Vector<Listener> listeners = new Vector<Listener>();
    private String type = "Object";
    private static Console instance = null;
}
