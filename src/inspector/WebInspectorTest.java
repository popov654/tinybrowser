package inspector;

import htmlparser.Node;
import htmlparser.TagLibrary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Alex
 */
public class WebInspectorTest {

    private static Node prepareTree() {
        Node root = new Node(1);
        root.tagName = "body";
        root.attributes.put("onload", "init()");

        Node p = new Node(root, 1);
        p.tagName = "p";

        Node text1 = new Node(p, 3);
        text1.nodeValue = "This is a ";
        
        Node i = new Node(p, 1);
        i.tagName = "i";
        i.attributes.put("style", "background-color: rgb(223, 216, 45); color: #8848a7; cursor: pointer");

        Node text2 = new Node(i, 3);
        text2.nodeValue = "paragraph";

        Node text3 = new Node(p, 3);
        text3.nodeValue = ".";

        Node img = new Node(p, 1);
        img.tagName = "img";
        img.attributes.put("src", "smiley.gif");

        return root;
    }


    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        final Node root = prepareTree();
        if (root == null) return;

        final JFrame frame = new JFrame("Document Inspector");
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        frame.setContentPane(cp);
        cp.setLayout(new BorderLayout());

        final JPanel contentpane = new JPanel();
        contentpane.setBackground(Color.WHITE);
        contentpane.setOpaque(true);

        //contentpane.setBounds(0, 0, 490, 380);
        final int width = 490, height = 418;

        final JScrollPane scrollpane = new JScrollPane(contentpane);
        scrollpane.setOpaque(false);
        scrollpane.getInsets();

        cp.add(scrollpane);
        scrollpane.setBackground(Color.WHITE);
        scrollpane.setOpaque(true);
        scrollpane.setPreferredSize(new Dimension(width, height));
        //setSize(518, 420);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TagLibrary.init();
                final Entry rootEntry = new Entry(root);
                contentpane.add(rootEntry);

                final JScrollPane sp = scrollpane;

                int width = sp.getVerticalScrollBar().isVisible() ? sp.getWidth() - sp.getVerticalScrollBar().getPreferredSize().width - 12 : sp.getWidth() + sp.getVerticalScrollBar().getPreferredSize().width;
                rootEntry.inflate(width);

                contentpane.addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentMoved(java.awt.event.ComponentEvent evt) {}

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent evt) {
                        int width = sp.getVerticalScrollBar().isVisible() ? sp.getWidth() - sp.getVerticalScrollBar().getPreferredSize().width - 12 : sp.getWidth() - 12;
                        rootEntry.setWidth(width);
                    }
                });
            }

        });

    }
}
