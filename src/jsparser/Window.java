package jsparser;

import bridge.Mapper;
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

    class confirmFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                args.add(JSValue.create("String", ""));

            }
            boolean value = confirm(args.get(0).asString().getValue());
            return new JSBool(value);
        }
    }

    class promptFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                args.add(JSValue.create("String", ""));

            }
            String value = prompt(args.get(0).asString().getValue());
            return new JSString(value);
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

    class addEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (document == null) return Undefined.getInstance();
            HTMLElement bodyElement = (HTMLElement) document.items.get("body");
            if (bodyElement == null) return Undefined.getInstance();

            JSObject proto = (JSObject) bodyElement.get("__proto__");

            return ((Function)proto.items.get("addEventListener")).call(bodyElement, args, as_constr);
        }
    }

    class removeEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (document == null) return Undefined.getInstance();
            HTMLElement bodyElement = (HTMLElement) document.items.get("body");
            if (bodyElement == null) return Undefined.getInstance();

            JSObject proto = (JSObject) bodyElement.get("__proto__");

            return ((Function)proto.items.get("removeEventListener")).call(bodyElement, args, as_constr);
        }
    }


    class getComputedStyleFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof HTMLElement)) {
                JSError e = new JSError(null, "Type error: argument 1 is not an instance of HTMLElement", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return ((HTMLElement)args.get(0)).getComputedStyles();
        }
    }

    public Window(Block block) {
        root = block;
        items = root.scope;
        items.put("alert", new alertFunction());
        items.put("confirm", new confirmFunction());
        items.put("prompt", new promptFunction());
        items.put("eval", new evalFunction());
        items.put("parseInt", new parseIntFunction());
        items.put("parseFloat", new parseFloatFunction());
        items.put("setTimeout", new setTimeoutFunction());
        items.put("clearTimeout", new clearTimeoutFunction());
        items.put("setInterval", new setIntervalFunction());
        items.put("clearInterval", new clearTimeoutFunction());
        items.put("addEventListener", new addEventListenerFunction());
        items.put("removeEventListener", new removeEventListenerFunction());
        items.put("getComputedStyle", new getComputedStyleFunction());

        JSObject screen = new JSObject();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screen.set("width", new JSInt(screenSize.width));
        screen.set("height", new JSInt(screenSize.height));
        items.put("screen", screen);

        tr = new TaskRunner(timers);
    }
    
    public Window(Block block, HTMLParser document) {
        this(block);
        setDocument(document);
    }

    public void setDocument(HTMLParser document) {
        this.parser = document;
        this.document = new HTMLDocument(document);
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

    private boolean confirm(String message) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        int result = JOptionPane.showConfirmDialog(windowFrame, message, "Confirm action", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return (result == JOptionPane.OK_OPTION || result == JOptionPane.YES_OPTION);
    }

    private String prompt(String message) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        return JOptionPane.showInputDialog(windowFrame, message, "Enter value", JOptionPane.PLAIN_MESSAGE);
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
    public void set(String str, JSValue value) {
        if (str.startsWith("on") && (value instanceof Function || value instanceof Null || value instanceof Undefined)) {
            boolean add = !(value instanceof Null || value instanceof Undefined);
            Function func = (Function) items.get(add ? "addEventListener" : "removeEventListener");
            if (func != null) {
                Vector<JSValue> args = new Vector<JSValue>();
                args.add(new JSString(str.substring(2).toLowerCase()));
                args.add(add ? value : items.get(str));
                if (args.get(1) instanceof Function) func.call(this, args);
            }
        }
        value.incrementRefCount();
        items.put(str, value);
    }

    @Override
    public void set(JSString str, JSValue value) {
        if (str.getValue().startsWith("on") && (value instanceof Function || value instanceof Null || value instanceof Undefined)) {
            boolean add = !(value instanceof Null || value instanceof Undefined);
            Function func = (Function) items.get(add ? "addEventListener" : "removeEventListener");
            if (func != null) {
                Vector<JSValue> args = new Vector<JSValue>();
                args.add(new JSString(str.getValue().substring(2).toLowerCase()));
                args.add(add ? value : items.get(str.getValue()));
                if (args.get(1) instanceof Function) func.call(this, args);
            }
        }
        value.incrementRefCount();
        items.put(str.getValue(), value);
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
        func.incrementRefCount();
        timers.add(new Timer(this, func, interval, run_once, params));
        if (timers.size() > 0 && !tr.started) startTaskRunner();
        return timers.lastElement().getId();
    }

    public void removeTimer(Timer t) {
        t.getFunction().decrementRefCount();
        timers.remove(t);
        if (timers.size() == 0 && tr.started) stopTaskRunner();
    }

    public void removeTimer(int id) {
        for (int i = 0; i < timers.size(); i++) {
            if (timers.get(i).getId() == id) {
                timers.get(i).getFunction().decrementRefCount();
                timers.remove(i);
                if (timers.size() == 0 && tr.started) stopTaskRunner();
                break;
            }
        }
    }

    public void addPromise(Promise p) {
        promises.add(p);
    }

    public java.awt.event.ComponentAdapter resizeListener = new java.awt.event.ComponentAdapter() {
        @Override
        public void componentMoved(java.awt.event.ComponentEvent evt) {}

        @Override
        public void componentResized(java.awt.event.ComponentEvent evt) {
            render.Block b = Mapper.get(((HTMLElement)document.items.get("body")).node);
            if (b != null) {
                items.put("innerWidth", new JSInt((int)Math.floor((double)b.viewport_width / b.ratio)));
                items.put("innerHeight", new JSInt((int)Math.floor((double)b.viewport_height / b.ratio)));
                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                ((JSObject)items.get("screen")).set("width", new JSInt(screenSize.width));
                ((JSObject)items.get("screen")).set("height", new JSInt(screenSize.height));
            }
        }
    };

    public HashMap<String, JSValue> getItems() {
        return items;
    }

    private Block root;
    private HTMLParser parser;
    private HTMLDocument document;
    private Frame windowFrame;
    private HashMap<String, JSValue> items;
    private String type = "Object";
    private Vector<Timer> timers = new Vector<Timer>();
    private Vector<Promise> promises = new Vector<Promise>();
    private TaskRunner tr;
}
