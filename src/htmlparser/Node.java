package htmlparser;

import cssparser.QuerySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Node {
    public Node() {
    }
    public Node(Node parent_node) {
        if (parent_node.nodeType == 1) {
            parent = parent_node;
            parent_node.addChild(this);
        }
        document = parent.document;
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
        document = parent.document;
    }

    public boolean addChild(Node node) {
        if (nodeType == 1) {
            children.add(node);
            if (document != null) {
                document.indexNode(this);
            }
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
            if (document != null) {
                document.removeSubtreeIndex(this);
            }
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
        if (attr.equals("id") && document != null) {
            document.getIdIndex().remove(attributes.get("id"));
            document.getIdIndex().put(val, this);
        }
        if (attr.equals("class") && document != null) {
            String[] oldClassNames = attributes.get("class").split("\\s+");
            String[] newClassNames = val.split("\\s+");
            List<String> old_list = Arrays.asList(oldClassNames);
            List<String> new_list = Arrays.asList(newClassNames);
            List<String> l1 = (List<String>)(new ArrayList<String>(old_list)).clone();
            l1.removeAll(new_list);
            List<String> l2 = (List<String>)(new ArrayList<String>(new_list)).clone();
            l2.removeAll(old_list);
            replaceValues(document.getClassIndex(), l1, l2);
        }
        if (attr.equals("name") && document != null) {
            replaceValue(document.getNamesIndex(), attributes.get("name"), val);
        }
        return attributes.put(attr, val);
    }

    public void setId(String id) {
        setAttribute("id", id);
    }

    public void addClass(String value) {
        String[] classNames = attributes.get("class").split("\\s+");
        for (String val: classNames) {
            if (val.equals(value)) return;
        }
        String new_value = attributes.get("class") + "" + value;
        setAttribute("class", new_value);
    }

    public void removeClass(String value) {
        String[] classNames = attributes.get("class").split("\\s+");
        boolean found = false;
        for (String val: classNames) {
            if (val.equals(value)) found = true;
        }
        if (!found) return;
        String new_value = attributes.get("class").replaceAll("(^|\\s+)" + value + "(\\s+|$)", "");
        setAttribute("class", new_value);
    }

    private void replaceValue(HashMap<String, Vector<Node>> index, String oldValue, String newValue) {
        Vector<Node> v = oldValue != null ?
            index.get(oldValue) : new Vector<Node>();
        if (v != null) v.remove(this);
        if (oldValue != null && v != null && v.size() > 0) {
            index.put(oldValue, v);
        } else if (oldValue != null) {
            index.remove(oldValue);
        }
        v = index.get(newValue);
        if (v == null) {
            v = new Vector<Node>();
        }
        v.add(this);
        index.put(newValue, v);
    }

    private void replaceValues(HashMap<String, Vector<Node>> index, List<String> oldValues, List<String> newValues) {
        for (String oldValue: oldValues) {
            Vector<Node> v = oldValue != null ?
                index.get(oldValue) : new Vector<Node>();
            if (v != null) v.remove(this);
            if (oldValue != null && v != null && v.size() > 0) {
                index.put(oldValue, v);
            } else if (oldValue != null) {
                index.remove(oldValue);
            }
        }

        for (String newValue: newValues) {
            Vector<Node> v = index.get(newValue);
            if (v == null) {
                v = new Vector<Node>();
            }
            v.add(this);
            index.put(newValue, v);
        }
    }

    public boolean removeAttribute(String attr) {
        if (attributes.containsKey(attr)) {
            attributes.remove(attr);
            return true;
        }
        return false;
    }

    public void addStateSelector(QuerySelector selector) {
        stateStyles.add(selector);
    }

    public Vector<QuerySelector> getStateStyles() {
        return stateStyles;
    }
    
    public Node parent;
    public Vector<Node> children = new Vector<Node>();
    public LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
    public LinkedHashMap<String, String> styles = new LinkedHashMap<String, String>();
    public Vector<QuerySelector> stateStyles = new Vector<QuerySelector>();
    public Vector<QuerySelector> beforeStyles = new Vector<QuerySelector>();
    public Vector<QuerySelector> afterStyles = new Vector<QuerySelector>();
    public Vector<String> states = new Vector<String>();
    public Node previousSibling;
    public Node nextSibling;
    public String tagName = "";
    public int nodeType = 3;
    public String nodeValue = "";
    public HTMLParser document;
}
