package com.alstarsoft.tinybrowser.htmlparser;

import java.util.HashMap;

/**
 *
 * @author Alex
 */
public class NodeChangeListener {

    public NodeChangeListener(NodeActionCallback listener, Object target, String eventType) {
        this.listener = listener;
        this.target = target;
        this.eventType = eventType;
    }

    public void fireEvent(String source) {
        listener.nodeChanged(new NodeEvent(target), source);
    }

    public void fireEvent(String source, HashMap<String, String> eventData, Node relatedNode) {
        listener.nodeChanged(new NodeEvent(target, relatedNode, eventData), source);
    }

    public NodeActionCallback getHandler() {
        return listener;
    }

    private NodeActionCallback listener;

    public Object target;
    public String eventType;

}
