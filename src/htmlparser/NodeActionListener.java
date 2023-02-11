package htmlparser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 *
 * @author Alex
 */
public class NodeActionListener implements ActionListener {

    public void setEventData(HashMap<String, String> data) {
        this.data = data;
    }

    public void setRelatedTarget(Node node) {
        this.relatedTarget = node;
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    public HashMap<String, String> data;
    public Node relatedTarget;
}
