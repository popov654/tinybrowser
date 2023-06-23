package htmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.Request;
import tinybrowser.CharsetDetector;

/**
 *
 * @author Alex
 */
public class HTMLParser {

    public HTMLParser() {}

    public HTMLParser(String filename) {
        loadFile(filename);
        scan();
    }

    public Node getRootNode() {
        return root;
    }

    public void printTree() {
        printNode(root, 0);
    }

    public void traverseTree(Callback callback) {
        processNode(root, 0, callback);
    }

    private void processNode(Node node, int level, Callback callback) {
        if (!(node.nodeType == 3 && node.nodeValue.matches("^\\s+$"))) {
            try {
                callback.process(node);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (int i = 0; i < node.children.size(); i++) {
            processNode(node.children.get(i), level+1, callback);
        }
    }

    public interface Callback {
        public void process(Node node);
    }

    private void printNode(Node node, int level) {
        if (!(node.nodeType == 3 && node.nodeValue.matches("^\\s+$"))) {
            for (int i = 0; i < level; i++) {
                System.out.print("----");
            }
            System.out.println(node.nodeType + ": " + (node.nodeType == 1 ? node.tagName : node.nodeValue));
        }
        for (int i = 0; i < node.children.size(); i++) {
            printNode(node.children.get(i), level+1);
        }
    }

    public void setData(String data) {
        this.data = data;
        this.pos = 0;
        scan();
    }

    public void loadFile(String filename) {
        if (filename.startsWith("http")) {
            data = Request.makeRequest(filename);
            System.out.println(data);
            return;
        }
        ObjectInputStream is = null;
        charset = CharsetDetector.detectCharset(new File(filename));
        try {
            FileInputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            char[] buf = new char[4096];
            int result = 0;
            while (result != -1) {
                result = br.read(buf);
                if (result > 0) {
                    data += (new StringBuilder()).append(Arrays.copyOf(buf, result)).toString();
                }
            }
            br.close();
            in.close();
            if (charset.displayName().startsWith("UTF")) {
                data = new String(data.getBytes(), charset);
            }
        } catch (IOException ex) {
            Logger.getLogger(HTMLParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {
                Logger.getLogger(HTMLParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(data);
    }

    private void addTextNode() {
        if (!cur_text.isEmpty()) {
            if (curNode == root) return;
            Node text = new Node(curNode, 3);
            text.nodeValue = cur_text;
        }
        cur_text = "";
        last_start = pos+1;
    }

    private void scanNext() {
        if (pos == 0) {
            TagLibrary.init();
            root.document = this;
            curNode = root;
            root.document = this;
            root.tagName = "html";
        }
        if (pos == 0 && data.substring(pos, pos+2).equals("<!")) {
            while (pos < data.length() && data.charAt(pos) != '>') pos++;
            pos++;
        }
        if (pos >= data.length()) return;
        char ch = data.charAt(pos);
        boolean ignoreOpenTag = pos < data.length()-1 && data.substring(pos, pos+2).matches("<[a-z]") && curNode != null && curNode.isLeaf();
        if (state == READY && pos < data.length() - 4 && data.substring(pos, pos + 4).equals("<!--")) {
            state = READ_COMMENT;
            last_start = pos+4;
            cur_tag = "";
            cur_text = "";
            pos += 3;
        } else if (state == READ_COMMENT && pos < data.length() - 3 && data.substring(pos, pos + 3).equals("-->")) {
            state = READY;
            last_start = pos+3;
            cur_tag = "";
            pos += 2;
            if (last_start >= 0 && !cur_text.isEmpty()) {
                Node text = new Node(curNode, 8);
                text.nodeValue = cur_text;
            }
            cur_text = "";
        } else if (state == READY && (ch != '<' || ignoreOpenTag) || state == READ_COMMENT) {
            cur_text += ch;
        } else if (state == READY && ch == '<') {
            cur_text += ch;
            state = READ_TAGNAME;
            cur_tag = "";
        } else if (state == READ_TAGNAME && (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z')) {
            cur_tag += ch;
            cur_text += ch;
        } else if (state == READ_TAGNAME && cur_tag.isEmpty() && ch == '/') {
            closing = true;
        } else if (state == READ_TAGNAME) {
            state = SEEK_END;
            if (ch != ' ' && ch != '	' && ch != '>') {
                // Malformed HTML tag of false positive
                cur_tag = "";
                state = READY;
                cur_text += ch;
            } else {
                // Add accumulated text before <
                if (cur_text.length() >= cur_tag.length() + 1) {
                    cur_text = cur_text.substring(0, cur_text.length() - cur_tag.length() - 1);
                }
                addTextNode();
            }
        } else if (state == SEEK_END && !cur_tag.isEmpty()) {
            if (state == SEEK_END && (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z')) {
                state = READ_ATTRIBUTE_NAME;
                cur_attr_name = "";
                cur_attr_name += ch;
            }
        } else if (state == READ_ATTRIBUTE_NAME && !cur_attr_name.isEmpty() && cur_attr_value.isEmpty()) {
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '-' || ch == '_') {
                cur_attr_name += ch;
            } else if (ch == '=') {
                state = READ_ATTRIBUTE_VALUE;
                cur_attr_value = "";
            } else if (!Character.isWhitespace(ch) && ch != '=') {
                if (Character.isWhitespace(ch) || ch == '>') {
                    attrs.put(cur_attr_name.toLowerCase(), "");
                }
                cur_attr_name = "";
                state = SEEK_END;
            }
        } else if (state == READ_ATTRIBUTE_VALUE && !cur_attr_name.isEmpty()) {
            if (!quotes && ch == '"' && cur_attr_value.isEmpty()) {
                quotes = true;
            } else if (ch != '"' && (quotes || ch != '=' && ch != ' ' && ch != '	')) {
                cur_attr_value += ch;
            } else if (!quotes && (ch == '>' || ch != ' ' || ch != '	') || quotes && ch == '"') {
                attrs.put(cur_attr_name.toLowerCase(), cur_attr_value);
                cur_attr_name = "";
                cur_attr_value = "";
                state = SEEK_END;
                if (quotes) {
                    pos++;
                    ch = data.charAt(pos);
                }
                quotes = false;
            }
        }
        if (state == SEEK_END && !cur_tag.isEmpty() && ch == '>' && !closing) {
            state = READY;
            Node parentNode = null;
            if (curNode != null) {
                parentNode = curNode;
            }
            if (parentNode.tagName.equals("html") && cur_tag.toLowerCase().equals("html")) {
                curNode = parentNode;
            } else {
                curNode = new Node(parentNode, 1);
            }
            if (attrs.containsKey("id")) {
                ids.put(attrs.get("id"), curNode);
            }
            if (attrs.containsKey("class")) {
                String[] classNames = attrs.get("class").split("\\s+");
                for (int i = 0; i < classNames.length; i++) {
                    if (!classes.containsKey(classNames[i])) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(curNode);
                        classes.put(classNames[i], v);
                    } else {
                        Vector<Node> v = classes.get(classNames[i]);
                        if (!v.contains(curNode)) v.add(curNode);
                    }
                }
            }
            if (attrs.containsKey("name")) {
                String name = attrs.get("name");
                if (!names.containsKey(name)) {
                    Vector<Node> v = new Vector<Node>();
                    v.add(curNode);
                    names.put(name, v);
                } else {
                    Vector<Node> v = names.get(name);
                    if (!v.contains(curNode)) v.add(curNode);
                }
            }
            curNode.tagName = cur_tag.toLowerCase();
            curNode.attributes = (LinkedHashMap<String, String>)attrs.clone();
            if (cur_tag.toLowerCase().equals("html")) {
                root = curNode;
            }
            attrs.clear();
            if (data.charAt(pos-1) == '/' || TagLibrary.tags.get(cur_tag) != null && TagLibrary.tags.get(cur_tag) == false) {
                curNode = parentNode;
            }
            cur_text = "";
            last_start = pos+1;
        } else if (state == SEEK_END && !cur_tag.isEmpty() && ch == '>' && closing) {
            // Add inner text fragment
            if (cur_text.length() >= cur_tag.length() + 1) {
                cur_text = cur_text.substring(0, cur_text.length() - cur_tag.length() - 1);
            }
            addTextNode();

            Node p = curNode;
            while (p != null && !p.tagName.toLowerCase().equals(cur_tag.toLowerCase())) {
                p = p.parent;
            }
            if (p != null) {
                curNode = p.parent;
            }
            closing = false;
            state = READY;
        }
        pos++;
    }

    public void scan() {
        while (pos < data.length()-1) scanNext();
        if (state != READY && !cur_tag.isEmpty()) {
            data += '>';
            scanNext();
        }
        if (!cur_text.isEmpty() && curNode != null) {
            Node text = new Node(curNode, 3);
            text.nodeValue = cur_text;
            cur_text = "";
        }
    }

    public void indexSubtree(Node node) {
        indexNode(node);
    }

    public void indexNode(Node node) {
        if (node.nodeType == 1) {
            if (node.attributes.containsKey("id")) {
                ids.put(node.attributes.get("id"), node);
            }
            if (node.attributes.containsKey("class")) {
                String[] classNames = node.attributes.get("class").split("\\s+");
                for (int i = 0; i < classNames.length; i++) {
                    if (!classes.containsKey(classNames[i])) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(node);
                        classes.put(classNames[i], v);
                    } else {
                        Vector<Node> v = classes.get(classNames[i]);
                        if (!v.contains(node)) v.add(node);
                    }
                }
            }
            if (node.attributes.containsKey("name")) {
                String name = node.attributes.get("name");
                if (!names.containsKey(name)) {
                    Vector<Node> v = new Vector<Node>();
                    v.add(node);
                    names.put(name, v);
                } else {
                    Vector<Node> v = names.get(name);
                    if (!v.contains(node)) v.add(node);
                }
            }
        }
        for (int i = 0; i < node.children.size(); i++) {
            indexNode(node.children.get(i));
        }
    }

    public void removeSubtreeIndex(Node node) {
        removeNodeIndex(node);
    }

    private void removeNodeIndex(Node node) {
        if (node.nodeType == 1) {
            if (node.attributes.containsKey("id")) {
                ids.remove(node.attributes.get("id"));
            }
            if (node.attributes.containsKey("class")) {
                String[] classNames = node.attributes.get("class").split("\\s+");
                for (int i = 0; i < classNames.length; i++) {
                    if (classes.containsKey(classNames[i])) {
                        classes.get(classNames[i]).remove(node);
                        if (classes.get(classNames[i]).size() == 0) {
                            classes.remove(classNames[i]);
                        }
                    }
                }
            }
            if (node.attributes.containsKey("name")) {
                String name = node.attributes.get("name");
                if (names.containsKey(name)) {
                    names.get(name).remove(node);
                    if (names.get(name).size() == 0) {
                        names.remove(name);
                    }
                }
            }
        }
        for (int i = 0; i < node.children.size(); i++) {
            removeNodeIndex(node.children.get(i));
        }
    }

    public HashMap<String, Vector<Node>> getNamesIndex() {
        return names;
    }

    public HashMap<String, Vector<Node>> getClassIndex() {
        return classes;
    }

    public HashMap<String, Node> getIdIndex() {
        return ids;
    }

    public Vector<Node> getElementsByClassName(String str) {
        return getElementsByClassName(null, str, true);
    }

    public Vector<Node> getElementsByClassName(Node node, String str, boolean deep) {
        str = str.replaceAll("^\\.", "");
        String[] classNames = str.split("[\\. ]");
        Vector<Node> results = new Vector<Node>();
        for (int i = 0; i < classNames.length; i++) {
            if (!classes.containsKey(classNames[i])) {
                results.clear();
                return results;
            }
            if (results.isEmpty()) {
                Vector<Node> v = classes.get(classNames[i]);
                if (node != null) filter(v, node, deep);
                results.addAll(v);
            } else {
                Vector<Node> v = classes.get(classNames[i]);
                if (node != null) filter(v, node, deep);
                results.retainAll(v);
            }
        }
        return results;
    }

    private void filter(Vector<Node> v, Node node, boolean deep) {
        for (int i = 0; i < v.size(); i++) {
            if (!deep && !v.get(i).parent.equals(node)) {
                v.remove(i);
            } else if (deep) {
                Node n = v.get(i);
                boolean flag = false;
                while (n.parent != null) {
                    if (n.parent.equals(node)) {
                        flag = true;
                    }
                }
                if (!flag) v.remove(i);
            }
        }
    }

    public Vector<Node> getElementsByTagName(String tag) {
        return getElementsByTagName(null, tag, true);
    }

    public Vector<Node> getElementsByTagName(Node node, String tag, boolean deep) {
        if (node == null) node = root;
        Vector<Node> results = new Vector<Node>();
        findChildElementsByTag(results, node, tag, deep);
        return results;
    }

    private void findChildElementsByTag(Vector<Node> results, Node node, String tag, boolean deep) {
        for (int i = 0; i < node.children.size(); i++) {
            if (node.children.get(i).nodeType == 1 && node.children.get(i).tagName.equals(tag)) {
                results.add(node.children.get(i));
            }
            if (deep) findChildElementsByTag(results, node.children.get(i), tag, true);
        }
    }

    public Vector<Node> getElementsByName(String name) {
        if (name == null) return new Vector<Node>();
        Vector<Node> v = names.get(name);
        if (v == null) v = new Vector<Node>();
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getAttribute("name") == null || !v.get(i).getAttribute("name").equals(name)) {
                v.remove(i--);
            }
        }
        return v;
    }

    public Vector<Node> getElementsByName(Node node, String name, boolean deep) {
        if (node == null) node = root;
        Vector<Node> results = new Vector<Node>();
        findChildElementsByName(results, node, name, deep);
        return results;
    }

    private void findChildElementsByName(Vector<Node> results, Node node, String name, boolean deep) {
        for (int i = 0; i < node.children.size(); i++) {
            if (node.children.get(i).nodeType == 1 && node.children.get(i).getAttribute("name") != null &&
                  node.children.get(i).getAttribute("name").equals(name)) {
                results.add(node.children.get(i));
            }
            if (deep) findChildElementsByName(results, node.children.get(i), name, true);
        }
    }

    public Node getElementById(String str) {
        str = str.replaceAll("^\\#", "");
        return getElementById(null, str, true);
    }

    public Node getElementById(Node node, String str, boolean deep) {
        if (!ids.containsKey(str)) {
            return null;
        }
        Node result = ids.get(str);
        if (node != null) {
            if (!deep && !result.parent.equals(node)) return null;
            if (deep) {
                Node n = result;
                boolean flag = false;
                while (n.parent != null) {
                    if (n.parent.equals(node)) {
                        flag = true;
                    }
                }
                if (!flag) return null;
            }
        }
        return result.getAttribute("id").equals(str) ? result : null;
    }

    private int state;

    private int READY = 0;
    private int SEEK_END = 1;
    private int READ_TAGNAME = 2;
    private int READ_ATTRIBUTE_NAME = 3;
    private int READ_ATTRIBUTE_VALUE = 4;
    private int READ_COMMENT = 5;

    private int pos = 0;
    private String cur_tag = "";
    private String cur_attr_name = "";
    private String cur_attr_value = "";
    private String cur_text = "";
    private int last_start = -1;
    private boolean quotes = false;
    private boolean closing = false;
    private LinkedHashMap<String, String> attrs = new LinkedHashMap<String, String>();
    private Node curNode = null;
    private Node root = new Node(1);
    private HashMap<String, Node> ids = new HashMap<String, Node>();
    private HashMap<String, Vector<Node>> classes = new HashMap<String, Vector<Node>>();
    private HashMap<String, Vector<Node>> names = new HashMap<String, Vector<Node>>();

    public String data = "";
    public Charset charset;
}
