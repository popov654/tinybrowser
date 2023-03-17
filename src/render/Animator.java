package render;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        int count = 0;
        for (Transition t: block.transitions) {
            if (t.block != block) continue;
            t.update();
            count++;
        }
        if (count == 0) {
            block.animator.stop();
            block.animator = null;
        }
    }

    public Block block;
}
