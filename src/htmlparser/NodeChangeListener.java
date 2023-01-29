package htmlparser;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Alex
 */
public class NodeChangeListener {

    public NodeChangeListener(ActionListener listener, Object target, String eventType) {
        this.listener = listener;
        this.target = target;
        this.eventType = eventType;
    }

    public void fireEvent(String source) {
        listener.actionPerformed(new ActionEvent(target, Event.ACTION_EVENT, source));
    }

    public ActionListener getHandler() {
        return listener;
    }

    private ActionListener listener;

    public Object target;
    public String eventType;

}
