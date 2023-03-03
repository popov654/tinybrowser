package bridge;

import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class ResourceManager {

    public ResourceManager(Document document) {
        this.document = document;
        if (document.rootNode != null) {
            findResources(document.rootNode);
        }
    }

    public Vector<Resource> getResources() {
        return resources;
    }

    public Document getDocument() {
        return document;
    }

    private void findResources(Node node) {
        if (node.nodeType != 1 || node.isPseudo()) return;
        Resource res = null;
        if (node.tagName.equals("img") && node.getAttribute("src") != null) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.IMAGE);
        } else if (node.tagName.equals("iframe") && node.getAttribute("src") != null) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.IFRAME);
        } else if (node.tagName.equals("link") && node.getAttribute("rel") != null &&
              node.getAttribute("rel").equals("stylesheet") &&  node.getAttribute("href") != null) {
            res = new Resource(node.getAttribute("href"), node, Resource.Type.STYLE);
        } else if (node.tagName.equals("script") && node.getAttribute("src") != null) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.SCRIPT);
        }
        if (res != null) addResource(res);
        for (int i = 0; i < node.children.size(); i++) {
            findResources(node.children.get(i));
        }
    }

    public void addResource(Resource res) {
        resources.add(res);
        if (res.type == Resource.Type.STYLE) {
            styles.add(res);
        } else if (res.type == Resource.Type.SCRIPT) {
            scripts.add(res);
        } else if (res.type == Resource.Type.IMAGE) {
            images.add(res);
        } else if (res.type == Resource.Type.IFRAME) {
            iframes.add(res);
        }
    }

    public Vector<Resource> getStyles() {
        return styles;
    }

    public Vector<Resource> getScripts() {
        return scripts;
    }

    public Vector<Resource> getImages() {
        return images;
    }

    public Vector<Resource> getIFrames() {
        return iframes;
    }

    private Document document;
    private Vector<Resource> resources = new Vector<Resource>();
    private Vector<Resource> styles = new Vector<Resource>();
    private Vector<Resource> scripts = new Vector<Resource>();
    private Vector<Resource> images = new Vector<Resource>();
    private Vector<Resource> iframes = new Vector<Resource>();
}
