package bridge;

import cssparser.QuerySelector;
import htmlparser.Node;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import render.Block;
import render.MediaPlayer;
import render.RoundedBorder;
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

    public Builder(WebDocument document) {
        this();
        setDocument(document);
    }

    public Block buildSubtree(WebDocument document, Block parent, Node node) {
        Block root = buildElement(document, parent, node);
        if (root == null) return root;
        if (!node.tagName.equals("svg")) {
            for (int i = 0; i < node.children.size(); i++) {
                Block b = buildSubtree(document, root, node.children.get(i));
                if (b != null) {
                    root.getChildren().add(b);
                }
            }
        }
        return root;
    }

    public Block buildSubtree(WebDocument document, Node node) {
        return buildSubtree(document, null, node);
    }

    public Block buildElement(WebDocument document, Block parent, Node node) {
        if (document == null) document = this.document;
        final Block b = new Block(document);
        b.node = node;
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
        } else if (node.nodeType == COMMENT) {
            return null;
        }
        if (b.document != null && (node.tagName.equals("audio") || node.tagName.equals("video")) &&
                node.getAttribute("src") != null) {
            String src = node.getAttribute("src");
            if (!src.isEmpty()) {
                createMediaPlayer(b, src);
            }
        } else if (BlockElements.contains(node.tagName)) {
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
        } else if (node.tagName.equals("svg")) {
            Document svgDoc = createSVGDocument(document, node);
            JSVGCanvas svgCanvas = new JSVGCanvas(null, false, false) {
                @Override
                public void paintComponent(Graphics g) {
                    Block clip = b.parent;
                    while (clip.overflow != Block.Overflow.SCROLL) {
                        clip = clip.parent;
                    }
                    g.setClip(new Rectangle(clip._x_ - b._x_ + b.scroll_x, clip._y_ - b._y_ + b.scroll_y, clip.viewport_width, clip.viewport_height));
                    super.paintComponent(g);
                }
            };
            svgCanvas.setDocument(svgDoc);
            svgCanvas.setOpaque(true);
            b.add(svgCanvas);
            Dimension dim = new Dimension(Integer.parseInt(node.getAttribute("width")), Integer.parseInt(node.getAttribute("height")));
            svgCanvas.setBackground(new Color(0, 0, 0, 0));
            svgCanvas.setPreferredSize(dim);
            svgCanvas.setMaximumSize(dim);
            svgCanvas.setMinimumSize(dim);
            svgCanvas.repaint();
        }
        b.builder = this;
        b.parent = parent;
        
        if (node.nodeType == 1) {
            b.id = node.getAttribute("id");
            b.setTextColor(node.getAttribute("color"));
            b.setBackgroundColor(node.getAttribute("bgcolor"));

            applyDefaultStyles(node, b);
            applyParentFontStyles(b, parent);

            applyStyles(node, b);
            applyInlineStyles(node, b);
        }

        return b;
    }

    private void applyParentFontStyles(Block block, Block parent) {
        if (parent == null) return;
        block.color = parent.color;
        block.fontFamily = parent.fontFamily;
        block.fontSize = parent.fontSize;
        block.text_align = parent.text_align;
        block.text_bold = parent.text_bold;
        block.text_italic = parent.text_italic;
        block.text_underline = parent.text_underline;
        block.text_strikethrough = parent.text_strikethrough;
    }

    public Document createSVGDocument(WebDocument document, Node node) {
        // Get a DOMImplementation object
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

        // Create a new document
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);

        // Get the root element (the 'svg' element).
        Element svgRoot = doc.getDocumentElement();
        
        Set<String> keys = node.attributes.keySet();
        for (String key: keys) {
            if (!key.equals("width") && !key.equals("height") && !key.equals("viewbox")) {
                svgRoot.setAttributeNS(null, key, node.attributes.get(key));
            }
        }

        for (int i = 0; i < node.children.size(); i++) {
            if (node.children.get(i).nodeType == 1) { 
                svgRoot.appendChild(processSVGSubtree(doc, node.children.get(i)));
            }
        }

        double ratio = (double)java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 96;
        if (document != null && document.forced_dpi > 0) {
            ratio = document.forced_dpi;
        }
        if ((node.getAttribute("width") == null || node.getAttribute("height") == null) && node.getAttribute("viewbox") != null) {
            String[] s = node.getAttribute("viewbox").split("\\s");
            int vw = 1; int vh = 1;
            if (s.length  == 4) {
                vw = Integer.parseInt(s[2]);
                vh = Integer.parseInt(s[3]);
            }
            int width = vw;
            int height = vh;
            if (node.getAttribute("width") != null) {
                width = (int) Math.round(Integer.parseInt(node.getAttribute("width")) * ratio);
                height = (int) ((double) width / vw * vh);
                node.setAttribute("height", height + "");
            } else if (node.getAttribute("height") != null) {
                height = (int) Math.round(Integer.parseInt(node.getAttribute("height")) * ratio);
                width = (int) ((double) height / vh * vw);
                node.setAttribute("width", width + "");
            } else {
                width = (int) (vw * ratio);
                height = (int) (vh * ratio);
                node.setAttribute("width", width + "");
                node.setAttribute("height", height + "");
            }
            //System.err.println(svgRoot.getAttribute("viewbox"));
            //svgRoot.setAttributeNS(null, "preserveAspectRatio", "none");
            svgRoot.setAttributeNS(null, "viewBox", "0 0 " + width + " " + height);
            svgRoot.setAttributeNS(null, "width", width + "");
            svgRoot.setAttributeNS(null, "height", height + "");

        }

        return doc;
    }

    private Element processSVGSubtree(Document doc, Node node) {
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Element el = doc.createElementNS(svgNS, node.tagName);
        Set<String> keys = node.attributes.keySet();
        for (String key: keys) {
            el.setAttributeNS(null, key, node.attributes.get(key));
        }
        for (int i = 0; i < node.children.size(); i++) {
            if (node.children.get(i).nodeType == 1) {
                el.appendChild(processSVGSubtree(doc, node.children.get(i)));
            }
        }
        return el;
    }

    public void createMediaPlayer(Block b, String src) {
        int type = detectMediaType(src);

        b.setDisplayType(Block.Display.INLINE_BLOCK);

        MediaPlayer mp = null;

        if (type == MediaPlayer.VIDEO) {
            int player_width = 320 - b.borderWidth[3] - b.borderWidth[1];
            int player_height = 206 - b.borderWidth[0] - b.borderWidth[2];
            b.setWidth(player_width);
            b.auto_height = true;
            b.height = -1;
            mp = new MediaPlayer(b, player_width, player_height);
        } else {
            int player_width = 230; //b.parent.width > 0 ? b.parent.width - b.parent.borderWidth[1] - b.parent.borderWidth[3] - b.parent.paddings[1] - b.parent.paddings[3] : -1;
            b.width = (int) Math.round(player_width * b.ratio);
            b.auto_height = true;
            b.height = -1;
            mp = new MediaPlayer(b, player_width);
        }

        if (mp != null) {
            mp.open(src);
            b.setMediaPlayer(mp);
            b.isMedia = true;
        }
    }

    public int detectMediaType(String source) {
        if (source.matches(".*(avi|mp4|ts|mpg|m4v|ogv|mkv|flv|3gp)$")) {
            return MediaPlayer.VIDEO;
        }
        return MediaPlayer.AUDIO;
    }

    public void applyDefaultStyles(Node node, Block b) {
        if (node == null) return;
        if (node.tagName.equals("a")) {
            b.href = baseUrl + node.getAttribute("href");
            b.color = b.linkColor;
        }
        else if (node.tagName.equals("img") && !b.special) {
            b.auto_width = false;
            b.width = -1;
            b.isImage = true;
            b.setBackgroundImage(baseUrl + node.getAttribute("src"));
        }
        else if (node.tagName.equals("p")) {
            b.setMargins(0, 0, 12, 0);
        }
        else if (node.tagName.equals("body")) {
            b.setPaddings(8);
            b.setBackgroundColor(Color.WHITE);
        }
        else if (node.tagName.equals("font")) {
            if (node.getAttribute("size") != null) {
                b.setFontSize(Integer.parseInt(node.getAttribute("size")));
            }
        }
        else if (node.tagName.equals("li")) {
            b.list_item_type = 2;
        }
        if (b.display_type != Block.Display.INLINE) {
            if (node.getAttribute("width") != null) {
                b.setWidth(Integer.parseInt(node.getAttribute("width")));
            }
            if (node.getAttribute("height") != null) {
                b.setHeight(Integer.parseInt(node.getAttribute("height")));
            }
        }
    }

    public void applyStyles(Node node, Block b) {
        Set<String> keys = node.styles.keySet();
        for (String key: keys) {
            if (!key.trim().isEmpty()) {
                if (key.trim().equals("content")) continue;
                b.setProp(key.trim(), node.styles.get(key).trim());
                b.cssStyles.put(key.trim(), node.styles.get(key).trim());
                if (b.document != null && b.document.lastSetProperties != null) {
                    b.document.lastSetProperties.add(key.trim());
                }
            }
        }
        generatePseudoElements(node, b);
    }

    public void applyInlineStyles(Node node, Block b) {
        if (node.getAttribute("style") != null) {
            String[] styles = node.getAttribute("style").split("\\s*;\\s*");
            for (String style: styles) {
                String[] p = style.trim().split("\\s*:\\s*");
                if (!p[0].trim().isEmpty()) {
                    b.setProp(p[0].trim(), p[1].trim());
                    b.cssStyles.put(p[0].trim(), p[1].trim());
                    if (b.document != null && b.document.lastSetProperties != null) {
                        b.document.lastSetProperties.add(p[0].trim());
                    }
                }
            }
        }
    }

    public void resetStyles(Block b, boolean no_update) {
        if (b.node == null || b.node.stateStyles.size() == 0) return;

        int old_width = b.viewport_width;
        int old_height = b.viewport_height;
        
        b.document.ready = false;
        b.setTextColor(b.parent != null ? b.parent.color : b.default_color);
        if (!b.special) {
            b.setBackgroundColor(new Color(0, 0, 0, 0));
            b.setBackgroundImage(null);
            b.setBorderWidth(0);
            b.setBorderType(RoundedBorder.SOLID);
            b.setBorderColor(Color.BLACK);
        }
        b.setMargins(0);
        b.setPaddings(0);
        b.setBorderRadius(0);
        applyDefaultStyles(b.node, b);
        
        // This will be faster than full scan
        if (b.cssStyles.size() > 0) {
            Set<String> keys = b.cssStyles.keySet();
            for (String key: keys) {
                b.setProp(key, b.cssStyles.get(key));
                if (b.document != null && b.document.lastSetProperties != null) {
                    b.document.lastSetProperties.add(key);
                }
            }
        } else if (b.node != null) {
            applyStyles(b.node, b);
            applyInlineStyles(b.node, b);
        }
        b.document.ready = true;

        if (!no_update) {
            b.document.smartUpdate(b, old_width, old_height);
        }
    }

    public void applyStateStyles(Block b) {
        if (b.node == null) {
            return;
        }
        Vector<QuerySelector> stateStyles = b.node.getStateStyles();
        for (int i = 0; i < stateStyles.size(); i++) {
            if (!stateStyles.get(i).getElements().contains(b.node)) continue;
            boolean flag = true;
            String[] states = {"hover", "focus", "active", "visited"};
            start:
            for (String state: states) {
                for (Node node: stateStyles.get(i).getControlElements().get(state)) {
                    if (!node.states.contains(state)) {
                        flag = false;
                        break start;
                    }
                }
            }
            if (flag) {
                HashMap<String, String> styles = stateStyles.get(i).getRules();
                Set<String> keys = styles.keySet();
                for (String key: keys) {
                    b.setProp(key, styles.get(key));
                    if (b.document.lastSetProperties != null) {
                        b.document.lastSetProperties.add(key);
                    }
                }
            }
        }
    }

    public void applyStateStyles(Block b, boolean no_update) {
        if (b.node == null || b.node.stateStyles.size() == 0) return;

        int old_width = b.viewport_width;
        int old_height = b.viewport_height;

        b.document.ready = false;
        applyStateStyles(b);
        b.document.ready = true;

        if (!no_update) {
            b.document.smartUpdate(b, old_width, old_height);
        }
    }

    public void applyStateStylesRecursive(Block b, boolean no_rec) {
        if (b.type != Block.NodeTypes.ELEMENT) return;
        
        int old_width = b.viewport_width;
        int old_height = b.viewport_height;

        if (b == b.document.root || !no_rec) {
            b.document.lastSetProperties = new java.util.HashSet<String>();
        }

        applyStateStyles(b, true);
        for (int i = 0; i < b.getChildren().size(); i++) {
            applyStateStylesRecursive(b.getChildren().get(i), true);
        }

        if (b == b.document.root || !no_rec) {
            b.document.smartUpdate(b, old_width, old_height);
        }
    }

    public void applyStateStylesRecursive(Block b) {
        applyStateStylesRecursive(b, false);
    }

    public void resetStylesRecursive(Block b, boolean no_rec) {
        if (b.type != Block.NodeTypes.ELEMENT) return;
        
        int old_width = b.viewport_width;
        int old_height = b.viewport_height;

        if (b == b.document.root || !no_rec) {
            b.document.lastSetProperties = new java.util.HashSet<String>();
        }

        resetStyles(b, true);
        for (int i = 0; i < b.getChildren().size(); i++) {
            resetStylesRecursive(b.getChildren().get(i), true);
        }

        if (b == b.document.root || !no_rec) {
            b.document.smartUpdate(b, old_width, old_height);
        }
    }

    public void resetStylesRecursive(Block b) {
        resetStylesRecursive(b, false);
    }

    public void setDocument(Block block, WebDocument document) {
        block.document = document;
        for (int i = 0; i < block.getChildren().size(); i++) {
            setDocument(block.getChildren().get(i), document);
        }
    }

    public void generatePseudoElements(Node node, Block b) {
        if (document == null) return;
        String content;

        if (node.beforeStyles.size() > 0) {
            Node n = new Node(1);
            n.parent = node;
            content = "";
            for (QuerySelector sel: node.beforeStyles) {
                if (sel.getRules().get("content") != null) {
                    content = sel.getRules().get("content");
                    if (content.matches("\".*\"") || content.matches("\'.*\'")) {
                        content = content.substring(1, content.length()-1);
                    }
                }
                n.styles.putAll(sel.getRules());
            }
            n.tagName = "span";
            n.nodeValue = content;
            Block before = buildElement(document, b, n);
            before.addText(n.nodeValue);
            b.addElement(before, 0, true);
            b.getChildren().remove(0);
            b.setBeforePseudoElement(before);
        } else {
            b.setBeforePseudoElement(null);
        }

        if (node.afterStyles.size() > 0) {
            Node n = new Node(1);
            n.parent = node;
            content = "";
            for (QuerySelector sel: node.afterStyles) {
                if (sel.getRules().get("content") != null) {
                    content = sel.getRules().get("content");
                    if (content.matches("\".*\"") || content.matches("\'.*\'")) {
                        content = content.substring(1, content.length()-1);
                    }
                }
                n.styles.putAll(sel.getRules());
            }
            n.tagName = "span";
            n.nodeValue = content;
            Block after = buildElement(document, b, n);
            after.addText(n.nodeValue);
            b.addElement(after, true);
            b.getChildren().remove(b.getChildren().size()-1);
            b.setAfterPseudoElement(after);
        } else {
            b.setAfterPseudoElement(null);
        }
    }

    public void setDocument(WebDocument document) {
        this.document = document;
        if (baseUrl.length() > 0 && document.baseUrl.length() == 0) {
            document.setBaseUrl(baseUrl);
        }
    }

    public void setBaseUrl(String url) {
        baseUrl = url;
    }

    public String baseUrl = "";
    public WebDocument document;

    public final static Vector<String> BlockElements = new Vector<String>();
    public final static Vector<String> InlineElements = new Vector<String>();

    public final static int ELEMENT = 1;
    public final static int TEXT = 3;
    public final static int COMMENT = 8;

}
