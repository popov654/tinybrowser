package render;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Animator implements ActionListener {

    public Animator(Block block) {
        this.block = block;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (block.document == null) return;
        
        int count = 0;
        if (block.document.lastSetProperties == null) {
            block.document.lastSetProperties = new HashSet<String>();
        }
        block.document.lastSetProperties.clear();
        int old_width = block.viewport_width;
        int old_height = block.viewport_height;

        boolean passiveMode = true;

        synchronized (block) {
            Vector<Transition> transitions = (Vector<Transition>) block.activeTransitions.clone();
            for (Transition t: transitions) {
                if (t.block != block) continue;
                if (!t.passiveMode) passiveMode = false;
                block.document.lastSetProperties.add(t.property);
                t.update();
                count++;
            }
        }

        if (passiveMode) {
            block.document.smartUpdate(block, old_width, old_height);
        } else {
            block.document.lastSetProperties.clear();
        }

        if (count == 0) {
            block.animator.stop();
            block.animator = null;
        }
    }

    public final Block block;
}
