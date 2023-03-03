package tinybrowser;

import bridge.Builder;
import bridge.Document;
import bridge.Mapper;
import cssparser.CSSParser;
import htmlparser.HTMLParser;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
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
        if (debug) System.out.println(Main.getInstallPath() + file);

        HTMLParser hp = new HTMLParser(Main.getInstallPath() + file);

        if (debug) {
            System.out.println("----------------------------------");
            hp.printTree();
            System.out.println();
            System.out.println("----------------------------------");
        }

        Builder builder = new Builder();
        builder.setBaseUrl(Main.getInstallPath());
        builder.customElements = customElements;

        CSSParser parser = new CSSParser(hp);
        builder.cssParser = parser;
        parser.findStyles(hp.getRootNode());
        parser.applyStyles();

        builder.findScripts(hp.getRootNode());

        final Block root = builder.buildSubtree(null, hp.getRootNode().lastElementChild());
        if (debug) root.printTree();

        parser.applyGlobalRules(builder.baseUrl);

        Document document = new Document(builder, hp.getRootNode(), root);
        builder.documentWrap = document;

        return document;
    }

    public WebDocument createDocumentView(final Document documentWrap, final String title, JFrame frame) {
        WebDocument webDocument = createDocumentView(documentWrap.rootBlock, title, frame);
        documentWrap.frame = frame;
        documentWrap.title = title;
        documentWrap.document = webDocument;
        return webDocument;
    }

    public WebDocument createDocumentView(final Block root, final String title, JFrame frame) {
        final WebDocument document = new WebDocument();
        root.builder.setWindowFrame(frame);
        document.setBaseUrl(Main.getInstallPath());

        document.setPreferredSize(new Dimension(460, 240));
        document.width = 460;
        document.height = 240;

        document.root.width = document.width;
        document.root.height = document.height;
        document.root.auto_height = false;
        document.root.setBounds(document.borderSize, document.borderSize, document.width-document.borderSize*2, document.height-document.borderSize*2);

        document.insertSubtreeWithoutRoot(document.root, root);
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

        document.getParent().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                document.resized();
                document.root.builder.updateWindowObjects();
                if (!document.loadEventFired) {
                    root.builder.runScripts();
                    document.fireLoadEvent();
                }
            }
        });
        document.getParent().dispatchEvent(new java.awt.event.ComponentEvent(document.getParent(), java.awt.event.ComponentEvent.COMPONENT_RESIZED));

        root.builder.compileScripts();

        return document;
    }

    public void insertDocumentView(final Block root, Container c, boolean auto_height) {
        final WebDocument document = new WebDocument();
        java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(c);
        root.builder.setWindowFrame(frame);
        document.setBaseUrl(Main.getInstallPath());

        Dimension size = c.getPreferredSize();
        document.setPreferredSize(new Dimension(size.width, size.height));
        document.width = size.width;
        document.height = size.height;

        document.root.width = document.width;
        document.root.height = document.height;
        document.root.auto_height = false;
        document.root.setBounds(document.borderSize, document.borderSize, document.width-document.borderSize*2, document.height-document.borderSize*2);

        document.insertSubtreeWithoutRoot(document.root, root);
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
                if (!document.loadEventFired && document.getParent().getWidth() > 0) {
                    root.builder.runScripts();
                    document.fireLoadEvent();
                }
            }
        });
        document.getParent().dispatchEvent(new java.awt.event.ComponentEvent(document.getParent(), java.awt.event.ComponentEvent.COMPONENT_RESIZED));

        root.builder.compileScripts();
    }

    public void displayDocument(final Block root, final String title) {
        displayDocument(root, title, 640, 480);
    }

    public void displayDocument(final Block root, final String title, final int width, final int height) {
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
                insertDocumentView(root, panel, false);
                
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

}
