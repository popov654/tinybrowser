package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Alex
 */
public class DOMStringMap extends JSObject {

    public DOMStringMap(HashMap<String, String> map, Node node) {
        this.node = node;
        Set<String> keys = map.keySet();
        for (String key: keys) {
            items.put(key, new JSString(map.get(key)));
        }
    }

    public DOMStringMap(HashMap<String, String> map) {
        this(map, null);
    }

    public void updateNode() {}

    @Override
    public void set(String key, JSValue value) {
        value = value.asString();
        super.set(key, value);
        updateNode();
    }

    @Override
    public String toString() {
        return "DOMStringMap" + super.toString();
    }

    Node node;
}
