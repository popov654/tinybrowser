package htmlparser;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 *
 * @author Alex
 */
public class NodeChangeListener {

    public NodeChangeListener(NodeActionListener listener, Object target, String eventType) {
        this.listener = listener;
        this.target = target;
        this.eventType = eventType;
    }

    public void fireEvent(String source) {
        listener.actionPerformed(new ActionEvent(target, Event.ACTION_EVENT, source));
    }

    public void fireEvent(String source, HashMap<String, String> eventData) {
        listener.setEventData(eventData);
        fireEvent(source);
    }

    public NodeActionListener getHandler() {
        return listener;
    }

    private NodeActionListener listener;

    public Object target;
    public String eventType;

}
