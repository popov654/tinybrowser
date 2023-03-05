package bridge;

import htmlparser.Node;
import jsparser.Block;

/**
 *
 * @author Alex
 */
public class ScriptElement {

    public ScriptElement(Node node) {
        this.isExternal = node.hasAttribute("src");
        this.node = node;
        if (!isExternal) {
            content = node.children.get(0).nodeValue;
        }
        isAsync = node.hasAttribute("async");
    }

    public void setBody(Block block) {
        body = block;
    }

    public Block getBody() {
        return body;
    }

    private Block body;

    boolean isExternal = false;
    boolean isAsync = false;

    volatile boolean loaded = false;
    volatile boolean compiled = false;
    volatile boolean finished = false;

    Node node;
    String content;
}
