package com.alstarsoft.tinybrowser.cssparser;

import com.alstarsoft.tinybrowser.htmlparser.HTMLParser;
import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.util.HashMap;

/**
 *
 * @author Alex
 */
public class StyleMap {

    public static Styles getNodeStyles(Node node) {
        try {
            HashMap<Node, Styles> map = styles.get(node.document);
        
            if (map == null) {
                map = new HashMap<Node, Styles>();
                styles.put(node.document, map);
            }
            Styles s = map.get(node);
            if (s == null && node.nodeType == 1) {
                s = new Styles();
                map.put(node, s);
            }
            return s;
        } catch (Exception ex) {
            System.err.println(node.tagName);
        }
        return null;
    }

    public static void addNodeStyles(Node node, Styles st) {
        HashMap<Node, Styles> map = styles.get(node.document);
        if (map == null) {
            map = new HashMap<Node, Styles>();
            styles.put(node.document, map);
        }
        Styles s = map.get(node);
        if (s != null) {
            s.styles.putAll(st.styles);
            s.stateStyles.addAll(st.stateStyles);
            s.beforeStyles.addAll(st.beforeStyles);
            s.afterStyles.addAll(st.afterStyles);
        } else {
            map.put(node, st);
        }
    }

    public static HashMap<Node, Styles> getDocumentStyles(HTMLParser document) {
        return styles.get(document);
    }

    public static void removeDocumentStyles(HTMLParser document) {
        styles.remove(document);
    }

    public static HashMap<HTMLParser, HashMap<Node, Styles>> styles = new HashMap<HTMLParser, HashMap<Node, Styles>>();
}