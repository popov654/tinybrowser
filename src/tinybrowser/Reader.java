package tinybrowser;

import bridge.Builder;
import bridge.Mapper;
import cssparser.CSSParser;
import htmlparser.HTMLParser;
import java.awt.CardLayout;
import java.awt.Color;
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

    public Block readDocument(String file) {
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
        parser.findStyles(hp.getRootNode());
        parser.applyStyles();

        builder.findScripts(hp.getRootNode());

        final Block root = builder.buildSubtree(null, hp.getRootNode().lastElementChild());
        if (debug) root.printTree();

        parser.applyGlobalRules(builder.baseUrl);

        builder.runScripts(hp);

        return root;
    }

    public void displayDocument(final Block root, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final JPanel panel = new JPanel();
                panel.setLayout(new CardLayout());
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

                document.root.addMouseListeners();

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

                panel.add(document, "main");
                frame.setContentPane(panel);

                panel.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
                panel.setPreferredSize(new Dimension(document.width + 18, document.height + 18));

                frame.pack();
                frame.setLocationRelativeTo(null);

                frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentMoved(java.awt.event.ComponentEvent evt) {}

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent evt) {
                        document.resized();
                        if (!document.loadEventFired) {
                            document.fireLoadEvent();
                        }
                    }
                });

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
