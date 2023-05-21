package jsparser;

import htmlparser.Node;
import htmlparser.NodeActionCallback;
import htmlparser.NodeEvent;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLNode extends JSObject {

    protected HTMLNode(Node node) {
        this.node = node;
        if (node.parent != null) ref_count = 1;
        items.put("__proto__", HTMLNodeProto.getInstance());
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
        if (items.get("firstChild") instanceof HTMLNode && items.get("firstChild").ref_count == 0) items.get("firstChild").ref_count++;
        Node last_child = node != null ? node.lastChild() : null;
        items.put("lastChild", last_child != null ? HTMLNode.create(last_child) : Null.getInstance());
        if (items.get("lastChild") instanceof HTMLNode && items.get("lastChild").ref_count == 0) items.get("lastChild").ref_count++;

        JSArray childNodes = new JSArray();
        for (Node child: node.children) {
            HTMLNode childNode = HTMLNode.create(child);
            childNodes.items.add(childNode);
            if (childNode.ref_count == 0) {
                childNode.ref_count++;
            }
        }
        childNodes.ref_count++;
        items.put("childNodes", childNodes);
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


    public HTMLDocument document;

    public static HashMap<Node, HTMLNode> map = new HashMap<Node, HTMLNode>();

    public Node node;

    protected String type = "Node";
}
