package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
        b.removeAllElements();
        for (int i = 0; i < insert.children.size(); i++) {
            b.addElement(insert.children.get(i));
            processSubtree(insert.children.get(i));
        }
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
            } else {
                b.parent.addText(b.textContent, index);
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
            root.setBounds(0, 0, width - borderSize * 2 - root.getScrollbarYSize(), height - borderSize * 2 - root.getScrollbarXSize());
            root.width = width - borderSize * 2;
            root.height = height - borderSize * 2;
            root.viewport_width = !root.hasVerticalScrollbar() ? root.width : root.width - root.getScrollbarYSize();
            root.viewport_height = !root.hasHorizontalScrollbar() ? root.height : root.height - root.getScrollbarXSize();
            root.orig_height = (int) (root.height / root.ratio);
            root.max_height = root.height;
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

    protected Layouter layouter = new Layouter(this);

    public Block root = new Block(this, null, -1, -1);

    public volatile boolean inLayout = false;
    public volatile boolean isPainting = false;

    public boolean ready = true;

    public volatile boolean resizing = false;

    public boolean debug = false;

    protected WebDocument parent_document;
    protected Block parent_document_block;

    private Watcher w;

    protected int last_width = 0;
    protected int last_height = 0;

    public boolean prevent_mixed_content = true;

    public String selected_text;
    public static boolean scale_borders = true;
    public int borderSize = 0;
    public Color borderColor;
    public final JPanel panel = new JPanel();
    public int width = 0;
    public int height = 0;
}
