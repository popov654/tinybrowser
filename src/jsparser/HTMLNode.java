package jsparser;

import bridge.Mapper;
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
public class HTMLNode extends JSObject {

    protected HTMLNode(Node node) {
        this.node = node;
        //node.addListener(eventListener, node, "any");
    }

    NodeActionCallback eventListener = new NodeActionCallback() {

        @Override
        public void nodeChanged(NodeEvent e, String source) {}

    };

    protected Vector<HTMLElement> getParents() {
        Vector<HTMLElement> result = new Vector<HTMLElement>();
        JSValue p = items.get("parentNode");
        while (p != null && p instanceof HTMLElement) {
            result.add((HTMLElement)p);
            p = ((HTMLElement)p).items.get("parentNode");
        }
        return result;
    }

    public static HTMLNode create(Node node) {
        if (node.nodeType == 1) {
            return HTMLElement.create(node);
        }
        HTMLNode element = map.get(node);
        if (element == null) {
            element = new HTMLNode(node);
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

        updateChildren();
    }

    public void updateChildren() {
        Node first_child = node != null ? node.firstChild() : null;
        items.put("firstChild", first_child != null ? HTMLNode.create(first_child) : Null.getInstance());
        Node last_child = node != null ? node.lastChild() : null;
        items.put("lastChild", last_child != null ? HTMLNode.create(last_child) : Null.getInstance());

        JSArray childNodes = new JSArray();
        for (Node child: node.children) {
            childNodes.push(HTMLNode.create(child));
        }
        items.put("childNodes", childNodes);
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
            return parent != null ? (parent.nodeType == 1 ? HTMLElement.create(parent) : HTMLNode.create(parent)) : Null.getInstance();
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
            return prev != null ? (prev.nodeType == 1 ? HTMLElement.create(prev) : HTMLNode.create(prev)) : Null.getInstance();
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
            return next != null ? (next.nodeType == 1 ? HTMLElement.create(next) : HTMLNode.create(next)) : Null.getInstance();
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
            return prev != null ? (prev.nodeType == 1 ? HTMLElement.create(prev) : HTMLNode.create(prev)) : Null.getInstance();
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
            return next != null ? (next.nodeType == 1 ? HTMLElement.create(next) : HTMLNode.create(next)) : Null.getInstance();
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
            return array;
        }
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.equals("nodeValue")) {
            node.nodeValue = value.asString().getValue();
            items.put("nodeValue", new JSString(node.nodeValue));
            items.put("textContent", new JSString(node.getTextContent()));
            node.fireEvent("valueChanged", "node");
        }
        super.set(str, value);
    }

    @Override
    public void set(JSString str, JSValue value) {
        if (str.getValue().equals("nodeValue")) {
            node.nodeValue = value.asString().getValue();
            items.put("nodeValue", new JSString(node.nodeValue));
            items.put("textContent", new JSString(node.getTextContent()));
            node.fireEvent("valueChanged", "node");
        }
        super.set(str, value);
    }

    public boolean equals(HTMLNode element) {
        return element.node == node;
    }

    @Override
    public String toString() {
        String type = node.nodeType == 3 ? "Text" : (node.nodeType == 8 ? "Comment" : "");
        return type + "Node \"" + node.nodeValue + "\"";
    }

    public static HashMap<Node, HTMLNode> map = new HashMap<Node, HTMLNode>();

    public Node node;
}
