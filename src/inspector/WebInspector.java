package inspector;

import htmlparser.TagLibrary;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import render.Block;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public class WebInspector {

    public static void insertTreeInspector(JFrame frame, Container c, final WebDocument document) {
        final Block root = document.root;

        final JPanel contentpane = new JPanel();
        contentpane.setBackground(Color.WHITE);
        contentpane.setOpaque(true);
        
        //contentpane.setBounds(0, 0, 490, 380);
        final int width = c.getSize().width, height = c.getSize().height;

        final JScrollPane scrollpane = new JScrollPane(contentpane);
        scrollpane.getVerticalScrollBar().setUnitIncrement(20);
        scrollpane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollpane.getInsets();

        c.add(scrollpane);
        scrollpane.setBackground(Color.WHITE);
        scrollpane.setOpaque(true);
        scrollpane.setPreferredSize(new Dimension(width, height));

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TagLibrary.init();
                final Entry rootEntry = new Entry(root.node.parent != null ? root.node.parent : root.node, document);
                contentpane.add(rootEntry);

                final JScrollPane sp = scrollpane;

                int width = sp.getVerticalScrollBar().isVisible() ? sp.getWidth() - sp.getVerticalScrollBar().getPreferredSize().width - 12 : sp.getWidth() + sp.getVerticalScrollBar().getPreferredSize().width;
                rootEntry.inflate(width);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollpane.getHorizontalScrollBar().setValue(0);
                    }
                });

                contentpane.addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentMoved(java.awt.event.ComponentEvent evt) {}

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent evt) {
                        final int width = sp.getVerticalScrollBar().isVisible() ? sp.getWidth() - sp.getVerticalScrollBar().getPreferredSize().width - 12 : sp.getWidth() - 12;
                        rootEntry.setWidth(width);
                    }
                });
            }

        });
    }

}
