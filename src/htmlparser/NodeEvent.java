package htmlparser;

import java.util.HashMap;

/**
 *
 * @author Alex
 */
public class NodeEvent extends javax.swing.event.ChangeEvent {

    public NodeEvent(Object source) {
        super(source);
        if (source instanceof Node) {
            target = (Node)source;
        }
    }

    public NodeEvent(Object source, Object relatedSource) {
        super(source);
        if (source instanceof Node) {
            target = (Node)source;
        }
        if (relatedSource instanceof Node) {
            relatedTarget = (Node)relatedSource;
        }
    }

    public NodeEvent(Object source, HashMap<String, String> data) {
        super(source);
        if (source instanceof Node) {
            target = (Node)source;
        }
        this.data = (HashMap<String, String>) data.clone();
    }

    public NodeEvent(Object source, Object relatedSource, HashMap<String, String> data) {
        this(source, relatedSource);
        this.data = (HashMap<String, String>) data.clone();
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public HashMap<String, String> data = new HashMap<String, String>();

    public Node target;
    public Node relatedTarget;
}
