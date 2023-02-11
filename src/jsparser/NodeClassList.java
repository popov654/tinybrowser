package jsparser;

import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class NodeClassList extends JSArray {

    NodeClassList(Node node) {
        super();
        this.node = node;
        _items.put("add", new addClassFunction(this));
        _items.put("contains", new hasClassFunction(this));
        _items.put("remove", new removeClassFunction(this));
    }

    public Node getNode() {
        return node;
    }

    @Override
    public JSValue get(JSString str) {
        String key = str.getValue();
        if (key.equals("add") || key.equals("contains") || key.equals("remove")) {
            return _items.get(key);
        }
        return super.get(str);
    }

    @Override
    public JSValue get(String key) {
        if (key.equals("add") || key.equals("contains") || key.equals("remove")) {
            return _items.get(key);
        }
        return super.get(key);
    }

    class addClassFunction extends Function {

        addClassFunction(NodeClassList parent) {
            list = parent;
        }

        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            node.addClass(args.get(0).asString().getValue());
            list.items.add(args.get(0).asString());

            return new JSBool(true);
        }

        private NodeClassList list;
    }

    class hasClassFunction extends Function {

        hasClassFunction(NodeClassList parent) {
            list = parent;
        }

        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            String classes = node.getAttribute("class");
            if (classes == null) {
                return new JSBool(false);
            }
            return new JSBool(classes.matches("(^|\\s)" + args.get(0).asString().getValue() + "(\\s|$)"));
        }

        private NodeClassList list;
    }

    class removeClassFunction extends Function {
        
        removeClassFunction(NodeClassList parent) {
            list = parent;
        }

        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            node.removeClass(args.get(0).asString().getValue());
            list.items.remove(args.get(0).asString());

            return new JSBool(true);
        }

        private NodeClassList list;
    }

    @Override
    public String toString() {
        return "DOMClassList" + super.toString();
    }

    private Node node;
    private JSElement element;

}
