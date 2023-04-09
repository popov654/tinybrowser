package bridge;

import htmlparser.Node;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import jsparser.HTMLElement;
import network.Request;
import render.Block;
import render.WebDocument;
import tinybrowser.Reader;

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
        if (node.tagName.equals("img") && node.getAttribute("src") != null && document.asyncImageLoad) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.IMAGE);
        } else if (node.tagName.equals("iframe") && node.getAttribute("src") != null && document.asyncIframeLoad) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.IFRAME);
        } else if (node.tagName.equals("link") && node.getAttribute("rel") != null &&
              node.getAttribute("rel").equals("stylesheet") &&  node.getAttribute("href") != null && document.enableExternalStyles) {
            res = new Resource(node.getAttribute("href"), node, Resource.Type.STYLE);
        } else if (node.tagName.equals("script") && node.getAttribute("src") != null && document.enableScripts) {
            res = new Resource(node.getAttribute("src"), node, Resource.Type.SCRIPT);
        }
        if (res != null) addResource(res);
        for (int i = 0; i < node.children.size(); i++) {
            findResources(node.children.get(i));
        }
    }

    public void downloadResources() {
        Request.setCache(document.cache);
        for (Resource res: resources) {
            final Resource resource = res;
            if (res.type == Resource.Type.IMAGE) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        try {
//                            Thread.sleep(800);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        BufferedImage image = null;
                        String path = resource.getURL();
                        File f = null;
                        try {
                            if (document.cache != null) {
                                path = document.cache.get(path);
                            }
                            f = new File(path);
                            if (f != null) {
                                image = ImageIO.read(f);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (image != null) {
                            resource.setImage(image);
                            resource.setFile(f);
                            Block block = Mapper.get(resource.getNode());
                            if (block != null) {
                                int old_width = block.parts.size() == 0 ? block.width : block.parts.get(0).width;
                                int old_height = block.parts.size() == 0 ? block.height : block.parts.get(0).height;
                                block.auto_width = false;
                                block.width = -1;
                                block.height = -1;
                                for (Block part: block.parts) {
                                    part.auto_width = false;
                                    part.width = -1;
                                    part.height = -1;
                                }
                                block.document.ready = false;
                                block.setBackgroundImage(path);
                                Set<String> keys = block.cssStyles.keySet();
                                for (String key: keys) {
                                    if (key.startsWith("border")) {
                                        block.setProp(key, block.cssStyles.get(key));
                                        for (Block part: block.parts) {
                                            part.setProp(key, block.cssStyles.get(key));
                                        }
                                    }
                                }
                                block.document.ready = true;
                                Block b = block.doIncrementLayout(old_width, old_height, false);
                                b.forceRepaint();
                                document.document.repaint();
                            }
                        }
                        resource.setLoaded(true);
                        checkResourcesStatus();
                    }
                });
                thread.start();
            } else if (res.type == Resource.Type.IFRAME) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        try {
//                            Thread.sleep(1200);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        loadResourceContent(resource);
                        String content = resource.getContent();
                        Reader reader = new Reader();
                        Document childDocument = reader.createDocumentFromString(content);
                        resource.setDocument(childDocument);
                        Block block = Mapper.get(resource.getNode());
                        if (block != null) {
                            WebDocument childView = reader.createDocumentView(childDocument, "", (JFrame) childDocument.builder.windowFrame);
                            block.addChildDocument(childView);
                            Block b = block.doIncrementLayout();
                            b.forceRepaint();
                            document.document.repaint();

                            childDocument.parentDocument = document;
                            childDocument.hostElement = block.node;
                            HTMLElement frameElement = HTMLElement.create(block.node);
                            if (childDocument.builder.jsWindow == null) {
                                childDocument.builder.jsWindow = new jsparser.Window(new jsparser.Block());
                            }
                            frameElement.set("contentWindow", childDocument.builder.jsWindow);
                            frameElement.set("contentDocument", childDocument.builder.jsWindow.get("document"));
                        }
                        resource.setLoaded(true);
                        checkResourcesStatus();
                    }
                });
                thread.start();
            } else if (res.type == Resource.Type.STYLE) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadResourceContent(resource);
                        String content = resource.getContent();
                        if (content != null && !content.isEmpty()) {
                            document.builder.cssParser.setStyleForNode(resource.getNode(), content);
                            if (document.document != null) {
                                document.builder.reapplyDocumentStyles(document.document);
                            }
                        }
                        resource.setLoaded(true);
                        checkResourcesStatus();
                    }
                });
                thread.start();
            } else if (res.type == Resource.Type.SCRIPT) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        if (!resource.getNode().getAttribute("src").contains("async")) {
//                            try {
//                                Thread.sleep(1500);
//                            } catch (InterruptedException ex) {
//                                Logger.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
                        loadResourceContent(resource);
                        String content = resource.getContent();
                        if (content != null && !content.isEmpty()) {
                            for (ScriptElement script: document.builder.scripts) {
                                if (script.node == resource.getNode()) {
                                    script.content = content;
                                    script.loaded = true;
                                    if (document.document != null) {
                                        document.builder.compileScript(script);
                                    }
                                    break;
                                }
                            }
                            if (document.document != null) {
                                document.builder.runScripts();
                            }
                        }
                        resource.setLoaded(true);
                        checkResourcesStatus();
                    }
                });
                thread.start();
            }
        }
    }

    public void checkResourcesStatus() {
        boolean flag = true;
        for (Resource res: resources) {
            if (!res.isLoaded()) {
                flag = false;
            }
        }
        if (flag && document.document != null && !document.document.loadEventFired) {
            document.document.fireLoadEvent();
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

    public void loadResourceContent(Resource resource) {
        String content = null;
        String path = resource.getURL();
        if (path.startsWith("http")) {
            content = Request.makeRequest(path);
        } else {
            try {
                content = "";
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[1000];
                int offset = 0;
                File f = new File(path);
                FileReader fr = new FileReader(f);
                while (fr.ready() && offset < f.length()) {
                    int len = fr.read(buffer);
                    offset += len;
                    sb.append(buffer);
                }
                fr.close();
                content = sb.toString().trim();
            } catch (IOException ex) {
                Logger.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resource.setContent(content);
        //System.out.println(content);
    }

    public Resource getResourceForBlock(Block b) {
        for (Resource res: resources) {
            Block block = Mapper.get(res.getNode());
            if (block == b) return res;
        }
        return null;
    }

    public Resource getResourceForNode(Node node) {
        for (Resource res: resources) {
            if (res.getNode() == node) return res;
        }
        return null;
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
