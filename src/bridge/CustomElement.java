package bridge;

import htmlparser.Node;
import java.awt.Component;
import render.Block;
import render.ReplacedBlock;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public abstract class CustomElement {

    public CustomElement(WebDocument document, Node node) {
        this.document = document;
        this.node = node;
        initialize();
    }

    public abstract void initialize();

    public Block createBlock(Builder b) {
        block = new ReplacedBlock(document, component);
        block.node = node;
        block.builder = b;
        if (width > 0) {
            block.setWidth(width);
        }
        if (height > 0) {
            block.setHeight(height);
        }
        return block;
    }

    protected void setComponent(Component c) {
        component = c;
    }

    public int width = 0;
    public int height = 0;

    public Block block;
    public Component component;
    public WebDocument document;
    public Node node;
}
