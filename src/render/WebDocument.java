package render;

import htmlparser.TagLibrary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 *
 * @author Alex
 */
public class WebDocument extends JPanel {

    public WebDocument() {
        setLayout(null);
        panel.setLayout(null);
        add(panel);
        setOpaque(false);
        panel.setOpaque(false);
        root.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                Component[] c = panel.getComponents();
                for (int i = 0; i < c.length; i++) {
                    if (c[i] instanceof Block) {
                        ((Block)c[i]).clearSelection();
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }

        });

        layouter.setCurrentBlock(root);
        root.setBackgroundColor(new Color(255, 255, 255));
        root.overflow = Block.Overflow.SCROLL;
        panel.add(root);

        final WebDocument instance = this;

        panel.getInputMap().put(KeyStroke.getKeyStroke("F12"), "inspector");
        panel.getActionMap().put("inspector", new Action() {

            public void actionPerformed(ActionEvent e) {
                if (inspector != null) {
                    boolean is_opened = inspector.isVisible();
                    inspector.setVisible(!is_opened);
                    return;
                }
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(instance);
                inspector = openInspector(frame);
            }

            public Object getValue(String key) {
                return null;
            }

            public void putValue(String key, Object value) {}

            public void setEnabled(boolean b) {}

            public boolean isEnabled() {
                return true;
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}

            public void removePropertyChangeListener(PropertyChangeListener listener) {}

        });
        panel.getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), "copy");
        panel.getActionMap().put("copy", new Action() {

            public void actionPerformed(ActionEvent e) {
                StringSelection stringSelection = new StringSelection(selected_text);
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
            }

            public Object getValue(String key) {
                return null;
            }

            public void putValue(String key, Object value) {}

            public void setEnabled(boolean b) {}

            public boolean isEnabled() {
                return true;
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}

            public void removePropertyChangeListener(PropertyChangeListener listener) {}

        });
    }

    public void setWidth(int value) {
        setBounds(getX(), getY(), value, this.height);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resized();
            }
        });
    }

    public void setHeight(int value) {
        setBounds(getX(), getY(), this.width, value);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resized();
            }
        });
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
    }

    public void insertSubtree(Block b, Block insert) {
        insert.parent = b.parent;
        if (b.parent != null) {
            int index = b.parent.children.indexOf(b);
            b.parent.children.set(index, insert);
        }
        processSubtree(insert);
    }

    public void insertSubtreeWithoutRoot(Block b, Block insert) {
        ready = false;
        b.removeAllElements();
        b.setProp("font-family", b.fontFamily);
        for (int i = 0; i < insert.children.size(); i++) {
            b.addElement(insert.children.get(i), true);
            processSubtree(insert.children.get(i));
        }
        ready = true;
    }

    private void processSubtree(Block b) {
        b.document = null;
        if (b.width < 0) {
            b.setWidth(-1);
            if (b.parent == null) {
                b.width = width;
            }
        }
        b.document = this;
        if (b.parent != null) {
            int index = b.parent.children.indexOf(b);
            b.parent.children.remove(b);
            if (b.type == Block.NodeTypes.ELEMENT) {
                if (b.width == 0) b.width = b.parent.width;
                b.parent.addElement(b, index, true);
                if (debug) System.err.println("Added element, index: " + index);
                if (b.node != null && (b.node.tagName.equals("audio") || b.node.tagName.equals("video")) &&
                        b.node.getAttribute("src") != null && !b.node.getAttribute("src").isEmpty()) {
                    b.removeAllElements();
                    b.builder.createMediaPlayer(b, b.node.getAttribute("src"));
                }
            } else {
                b.parent.addText(b.textContent, index);
                b.parent.children.get(index).node = b.node;
                if (debug) System.err.println("Added text, content: " + b.textContent);
            }
        } else {
            //b.setBounds(0, 0, root.width, root.height);
            b.width = width;
            b.setBounds(borderSize, borderSize, width-borderSize*2, height-borderSize*2);
            panel.remove(root);
            root = b;
            b.pos = 0;
            layouter.setCurrentBlock(root);
            root.setBackgroundColor(new Color(255, 255, 255));
            root.overflow = Block.Overflow.SCROLL;
            panel.add(root);
        }
        if (b.builder != null) {
            b.builder.document = this;
            b.builder.generatePseudoElements(b.node, b);
        }
        b.setProp("font-family", b.fontFamily);
        for (int i = 0; i < b.children.size(); i++) {
            processSubtree(b.children.get(i));
        }
    }

    public void resized() {
        //panel.repaint();
        width = getWidth();
        height = getHeight();
        borderSize = this.getBorder().getBorderInsets(this).left;
        panel.setBounds(borderSize, borderSize, getWidth() - borderSize * 2, getHeight() - borderSize * 2);
        if (getWidth() != last_width || getHeight() != last_height) {
            //System.err.println("Size updated");
            if (keep_root_scrollbars_outside) {
                root.setBounds(0, 0, width - root.getScrollbarYSize() - borderSize * 2, height - root.getScrollbarXSize() - borderSize * 2);
                root.width = root.viewport_width = width - root.getScrollbarYSize() - borderSize * 2;
                root.height = root.viewport_height = height - root.getScrollbarXSize() - borderSize * 2;
            } else {
                root.setBounds(0, 0, width - borderSize * 2, height - borderSize * 2);
                root.width = width - borderSize * 2;
                root.height = height - borderSize * 2;
                root.viewport_width = !root.hasVerticalScrollbar() ? root.width : root.width - root.getScrollbarYSize();
                root.viewport_height = !root.hasHorizontalScrollbar() ? root.height : root.height - root.getScrollbarXSize();
            }
            root.orig_height = (int) (root.height / root.ratio);
            root.max_height = root.height;

            Component[] c = root.getComponents();
            for (int i = 0; i < c.length; i++) {
                c[i].setBounds(0, 0, root.width, root.height);
            }

            //System.err.println("Root width: " + root.width);
            //System.err.println("Viewport width: " + root.viewport_width);
            if (root.document.ready && !resizing) {
                resizing = true;
                root.flushBuffersRecursively();
                root.performLayout();
                root.forceRepaint();
                resizing = false;
                getParent().repaint();
            }
        }
        last_width = getWidth();
        last_height = getHeight();
    }

    public JFrame openInspector(JFrame parent) {
        if (root.node == null) return null;

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
        frame.setLocationRelativeTo(parent);
        frame.setLocation(parent.getX() - getWidth() - 86, parent.getY() - 80);
        frame.pack();
        frame.setVisible(true);

        ((JComponent)frame.getContentPane().getComponents()[0]).getInputMap().put(KeyStroke.getKeyStroke("F12"), "close");
        ((JComponent)frame.getContentPane().getComponents()[0]).getActionMap().put("close", new Action() {

            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }

            public Object getValue(String key) {
                return null;
            }

            public void putValue(String key, Object value) {}

            public void setEnabled(boolean b) {}

            public boolean isEnabled() {
                return true;
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}

            public void removePropertyChangeListener(PropertyChangeListener listener) {}

        });

        final WebDocument instance = this;

        //final boolean showAttributes = true;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TagLibrary.init();
                final ExtendedEntry rootEntry = new ExtendedEntry(root.node, instance);
                //rootEntry.setMinimumSize(new Dimension(width, 26));
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

        return frame;
        
    }

    public Color getDocumentBackground() {
        return panel.getBackground();
    }

    public void startWatcher() {
        if (w != null) return;
        w = new Watcher();
        Thread th = new Thread(w);
        th.setPriority(Thread.MIN_PRIORITY);
        th.start();
    }

    public void stopWatcher() {
        if (w != null) w.stop();
    }

    class Watcher implements Runnable {

        public void run() {
            while (!stop) {
                long time = System.currentTimeMillis();
                Component[] c = panel.getComponents();
                for (int i = 0; i < c.length; i++) {
                    if (c[i] instanceof Block) {
                        if (((Block)c[i]).has_animation && time > ((Block)c[i]).nextFrameDisplayTime()) {
                            ((Block)c[i]).displayNextFrame();
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {}
            }
        }

        public void stop() {
            stop = true;
        }

        public volatile boolean stop = false;

    }

    public Layouter getLayouter() {
        return layouter;
    }

    public Block getRoot() {
        return root;
    }

    public WebDocument getParentDocument() {
        return parent_document;
    }

    public void setParentDocument(WebDocument d) {
        parent_document = d;
    }

    public Block getParentDocumentBlock() {
        return parent_document_block;
    }

    public void setBorderSize(int size) {
        borderSize = size;
        setBounds(borderSize, borderSize, width - borderSize*2, height - borderSize*2);
    }

    public void setBaseUrl(String path) {
        baseUrl = path;
    }

    public void smartUpdate(Block b, int old_width, int old_height) {
        Set<String> onlyRepaint = new java.util.HashSet<String>();
        onlyRepaint.add("background");
        onlyRepaint.add("linear-gradient");
        onlyRepaint.add("background-color");
        onlyRepaint.add("border-color");
        onlyRepaint.add("color");
        onlyRepaint.add("cursor");
        Set<String> diff = new HashSet<String>(lastSetProperties);
        diff.removeAll(onlyRepaint);

        if (b.document.lastSetProperties == null || diff.size() > 0) {
            b.doIncrementLayout(old_width, old_height, false);
            b.setNeedRestoreSelection(true);
        }
        b.forceRepaint();
        b.document.repaint();
        b.setNeedRestoreSelection(false);

        b.document.lastSetProperties.clear();
        b.document.lastSetProperties = null;
    }

    public String baseUrl = "";

    public Set<String> lastSetProperties;

    protected Layouter layouter = new Layouter(this);

    public Block root = new Block(this, null, -1, -1);

    public volatile boolean inLayout = false;
    public volatile boolean isPainting = false;

    public Block active_block;

    public boolean ready = true;

    public volatile boolean resizing = false;
    public boolean scroll_intercepted = false;
    public boolean is_video_fullscreen = false;

    public boolean debug = false;

    protected WebDocument parent_document;
    protected Block parent_document_block;

    private Watcher w;

    protected int last_width = 0;
    protected int last_height = 0;

    public boolean prevent_mixed_content = true;
    public boolean preserve_layout_on_copy = true;
    public boolean use_native_inputs = false;
    public boolean keep_root_scrollbars_outside = false;
    public boolean smooth_borders = true;
    public double forced_dpi = 0;

    public String selected_text;
    public static boolean scale_borders = true;
    public int borderSize = 0;
    public Color borderColor;
    public final JPanel panel = new JPanel();
    public int width = 0;
    public int height = 0;

    private JFrame inspector;
}
