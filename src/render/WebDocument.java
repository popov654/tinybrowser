package render;

import bridge.Mapper;
import htmlparser.Node;
import inspector.JSConsole;
import inspector.WebInspector;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;

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
        root = new Block(this, null, -1, -1);
        setRoot(root);

        final WebDocument instance = this;

        glass = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return highlighted_block != null ? root.getBounds().getSize() : new Dimension(0, 0);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            @Override
            public void paintComponent(Graphics g) {
                if (highlighted_block != null && instance.highlight_text) {
                    highlighted_block.highlightBlock(g);
                } else if (highlighted_block != null) {
                    highlighted_block.highlightMargins(g);
                }
                super.paintComponent(g);
            }
        };
        glass.setOpaque(false);
        glass.setBackground(new Color(0, 0, 0, 0));
        panel.add(glass);
        panel.setComponentZOrder(glass, 0);

        panel.getInputMap().put(KeyStroke.getKeyStroke("F12"), "inspector");
        panel.getActionMap().put("inspector", new Action() {

            public void actionPerformed(ActionEvent e) {
                if (inspector != null) {
                    boolean is_opened = inspector.isVisible();
                    inspector.setVisible(!is_opened);
                    if (is_opened && highlighted_block != null) {
                        highlighted_block = null;
                        root.forceRepaintAll();
                        repaint();
                    }
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

    public void setRoot(Block b) {
        b.document = this;
        b.parent = null;
        Component[] c = root.getComponents();
        for (int i = 0; i < c.length; i++) {
            root.remove(c[i]);
            b.add(c[i]);
        }
        root = b;

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

        root.addMouseListeners();

        ready = false;
        layouter.setCurrentBlock(root);
        root.setBackgroundColor(new Color(255, 255, 255));
        root.overflow = Block.Overflow.SCROLL;
        panel.add(root, 0);
        if (glass != null) {
            panel.setComponentZOrder(glass, 0);
        }
        ready = true;
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resized();
            }
        });
    }

    public void insertSubtree(Block b, Block insert) {
        insert.parent = b.parent;
        if (b.parent != null) {
            int index = b.parent.children.indexOf(b);
            b.parent.children.set(index, insert);
        }
        processSubtree(insert);
    }

    public void insertSubtreeWithoutRoot(Block b, final Block insert) {
        ready = false;
        b.removeAllElements();
        b.setProp("font-family", b.fontFamily);
        for (int i = 0; i < insert.children.size(); i++) {
            b.addElement(insert.children.get(i), true);
            processSubtree(insert.children.get(i));
        }
        b.builder = insert.builder;
        if (b == root) {
            root.node = insert.node;
        }
        ready = true;
    }

    public void processSubtree(Block b) {
        b.document = null;
        if (b.width < 0) {
            b.setWidth(-1);
            if (b.parent == null) {
                b.width = width;
            }
        }
        b.document = this;
        if (b.childDocument != null) {
            b.childDocument.parent_document = this;
        }
        if (b.parent != null) {
            int index = b.parent.children.indexOf(b);
            b.parent.children.remove(b);
            if (index < 0) index = b.parent.children.size();
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
                Mapper.add(b.node, b.parent.children.get(index));
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
            //b.builder.generatePseudoElements(b.node, b);
        }
        if (b.getComponents().length == 1 && b.getComponents()[0] instanceof JSVGCanvas) {
            b.getComponents()[0].addMouseListener(b);
            b.getComponents()[0].addMouseMotionListener(b);
        }
        b.setProp("font-family", b.fontFamily);
        for (int i = 0; i < b.children.size(); i++) {
            processSubtree(b.children.get(i));
        }
    }

    public void replaceElement(Block b, Block new_block) {
        b.replaceWith(new_block);
    }

    public void resized() {
        //panel.repaint();
        width = getWidth();
        height = getHeight();
        borderSize = this.getBorder() != null ? this.getBorder().getBorderInsets(this).left : 0;
        panel.setBounds(borderSize, borderSize, getWidth() - borderSize * 2, getHeight() - borderSize * 2);
        if (getWidth() != last_width || getHeight() != last_height) {
            //System.err.println("Size updated");
            processResize();
        }
        last_width = getWidth();
        last_height = getHeight();
    }

    public void processResize() {
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
        root.orig_width = (int) (root.width / root.ratio);
        root.orig_height = (int) (root.height / root.ratio);
        root.max_height = root.height;

        glass.setBounds(root.getBounds());

        Component[] c = root.getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].setBounds(0, 0, root.width, root.height);
        }

        //System.err.println("Root width: " + root.width);
        //System.err.println("Viewport width: " + root.viewport_width);
        if (root.document.ready && !resizing) {
            resizing = true;
            if (root.builder != null) {
                root.builder.reapplyDocumentStyles(this);
            } else {
                root.flushBuffersRecursively();
                root.performLayout();
                root.forceRepaint();
            }
            resizing = false;
            getParent().repaint();
        }
    }

    public JFrame openInspector(final JFrame parent) {
        if (root.node == null) return null;

        final JFrame frame = new JFrame("Document Inspector");
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        frame.setContentPane(cp);
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

        cp.setSize(new Dimension(500, 438));
        WebInspector.insertTreeInspector(frame, cp, this);

        JSConsole console = new JSConsole();
        console.insertConsole(frame, cp, this);

        frame.setLocationRelativeTo(parent);
        frame.setLocation(parent.getX() - getWidth() - 98, parent.getY() - 90);
        frame.pack();
        frame.setVisible(true);

        ((JComponent)frame.getContentPane()).getInputMap().put(KeyStroke.getKeyStroke("F12"), "close");
        ((JComponent)frame.getContentPane()).getActionMap().put("close", new Action() {

            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                if (highlighted_block != null) {
                    highlighted_block = null;
                    root.forceRepaintAll();
                    repaint();
                }
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
        frame.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                if (highlighted_block != null) {
                    highlighted_block = null;
                    root.forceRepaintAll();
                    repaint();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}

        });

        //final boolean showAttributes = true;

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

    public void fireEventForNode(Node node, String event) {
        HashSet<String> events = eventsFired.get(node);
        if (events == null) {
            events = new HashSet<String>();
            eventsFired.put(node, events);
        }
        events.add(event);
    }

    public synchronized void fireReadyEvent() {
        if (readyEventFired) return;
        readyEventFired = true;
        root.node.fireEvent("DOMContentLoaded", "render", null, null);
    }

    public synchronized void fireLoadEvent() {
        if (loadEventFired) return;
        loadEventFired = true;
        root.node.fireEvent("load", "render", null, null);
    }

    public boolean eventWasFired(Node node, String event) {
        HashSet<String> events = eventsFired.get(node);
        if (events == null) {
            return false;
        }
        return events.contains(event);
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

    public void setZoomPercentage(double value) {
        setZoom(value / 100);
    }

    public void setZoom(double value) {
        zoom = value;
        updateElementsDpiRecursive(root);
        root.performLayout();
        root.forceRepaintAll();
        //repaint();
    }

    private void updateElementDpi(Block block) {
        double old_ratio = block.ratio;

        block.ratio = (double)java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 96 * zoom;
        if (forced_dpi > 0) {
            block.ratio = forced_dpi * zoom;
        }

        block.fontSize = (int)Math.round(block.fontSize / old_ratio * block.ratio);
        block.scale_borders = WebDocument.scale_borders;

        block.width = block.orig_width > 0 ? (int)Math.round(block.orig_width * block.ratio) : -1;

        if (block.height >= 0) {
            block.height = (int)Math.round(height / old_ratio * block.ratio);
        }

        for (int i = 0; i < block.margins.length; i++) {
            block.margins[i] = (int) ((double) block.margins[i] / old_ratio * block.ratio);
        }
        for (int i = 0; i < block.paddings.length; i++) {
            block.paddings[i] = (int) ((double) block.paddings[i] / old_ratio * block.ratio);
        }
        
        for (int i = 0; i < block.arc.length; i++) {
            block.arc[i] = (int) ((double) block.arc[i] / old_ratio * block.ratio);
        }
        if (WebDocument.scale_borders) {
            for (int i = 0; i < block.borderWidth.length; i++) {
                block.borderWidth[i] = (int) ((double) block.borderWidth[i] / old_ratio * block.ratio);
            }
        }

        block.updateBorder();

        block.orig_width = block.width;
        block.orig_height = block.height;

        if (block.width < 0 && block.parent == null) {
            block.width = width - borderSize * 2;
        }
    }

    private void updateElementsDpiRecursive(Block block) {
        updateElementDpi(block);
        for (int i = 0; i < block.children.size(); i++) {
            Block child = block.children.get(i);
            updateElementsDpiRecursive(child);
            for (Block part: child.parts) {
                updateElementsDpiRecursive(part);
            }
        }
    }

    public void smartUpdate(Block b, int old_width, int old_height) {
        Set<String> onlyRepaint = new java.util.HashSet<String>();
        onlyRepaint.add("background");
        onlyRepaint.add("background-color");
        onlyRepaint.add("border-color");
        onlyRepaint.add("color");
        onlyRepaint.add("cursor");
        onlyRepaint.add("transition");

        if (lastSetProperties == null) {
            lastSetProperties = new HashSet<String>();
        }

        Set<String> diff = new HashSet<String>(lastSetProperties);
        diff.removeAll(onlyRepaint);

        Block block = b;
        if (b.document.lastSetProperties == null || diff.size() > 0) {
            if (b.parent != null) {
                block = b.doIncrementLayout(old_width, old_height, false);
            } else {
                b.performLayout();
            }
        }
        b.document.root.setNeedRestoreSelection(true);
        block.forceRepaint();
        repaint();
        b.document.root.setNeedRestoreSelection(false);

        b.document.lastSetProperties.clear();
        b.document.lastSetProperties = null;
    }



    public String baseUrl = "";

    public Set<String> lastSetProperties;

    protected Layouter layouter = new Layouter(this);

    public Block root;

    public volatile boolean inLayout = false;
    public volatile boolean isPainting = false;
    public volatile boolean no_layout = false;
    public volatile boolean fast_update = true;

    public Block active_block;
    public static WebDocument active_document;
    public HashSet<WebDocument> child_documents = new HashSet<WebDocument>();

    public boolean ready = true;

    public volatile boolean resizing = false;
    public boolean scroll_intercepted = false;
    public boolean is_video_fullscreen = false;
    public boolean ignore_transitions = false;
    public boolean no_immediate_apply = false;

    public boolean debug = false;

    protected WebDocument parent_document;
    protected Block parent_document_block;
    public Block hovered_block;
    public Block highlighted_block;

    public HashMap<Node, HashSet<String>> eventsFired = new HashMap<Node, HashSet<String>>();
    public volatile boolean readyEventFired = false;
    public volatile boolean loadEventFired = false;

    private Watcher w;

    protected int last_width = 0;
    protected int last_height = 0;

    public boolean prevent_mixed_content = true;
    public boolean preserve_layout_on_copy = true;
    public boolean use_native_inputs = false;
    public boolean keep_root_scrollbars_outside = false;
    public boolean return_size_for_inlines = true;
    public boolean highlight_text = false;
    public boolean smooth_borders = true;
    public double forced_dpi = 0;
    public double zoom = 1f;

    public String selected_text;
    public static boolean scale_borders = true;
    public int borderSize = 0;
    public Color borderColor;
    public int fontSize = 14;
    public final JPanel panel = new JPanel();
    public JPanel glass;
    public int width = 0;
    public int height = 0;

    private JFrame inspector;
}
