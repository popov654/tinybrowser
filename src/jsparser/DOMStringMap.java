package jsparser;

import htmlparser.Node;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Alex
 */
public class DOMStringMap extends JSObject {

    public DOMStringMap(HashMap<String, String> map) {
        Set<String> keys = map.keySet();
        for (String key: keys) {
            set(key, new JSString(map.get(key)));
        }
    }

    public DOMStringMap(HashMap<String, String> map, Node node) {
        this(map);
        this.node = node;
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
