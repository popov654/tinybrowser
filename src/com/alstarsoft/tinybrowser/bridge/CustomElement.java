package com.alstarsoft.tinybrowser.bridge;

import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.awt.Component;
import com.alstarsoft.tinybrowser.render.Block;
import com.alstarsoft.tinybrowser.render.ReplacedBlock;
import com.alstarsoft.tinybrowser.render.WebDocument;

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
