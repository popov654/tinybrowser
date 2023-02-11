package jsparser;

import htmlparser.Node;
import htmlparser.NodeActionCallback;
import htmlparser.NodeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSElement extends JSObject {

    private JSElement(Node node) {
        this.node = node;

        items.put("getElementById", new getElementByIdFunction());
        items.put("getElementsByTagName", new getElementsByTagNameFunction());
        items.put("getElementsByName", new getElementsByNameFunction());
        items.put("getElementsByClassName", new getElementsByClassNameFunction());

        items.put("getAttribute", new getAttributeFunction());
        items.put("hasAttribute", new hasAttributeFunction());
        items.put("setAttribute", new setAttributeFunction());
        items.put("removeAttribute", new removeAttributeFunction());

        items.put("addEventListener", new addEventListenerFunction());
        items.put("removeEventListener", new removeEventListenerFunction());

        node.addListener(eventListener, node, "any");
    }

    NodeActionCallback eventListener = new NodeActionCallback() {

        @Override
        public void nodeChanged(NodeEvent e, String source) {
            if (!source.split(":")[0].equals("render")) {
                updateClassList();
                return;
            }
            HashMap<String, String> data = e.getData();
            //if (data != null) System.out.println(data.get("pageX") + ", " + data.get("pageY"));
            Vector<JSElement> parents = getParents();
            if (data != null) data.put("bubbles", parents.size() > 1 ? "true" : "false");
            JSEvent event = new JSEvent(JSElement.create(node), e.relatedTarget != null ? JSElement.create(e.relatedTarget) : null, data);
            Vector<JSValue> args = new Vector<JSValue>();
            args.add(event);
            for (int i = parents.size()-1; i >= 0; i--) {
                if (event.get("cancelBubble").asBool().getValue()) break;
                Vector<Function> funcs = parents.get(i).listeners_0.get(data.get("type"));
                if (funcs == null) continue;
                for (Function func: funcs) {
                    func.call(args);
                }
            }
            for (int i = 0; i < parents.size(); i++) {
                if (event.get("cancelBubble").asBool().getValue()) break;
                Vector<Function> funcs = parents.get(i).listeners.get(data.get("type"));
                if (funcs == null) continue;
                for (Function func: funcs) {
                    func.call(args);
                }
            }
            if (!data.get("type").equals("\"mousemove\"")) {
                System.out.println("JS event: " + source.split(":")[1] + " " + e.target.tagName);
                /* if (relatedTarget != null) {
                    System.out.println("Related target: " + relatedTarget.tagName);
                } */
            }
        }

    };

    private Vector<JSElement> getParents() {
        Vector<JSElement> result = new Vector<JSElement>();
        result.add(this);
        JSValue p = items.get("parentNode");
        while (p != null && p instanceof JSElement) {
            result.add((JSElement)p);
            p = ((JSElement)p).items.get("parentNode");
        }
        return result;
    }

    public static JSElement create(Node node) {
        JSElement element = map.get(node);
        if (element == null) {
            element = new JSElement(node);
            map.put(node, element);
            element.calculateValues();
        }
        return element;
    }

    public void calculateValues() {
        items.put("nodeValue", new JSString(node.nodeValue));
        items.put("textContent", new JSString(node.getTextContent()));
        items.put("nodeType", new JSInt(node.nodeType));

        Node parent = node != null ? node.parent : null;
        items.put("parentNode", parent != null ? JSElement.create(parent) : Null.getInstance());
        Node prev = node != null ? node.previousSibling : null;
        items.put("previousSibling", prev != null ? JSElement.create(prev) : Null.getInstance());
        Node next = node != null ? node.nextSibling : null;
        items.put("nextSibling", next != null ? JSElement.create(next) : Null.getInstance());
        Node prev_el = node != null ? node.previousElementSibling() : null;
        items.put("previousElementSibling", prev_el != null ? JSElement.create(prev_el) : Null.getInstance());
        Node next_el = node != null ? node.nextElementSibling() : null;
        items.put("nextElementSibling", next_el != null ? JSElement.create(next_el) : Null.getInstance());

        JSArray childNodes = new JSArray();
        for (Node child: node.children) {
            childNodes.push(JSElement.create(child));
        }
        items.put("childNodes", childNodes);
        
        JSArray children = new JSArray();
        for (Node child: node.children) {
            if (child.nodeType == 1) {
                children.push(JSElement.create(child));
            }
        }
        items.put("children", children);

        NodeClassList classes = new NodeClassList(node);
        String c = node.getAttribute("class");
        if (c != null) {
            String[] classList = c.split("\\s");
            for (int i = 0; i < classList.length; i++) {
                classes.push(new JSString(classList[i]));
            }
        }
        items.put("classList", classes);
    }

    class getParentFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node parent = node != null ? node.parent : null;
            return parent != null ? new JSElement(parent) : Null.getInstance();
        }
    }

    class getPreviousSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousSibling : null;
            return prev != null ? new JSElement(prev) : Null.getInstance();
        }
    }

    class getNextSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextSibling : null;
            return next != null ? new JSElement(next) : Null.getInstance();
        }
    }

    class getPreviousElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousElementSibling() : null;
            return prev != null ? new JSElement(prev) : Null.getInstance();
        }
    }

    class getNextElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextElementSibling() : null;
            return next != null ? new JSElement(next) : Null.getInstance();
        }
    }

    class childNodesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            for (Node child: node.children) {
                array.push(new JSElement(child));
            }
            return array;
        }
    }

    class childrenFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            for (Node child: node.children) {
                if (child.nodeType == 1) {
                    array.push(new JSElement(child));
                }
            }
            return array;
        }
    }

    class getElementByIdFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Node n = node.document.getElementById(node, args.get(i).asString().getValue(), true);
                return n != null ? new JSElement(n) : Null.getInstance();
            }
            return Null.getInstance();
        }
    }

    class getElementsByTagNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = node.document.getElementsByTagName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new JSElement(node));
                }
            }
            return array;
        }
    }

    class getElementsByNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = node.document.getElementsByName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new JSElement(node));
                }
            }
            return array;
        }
    }

    class getElementsByClassNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = node.document.getElementsByClassName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new JSElement(node));
                }
            }
            return array;
        }
    }

    class getAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return new JSString(node.getAttribute(args.get(0).toString()));
        }
    }

    class hasAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return new JSBool(node.getAttribute(args.get(0).toString()) != null);
        }
    }

    class setAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            node.setAttribute(args.get(0).asString().getValue(), args.get(1).asString().getValue());
            updateClassList();
            return Undefined.getInstance();
        }
    }

    class removeAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            node.removeAttribute(args.get(0).toString());
            updateClassList();
            return Undefined.getInstance();
        }
    }

    public void updateClassList() {
        NodeClassList classes = new NodeClassList(node);
        String c = node.getAttribute("class");
        if (c != null) {
            String[] classList = c.split("\\s");
            for (int i = 0; i < classList.length; i++) {
                classes.push(new JSString(classList[i]));
            }
        }
        items.put("classList", classes);
    }

    class addEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required, but only 1 present", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(1) instanceof Function)) {
                JSError e = new JSError(null, "Argument 2 type error: Function expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            boolean capture_phase = args.size() > 2 && args.get(2).asBool().getValue();
            String event_type = args.get(0).asString().getValue();
            Vector<Function> funcs = listeners.get(event_type);
            if (funcs == null) {
                funcs = new Vector<Function>();
                if (!capture_phase) listeners.put(event_type, funcs);
                else listeners_0.put(event_type, funcs);
            }
            funcs.add((Function) args.get(1));
            return Undefined.getInstance();
        }
    }

    class removeEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required, but only 1 present", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(1) instanceof Function)) {
                JSError e = new JSError(null, "Argument 2 type error: Function expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            boolean capture_phase = args.size() > 2 && args.get(2).asBool().getValue();
            String event_type = args.get(0).asString().getValue();
            Vector<Function> funcs = !capture_phase ? listeners.get(event_type) : listeners_0.get(event_type);
            for (int i = 0; i < funcs.size(); i++) {
                if (funcs.get(i) == args.get(1)) {
                    funcs.remove(i);
                    break;
                }
            }
            return Undefined.getInstance();
        }
    }

    class addClassFunction extends Function {
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
            
            return new JSBool(true);
        }
    }

    class hasClassFunction extends Function {
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
    }

    class removeClassFunction extends Function {
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

            return new JSBool(true);
        }
    }

    public boolean equals(JSElement element) {
        return element.node == node;
    }

    @Override
    public String toString() {
        String result = "";
        Set keys = new TreeSet(items.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (str.equals("children") || str.equals("childNodes") || str.matches("(previous|next)(Element)?Sibling")) continue;
            if (items.get(str) == this) continue;
            if (result.length() > 0) result += ", ";
            result += str + ": " + items.get(str).toString();
        }
        return "HTMLElement {" + result + "}";
    }

    public static HashMap<Node, JSElement> map = new HashMap<Node, JSElement>();

    LinkedHashMap<String, Vector<Function>> listeners = new LinkedHashMap<String, Vector<Function>>();
    LinkedHashMap<String, Vector<Function>> listeners_0 = new LinkedHashMap<String, Vector<Function>>();
    HashMap<String, String> styles = new HashMap<String, String>();
    public Node node;
}
