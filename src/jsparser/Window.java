package jsparser;

import htmlparser.HTMLParser;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author Alex
 */
public class Window extends JSObject {

    class alertFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                args.add(JSValue.create("String", ""));
                
            }
            alert(args.get(0).asString().getValue());
            return Undefined.getInstance();
        }
    }

    class parseIntFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                args.add(new JSString(""));
            }
            try {
                return new JSInt(Integer.parseInt(args.get(0).asString().getValue()));
            } catch(NumberFormatException ex) {
                return new NaN();
            }
        }
    }

    class parseFloatFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                args.add(new JSString(""));
            }
            try {
                return new JSFloat(Float.parseFloat(args.get(0).asString().getValue()));
            } catch(NumberFormatException ex) {
                return new NaN();
            }
        }
    }

    class evalFunction extends Function implements DynamicContext {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0 && args.get(0).getType().equals("String")) {
                JSParser jp = new JSParser(((JSString)args.get(0)).getValue());
                Block b = (Block)Expression.create(jp.getHead());
                b.parent_block = ctx;
                b.eval();
                return b.getValue();
            }
            return Undefined.getInstance();
        }
        public void setContext(Block b) {
            ctx = b;
        }
        private Block ctx = null;
    }

    class setTimeoutFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            int interval = args.size() > 1 ? (int)args.get(1).asInt().getValue() : 0;
            return new JSInt(((Window)context).addTimer((Function)args.get(0), interval, true, new Vector(args.subList(2, args.size()))));
        }
    }

    class clearTimeoutFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return Undefined.getInstance();
            }
            removeTimer((int)args.get(0).asInt().getValue());
            return Undefined.getInstance();
        }
    }

    class setIntervalFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Type error: argument 1 is not a function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            int interval = args.size() > 1 ? (int)args.get(1).asInt().getValue() : 0;
            return new JSInt(((Window)context).addTimer((Function)args.get(0), interval, false, new Vector(args.subList(2, args.size()))));
        }
    }

    public Window(Block block) {
        root = block;
        items = root.scope;
        items.put("alert", new alertFunction());
        items.put("eval", new evalFunction());
        items.put("parseInt", new parseIntFunction());
        items.put("parseFloat", new parseFloatFunction());
        items.put("setTimeout", new setTimeoutFunction());
        items.put("clearTimeout", new clearTimeoutFunction());
        items.put("setInterval", new setIntervalFunction());
        items.put("clearInterval", new clearTimeoutFunction());
        tr = new TaskRunner(timers);
    }
    
    public Window(Block block, HTMLParser document) {
        this(block);
        setDocument(document);
    }

    public void setDocument(HTMLParser document) {
        this.parser = document;
        this.document = new JSDocument(document);
        items.put("document", this.document);
    }

    public void setWindowFrame(Frame frame) {
        this.windowFrame = frame;
    }

    private void alert(String message) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        JOptionPane.showMessageDialog(windowFrame, message, "Alert", JOptionPane.PLAIN_MESSAGE);
    }

    public void startTaskRunner() {
        tr.started = true;
        tr.start();
    }

    public void stopTaskRunner() {
        tr.stop = true;
    }

    public void runPromises() {
        for (int i = 0; i < promises.size(); i++) {
            promises.get(i).run();
        }
        promises.clear();
    }

    @Override
    public JSValue get(JSString str) {
        JSValue val = items.get(str.getValue());
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public JSValue get(String str) {
        JSValue val = items.get(str);
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public void set(JSString str, JSValue value) {
        items.put(str.getValue(), value);
    }

    @Override
    public void set(String str, JSValue value) {
        items.put(str, value);
    }

    @Override
    public JSValue removeProperty(JSString str) {
        return items.remove(str.getValue());
    }

    @Override
    public JSValue removeProperty(String str) {
        return items.remove(str);
    }

    @Override
    public String toString() {
        String result = "";
        Set keys = new TreeSet(items.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (items.get(str) == this) continue;
            if (result.length() > 0) result += ", ";
            result += str + ": " + items.get(str).toString();
        }
        return "{" + result + "}";
    }

    public Block getRoot() {
        return root;
    }
    
    @Override
    public String getType() {
        return type;
    }

    public Vector<Timer> getTimers() {
        return timers;
    }

    public int addTimer(Function func, int interval, boolean run_once, Vector<JSValue> params) {
        timers.add(new Timer(this, func, interval, run_once, params));
        if (timers.size() > 0 && !tr.started) startTaskRunner();
        return timers.lastElement().getId();
    }

    public void removeTimer(Timer t) {
        timers.remove(t);
        if (timers.size() == 0 && tr.started) stopTaskRunner();
    }

    public void removeTimer(int id) {
        for (int i = 0; i < timers.size(); i++) {
            if (timers.get(i).getId() == id) {
                timers.remove(i);
                if (timers.size() == 0 && tr.started) stopTaskRunner();
                break;
            }
        }
    }

    public void addPromise(Promise p) {
        promises.add(p);
    }

    private Block root;
    private HTMLParser parser;
    private JSDocument document;
    private Frame windowFrame;
    private HashMap<String, JSValue> items;
    private String type = "Object";
    private Vector<Timer> timers = new Vector<Timer>();
    private Vector<Promise> promises = new Vector<Promise>();
    private TaskRunner tr;
}
