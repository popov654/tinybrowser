package render;

import java.awt.Color;
import java.awt.Component;
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

/**
 *
 * @author Alex
 */
public class WebDocument extends JFrame {

    WebDocument(String title) {
        super(title);
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

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (last_width == 0) {
                    last_width = getWidth();
                }
                panel.setSize(panel.getWidth() + getWidth() - last_width, panel.getHeight());
                width = panel.getWidth();
                if (last_width != 0 && last_height != 0 && (getWidth() != last_width || getHeight() != last_height)) {
                    updateUI(last_width, last_height, getWidth(), getHeight());
                    root.setBounds(borderSize, borderSize, panel.getWidth()-borderSize*2, height-borderSize*2);
                    Component[] c = panel.getComponents();
                    if (c.length == 1) {
                        ((Block)c[0]).no_layout = true;
                        ((Block)c[0]).setHeight(panel.getHeight()-borderSize*2);
                        ((Block)c[0]).width = panel.getWidth()-borderSize*2;
                        ((Block)c[0]).viewport_width = ((Block)c[0]).width;
                        ((Block)c[0]).no_layout = false;
                        ((Block)c[0]).updateOnResize();
                    } else {
                        for (int i = 0; i < c.length; i++) {
                            if (c[i] instanceof Block) {
                                ((Block)c[i]).updateOnResize();
                            }
                        }
                    }
                }
                last_width = getWidth();
                last_height = getHeight();
            }
        });
    }

    public void updateUI(int last_width, int last_height, int width, int height) {}

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

    protected Layouter layouter = new Layouter(this);

    protected Block root = new Block(this, null, -1, -1);

    public boolean ready = true;

    private Watcher w;

    protected int last_width = 0;
    protected int last_height = 0;

    public String selected_text;
    public static boolean scale_borders = true;
    public int borderSize = 0;
    public Color borderColor;
    final JPanel panel = new JPanel();
    public int width = 0;
    public int height = 0;
}
