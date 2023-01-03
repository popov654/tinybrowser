package htmlparser;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Node {
    public Node() {
    }
    public Node(Node parent_node) {
        parent = parent_node;
    }

    public Node(int node_type) {
        nodeType = node_type;
    }

    public Node(Node parent_node, int node_type) {
        if (parent_node.nodeType == 1) {
            parent = parent_node;
            parent_node.addChild(this);
        }
        nodeType = node_type;
    }

    public boolean addChild(Node node) {
        if (nodeType == 1) {
            children.add(node);
            return true;
        }
        return false;
    }

    public Node nthChild(int index) {
        if (index > 0 && index <= children.size()) {
            return children.get(index-1);
        } else {
            return null;
        }
    }

    public Node nthElementChild(int index) {
        int j = 1;
        for (int i = 0; i < children.size(); i++) {
            if (j == index && children.get(i).nodeType == 1) {
                return children.get(i);
            }
            if (children.get(i).nodeType == 1) j++;
        }
        return null;
    }

    public Node firstChild() {
        if (children.size() > 0) {
            return children.get(0);
        } else {
            return null;
        }
    }

    public Node firstElementChild() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).nodeType == 1) {
                return children.get(i);
            }
        }
        return null;
    }

    public Node lastChild() {
        if (children.size() > 0) {
            return children.get(children.size()-1);
        } else {
            return null;
        }
    }

    public Node lastElementChild() {
        for (int i = children.size()-1; i >= 0; i--) {
            if (children.get(i).nodeType == 1) {
                return children.get(i);
            }
        }
        return null;
    }

    public boolean isFirstChild() {
        if (parent == null) {
            return false;
        }
        return this.equals(parent.firstChild());
    }

    public boolean isFirstElementChild() {
        if (parent == null || this.nodeType != 1) {
            return false;
        }
        return this.equals(parent.firstElementChild());
    }

    public boolean isLastChild() {
        if (parent == null) {
            return false;
        }
        return this.equals(parent.lastChild());
    }

    public boolean isLastElementChild() {
        if (parent == null || this.nodeType != 1) {
            return false;
        }
        return this.equals(parent.lastElementChild());
    }

    public boolean isNthChild(int index) {
        if (parent == null) {
            return false;
        }
        return this.equals(parent.nthChild(index));
    }

    public boolean isNthElementChild(int index) {
        if (parent == null || this.nodeType != 1) {
            return false;
        }
        return this.equals(parent.nthElementChild(index));
    }

    public boolean isOddElementChild() {
        if (parent == null) return true;
        for (int i = 0; i < parent.children.size(); i++) {
            if (this.equals(parent.children.get(i))) {
                return i % 2 == 0;
            }
        }
        return false;
    }

    public boolean isEvenElementChild() {
        if (parent == null) return false;
        for (int i = 0; i < parent.children.size(); i++) {
            if (this.equals(parent.children.get(i))) {
                return i % 2 == 1;
            }
        }
        return false;
    }

    public Node previousElementSibling() {
        for (int i = 0; i < parent.children.size(); i++) {
            if (this.equals(parent.children.get(i))) {
                return i > 0 ? parent.children.get(i-1) : null;
            }
        }
        return null;
    }

    public Node nextElementSibling() {
        for (int i = 0; i < parent.children.size(); i++) {
            if (this.equals(parent.children.get(i))) {
                return i < parent.children.size() ? parent.children.get(i+1) : null;
            }
        }
        return null;
    }

    public int getNestingLevel() {
        if (parent == null) {
            return 0;
        }
        int n = 0;
        Node node = this;
        while (node.parent != null) {
            node = node.parent;
            n++;
        }
        return n;
    }

    public boolean removeSubtree() {
        if (parent != null) {
            parent.children.remove(this);
            return true;
        }
        return false;
    }

    public boolean removeChild(int index) {
        if (index >= 1 && index <= children.size()) {
            children.remove(index - 1);
            return true;
        }
        return false;
    }

    public Node replaceSubtree(Node node) {
        this.children.clear();
        this.children = (Vector<Node>)node.children.clone();
        for (int i = 0; i < this.children.size(); i++) {
            this.children.get(i).parent = this;
        }
        node.removeSubtreeFromIndex();
        indexSubtree();
        return this;
    }

    public Node replaceSubtreeAndSelf(Node node) {
        node.parent = this.parent;
        Vector<Node> nodes = parent.children;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).equals(this)) {
                nodes.setElementAt(node, i);
            }
        }
        this.parent = null;
        removeSubtreeFromIndex();
        this.children.clear();
        node.indexSubtree();
        return node;
    }

    public void replaceInnerHTML(String data) {
        replaceSubtree((new HTMLFragmentParser(data)).getRootNode());
    }

    public void replaceOuterHTML(String data) {
        replaceSubtreeAndSelf((new HTMLFragmentParser(data)).getRootNode());
    }

    public void removeSubtreeFromIndex() {
        Node n = this;
        while (n.parent != null) n = n.parent;
        if (n.document  == null) return;
        n.document.removeSubtreeIndex(this);
    }

    private void indexSubtree() {
        Node n = this;
        while (n.parent != null) n = n.parent;
        if (n.document  == null) return;
        n.document.indexSubtree(this);
    }

    public boolean setTagName(String name) {
        if (nodeType == 1) {
            tagName = name;
            return true;
        }
        return false;
    }

    public boolean hasAttribute(String attr) {
        return attributes.containsKey(attr);
    }

    public String getAttribute(String attr) {
        return attributes.get(attr);
    }

    public String setAttribute(String attr, String val) {
        return attributes.put(attr, val);
    }

    public boolean removeAttribute(String attr) {
        if (attributes.containsKey(attr)) {
            attributes.remove(attr);
            return true;
        }
        return false;
    }
    
    public Node parent;
    public Vector<Node> children = new Vector<Node>();
    public Hashtable<String, String> attributes = new Hashtable<String, String>();
    public Node previousSibling;
    public Node nextSibling;
    public String tagName = "";
    public int nodeType = 3;
    public String nodeValue = "";
    public HTMLParser document;
}
