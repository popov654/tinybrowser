/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bridge;

import htmlparser.Node;
import java.util.Set;
import java.util.Vector;
import render.Block;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public class Builder {

    public Builder() {
        BlockElements.clear();
        BlockElements.add("div");
        BlockElements.add("p");
        BlockElements.add("blockquote");
        BlockElements.add("hr");
        BlockElements.add("br");

        InlineElements.clear();
        InlineElements.add("span");
        InlineElements.add("a");
        InlineElements.add("img");
        InlineElements.add("b");
        InlineElements.add("i");
        InlineElements.add("s");
        InlineElements.add("u");
        InlineElements.add("strong");
        InlineElements.add("em");
        InlineElements.add("cite");
        InlineElements.add("font");
    }

    public Block buildSubtree(WebDocument document, Node node) {
        Block root = buildElement(document, node);
        for (int i = 0; i < node.children.size(); i++) {
            Block b = buildSubtree(document, node.children.get(i));
            if (b != null) {
                root.getChildren().add(b);
                b.parent = root;
            }
        }
        return root;
    }

    public Block buildElement(WebDocument document, Node node) {
        Block b = new Block(document);
        if (node.nodeType == ELEMENT) {
            b.type = Block.NodeTypes.ELEMENT;
            b.width = -1;
            b.height = -1;
            b.auto_width = true;
            b.auto_height = true;
            b.text_bold = node.tagName.equals("b") || node.tagName.equals("strong");
            b.text_italic = node.tagName.equals("i") || node.tagName.equals("em");
            b.text_underline = node.tagName.equals("u");
            b.text_strikethrough = node.tagName.equals("s");
        } else if (node.nodeType == TEXT) {
            if (node.nodeValue.matches("\\s*")) {
                return null;
            }
            b.type = Block.NodeTypes.TEXT;
            b.textContent = node.nodeValue;
            b.text_bold = node.parent.tagName.equals("b") || node.parent.tagName.equals("strong");
            b.text_italic = node.parent.tagName.equals("i") || node.parent.tagName.equals("em");
            b.text_underline = node.parent.tagName.equals("u");
            b.text_strikethrough = node.parent.tagName.equals("s");
            return b;
        } else if (node.nodeType == COMMENT) {
            return null;
        }
        if (BlockElements.contains(node.tagName)) {
            b.display_type = Block.Display.BLOCK;
        } else if (InlineElements.contains(node.tagName)) {
            b.display_type = Block.Display.INLINE;
        } else if (node.tagName.equals("table")) {
            b.display_type = Block.Display.TABLE;
        } else if (node.tagName.equals("tr")) {
            b.display_type = Block.Display.TABLE_ROW;
        } else if (node.tagName.equals("td")) {
            b.display_type = Block.Display.TABLE_CELL;
            b.colspan = Integer.parseInt(node.getAttribute("colspan"));
            b.rowspan = Integer.parseInt(node.getAttribute("rowspan"));
        }
        b.id = node.getAttribute("id");
        b.setTextColor(node.getAttribute("color"));
        b.setBackgroundColor(node.getAttribute("bgcolor"));
        if (node.tagName.equals("a")) b.href = node.getAttribute("href");
        else if (node.tagName.equals("img")) {
            b.width = -1;
            b.isImage = true;
            b.setBackgroundImage(node.getAttribute("src"));
        }
        else if (node.tagName.equals("p")) {
            b.setMargins(0, 0, 12, 0);
        }
        else if (node.tagName.equals("font")) {
            if (node.getAttribute("size") != null) {
                b.setFontSize(Integer.parseInt(node.getAttribute("size")));
            }
        }
        else if (node.tagName.equals("li")) {
            b.list_item_type = 2;
        }
        applyStyles(node, b);
        applyInlineStyles(node, b);

        return b;
    }

    public void applyStyles(Node node, Block b) {
        Set<String> keys = node.styles.keySet();
        for (String key: keys) {
            if (!key.trim().isEmpty()) b.setProp(key.trim(), node.styles.get(key).trim());
        }
    }

    public void applyInlineStyles(Node node, Block b) {
        if (node.getAttribute("style") != null) {
            String[] styles = node.getAttribute("style").split("\\s*;\\s*");
            for (String style: styles) {
                String[] p = style.trim().split("\\s*:\\s*");
                if (!p[0].trim().isEmpty()) b.setProp(p[0].trim(), p[1].trim());
            }
        }
    }

    public void setDocument(Block block, WebDocument document) {
        block.document = document;
        for (int i = 0; i < block.getChildren().size(); i++) {
            setDocument(block.getChildren().get(i), document);
        }
    }

    public final static Vector<String> BlockElements = new Vector<String>();
    public final static Vector<String> InlineElements = new Vector<String>();

    public final static int ELEMENT = 1;
    public final static int TEXT = 3;
    public final static int COMMENT = 8;

}
