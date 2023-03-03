package bridge;

import htmlparser.Node;
import java.awt.image.BufferedImage;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public class Resource {

    public Resource(String url, Node node, int type) {
        this.url = url;
        this.node = node;
        this.type = type;
    }

    public String url;
    public Node node;

    public int type = 0;

    public String content = "";
    public BufferedImage image;
    public Builder builder;
    public WebDocument document;
    private boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    public String getContent() {
        return content;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void unset() {
        content = "";
        image = null;
        loaded = false;
    }


    public static class Type {
        public static final int STYLE = 0;
        public static final int SCRIPT = 1;
        public static final int IMAGE = 2;
        public static final int IFRAME = 3;
    }
}
