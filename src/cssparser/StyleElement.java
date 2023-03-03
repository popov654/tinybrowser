package cssparser;

import htmlparser.Node;

/**
 *
 * @author Alex
 */
public class StyleElement {

    public StyleElement(Node node) {
        this.isExternal = node.tagName.equals("link");
        this.node = node;
        if (!isExternal) {
            content = node.children.get(0).nodeValue;
        }
    }

    boolean isExternal = false;
    Node node;
    String content;
}
