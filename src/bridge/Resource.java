package bridge;

import htmlparser.Node;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
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

    public String getURL() {
        return url;
    }

    public Node getNode() {
        return node;
    }

    public void setLoaded(boolean value) {
        loaded = value;
    }

    public String url;
    public Node node;

    public int type = 0;

    public String content = "";
    public BufferedImage image;
    public Builder builder;
    public Document document;
    public File file;
    private volatile boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    public String getContent() {
        return content;
    }

    public BufferedImage getImage() {
        return image;
    }

    public File getFile() {
        return file;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setFile(File f) {
        this.file = f;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void unset() {
        content = "";
        image = null;
        loaded = false;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset ch) {
        charset = ch;
    }

    private Charset charset;

    public static class Type {
        public static final int STYLE = 0;
        public static final int SCRIPT = 1;
        public static final int IMAGE = 2;
        public static final int IFRAME = 3;
    }
}
