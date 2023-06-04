package tinybrowser;

import bridge.Builder;
import bridge.Document;
import bridge.Mapper;
import cache.DefaultCache;
import cssparser.CSSParser;
import htmlparser.HTMLParser;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import render.Block;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public class Reader {

    public Document readDocument(String file) {
        String path = Main.getInstallPath() + file;
        if (debug) System.out.println(path);
        HTMLParser hp = new HTMLParser(path);
        return createDocument(hp, path);
    }

    public Document createDocumentFromString(String content) {
        if (debug) System.out.println(content);
        HTMLParser hp = new HTMLParser();
        hp.data = content;
        hp.scan();
        return createDocument(hp, null);
    }

    public Document createDocument(HTMLParser hp, String path) {
        if (debug) {
            System.out.println("----------------------------------");
            hp.printTree();
            System.out.println();
            System.out.println("----------------------------------");
        }

        Builder builder = new Builder();
        if (path != null) {
            path = path.replaceAll("\\\\", "/");
            path = path.substring(0, path.lastIndexOf("/") + 1);
            if (!path.matches("(https?|ftp|file)://.*")) {
                path = path.replaceAll("/", "\\\\");
            }
        } else {
            path = Main.getInstallPath();
        }
        builder.setBaseUrl(path);
        builder.customElements = customElements;

        WebDocument renderDocument = new WebDocument();
        
        renderDocument.width = DEFAULT_WIDTH;
        renderDocument.height = DEFAULT_HEIGHT;

        renderDocument.root.width = renderDocument.width;
        renderDocument.root.height = renderDocument.height;
        renderDocument.root.auto_height = false;

        builder.setDocument(renderDocument);

        Document document = new Document(builder, hp.getRootNode(), null, new DefaultCache());
        document.document = renderDocument;
        builder.documentWrap = document;

        CSSParser parser = new CSSParser(hp);
        builder.cssParser = parser;
        parser.findStyles(hp.getRootNode());
        parser.applyStyles();

        renderDocument.root.node = hp.getRootNode().lastElementChild();
        renderDocument.root.node.document = hp;

        builder.findScripts(hp.getRootNode());
        document.getResourceManager().downloadResources();

        renderDocument.ready = false;

        final Block root = builder.buildSubtree(renderDocument, hp.getRootNode().lastElementChild());
        document.rootBlock = root;
        if (debug) root.printTree();

        parser.applyGlobalRules(builder.baseUrl);

        return document;
    }

    public WebDocument createDocumentView(final Document documentWrap, final String title, JFrame frame) {
        final WebDocument document = new WebDocument();

        documentWrap.frame = frame;
        documentWrap.title = title;
        documentWrap.document = document;

        Block root = documentWrap.rootBlock;

        root.builder.setWindowFrame(frame);
        //document.setBaseUrl(Main.getInstallPath());

        document.width = DEFAULT_WIDTH;
        document.height = DEFAULT_HEIGHT;
        document.setPreferredSize(new Dimension(document.width, document.height));

        document.root.width = document.width;
        document.root.height = document.height;
        document.root.auto_height = false;
        document.root.setBounds(document.borderSize, document.borderSize, document.width-document.borderSize*2, document.height-document.borderSize*2);

        document.insertSubtreeWithoutRoot(document.root, root);
        document.linkElements();
        document.root.setId("root");

        if (document.root.getChildren().size() > 0) {
            document.ready = false;
            document.root.setPaddings(8);
            document.root.node = document.root.getChildren().get(0).node.parent;
            document.root.builder = document.root.getChildren().get(0).builder;
            Mapper.add(document.root.node, document.root);
        } else {
            document.root.node = root.node;
            document.root.builder = root.builder;
            Mapper.add(document.root.node, document.root);
        }

        //document.debug = true;

        document.panel.setBackground(Color.WHITE);
        document.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        document.setBorderSize(1);

        root.auto_height = false;

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        document.ready = true;

        root.builder.reapplyDocumentStyles(document);

        root.builder.compileScripts();

        return document;
    }

    public void insertDocumentView(final Document documentWrap, Container c, boolean auto_height) {
        boolean existed = true;

        if (documentWrap.document == null) {
            documentWrap.document = new WebDocument();
            existed = false;
        }

        final WebDocument document = documentWrap.document;

        Block root = documentWrap.getRootBlock();
        java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(c);
        root.builder.setWindowFrame(frame);
        //document.setBaseUrl(Main.getInstallPath());

        Dimension size = c.getPreferredSize();
        document.setPreferredSize(new Dimension(size.width, size.height));
        document.width = size.width;
        document.height = size.height;

        document.root.width = document.width;
        document.root.height = document.height;
        document.root.auto_height = false;
        document.root.setBounds(document.borderSize, document.borderSize, document.width-document.borderSize*2, document.height-document.borderSize*2);

        document.insertSubtreeWithoutRoot(document.root, root, !existed);
        document.linkElements();
        document.root.setId("root");

        if (document.root.getChildren().size() > 0) {
            document.ready = false;
            document.root.setPaddings(8);
            document.root.node = document.root.getChildren().get(0).node.parent;
            document.root.builder = document.root.getChildren().get(0).builder;
            Mapper.add(document.root.node, document.root);
        } else {
            document.root.node = root.node;
            document.root.builder = root.builder;
            Mapper.add(document.root.node, document.root);
        }

        //document.debug = true;

        document.panel.setBackground(Color.WHITE);

        root.auto_height = auto_height;

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        document.ready = true;

        root.builder.reapplyDocumentStyles(document);

        c.setLayout(new CardLayout());
        c.add(document, "main");

        //c.setPreferredSize(new Dimension(document.width + 18, document.height + 18));

        document.getParent().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                document.resized();
                document.root.builder.updateWindowObjects();
                if (!document.readyEventFired && document.getParent().getWidth() > 0) {
                    document.root.builder.runScripts();
                    document.fireReadyEvent();
                    documentWrap.getResourceManager().checkResourcesStatus();
                }
            }
        });
        document.getParent().dispatchEvent(new java.awt.event.ComponentEvent(document.getParent(), java.awt.event.ComponentEvent.COMPONENT_RESIZED));

        root.builder.compileScripts();
    }

    public void displayDocument(final Document document, final String title) {
        displayDocument(document, title, 640, 480);
    }

    public void displayDocument(final Document document, final String title, final int width, final int height) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setTitle(title);

                final JPanel panel = new JPanel();
                panel.setLayout(new CardLayout());
                panel.setPreferredSize(new Dimension(width, height));
                panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10), BorderFactory.createLineBorder(Color.BLACK, 1)));

                frame.setContentPane(panel);
                insertDocumentView(document, panel, false);
                
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public void addCustomElement(String tag, Class className) {
        customElements.put(tag, className);
    }

    public HashMap<String, Class> customElements = new HashMap<String, Class>();

    public boolean debug = false;

    public final static int DEFAULT_WIDTH = 460;
    public final static int DEFAULT_HEIGHT = 380;

}
