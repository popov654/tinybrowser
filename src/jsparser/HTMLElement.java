package jsparser;

import cssparser.StyleMap;
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
public class HTMLElement extends JSObject {

    private HTMLElement(Node node) {
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
                updateStyles();
                updateAttributesList();
                updateDataset();
                return;
            }
            HashMap<String, String> data = e.getData();
            //if (data != null) System.out.println(data.get("pageX") + ", " + data.get("pageY"));
            Vector<HTMLElement> parents = getParents();
            if (data != null) data.put("bubbles", parents.size() > 1 ? "true" : "false");
            JSEvent event = new JSEvent(HTMLElement.create(node), e.relatedTarget != null ? HTMLElement.create(e.relatedTarget) : null, data);
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

    private Vector<HTMLElement> getParents() {
        Vector<HTMLElement> result = new Vector<HTMLElement>();
        result.add(this);
        JSValue p = items.get("parentNode");
        while (p != null && p instanceof HTMLElement) {
            result.add((HTMLElement)p);
            p = ((HTMLElement)p).items.get("parentNode");
        }
        return result;
    }

    public static HTMLElement create(Node node) {
        HTMLElement element = map.get(node);
        if (element == null) {
            element = new HTMLElement(node);
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
        items.put("parentNode", parent != null ? HTMLElement.create(parent) : Null.getInstance());
        Node prev = node != null ? node.previousSibling : null;
        items.put("previousSibling", prev != null ? HTMLElement.create(prev) : Null.getInstance());
        Node next = node != null ? node.nextSibling : null;
        items.put("nextSibling", next != null ? HTMLElement.create(next) : Null.getInstance());
        Node prev_el = node != null ? node.previousElementSibling() : null;
        items.put("previousElementSibling", prev_el != null ? HTMLElement.create(prev_el) : Null.getInstance());
        Node next_el = node != null ? node.nextElementSibling() : null;
        items.put("nextElementSibling", next_el != null ? HTMLElement.create(next_el) : Null.getInstance());

        JSArray childNodes = new JSArray();
        for (Node child: node.children) {
            childNodes.push(HTMLElement.create(child));
        }
        items.put("childNodes", childNodes);
        
        JSArray children = new JSArray();
        for (Node child: node.children) {
            if (child.nodeType == 1) {
                children.push(HTMLElement.create(child));
            }
        }
        items.put("children", children);

        updateClassList();
        updateStyles();
        updateAttributesList();
        updateDataset();
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
            return parent != null ? new HTMLElement(parent) : Null.getInstance();
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
            return prev != null ? new HTMLElement(prev) : Null.getInstance();
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
            return next != null ? new HTMLElement(next) : Null.getInstance();
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
            return prev != null ? new HTMLElement(prev) : Null.getInstance();
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
            return next != null ? new HTMLElement(next) : Null.getInstance();
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
                array.push(new HTMLElement(child));
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
                    array.push(new HTMLElement(child));
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
                return n != null ? new HTMLElement(n) : Null.getInstance();
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
                    array.push(new HTMLElement(node));
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
                    array.push(new HTMLElement(node));
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
                    array.push(new HTMLElement(node));
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
            return new JSString(node.getAttribute(args.get(0).asString().getValue()));
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
            return new JSBool(node.getAttribute(args.get(0).asString().getValue()) != null);
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
            String key = args.get(0).asString().getValue();
            node.setAttribute(key, args.get(1).asString().getValue());
            updateAttributesList();
            updateClassList();
            if (key.startsWith("data-")) {
                getDataset().items.put(key.substring(5), args.get(1).asString());
            }
            if (key.equals("style")) {
                updateStyles();
            }
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
            String key = args.get(0).asString().getValue();
            node.removeAttribute(key);
            updateAttributesList();
            updateClassList();
            if (key.startsWith("data-")) {
                getDataset().items.remove(key.substring(5));
            }
            if (key.equals("style")) {
                updateStyles();
            }
            return Undefined.getInstance();
        }
    }

    public DOMStringMap getDataset() {
        return items.get("dataset") != null ? (DOMStringMap) items.get("dataset") : null;
    }

    public DOMStringMap getComputedStyles() {
        DOMStringMap style = new DOMStringMap(StyleMap.getNodeStyles(node).styles, node);
        return style;
    }

    public void updateStyles() {
        DOMStringMap style = new DOMStringMap(StyleMap.getNodeStyles(node).runtimeStyles, node) {
            @Override
            public void updateNode() {
                if (node.document == null) return;
                HashMap<String, String> st = StyleMap.getNodeStyles(node).runtimeStyles;
                st.clear();
                Set<String> keys = items.keySet();
                for (String key: keys) {
                    if (key.equals("__proto__")) continue;
                    st.put(key, items.get(key).asString().getValue());
                }
                node.fireEvent("stylesChanged", "node");
            }
        };
        items.put("style", style);
    }

    public void updateAttributesList() {
        Vector<JSValue> attrs = new Vector<JSValue>();
        for (String str: node.attributes.keySet()) {
            if (str.equals("__proto__")) continue;
            attrs.add(new JSString(str));
        }
        items.put("attributes", new JSArray(attrs) {
            @Override
            public JSArray push(JSValue value) {
                return this;
            }
            @Override
            public JSArray pop() {
                return this;
            }
            @Override
            public JSArray shift() {
                return this;
            }
            @Override
            public JSArray unshift(JSValue value) {
                return this;
            }
        });
    }

    public void updateDataset() {
        HashMap<String, String> data = new HashMap<String, String>();
        Set<String> keys = node.attributes.keySet();
        for (String key: keys) {
            if (key.startsWith("data-")) {
                data.put(key.substring(5), node.attributes.get(key));
            }
        }
        DOMStringMap attrs = new DOMStringMap(data, node);
        items.put("dataset", attrs);
    }

    public void updateClassList() {
        NodeClassList classes = new NodeClassList(node);
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

    public boolean equals(HTMLElement element) {
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

    public static HashMap<Node, HTMLElement> map = new HashMap<Node, HTMLElement>();

    LinkedHashMap<String, Vector<Function>> listeners = new LinkedHashMap<String, Vector<Function>>();
    LinkedHashMap<String, Vector<Function>> listeners_0 = new LinkedHashMap<String, Vector<Function>>();
    HashMap<String, String> styles = new HashMap<String, String>();
    public Node node;
}
