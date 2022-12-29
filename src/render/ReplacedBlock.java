package render;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 *
 * @author Alex
 */
public class ReplacedBlock extends Block {

    public ReplacedBlock(WebDocument document, Component comp) {
        super(document);
        component = comp;
        add(component);
    }

    @Override
    public void performLayout(boolean no_rec, boolean no_viewport_reset) {
        super.performLayout(no_rec, no_viewport_reset);
        component.setBounds(_x_, _y_, width, height);
    }

    @Override
    public void draw() {}

    @Override
    public void draw(Graphics g) {}

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public Component component;
}
