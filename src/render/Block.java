package render;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import network.FormEntry;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Alex
 */
public class Block extends JPanel implements Drawable, MouseListener, MouseMotionListener, MouseWheelListener {

    public Block(WebDocument document, Block parent, int width, int height, int borderWidth, int arc, Color borderColor) {

        this.document = document;
        this.parent = parent;
        setLayout(null);
        setOpaque(false);

        ratio = (double)java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 96 * (document != null ? document.zoom : 1);
        if (document != null && document.forced_dpi > 0) {
            ratio = document.forced_dpi * document.zoom;
        }

        if (document != null) fontSize = document.fontSize;
        fontSize = (int)Math.round(fontSize * ratio);
        scale_borders = WebDocument.scale_borders;

        this.width = width > 0 ? (int)Math.round(width * ratio) : -1;
        if (this.width >= 0) {
            this.orig_width = width;
        } else {
            this.auto_width = true;
        }
        if (height >= 0) {
            auto_height = false;
            this.height = (int)Math.round(height * ratio);
            this.orig_height = height;
        } else {
            this.height = -1;
            auto_height = true;
        }
        arc = WebDocument.scale_borders ? (int)Math.round(arc * 2.45 * ratio) : (int)Math.round(arc * 2.45);
        this.arc = new int[4];
        this.arc[0] = arc;
        this.arc[1] = arc;
        this.arc[2] = arc;
        this.arc[3] = arc;

        int bw = WebDocument.scale_borders ? (int)Math.floor(borderWidth * ratio) : borderWidth;
        for (int i = 0; i < 4; i++) {
            this.borderWidth[i] = bw;
        }
        for (int i = 0; i < 4; i++) {
            this.borderColor[i] = borderColor;
        }
        this.border = new RoundedBorder(this, this.borderWidth, this.arc, this.borderColor, borderType);
        platform = Float.parseFloat(System.getProperties().getProperty("os.version"));

        rules_for_recalc = new HashMap<String, String>();
        originalStyles = new LinkedHashMap<String, Object>();
        cssStyles = new LinkedHashMap<String, String>();

        orig_width = width;
        orig_height = height;

        if (width < 0 && document != null && parent == null) {
            width = document.width;
        }
        if (document != null && parent == null) {
            setBounds(0, 0, document.width, document.height);
        }

    }

    public Block(WebDocument document, Block parent, int width, int height) {
        this(document, parent, width, height, 0, 0, Color.BLACK);
    }

    public Block(WebDocument document) {
        this(document, null, 0, 0, 0, 0, Color.BLACK);
        auto_width = true;
        auto_height = true;
    }

    public Block(int width, int height) {
        this(null, null, width, height, 0, 0, Color.BLACK);
    }

    public Block() {
        this(null, null, 0, 0, 0, 0, Color.BLACK);
        auto_width = true;
        auto_height = true;
    }

    public void addMouseListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        if (this == document.root && getParent() != null && getParent().getParent() != null) {
            java.awt.Component c = this;
            while (c.getParent() != null) {
                c = c.getParent();
            }
            c.addMouseListener(parentListener);
            final Block instance = this;
            parentMotionListener = new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (parentListener.isDown) {
                        java.awt.Component c = instance;
                        int x = 0, y = 0;
                        while (c != null && c != e.getSource()) {
                            x += c.getX();
                            y += c.getY();
                            c = c.getParent();
                        }
                        MouseEvent evt = new MouseEvent((Block)instance, 0, 0, 0, e.getX() - x, e.getY() - y, 1, false, MouseEvent.BUTTON1);
                        instance.mouseDragged(evt);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {}

            };
            c.addMouseMotionListener(parentMotionListener);
        }
    }

    public void mouseWheel(MouseWheelEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        Block[] blocks = new Block[layer_list.size()];
        blocks = layer_list.toArray(blocks);

        int mouse_x = e.getX();
        int mouse_y = e.getY();

        if (mouse_x < _x_ || mouse_x > _x_ + viewport_width ||
            mouse_y < _y_ || mouse_y > _y_ + viewport_height) {
              return;
        }

        if (this == e.getSource()) {
            document.scroll_intercepted = false;
        }

        for (int i = blocks.length-1; i >= 0; i--) {
            Block b = blocks[i].original != null ? blocks[i].original : blocks[i];
            if (b.display_type == Display.INLINE || b.overflow != Overflow.SCROLL) continue;
            if (b.isMouseInside(mouse_x, mouse_y)) {
                b.mouseWheel(e);
                if (document.scroll_intercepted) {
                    document.scroll_intercepted = false;
                    return;
                }
            }
        }

        if (e.isShiftDown() && this.scrollbar_x != null && this.scrollbar_y != null ||
              this.scrollbar_x != null && this.scrollbar_y == null) {
            scroll_left += e.getWheelRotation() * scroll_delta;
            int parent_width = parent != null ? parent.viewport_width : document.width;
            if (scroll_left < 0) {
                scroll_left = 0;
            }
            if (scroll_left >= content_x_max - viewport_width) {
                scroll_left = content_x_max - viewport_width;
            }
            scroll_x = scroll_left;
            scrollbar_x.getModel().setValue(scroll_x);
            forceRepaint();
            document.scroll_intercepted = true;
        } else if (this.scrollbar_y != null) {
            scroll_top += e.getWheelRotation() * scroll_delta;
            int parent_height = parent != null ? parent.viewport_height : document.height;
            if (scroll_top < 0) {
                scroll_top = 0;
            }
            if (scroll_top >= content_y_max - viewport_height) {
                scroll_top = content_y_max - viewport_height;
            }
            scroll_y = scroll_top;
            scrollbar_y.getModel().setValue(scroll_y);
            forceRepaint();
            document.scroll_intercepted = true;
        }
        document.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        Block b0 = this;
        while (b0.parent != null) {
            b0 = b0.parent;
        }
        if (b0 != this) {
            b0.mouseWheelMoved(e);
            return;
        }

        mouseWheel(e);
    }

    public boolean isMouseInside(int x, int y) {
        boolean result = false;
        if (display_type != Display.INLINE) {
            result = (x >= _x_ && x < _x_ + viewport_width && y >= _y_ && y < _y_ + viewport_height);
        } else {
            if (x >= _x_ && x < _x_ + viewport_width && y >= _y_ && y < _y_ + viewport_height) {
                result = true;
            }
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i).isMouseInside(x, y)) return true;
            }
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).isMouseInside(x, y)) return true;
            }
        }
        return result;
    }

    private int scroll_delta = 20;
    

    class MyMouseListener implements MouseListener {

        @Override
        public void mousePressed(MouseEvent e) {
            isDown = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isDown = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {
            document.root.clearSelection();
            document.root.forceRepaint();
        }

        public boolean isDown = false;

    }

    MyMouseListener parentListener = new MyMouseListener();
    MouseMotionListener parentMotionListener;
    

    public int getOffsetLeft() {
        if (positioning == Position.ABSOLUTE || positioning == Position.FIXED) {
            return margins[3] + left;
        }
        return (parent == null) ? _x_ : _x_ - parent._x_;
    }

    public int getOffsetTop() {
        if (positioning == Position.ABSOLUTE || positioning == Position.FIXED) {
            return margins[0] + top;
        }
        return (parent == null) ? _y_ : _y_ - parent._y_;
    }

    public synchronized void forceRepaint() {
        if (document == null || no_draw || document.root.width < 0 || !document.ready || document.inLayout || this == document.root && document.isPainting) {
            return;
        }
        document.isPainting = true;
        buffer = null;
        invalidate();
        draw();
        document.isPainting = false;
    }

    public synchronized void forceRepaint(Graphics g) {
        document.isPainting = true;
        buffer = null;
        if (childDocument != null) {
            childDocument.root.flushBuffersRecursively();
        }
        draw();
        document.isPainting = false;
    }

    public synchronized void forceRepaintAll() {
        if (document.isPainting) {
            try {
                if (document.debug) System.out.println("Retrying repaint...");
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
            forceRepaintAll();
            return;
        }
        document.isPainting = true;
        if (document.debug) {
            System.out.println("Complete repaint started");
        }
        Block b = this;
        while (b.parent != null) {
            b = b.parent;
        }
        b.flushBuffersRecursively();
        b.draw();
        if (document.debug) {
            System.out.println("Complete repaint finished");
        }
        document.isPainting = false;
    }

    public synchronized void doPaint(Graphics g) {
        buffer = null;
        draw(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        //g.clearRect(0, 0, width, height);
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }

        if (positioning == Position.FIXED) {
            scroll_x = scroll_y = 0;
        }

        if (childDocument != null) {
            scroll_x = (parent != null ? parent.scroll_x : 0) + scroll_left;
            scroll_y = (parent != null ? parent.scroll_y : 0) + scroll_top;
            childDocument.setBounds(_x_ - scroll_x, _y_ - scroll_y, width, height);
            childDocument.repaint();
            return;
        }

        int dx = 0;
        int dy = 0;

        if (parent != null && positioning != Position.FIXED) {
            dx += parent.scroll_x;
            dy += parent.scroll_y;
        }

//        if (display_type == 2) dy += 1;
//        Block b = this;
//        while (b != null) {
//            if (b.overflow != Overflow.VISIBLE) {
//                dx = b._x_;
//                dy = b._y_;
//                break;
//            }
//            b = b.parent;
//        }

        if (transform) {
            int w = (int)(getTransformedSize()*1.1);
            dx = (w-width) / 2;
            dy = (w-height) / 2;
            if (height < 20) {
                dx -= 4;
                dy += 2;
            }
            //height = viewport_height = w;
        }
        if (has_shadow) {
            int x0 = -shadow_x + shadow_blur + shadow_size;
            int y0 = -shadow_y + shadow_blur + shadow_size;
            g.drawImage(buffer, _x_ - x0 - dx, _y_ - y0 - dy, this);
        } else {
            //g.setClip(null);
            g.drawImage(buffer, _x_ - dx, _y_ - dy, this);
        }
        
        /* if (text_layer != null) {
            text_layer.setBounds(-scroll_x, -scroll_y, text_layer.getWidth(), text_layer.getHeight());
        } */
        Component[] c = getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof JSVGCanvas) {
                Element root = ((JSVGCanvas)c[i]).getSVGDocument().getDocumentElement();
                //String[] s = root.getAttribute("viewBox").split("\\s");
                root.setAttributeNS(null, "width", width + "");
                root.setAttributeNS(null, "height", height + "");
                root.setAttributeNS(null, "viewBox", "0 0 " + width + " " + height);
                c[i].setBounds(_x_ - scroll_x, _y_ - scroll_y, width, height);
                c[i].repaint();
                continue;
            }
            if (c[i] == text_layer || (!(c[i] instanceof JTextField) && !(c[i] instanceof JTextArea) && !(c[i] instanceof JLabel) && !(c[i] instanceof JButton) && !(c[i] instanceof JRadioButton) && !(c[i] instanceof JCheckBox))) continue;
            if (inputType < Input.BUTTON) {
                c[i].setBounds(_x_ + borderWidth[3] + paddings[3] - scroll_x, _y_ + borderWidth[0] - scroll_y, width - borderWidth[3] - borderWidth[1] - paddings[3] - paddings[1], height - borderWidth[0] - borderWidth[2]);
            } else if (inputType == Input.BUTTON) {
                c[i].setBounds(_x_ + borderWidth[3] - scroll_x, _y_ + borderWidth[0] - scroll_y, width - borderWidth[3] - borderWidth[1], height - borderWidth[0] - borderWidth[2]);
            } else if (inputType == Input.RADIO || inputType == Input.CHECKBOX) {
                c[i].setBounds(_x_ + width / 2 - c[i].getPreferredSize().width / 2 - 1 - scroll_x, _y_ + height / 2 - c[i].getPreferredSize().height / 2 - scroll_y, height, height);
                c[i].repaint();
            } else if (c[i] instanceof JLabel && inputType == Input.FILE || c[i] instanceof JButton && parent.inputType == Input.FILE) {
                if (c[i] instanceof JLabel) {
                    c[i].setBounds(_x_ + borderWidth[3] + paddings[3] - scroll_x, _y_ - scroll_y, c[i].getWidth(), height);
                } else {
                    c[i].setBounds(_x_ + width - scroll_x - c[i].getWidth(), _y_ - scroll_y, c[i].getWidth(), height);
                }
                c[i].repaint();
            }
        }

        super.paintComponent(g);
    }

    public void clearBuffer() {

        if (buffer == null) return;

        Shape rect = new RoundedRect(0, 0, width, height, arc[0] / 1.9, arc[1] / 1.9, arc[2] / 1.9, arc[3] / 1.9);

        Graphics g = buffer.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1f);
        g2d.setComposite(composite);

        g2d.fill(rect);

        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(composite);

    }

    public void flushBuffersRecursively() {
        buffer = null;
        text_shadow_buffer = null;
        if (text_layer != null) {
            remove(text_layer);
            text_layer = null;
        }
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).text_layer != null) {
                parts.get(i).remove(parts.get(i).text_layer);
                parts.get(i).text_layer = null;
            }
        }
        if (children == null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).flushBuffersRecursively();
        }
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            for (int j = 0; j < line.elements.size(); j++) {
                if (line.elements.get(j) instanceof Block) {
                    ((Block)line.elements.get(j)).flushBuffersRecursively();
                }
            }
        }
    }

    protected void adjustCorners(double[] arcs, Block block) {

        if (block.scrollbar_y != null) {
            arcs[1] = 0.0;
        }
        if (block.scrollbar_x != null || block.scrollbar_y != null) {
            arcs[2] = 0.0;
        }
        if (block.scrollbar_x != null) {
            arcs[3] = 0.0;
        }

        if (block.borderWidth[3] > block.arc[0] / 2 || block.borderWidth[0] > block.arc[0] / 2) {
            arcs[0] = 0.0;
        }
        if (block.borderWidth[1] > block.arc[1] / 2 || block.borderWidth[0] > block.arc[1] / 2) {
            arcs[1] = 0.0;
        }
        if (block.borderWidth[1] > block.arc[2] / 2 || block.borderWidth[2] > block.arc[2] / 2) {
            arcs[2] = 0.0;
        }
        if (block.borderWidth[3] > block.arc[3] / 2 || block.borderWidth[2] > block.arc[3] / 2) {
            arcs[3] = 0.0;
        }
    }

    public void draw() {
        if (auto_width && width == 0 && type == NodeTypes.TEXT) {
            int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            width = getFontMetrics(new Font(fontFamily, style, fontSize)).stringWidth(textContent);
        }
        if (buffer == null && (width > 0 && height > 0 || node != null && node.states.contains("highlighted"))) {
            int w = Math.max(1, width);
            int h = Math.max(1, height);
            if (children.size() > 0 && children.lastElement().type == NodeTypes.TEXT && text_italic) w += 2;
            buffer = new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        if (buffer == null) {
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).draw();
            }
            return;
        }
        Graphics g = buffer.getGraphics();
        draw(g);
    }

    protected void draw(Graphics g) {
        if (type == NodeTypes.TEXT || display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        if (childDocument != null) {
            childDocument.getRoot().draw();
            return;
        }
        if (parts.size() > 0) {
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).draw(g);
            }
            return;
        }
        Graphics2D g2d = (Graphics2D)g;
        document.setBackground(document.getDocumentBackground());

        int bw = viewport_width > 0 ? viewport_width + (scrollbar_y != null ? scrollbar_y.getPreferredSize().width : 0) : this.width;
        int bh = viewport_height > 0 ? viewport_height + (scrollbar_x != null ? scrollbar_x.getPreferredSize().height : 0): this.height;

        if (parent == null && background != null && background.bgcolor != null) {
            g2d.setColor(background.bgcolor);
            g2d.fillRect(0, 0, bw, bh);
        }
        Background parentBackground = parent != null ? parent.background : null;
        if (parentBackground != null && parentBackground.bgcolor != null && parentBackground.gradient == null && parentBackground.bgImage == null) {
            g2d.setColor(parentBackground.bgcolor);
            //g2d.fillRect(0, 0, bw, bh);
        }

        int x0 = has_shadow ? -shadow_x + shadow_blur + shadow_size : 0;
        int y0 = has_shadow ? -shadow_y + shadow_blur + shadow_size : 0;

        if (parent != null) {
            //x0 -= parent.scroll_x;
            //y0 -= parent.scroll_y;
        }

        scroll_x = positioning != Position.FIXED ? (parent != null ? parent.scroll_x : 0) + scroll_left : 0;
        scroll_y = positioning != Position.FIXED ? (parent != null ? parent.scroll_y : 0) + scroll_top : 0;

        if (getComponents().length > 0 && getComponents()[0] instanceof MediaPlayer.VideoRenderer) {
            int sx = 0;
            int sy = 0;
            Block b = this;
            while (b.parent != null) {
                sx += b.scroll_left;
                sy += b.scroll_top;
                b = b.parent;
            }
            getComponents()[0].setBounds(_x_ - scroll_x, _y_ - scroll_y, width, height);
        }

        if (has_shadow && parent != null) {
            paintShadow(x0, y0);
        }

        if (parent != null) {
            g = buffer.getGraphics();
            g2d = (Graphics2D) g;

            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(composite);

            if (parent.transform) {
                int sx = _x_ - parent._x_ - parent.width / 2;
                int sy = _y_ - parent._y_ - parent.height / 2;
                ((Graphics2D)g).setTransform(AffineTransform.getRotateInstance(Math.PI / 4, -sx, -sy));
            }
        }

        if (transform) {
            int w = (int)(getTransformedSize()*1.1);
            buffer = new BufferedImage(w, w, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g = buffer.getGraphics();
            g2d = (Graphics2D) g;
            x0 = (w-width) / 2;
            y0 = (w-height) / 2;
            double rotation = Math.toRadians(45);
            double locationX = w / 2;
            double locationY = w / 2;
            if (height < 20) {
                locationX -= 5;
                locationY += 4;
            }
            AffineTransform tx = AffineTransform.getRotateInstance(rotation, locationX, locationY);
            AffineTransform t = g2d.getTransform();
            t.concatenate(tx);
            g2d.setTransform(t);
        }

        if (alpha < 1.0f) {
            int type = AlphaComposite.SRC_OVER;
            AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
            g2d.setComposite(composite);
        }
        if (sharp) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        @SuppressWarnings("unchecked")
        Map<String, String> desktopHints = (Map<String, String>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (fontSize >= 21) {
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
        }
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        if (desktopHints != null) {
            g2d.addRenderingHints(desktopHints);
        }
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //RoundRectangle2D rect = null;
        //rect = new RoundRectangle2D.Double(-borderWidth[3], -borderWidth[0], width+borderWidth[1]+2, height+borderWidth[2]+1, arc > 6 ? arc-5 : 0, arc > 6 ? arc-5 : 0);

        RoundedRect clip_rect = null;

        if (isPartlyHidden()) {

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int sx = clipping_block.parent != null ? clipping_block.parent.scroll_x : 0;
            int sy = clipping_block.parent != null ? clipping_block.parent.scroll_y : 0;

            int xc = clipping_block._x_ - _x_ + clipping_block.borderWidth[3] + parent.scroll_x;
            int yc = clipping_block._y_ - _y_ + clipping_block.borderWidth[0] + parent.scroll_y;
            int wc = clipping_block.viewport_width - clipping_block.borderWidth[3] - clipping_block.borderWidth[1];
            int hc = clipping_block.viewport_height - clipping_block.borderWidth[0] - clipping_block.borderWidth[2];


            double[] arcs = new double[4];
            arcs[0] = clipping_block.arc[0] / 2 - 1;
            arcs[1] = clipping_block.arc[1] / 2 - 1;
            arcs[2] = clipping_block.arc[2] / 2 - 1;
            arcs[3] = clipping_block.arc[3] / 2 - 1;

            adjustCorners(arcs, clipping_block);

            clip_rect = new RoundedRect(xc, yc, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);
            if (clipping_block.overflow != Overflow.VISIBLE) {
                g2d.setClip(clip_rect);
            } else {
                g2d.setClip(null);
            }

        }

        if (clipping_block == null && overflow != Overflow.VISIBLE) {
            int vw = viewport_width;
            if (children.size() > 0 && children.lastElement().type == NodeTypes.TEXT && text_italic) vw += 2;
            clip_rect = new RoundedRect(0, 0, vw, viewport_height, arc[0] / 3.24, arc[1] / 3.24, arc[2] / 3.24, arc[3] / 3.24);
            g2d.setClip(clip_rect);
        }

        if (bg_clip_x > -1 || bg_clip_y > 0) {
            int sx = 0;
            int sy = 0;
            int clip_x = bg_clip_x > -1 ? bg_clip_x : viewport_width;
            int clip_y = bg_clip_y > -1 ? bg_clip_y : viewport_height;
            if (clipping_block != null) {
                if (clipping_block.scroll_left > _x_ - clipping_block._x_ - clipping_block.borderWidth[3]) {
                    sx = -(_x_ - clipping_block._x_ - clipping_block.borderWidth[3] - clipping_block.scroll_left);
                }
                if (clipping_block.scroll_top > _y_ - clipping_block._y_ - parent.borderWidth[0]) {
                    sy = -(_y_ - clipping_block._y_ - clipping_block.borderWidth[0] - clipping_block.scroll_top);
                }
                if (_x_ + sx + clip_x > clipping_block.viewport_width - clipping_block.borderWidth[1]) {
                    clip_x = clipping_block.viewport_width - clipping_block.borderWidth[1] - (_x_ + sx);
                }
                if (_y_ + sy + clip_y > clipping_block.viewport_height - clipping_block.borderWidth[2]) {
                    clip_y = clipping_block.viewport_height - clipping_block.borderWidth[2] - (_y_ + sy);
                }
            }
            clip_rect = new RoundedRect(sx, sy, clip_x, clip_y, 0, 0, 0, 0);
            g2d.setClip(clip_rect);
        }

        int width = viewport_width > 0 ? viewport_width : this.width;
        int height = viewport_height > 0 ? viewport_height : this.height;

        if (children.size() > 0 && children.lastElement().type == NodeTypes.TEXT && text_italic) width += 2;

        if (background != null) {

            if (target_background != null && backgroundState > 0) {
                int type = AlphaComposite.SRC_OVER;
                AlphaComposite composite = AlphaComposite.getInstance(type, alpha * (1 - (float) backgroundState));
                g2d.setComposite(composite);
            }
            
            paintBackground(background, g2d, x0, y0);

            if (target_background != null && backgroundState > 0) {
                int type = AlphaComposite.SRC_OVER;
                AlphaComposite composite = AlphaComposite.getInstance(type, alpha * (float) backgroundState);
                g2d.setComposite(composite);

                paintBackground(target_background, g2d, x0, y0);

                composite = AlphaComposite.getInstance(type, alpha);
                g2d.setComposite(composite);
            }
        }

        if (clipping_block == this) g.setClip(null);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (border != null) {
            if (clipping_block == null && overflow != Overflow.VISIBLE) {
                g.setClip(null);
            }
            border.paintBorder(this, g, x0, y0, bw, bh);
        }
        g.setClip(null);
        clipping_block = null;

        renderContent(g);

        boolean is_highlighted = node != null && node.states.contains("highlighted") ||
                children.size() == 1 && children.get(0).node != null && children.get(0).node.states.contains("highlighted");
        if (is_highlighted && document != null) {
            document.highlighted_block = this;
            if (!document.highlight_text) {
                highlightBlock(g);
            }
        }

    }

    public void paintBackground(Background background, Graphics2D g2d, int x0, int y0) {
        if (background.gradient != null) {
            paintGradient(background.gradient, g2d, x0, y0);
        }
        else if (background.bgcolor != null && !(inputType >= Input.RADIO && inputType <= Input.CHECKBOX)) {
            g2d.setColor(background.bgcolor);
            if (arc[0] > 0 || arc[1] > 0 || arc[2] > 0 || arc[3] > 0) {
                double[] arcs = new double[4];
                for (int i = 0; i < 4; i++) {
                    arcs[i] = arc[i] / 2.5;
                }
                adjustCorners(arcs, this);
                RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
                g2d.fill(rect);
            } else {
                g2d.fillRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2]);
            }
        }
        //if (display_type == 2) System.err.println(width + "x" + height);
        else if (background.bgImage != null) {
            paintBackgroundImage(g2d, x0, y0);
        }
    }

    public void paintGradient(Gradient gradient, Graphics2D g2d, int x0, int y0) {
        if (gradient.type == Gradient.LINEAR) {
            Point2D[] p = Gradient.getPoints(gradient.getAngle(), 0, 0, width, height);
            Point2D start = p[0];
            Point2D end = p[1];
            start.setLocation(start.getX() + x0, start.getY() + y0);
            end.setLocation(end.getX() + x0, end.getY() + y0);

            Color[] colors = gradient.getColors();
            float[] dist = gradient.getPositions(gradient.getAngle(), start, end);

            for (int i = 1; i < dist.length; i++) {
                if (dist[i] == dist[i-1]) {
                    if (dist[i] < 1) dist[i] += 0.000001;
                    else dist[i-1] -= 0.000001;
                }
            }

            LinearGradientPaint gp = new LinearGradientPaint(start, end, dist, colors);
            g2d.setPaint(gp);
            if (arc[0] > 0 || arc[1] > 0 || arc[2] > 0 || arc[3] > 0) {
                double[] arcs = new double[4];
                for (int i = 0; i < 4; i++) {
                    arcs[i] = arc[i] / 2.5;
                }
                adjustCorners(arcs, this);
                RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
                g2d.fill(rect);
            } else {
                g2d.fillRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2]);
            }
        } else if (gradient.type == Gradient.RADIAL) {
            Point2D center = new Point2D.Double(_x_ + gradient.cx, _y_ + gradient.cy);
            double dx = gradient.dx;
            double dy = gradient.dy;

            Color[] colors = gradient.getColors();
            float[] dist = gradient.getPositions();

            for (int i = 1; i < dist.length; i++) {
                if (dist[i] == dist[i-1]) {
                    if (dist[i] < 1) dist[i] += 0.000001;
                    else dist[i-1] -= 0.000001;
                }
            }

            if (dx <= 0 || dy <= 0) {

                double[] arcs = new double[4];
                for (int i = 0; i < 4; i++) {
                    arcs[i] = arc[i] / 2.5;
                }
                adjustCorners(arcs, this);
                RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
                g2d.setClip(rect);
                g2d.setColor(colors[colors.length-1]);
                g2d.fillRect(x0, y0, width, height);
                g2d.setClip(null);

            } else if (dx / dy < 3) {

                RadialGradientPaint gp = new RadialGradientPaint(center, (float) dx, dist, colors);
                g2d.setPaint(gp);
                AffineTransform t0 = g2d.getTransform();
                if (dx != dy) {
                    AffineTransform t = (AffineTransform) g2d.getTransform().clone();
                    t.concatenate(AffineTransform.getScaleInstance(dx / dy * 1.005, 1));
                    t.concatenate(AffineTransform.getTranslateInstance(-1.6, 0));
                    g2d.setTransform(t);
                }
                if (arc[0] > 0 || arc[1] > 0 || arc[2] > 0 || arc[3] > 0) {
                    double[] arcs = new double[4];
                    for (int i = 0; i < 4; i++) {
                        arcs[i] = arc[i] / 2.5;
                    }
                    adjustCorners(arcs, this);
                    RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
                    if (dx != dy) {
                        g2d.setClip(new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], (double) (width - borderWidth[1] - borderWidth[3]) * dy / dx, height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]));
                    }
                    g2d.fill(rect);
                } else {
                    g2d.fillRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2]);
                }
                if (dx != dy) {
                    g2d.setClip(null);
                    g2d.setTransform(t0);
                }

            } else {

                double[] arcs = new double[4];
                for (int i = 0; i < 4; i++) {
                    arcs[i] = arc[i] / 2.5;
                }
                adjustCorners(arcs, this);
                RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
                g2d.setClip(rect);

                if (dist[0] > 0) {
                    Color[] colors2 = new Color[colors.length+1];
                    float[] dist2 = new float[dist.length+1];
                    colors2[0] = colors[0];
                    dist2[0] = 0f;
                    for (int i = 1; i < colors.length; i++) {
                        colors2[i] = colors[i-1];
                        dist2[i] = dist[i-1];
                    }
                    colors = colors2;
                    dist = dist2;
                }

                if (dist[dist.length-1] < 1f) {
                    Color[] colors2 = new Color[colors.length+1];
                    float[] dist2 = new float[dist.length+1];
                    for (int i = 0; i < colors.length; i++) {
                        colors2[i] = colors[i];
                        dist2[i] = dist[i];
                    }
                    colors2[colors.length] = colors[colors.length-1];
                    dist2[dist.length] = 1f;
                    colors = colors2;
                    dist = dist2;
                }

                g2d.setColor(colors[colors.length-1]);
                g2d.fillRect(x0, y0, width, height);

                if (dx <= 0 || dy <= 0) return;

                for (int i = 0; i < colors.length-1; i++) {
                    Color col1 = colors[i];
                    Color col2 = colors[i+1];

                    double r11 = dx * dist[i], r12 = dy * dist[i];
                    double r21 = dx * dist[i+1], r22 = dy * dist[i+1];

                    double delta_red = (double) (col2.getRed() - col1.getRed()) / (r21 - r11);
                    double delta_green = (double) (col2.getGreen() - col1.getGreen()) / (r21 - r11);
                    double delta_blue = (double) (col2.getBlue() - col1.getBlue()) / (r21 - r11);
                    double delta_alpha = (double) (col2.getAlpha() - col1.getAlpha()) / (r21 - r11);

                    for (double r = r11; r < r21; r += 1) {
                        double h = r21 > 0 ? r * r22 / r21 : 0;
                        //System.out.println((int)(col1.getAlpha() + (r - r11) * delta_alpha));
                        Color col = new Color((int)(col1.getRed() + (r - r11) * delta_red),
                                              (int)(col1.getGreen() + (r - r11) * delta_green),
                                              (int)(col1.getBlue() + (r - r11) * delta_blue),
                                              (int)(col1.getAlpha() + (r - r11) * delta_alpha));
                        g2d.setColor(col);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawOval((int)(gradient.cx - r), (int)(gradient.cy - h), (int) (r * 2), (int) (h * 2));
                    }
                }
                g2d.setClip(null);
            }
        }
    }

    public void paintBackgroundImage(Graphics2D g2d, int x0, int y0) {
        if (background == null || background.bgImage == null) return;
        if (arc[0] > 0 || arc[1] > 0 || arc[2] > 0 || arc[3] > 0) {
            double[] arcs = new double[4];
            for (int i = 0; i < 4; i++) {
                arcs[i] = arc[i] / 2.5;
            }
            adjustCorners(arcs, this);
            RoundedRect rect = new RoundedRect(x0 + borderWidth[3], y0 + borderWidth[0], width - borderWidth[1] - borderWidth[3], height - borderWidth[0] - borderWidth[2], arcs[0], arcs[1], arcs[2], arcs[3]);
            Shape clip = g2d.getClip();
            if (clip != null) {
                Area clip_area = new Area(clip);
                clip_area.intersect(new Area(rect));
                g2d.setClip(clip_area);
            } else {
                g2d.setClip(rect);
            }
        }
        if (background != null && background.bg_alpha < 1.0f) {
            int type = AlphaComposite.SRC_OVER;
            AlphaComposite composite = AlphaComposite.getInstance(type, alpha * background.bg_alpha);
            g2d.setComposite(composite);
        }
        if (background.background_repeat == BackgroundRepeat.NONE) {
            int x = x0 + borderWidth[3] + background.background_pos_x;
            int y = y0 + borderWidth[0] + background.background_pos_y;
            int iw = background.background_size_x < 0 ? background.bgImage.getWidth() : background.background_size_x;
            int ih = background.background_size_y < 0 ? background.bgImage.getHeight() : background.background_size_y;
            //Image scaledImage = bgImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(background.bgImage, x, y, x+iw, y+ih, 0, 0, background.bgImage.getWidth(), background.bgImage.getHeight(), this);
        } else {
            int x = x0 + borderWidth[3] + background.background_pos_x;
            int y = y0 + borderWidth[0] + background.background_pos_y;

            int iw = background.background_size_x < 1 ? background.bgImage.getWidth() : background.background_size_x;
            int ih = background.background_size_y < 1 ? background.bgImage.getHeight() : background.background_size_y;

            int w = x + iw;
            int h = y + ih;

            if ((background.background_repeat & BackgroundRepeat.REPEAT_X) > 0) {
                while (x > x0 + borderWidth[3]) x -= iw;
                w = width - borderWidth[1];
            }
            if ((background.background_repeat & BackgroundRepeat.REPEAT_Y) > 0) {
                while (y > y0 + borderWidth[0]) y -= ih;
                h = height - borderWidth[2];
            }

            while (y < h) {
                while (x < w) {
                    int x1 = Math.max(x0 + borderWidth[3], x);
                    int y1 = Math.max(y0 + borderWidth[0], y);
                    int x2 = Math.min(w, x+iw);
                    int y2 = Math.min(h, y+ih);
                    int x_from = x < borderWidth[3] ? x0+borderWidth[3]-x : x0;
                    int y_from = y < borderWidth[0] ? y0+borderWidth[0]-y : y0;
                    g2d.drawImage(background.bgImage, x1, y1, x2, y2, x_from, y_from, x_from+x2-x1, y_from+y2-y1, this);
                    x += iw;
                }
                y += ih;
                x = x0 + borderWidth[3] + background.background_pos_x;
            }
        }
    }

    public void highlightBlock(Graphics g) {
        int x0 = document.highlight_text ? _x_ - scroll_x : 0;
        int y0 = document.highlight_text ? _y_ - scroll_y : 0;

        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (parts.size() == 1) {
            x0 = parts.get(0)._x_ - scroll_x;
            y0 = parts.get(0)._y_ - scroll_y;
            w = Math.max(1, parts.get(0).width);
            h = Math.max(1, parts.get(0).height);
        }
        g.setColor(new Color(180, 230, 255, 90));
        if (w > 1 && h > 1) g.fillRect(x0, y0, w, h);
        else {
            g.setColor(new Color(160, 210, 255, 210));
            buffer.setRGB(x0, y0, g.getColor().getRGB());
        }

        highlightMargins(g);
    }

    public void highlightMargins(Graphics g) {
        g.setColor(new Color(245, 158, 0, 58));
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (margins[0] > 0) {
            g.fillRect(_x_, _y_ - margins[0], w, margins[0]);
        }
        if (margins[1] > 0) {
            g.fillRect(_x_ + width, _y_, margins[1], h);
        }
        if (margins[2] > 0) {
            g.fillRect(_x_, _y_ + height, w, margins[2]);
        }
        if (margins[3] > 0) {
            g.fillRect(_x_ - margins[3], _y_, margins[3], h);
        }
    }

    private void paintShadow(int x0, int y0) {
        buffer = new BufferedImage(Math.abs(shadow_x) + width + (shadow_blur + shadow_size)*2, Math.abs(shadow_y) + height + (shadow_blur + shadow_size)*2, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(shadow_color);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (alpha < 1.0f) {
            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(composite);
        }
        int sx = x0 + shadow_x - shadow_size;
        int sy = y0 + shadow_y - shadow_size;
        if (arc[0] > 0 || arc[1] > 0 || arc[2] > 0 || arc[3] > 0) {
            RoundedRect rect = new RoundedRect(sx, sy, width + shadow_size * 2, height + shadow_size * 2, Math.max(0, arc[0]-4), Math.max(0, arc[1]- 4), Math.max(0, arc[2]-4), Math.max(0, arc[3]-4));
            g2d.fill(rect);
        } else {
            g2d.fillRect(sx, sy, width + shadow_size * 2, height + shadow_size * 2);
        }

        if (shadow_blur > 0) {
            int r1, r2;
            float delta = 1f / shadow_blur;
            r1 = (int)Math.round(arc[0] / 2.5);
            r2 = (int)Math.round(arc[3] / 2.5);
            for (int y = sy + r1; y < sy + height + shadow_size * 2  - r2; y++) {
                int limit = sx - shadow_blur;
                for (int x = limit; x < limit + shadow_blur; x++) {
                    float a = 0;
                    for (int d = -shadow_blur; d <= shadow_blur; d++) {
                        a += x + d > limit ? (shadow_blur + 1 - (limit + shadow_blur - x)) * delta : 0;
                    }
                    a /= shadow_blur * 2;
                    g2d.setColor(new Color(shadow_color.getRed(), shadow_color.getGreen(), shadow_color.getBlue(), (int)Math.round(shadow_color.getAlpha() * a)));
                    g2d.fillRect(x, y, 1, 1);
                }
            }
            r1 = (int)Math.round(arc[1] / 2.5);
            r2 = (int)Math.round(arc[2] / 2.5);
            for (int y = sy + r1; y < sy + height + shadow_size * 2  - r2; y++) {
                int limit = sx + width + shadow_size * 2 + shadow_blur - 1;
                for (int x = limit - shadow_blur + 1; x <= limit; x++) {
                    float a = 0;
                    for (int d = -shadow_blur; d <= shadow_blur; d++) {
                        a += x + d < limit-1 ? (shadow_blur + 1 - (x - (limit - shadow_blur))) * delta : 0;
                    }
                    a /= shadow_blur * 2;
                    g2d.setColor(new Color(shadow_color.getRed(), shadow_color.getGreen(), shadow_color.getBlue(), (int)Math.round(shadow_color.getAlpha() * a)));
                    g2d.fillRect(x, y, 1, 1);
                }
            }
            r1 = (int)Math.round(arc[0] / 2.5);
            r2 = (int)Math.round(arc[1] / 2.5);
            for (int x = sx + r1; x < sx + width + shadow_size * 2  - r2; x++) {
                int limit = sy - shadow_blur;
                for (int y = limit; y < limit + shadow_blur; y++) {
                    float a = 0;
                    for (int d = -shadow_blur; d <= shadow_blur; d++) {
                        a += y + d > limit ? (shadow_blur + 1 - (limit + shadow_blur - y)) * delta : 0;
                    }
                    a /= shadow_blur * 2;
                    g2d.setColor(new Color(shadow_color.getRed(), shadow_color.getGreen(), shadow_color.getBlue(), (int)Math.round(shadow_color.getAlpha() * a)));
                    g2d.fillRect(x, y, 1, 1);
                }
            }
            r1 = (int)Math.round(arc[4] / 2.5);
            r2 = (int)Math.round(arc[3] / 2.5);
            for (int x = sx + r1; x < sx + width + shadow_size * 2 - r2; x++) {
                int limit = sy + height + shadow_size * 2 + shadow_blur - 1;
                for (int y = limit - shadow_blur + 1; y <= limit; y++) {
                    float a = 0;
                    for (int d = -shadow_blur; d <= shadow_blur; d++) {
                        a += y + d < limit-1 ? (shadow_blur + 1 - (y - (limit - shadow_blur))) * delta : 0;
                    }
                    a /= shadow_blur * 2;
                    g2d.setColor(new Color(shadow_color.getRed(), shadow_color.getGreen(), shadow_color.getBlue(), (int)Math.round(shadow_color.getAlpha() * a)));
                    g2d.fillRect(x, y, 1, 1);
                }
            }

            r1 = (int)Math.round(arc[0] / 2.5);
            r2 = (int)Math.round(arc[2] / 2.5);

            int w = sx + r1;
            int h = sy + r1;

            int y2 = sy + height + shadow_size * 2 - r2;
            int h2 = buffer.getHeight() - y2;

            BufferedImage img1 = new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gr = img1.createGraphics();
            gr.drawImage(buffer, 0, 0, w, h, 0, 0, w, h, this);

            int blur = shadow_blur;
            gr = img1.createGraphics();
            Shape clip = new RoundedRect(sx, sy, width + shadow_size * 2, height + shadow_size * 2, Math.max(0, arc[0]-4), Math.max(0, arc[1]-4), Math.max(0, arc[2]-4), Math.max(0, arc[3]-4));
            for (int y = sy + r1 - 1; y >= 0; y--) {
                for (int x = sx + r1 - 1; x >= 0; x--) {
                    Point2D p = new Point2D.Float(x+2, y);
                    if (clip.contains(p)) {
                        p = new Point2D.Float(x, y);
                        if (!clip.contains(p)) {
                            if (x > sx + r1 - 2) continue;
                            int c1 = x > 2 ? img1.getRGB(x-2, y) : 0;
                            int c2 = buffer.getRGB(x+1, y);
                            int a = (int)Math.round((((c1 >> 24 & 0xFF) * 2 + (c2 >> 24 & 0xFF) * 7) / 9));
                            img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                            if (x > sx + r1 - 3) continue;
                            c2 = buffer.getRGB(x+2, y);
                            a = (int)(((c1 >> 24 & 0xFF) * 1 + (c2 >> 24 & 0xFF) * 12) / 13);
                            img1.setRGB(x+1, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                        }
                        continue;
                    }
                    int a = 0;
                    if (x < 0 || y < 0) continue;
                    for (int d = -blur; d <= blur; d++) {
                        int c = x+d >= 0 ? buffer.getRGB(x+d, y) >> 24 & 0xFF : 0;
                        a += c;
                    }
                    a = (int)Math.round(a / (blur*2+1));
//                        Point2D p1 = new Point2D.Float(x, y);
//                        Point2D p2 = new Point2D.Float(x, y+1);
//                        if (!clip.contains(p1) && clip.contains(p2)) {
//                            a = (int)Math.round((a * 3 + (buffer.getRGB(x, y+1) >> 24 & 0xFF) * 4) / 7);
//                            img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
//                        }
                    img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                }
            }

            repaintRegion(g2d, 0, 0, w, h, img1);

            for (int y = sy + r1 - 1; y >= 0; y--) {
                for (int x = sx + r1 - 1; x >= 0; x--) {
                    Point2D p = new Point2D.Float(x, y+1);
                    if (clip.contains(p)) {
                        p = new Point2D.Float(x, y);
                        if (!clip.contains(p)) {
                            if (y > sy + r1 - 2) continue;
                            int c1 = y > 2 ? img1.getRGB(x, y-2) : 0;
                            int c2 = buffer.getRGB(x, y+1);
                            int a = (int)(((c1 >> 24 & 0xFF) * 1 + (c2 >> 24 & 0xFF) * 7) / 8);
                            img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                            if (y > sy + r1 - 3) continue;
                            c2 = buffer.getRGB(x, y+2);
                            a = (int)(((c1 >> 24 & 0xFF) * 1 + (c2 >> 24 & 0xFF) * 34) / 35);
                            img1.setRGB(x, y+1, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                        }
                        continue;
                    }
                    int a = 0;
                    if (x < 0 || y < 0) continue;
                    for (int d = -blur; d <= blur; d++) {
                        int c = y+d >= 0 ? buffer.getRGB(x, y+d) >> 24 & 0xFF : 0;
                        a += c;
                    }
                    a = (int)Math.round(a / (blur*2+1));
//                        Point2D p1 = new Point2D.Float(x, y);
//                        Point2D p2 = new Point2D.Float(x+1, y);
//                        if (!clip.contains(p1) && clip.contains(p2)) {
//                            a = (int)Math.round((a * 3 + (buffer.getRGB(x+1, y) >> 24 & 0xFF) * 4) / 7.5);
//                            img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
//                        }
                    img1.setRGB(x, y, (shadow_color.getRGB() & 0x00FFFFFF) | a << 24);
                }
            }

            repaintRegion(g2d, 0, 0, w, h, img1);

            BufferedImage img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    img2.setRGB(x, h-y-1, img1.getRGB(x, y));
                }
            }

            repaintRegion(g2d, 0, y2, w, h, img2);

            BufferedImage img3 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    img3.setRGB(w-x-1, y, img1.getRGB(x, y));
                }
            }

            repaintRegion(g2d, sx + width + shadow_size * 2 - w + shadow_blur - 1, 0, w, h, img3);

            BufferedImage img4 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    img4.setRGB(w-x-1, h-y-1, img1.getRGB(x, y));
                }
            }

            //tx = AffineTransform.getRotateInstance(-Math.PI / 2, w/2, h2/2);
            //op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
            //BufferedImage img4 = op.filter(img2, null);
            repaintRegion(g2d, sx + width + shadow_size * 2 - w + shadow_blur - 1, y2, w, h, img4);

        }
    }

    private void repaintRegion(Graphics2D g2d, int x, int y, int w, int h, BufferedImage img) {
        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1f);
        g2d.setComposite(composite);

        g2d.fillRect(x, y, w, h);

        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        g2d.setComposite(composite);

        g2d.drawImage(img, x, y, this);
    }

    public void readGIF(ImageReader reader) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }
        }

        BufferedImage master = null;
        Graphics2D masterGraphics = null;

        for (int frameIndex = 0;; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = Integer.valueOf(gce.getAttribute("delayTime"));
            String disposal = gce.getAttribute("disposalMethod");

            int x = 0;
            int y = 0;

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else {
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }
            }
            masterGraphics.drawImage(image, x, y, null);

            BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
            frames.add(new ImageFrame(copy, delay, disposal));

            if (disposal.equals("restoreToPrevious")) {
                BufferedImage from = null;
                for (int i = frameIndex - 1; i >= 0; i--) {
                    if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                        from = frames.get(i).getImage();
                        break;
                    }
                }
                if (from == null) from = frames.get(0).getImage();

                master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else if (disposal.equals("restoreToBackgroundColor")) {
                masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
            }
        }
        reader.dispose();

        animation_frames = frames.toArray(new ImageFrame[frames.size()]);
        last_frame_displayed = System.currentTimeMillis();

    }

    public long nextFrameDisplayTime() {
        if (!has_animation) return 0;
        return last_frame_displayed + animation_frames[current_frame].getDelay()*10;
    }

    public void displayNextFrame() {
        if (!has_animation) return;
        current_frame = current_frame + 1 > animation_frames.length - 1 ? 0 : current_frame + 1;
        background.bgImage = animation_frames[current_frame].getImage();
        last_frame_displayed = System.currentTimeMillis();
        forceRepaint();
        repaint();
    }

    public int _getX() {
        return getOffsetLeft();
    }

    public int _getY() {
        return getOffsetTop();
    }

    public void setX(int value) {
        int old_value = _x_;
        _x_ = parent != null ? parent._x_ + value : value;
        //if (overflow != Overflow.VISIBLE) {
        //    setBounds(_x_, _y_, width, height);
        //}
        if (old_value != _x_) {
            if (text_layer != null) {
                Component[] c = text_layer.getComponents();
                for (int i = 0; i < c.length; i++) {
                    Rectangle rect = c[i].getBounds();
                    c[i].setBounds(rect.x + _x_ - old_value, rect.y, rect.width, rect.height);
                }
            }
            if (document != null && document.no_layout && children.size() > 0 && children.get(0).type == NodeTypes.TEXT) {
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.get(i).elements.size(); j++) {
                        lines.get(i).elements.get(j).setX(lines.get(i).elements.get(j)._getX());
                    }
                }
            }
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).type == NodeTypes.ELEMENT) {
                    children.get(i).no_draw = true;
                    children.get(i).setX(children.get(i)._getX() + _x_ - old_value);
                    children.get(i).no_draw = false;
                }
            }
        }
        forceRepaint();
    }

    public void setY(int value) {
        int old_value = _y_;
        //if (overflow != Block.Overflow.VISIBLE) {
        //    setBounds(_x_, _y_, width, height);
        //}
        _y_ = parent != null ? parent._y_ + value : value;
        if (old_value != _y_) {
            if (text_layer != null) {
                Component[] c = text_layer.getComponents();
                for (int i = 0; i < c.length; i++) {
                    Rectangle rect = c[i].getBounds();
                    c[i].setBounds(rect.x, rect.y + _y_ - old_value, rect.width, rect.height);
                }
            }
            if (document != null && document.no_layout && children.size() > 0 && children.get(0).type == NodeTypes.TEXT) {
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.get(i).elements.size(); j++) {
                        lines.get(i).elements.get(j).setY(lines.get(i).elements.get(j)._getY());
                    }
                }
            }
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).type == NodeTypes.ELEMENT) {
                    children.get(i).no_draw = true;
                    children.get(i).setY(children.get(i)._getY() + _y_ - old_value);
                    children.get(i).no_draw = false;
                }
            }
        }
        forceRepaint();
    }

    class ImageFrame {
        private final int delay;
        private final BufferedImage image;
        private final String disposal;

        public ImageFrame(BufferedImage image, int delay, String disposal) {
            this.image = image;
            this.delay = delay;
            this.disposal = disposal;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelay() {
            return delay;
        }

        public String getDisposal() {
            return disposal;
        }
    }

    public void setRoot(WebDocument root) {
        this.document = root;
    }

    public void selectAll() {
        if (sel == null) {
            sel = new int[2];
        }
        sel[0] = 0;
        sel[1] = v.size()-1;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i) instanceof Block) {
                ((Block)v.get(i)).selectAll();
            } else {
                Character c = (Character) v.get(i);
                selectCharacter(c);
            }
        }
        forceRepaint();
    }

    public void setSelection(int i1, int i2) {
        if (sel == null) {
            sel = new int[2];
        }
        if (i1 >= 0) {
            sel[0] = i1;
        }
        if (i2 >= 0) {
            sel[1] = i2;
        }
        forceRepaint();
    }

    public void getSelectedText(boolean rec) {
        if (sel == null) {
            selected_text = "";
            return;
        }
        int[] s = Arrays.copyOf(sel, 2);
        int from_element = sel[0];
        int to_element = sel[1];
        if (from_element > to_element) {
            int a = s[0];
            s[0] = s[1];
            s[1] = a;
            from_element = s[0];
            to_element = s[1];
        }

        String result = "";
        try {
            for (int i = 0; i < v.size(); i++) {
                Drawable d = v.get(i);
                if (d instanceof Block) {
                    Block b = (Block)d;
                    b.getSelectedText(true);
                    result += b.selected_text;
                    if (!b.selected_text.isEmpty() && (b.display_type == Display.BLOCK || b.display_type == Display.TABLE_CELL)) {
                        if (document.preserve_layout_on_copy && b.display_type == Display.TABLE_CELL &&
                              i < v.size()-1 && ((Block)v.get(i+1)).display_type == Display.TABLE_CELL && ((Block)v.get(i+1)).getOffsetTop() == b.getOffsetTop()) {
                            result += "\t";
                        } else {
                            result += "\n";
                        }
                    }
                } else if (((Character)d).selected) {
                    result += ((Character)d).getText();
                }
            }
        } catch (Exception e) {}
        if (!rec) result = result.trim();
        selected_text = result;
        document.selected_text = result;
    }

    public void getSelectedText() {
        getSelectedText(false);
    }

    public void clearSelection() {
        sel = null;
        needToRestoreSelection = false;
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).elements.size(); j++) {
                if (lines.get(i).elements.get(j) instanceof Block) {
                    ((Block)lines.get(i).elements.get(j)).clearSelection();
                } else if (lines.get(i).elements.get(j) instanceof Character) {
                    unselectCharacter((Character)lines.get(i).elements.get(j));
                }
                lines.get(i).elements.get(j).selected(false);
            }
        }
        if (original != null) {
            original.clearSelection();
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).positioning != Position.STATIC ||
                    children.get(i).display_type == Display.TABLE_ROW || children.get(i).display_type == Display.TABLE_CELL) {
                children.get(i).clearSelection();
                children.get(i).selected(false);
            }
        }
        //forceRepaint();
        //document.repaint();
    }

    private void addScrollbarX() {
        if (overflow == Overflow.SCROLL) {
            if (scrollbar_x != null) {
                document.panel.remove(scrollbar_x);
            }
            scrollbar_x = new ClippedScrollBar();
            scrollbar_x.setOrientation(JScrollBar.HORIZONTAL);
            document.panel.add(scrollbar_x, 0);
            int sw = width - borderWidth[1] - borderWidth[3];
            if (scrollbar_y != null) {
                scrollbar_y.setBounds(width - borderWidth[1] - scrollbar_y.getPreferredSize().width, borderWidth[0], scrollbar_y.getPreferredSize().width, height - borderWidth[0] - borderWidth[2] - scrollbar_x.getPreferredSize().height);
                //sw -= scrollbar_y.getPreferredSize().width;
            }
            scrollbar_x.setBounds(_x_ + borderWidth[3], _y_ + height - borderWidth[2] - scrollbar_x.getPreferredSize().height, sw, scrollbar_x.getPreferredSize().height);
            int w = content_x_max + borderWidth[1] + borderWidth[3];
            scrollbar_x.getModel().setRangeProperties(0, viewport_width, 0, w+1, false);
            scrollbar_x.setVisibleAmount(viewport_width - borderWidth[3]);
            scrollbar_x.addAdjustmentListener(new ScrollListener(this, 0));
            if (content_y_max == viewport_height) {
                content_y_max = viewport_height - scrollbar_x.getPreferredSize().height;
                if (children.size() == 1 && children.get(0).type == NodeTypes.ELEMENT && children.get(0).auto_y_margin) {
                    viewport_height = viewport_height - scrollbar_x.getPreferredSize().height;
                    children.get(0).setAutoYMargin(true);
                }
            } else {
                viewport_height = viewport_height - scrollbar_x.getPreferredSize().height;
            }
            if (scrollbar_y == null) {
                viewport_width = width;
            } else {
                viewport_width = width - scrollbar_y.getPreferredSize().width;
            }
            if (scrollbar_y != null) {
                scrollbar_y.setVisibleAmount(viewport_height - borderWidth[0]);
            }
            //setBounds(_x_, _y_, width, height);
            //width = viewport_width + (scrollbar_y != null ? scrollbar_y.getPreferredSize().width : 0);
            //width = Math.max(width, w);
            //width = w;
        }
    }

    private void addScrollbarY() {
        if (overflow == Overflow.SCROLL) {
            if (scrollbar_y != null) {
                if (width == viewport_width && this == document.root) {
                    viewport_width += scrollbar_y.getPreferredSize().width;
                    width = viewport_width;
                }
                document.panel.remove(scrollbar_y);
                scrollbar_y = null;
                viewport_height = height;
                if (scrollbar_x != null) {
                    viewport_height -= scrollbar_x.getPreferredSize().height;
                }
            }
            scrollbar_y = new ClippedScrollBar();
            document.panel.add(scrollbar_y, 0);
            int sh = height - borderWidth[0] - borderWidth[2];
            if (scrollbar_x != null) {
                sh -= scrollbar_x.getPreferredSize().height;
                scrollbar_x.setBounds(_x_ + borderWidth[3], _y_ + height - borderWidth[2] - scrollbar_x.getPreferredSize().height, width - borderWidth[1] - borderWidth[3], scrollbar_x.getPreferredSize().height);
            }
            scrollbar_y.setBounds(_x_ + width - scrollbar_y.getPreferredSize().width, _y_ + borderWidth[0], scrollbar_y.getPreferredSize().width, sh);
            int h = Math.max(content_y_max, lines.size() > 0 ? lines.lastElement().getOffsetTop() + lines.lastElement().getHeight() + paddings[2] : 0);
            //h += borderWidth[0] + borderWidth[2];
            scrollbar_y.getModel().setRangeProperties(0, viewport_height, 0, h, false);
            scrollbar_y.setVisibleAmount(viewport_height);
            scrollbar_y.addAdjustmentListener(new ScrollListener(this, 1));
            viewport_width = width - scrollbar_y.getPreferredSize().width;
            viewport_height = height;
            if (scrollbar_x != null) {
                viewport_height -= scrollbar_x.getPreferredSize().height;
            }
            //setBounds(_x_, _y_, width, height);
            //height = viewport_height + (scrollbar_x != null ? scrollbar_x.getPreferredSize().height : 0);
            //height = Math.max(height, h);
            //height = h;
        }
    }

    public boolean hasHorizontalScrollbar() {
        return scrollbar_x != null;
    }

    public boolean hasVerticalScrollbar() {
        return scrollbar_y != null;
    }

    public int getScrollbarXSize() {
        return scrollbar_x != null ? scrollbar_x.getPreferredSize().height : 0;
    }

    public int getScrollbarYSize() {
        return scrollbar_y != null ? scrollbar_y.getPreferredSize().width : 0;
    }

    class ScrollListener implements AdjustmentListener {

        ScrollListener(Block b, int t) {
           block = b;
           type = t;
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (type > 0) {
                block.scroll_top = e.getValue();
            } else {
                block.scroll_left = e.getValue();
            }
            block.flushBuffersRecursively();
            block.forceRepaint();
            document.repaint();
        }

        int type = 0;
        Block block;
    }

    public boolean processImage() {
        if (!isImage) return false;
        if (background == null) {
            background = new Background();
        }
        if (background.bgImage == null) {
            if (width < 0) width = viewport_width = (int) Math.round(16 * ratio);
            if (height < 0) height = viewport_height = (int) Math.round(16 * ratio);
            setBorderColor("#8a8a9f");
            borderWidth = new int[] {1, 1, 1, 1};
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            if (background.bgcolor == null || background.bgcolor.equals(new Color(0, 0, 0, 0))) {
                background.bgcolor = new Color(85, 85, 85, 75);
            }
            setBackgroundImage("res" + File.separatorChar + "photo_16.png");
            background.background_pos_x = 1;
            background.background_size_x = 16;
            background.background_size_y = 16;
            background.background_size_x_auto = false;
            background.background_size_y_auto = false;
            setBackgroundRepeat(BackgroundRepeat.NONE);
            special = true;
        } else {
            if (width < 0 && height < 0) {
                width = viewport_width = background.bgImage.getWidth();
                height = viewport_height = background.bgImage.getHeight();
                auto_width = true;
                auto_height = true;
            } else {
                double aspect_ratio = (double) background.bgImage.getWidth() / background.bgImage.getHeight();
                if (width < 0) {
                    width = viewport_width = (int) (height * aspect_ratio);
                    auto_width = true;
                    auto_height = false;
                } else if (height < 0) {
                    height = viewport_height = (int) ((double) width / aspect_ratio);
                    auto_width = false;
                    auto_height = true;
                }
            }
            borderWidth = new int[] {0, 0, 0, 0};
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            background.bgcolor = null;
            background.background_pos_x = 0;
            background.background_pos_y = 0;
            background.background_size_x = viewport_width;
            background.background_size_y = viewport_height;
            special = false;
        }

        return true;
    }

    public boolean processInput() {
        if (inputType == Input.NONE) return false;
        if (inputType >= Input.TEXT && inputType <= Input.BUTTON) {
            if (getComponents().length > 0 && getComponents()[0] instanceof JButton && children.size() > 0) {
                children.get(0).textContent = ((JButton)this.getComponents()[0]).getText();
            }
            removeAll();
            final JTextComponent tf = inputType == Input.TEXT ? new JTextField() : new JTextArea();
            final JButton btn = new JButton();
            if (children.size() > 0 && children.get(0).textContent != null) {
                tf.setText(children.get(0).textContent);
                btn.setText(children.get(0).textContent);
                children.get(0).setTextColor(new Color(0, 0, 0, 0));
                children.get(0).textContent = "";
            }
            tf.setPreferredSize(new Dimension(width, height));
            btn.setPreferredSize(new Dimension(width, height));
            tf.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
            btn.setFont(new Font(fontFamily, Font.PLAIN, fontSize));

            if (inputType < Input.BUTTON) {
                add(tf);
                tf.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        Block parent = ((Block)tf.getParent());
                        parent.inputValue = tf.getText();
                        if (parent.node != null) {
                            parent.fireEventForNode(e, parent.node, null, "keyPress");
                            parent.fireEventForNode(e, parent.node, null, "input");
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        Block parent = ((Block)tf.getParent());
                        if (parent.node != null) {
                            parent.fireEventForNode(e, parent.node, null, "keyDown");
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        Block parent = ((Block)tf.getParent());
                        if (parent.node != null) {
                            parent.fireEventForNode(e, parent.node, null, "keyUp");
                        }
                    }
                });
                tf.setBounds(_x_, _y_, width, height);
                tf.addMouseListener(this);
            } else {
                add(btn);
                btn.setFocusPainted(false);
                insertButton(btn);
            }

            tf.setMargin(new Insets(paddings[0], paddings[1], paddings[2], paddings[3]));
            btn.setMargin(new Insets(paddings[0], paddings[1], paddings[2], paddings[3]));
            if (background != null && background.bgcolor != null) {
                tf.setBackground(background.bgcolor);
                btn.setBackground(background.bgcolor);
            } else if (inputType == Input.BUTTON) {
                Vector<Color> c = new Vector<Color>();
                Vector<Float> p = new Vector<Float>();
                c = new Vector<Color>();
                c.add(new Color(110, 110, 132));
                c.add(new Color(165, 165, 178));
                c.add(new Color(235, 235, 235));
                p = new Vector<Float>();
                p.add(0f);
                p.add(0.28f);
                p.add(0.82f);
                setLinearGradient(c, p, 180);
            }
            tf.setForeground(color);
            btn.setForeground(color);
            btn.setContentAreaFilled(false);
            if ((borderWidth[0] > 0 || borderWidth[1] > 0 || borderWidth[2] > 0 || borderWidth[3] > 0) &&
                   (borderColor[0].getAlpha() > 0 || borderColor[1].getAlpha() > 0 || borderColor[2].getAlpha() > 0 || borderColor[3].getAlpha() > 0) ||
                   background != null && background.bgcolor != null && (background.bgcolor.getAlpha() < 255 || inputType == 3 && background.bgcolor.getAlpha() > 0)) {
                tf.setOpaque(false);
                tf.setBorder(null);
                btn.setOpaque(false);
                btn.setBorderPainted(false);
                if (inputType < 3) {
                    tf.setBounds(_x_ + borderWidth[3] + paddings[3], _y_ + borderWidth[0], width - borderWidth[3] - borderWidth[1] - paddings[3] - paddings[1], height - borderWidth[0] - borderWidth[2]);
                } else {
                    btn.setBounds(_x_ + borderWidth[3], _y_ + borderWidth[0], width - borderWidth[3] - borderWidth[1], height - borderWidth[0] - borderWidth[2]);
                }
            }
        }
        if (inputType >= Input.RADIO && inputType <= Input.CHECKBOX) {
            boolean ready = document.ready;
            document.ready = false;
            removeAllElements();
            document.ready = ready;
            final Block instance = this;
            final JToggleButton rb;
            if (inputType == Input.RADIO) {
                rb = new JRadioButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        if (instance.document.use_native_inputs) {
                            super.paintComponent(g);
                            return;
                        }

                        int size = (int) Math.round(12 * ratio);
                        //Graphics2D g2d = (Graphics2D) g;
                        instance.clearBuffer();
                        Graphics2D g2d = (Graphics2D) instance.buffer.getGraphics();

                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(new Color(13, 13, 13, 18));
                        int x0 = Math.max(0, (int)((getWidth() - size) / 2) - 2);
                        int y0 = Math.max(0, (int)((getHeight() - size) / 2) - 2);
                        g2d.drawOval(x0, y0, size, size);
                        g2d.setColor(new Color(13, 13, 13, 48));
                        g2d.drawOval(x0 + 1, y0 + 1, size - 2, size - 2);
                        g2d.setColor(new Color(45, 47, 58, 153));
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawOval(x0 + 2, y0 + 2, size - 4, size - 4);
                        Color col = instance.background != null && instance.background.bgcolor != null ? instance.background.bgcolor : new Color(245, 245, 245);
                        g2d.setColor(col);
                        g2d.fillOval(x0 + 3, y0 + 3, size - 6, size - 6);

                        if (this.getModel().isSelected()) {
                            g2d.setColor(new Color(45, 47, 58, 153));
                            g2d.fillOval(x0 + 5, y0 + 5, size - 9, size - 9);
                            g2d.setColor(new Color(83, 97, 115, 203));
                            g2d.fillOval(x0 + 6, y0 + 6, size - 11, size - 11);
                            g2d.setColor(new Color(183, 187, 195, 47));
                            g2d.fillOval(x0 + 7, y0 + 7, 2, 2);
                            g2d.setColor(new Color(183, 187, 195, 38));
                            g2d.fillOval(x0 + 9, y0 + 7, 1, 1);
                        }
                    }
                };
            } else {
                rb = new JCheckBox() {
                    @Override
                    public void paintComponent(Graphics g) {
                        if (instance.document.use_native_inputs) {
                            super.paintComponent(g);
                            return;
                        }

                        int size = (int) Math.round(12 * ratio);
                        //Graphics2D g2d = (Graphics2D) g;
                        instance.clearBuffer();
                        Graphics2D g2d = (Graphics2D) instance.buffer.getGraphics();

                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        //g2d.setColor(new Color(13, 13, 13, 18));
                        int x0 = Math.max(0, (int)((getWidth() - size) / 2) - 2);
                        int y0 = Math.max(0, (int)((getHeight() - size) / 2) - 2);
                        //g2d.drawRoundRect(x0, y0, size, size, 2, 2);
                        g2d.setColor(new Color(13, 13, 13, 26));
                        g2d.drawRoundRect(x0 + 1, y0 + 1, size - 2, size - 2, 2, 2);
                        g2d.setColor(new Color(75, 78, 85, 153));
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRoundRect(x0 + 2, y0 + 2, size - 4, size - 4, 2, 2);
                        Color col = instance.background != null && instance.background.bgcolor != null ? instance.background.bgcolor : new Color(245, 245, 245);
                        g2d.setColor(col);
                        g2d.fillRoundRect(x0 + 3, y0 + 3, size - 6, size - 6, 2, 2);

                        if (this.getModel().isSelected()) {
                            g2d.setColor(new Color(65, 67, 78, 23));
                            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f));
                            g2d.drawLine(x0 + 3, y0 + 8, x0 + 7, y0 + 14);
                            g2d.drawLine(x0 + 7, y0 + 14, x0 + 14, y0 + 2);
                            
                            g2d.setColor(new Color(96, 103, 125));

                            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f));
                            g2d.drawLine(x0 + 2, y0 + 10, x0 + 6, y0 + 14);
                            g2d.drawLine(x0 + 6, y0 + 14, x0 + 14, y0 + 3);

                            g2d.setColor(new Color(83, 97, 115, 35));
                            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f));
                            g2d.drawLine(x0 + 2, y0 + 12, x0 + 6, y0 + 15);
                            g2d.drawLine(x0 + 6, y0 + 15, x0 + 14, y0 + 4);
                        }
                    }
                };
            }
            if (!document.use_native_inputs) rb.setOpaque(false);
            add(rb);
            int size = (int) Math.round(12 * ratio);
            rb.setPreferredSize(new Dimension(size, size));
            if (width < rb.getPreferredSize().width) width = viewport_width = rb.getPreferredSize().width;
            height = viewport_height = rb.getPreferredSize().height;
            rb.setBounds(_x_ + width / 2 - rb.getPreferredSize().width / 2 - 1, _y_ + height / 2 - rb.getPreferredSize().height / 2, height, height);
            if (checked || node != null && node.states.contains("checked")) {
                checked = true;
                if (node != null && !node.states.contains("checked")) {
                    node.states.add("checked");
                    node.attributes.put("selected", "");
                }
                rb.getModel().setSelected(true);
            }
        }
        if (inputType == Input.FILE) {
            boolean ready = document.ready;
            document.ready = false;
            removeAllElements();

            final Block label = new Block(document);
            final Block btn = createButton("…", null);

            addElement(label, true);
            label.addText("No file selected");

            label.setPositioning(Position.ABSOLUTE);
            label.setLeft(0, Units.px);
            label.setTop(2, Units.px);
            label.width = label.viewport_width = width - btn.width - 6;
            label.height = label.viewport_height = height;
            label.fontSize = (int) Math.round(12 * ratio);
            label.setWhiteSpace(WhiteSpace.NO_WRAP);
            label.setOverflow(Overflow.HIDDEN);

            addElement(btn, true);

            btn.setPositioning(Position.ABSOLUTE);
            btn.setRight(0, Units.px);
            btn.setTop(0, Units.px);

            ((JButton)btn.getComponent(0)).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(document);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        label.children.get(0).textContent = selectedFile.getAbsolutePath();
                        label.performLayout();
                        label.forceRepaint();
                    }
                }
            });

            document.ready = ready;

            label.performLayout();
            label.forceRepaint();

            //label.performLayout();
            //if (!document.isPainting) label.draw();
            //JLabel label = new JLabel("No file selected");
            //label.setFont(new Font(fontFamily, Font.PLAIN, (int) Math.round(12 * ratio)));
            //add(label);
            //label.setBounds(_x_ + 2, _y_ + 2, width - btn.width - 10, label.getPreferredSize().height);
            //label.text_layer.setBounds(_x_, _y_, width, height);
            if (width < btn.width) width = viewport_width = btn.width;
        }
        if (display_type > Display.INLINE_BLOCK) display_type = Display.INLINE_BLOCK;

        return true;
    }

    private Block createButton(String label, Color color) {

        final Block b = new Block(document);

        final JButton btn = new JButton();

        btn.setText(label);
        btn.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
        int width = getFontMetrics(btn.getFont()).stringWidth(label) + 16;
        btn.setPreferredSize(new Dimension(width, height));

        b.width = b.viewport_width = width;
        b.height = b.viewport_height = height;
        b.auto_width = false;
        b._x_ = _x_ + this.width - width;
        b._y_ = _y_;

        b.setBorderColor(new Color(118, 118, 123));
        b.setScaleBorder(false);
        b.setBorderWidth(1);
        b.setBorderRadius(2);

        b.add(btn);
        btn.setFocusPainted(false);
        if (!document.use_native_inputs) {
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
        }
        b.insertButton(btn);

        return b;
    }

    public void insertButton(final JButton btn) {
        btn.setBounds(_x_, _y_, width, height);
        if (background == null || background.bgcolor == null && background.gradient == null) {
            //bgcolor = new Color(207, 210, 218);
            Vector<Color> c = new Vector<Color>();
            c.add(new Color(117, 113, 138));
            c.add(new Color(218, 218, 228));
            c.add(new Color(235, 235, 235));
            Vector<Float> p = new Vector<Float>();
            p.add(0f);
            p.add(0.28f);
            p.add(0.82f);
            setLinearGradient(c, p, 180);
            final Block instance = this;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    btn.getModel().setPressed(true);
                    btn.getModel().setPressed(false);
                    instance.clearBuffer();
                    instance.forceRepaint();
                    instance.document.repaint();
                }

            });
        }
        final Color col = background.bgcolor;
        final Color new_col = col != null ? new Color((int)Math.min(col.getRed() * 1.03, 255), (int)Math.min(col.getGreen() * 1.03, 255), (int)Math.min(col.getBlue() * 1.03, 255)) : null;

        btn.getModel().addChangeListener(new ChangeListener() {
            private boolean rollover = false;
            private boolean pressed = false;

            @Override
            public void stateChanged(ChangeEvent e) {
                ButtonModel model = (ButtonModel) e.getSource();
                if (col != null) {
                    if (model.isRollover() != rollover || model.isPressed() != pressed) {
                        rollover = model.isRollover();
                        pressed = model.isPressed();
                        if (((Block)btn.getParent()).background == null) {
                            ((Block)btn.getParent()).background = new Background();
                        }
                        if (rollover) {
                            ((Block)btn.getParent()).background.bgcolor = new_col;
                        } else {
                            ((Block)btn.getParent()).background.bgcolor = col;
                        }
                    }
                    return;
                }
                if (model.isRollover() != rollover || model.isPressed() != pressed) {
                    rollover = model.isRollover();
                    pressed = model.isPressed();
                    Vector<Color> c = new Vector<Color>();
                    Vector<Float> p = new Vector<Float>();
                    if (rollover && !pressed) {
                        c = new Vector<Color>();
                        c.add(new Color(146, 151, 164));
                        c.add(new Color(207, 208, 214));
                        c.add(new Color(210, 210, 218));
                        c.add(new Color(235, 235, 235));
                        p = new Vector<Float>();
                        p.add(0f);
                        p.add(0.29f);
                        p.add(0.38f);
                        p.add(0.82f);
                    } else if (!rollover && pressed) {
                        c = new Vector<Color>();
                        c.add(new Color(119, 119, 130));
                        c.add(new Color(176, 176, 183));
                        c.add(new Color(237, 237, 237));
                        p = new Vector<Float>();
                        p.add(0f);
                        p.add(0.32f);
                        p.add(0.78f);
                    } else if (rollover && pressed) {
                        c = new Vector<Color>();
                        c.add(new Color(123, 123, 132));
                        c.add(new Color(181, 181, 187));
                        c.add(new Color(238, 238, 238));
                        p = new Vector<Float>();
                        p.add(0f);
                        p.add(0.32f);
                        p.add(0.78f);
                    } else {
                        c = new Vector<Color>();
                        c.add(new Color(131, 137, 148));
                        c.add(new Color(196, 196, 201));
                        c.add(new Color(196, 196, 201));
                        c.add(new Color(235, 235, 235));
                        p = new Vector<Float>();
                        p.add(0f);
                        p.add(0.23f);
                        p.add(0.27f);
                        p.add(0.82f);
                    }
                    setLinearGradient(c, p, 180);
                    if (btn.getParent() != null) ((Block)btn.getParent()).setLinearGradient(c, p, 180);
                }
            }
        });
        btn.getModel().setRollover(false);
    }

    public void saveSelectionRange() {
        int from = -1;
        int to = -1;
        int len = 0;
        if (parts.size() > 0) {
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i).sel == null) parts.get(i).sel = new int[] {-1, -1};
                if ((parts.get(i).sel[0] < 0 || from >= 0) && parts.get(i).sel[1] < 0) {
                    if (parts.get(i).children.size() > 0) {
                        len += parts.get(i).children.get(0).textContent.length();
                    }
                    continue;
                }
                if (parts.get(i).sel[0] >= 0 && from < 0) {
                    from = len;
                    len = 0;
                }
                if (parts.get(i).sel[1] > 0 && from >= 0) {
                    if (parts.get(i).children.size() > 0) {
                        len += parts.get(i).sel[1];
                    }
                    to = len;
                    break;
                } else if (parts.get(i).children.size() > 0) {
                    len += parts.get(i).children.get(0).textContent.length() - parts.get(i).sel[0];
                }
            }
            sel = new int[] {from, to};
        }
    }

    public void restoreSelectionRange() {
        if (sel == null || sel[0] < 0 && sel[1] < 0) return;
        int pos = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).children.size() == 0) continue;
            if (parts.get(i).sel == null) {
                parts.get(i).sel = new int[] {-1, -1};
            }
            int len = parts.get(i).children.get(0).textContent.length();
            if (sel[0] >= pos && sel[0] < pos + len) {
                parts.get(0).sel[0] = sel[0] - pos;
                parts.get(0).sel[1] = Math.min(sel[1] - pos, len-1);
            } else if (sel[1] >= pos && sel[1] < pos + len) {
                parts.get(0).sel[0] = 0;
                parts.get(0).sel[1] = Math.min(sel[1] - pos, len-1);
            } else if (sel[0] < pos && sel[1] >= pos + len) {
                parts.get(0).sel[0] = 0;
                parts.get(0).sel[1] = len-1;
            }
            pos += len;
        }
    }

    public synchronized void performLayout() {
        if (document == null) return;
        if (document.inLayout && this == document.root) {
            try {
                if (document.debug) System.out.println("Retrying layout...");
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
            performLayout();
            return;
        }
        if (this == document.root) document.inLayout = true;
        performLayout(false, false);
        if (this == document.root) document.inLayout = false;
    }

    public synchronized void performLayout(boolean no_rec) {
        performLayout(no_rec, false);
    }

    public synchronized void performLayout(boolean no_rec, boolean no_viewport_reset) {

        if (no_layout || !document.ready || display_type == Display.NONE) return;

        if (!no_viewport_reset && auto_width && parent != null) viewport_width = 0;

        if (document.debug) {
            System.out.println("Layout started for block " + toString());
            System.out.println();
        }

        text_shadow_buffer = null;

        if (processImage()) return;
        if (processInput()) return;

        if (children.size() == 0 && getComponents().length == 1 && getComponents()[0] instanceof MediaPlayer.VideoRenderer) {
            int delta = parent.width - width;
            width = parent.width;
            height = parent.height - parent.children.get(1).height;
            Block progress = parent.children.get(1).children.get(1);
            progress.width = progress.max_width = progress.children.get(0).width = progress.width + delta;
            progress.orig_width = (int) ((double)progress.width / ratio);
            getComponents()[0].setBounds(_x_ - scroll_x, _y_ - scroll_y, width, height);
            return;
        }

        if (parent != null && parent.isMedia && height == Math.round(MediaPlayer.panel_height * ratio)) {
            int delta = parent.width - width;
            width = parent.width;
            Block progress = children.get(1);
            progress.width = progress.max_width = progress.children.get(0).width = progress.width + delta;
            progress.orig_width = (int) ((double)progress.width / ratio);
            progress.children.get(0).orig_width = progress.orig_width;
        }

//      if (children.size() == 1 && children.get(0).type == NodeTypes.ELEMENT && children.get(0).auto_y_margin) {
//          if (viewport_height > children.get(0).height) children.get(0).setAutoYMargin();
//      }

        lines.clear();
        layouter = new Layouter(document);
        layouter.clearBoth();
        layouter.setCurrentBlock(this);

        pref_size = 0;
        min_size = 0;

        if (text_layer != null) {
            text_layer.removeAll();
            remove(text_layer);
            text_layer = null;
        }

        //if (beforePseudoElement != null) document.root.remove(beforePseudoElement);
        //if (afterPseudoElement != null) document.root.remove(afterPseudoElement);

        content_x_max = 0;
        content_y_max = 0;

        boolean is_table_cell = parent != null && parent.parent != null && (parent.parent.display_type == Block.Display.TABLE || parent.parent.display_type == Block.Display.INLINE_TABLE);

        if (!is_table_cell && !no_viewport_reset && viewport_width > 0) {
            width = viewport_width + (scrollbar_y != null ? scrollbar_y.getPreferredSize().width : 0);
        }
        if (!is_table_cell && !no_viewport_reset && viewport_height > 0) {
            height = viewport_height + (scrollbar_x != null ? scrollbar_x.getPreferredSize().height : 0);
        }

        if (is_table_cell) {
            if (auto_width) width = children.size() == 0 ? 1 : 50000;
            if (auto_height) height = 1;
            else height = parent.parent.fontSize + borderWidth[0] + borderWidth[2] + paddings[0] + paddings[2];
        }

        if (viewport_width == 0) viewport_width = width;
        if (viewport_height == 0 || !no_viewport_reset) viewport_height = height;

        if (dimensions != null) {
            boolean ready = document.ready;
            document.ready = false;
            Set<String> keys = dimensions.keySet();
            for (String key: keys) {
                DynamicValue dvalue = dimensions.get(key);
                CssLength value = dvalue.expression != null ? new CssLength(getValueInCssPixels(dvalue.expression), Units.px) : dvalue.length;
                if (key.equals("left")) {
                    setLeft(value.value, value.unit);
                } else if (key.equals("right")) {
                    setRight(value.value, value.unit);
                } else if (key.equals("top")) {
                    setTop(value.value, value.unit);
                } else if (key.equals("bottom")) {
                    setBottom(value.value, value.unit);
                } else if (key.equals("width") && (display_type != Display.BLOCK && display_type != Display.FLEX || value.value > 0)) {
                    setWidth((int) Math.round(value.value), value.unit);
                } else if (key.equals("height") && value.value >= 0) {
                    setHeight((int) Math.round(value.value), value.unit);
                }
                if (dvalue.expression != null) {
                    dimensions.put(key, dvalue);
                }
            }

            CssLength widthValue = keys.contains("width") ? (dimensions.get("width").expression != null ? new CssLength(getValueInCssPixels(dimensions.get("width").expression), Units.px) : dimensions.get("width").length) : null;
            CssLength heightValue = keys.contains("height") ? (dimensions.get("height").expression != null ? new CssLength(getValueInCssPixels(dimensions.get("height").expression), Units.px) : dimensions.get("height").length) : null;

            if (aspect_ratio >= 0 && (keys.contains("width") && (!keys.contains("height") || heightValue.value < 0) || keys.contains("height") && (!keys.contains("width") || widthValue.value < 0))) {

                if (keys.contains("width") && (!keys.contains("height") || heightValue.value < 0)) {
                    double w = getValueInCssPixels(widthValue.value, widthValue.unit);
                    double h = aspect_ratio > 0 ? w / aspect_ratio : 0;
                    setHeight((int) Math.round(h), Units.px);
                } else {
                    double h = getValueInCssPixels(heightValue.value, heightValue.unit);
                    double w = aspect_ratio * h;
                    setWidth((int) Math.round(w), Units.px);
                }
            }
            document.ready = ready;
        }

        Vector<Block> floats = new Vector<Block>();
        processFloatBlocks(floats, no_rec);
        Vector<Block> blocks = ((Vector<Block>)children.clone());
        blocks.removeAll(floats);

        processListElements(blocks);

        if (beforePseudoElement != null) blocks.add(0, beforePseudoElement);
        if (afterPseudoElement != null) blocks.add(afterPseudoElement);

        for (int i = 0; i < blocks.size(); i++) {
            Block el = blocks.get(i);

            if (el.type == NodeTypes.TEXT) {
                if (el.white_space != WhiteSpace.PRE_WRAP && el.textContent.matches("^\\s*$") && (parent.layouter.last_line == null || parent.layouter.last_line.elements.isEmpty())) {
                    continue;
                }
                if (parent == null || !isPseudoElement()) {
                    el.textContent = replaceEntities(el.textContent);
                }
                String[] w = el.textContent.split("((?<=\\s+)|(?=\\s+))");
                int style = (el.parent.text_bold || el.parent.text_italic) ? ((el.parent.text_bold ? Font.BOLD : 0) | (el.parent.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font f = new Font(el.parent.fontFamily, style, el.parent.fontSize);
                layouter.last_element = i;
                for (int j = 0; j < w.length; j++) {
                    if (w[j].length() == 0) continue;
                    layouter.last_word = j;
                    layouter.addWord(w[j], f);
                    layouter.last_word = -1;

                    if (original != null && original.parts.size() > 1) {
                        int h = original.parts.lastElement().lines.get(0).getHeight();
                        original.parts.lastElement().height = original.parts.lastElement().viewport_height = h;
                        lines.lastElement().height = h;
                    }

                    content_y_max = lines.lastElement().getY() - borderWidth[0] + lines.lastElement().getHeight() + paddings[2];
                    if (parent != null && content_y_max > parent.height && !parent.auto_height) {
                        boolean had_scroll = scrollbar_y != null;
                        addScrollbarY();
                        if (!had_scroll && overflow == Overflow.SCROLL) {
                            performLayout(no_rec, true);
                            return;
                        }
                    }
                }
                layouter.last_element = -1;
                Line last = lines.lastElement();
                if (auto_height) {
                    viewport_height = height = last.getY() + last.getHeight() + paddings[2] + borderWidth[2];
                }
                if (is_table_cell && auto_width) {
                    width = Math.max(min_width, pref_size);
                    orig_width = (int)Math.floor(width / ratio);
                }
            } else {

                if (el.viewport_width == 0) el.viewport_width = el.width;
                if (el.viewport_height == 0) el.viewport_height = el.height;

                if (no_rec) el.no_layout = true;
                layouter.last_element = i;
                layouter.addBlock(el);
                layouter.last_element = -1;
                el.no_layout = false;

                for (int j = 0; j < el.parts.size(); j++) {
                    el.parts.get(j).sortBlocks();
                }

                updateMaxContentSize(el);

                if (scrollbar_y != null) {
                    document.panel.setComponentZOrder(scrollbar_y, 0);
                }
            }
        }
        pref_size += paddings[3] + paddings[1] + borderWidth[3] + borderWidth[1];
        min_size += paddings[3] + paddings[1] + borderWidth[3] + borderWidth[1];
        if ((layouter.getBlock().cut & Cut.LEFT) > 0 && (layouter.getBlock().cut & Cut.RIGHT) == 0) {
            layouter.getBlock().width = layouter.getBlock().lines.get(0).getWidth() + paddings[1] + borderWidth[1];
            layouter.getBlock().viewport_width = layouter.getBlock().width;
            layouter.getBlock().orig_width = (int)Math.floor(layouter.getBlock().width / layouter.getBlock().ratio);
        }
        if (lines.size() > 0) {
            if (display_type == Display.FLEX || display_type == Display.INLINE_FLEX) {
                processFlex();
            }
            Line last = lines.lastElement();
            while (display_type != Display.INLINE && !last.elements.isEmpty() && last.elements.lastElement() instanceof Character &&
                    ((Character)last.elements.lastElement()).getText().matches("\\s+")) {
                last.cur_pos -= ((Character)last.elements.lastElement()).getWidth();
                last.elements.remove(last.elements.size()-1);
            }
            if (Layouter.stack.isEmpty() && width == 0) {
                last.setWidth(last.cur_pos);
                boolean inversed = display_type != Display.FLEX && display_type != Display.INLINE_FLEX || flex_direction != Direction.COLUMN && flex_direction != Direction.COLUMN_REVERSED;
                width = borderWidth[3] + paddings[3] + (!inversed ? lines.get(0).getWidth() : last.getWidth()) + paddings[1] + borderWidth[1];
                height = borderWidth[0] + paddings[0] + last.getY() + (!inversed ? last.getHeight() : lines.get(0).getWidth()) + paddings[2] + borderWidth[2];
                viewport_width = width;
                viewport_height = height;
                orig_width = (int)Math.floor(width / ratio);
                orig_height = (int)Math.floor(height / ratio);
            }
        }
        if (auto_height) {
            if (lines.size() == 0) {
                height = paddings[0] + paddings[2] + borderWidth[0] + borderWidth[2];
            } else {
                height = lines.lastElement().getY() + lines.lastElement().getHeight() + paddings[2] + borderWidth[2];
                if (lines.lastElement().elements.size() > 0 && lines.lastElement().elements.get(0) instanceof Block &&
                        ((Block)lines.lastElement().elements.get(0)).display_type == Block.Display.BLOCK) {
                    height += ((Block)lines.lastElement().elements.get(0)).margins[2];
                }
            }
            orig_height = (int)Math.round(height / ratio);
            viewport_height = height;
        } else {
            if (children.size() == 1 && children.get(0).auto_y_margin) {
                children.get(0).setAutoYMargin(true);
            }
        }

        if (display_type != Display.INLINE) performSizeCheck();

        if (childDocument != null && viewport_width > 0 && (auto_height || viewport_height > 0)) {
            Block root = childDocument.getRoot();
            root.width = root.viewport_width = width;
            root.height = root.viewport_height = height;
            root.orig_width = (int)Math.floor(root.width / root.ratio);
            root.orig_height = (int)Math.floor(root.height / root.ratio);
            //childDocument.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));

            childDocument.width = childDocument.root.width = viewport_width;

            if (auto_height) {
                root.auto_height = true;
                root.height = -1;
                root.performLayout();
                childDocument.height = root.viewport_height;
                height = viewport_height = root.viewport_height;
                orig_height = (int) Math.floor((double) height / ratio);
                childDocument.setBounds(_x_, _y_, viewport_width, viewport_height);
                childDocument.setSize(viewport_width, viewport_height);
                //childDocument.root.setBounds(0, 0, viewport_width, viewport_height);
            } else {
                childDocument.setBounds(_x_, _y_, viewport_width, viewport_height);
                childDocument.setSize(viewport_width, viewport_height);
                //childDocument.root.setBounds(0, 0, viewport_width, viewport_height);
                childDocument.root.auto_height = false;
                childDocument.height = childDocument.root.height = viewport_height;
                root.performLayout();
            }
        }

        boolean is_flex = display_type == Block.Display.FLEX || display_type == Block.Display.INLINE_FLEX;

        if (text_align != TextAlign.ALIGN_LEFT || is_flex && flex_justify != FlexJustify.START) {
            Layouter.applyHorizontalAlignment(this);
        }

        sortBlocks();
        
        if (this == document.root && this.children.size() > 0) {
            setZIndices();
            clipScrollbars();
        }

        if (node != null && display_type != Display.INLINE) {
            node.fireEvent("layout", "render");
        }
        
        if (document.debug && display_type != Display.INLINE) {
            System.out.println();
            System.out.println("Layout ended for block " + toString());
        }
    }

    public void processFlex() {
        content_x_max = 0;
        content_y_max = 0;
        int delta_y = 0;
        boolean x_axis = flex_direction == Direction.ROW || flex_direction == Direction.ROW_REVERSED;
        boolean inversed = flex_direction == Direction.ROW_REVERSED || flex_direction == Direction.COLUMN_REVERSED;

        int xmax = width - borderWidth[1] - paddings[1] - borderWidth[3];
        int ymax = height - borderWidth[2] - paddings[2] - borderWidth[0];

        if (scrollbar_x != null) {
            ymax -= scrollbar_x.getPreferredSize().height;
        }
        if (scrollbar_y != null) {
            xmax -= scrollbar_y.getPreferredSize().width;
        }

        if (x_axis) {
            delta_y = ymax - (lines.lastElement().top + lines.lastElement().height);
        } else {
            delta_y = xmax - (lines.lastElement().left + lines.lastElement().height);
        }
        for (int i = 0; i < lines.size(); i++) {
            int width = lines.get(i).getWidth();
            int weight_sum_grow = 0;
            int weight_sum_shrink = 0;

            int cur_width = 0;
            int index = 0;
            Block last_block = null;
            for (Drawable d: lines.get(i).elements) {
                if (d instanceof Block) {
                    Block b = (Block) d;
                    int w = x_axis ? b.margins[3] + b.width + b.margins[1] : b.margins[0] + b.height + b.margins[2];
                    cur_width += w + (index > 0 ? flex_gap : 0);
                    weight_sum_grow += b.flex_grow;
                    weight_sum_shrink += b.flex_shrink;
                    last_block = b;
                }
                index++;
            }

            int delta = width - cur_width;
            int dx = 0;

            if (delta == 0) continue;

            int last_x = x_axis ? _x_ + lines.get(i).getX() : _y_ + lines.get(i).getY();
            for (Drawable d: lines.get(i).elements) {
                if (d instanceof Block) {
                    Block b = (Block) d;

                    dx = (int) Math.floor((double) delta / index);
                    int block_dx = dx;
                    if (delta > 0) {
                        block_dx = (int) Math.floor(delta * (double) b.flex_grow / weight_sum_grow);
                    } else {
                        block_dx = (int) Math.floor(delta * (double) b.flex_shrink / weight_sum_shrink);
                    }

                    if (x_axis) {
                        if (b.max_width > 0 && b.width + block_dx > b.max_width) {
                            block_dx = b.max_width - b.width;
                        } else if (b.width + block_dx < b.min_size) {
                            block_dx = b.min_size - b.width;
                        }

                        b._x_ = last_x + (!inversed ? b.margins[3] : b.margins[1]);
                        b.width += block_dx;
                        b.viewport_width += block_dx;
                    } else {
                        if (b.max_height > 0 && b.height + block_dx > b.max_height) {
                            block_dx = b.max_height - b.height;
                        }

                        b._y_ = last_x + (!inversed ? b.margins[0] : b.margins[2]);
                        b.height += block_dx;
                        b.viewport_height += block_dx;
                    }

                    delta -= block_dx;
                    lines.get(i).cur_pos += block_dx;
                    weight_sum_grow -= b.flex_grow;
                    weight_sum_shrink -= b.flex_shrink;

                    if (x_axis) {
                        if (b == last_block && block_dx != 0 && b._x_ + b.width > _x_ + lines.get(i).getX() + lines.get(i).getWidth()) {
                            int sub = Math.max(b.width - b.min_size, b._x_ + b.width - (_x_ + xmax - paddings[3]));
                            b.width -= sub;
                            b.viewport_width -= sub;
                        }
                        b.orig_width = (int) Math.floor((double) b.width / b.ratio);
                    } else {
                        if (b == last_block && block_dx != 0 && b._y_ + b.height > _y_ + lines.get(i).getY() + lines.get(i).getWidth()) {
                            int sub = b._y_ + b.height - (_y_ + ymax - paddings[0]);
                            b.height -= sub;
                            b.viewport_height -= sub;
                        }
                        b.orig_height = (int) Math.floor((double) b.height / b.ratio);
                    }
                    b.performLayout();

                    updateMaxContentSize(b);

                    if (x_axis) {
                        if (b._x_ - _x_ + b.width > content_x_max) {
                            content_x_max = b._x_ - _x_ + b.width;
                        }
                        last_x = b._x_ + b.width + b.margins[1] + flex_gap;
                    } else {
                        if (b._y_ - _y_ + b.height > content_y_max) {
                            content_y_max = b._y_ - _y_ + b.height;
                        }
                        last_x = b._y_ + b.height + b.margins[2] + flex_gap;
                    }
                } else {
                    if (x_axis) {
                        d.setX(last_x);
                    } else {
                        d.setY(last_x);
                    }
                }
            }
        }

        processFlexCrossAxis(x_axis, delta_y);

        if (x_axis) {
            content_y_max = lines.lastElement().top + lines.lastElement().height;
        } else {
            content_x_max = lines.lastElement().top + lines.lastElement().height;
        }
    }

    public void processFlexCrossAxis(boolean x_axis, int delta_y) {

        int xmax = width - borderWidth[1] - paddings[1] - borderWidth[3];
        int ymax = height - borderWidth[2] - paddings[2] - borderWidth[0];

        if (scrollbar_x != null) {
            ymax -= scrollbar_x.getPreferredSize().height;
        }
        if (scrollbar_y != null) {
            xmax -= scrollbar_y.getPreferredSize().width;
        }

        // Process cross-axis
        if (delta_y != 0) {
            int dy = (int) Math.floor((double) delta_y / lines.size());

            if (flex_align_content == FlexAlign.STRETCH) {
                for (int i = 0; i < lines.size(); i++) {
                    int h = lines.get(i).height + dy;
                    if (x_axis) {
                        lines.get(i).setY(lines.get(i).getY() + dy * i);
                    } else {
                        lines.get(i).setX(lines.get(i).getX() + dy * i);
                    }
                    if (x_axis && lines.get(i).top + h > _y_ + ymax) {
                        h -= lines.get(i).top + h - (_y_ + ymax);
                    }
                    if (!x_axis && lines.get(i).left + h > _x_ + xmax) {
                        h -= lines.get(i).left + h - (_x_ + xmax);
                    }
                    lines.get(i).setHeight(h);
                    for (Drawable d: lines.get(i).elements) {
                        if (x_axis) {
                            d.setY(lines.get(i).top);
                            if (flex_align_items == FlexAlign.STRETCH && d instanceof Block && ((Block)d).auto_height) {
                                Block b = (Block) d;
                                b.height = b.viewport_height = lines.get(i).height;
                                b.orig_height = (int) Math.floor((double) b.height / b.ratio);
                            } else if (flex_align_items == FlexAlign.FLEX_CENTER) {
                                d.setY(lines.get(i).top + (lines.get(i).height - d._getHeight()) / 2);
                            } else if (flex_align_items == FlexAlign.FLEX_END) {
                                d.setY(lines.get(i).top + lines.get(i).height - d._getHeight());
                            }
                        } else {
                            d.setX(lines.get(i).left);
                            if (flex_align_items == FlexAlign.STRETCH && d instanceof Block && ((Block)d).auto_width) {
                                Block b = (Block) d;
                                b.width = b.viewport_width = lines.get(i).height;
                                b.orig_width = (int) Math.floor((double) b.width / b.ratio);
                            } else if (flex_align_items == FlexAlign.FLEX_CENTER) {
                                d.setX(lines.get(i).left + (lines.get(i).height - d._getWidth()) / 2);
                            } else if (flex_align_items == FlexAlign.FLEX_END) {
                                d.setX(lines.get(i).left + lines.get(i).height - d._getWidth());
                            }
                        }
                    }
                }
            }
            else if (flex_align_content == FlexAlign.FLEX_CENTER) {
                for (int i = 0; i < lines.size(); i++) {
                    if (x_axis) {
                        lines.get(i).setY(lines.get(i).getY() + delta_y / 2);
                    } else {
                        lines.get(i).setX(lines.get(i).getX() + delta_y / 2);
                    }
                }
            }
            else if (flex_align_content == FlexAlign.FLEX_END) {
                for (int i = 0; i < lines.size(); i++) {
                    if (x_axis) {
                        lines.get(i).setY(lines.get(i).getY() + delta_y);
                    } else {
                        lines.get(i).setX(lines.get(i).getX() + delta_y);
                    }
                }
            }
        }
    }

    public void updateMaxContentSize(Block el) {
        int new_value = el.content_x_max;
        if (el.positioning != Position.ABSOLUTE && positioning != Position.FIXED) {
            new_value += el.margins[1] + paddings[1];
        }
        int w = Math.max(el.viewport_width - el.borderWidth[1] - el.borderWidth[3], new_value);
        if (el.getOffsetLeft() - borderWidth[3] + w > content_x_max) {
            if (el.float_type == FloatType.NONE && el.margins[1] < 0) {
                el.margins[1] = 0;
                el.margins[3] = 0;
            }
            content_x_max = el.getOffsetLeft() - borderWidth[3] + w;
        }
        int h = el.viewport_height - borderWidth[0] - borderWidth[2];
        if (el.positioning != Position.ABSOLUTE && positioning != Position.FIXED && el.parent != null && el.height > 0) {
            h += el.margins[2] + paddings[2];
        }
        if (el.getOffsetTop() - borderWidth[0] + h > content_y_max) {
            if (el.float_type == FloatType.NONE && el.auto_y_margin && el.margins[0] < 0) {
                el.margins[0] = 0;
                el.margins[2] = 0;
            }
            content_y_max = el.getOffsetTop() - borderWidth[0] + h;
        }
    }

    public void processFloatBlocks(Vector<Block> floats, boolean no_rec) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).type == NodeTypes.ELEMENT && children.get(i).float_type != FloatType.NONE) {
                floats.add(children.get(i));
            }
        }

        for (int i = 0; i < floats.size(); i++) {
            Block el = floats.get(i);
            if (el.viewport_width == 0) el.viewport_width = el.width;
            if (el.viewport_height == 0) el.viewport_height = el.height;

            if (no_rec) el.no_layout = true;
            layouter.last_element = i;
            layouter.addBlock(el);
            layouter.last_element = -1;
            el.no_layout = false;

            for (int j = 0; j < el.parts.size(); j++) {
                el.parts.get(j).sortBlocks();
            }

            updateMaxContentSize(el);

            if (content_y_max > height - borderWidth[0] - borderWidth[2] && !auto_height && overflow == Overflow.SCROLL) {
                boolean had_scroll = scrollbar_y != null;
                addScrollbarY();
                if (!had_scroll) {
                    performLayout(no_rec, true);
                    return;
                }
            } else {
                if (scrollbar_y != null) {
                    document.panel.remove(scrollbar_y);
                    scrollbar_y = null;
                    scroll_y = 0;
                    scroll_top = 0;
                    viewport_height = height;
                }
            }
        }
    }
    
    public void processListElements(Vector<Block> blocks) {
        int list_index = 0;
        list_max_offset = 0;

        for (int i = 0; i < blocks.size(); i++) {
            Block el = blocks.get(i);
            if (el.type == NodeTypes.ELEMENT && el.list_item_type >= 10 && el.list_item_type < 16) {
                list_index++;
                String str = (el.list_item_type < 12 ? (el.list_item_type == 11 && list_index < 10 ? "0" + list_index : list_index) : (el.list_item_type < 14 ? el.as_alpha(list_index) : el.as_roman(list_index))) + ".";
                if (list_item_type == 13 || list_item_type == 15) {
                    str = str.toLowerCase();
                }

                int style = (el.text_bold || el.text_italic) ? ((el.text_bold ? Font.BOLD : 0) | (el.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font font = new Font(fontFamily, style, el.fontSize);

                int offset = el.getFontMetrics(font).stringWidth(str);
                offset += Math.round(getFontMetrics(font).getHeight() / 2);
                //offset += Math.round(parent.getFontMetrics(font).getHeight() / 1.8);
                if (offset > list_max_offset) {
                    list_max_offset = offset;
                }
            }
        }
    }

    public void performSizeCheck() {
        if (content_x_max > viewport_width - borderWidth[1] - borderWidth[3]) {
            addScrollbarX();
        }
        else if (content_x_max <= viewport_width - borderWidth[1] - borderWidth[3] && scrollbar_x != null) {
            removeScrollbarX();
        }
        updateScrollbarY();
    }

    private void removeScrollbarX() {
        document.panel.remove(scrollbar_x);
        scrollbar_x = null;
        viewport_height = height;
        scroll_x = 0;
        scroll_left = 0;
        if (this == document.root && document.keep_root_scrollbars_outside) {
            document.root.setBounds(0, 0, document.width - document.borderSize * 2, document.height - document.borderSize * 2);
        }
    }

    private void removeScrollbarY() {
        if (scrollbar_y != null) {
            document.panel.remove(scrollbar_y);
            scrollbar_y = null;
            viewport_width = width;
            scroll_y = 0;
            scroll_top = 0;
            if (scrollbar_x != null) {
                scrollbar_x.setBounds(_x_ + borderWidth[3], _y_ + borderWidth[0] + viewport_height, width - borderWidth[3] - borderWidth[1], scrollbar_x.getPreferredSize().height);
            }
        }
    }

    private void updateScrollbarY() {
        if (content_y_max > viewport_height - borderWidth[0] - borderWidth[2] && !auto_height && overflow == Overflow.SCROLL) {
            boolean had_scroll = scrollbar_y != null;
            addScrollbarY();
            if (!had_scroll) performLayout(false, true);
        } else if (content_y_max > viewport_height - borderWidth[0] - borderWidth[2] && auto_height) {
            boolean had_scroll = scrollbar_y != null;
            if (this == document.root && document.keep_root_scrollbars_outside && had_scroll) {
                viewport_width += scrollbar_x.getPreferredSize().height;
                width += scrollbar_x.getPreferredSize().height;
            }
            removeScrollbarY();
            if (had_scroll) performLayout(false, true);
        } else if (content_y_max <= viewport_height - borderWidth[0] - borderWidth[2]) {
            boolean had_scroll = scrollbar_y != null;
            removeScrollbarY();
            if (had_scroll) {
                performLayout(false, true);
            }
        }
    }

    public void clipScrollbars() {
        int size = 40;
        for (int i = 0; i < layer_list.size(); i++) {
            Block b = layer_list.get(i);
            if (b.type == NodeTypes.ELEMENT && b.overflow == Overflow.SCROLL) {
                if (b.scrollbar_x == null && b.scrollbar_y == null) continue;
                Shape rect_sbx = new RoundedRect(b._x_, b._y_ + b.height - size, b.width, size, 0, 0, b.arc[2], b.arc[3]);
                Area ax = new Area(rect_sbx);
                Shape rect_sby = new RoundedRect(b._x_ + b.width - size, b._y_, size, b.height, 0, b.arc[1], b.arc[2], 0);
                Area ay = new Area(rect_sby);
                for (int j = i+1; j < layer_list.size(); j++) {
                    Block b1 = layer_list.get(j);
                    if (b1.type == NodeTypes.ELEMENT) {
                        Shape rect1 = new RoundedRect(b1._x_, b1._y_, b1.width, b1.height, b1.arc[0], b1.arc[1], b1.arc[2], b1.arc[3]);
                        ax.subtract(new Area(rect1));
                        ay.subtract(new Area(rect1));
                    }
                }

                AffineTransform at;
                at = AffineTransform.getTranslateInstance(0, -(b._y_ + (b.height-size)));
                ax.transform(at);
                at = AffineTransform.getTranslateInstance(-(b._x_ + (b.width-size)), 0);
                ay.transform(at);

                if (b.scrollbar_x != null) ((ClippedScrollBar)b.scrollbar_x).setClip(ax);
                if (b.scrollbar_y != null) ((ClippedScrollBar)b.scrollbar_y).setClip(ay);
            }
        }
    }

    public void findPreferredSizes() {
        int max = lines.size() > 0 ? lines.firstElement().width : 0;
        int last = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0 && (lines.get(i).elements.firstElement() instanceof Character ||
                lines.get(i).elements.firstElement() instanceof Block && ((Block)lines.get(i).elements.firstElement()).display_type != Display.BLOCK &&
                lines.get(i-1).elements.lastElement() instanceof Block && ((Block)lines.get(i-1).elements.lastElement()).display_type != Display.BLOCK)) {
                if (last == i-1) {
                    max += lines.get(i).cur_pos;
                } else {
                    max = lines.get(i-1).cur_pos + lines.get(i).cur_pos;
                    last = i-1;
                }
            }
            if (lines.get(i).elements.firstElement() instanceof Character ||
                    lines.get(i).elements.firstElement() instanceof Block && ((Block)lines.get(i).elements.firstElement()).display_type != Display.BLOCK) {
                max = findMaxSizeInLine(lines.get(i));
                if (max > min_size) {
                    min_size = max;
                }
            } else {
                Block b = (Block)lines.get(i).elements.firstElement();
                if (b.auto_x_margin) {
                    if (b.width > min_size) {
                        min_size = b.width;
                    }
                } else {
                    if (b.width + b.margins[3] + b.margins[1] > min_size) {
                        min_size = b.width + b.margins[3] + b.margins[1];
                    }
                }
            }
        }
        pref_size = max + paddings[3] + paddings[1] + borderWidth[3] + borderWidth[1];
        min_size = min_size + paddings[3] + paddings[1] + borderWidth[3] + borderWidth[1];
    }

    private int findMaxSizeInLine(Line line) {
        int max = 0;
        int w = 0;
        for (int i = 0; i < line.elements.size(); i++) {
            if (line.elements.get(i) instanceof Character) {
                w += ((Character)line.elements.get(i)).width;
                if ((white_space == WhiteSpace.WORD_BREAK || ((Character)line.elements.get(i)).getText().matches("\\s+")) &&
                        w > max) {
                    max = w;
                    w = 0;
                }
            } else {
                Block b = (Block)line.elements.get(i);
                if (b.width + b.margins[3] + b.margins[1] > max) {
                    max = b.width + b.margins[3] + b.margins[1];
                }
            }
        }
        return max;
    }

    public void sortBlocks() {
        v.clear();
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).elements.size(); j++) {
                Drawable d = lines.get(i).elements.get(j);
                for (int k = 0; k < children.size(); k++) {
                    Block b = children.get(k);
                    if ((b.positioning == Position.ABSOLUTE || positioning == Position.FIXED ||
                        b.float_type != FloatType.NONE || b.display_type == Display.TABLE_CELL) &&
                        b._getX() <= d._getX() &&
                        b._getY() >= lines.get(i).getY() &&
                        b._getY() <= lines.get(i).getY() + lines.get(i).getHeight()) {
                        v.add(b);
                    }
                }
                if (!(d instanceof Block) || ((Block)d).parts.size() == 0) {
                    v.add(d);
                } else {
                    for (Block part: parts) {
                        v.add(part);
                    }
                }
            }
        }

        Vector<Block> bv = new Vector<Block>();
        for (int i = 0; i < children.size(); i++) {
            if (!v.contains(children.get(i)) && children.get(i).type == 0 && children.get(i).parts.size() == 0) {
                bv.add(children.get(i));
            }
        }

        if (bv.size() > 0) {
            Block[] blocks = new Block[bv.size()];
            bv.toArray(blocks);
            Arrays.sort(blocks, new BlockSort());

            for (int i = 0; i < blocks.length; i++) {
                Block b = blocks[i];
                if (b.visibility == Visibility.HIDDEN || b.display_type == Display.NONE) continue;
                v.add(b);
            }
        }
    }

    public Vector<Block> findBlocksByName(Block b, String name) {
        Vector<Block> result = new Vector<Block>();

        if (b.node.getAttribute("name").equals(name)) {
            result.add(b);
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).type == NodeTypes.ELEMENT) {
                result.addAll(findBlocksByName(children.get(i), name));
            }
        }

        return result;
    }

    class BlockSort implements Comparator<Block> {

        @Override
        public int compare(Block a, Block b) {
            if (a._y_ != b._y_) return a._y_ - b._y_;
            return a._x_ - b._x_;
        }
    }

    public void renderContent(Graphics g) {

        last_list_index = 0;

        boolean paintShadow = textShadowColor != null && text_shadow_buffer == null;

        if (text_layer == null || list_item_type > 0 || textRenderingMode == 1 || paintShadow) {
            renderText(g);
        } else {
            if (needToRestoreSelection) restoreSelection(g);
            text_layer.repaint();
        }
        for (int i = 0; i < lines.size(); i++) {
            Vector<Drawable> v = lines.get(i).elements;
            for (int j = 0; j < v.size(); j++) {
                if (v.get(j) instanceof Block) {
                    ((Block)v.get(j)).forceRepaint();
                }
            }
        }
        for (int i = 0; i < children.size(); i++) {
            Drawable d = children.get(i);
            if (d instanceof Block) {
                Block b = (Block)d;
                if (b.positioning == Block.Position.ABSOLUTE || b.positioning == Position.FIXED ||
                      b.float_type != Block.FloatType.NONE || b.display_type == Display.TABLE_ROW || b.display_type == Display.TABLE_CELL) {
                    b.forceRepaint();
                }
            }
        }
        document.validate();
    }

    private LinkedList<Block> getZIndexList() {
        if (this == document.root) this.zIndexAuto = false;
        LinkedList<Block> list = getSubtree(this);
        if (this == document.root) list.removeFirst();

        LinkedList<Block> l1 = new LinkedList<Block>();
        LinkedList<Block> l2 = new LinkedList<Block>();
        LinkedList<Block> l3 = new LinkedList<Block>();

        sortList(list, l1, l2, l3);

        if (list.size() > 0) list = reorderList(list);

        if (l1.size() == 0 && l2.size() == 0 && l3.size() == 0) {
            return list;
        }

        Block[] a1 = new Block[l1.size()];
        Block[] a2 = new Block[l2.size()];
        Block[] a3 = new Block[l3.size()];

        a1 = (Block[])l1.toArray(a1);
        a2 = (Block[])l2.toArray(a2);
        a3 = (Block[])l3.toArray(a3);
        Arrays.sort(a1, new BlockSort2());
        Arrays.sort(a2, new BlockSort2());
        Arrays.sort(a3, new BlockSort2());

        for (int i = 0; i < a1.length; i++) {
            list.addAll(a1[i].getZIndexList());
        }
        for (int i = 0; i < a2.length; i++) {
            if (a2[i].zIndexAuto) {
                list.addAll(a2[i].reorderList(getSubtree(a2[i])));
            } else {
                list.addAll(a2[i].getZIndexList());
            }
        }
        for (int i = 0; i < a3.length; i++) {
            list.addAll(a3[i].getZIndexList());
        }

        return list;
    }

    private void sortList(List list, List l1, List l2, List l3) {
        Block b;
        int pos = -1;
        Iterator it = list.listIterator();
        if (this != document.root) {
            it.next();
            pos++;
        }

        while (it.hasNext()) {
            b = (Block)(it.next());
            pos++;
            if (b.zIndex < 0 && !b.zIndexAuto && b.positioning != Position.STATIC) {
                l1.add(b);
            }
            if ((b.zIndex == 0 && !b.zIndexAuto || b.zIndexAuto) && b.positioning != Position.STATIC) {
                l2.add(b);
            }
            if (b.zIndex > 0 && !b.zIndexAuto && b.positioning != Position.STATIC) {
                l3.add(b);
            }
        }

        list.removeAll(l1);
        list.removeAll(l2);
        list.removeAll(l3);
    }

    public void setZIndices() {
        LinkedList<Block> list = getZIndexList();
        int offset = (this == document.root) ? list.size()-1 : (getParent() != null ? getParent().getComponentZOrder(this) : pos);
        for (int i = list.size()-1; i >= 0; i--) {
            Block b = list.get(i);
            java.awt.Container c = b.getParent();
            if (c == null && b.parts.size() > 0) {
                c = b.parts.get(0).getParent();
                list.remove(i);
                for (int j = 0; j < b.parts.size(); j++) {
                    Block block = b.parts.get(j);
                    list.add(i+j, block);
                    try {
                        c.setComponentZOrder(block, offset-i+j);
                    } catch (Exception ex) {}
                }
            }
            else {
                try {
                    c.setComponentZOrder(b, offset-i);
                } catch (Exception ex) {}
            }
        }
        layer_list = list;
    }

    private LinkedList<Block> reorderList(LinkedList<Block> list) {
        LinkedList<Block> list2 = new LinkedList<Block>();
        if (this != document.root) {
            list2.add(list.remove());
        }
        Iterator it = list.listIterator();
        if (!it.hasNext()) return list2;
        while (it.hasNext()) {
            Block b = (Block)it.next();
            if (b.display_type == Display.NONE) {
                list2.add(b);
            }
        }
        list.removeAll(list2);

        it = list.listIterator();
        while (it.hasNext()) {
            Block b = (Block)it.next();
            if (b.float_type == FloatType.NONE && b.positioning == Position.STATIC &&
                    (b.display_type == Display.BLOCK || b.display_type == Display.TABLE || b.display_type == Display.TABLE_ROW || b.display_type == Display.TABLE_CELL)) {
                list2.add(b);
            }
        }
        list.removeAll(list2);
        
        it = list.listIterator();
        while (it.hasNext()) {
            Block b = (Block)it.next();
            if (b.float_type != FloatType.NONE) {
                List l = b.reorderList(getSubtree(b));
                list2.addAll(l);
            }
        }
        list.removeAll(list2);
        
        it = list.listIterator();
        while (it.hasNext()) {
            Block b = (Block)it.next();
            if (!b.text_italic) {
                List l = b.reorderList(getSubtree(b));
                list2.addAll(l);
            }
        }
        list.removeAll(list2);
        
        it = list.listIterator();
        while (it.hasNext()) {
            Block b = (Block)it.next();
            List l = b.reorderList(getSubtree(b));
            list2.addAll(l);
        }
        return list2;
    }

    private static LinkedList<Block> getSubtree(Block block) {
        LinkedList<Block> list = new LinkedList<Block>();
        list.add(block);
        Block b = block;
        int pos = 0;
        LinkedList<Block> stack = new LinkedList<Block>();
        LinkedList<Integer> stack2 = new LinkedList<Integer>();
        if (b.children.size() == 0) return list;
        int psc = 0;
        if (block.beforePseudoElement != null) {
            b = block.beforePseudoElement;
            pos = -1;
        } else {
            b = b.children.get(0);
            pos = 0;
        }
        boolean z_auto = block.zIndexAuto || (block.parent != null && block.positioning == Position.STATIC);

        while (b != null) {
            /* Do not include descendants of floats and inline-blocks except elements
               generating their own contexts */

            boolean skip = z_auto && b.positioning != Position.STATIC && !b.zIndexAuto;
            if (b.type != NodeTypes.TEXT && !skip && (psc <= 0 || b.positioning != Position.STATIC && !b.zIndexAuto)) {
                list.add(b);
            }
            if (b.positioning == Position.STATIC || b.zIndexAuto) {
                /* Traverse everything except elements
                   generating their own contexts */
                
                boolean is_leaf = (b.children.size() == 1 && b.children.get(0).type == NodeTypes.TEXT);
                if (b.children.size() > 0 && !is_leaf) {
                    stack.add(b);
                    stack2.add(pos);
                    if (b.float_type != FloatType.NONE || b.display_type == Display.INLINE_BLOCK ||
                            b.display_type == Display.INLINE || (b.positioning != Position.STATIC && b.zIndexAuto)) {
                        psc++;
                    }
                    if (b.beforePseudoElement != null) {
                        b = b.beforePseudoElement;
                        pos = -1;
                    } else {
                        b = b.children.get(0);
                        pos = 0;
                    }
                } else {
                    if (b.parent.children.size() <= pos+1 && b != b.parent.afterPseudoElement && b.parent.afterPseudoElement != null) {
                        b = b.parent.afterPseudoElement;
                        continue;
                    }
                    while (b.parent.children.size() <= pos+1 && !stack.isEmpty()) {
                        pos = stack2.pollLast();
                        b = stack.pollLast();
                        if (b.float_type != FloatType.NONE || b.display_type == Display.INLINE_BLOCK ||
                                b.display_type == Display.INLINE || (b.positioning != Position.STATIC && b.zIndexAuto)) {
                            psc--;
                        }
                    }
                    if (b.parent.children.size() > pos+1) {
                        pos++;
                        b = b.parent.children.get(pos);
                    } else {
                        if (b != b.parent.afterPseudoElement && b.parent.afterPseudoElement != null) {
                            b = b.parent.afterPseudoElement;
                        } else {
                            b = null;
                        }
                    }
                }
                continue;
            } else {
                /*  Do not include descendants of blocks generating their own contexts */
                while (b.parent.children.size() <= pos+1 && !stack.isEmpty()) {
                    pos = stack2.pollLast();
                    b = stack.pollLast();
                }
            }
            if (b.parent.children.size() > pos+1) {
                if (b != b.parent.afterPseudoElement && b.parent.afterPseudoElement != null) {
                    b = b.parent.afterPseudoElement;
                } else {
                    b = null;
                }
            }
        }
        return list;
    }

    class BlockSort2 implements Comparator<Block> {

        @Override
        public int compare(Block a, Block b) {
            return a.zIndex - b.zIndex;
        }
    }

    public void setZIndex(String value) {
        if (value == null || value.equals("auto")) {
            zIndex = 0;
            zIndexAuto = true;
        } else {
            zIndex = Integer.parseInt(value);
            zIndexAuto = false;
        }
    }

    protected int last_list_index = 0;
    protected int list_max_offset = 0;

    protected String as_alpha(int n) {
        String result = "";
        int k = n;
        do {
            n--;
            k = n % 26;
            result = String.valueOf((char)('A' + k)) + result;
            n /= 26;
        } while (n > 0);
        return result;
    }

    protected String as_roman(int n) {
        String result = "";
        String[] d = { "I", "V", "X", "L", "C", "D", "M" };
        char[] s = ((new Integer(n)).toString()).toCharArray();
        for (int i = s.length-1; i >= 0; i--) {
            int p = s.length-1-i;
            if (s[p] <= '3') {
                for (int j = 0; j < s[p]-'0'; j++) {
                    result += d[i * 2];
                }
            }
            else if (s[p] == '4') {
                result += d[i * 2] + d[i * 2 + 1];
            }
            else if (s[p] >= '5' && s[p] <= 8) {
                result += d[i * 2 + 1];
                for (int j = 0; j < s[p]-'5'; j++) {
                    result += d[i * 2];
                }
            }
            else if (s[p] == '9') {
                result += d[i * 2] + d[(i+1) * 2];
            }
        }
        return result;
    }

    public void renderText(Graphics g) {

        if (sel == null) {
            sel = new int[2];
            sel[0] = -1;
            sel[1] = -1;
        }

        if (isImage || isMedia || node != null && node.tagName.equals("svg")) return;

        boolean paintShadow = false;

        if (textShadowColor != null && text_shadow_buffer == null && children.get(0).type == NodeTypes.TEXT && textRenderingMode == 0) {
            text_shadow_buffer = new BufferedImage(document.root.width, document.root.height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).text_shadow_buffer = text_shadow_buffer;
            }
            paintShadow = true;
        }

        boolean paintChars = text_layer == null || textRenderingMode == 1;

        prepareTextLayers();

        boolean render_chars = paintChars || paintShadow;
        
        if (list_item_type > 0) {
            int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            Font font = new Font("Tahoma", style, fontSize);

            if (list_item_type < 10) {
                Character c = new Character(null, lm[list_item_type]);
                c.setFont(font);

                int offset = getFontMetrics(font).stringWidth(c.getText());
                c.width = offset;

                c.height = getFontMetrics(font).getHeight();
                offset += Math.round(getFontMetrics(font).getHeight() / 1.5);
                c.setX(0);
                c.setY(paddings[0]-1);

                if (list_item_type >= 2 && list_item_type <= 6) {
                    g.setColor(color);
                    if (list_item_type == 2) {
                        c.width *= 1.34;
                        int d = (int)Math.floor(c.width * 0.84);
                        BasicStroke pen = new BasicStroke(1.18f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                        ((Graphics2D)g).setStroke(pen);
                        g.drawOval(paddings[3] + (int)Math.floor(c.width/3), paddings[0] + (int)Math.floor(c.width/3)+d, (int)Math.floor(c.width/1.4), (int)Math.floor(c.width/1.4));
                    } else if (list_item_type == 3) {
                        g.fillRect(paddings[3] + (int)Math.floor(c.width/6), paddings[0] + (int)Math.floor(c.height/2 - c.width/3), c.width, c.width);
                    } else if (list_item_type == 4) {
                        g.drawRect(paddings[3] + (int)Math.floor(c.width/6), paddings[0] + (int)Math.floor(c.height/2 - c.width/3), c.width, c.width);
                    } else if (list_item_type == 5) {
                        int d = (int)Math.floor(c.width * 0.18);
                        int[] x = { paddings[3] + (int)Math.floor(c.width/6) - 2, paddings[3] + (int)Math.floor(c.width/2) - 3, paddings[3] + (int)Math.ceil(c.width*5/6) - 3, paddings[3] + (int)Math.floor(c.width/2) - 3 };
                        int[] y = { paddings[0] + (int)Math.floor(c.width/2) + d, paddings[0] + (int)Math.floor(c.width/6) + d, paddings[0] + (int)Math.floor(c.width/2) + d, paddings[0] + (int)Math.floor(c.width*5/6) + d };
                        g.fillPolygon(x, y, 4);
                    } else {
                        int d = (int)Math.floor(c.width * 0.48);
                        int[] x = { paddings[3] + (int)Math.floor(c.width/6) - 1, paddings[3] + (int)Math.floor(c.width/2) - 2, paddings[3] + (int)Math.ceil(c.width*5/6) - 2, paddings[3] + (int)Math.floor(c.width/2) - 2 };
                        int[] y = { paddings[0] + (int)Math.floor(c.width/2) + d, paddings[0] + (int)Math.floor(c.width/6) + d, paddings[0] + (int)Math.floor(c.width/2) + d, paddings[0] + (int)Math.floor(c.width*5/6) + d + 1 };
                        BasicStroke pen = new BasicStroke(1.18f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                        ((Graphics2D)g).setStroke(pen);
                        g.drawPolygon(x, y, 4);
                    }
                }
                else if (render_chars) processTextChar(g, c, paintShadow, paintChars);
            }

            else if (list_item_type >= 10 && list_item_type < 16) {
                Block b = parent;
                while (b != null && b.type != NodeTypes.ELEMENT) {
                    b = b.parent;
                }
                if (b != null) {
                    b.last_list_index++;
                    int index = b.last_list_index;

                    int n = 0;
                    for (int i = 0; i < b.children.size(); i++) {
                        int t = b.children.get(i).list_item_type;
                        if (t >= 10 && t < 16) {
                            n++;
                        }
                        if (b.children.get(i) == this) break;
                    }

                    String str = (list_item_type < 12 ? (list_item_type == 11 && n < 10 ? "0" + n : n) : (list_item_type < 14 ? as_alpha(index) : as_roman(index))) + ".";
                    if (list_item_type == 13 || list_item_type == 15) {
                        str = str.toLowerCase();
                    }

                    Character c = new Character(null, str);
                    c.setFont(font);


                    int offset = getFontMetrics(font).stringWidth(str);
                    offset += Math.round(getFontMetrics(font).getHeight() / 2);
                    c.setX(parent.list_max_offset - offset);
                    c.setY(paddings[0]);
                    if (render_chars) processTextChar(g, c, paintShadow, paintChars);
                }
            }
        }
        if (render_chars) {
            for (int i = 0; i < lines.size(); i++) {
                for (int j = 0; j < lines.get(i).elements.size(); j++) {
                    if (lines.get(i).elements.get(j) instanceof Character) {
                        Character c = (Character)lines.get(i).elements.get(j);
                        processTextChar(g, c, paintShadow, paintChars);
                    }
                }
            }
            if (needToRestoreSelection) {
                restoreSelection(g);
            }
        }
        if (paintShadow && textShadowBlur > 0) {
            blurTextShadow();
        }
        //invalidate();
        document.repaint();
    }

    private void prepareTextLayers() {
        if (textRenderingMode == 1) return;

        if (text_shadow_layer == null) {
            text_shadow_layer = new JPanel() {
                @Override
                public void paintComponent(Graphics g) {
                    if (getParent() != null && getParent() instanceof Block) {
                        if (((Block)getParent()).text_shadow_buffer != null) {
                            g.drawImage(((Block)getParent()).text_shadow_buffer, 0, 0, null);
                        }
                    }
                }
            };
            text_shadow_layer.setLayout(null);
            add(text_shadow_layer);
            text_shadow_layer.setBounds(0, 0, document.root.width, document.root.height);
            text_shadow_layer.setOpaque(false);
            setComponentZOrder(text_shadow_layer, getComponents().length-1);
        }

        Block b = this;
        while (b.parent != null && !b.transform) {
            b = b.parent;
        }

        if (text_layer == null) {
            text_layer = new JPanel();
            text_layer.setLayout(null);
            add(text_layer);
            text_layer.setBounds(0, 0, b.getWidth(), b.getHeight());
            text_layer.setOpaque(false);
            setComponentZOrder(text_layer, 0);
        }
    }

    public void blurTextShadow() {
        //long start = System.nanoTime();
        java.awt.image.BufferedImageOp op = null;
        int blur = 3;

        double sqr = blur * blur;
        float[] data = new float[blur * blur];
        float q = (float) (blur == 3 ? 1.23 : 1.15);
        float q2 = (float) (1f / sqr * (textShadowBlur == 3 ? 0.045 : (0.085 - (0.05 * textShadowBlur))));
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) (1f / sqr * (q - (0.003 * blur) + 0.01 * (double)fontSize / 20));
        }
        data[data.length / 2] = q2;
        Kernel kernel = new Kernel(blur, blur, data);
        op = new java.awt.image.ConvolveOp(kernel);

        for (int i = 0; i < textShadowBlur; i++) {
            text_shadow_buffer = op.filter(text_shadow_buffer, null);
        }
        //long end = System.nanoTime();
        //System.out.println("Text blur finished in " + ((double) (end - start)) / 1000000  + "ms");
    }

    public void setNeedRestoreSelection(boolean value) {
        needToRestoreSelection = value;
        for (Block part: parts) {
            part.needToRestoreSelection = value;
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setNeedRestoreSelection(value);
        }
    }

    public boolean needToRestoreSelection = false;

    private void processTextChar(Graphics g, Character c, boolean paintShadow, boolean paintText) {
        
        if (paintShadow) {
            AffineTransform t0 = ((Graphics2D)g).getTransform();
            //AffineTransform t = ((AffineTransform)t0.clone());
            //t.concatenate(AffineTransform.getTranslateInstance(textShadowOffset[0] * ratio, textShadowOffset[1] * ratio));
            //((Graphics2D)g).setTransform(t);
            g.setColor(textShadowColor);
            Color curCol = this.color;
            c.setColor(textShadowColor);
            //int delta = Math.min(8, textShadowBlur) + 4;
            if (textShadowBlur < 0) {
                c.setColor(new Color((int)(textShadowColor.getRed() * 0.95), (int)(textShadowColor.getGreen() * 0.95), (int)(textShadowColor.getBlue() * 0.95), (int)Math.min(255, textShadowColor.getAlpha() * 1.1)));
                //c.draw(g);
                BufferedImage bufferedImage = text_shadow_buffer;
                Graphics2D g2d = (Graphics2D)bufferedImage.getGraphics();
                //g2d.setTransform(AffineTransform.getScaleInstance(1, 1.003));

                int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font font = new Font(fontFamily, style, fontSize);
                FontMetrics fm = getFontMetrics(font);
                g2d.setFont(font);
                g2d.setColor(textShadowColor);
                g2d.drawString(c.getText(), (int) (_x_ + c.getX() + textShadowOffset[0]), (int) (_y_ + c.getY() + fm.getHeight() - (int) (fontSize * 0.24)));
                //g2d.drawString(c.getText(), (int) (-delta + c.getX()), (int) (-delta + c.getY() + fm.getHeight() - 6));
            } else {

                int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font font = new Font(fontFamily, style, fontSize);
                FontMetrics fm = getFontMetrics(font);
                
                //c.draw(bufferedImage.getGraphics());
                //int blur = 5;

                Graphics2D g2d = textRenderingMode == 0 ? (Graphics2D)text_shadow_buffer.getGraphics() : (Graphics2D)g;
                //Graphics2D g2d = (Graphics2D)bufferedImage.getGraphics();
                AffineTransform t2 = AffineTransform.getScaleInstance(1, 1 + (textShadowBlur <= 0 || fontSize < 28 ? 0.006 * Math.max(1, textShadowBlur-3) : -0.001 * fontSize / 2));
                t2.concatenate(AffineTransform.getTranslateInstance(0, textShadowBlur <= 0 ? 0.64 * fontSize / 6 : 0.28 * (fontSize + textShadowBlur) / 8));
                g2d.setTransform(t2);
                g2d.setFont(font);

                //g2d.setColor(new Color((int)(textShadowColor.getRed() * 0.95), (int)(textShadowColor.getGreen() * 0.95), (int)(textShadowColor.getBlue() * 0.95), (int)Math.min(255, textShadowColor.getAlpha() * 1.3)));
                
                g2d.setColor(textShadowColor);
                
                //g2d.drawString(c.getText(), blur, blur);
                int dy = textShadowBlur < 5 ? (int) ((fontSize + textShadowBlur) * 0.23) : (fontSize < 28 ? (int) ((fontSize + textShadowBlur) * 0.24) : (int) ((fontSize + textShadowBlur) * 0.15));
                g2d.drawString(c.getText(), (int) (_x_ + c.getX() + textShadowOffset[0]), (int) (_y_ + c.getY() + fm.getHeight() - dy));
                //g2d.drawString(c.getText(), (int) (-delta + c.getX()), (int) (-delta + c.getY() + fm.getHeight() - 6));
                //double sx = fontSize < 28 ? 0.16 : (textShadowBlur > 2 ? 0.16 - (0.05 * (textShadowBlur-1)) : 0.07);
                //text_shadow_buffer.getGraphics().drawImage(img, (int) (_x_ + c.getX() + textShadowOffset[0] - blur), (int) (_y_ + c.getY() + fm.getHeight() - (int) (fontSize * 0.24) - blur), null);
                //g.drawImage(bufferedImage, -_x_, -_y_, null);
                //g.drawImage(bufferedImage, (int)(c.getX() - sx * w), (int)(c.getY() - 0.03 * fm.getHeight()), null);
            }
            c.setColor(curCol);
            ((Graphics2D)g).setTransform(t0);
            if (textRenderingMode == 0 && !paintText) {
                return;
            }
        }
        if (textRenderingMode > 0) {
            if (paintText) {
                c.draw(g);
            }
            return;
        }

        int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font font = new Font(fontFamily, style, fontSize);

        Block b = this;
        while (b.parent != null && !b.transform) {
            b = b.parent;
        }
        final Block instance = this;
        final Block origin_block = b;

        JLabel label = new JLabel(c.getText());

        Color col = hasParentLink || href != null ? linkColor : color;
        label.setForeground(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)Math.round(col.getAlpha() * alpha)));

        boolean underline = text_underline || (hasParentLink || href != null) && linksUnderlineMode == 0;
        boolean strikethrough = text_strikethrough;

        if (underline || strikethrough) {
            Map attributes = font.getAttributes();
            if (underline) attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            if (strikethrough) attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            label.setFont(font.deriveFont(attributes));
        } else {
            label.setFont(font);
        }

        boolean isTransformed = false;

        while (b.parent != null) {
            if (b.transform) {
                isTransformed = true;
            }
            b = b.parent;
        }

        if (isTransformed) {
            int sx = _x_ + c.getX() - (origin_block._x_ + origin_block.width / 2);
            int sy = _y_ + c.getY() - (origin_block._y_ + origin_block.height / 2);
            ((Graphics2D)g).setTransform(AffineTransform.getRotateInstance(Math.PI / 4, -sx, -sy));
            //setSize(new Dimension((int) (getWidth() * 1.3), (int) (getHeight() * 1.3)));
            //g.setClip(null);
            c.draw(g);
            return;
        }

        text_layer.add(label);
        c.glyph = label;

        boolean hidden = isPartlyHidden();
        boolean flag = false;
        FontMetrics fm = getFontMetrics(new Font(fontFamily, style, fontSize));
        int delta1 = fm.getAscent() - fm.getHeight();
        int delta2 = -fm.getDescent();
        if (clipping_block != null && (_x_ + c.getX() + 1 - clipping_block.scroll_left < clipping_block._x_ + clipping_block.borderWidth[3] || _y_ + c.getY() + 1 - clipping_block.scroll_top < clipping_block._y_ + clipping_block.borderWidth[0] + delta1 ||
               _x_ + c.getX() - clipping_block.scroll_left + c.getWidth() > clipping_block._x_ + clipping_block.viewport_width - clipping_block.borderWidth[1] || _y_ + c.getY() - clipping_block.scroll_top + c.getHeight() + delta2 > clipping_block._y_ + clipping_block.viewport_height - clipping_block.borderWidth[2])) {
            flag = true;
        }
        label.setBounds(_x_ + c.getX() - scroll_x, _y_ + c.getY() - scroll_y, text_italic ? c.getWidth() + 2 : c.getWidth(), c.getHeight());

        if (isTransformed || (hidden && flag)) {
            if (clipping_block != null) {

                int sx = clipping_block.parent != null ? clipping_block.parent.scroll_x : 0;
                int sy = clipping_block.parent != null ? clipping_block.parent.scroll_y : 0;

                int xc = clipping_block._x_ - sx - _x_ + clipping_block.borderWidth[3] + parent.scroll_x;
                int yc = clipping_block._y_ - sy - _y_ + clipping_block.borderWidth[0] + parent.scroll_y;
                int wc = clipping_block.viewport_width - clipping_block.borderWidth[3] - clipping_block.borderWidth[1];
                int hc = clipping_block.viewport_height - clipping_block.borderWidth[0] - clipping_block.borderWidth[2];

                double[] arcs = new double[4];
                arcs[0] = clipping_block.arc[0] / 2 - 1;
                arcs[1] = clipping_block.arc[1] / 2 - 1;
                arcs[2] = clipping_block.arc[2] / 2 - 1;
                arcs[3] = clipping_block.arc[3] / 2 - 1;

                adjustCorners(arcs, clipping_block);

                RoundedRect rect = new RoundedRect(xc, yc, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);
                g.setClip(rect);
                clipping_block = null;
            }

            BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            label.paint(img.getGraphics());

            label.setVisible(false);
            g.drawImage(img, c.getX(), c.getY(), this);
        }
    }

    public void restoreSelection(Graphics g) {
        if (sel == null) return;
        int from_element = sel[0];
        int to_element = sel[1];
        if (from_element > to_element) {
            int a = from_element;
            from_element = to_element;
            to_element = a;
        }

        Color sel_color = WebDocument.active_document == document ? selection_color : selection_inactive_color;

        if (from_element > -1 && to_element > -1) {

            for (int k = 0; k < v.size(); k++) {
                if (!(v.get(k) instanceof Character)) continue;
                Character c = (Character)v.get(k);
                if (k >= from_element && k <= to_element) {
                    c.selected = true;
                }
                if (!c.selected) continue;
                JLabel label = c.glyph;

                if (label != null) {
                    label.setForeground(Color.WHITE);
                    if (!text_italic) {
                        label.setBackground(sel_color);
                        label.setOpaque(true);
                    } else {
                        g.setColor(sel_color);
                        g.fillRect(c.getX(), c.getY(), label.getWidth()-1, label.getHeight());
                    }
                } else {
                    selectCharacter(c);
                    c.draw(g);
                }
            }
            document.repaint();
        }
    }

    public boolean isPartlyHidden() {

        int dx = getOffsetLeft();
        int dy = getOffsetTop();
        if (positioning == Position.FIXED) {
            return dx < 0 || dy < 0 || dx + width > document.root.viewport_width && dy + height > document.root.viewport_height;
        }
        Block b = parent;
        while (b != null) {
            if ((dx - b.scroll_left < b.borderWidth[3] || dy - b.scroll_top < b.borderWidth[0] ||
                dx - b.scroll_left + viewport_width > b.viewport_width - b.borderWidth[1] ||
                dy - b.scroll_top + viewport_height > b.viewport_height - b.borderWidth[2]) &&
                b.overflow != Overflow.VISIBLE) {
                  clipping_block = b;
                  return true;
            }
            dx += b.getOffsetLeft() - b.scroll_left;
            dy += b.getOffsetTop() - b.scroll_top;
            b = b.parent;
        }
        clipping_block = null;
        return false;
    }

    public Block clipping_block = null;

    public String replaceEntities(String str) {
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&nbsp;", "\u0A00");
        return str;
    }

    public void setTransform(boolean value) {
        transform = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).transform = value;
        }
        buffer = null;
        forceRepaint();
    }

    public int getTransformedSize() {
        int w = 0;
        if (width > height) {
            double t = (double)height / width;
            w = (int)Math.round(width / Math.sqrt(1 / (t * t + 1)));
        } else if (width < height) {
            double t = (double)width / height;
            w = (int)Math.round(height / Math.sqrt(1 / (t * t + 1)));
        } else {
            w = (int)Math.round(width * Math.sqrt(2));
        }
        return w;
    }

    public Color parseColor(String value) {
        if (value.matches("rgb\\([0-9]+, [0-9]+, [0-9]+\\)")) {
            String[] s = value.substring(4, value.length()-1).split("\\s*,\\s*");
            return new Color(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
        } else if (value.matches("rgba\\([0-9]+, [0-9]+, [0-9]+, [0-9.]+\\)")) {
            String[] s = value.substring(5, value.length()-1).split("\\s*,\\s*");
            return new Color(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), (int)Math.round(255 * Double.parseDouble(s[3])));
        }
        if (!value.matches("#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}")) {
            if (value.matches("^[A-Za-z]+$")) {
                try {
                    Field f = Color.class.getDeclaredField(value);
                    if (f.getType() == Color.class) {
                        return (Color) f.get(Color.class);
                    }
                } catch (Exception ex) {}
            }
            return null;
        }
        if (value.matches("#[0-9a-fA-F]{3}")) {
            value = "#" + value.charAt(1) + value.charAt(1) +
                          value.charAt(2) + value.charAt(2) +
                          value.charAt(3) + value.charAt(3);
        }
        return Color.decode(value.toUpperCase());
    }

    public void updateTextLayer() {
        if (text_layer != null) {
            Component[] c = text_layer.getComponents();
            for (int i = 0; i < c.length; i++) {
                if (!(sel != null && i >= sel[0] && i <= sel[1]) && c[i] instanceof JLabel) {
                    ((JLabel)c[i]).setForeground(color);
                }
            }
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).color = this.color;
            parts.get(i).updateTextLayer();
        }
    }

    public void setTextColor(Color col) {
        color = col;
        if (this.textRenderingMode == 0) {
            updateTextLayer();
        } else {
            forceRepaint();
        }
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setTextColorRecursive(Color col) {
        color = col;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setTextColorRecursive(col);
        }
        if (this.textRenderingMode == 0) {
            updateTextLayer();
        } else {
            forceRepaint();
        }
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setBackgroundColor(Color col) {
        if (background == null && col == null) {
            return;
        }
        if (background == null) {
            background = new Background();
        }
        background.bgcolor = col;
        forceRepaint();
    }

    public void setBackgroundImage(String path) {
        if (background == null && (path == null || path.isEmpty())) {
            return;
        }
        if (background == null) {
            background = new Background();
        }
        if (path == null || path.isEmpty()) {
            background.bgImage = null;
            background.imgSrc = "";
            forceRepaint();
            return;
        }
        try {
            File f;
            if (path.equals(background.imgSrc)) return;
            background.imgSrc = path;
            if (path.startsWith("http")) {
                background.bgImage = ImageIO.read(new URL(path));
                String[] str = path.split("/");
                f = File.createTempFile("tmp_", str[str.length-1]);
                ImageIO.write(background.bgImage, "png", f);
            } else {
                f = new File(path);
                background.bgImage = ImageIO.read(f);
            }
            ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
            ir.setInput(ImageIO.createImageInputStream(f));
            if (ir.getNumImages(true) > 1) {
                if (has_animation) {
                    stopWatcher();
                    w = null;
                }
                readGIF(ir);
                has_animation = true;
                startWatcher();
            } else {
                stopWatcher();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (parts.size() == 1) {
            parts.get(0).setBackgroundImage(path);
        }
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setBackground(Background background) {
        this.background = background;
        if (background.imgSrc == null) return;
        try {
            File f;
            if (background.imgSrc.startsWith("http")) {
                background.bgImage = ImageIO.read(new URL(background.imgSrc));
                String[] str = background.imgSrc.split("/");
                f = File.createTempFile("tmp_", str[str.length-1]);
                ImageIO.write(background.bgImage, "png", f);
            } else {
                f = new File(background.imgSrc);
                background.bgImage = ImageIO.read(f);
            }
            ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
            ir.setInput(ImageIO.createImageInputStream(f));
            if (ir.getNumImages(true) > 1) {
                if (has_animation) {
                    stopWatcher();
                    w = null;
                }
                readGIF(ir);
                has_animation = true;
                startWatcher();
            } else {
                stopWatcher();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (parts.size() == 1) {
            parts.get(0).setBackgroundImage(background.imgSrc);
        }
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setLinearGradient(Vector<Color> colors, Vector<Float> positions, int angle) {
        if (background == null) {
            background = new Background();
        }
        background.setLinearGradient(colors, positions, angle);
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setLinearGradientWithUnits(Vector<Color> colors, Vector<String> positions, double angle) {
        if (background == null) {
            background = new Background();
        }
        background.setLinearGradientWithUnits(this, colors, positions, angle);
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setRadialGradientWithUnits(int[] center, double[] size, Vector<Color> colors, Vector<String> positions) {
        if (background == null) {
            background = new Background();
        }
        background.setRadialGradientWithUnits(this, center, size, colors, positions);
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setRadialGradient(int[] center, double[] radius, Vector<Color> colors, Vector<Float> positions) {
        if (background == null) {
            background = new Background();
        }
        background.setRadialGradient(center, radius, colors, positions);
        forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setTextAlign(int value) {
        text_align = value;
        Layouter.applyHorizontalAlignment(this);
        repaint();
    }

    public void setVerticalAlign(int value) {
        vertical_align = value;
    }

    public void setWhiteSpace(int value) {
        white_space = value;
    }

    public void setVisibility(int type) {
        visibility = type;
        if (text_layer != null) {
            text_layer.setVisible(type != Visibility.HIDDEN);
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).setVisibility(type);
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setVisibility(type);
        }
        document.repaint();
    }

    public void setTextShadow(Color color, int offsetX, int offsetY) {
        setTextShadow(color, offsetX, offsetY, 0, false);
    }

    public void setTextShadow(Color color, int offsetX, int offsetY, int blurRadius) {
        setTextShadow(color, offsetX, offsetY, blurRadius, false);
    }

    public void setTextShadow(Color color, int offsetX, int offsetY, int blurRadius, boolean no_rec) {
        textShadowColor = color;
        textShadowOffset[0] = offsetX;
        textShadowOffset[1] = offsetY;
        textShadowBlur = blurRadius;
        text_shadow_buffer  = null;
        if (text_shadow_layer != null) {
            remove(text_shadow_layer);
            invalidate();
        }
        text_shadow_layer = null;
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).setTextShadow(color, offsetX, offsetY, blurRadius, true);
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setTextShadow(color, offsetX, offsetY, blurRadius, true);
        }
        if (document != null && document.ready && !no_rec) {
            forceRepaint();
            document.repaint();
        }
    }

    public void setFontFamily(String value, boolean skip_check) {
        boolean found = true;
        if (!skip_check) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] s = ge.getAvailableFontFamilyNames();
            found = false;
            for (String str: s) {
                if (str.equalsIgnoreCase(value)) {
                    found = true;
                }
            }
        }

        fontFamily = value;

        if (!found) value = "Tahoma";

        Vector<Block> blocks = new Vector<Block>();
        if (beforePseudoElement != null) blocks.add(beforePseudoElement);
        blocks.addAll(children);
        if (afterPseudoElement != null) blocks.add(afterPseudoElement);

        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setFontFamily(value, true);
        }
        
        Block b = doIncrementLayout();
        if (b != null && !no_draw) b.forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setFontFamily(String value) {
        setFontFamily(value, false);
    }

    public void setFontSize(int value) {
        setFontSizePx((int) Math.round(value * ratio));
    }

    public void setFontSize(double val, int units) {
        setFontSizePx(getValueInPixels(val, units));
    }

    public void setFontSizePx(int value) {
        if (type != NodeTypes.ELEMENT) return;
        fontSize = value;

        Vector<Block> blocks = new Vector<Block>();
        if (beforePseudoElement != null) blocks.add(beforePseudoElement);
        blocks.addAll(children);
        if (afterPseudoElement != null) blocks.add(afterPseudoElement);

        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setFontSizePx(value);
        }
        Block b = doIncrementLayout();
        if (b != null && !no_draw) b.forceRepaint();
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void setBold(boolean value) {
        text_bold = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setBold(value);
        }
        performLayout();
        forceRepaint();
    }

    public void setItalic(boolean value) {
        text_italic = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setItalic(value);
        }
        performLayout();
        forceRepaint();
    }

    public void setUnderline(boolean value) {
        text_underline = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setUnderline(value);
        }
        performLayout();
        forceRepaint();
    }

    public void setStrikeThrough(boolean value) {
        text_strikethrough = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setStrikeThrough(value);
        }
        performLayout();
        forceRepaint();
    }

    public void setSelectionEnabled(boolean value) {
        select_enabled = value;
        for (Block part: parts) {
            part.select_enabled = value;
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setSelectionEnabled(value);
        }
    }

    public void setCursor(String value) {
        if (value.equals("pointer")) value = "hand";
        value = value.toUpperCase() + "_CURSOR";
        Field field;
        int cursor_id = -1;
        try {
            field = Cursor.class.getDeclaredField(value);
            cursor_id = field.getInt(Cursor.class);
        } catch (Exception ex) {}
        if (cursor_id >= 0) {
            for (Block part: parts) {
                part.cursor = Cursor.getPredefinedCursor(cursor_id);
            }
            cursor = Cursor.getPredefinedCursor(cursor_id);
        }
    }

    public void setTextColor(String value) {
        Color col = value != null ? parseColor(value) : default_color;
        if (col != null) {
            setTextColor(col);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).setTextColor(col);
            }
        }
    }

    public void setBackgroundColor(String value) {
        Color col = value != null ? parseColor(value) : new Color(0, 0, 0, 0);
        if (col != null) {
            for (Block part: parts) {
                part.setBackgroundColor(col);
            }
            setBackgroundColor(col);
        }
        forceRepaint();
    }

    public void setBorderColor(String value) {
        Color col = parseColor(value);
        if (col != null) {
            setBorderColor(col);
        }
    }

    public void setBorderColor(Color col) {
        for (Block part: parts) {
            for (int i = 0; i < 4; i++) {
                part.borderColor[i] = col;
            }
        }
        for (int i = 0; i < 4; i++) {
            borderColor[i] = col;
        }
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderWidth(int value) {
        int bw = WebDocument.scale_borders && scale_borders ? (int)Math.round(value*ratio) : value;
        for (Block part: parts) {
            for (int i = 0; i < 4; i++) {
                part.borderWidth[i] = bw;
            }
        }
        for (int i = 0; i < 4; i++) {
            borderWidth[i] = bw;
        }
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        updateAbsolutePositionedChildren();
        forceRepaint();
    }

    public void setBorderRadius(int radius) {
        setBorderRadius(radius, radius, radius, radius);
    }

    public void setBorderRadius(int r1, int r2) {
        setBorderRadius(r1, r2, r1, r2);
    }

    public void setBorderRadius(int r1, int r2, int r3) {
        setBorderRadius(r1, r2, r3, r2);
    }

    public void setBorderRadius(int r1, int r2, int r3, int r4) {
        arc[0] = (int)(r1 * 2 * ratio);
        arc[1] = (int)(r2 * 2 * ratio);
        arc[2] = (int)(r3 * 2 * ratio);
        arc[3] = (int)(r4 * 2 * ratio);
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderRadiusNW(int r) {
        arc[0] = (int)(r * 2 * ratio);
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderRadiusNE(int r) {
        arc[1] = (int)(r * 2 * ratio);
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderRadiusSE(int r) {
        arc[2] = (int)(r * 2 * ratio);
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderRadiusSW(int r) {
        arc[3] = (int)(r * 2 * ratio);
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderColor(Color[] col) {
        borderColor = col;
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderWidth(int[] value) {
        if (WebDocument.scale_borders && scale_borders) {
            for (int i = 0; i < 4; i++) {
                value[i] = (int)Math.round(value[i]*ratio);
            }
        }
        borderWidth = value;
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        document.root.performLayout();
        forceRepaint();
    }

    public void setBorderType(int value) {
        for (int i = 0; i < 4; i++) {
            borderType[i] = value;
        }
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderType(int[] value) {
        borderType = value;
        this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
        forceRepaint();
    }

    public void setBorderClipMode(int mode) {
        borderClipMode = mode;
        forceRepaint();
    }

    public void updateBorder() {
        border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
    }

    public void updateAbsolutePositionedChildren() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).positioning == Position.ABSOLUTE || children.get(i).positioning == Position.FIXED) {
                Block b = children.get(i);
                if (b.rules_for_recalc.containsKey("left")) {
                    b.setProp("left", b.rules_for_recalc.get("left"));
                }
                if (b.rules_for_recalc.containsKey("top")) {
                    b.setProp("top", b.rules_for_recalc.get("top"));
                } else {
                    b.setX(borderWidth[3] + b.margins[3] + b.left);
                    b.setY(borderWidth[0] + b.margins[0] + b.top);
                }
            }
        }
    }

    public void setMargins(int a, int b, int c, int d) {
        margins[0] = (int)Math.round(a*ratio);
        margins[1] = (int)Math.round(b*ratio);
        margins[2] = (int)Math.round(c*ratio);
        margins[3] = (int)Math.round(d*ratio);

        Block block = doIncrementLayout();
        if (block != null && !no_draw) block.forceRepaint();
    }

    public void setMargins(int a, int b, int c) {
        setMargins(a, b, c, b);
    }

    public void setMargins(int a, int b) {
        setMargins(a, b, a, b);
    }

    public void setMargins(int a) {
        setMargins(a, a, a, a);
    }

    public void setPaddings(int a, int b, int c, int d) {
        paddings[0] = (int)Math.round(a*ratio);
        paddings[1] = (int)Math.round(b*ratio);
        paddings[2] = (int)Math.round(c*ratio);
        paddings[3] = (int)Math.round(d*ratio);

        Block block = doIncrementLayout();
        if (block != null && !no_draw) block.forceRepaint();
    }

    public void setPaddings(int a, int b, int c) {
        setPaddings(a, b, c, b);
    }

    public void setPaddings(int a, int b) {
        setPaddings(a, b, a, b);
    }

    public void setPaddings(int a) {
        setPaddings(a, a, a, a);
    }

    public void setAutoXMargin() {
        auto_x_margin = true;
        int pw = parent != null ? parent.width : document.width;
        if (parent != null) pw -= parent.borderWidth[3] + parent.borderWidth[1] + parent.paddings[3] + parent.paddings[1];
        if (line != null) pw = line.getWidth();
        margins[3] = (int)Math.round((pw-width)/2);
        margins[1] = pw - width - margins[3];
        if (auto_width) {
            setWidth(-1);
        }
        rules_for_recalc.put("margin-left", "auto");
    }

    public void setAutoYMargin() {
        setAutoYMargin(false);
    }

    public void setAutoYMargin(boolean apply) {
        auto_y_margin = true;
        int ph = parent != null ? parent.viewport_height : document.height;
        if (ph <= 0) return;
        if (parent != null) ph -= parent.borderWidth[0] + parent.borderWidth[2];
        margins[0] = Math.round((ph-height)/2);
        margins[2] = ph - height - margins[0];
        rules_for_recalc.put("margin-top", "auto");
        if (apply && parent.children.size() == 1) {
            _y_ = margins[0];
            parent.lines.lastElement().top = margins[0];
            if (margins[0] < 0) {
                margins[0] = margins[2] = 0;
                _y_ = 0;
                parent.lines.lastElement().top = 0;
                parent.content_y_max = height;
            }
        }
    }

    public void setMaxWidth(int w) {
        w = (int) (w * ratio);
        max_width = w;
        if (width > w) {
            setWidth(w);
        }
    }

    public void setMaxHeight(int h) {
        max_height = h;
        if (height > h) {
            setHeight(h);
        }
    }

    public void setMaxWidthPercentage(double value) {
        max_width_percent = value;
        if (!(parent.auto_width && parent.display_type == Display.INLINE_BLOCK)) {
            max_width = (int) Math.round((parent.viewport_width - parent.borderWidth[3] - parent.borderWidth[1] - parent.paddings[3] - parent.paddings[1]) * (double) max_width_percent / 100);
        }
    }

    public void setMaxHeightPercentage(double value) {
        max_height_percent = value;
        if (!parent.auto_height) {
            max_height = (int) Math.round((parent.viewport_height - parent.borderWidth[0] - parent.borderWidth[2] - parent.paddings[0] - parent.paddings[2]) * (double) max_width_percent / 100);
        }
    }

    public void setWidth(int w, boolean no_recalc) {
        int old_width = viewport_width;
        int old_height = viewport_height;

        if (isImage) {
            width = viewport_width = w >= 0 ? (int)Math.round(w*ratio) : -1;
            orig_width = w;
            if (max_width > -1 && width > max_width) {
                width = max_width;
            }
            if (min_width > -1 && width < min_width) {
                width = min_width;
            }
            auto_width = width < 0;
            Block b = doIncrementLayout(old_width, old_height, no_recalc);
            if (b != null && !no_draw) {
                b.forceRepaint();
            }
            return;
        }

        if (w < 0) {
            if (parent != null) {
                boolean is_flex = parent.display_type == Block.Display.FLEX || parent.display_type == Block.Display.INLINE_FLEX;
                boolean x_axis = parent.flex_direction == Block.Direction.ROW || parent.flex_direction == Block.Direction.ROW_REVERSED;

                if (line == null) {
                    _x_ = margins[3];
                    width = parent.width-parent.borderWidth[1]-parent.borderWidth[3]-margins[1]-_x_-parent.paddings[3]-parent.paddings[1];
                } else {
                    width = !is_flex || x_axis ? line.getWidth()-margins[1]-margins[3] : line.getHeight()-margins[1]-margins[3];
                }
                orig_width = (int)Math.round(width / ratio);
            } else if (document != null) {
                _x_ = margins[3];
                width = document.width-document.borderSize*2-margins[1]-_x_;
                orig_width = (int)Math.round(width / ratio);
            }
            if (max_width > -1 && width > max_width) width = max_width;
            if (min_width > -1 && width < min_width) width = min_width;
            //content_x_max = width;
            auto_width = true;
            rules_for_recalc.put("width", "auto");
        } else {
            width = (int)Math.round(w*ratio);
            orig_width = w;
            if (max_width > -1 && width > max_width) {
                width = max_width;
            }
            if (min_width > -1 && width < min_width) {
                width = min_width;
            }
            rules_for_recalc.remove("width");
            auto_width = false;
            viewport_width = width;
            if (scrollbar_y != null) {
                viewport_width = width - scrollbar_y.getPreferredSize().width;
            }
            //content_x_max = viewport_width;
        }
        if (auto_x_margin && !auto_width) {
            setAutoXMargin();
        }

        if (no_recalc || document != null && !document.ready) return;

        Block b = doIncrementLayout(old_width, old_height, no_recalc);

        if (b != null && !no_draw) {
            b.forceRepaint();
        }
    }

    public void setWidth(int w) {
        setWidth(w, false);
        if (!(dimensions.containsKey("width") && dimensions.get("width").expression != null)) {
            dimensions.put("width", new DynamicValue(w, Units.px, null));
        }
    }

    public void setWidth(int w, int units) {
        int value = (int) Math.round(getValueInCssPixels(w, units));
        if (units == Units.percent) {
            value -= (int) ((double) (margins[3] + margins[1]) / ratio);
        }
        if (units != Units.px) {
            dimensions.put("width", new DynamicValue(w, units, null));
        }
        setWidth(value, false);
    }

    public void setWidthHeight(int w, int h) {
        int old_width = viewport_width;
        int old_height = viewport_height;

        width = viewport_width = (int) (w * ratio);
        height = viewport_height = (int) (h * ratio);
        auto_width = w < 0;
        auto_height = h < 0;

        orig_width = w;
        if (max_width_percent >= 0 && !(parent.auto_width && parent.display_type == Display.INLINE_BLOCK)) {
            max_width = (int) Math.round((parent.viewport_width - parent.borderWidth[3] - parent.borderWidth[1] - parent.paddings[3] - parent.paddings[1]) * (double) max_width_percent / 100);
        }
        if (max_width > -1 && width > max_width) {
            width = max_width;
        }
        if (min_width > -1 && width < min_width) {
            width = min_width;
        }
        rules_for_recalc.remove("width");
        auto_width = false;

        if (auto_x_margin && !auto_width) {
            setAutoXMargin();
        }

        orig_height = h;
        max_height = height;
        auto_height = false;

        if (auto_y_margin) {
            setAutoYMargin();
        }

        Block b = doIncrementLayout(old_width, old_height, false);

        if (b != null && !no_draw) {
            b.forceRepaint();
        }
    }

    public boolean no_draw = false;
    public boolean no_layout = false;

    public void setHeight(int h, boolean no_recalc) {
        int old_width = viewport_width;
        int old_height = viewport_height;

        if (isImage) {
            height = viewport_height = (int)Math.round(h*ratio);
            if (max_height > -1 && height > max_height) {
                height = max_height;
            }
            if (min_height > -1 && height < min_height) {
                height = min_height;
            }
            viewport_height = height;
            orig_height = h;
            auto_height = false;
            Block b = doIncrementLayout(old_width, old_height, no_recalc);
            if (b != null && !no_draw) {
                b.forceRepaint();
            }
            return;
        }
        
        if (h < 0) {
            auto_height = true;
            Block b = parent != null ? parent : this;
            if (content_y_max > 0 && parent != null) {
                height = viewport_height = content_y_max + paddings[2] + borderWidth[2];
                orig_height = (int)Math.round(height / ratio);
                b = parent.doIncrementLayout();
            } else if (document != null && document.ready) {
                orig_height = height = -1;
                b.performLayout(false);
            }
            if (!b.no_draw) {
                b.forceRepaint();
            }
            document.repaint();
            return;
        } else {
            height = (int)Math.round(h*ratio);
            if (max_height > -1 && height > max_height) {
                height = max_height;
            }
            if (min_height > -1 && height < min_height) {
                height = min_height;
            }
            viewport_height = height;
            orig_height = h;
            auto_height = false;
            viewport_height = height;
            if (scrollbar_x != null) {
                viewport_height = height - scrollbar_x.getPreferredSize().height;
            }
        }
        if (auto_y_margin) {
            setAutoYMargin();
        }
        
        Block b = doIncrementLayout(old_width, old_height, no_recalc);
        
        if (b != null && !no_draw) {
            b.forceRepaint();
        }
    }

    public void setHeight(int h) {
        setHeight(h, false);
        if (!(dimensions.containsKey("height") && dimensions.get("height").expression != null)) {
            dimensions.put("height", new DynamicValue(h, Units.px, null));
        }
    }

    public void setHeight(int h, int units) {
        int value = (int) Math.round(getValueInCssPixels(h, units));
        if (units != Units.px) {
            dimensions.put("height", new DynamicValue(h, units, null));
        }
        setHeight(value, false);
    }

    public Block doIncrementLayout() {
        return doIncrementLayout(viewport_width, viewport_height, false);
    }

    public Block doIncrementLayout(boolean no_recalc) {
        return doIncrementLayout(viewport_width, viewport_height, no_recalc);
    }

    public Block doIncrementLayout(int old_width, int old_height, boolean no_recalc) {
        if (document == null || document.inLayout || !document.ready) return null;

        double old_pos_x = background != null && old_width > 0 ? (double) background.background_pos_x / old_width : 0;
        double old_pos_y = background != null && old_height > 0 ? (double) background.background_pos_y / old_height : 0;
        double old_size_x = background != null && old_width > 0 ? (double) background.background_size_x / old_width : 1;
        double old_size_y = background != null && old_height > 0 ? (double) background.background_size_y / old_height : 1;

        Block last = this;

        if (layouter != null || parts.size() > 0 && parts.get(0).layouter != null) {
            boolean use_fast_update = display_type != Display.FLEX && display_type != Display.INLINE_FLEX && document.fast_update;
            document.no_layout = use_fast_update;
            performLayout(use_fast_update);
            if (viewport_height != old_height && background != null && background.bgImage != null) {
                background.background_pos_x = (int) (old_pos_x * viewport_width);
                background.background_pos_y = (int) (old_pos_y * viewport_height);
                background.background_size_x = (int) Math.max(0, old_size_x * viewport_width);
                background.background_size_y = (int) Math.max(0, old_size_y * viewport_height);
            }
            for (int i = 0; i < children.size(); i++) {
                Block child = children.get(i);
                if (child.dimensions != null && (child.dimensions.containsKey("width") && (child.dimensions.get("width").length.unit == Units.percent || child.dimensions.get("width").expression != null) && old_width != viewport_width ||
                      child.dimensions.containsKey("height") && (child.dimensions.get("height").length.unit == Units.percent || child.dimensions.get("height").expression != null) && old_height != viewport_height)) {
                    performLayout(false);
                }
            }
            Block b = this;
            while ((old_width != b.viewport_width || old_height != b.viewport_height) && b.parent != null && !no_recalc) {
                b = b.parent;
                old_width = b.viewport_width;
                old_height = b.viewport_height;
                b.performLayout(use_fast_update);
                for (int i = 0; i < b.children.size(); i++) {
                    Block child = b.children.get(i);
                    if (child.dimensions != null && (child.dimensions.containsKey("width") && (child.dimensions.get("width").length.unit == Units.percent || child.dimensions.get("width").expression != null) && old_width != b.viewport_width ||
                          child.dimensions.containsKey("height") && (child.dimensions.get("height").length.unit == Units.percent || child.dimensions.get("height").expression != null) && old_height != b.viewport_height)) {
                        b.performLayout(false);
                    }
                }
            }
            document.no_layout = false;
            last = b;
            // This will be called inside performLayout() for root element
            if (b != document.root) b.setZIndices();

            //document.root.setZIndices();
            document.root.clipScrollbars();

        } else if (this == document.root && document.ready) {
            document.root.performLayout();
            if (viewport_height != old_height && background != null && background.bgImage != null) {
                background.background_pos_x = (int) (old_pos_x * viewport_width);
                background.background_pos_y = (int) (old_pos_y * viewport_height);
                background.background_size_x = (int) Math.max(0, old_size_x * viewport_width);
                background.background_size_y = (int) Math.max(0, old_size_y * viewport_height);
            }
        } else if (isImage) {
            last = parts.size() == 0 ? this : parts.get(0);
            last.processImage();
        }
        document.repaint();
        return last;
    }

    public void setBackgroundPositionX(double val, int units) {
        if (background == null) {
            background = new Background();
        }
        background.background_pos_x = getValueInPixels(val, units);
        forceRepaint();
    }

    public void setBackgroundPositionY(double val, int units) {
        if (background == null) {
            background = new Background();
        }
        background.background_pos_y = getValueInPixels(val, units);
        forceRepaint();
    }

    public void setBackgroundSizeX(double val, int units) {
        if (background == null) {
            background = new Background();
        }
        if (val < 0) {
            background.background_size_x_auto = true;
            forceRepaint();
            return;
        }
        background.background_size_x = getValueInPixels(val, units);
        background.background_size_x_auto = false;
        if (background.background_size_y_auto && background.bgImage != null && background.bgImage.getHeight() > 0) {
            background.background_size_y = (int)Math.round(background.background_size_x / ((double) background.bgImage.getWidth() / background.bgImage.getHeight()));
        }
        forceRepaint();
    }

    public void setBackgroundSizeY(double val, int units) {
        if (background == null) {
            background = new Background();
        }
        if (val < 0) {
            background.background_size_y_auto = true;
            forceRepaint();
            return;
        }
        background.background_size_y = getValueInPixels(val, units);
        background.background_size_y_auto = false;
        if (background.background_size_x_auto && background.bgImage != null) {
            background.background_size_x = (int)Math.round(background.background_size_y * ((double) background.bgImage.getWidth() / background.bgImage.getHeight()));
        }
        forceRepaint();
    }

    public void setBackgroundSizeXY(double val_x, double val_y, int units) {
        if (background == null) {
            background = new Background();
        }
        if (val_x < 0) background.background_size_x_auto = true;
        if (val_y < 0) background.background_size_y_auto = true;

        int value_x = 0, value_y = 0;
        if (units == Units.px) {
            value_x = val_x > 0 ? (int)Math.round(val_x) : -1;
            value_y = val_y > 0 ? (int)Math.round(val_y) : -1;
        }
        else if (units == Units.percent) {
            value_x = (int)Math.ceil((height-borderWidth[0]-borderWidth[2])*(val_x/100));
            value_y = (int)Math.ceil((height-borderWidth[0]-borderWidth[2])*(val_y/100));
        }
        else if (units == Units.em) {
            Block b = parent != null ? parent : (document != null ? document.root : null);
            double size = (b != null ? b.fontSize : 16 * ratio);
            value_x = (int)Math.round(size * value_x);
            value_y = (int)Math.round(size * value_y);
        }
        else if (units == Units.rem) {
            Block b = document != null ? document.root : null;
            double size = (b != null ? b.fontSize : 16 * ratio);
            value_x = (int)Math.round(size * value_x);
            value_y = (int)Math.round(size * value_y);
        }
        background.background_size_x = value_x;
        background.background_size_y = value_y;
        background.background_size_x_auto = val_x < 0;
        background.background_size_y_auto = val_y < 0;

        if (!background.background_size_x_auto && background.background_size_y_auto && background.bgImage != null && background.bgImage.getHeight() > 0) {
            background.background_size_y = (int)Math.round(background.background_size_x / ((double) background.bgImage.getWidth() / background.bgImage.getHeight()));
        }
        else if (background.background_size_x_auto && !background.background_size_y_auto && background.bgImage != null && background.bgImage.getHeight() > 0) {
            background.background_size_x = (int)Math.round(background.background_size_y * ((double) background.bgImage.getWidth() / background.bgImage.getHeight()));
        }
        else if (background.background_size_x_auto && background.background_size_y_auto && background.bgImage != null && background.bgImage.getHeight() > 0) {
            background.background_size_x = background.bgImage.getWidth();
            background.background_size_y = background.bgImage.getHeight();
        }

        forceRepaint();
    }

    public void setBackgroundSizeAuto() {
        setBackgroundSizeXY(-1f, -1f, Units.px);
    }

    public void setBackgroundContain() {
        if (width < 0 || height < 0) return;
        if (background == null) {
            background = new Background();
        }
        double r1 = (background.bgImage != null && background.bgImage.getHeight() > 0) ? (double) background.bgImage.getWidth() / background.bgImage.getHeight() : 0;
        double r2 = height > 0 ? (double) width / height : 0;
        if (r1 > r2) {
            background.background_size_y_auto = true;
            setBackgroundSizeX(100, Units.percent);
        } else {
            background.background_size_x_auto = true;
            setBackgroundSizeY(100, Units.percent);
        }
        setBackgroundPositionX(50, Units.percent);
        setBackgroundPositionY(50, Units.percent);
        forceRepaint();
    }

    public void setBackgroundCover() {
        if (background == null) {
            background = new Background();
        }
        double r1 = (background.bgImage != null && background.bgImage.getHeight() > 0) ? (double) background.bgImage.getWidth() / background.bgImage.getHeight() : 0;
        double r2 = height > 0 ? (double) width / height : 0;
        if (r1 < r2) {
            background.background_size_y_auto = true;
            setBackgroundSizeX(100, Units.percent);
        } else {
            background.background_size_x_auto = true;
            setBackgroundSizeY(100, Units.percent);
        }
        setBackgroundPositionX(50, Units.percent);
        setBackgroundPositionY(50, Units.percent);
        forceRepaint();
    }

    public void setBackgroundRepeat(int value) {
        if (background == null) {
            background = new Background();
        }
        background.background_repeat = value;
        forceRepaint();
    }

    public void setPositioning(int value) {
        positioning = value;
        Block b = doIncrementLayout();
        if (b != null && !no_draw) {
            b.forceRepaint();
        }
    }

    public void setDisplayType(int value) {
        display_type = value;
        if (display_type != Display.BLOCK && (auto_width || display_type == Display.INLINE)) {
            width = borderWidth[3] + paddings[3] + paddings[1] + borderWidth[1];
            if (height < 0 || display_type == Display.INLINE) {
                height = borderWidth[0] + paddings[0] + paddings[2] + borderWidth[2];
            }
        }
        if (display_type == Display.INLINE_BLOCK && auto_width) {
            width = 0;
            viewport_width = 0;
        }
        if (display_type == Display.NONE) {
            removeTextLayers();
            for (Block part: parts) {
                part.clearBuffer();
            }
            if (parent != null) {
                parent.removeFromLayout(this);
            }
        } else if (parent != null && parent.layouter != null) {
            Block root = this;
            while (root.parent != null) {
                root = root.parent;
            }
            parent.addToLayout(this, parent.children.indexOf(this), root);
        }
        Block b = doIncrementLayout();
        if (b != null && !no_draw) {
            b.forceRepaint();
        }
        if (document != null && document.ready) {
            document.repaint();
        }
    }

    public void removeTextLayers() {
        if (text_layer != null) {
            remove(text_layer);
            text_layer = null;
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).removeTextLayers();
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).removeTextLayers();
        }
    }

    public TableLayout getTable() {
        return table;
    }

    public boolean isRoot() {
        return positioning != 0;
    }

    public boolean hasBlockChildren() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).type == NodeTypes.ELEMENT &&
                   children.get(i).display_type != Display.INLINE) {
                return true;
            }
        }
        return false;
    }

    public String toHyphens(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            if (i > 0 && java.lang.Character.isUpperCase(str.charAt(i)) && java.lang.Character.isLowerCase(str.charAt(i-1))) {
                result += "-" + java.lang.Character.toLowerCase(str.charAt(i));
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }

    public void setProp(String prop, String value) {
        if (document != null && document.no_immediate_apply) return;
        prop = toHyphens(prop);
        if (original != null) {
            original.setProp(prop, value);
        }
        if (prop.equals("display")) {
            String[] display_types = new String[] { "block", "inline-block", "inline", "none",  "table", "inline-table", "table-row", "table-cell" };
            for (int i = 0; i < display_types.length; i++) {
                if (display_types[i].equals(value)) {
                    setDisplayType(i);
                    break;
                }
            }
            return;
        }
        if (prop.equals("position")) {
            String[] position_types = new String[] { "static", "relative", "absolute", "fixed" };
            for (int i = 0; i < position_types.length; i++) {
                if (position_types[i].equals(value)) {
                    setPositioning(i);
                    break;
                }
            }
            return;
        }
        if (prop.equals("visibility")) {
            setVisibility(value.equals("hidden") ? Visibility.HIDDEN : Visibility.VISIBLE);
            return;
        }
        if (prop.equals("font-family")) {
            setFontFamily(value);
            return;
        }
        if (prop.equals("font-size")) {
            int val = getValueInPixels(value);
            setFontSizePx(val);
            return;
        }
        if (prop.equals("font-weight")) {
            if (value.matches("[0-9]+")) {
                text_bold = Integer.parseInt(value) > 400;
            }
            text_bold = value.equals("bold");
            Block b = doIncrementLayout();
            if (b != null && !no_draw) b.forceRepaint();
            if (document != null && document.ready) {
                document.repaint();
            }
            return;
        }
        if (prop.equals("font-style")) {
            text_italic = value.equals("italic");
            Block b = doIncrementLayout();
            if (b != null && !no_draw) b.forceRepaint();
            if (document != null && document.ready) {
                document.repaint();
            }
            return;
        }
        if (prop.equals("text-decoration")) {
            text_underline = value.equals("underline");
            text_strikethrough = value.equals("strikethrough");
            Block b = doIncrementLayout();
            if (b != null && !no_draw) b.forceRepaint();
            if (document != null && document.ready) {
                document.repaint();
            }
            return;
        }
        if (prop.equals("text-align")) {
            String[] align_types = new String[] { "left", "center", "right", "justify" };
            for (int i = 0; i < align_types.length; i++) {
                if (align_types[i].equals(value)) {
                    setTextAlign(i);
                    break;
                }
            }
            return;
        }
        if (prop.equals("vertical-align")) {
            String[] align_types = new String[] { "top", "middle", "baseline", "bottom" };
            for (int i = 0; i < align_types.length; i++) {
                if (align_types[i].equals(value)) {
                    setVerticalAlign(i);
                    break;
                }
            }
            return;
        }
        if (prop.equals("margin")) {
            String[] s = value.split("\\s");
            if (s.length > 0) {
                if (s.length == 4) {
                    setProp("margin-top", s[0]);
                    setProp("margin-right", s[1]);
                    setProp("margin-bottom", s[2]);
                    setProp("margin-left", s[3]);
                    return;
                } else if (s.length == 3) {
                    setProp("margin-top", s[0]);
                    setProp("margin-right", s[1]);
                    setProp("margin-bottom", s[2]);
                    setProp("margin-left", s[1]);
                    return;
                } else if (s.length == 2) {
                    setProp("margin-top", s[0]);
                    setProp("margin-right", s[1]);
                    setProp("margin-bottom", s[0]);
                    setProp("margin-left", s[1]);
                    return;
                } else {
                    setProp("margin-top", s[0]);
                    setProp("margin-right", s[0]);
                    setProp("margin-bottom", s[0]);
                    setProp("margin-left", s[0]);
                    return;
                }
            }
        }
        if (prop.matches("margin(-left|-right)?") && value.equals("auto")) {
            setAutoXMargin();
            return;
        }
        if (prop.matches("margin(-top|-bottom)?") && value.equals("auto")) {
            setAutoYMargin();
            return;
        }
        if (prop.equals("width") && value.equals("auto")) {
            setWidth(-1);
            return;
        }
        if (prop.equals("width")) {
            if (value.matches("calc\\(.*\\)")) {
                dimensions.put("width", new DynamicValue(0, -1, value));
            }
            setWidth((int)Math.round(getValueInCssPixels(value)));
            return;
        }
        if (prop.equals("height") && value.equals("auto")) {
            int old_height = viewport_height;
            auto_height = true;
            viewport_height = height = -1;
            Block b = doIncrementLayout(viewport_width, old_height, false);
            b.forceRepaint();
            if (document != null) document.repaint();
            return;
        }
        if (prop.equals("height")) {
            setHeight((int)Math.round(getValueInCssPixels(value)));
            if (value.matches("calc\\(.*\\)")) {
                dimensions.put("height", new DynamicValue(0, -1, value));
            }
            return;
        }
        if (prop.equals("max-width")) {
            int old_width = width;
            if (value.matches("auto|unset")) {
                max_width_percent = -1;
                Block b = doIncrementLayout(viewport_width, viewport_height, false);
                b.forceRepaint();
                if (document != null) document.repaint();
                return;
            } else if (!value.endsWith("%")) {
                setMaxWidth(getValueInPixels(value));
            } else {
                setMaxWidthPercentage(Float.parseFloat(value.replaceAll("[a-z%]+$", "")));
            }
            Block b = doIncrementLayout(old_width, viewport_height, false);
            b.forceRepaint();
            if (document != null) document.repaint();
            return;
        }
        if (prop.equals("max-height")) {
            int old_height = height;
            if (value.matches("auto|unset")) {
                max_height_percent = -1;
                doIncrementLayout();
                return;
            } else if (!value.endsWith("%")) {
                setMaxHeight(getValueInPixels(value));
            } else {
                setMaxHeightPercentage(Float.parseFloat(value.replaceAll("[a-z%]+$", "")));
            }
            doIncrementLayout(viewport_width, old_height, false);
            return;
        }
        if (prop.equals("aspect-ratio") && value.matches("[0-9]+")) {
            aspect_ratio = Integer.parseInt(value);
            doIncrementLayout(viewport_width, viewport_height, false);
            return;
        }
        if (prop.equals("left") && value.equals("auto")) {
            auto_left = true;
            rules_for_recalc.put("left", "auto");
            return;
        } else if (prop.equals("left")) {
            auto_left = false;
            rules_for_recalc.remove("left");
        }
        if (prop.equals("right") && value.equals("auto")) {
            auto_right = true;
            return;
        }

        if (prop.equals("top") && value.equals("auto")) {
            auto_top = true;
            return;
        } else if (prop.equals("top")) {
            auto_top = false;
        }
        if (prop.equals("bottom") && value.equals("auto")) {
            auto_bottom = true;
            return;
        } else if (prop.equals("bottom")) {
            auto_bottom = false;
        }

        if (prop.equals("right") && value.equals("auto")) {
            auto_right = true;
            return;
        }

        if (prop.matches("border(-left|-right|-top|-bottom)-color")) {
            if (prop.contains("-left")) borderColor[3] = parseColor(value);
            if (prop.contains("-right")) borderColor[1] = parseColor(value);
            if (prop.contains("-top")) borderColor[0] = parseColor(value);
            if (prop.contains("-bottom")) borderColor[2] = parseColor(value);
            for (Block part: parts) {
                part.borderColor[0] = borderColor[0];
                part.borderColor[1] = borderColor[1];
                part.borderColor[2] = borderColor[2];
                part.borderColor[3] = borderColor[3];
            }
            forceRepaint();
            return;
        }

        if (prop.equals("border-color")) {
            String[] s = value.split("(?<=[^,])\\s");
            if (s.length > 0) {
                if (s.length == 4) {
                    borderColor[0] = parseColor(s[0]);
                    borderColor[1] = parseColor(s[1]);
                    borderColor[2] = parseColor(s[2]);
                    borderColor[3] = parseColor(s[3]);
                } else if (s.length == 3) {
                    borderColor[0] = parseColor(s[0]);
                    borderColor[1] = parseColor(s[1]);
                    borderColor[2] = parseColor(s[2]);
                    borderColor[3] = borderColor[1];
                } else if (s.length == 2) {
                    borderColor[0] = parseColor(s[0]);
                    borderColor[1] = parseColor(s[1]);
                    borderColor[2] = borderColor[0];
                    borderColor[3] = borderColor[1];
                } else {
                    borderColor[0] = parseColor(s[0]);
                    borderColor[1] = borderColor[0];
                    borderColor[2] = borderColor[0];
                    borderColor[3] = borderColor[0];
                }
                for (Block part: parts) {
                    part.borderColor[0] = borderColor[0];
                    part.borderColor[1] = borderColor[1];
                    part.borderColor[2] = borderColor[2];
                    part.borderColor[3] = borderColor[3];
                }
                forceRepaint();
                return;
            }
        }

        if (prop.equals("border-width")) {
            setProp("border-left-width", value);
            setProp("border-right-width", value);
            setProp("border-top-width", value);
            setProp("border-bottom-width", value);
            return;
        }

        if (prop.equals("color")) {
            setTextColor(value);
            return;
        }

        if (prop.equals("background")) {
            if (value.matches("(-[a-z]+-)?linear-gradient\\(.*\\)")) {
                setRadialGradientFromCSS(value);
                return;
            }
            if (value.matches("(-[a-z]+-)?radial-gradient\\(.*\\)")) {
                setRadialGradientFromCSS(value);
                return;
            }
            String[] parts = value.split("(?<=[^,])\\s+");
            int pos = -1;
            int size = -1;
            int slash = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("[0-9.]+[0-9]+(px|%)$")) {
                    if (pos == -1 && slash == -1) {
                        setProp("background-position-x", parts[i]);
                        pos = i;
                    }
                    if (pos == i-1 && slash == -1) {
                        setProp("background-position-y", parts[i]);
                        pos = i;
                    }
                    if (slash == i-1 && size == -1) {
                        setProp("background-size-x", parts[i]);
                        size = i;
                    }
                    if (slash == i-2 && size == i-1) {
                        setProp("background-size-y", parts[i]);
                        size = i;
                    }
                }
                else if (parts[i].equals("/")) {
                    slash = i;
                }
                else if (parseColor(parts[i]) != null) {
                    setProp("background-color", parts[i]);
                }
                else if (parts[i].startsWith("url(")) {
                    setProp("background-image", parts[i]);
                }
                else if (slash == i-1 && parts[i].matches("^(cover|contain|auto)$")) {
                    if (parts[i].equals("cover")) setBackgroundCover();
                    else if (parts[i].equals("contain")) setBackgroundContain();
                    else if (parts[i].equals("auto")) setBackgroundSizeAuto();
                }
            }
            return;
        }

        if (prop.equals("background-color")) {
            setBackgroundColor(value);
            return;
        }

        if (prop.equals("background-image")) {
            if (!value.startsWith("url(")) return;
            int pos = value.indexOf("\"");
            int pos2 = value.lastIndexOf("\"");
            if (pos < 0 && pos2 < 0) {
                pos = value.indexOf("(");
                pos2 = value.lastIndexOf(")");
            }
            if (pos < 0 && pos2 < 0 || pos == pos2) return;
            setBackgroundImage(value.substring(pos+1, pos2));
            return;
        }

        if (prop.equals("background-position-x")) {
            if (value.matches("^[0-9.]+[0-9](px|em|rem|%|v(w|h|min|max))$")) {
                int[] val = parseValueStringToArray(value);
                setBackgroundPositionX(val[0], val[1]);
            }
        }

        if (prop.equals("background-position-y")) {
            if (value.matches("^[0-9.]+[0-9](px|em|rem|%|v(w|h|min|max)$")) {
                int[] val = parseValueStringToArray(value);
                setBackgroundPositionY(val[0], val[1]);
            }
        }

        if (prop.equals("background-size-x")) {
            if (value.matches("^[0-9.]+[0-9](px|em|rem|%|v(w|h|min|max))$")) {
                int[] val = parseValueStringToArray(value);
                setBackgroundSizeX(val[0], val[1]);
            }
        }

        if (prop.equals("background-size-y")) {
            if (value.matches("^[0-9.]+[0-9](px|em|rem|%|v(w|h|min|max))$")) {
                int[] val = parseValueStringToArray(value);
                setBackgroundSizeY(val[0], val[1]);
            }
        }

        if (prop.equals("box-shadow")) {
            String[] s = value.split("(?<=[^,])\\s");
            try {
                has_shadow = true;
                shadow_x = (int)Math.round(Double.parseDouble(s[0].replaceAll("[^0-9.-]+", "")));
                shadow_y = (int)Math.round(Double.parseDouble(s[1].replaceAll("[^0-9.-]+", "")));
                shadow_blur = (int)Math.round(Double.parseDouble(s[2].replaceAll("[^0-9.]+", "")));
                shadow_size = (int)Math.round(Double.parseDouble(s[3].replaceAll("[^0-9.]+", "")));
                shadow_color = parseColor(s[4]);
            } catch(Exception ex) {
                has_shadow = false;
                shadow_x = 0;
                shadow_y = 0;
                shadow_blur = 0;
                shadow_size = 0;
                shadow_color = Color.BLACK;
            }
            return;
        }

        if (prop.equals("text-shadow")) {
            Color col = null;
            String[] s = value.split("(?<=[^,])\\s");
            int seriesLength = 0;
            int offsetX = 1;
            int offsetY = 1;
            int blurRadius = 0;
            boolean readSeries = false;
            for (int i = 0; i < s.length; i++) {
                boolean colorWasSet = col != null;
                col = parseColor(s[i]);
                if (!colorWasSet && col != null) {
                    readSeries = false;
                    continue;
                }
                if (s[i].matches("[0-9]+[.0-9]*px")) {
                    // Do not allow to put shadow color in the middle of the string
                    if (seriesLength > 0 && !readSeries) return;
                    readSeries = true;
                    seriesLength++;
                    if (seriesLength == 1) {
                        offsetX = Integer.parseInt(s[i].substring(0, s[i].length()-2));
                    } else if (seriesLength == 2) {
                        offsetY = Integer.parseInt(s[i].substring(0, s[i].length()-2));
                    }
                     else if (seriesLength == 3) {
                        blurRadius = Integer.parseInt(s[i].substring(0, s[i].length()-2));
                    }
                }
            }
            setTextShadow(col, offsetX, offsetY, blurRadius);
            if (textRenderingMode == 0) {
                removeTextLayers();
            }
            forceRepaint();
            return;
        }

        if (prop.equals("border(-left|-right|-bottom|-top)-width")) {
            if (value.matches("^[0-9]+.*(px|em)$")) {
                int val = getValueInPixels(value);
                if (prop.contains("-left")) this.borderWidth[3] = val;
                if (prop.contains("-right")) this.borderWidth[1] = val;
                if (prop.contains("-bottom")) this.borderWidth[2] = val;
                if (prop.contains("-top")) this.borderWidth[0] = val;
            }
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            forceRepaint();
            return;
        }

        if (prop.matches("border-(top-left|top-right|bottom-left|bottom-right)-radius")) {
            int val = 0;
            if (value.matches("^[0-9]+.*(px|em)$")) {
                val = getValueInPixels(value);
            } else if (value.matches(".*%")) {
                val = (int) ((double) width * val / 100);
            }
            if (prop.contains("top-left")) arc[0] = (int)(val * 2);
            if (prop.contains("top-right")) arc[1] = (int)(val * 2);
            if (prop.contains("bottom-right")) arc[2] = (int)(val * 2);
            if (prop.contains("bottom-left")) arc[3] = (int)(val * 2);
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            forceRepaint();
            return;
        }

        if (prop.equals("border-radius")) {
            int val = 0;
            if (value.matches("^[0-9]+.*(px|em)$")) {
                val = getValueInPixels(value);
            } else {
                val = (int) ((double) width * val / 100);
            }
            arc[0] = (int)(val * 2);
            arc[1] = (int)(val * 2);
            arc[2] = (int)(val * 2);
            arc[3] = (int)(val * 2);
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            forceRepaint();
            return;
        }

        if (prop.matches("border(-left|-right|-bottom|-top)?")) {
            String[] s = value.split("\\s+");
            HashMap<String, Integer> types = new HashMap<String, Integer>();
            types.put("solid", 0);
            types.put("dashed", 1);
            types.put("dotted", 2);
            for (int i = 0; i < s.length; i++) {
                if (s[i].matches("^[0-9]+.*(px|em|rem|v(w|h|min|max))$")) {
                    int val = getValueInPixels(s[i]);
                    if (prop.contains("-left") || prop.equals("border")) borderWidth[3] = val;
                    if (prop.contains("-right") || prop.equals("border")) borderWidth[1] = val;
                    if (prop.contains("-bottom") || prop.equals("border")) borderWidth[2] = val;
                    if (prop.contains("-top") || prop.equals("border")) borderWidth[0] = val;
                } else if (s[i].matches("^(#|rgba?).*") || !types.containsKey(s[i])) {
                    Color col = parseColor(s[i]);
                    if (prop.contains("-left") || prop.equals("border")) borderColor[3] = col;
                    if (prop.contains("-right") || prop.equals("border")) borderColor[1] = col;
                    if (prop.contains("-bottom") || prop.equals("border")) borderColor[2] = col;
                    if (prop.contains("-top") || prop.equals("border")) borderColor[0] = col;
                } else {
                    if (types.containsKey(s[i])) {
                        int t = types.get(s[i]);
                        if (prop.contains("-left") || prop.equals("border")) borderType[3] = t;
                        if (prop.contains("-right") || prop.equals("border")) borderType[1] = t;
                        if (prop.contains("-bottom") || prop.equals("border")) borderType[2] = t;
                        if (prop.contains("-top") || prop.equals("border")) borderType[0] = t;
                    }
                }
            }
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            forceRepaint();
            return;
        }

        if (prop.equals("z-index")) {
            setZIndex(value);
            return;
        }

        if (prop.endsWith("user-select")) {
            setSelectionEnabled(!value.equals("none"));
            return;
        }

        if (prop.equals("cursor")) {
            setCursor(value);
            return;
        }

        if (prop.equals("flex-basis")) {
            if (value.matches("([0-9]+(px|em|rem|%|v(w|h|min|max))|auto|(min-|max-)content)$")) {
                if (value.matches("[0-9]+(px|em|rem|%|v(w|h|min|max))")) {
                    flex_basis = getValueInPixels(value);
                    flex_basis_mode = FlexBasis.EXPLICIT;
                } else if (value.equals("auto")) {
                    flex_basis_mode = FlexBasis.AUTO;
                } else if (value.equals("min-content")) {
                    flex_basis_mode = FlexBasis.MIN_CONTENT;
                } else if (value.equals("max-content")) {
                    flex_basis_mode = FlexBasis.MAX_CONTENT;
                }
            }
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex-grow") && value.matches("[0-9]+")) {
            flex_grow = Integer.parseInt(value);
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex-shrink") && value.matches("[0-9]+")) {
            flex_shrink = Integer.parseInt(value);
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex-wrap")) {
            flex_wrap = value.equals("wrap") ? WhiteSpace.NORMAL : WhiteSpace.NO_WRAP;
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("justify-content")) {
            String[] modes = new String[] { "flex-start", "center", "flex-end", "space-between", "space-around", "space-evenly" };
            for (int i = 0; i < modes.length; i++) {
                if (value.equals(modes[i])) {
                    flex_justify = i;
                    break;
                }
            }
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("align-items")) {
            String[] modes = new String[] { "stretch", "flex-start", "flex-center", "flex-end" };
            for (int i = 0; i < modes.length; i++) {
                if (value.equals(modes[i])) {
                    flex_align_items = i;
                    break;
                }
            }
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("align-content")) {
            String[] modes = new String[] { "stretch", "flex-start", "flex-center", "flex-end" };
            for (int i = 0; i < modes.length; i++) {
                if (value.equals(modes[i])) {
                    flex_align_content = i;
                    break;
                }
            }
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex-direction")) {
            String[] modes = new String[] { "row", "row-reverse", "column", "column-reverse" };
            for (int i = 0; i < modes.length; i++) {
                if (value.equals(modes[i])) {
                    flex_direction = i;
                    break;
                }
            }
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex-gap") && value.matches("[0-9]+(px|%|em|rem|v(w|h|min|max))")) {
            flex_gap = getValueInPixels(value);
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.equals("flex")) {
            boolean ready = document.ready;
            document.ready = false;
            String[] parts = value.split("\\s+");
            int n = 0;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("([0-9]+(px|em|rem|%|v(w|h|min|max))|auto|(min-|max-)content)$")) {
                    if (parts[i].matches("[0-9]+(px|em|rem|%|v(w|h|min|max))")) {
                        flex_basis = getValueInPixels(parts[i]);
                        flex_basis_mode = FlexBasis.EXPLICIT;
                    } else if (parts[i].equals("auto")) {
                        flex_basis_mode = FlexBasis.AUTO;
                    } else if (parts[i].equals("min-content")) {
                        flex_basis_mode = FlexBasis.MIN_CONTENT;
                    } else if (parts[i].equals("max-content")) {
                        flex_basis_mode = FlexBasis.MAX_CONTENT;
                    }
                } else if (parts[i].matches("[0-9]+")) {
                    if (n == 0) {
                        flex_grow = Integer.parseInt(parts[i]);
                    } else if (n == 1) {
                        flex_shrink = Integer.parseInt(parts[i]);
                    }
                    n++;
                } else if (parts[i].matches("(no-)?wrap")) {
                    flex_wrap = value.equals("wrap") ? WhiteSpace.NORMAL : WhiteSpace.NO_WRAP;
                }
            }
            document.ready = ready;
            if (parent != null) {
                parent.performLayout();
                parent.forceRepaint();
                return;
            }
        }

        if (prop.endsWith("transition")) {
            Vector<String> parts = new Vector<String>();
            int pos = 0;
            int level = 0;
            String part = "";
            while (pos < value.length()) {
                if (value.charAt(pos) == '(') level++;
                else if (value.charAt(pos) == ')') level--;
                if (level == 0 && value.charAt(pos) == ',') {
                    parts.add(part);
                    part = "";
                    pos++;
                    continue;
                }
                part += value.charAt(pos);
                pos++;
            }
            if (!part.isEmpty() && !part.matches("\\s+")) {
                parts.add(part);
            }

            for (int i = 0; i < parts.size(); i++) {
                int n = 0;
                String property = "";
                int timingFunction = Transition.TimingFunction.LINEAR;
                int duration = 0;
                int delay = 0;
                Vector<String> timingFuncs = new Vector<String>(Arrays.asList(new String[] {"linear", "ease-in", "ease-out", "ease", "bounce"}));

                String[] p = parts.get(i).split("(?<=[^,])\\s+");
                for (String str: p) {
                    if (str.isEmpty()) continue;
                    if (str.endsWith("s")) {
                        String ch = str.substring(0, 1);
                        String val = "";
                        int index = 0;
                        while (ch.matches("[0-9.]")) {
                            val += ch;
                            index++;
                            ch = str.substring(index, index+1);
                        }
                        int time = 0;
                        String u = str.substring(index);
                        if (u.equals("s")) {
                            time = (int) Math.round(Float.parseFloat(val) * 1000);
                        } else {
                            time = (int) Math.round(Float.parseFloat(val));
                        }
                        if (n == 0) {
                            duration = time;
                        } else {
                            delay = time;
                        }
                        n++;
                    } else if (timingFuncs.contains(str.trim())) {
                        if (str.trim().equals("linear")) {
                            timingFunction = Transition.TimingFunction.LINEAR;
                        } else if (str.trim().equals("ease-in")) {
                            timingFunction = Transition.TimingFunction.EASE_IN;
                        } else if (str.trim().equals("ease-out")) {
                            timingFunction = Transition.TimingFunction.EASE_OUT;
                        } else if (str.trim().equals("ease")) {
                            timingFunction = Transition.TimingFunction.EASE;
                        } else if (str.trim().equals("bounce")) {
                            timingFunction = Transition.TimingFunction.BOUNCE;
                        }
                    } else if (str.matches("[a-z-]+[a-z]")) {
                        property = str;
                    }
                }

                if (duration > 0 && !property.isEmpty()) {
                    TransitionInfo trans = new TransitionInfo(this, property, duration, timingFunction, delay);
                    transitions.put(property, trans);
                }
            }
            return;
        }

        String ch = value.substring(0, 1);
        String n = "";
        int index = 0;
        if (!ch.matches("[0-9]")) return;
        while (ch.matches("[0-9.]")) {
            n += ch;
            index++;
            ch = value.substring(index, index+1);
        }
        String u = value.substring(index);
        if (!u.matches("px|%|em")) return;
        int units = 0;
        if (u.equals("%")) units = Units.percent;
        else if (u.equals("em")) units = Units.em;
        else if (u.equals("rem")) units = Units.rem;
        setProp(prop, Double.parseDouble(n), units);
    }

    public void setProp(String prop, double val, int units) {
        if (prop.equals("left")) {
            setLeft(val, units);
            return;
        }
        if (prop.equals("right")) {
            setRight(val, units);
            return;
        }
        if (prop.equals("top")) {
            setTop(val, units);
            return;
        }
        if (prop.equals("bottom")) {
            setBottom(val, units);
            return;
        }
        if (prop.matches("margin(-left|-right|-top|-bottom)?")) {
            int value = getValueInPixels(val, units);
            if (prop.matches("margin(-left)?")) {
                margins[3] = value;
            }
            if (prop.matches("margin(-right)?")) {
                margins[1] = value;
            }
            if (prop.matches("margin(-top)?")) {
                margins[0] = value;
            }
            if (prop.matches("margin(-bottom)?")) {
                margins[2] = value;
            }
            Block b = doIncrementLayout();
            if (b != null && !no_draw) b.forceRepaint();
            return;
        }
        if (prop.matches("border(-left|-right|-top|-bottom)-width")) {
            if (prop.contains("-left")) this.borderWidth[3] = (int)val;
            if (prop.contains("-right")) this.borderWidth[1] = (int)val;
            if (prop.contains("-top")) this.borderWidth[0] = (int)val;
            if (prop.contains("-bottom")) this.borderWidth[2] = (int)val;
            if (WebDocument.scale_borders) {
                for (int i = 0; i < 4; i++) {
                    this.borderWidth[i] = (int)Math.round(borderWidth[i]*ratio);
                }
            }
            this.border = new RoundedBorder(this, borderWidth, arc, borderColor, borderType);
            if (!no_draw) forceRepaint();
            return;
        }
    }

    public void setLinearGradientFromCSS(String value) {
        if (!value.matches("(-[a-z]+-)?linear-gradient\\(.*\\)")) return;
        value = value.substring(value.indexOf("(")+1, value.length()-1);
        int pos = 0;
        int braces = 0;
        String part = "";
        Vector<String> parts = new Vector<String>();
        while (pos < value.length()) {
            char ch = value.charAt(pos);
            if (ch == '(') braces++;
            if (ch == ')') braces--;
            if (braces == 0 && ch == ',' && !part.isEmpty()) {
                parts.add(part.trim());
                part = "";
            } else {
                part += ch;
            }
            pos++;
        }
        if (!part.isEmpty()) {
            parts.add(part.trim());
        }
        if (parts.size() < 3) return;

        double angle = 0;
        
        if (!parts.get(0).endsWith("deg")) {
            String s = parts.get(0);
            if (!(s.matches("((from|to) )?(left|right|top|bottom)"))) {
                return;
            }
            if (s.equals("to bottom") || s.matches("(from )?top")) {
                parts.set(0, "0deg");
                angle = 0;
            }
            else if (s.equals("to right") || s.matches("(from )?left")) {
                parts.set(0, "90deg");
                angle = 90;
            }
            else if (s.equals("to bottom") || s.matches("(from )?top")) {
                parts.set(0, "180deg");
                angle = 180;
            }
            else if (s.equals("to bottom") || s.matches("(from )?top")) {
                parts.set(0, "180deg");
                angle = 180;
            }
            else if (s.equals("to left") || s.matches("(from )?right")) {
                parts.set(0, "270deg");
                angle = 270;
            }
        } else if (parts.get(0).endsWith("deg")) {
            angle = Double.parseDouble(parts.get(0).substring(0, parts.get(0).length()-3));
        }

        Vector<Color> cols = new Vector<Color>();
        Vector<String> positions = new Vector<String>();

        for (int i = 1; i < parts.size(); i++) {
            String[] s = parts.get(i).split("\\s+");
            Color col = null;
            col = parseColor(s[0]);
            if (col == null) {
                col = parseColor(s[1]);
                positions.add(s[0]);
            } else {
                positions.add(s.length > 1 ? s[1] : (i > 1 ? "0px" : "100%"));
            }
            if (col == null) return;
            cols.add(col);
        }

        setLinearGradientWithUnits(cols, positions, angle);
    }

    public void setRadialGradientFromCSS(String value) {
        if (!value.matches("(-[a-z]+-)?radial-gradient\\(.*\\)")) return;
        value = value.substring(value.indexOf("(")+1, value.length()-1);
        int pos = 0;
        int braces = 0;
        String part = "";
        Vector<String> parts = new Vector<String>();
        while (pos < value.length()) {
            char ch = value.charAt(pos);
            if (ch == '(') braces++;
            if (ch == ')') braces--;
            if (braces == 0 && ch == ',' && !part.isEmpty()) {
                parts.add(part.trim());
                part = "";
            } else {
                part += ch;
            }
            pos++;
        }
        if (!part.isEmpty()) {
            parts.add(part.trim());
        }
        if (parts.size() < 2) return;

        Block b = parent != null ? parent : (document != null ? document.root : null);
        Block b_root = document != null ? document.root : null;
        double fontSize = b != null ? b.fontSize : 16 * ratio;
        double rootFontSize = b_root != null ? b_root.fontSize : 16 * ratio;

        int width = viewport_width > 0 ? viewport_width : this.width;
        int height = viewport_height > 0 ? viewport_height : this.height;

        double r = Math.sqrt(width * width + height * height);

        int cx = width > 0 ? width / 2 : 0;
        int cy = height > 0 ? height / 2 : 0;

        double dx = width > 0 ? (width >= height ? r : width) : 0;
        double dy = height > 0 ? (width <= height ? r : height) : 0;

        String mode = "farthest-corner";

        int count = 0;

        if (parts.get(0).equals("circle")) {
            dx = dy = Math.min(dx, dy);
        } else {

            String[] p = parts.get(0).split("\\s+");

            for (int i = 0; i < p.length; i++) {

                if (p[i].matches("[0-9.]+[0-9]+(px|%|em)")) {
                    if (i > 0 && !p[i-1].equals("at")) {
                        int[] val = parseValueStringToArray(p[i].trim());
                        val[0] = Math.max(0, Math.min(width, val[0]));
                        if (val[1] == Units.percent) {
                            val[0] *= (double) width / 100;
                        } else if (val[1] == Units.em) {
                            val[0] *= fontSize;
                        } else if (val[1] == Units.rem) {
                            val[0] *= rootFontSize;
                        } else {
                            val[0] *= ratio;
                        }
                        dx = val[0];

                        if (i < p.length-1 && p[i+1].matches("[0-9.]+[0-9]+(px|%|em)")) {
                            i++;
                            val = parseValueStringToArray(p[i].trim());
                            val[0] = Math.max(0, Math.min(width, val[0]));
                            if (val[1] == Units.percent) {
                                val[0] *= (double) width / 100;
                            } else if (val[1] == Units.em) {
                                val[0] *= fontSize;
                            } else if (val[1] == Units.rem) {
                                val[0] *= rootFontSize;
                            } else {
                                val[0] *= ratio;
                            }
                            dy = val[0];
                        }
                    } else if (i > 0 && p[i-1].equals("at")) {
                        int[] val = parseValueStringToArray(p[i].trim());
                        val[0] = Math.max(0, Math.min(width, val[0]));
                        if (val[1] == Units.percent) {
                            val[0] *= (double) width / 100;
                        } else if (val[1] == Units.em) {
                            val[0] *= fontSize;
                        } else if (val[1] == Units.rem) {
                            val[0] *= rootFontSize;
                        } else {
                            val[0] *= ratio;
                        }
                        cx = (int) Math.round(val[0]);

                        if (i < p.length-1 && p[i+1].matches("[0-9.]+[0-9]+(px|%|em)")) {
                            i++;
                            val = parseValueStringToArray(p[i].trim());
                            val[0] = Math.max(0, Math.min(width, val[0]));
                            if (val[1] == Units.percent) {
                                val[0] *= (double) width / 100;
                            } else if (val[1] == Units.em) {
                                val[0] *= fontSize;
                            } else if (val[1] == Units.rem) {
                                val[0] *= rootFontSize;
                            } else {
                                val[0] *= ratio;
                            }
                            cy = (int) Math.round(val[0]);
                        }
                    }
                    count++;
                } else if (i > 0 && p[i-1].equals("at")) {
                    if (p[i].equals("top")) {
                        cy = 0;
                    }
                    else if (p[i].equals("top-left")) {
                        cx = 0;
                        cy = 0;
                    }
                    else if (p[i].equals("top-right")) {
                        cx = width;
                        cy = 0;
                    }
                    else if (p[i].equals("bottom")) {
                        cy = height;
                    }
                    else if (p[i].equals("bottom-left")) {
                        cx = 0;
                        cy = height;
                    }
                    else if (p[i].equals("bottom-right")) {
                        cx = width;
                        cy = height;
                    }
                    else if (p[i].equals("left")) {
                        cx = 0;
                    }
                    else if (p[i].equals("right")) {
                        cx = width;
                    }
                    count++;
                } else if (p[i].matches("(closest|farthest)-(side|corner)")) {
                    mode = p[i];
                    count++;
                }
            }
        }

        if (count == 0) {
            parts.add(0, "circle");
        }

        if (mode.equals("farthest-corner")) {
            // north-east
            if (cx >= (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt(cx * cx + (height - cy) * (height - cy));
            }
            // south-east
            if (cx >= (double) width / 2 && cy > (double) height / 2) {
                dx = dy = Math.sqrt(cx * cx + cy * cy);
            }
            // north-west
            if (cx < (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt((width - cx) * (width - cx) + (height - cy) * (height - cy));
            }
            // south-west
            if (cx < (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt((width - cx) * (width - cx) + cy * cy);
            }
        } else if (mode.equals("closest-corner")) {
            // north-east
            if (cx >= (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt((width - cx) * (width - cx) + cy * cy);
            }
            // south-east
            if (cx >= (double) width / 2 && cy > (double) height / 2) {
                dx = dy = Math.sqrt((width - cx) * (width - cx) + (height - cy) * (height - cy));
            }
            // north-west
            if (cx < (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt(cx * cx + cy * cy);
            }
            // south-west
            if (cx < (double) width / 2 && cy <= (double) height / 2) {
                dx = dy = Math.sqrt(cx * cx + (height - cy) * (height - cy));
            }
        } else if (mode.equals("farthest-side")) {
            dx = dy = Math.max(cx, Math.max(width - cx, Math.max(cy, height - cy)));
        } else if (mode.equals("closest-side")) {
            dx = dy = Math.min(cx, Math.min(width - cx, Math.min(cy, height - cy)));
        }

        Vector<Color> cols = new Vector<Color>();
        Vector<String> positions = new Vector<String>();

        for (int i = 1; i < parts.size(); i++) {
            String[] s = parts.get(i).split("\\s+");
            Color col = null;
            col = parseColor(s[0]);
            if (col == null) {
                col = parseColor(s[1]);
                positions.add(s[0]);
            } else {
                positions.add(s.length > 1 ? s[1] : (i == 1 ? "0px" : "100%"));
            }
            if (col == null) return;
            cols.add(col);
        }

        setRadialGradientWithUnits(new int[] {cx, cy}, new double[] {dx, dy}, cols, positions);
    }

    public int[] parseValueStringToArray(String value) {
        if (value.matches("calc\\(.*\\)")) {
            return new int[] { (int) Math.round(calculateCssExpression(value.substring(5, value.length()-1))), Units.px };
        }
        if (value.matches("^([0-9.]+[0-9]|[0-9]+)(px|em|rem|%|v(w|h|min|max))$")) {
            String ch = value.substring(0, 1);
            String n = "";
            int index = 0;
            while (ch.matches("[0-9.]")) {
                n += ch;
                index++;
                ch = value.substring(index, index+1);
            }
            String u = value.substring(index);
            int val = (int)Math.round(Float.parseFloat(n));
            int units = u.equals("px") ? Units.px : (u.matches("em|rem") ? (u.equals("em") ? Units.em : Units.rem) : Units.percent);
            if (u.equals("vw")) units = Units.vw;
            else if (u.equals("vh")) units = Units.vh;
            else if (u.equals("vmin")) units = Units.vmin;
            else if (u.equals("vmax")) units = Units.vmax;
            return new int[] {val, units};
        }
        return null;
    }

    public double calculateCssExpression(String str) {
        int pos = 0;
        int state = 0;
        String token = "";
        Vector<String> tokens = new Vector<String>();
        while (pos < str.length()) {
            char ch = str.charAt(pos);
            if (java.lang.Character.isWhitespace(ch) || ch == '(' || ch == ')') {
                if (!token.isEmpty()) {
                    tokens.add(token);
                    token = "";
                }
                if (java.lang.Character.isWhitespace(ch)) {
                    pos++;
                }
                if (ch == '(') {
                    int level = 1;
                    int pos2 = pos+1;
                    while (pos2 < str.length() && level > 0) {
                        if (str.charAt(pos2) == '(') level++;
                        if (str.charAt(pos2) == ')') level--;
                        pos2++;
                    }
                    if (level == 0) {
                        double val = calculateCssExpression(str.substring(pos+1, pos2-1));
                        if (val < 0) {
                            return -1;
                        }
                        tokens.add(val + "px");
                    }
                    pos = pos2;
                }
                continue;
            }
            token += ch;
            if (pos == str.length()-1 && !token.isEmpty()) {
                tokens.add(token);
                token = "";
            }
            pos++;
        }
        return calc(tokens);
    }

    private double calc(Vector<String> tokens) {
        double result = -1;
        if (tokens.size() == 1) {
            return getValueInCssPixels(tokens.get(0));
        }
        Vector<Integer> p = new Vector<Integer>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.matches("[*/+-]") && (i == 0 || i == tokens.size()-1)) {
                return -1;
            }
            if (token.matches("[*/]")) {
                String leftOp = tokens.get(i-1);
                String rightOp = tokens.get(i+1);
                if (!leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?.*") || !rightOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?.*")) {
                    return -1;
                }
                if (leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?") && rightOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?")) {
                    p.add(2);
                } else {
                    p.add(3);
                }
            }
            else if (token.matches("[+-]")) {
                String leftOp = tokens.get(i-1);
                String rightOp = tokens.get(i+1);
                if (!leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?.*") || !rightOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?.*")) {
                    return -1;
                }
                p.add(1);
            }
            else {
                p.add(0);
            }
        }
        for (int j = 3; j >= 1; j--) {
            if (tokens.size() == 1) break;
            for (int i = 0; i < tokens.size(); i++) {
                if (p.get(i) == j) {
                    if (tokens.get(i).equals("*")) {
                        String leftOp = tokens.get(i-1);
                        String rightOp = tokens.get(i+1);
                        if (leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?") && rightOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?")) {
                            return -1;
                        }
                        double a = 0;
                        double b = 0;
                        if (leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?")) {
                            a = Double.parseDouble(leftOp);
                            b = getValueInCssPixels(rightOp);
                        } else if (leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?")) {
                            a = Double.parseDouble(rightOp);
                            b = getValueInCssPixels(leftOp);
                        }
                        tokens.add(i-1, (a * b) + "px");
                        p.add(i-1, 0);
                        for (int k = 0; k < 3; k++) {
                            tokens.remove(i);
                            p.remove(i);
                        }
                    } else if (tokens.get(i).equals("/")) {
                        String leftOp = tokens.get(i-1);
                        String rightOp = tokens.get(i+1);
                        if (leftOp.matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?")) {
                            return -1;
                        }
                        double a = getValueInCssPixels(leftOp);
                        double b = Double.parseDouble(rightOp);
                        tokens.add(i-1, (a / b) + "px");
                        p.add(i-1, 0);
                        for (int k = 0; k < 3; k++) {
                            tokens.remove(i);
                            p.remove(i);
                        }
                    } else if (tokens.get(i).equals("+") || tokens.get(i).equals("-")) {
                        String leftOp = tokens.get(i-1);
                        String rightOp = tokens.get(i+1);
                        double a = getValueInCssPixels(leftOp);
                        double b = getValueInCssPixels(rightOp);
                        double res = tokens.get(i).equals("+") ? a + b : a - b;
                        tokens.add(i-1, res + "px");
                        p.add(i-1, 0);
                        for (int k = 0; k < 3; k++) {
                            tokens.remove(i);
                            p.remove(i);
                        }
                    }
                }
            }
        }
        if (tokens.size() > 1) {
            return -1;
        }
        if (tokens.get(0).matches("([0-9]|[1-9][0-9]*)(\\.[0-9]+)?px")) {
            result = Double.parseDouble(tokens.get(0).substring(0, tokens.get(0).length()-2));
        }
        return result;
    }

    public class CssLength {

        public CssLength(double value, int unit) {
            this.value = value;
            this.unit = unit;
        }

        public double value;
        public int unit;
    }

    public CssLength parseValueString(String value) {
        if (value.matches("^([0-9.]+[0-9]|[0-9]+)(px|em|rem|%|v(w|h|min|max))$")) {
            String ch = value.substring(0, 1);
            String n = "";
            int index = 0;
            while (ch.matches("[0-9.]")) {
                n += ch;
                index++;
                ch = value.substring(index, index+1);
            }
            String u = value.substring(index);
            double val = Float.parseFloat(n);
            int units = u.equals("px") || u.isEmpty() ? Units.px : (u.matches("em|rem") ? (u.equals("em") ? Units.em : Units.rem) : Units.percent);
            if (u.equals("vw")) units = Units.vw;
            else if (u.equals("vh")) units = Units.vh;
            else if (u.equals("vmin")) units = Units.vmin;
            else if (u.equals("vmax")) units = Units.vmax;
            return new CssLength(val, units);
        }
        return null;
    }

    public int getValueInPixels(double value, int units) {
        int val = (int)Math.round(value * ratio);
        if (units == Units.em) {
            Block b = parent != null ? parent : (document != null ? document.root : null);
            double size = (b != null ? b.fontSize : 16 * ratio);
            val = (int)Math.round(value * size);
        }
        else if (units == Units.rem) {
            Block b = document != null ? document.root : null;
            double size = (b != null ? b.fontSize : 16 * ratio);
            val = (int)Math.round(value * size);
        }
        else if (units == Units.percent) {
            val = (int)Math.round(value / 100 * (parent != null && positioning != Position.FIXED ? parent.viewport_width :
                document.width - document.borderSize * 2));
        }
        else if (units == Units.vw) {
            val = (int)Math.round(value / 100 * (document.width - document.borderSize * 2));
        }
        else if (units == Units.vh) {
            val = (int)Math.round(value / 100 * (document.height - document.borderSize * 2));
        }
        else if (units == Units.vmin) {
            val = (int)Math.round(value / 100 * (Math.min(document.width, document.height) - document.borderSize * 2));
        }
        else if (units == Units.vmax) {
            val = (int)Math.round(value / 100 * (Math.max(document.width, document.height) - document.borderSize * 2));
        }

        return val;
    }

    public int getValueInPixels(String value, String units_list) {
        if (value.matches("calc\\(.*\\)")) {
            return (int) Math.round(calculateCssExpression(value.substring(5, value.length()-1)) * ratio);
        }
        String ch = value.substring(0, 1);
        String n = "";
        int index = 0;
        while (ch.matches("[0-9.]")) {
            n += ch;
            index++;
            ch = value.substring(index, index+1);
        }
        String u = value.substring(index);
        if (!u.matches(units_list)) return 0;

        List<String> units = Arrays.asList(new String[] { "px", "%", "em", "rem", "vw", "vh", "vmin", "vmax" });

        return getValueInPixels(Double.parseDouble(n), units.indexOf(u));
    }

    public double getValueInCssPixels(double value, int units) {
        double val = value;
        if (units == Units.em) {
            Block b = parent != null ? parent : (document != null ? document.root : null);
            double size = (b != null ? (double) b.fontSize / ratio : 16);
            val = (int)Math.round(value * size);
        }
        else if (units == Units.rem) {
            Block b = document != null ? document.root : null;
            double size = (b != null ? (double) b.fontSize / ratio : 16);
            val = (int)Math.round(value * size);
        }
        else if (units == Units.percent) {
            val = value / 100 * (parent != null && positioning != Position.FIXED ? (double) (parent.viewport_width - parent.paddings[3] - parent.paddings[1] - parent.borderWidth[3] - parent.borderWidth[1]) / ratio :
                (double) (document.width - document.borderSize * 2) / ratio);
        }
        else if (units == Units.vw) {
            val = value / 100 * (document.width - document.borderSize * 2) / ratio;
        }
        else if (units == Units.vh) {
            val = value / 100 * (document.height - document.borderSize * 2) / ratio;
        }
        else if (units == Units.vmin) {
            val = value / 100 * (Math.min(document.width, document.height) - document.borderSize * 2) / ratio;
        }
        else if (units == Units.vmax) {
            val = value / 100 * (Math.max(document.width, document.height) - document.borderSize * 2) / ratio;
        }

        return val;
    }

    public double getValueInCssPixels(String value) {
        if (value.matches("calc\\(.*\\)")) {
            return (int) Math.round(calculateCssExpression(value.substring(5, value.length()-1)));
        }
        String ch = value.substring(0, 1);
        String n = "";
        int index = 0;
        while (ch.matches("[0-9.]")) {
            n += ch;
            index++;
            ch = value.substring(index, index+1);
        }
        String u = value.substring(index);

        List<String> units = Arrays.asList(new String[] { "px", "%", "em", "rem", "vw", "vh", "vmin", "vmax" });

        return getValueInCssPixels(Double.parseDouble(n), units.indexOf(u));
    }

    public int getValueInPixels(String value) {
        return getValueInPixels(value, "px|em|rem|%|v(w|h|min|max)");
    }

    public static int[] parseValueStringToArrayStatic(String value) {
        if (value.matches("^([0-9.]+[0-9]|[0-9]+)(px|em|rem|%|v(w|h|min|max))$")) {
            String ch = value.substring(0, 1);
            String n = "";
            int index = 0;
            while (ch.matches("[0-9.]")) {
                n += ch;
                index++;
                ch = value.substring(index, index+1);
            }
            String u = value.substring(index);
            int val = (int)Math.round(Float.parseFloat(n));
            int units = u.equals("px") ? Units.px : (u.matches("em|rem") ? (u.equals("em") ? Units.em : Units.rem) : Units.percent);
            if (u.equals("vw")) units = Units.vw;
            else if (u.equals("vh")) units = Units.vh;
            else if (u.equals("vmin")) units = Units.vmin;
            else if (u.equals("vmax")) units = Units.vmax;
            return new int[] {val, units};
        }
        return null;
    }

    public void setLeft(double val, int units) {
        auto_left = false;
        if (positioning == Position.FIXED && units == Units.percent) {
            left = (int)Math.round(val / 100 * (document.root.viewport_width - document.borderSize * 2));
        } else {
            left = getValueInPixels(val, units);
        }
        if (units != Units.px) {
            dimensions.put("left", new DynamicValue(val, units, null));
        }
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
                forceRepaint();
            }
            else if (positioning == Block.Position.ABSOLUTE || positioning == Block.Position.FIXED) {
                if (parent != null) {
                    setX(parent.borderWidth[3] + margins[3] + left);
                } else {
                    setX(left);
                }
            }
            while (b.parent != null) b = b.parent;
            b.repaint();
        }
    }

    public void setRight(double val, int units) {
        auto_right = false;
        if (positioning == Position.FIXED && units == Units.percent) {
            right = (int)Math.round(val / 100 * (document.root.viewport_width - document.borderSize * 2));
        } else {
            right = getValueInPixels(val, units);
        }
        if (units != Units.px) {
            dimensions.put("right", new DynamicValue(val, units, null));
        }
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE || positioning == Block.Position.FIXED) {
                if (parent != null) {
                    setX(parent.borderWidth[3] + margins[3] + parent.width - right - width);
                } else {
                    setX(document.width - document.borderSize * 2 - right - width);
                }
            }
            forceRepaint();
            while (b.parent != null) b = b.parent;
            b.repaint();
        }
    }

    public void setTop(double val, int units) {
        auto_top = false;
        if (positioning == Position.FIXED && units == Units.percent) {
            top = (int)Math.round(val / 100 * (document.root.viewport_height - document.borderSize * 2));
        } else {
            top = getValueInPixels(val, units);
        }
        if (units != Units.px) {
            dimensions.put("top", new DynamicValue(val, units, null));
        }
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE || positioning == Block.Position.FIXED) {
                if (parent != null) {
                    setY(parent.borderWidth[0] + margins[0] + top);
                } else {
                    setY(top);
                }
            }
            forceRepaint();
            while (b.parent != null) b = b.parent;
            b.repaint();
        }
    }

    public void setBottom(double val, int units) {
        auto_bottom = false;
        if (positioning == Position.FIXED && units == Units.percent) {
            bottom = (int)Math.round(val / 100 * (document.root.viewport_height - document.borderSize * 2));
        } else {
            bottom = getValueInPixels(val, units);
        }
        if (units != Units.px) {
            dimensions.put("bottom", new DynamicValue(val, units, null));
        }
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE || positioning == Block.Position.FIXED) {
                if (parent != null) {
                    setY(parent.borderWidth[0] + margins[0] + parent.height - bottom - height);
                } else {
                    setY(document.height - document.borderSize * 2 - bottom - height);
                }
            }
            forceRepaint();
            while (b.parent != null) b = b.parent;
            b.repaint();
        }
    }

    public void addText(String text) {
        addText(text, children.size());
    }

    public void addText(String text, int pos) {
        Block tb = new Block(document, this, 0, 0, 0, 0, Color.BLACK);
        htmlparser.Node n = new htmlparser.Node(3);
        n.nodeValue = text;
        tb.node = n;
        tb.display_type = 2;
        tb.textContent = text;
        tb.type = NodeTypes.TEXT;
        tb.fontFamily = fontFamily;
        tb.text_bold = text_bold;
        tb.text_italic = text_italic;
        tb.text_underline = text_underline;
        tb.text_strikethrough = text_strikethrough;
        if (pos < children.size()) {
            children.add(pos, tb);
        } else {
            children.add(tb);
        }
        if (document.prevent_mixed_content) {
            normalizeContent();
        }
        if (document != null && document.ready) {
            Block block = this;
            if ((display_type == Display.INLINE_BLOCK || display_type == Display.INLINE) && parent != null) {
                boolean val = document.fast_update;
                document.fast_update = false;
                block = parent.doIncrementLayout();
                document.fast_update = val;
            } else {
                block = doIncrementLayout();
            }
            document.root.setNeedRestoreSelection(true);
            if (block != null) block.forceRepaint();
            document.root.setNeedRestoreSelection(false);
            document.repaint();
        }
    }

    public void addElement(Block b) {
        addElement(b, children.size(), false);
    }

    public void addElement(Block b, boolean preserve_style) {
        addElement(b, children.size(), preserve_style);
    }

    public void addElement(Block b, int pos) {
        addElement(b, pos, false);
    }

    public void addElement(Block b, int pos, boolean preserve_style) {
        type = NodeTypes.ELEMENT;
        if (children.contains(b)) {
            System.err.println("Duplicate block insert");
            return;
        }
        if (pos < children.size()) {
            children.add(pos, b);
        } else {
            children.add(b);
        }
        b.addToContainer(pos);

        b.pos = getComponentCount()-1;

        Block root = this;
        while (root.parent != null) {
            if (root.hasParentLink || root.href != null) {
                b.hasParentLink = true;
            }
            root = root.parent;
        }
        if (document != null && (root.width <= 0 || root.height <= 0)) {
            root = document.root;
        }
        b.setBounds(0, 0, root.width, root.height);

        b.document = document;
        if (b.childDocument != null) {
            b.document.child_documents.add(b.childDocument);
        }

        b.parent = this;
        b.alpha = alpha;
        if (!preserve_style) {
            b.color = color;
            b.fontSize = fontSize;
            b.fontFamily = fontFamily;
            b.text_align = text_align;
            b.text_bold = text_bold;
            b.text_italic = text_italic;
            b.text_underline = text_underline;
            b.text_strikethrough = text_strikethrough;
            b.linksUnderlineMode = linksUnderlineMode;
            b.textShadowColor = textShadowColor;
            b.textShadowOffset = textShadowOffset;
        }
        if (document != null && document.prevent_mixed_content) {
            normalizeContent();
        }
        if (b.inputType != Input.NONE) {
            b.addToClosestForm();
        }
        addToLayout(b, pos, root);
    }

    private void addToLayout(Block d, int pos, Block root) {
        if (document != null && document.ready && layouter != null && parent != null) {
            if (d.display_type == Display.BLOCK && d.auto_width && d.parent != null && d.width != d.parent.viewport_width - d.parent.borderWidth[3] - d.parent.borderWidth[1] - d.parent.paddings[3] - d.parent.paddings[1] - d.margins[3] - d.margins[1]) {
                d.setWidth(-1, false);
            }
            Block block = this;
            if (d.display_type == Display.INLINE) {
                Line line = layouter.last_line;
                boolean found = false;
                Block last_block = null;

                Block ref_block = pos < children.size()-1 ? children.get(pos+1) : children.get(pos);
                if (ref_block.parts.size() > 0) {
                    ref_block = ref_block.parts.get(0);
                }

                for (int i = 0; i < lines.size(); i++) {
                    Vector<Drawable> elements = (Vector<Drawable>) lines.get(i).elements.clone();
                    for (int j = 0; j < elements.size(); j++) {

                        if (lines.get(i).elements.get(j) == ref_block) {
                            layouter.last_line = lines.get(i);
                            for (int k = i+1; k < lines.size(); k++) {
                                lines.remove(k--);
                            }
                            for (int k = j; k < lines.get(i).elements.size(); k++) {
                                Block b0 = (Block) lines.get(i).elements.get(k);
                                lines.get(i).elements.remove(k--);
                                lines.get(i).cur_pos -= b0.width + b0.margins[3] + b0.margins[1];
                            }
                            layouter.addBlock(d);

                            found = true;
                            if (pos < children.size()-1) j--;
                            continue;
                        }
                        else if (found) {
                            Drawable el = elements.get(j);
                            if (el instanceof Block) {
                                Block b0 = (Block)el;
                                if (b0.original != null) {
                                    b0 = b0.original;
                                }
                                if (b0 == last_block) {
                                    continue;
                                }
                                last_block = b0;
                                boolean val = root.no_layout;
                                b0.no_layout = true;
                                layouter.addBlock(b0);
                                b0.no_layout = val;
                            }
                        }
                    }

                }
                if (pos == children.size()-1) {
                    layouter.addBlock(d);
                }

                sortBlocks();
            } else {
                d.performLayout();
                block = doIncrementLayout();
            }
            document.root.setNeedRestoreSelection(true);
            block.forceRepaint();
            document.root.setNeedRestoreSelection(false);
            document.repaint();
        }
    }

    public void normalizeContent() {
        boolean text = false, elements = false;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).type == NodeTypes.TEXT) text = true;
            else elements = true;
            if (text && elements) break;
        }
        if (text && elements) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).type == NodeTypes.TEXT) {
                    Block b = new Block(document, parent, -1, -1, 0, 0, Color.BLACK);
                    b.display_type = Display.INLINE;
                    b.node = children.get(i).node;
                    b.addText(children.get(i).textContent);
                    b.children.get(0).node = b.node;
                    children.remove(i);
                    addElement(b, i, false);
                }
            }
        }
    }

    public void addToClosestForm() {
        Block b = this;
        while (b != null && b.form == null) {
            b = b.parent;
        }
        if (b != null) {
            if (!b.form.inputs.contains(this)) {
                b.form.inputs.add(this);
            }
        }
    }

    public void removeFromClosestForm() {
        Block b = this;
        while (b != null && b.form == null) {
            b = b.parent;
        }
        if (b != null) {
            b.form.inputs.remove(this);
        }
    }

    public void addChildDocument(WebDocument d) {
        removeAllElements();
        childDocument = d;
        if (document != null) {
            document.child_documents.add(d);
        }
        d.parent_document = this.document;
        d.parent_document_block = this;
        add(d);
    }

    public void removeChildDocument() {
        if (childDocument == null) return;
        childDocument.getRoot().removeAllElements();
        if (document != null) {
            document.child_documents.remove(childDocument);
        }
        childDocument.parent_document = null;
        childDocument.parent_document_block = null;
        childDocument = null;
        remove(childDocument);
    }

    public void removeElement(Block b) {
        if (b == null || !(b instanceof Block) || !children.contains(b)) {
            System.err.println("Child not found");
            return;
        }
        b.hasParentLink = false;
        b.removeTextLayers();
        children.remove(b);
        b.removeFromContainer();
        if (b.inputType != Input.NONE) {
            b.removeFromClosestForm();
        }
        removeFromLayout(b);
        //d.flushBuffersRecursively();
        document.repaint();
    }

    private void addToContainer(int pos) {
        if (document != null && getParent() != document.root) {
            if (pos < document.root.getComponentCount()) {
                document.root.add(this, pos++);
            } else {
                document.root.add(this);
            }
        } else {
            for (Block part: parts) {
                if (pos < document.root.getComponentCount()) {
                    document.root.add(part, pos++);
                } else {
                    document.root.add(part);
                }
            }
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).type == NodeTypes.ELEMENT) {
                children.get(i).addToContainer(pos);
            }
        }
    }

    private void removeFromContainer() {
        if (getParent() == document.root) {
            document.root.remove(this);
        } else {
            for (Block part: parts) {
                document.root.remove(part);
            }
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).removeFromContainer();
        }
    }

    public void removeElement(int index) {
        removeElement(children.get(index));
    }

    public void removeAllElements() {
        for (int i = 0; i < children.size(); i++) {
            if (document != null) document.root.remove(children.get(i));
            if (children.get(i).text_layer != null) {
                children.get(i).text_layer.removeAll();
            }
            children.get(i).removeAll();
        }
        children.clear();
        if (document != null && document.ready) {
            performLayout();
            forceRepaint();
            document.repaint();
        }
    }

    private void removeFromLayout(Block d) {
        if (document != null) {
            if (d.display_type == Display.INLINE && d.parts.size() > 0 && d.parts.get(0).line != null) {
                Line line = d.parts.get(0).line;
                for (int j = line.elements.size()-1; j >= 0; j--) {
                    Drawable el = line.elements.get(j);
                    if (!(el instanceof Block) || !d.parts.contains((Block)el)) {
                        el.setX(!rtl ? el._getX() - d.parts.get(0).width : el._getX() + d.parts.get(0).width);
                    } else {
                        line.elements.remove(el);
                        if (el instanceof Block) {
                            document.root.remove((Block)el);
                        }
                        v.remove(el);
                        if (el == d.parts.get(0)) break;
                    }
                }
            }
            boolean val = document.fast_update;
            if (parent == null || line != line.parent.lines.lastElement()) document.fast_update = false;
            Block block = doIncrementLayout();
            if (block == null) return;
            document.fast_update = val;
            document.root.setNeedRestoreSelection(true);
            block.forceRepaint();
            document.root.setNeedRestoreSelection(false);
            document.repaint();
        }
    }

    public void applyHoverStyles() {
        if (builder != null && node != null) {
            node.states.add("hover");
            builder.applyStateStyles(this);
        }
    }

    public void applyFocusStyles() {
        if (builder != null && node != null) {
            node.states.add("focus");
            builder.applyStateStyles(this);
        }
    }

    public void applyActiveStyles() {
        if (builder != null && node != null) {
            node.states.add("active");
            builder.applyStateStyles(this);
        }
    }

    public void applyStateStyles() {
        if (builder != null && node != null) {
            //builder.applyStateStylesRecursive(document.root);
            builder.applyStateStylesRecursive(this);
        }
    }

    public void resetStyles() {
        if (builder != null && node != null) {
            //builder.resetStylesRecursive(document.root);
            builder.resetStylesRecursive(this);
        }
    }

    public void setHref(String href, boolean force) {
        this.href = href;
        if (!this.hasParentLink && (href != null || force)) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).hasParentLink = true;
                children.get(i).setHref("", true);
            }
        } else if (!this.hasParentLink) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).hasParentLink = false;
                children.get(i).setHref(null);
            }
        }
    }

    public void setHref(String href) {
        setHref(href, false);
    }

    public void setAlpha(float value) {
        alpha = (parent != null ? parent.alpha : 1.0f) * value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setAlpha(children.get(i).alpha);
        }
        forceRepaint();
    }

    @Override
    public Font getFont() {
        int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font f = new Font(fontFamily, style, fontSize);
        if (text_underline || text_strikethrough) {
            Map attributes = f.getAttributes();
            if (text_underline) attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            if (text_strikethrough) attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            f.deriveFont(attributes);
        }
        return f;
    }

    public Vector<Block> getChildren() {
        return children;
    }

    public Layouter getLayouter() {
        return layouter;
    }

    public int getContentMaxHeight() {
        return content_y_max;
    }

    @Override
    public void setLine(Line l) {
        this.line = l;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String[] types = new String[] {"block", "inline-block", "inline", "none", "table", "inline-table", "table-row", "table-cell", "flex", "inline-flex"};
        String s = "";
        s += type == NodeTypes.ELEMENT ? ("Element (Display: " + types[display_type] + ") ") : "TextNode ";
        s += "ID " + (id != null && id.length() > 0 ? id : "<none>") + " ";
        s +=  "(" + width + "x" + height + ")";
        if (textContent != null && textContent.length() > 0) {
            s += " content: \"" + textContent + "\"";
        }
        return s;
    }

    public String toStringRecursive(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "----";
        }
        result += toString() + "\n";

        for (int i = 0; i < children.size(); i++) {
            result += children.get(i).toStringRecursive(level+1);
        }

        return result;
    }

    public String toStringRecursive() {
        return toStringRecursive(0);
    }

    public void printTree() {
        System.out.println(toStringRecursive());
    }

    public void updateOnResize() {
        HashMap<String, String> rules = (HashMap<String, String>)rules_for_recalc.clone();
        if (parent != null && rules_for_recalc.size() > 0) {
            Set keys = rules.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                String str = (String)it.next();
                setProp(str, rules.get(str));
            }
        }

        performLayout();
        forceRepaintAll();

        if (parent == null) {
            this.getParent().repaint();
        }
    }

    public BufferedImage getBuffer() {
        return buffer;
    }

    public int[] getSelection() {
        return sel;
    }

    public void setSelection(int[] range) {
        sel = range;
    }

    private float platform = 6.1f;

    public String textContent;

    public double ratio = 1;

    public int[] margins = {0, 0, 0, 0};
    public int[] paddings = {0, 0, 0, 0};

    public int _x_;
    public int _y_;

    public int left = 0;
    public int top = 0;
    public int right = 0;
    public int bottom = 0;

    public int orig_width;
    public int orig_height;

    public int white_space = 0;

    public int left_units = 0;
    public int top_units = 0;
    public int right_units = 0;
    public int bottom_units = 0;

    public boolean auto_width = false;
    public boolean auto_height = true;
    public boolean auto_left = true;
    public boolean auto_right = true;
    public boolean auto_top = true;
    public boolean auto_bottom = true;
    public boolean auto_x_margin = false;
    public boolean auto_y_margin = false;

    public int flex_grow = 1;
    public int flex_shrink = 1;
    public int flex_basis = 0;
    public int flex_basis_mode = FlexBasis.AUTO;
    public int flex_wrap = WhiteSpace.NO_WRAP;
    public int flex_justify = TextAlign.ALIGN_LEFT;
    public int flex_align_content = FlexAlign.STRETCH;
    public int flex_align_items = FlexAlign.STRETCH;
    public int flex_direction = Direction.ROW;
    public int flex_gap = 0;

    public int display_type = 0;
    public int visibility = 0;
    public int float_type = 0;
    public int clear_type = 0;

    public int letter_spacing = 0;
    public int word_spacing = 0;
    private Color selection_color = new Color(0, 0, 196, 186);
    private Color selection_inactive_color = new Color(120, 120, 120, 186);
    private int[] sel = null;
    private volatile int from_x = -1;
    private volatile int from_y = -1;

    public int content_x_max = 0;
    public int content_y_min = Integer.MAX_VALUE;
    public int content_y_max = 0;

    public htmlparser.Node node;
    public bridge.Builder builder;


    public static class Input {
        public static final int NONE = 0;
        public static final int TEXT = 1;
        public static final int TEXTAREA = 2;
        public static final int BUTTON = 3;
        public static final int RADIO = 4;
        public static final int CHECKBOX = 5;
        public static final int SELECT = 6;
        public static final int FILE = 7;
    }

    public static class FloatType {
        public static final int NONE = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
    }

    public static class ClearType {
        public static final int NONE = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int BOTH = 3;
    }

    public static class Direction {
        public static final int ROW = 0;
        public static final int ROW_REVERSED = 1;
        public static final int COLUMN = 2;
        public static final int COLUMN_REVERSED = 3;
    }

    public static class FlexBasis {
        public static final int AUTO = 0;
        public static final int MIN_CONTENT = 1;
        public static final int MAX_CONTENT = 2;
        public static final int EXPLICIT = 3;
    }

    public static class FlexAlign {
        public static final int STRETCH = 0;
        public static final int FLEX_START = 1;
        public static final int FLEX_CENTER = 2;
        public static final int FLEX_END = 3;
    }

    public static class FlexJustify {
        public static final int START = 0;
        public static final int CENTER = 1;
        public static final int END = 2;
        public static final int SPACE_BETWEEN = 3;
        public static final int SPACE_AROUND = 4;
        public static final int SPACE_EVENLY = 5;
    }

    public static class TextAlign {
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_CENTER = 1;
        public static final int ALIGN_RIGHT = 2;
        public static final int ALIGN_JUSTIFY = 3;
    }

    public static class VerticalAlign {
        public static final int ALIGN_TOP = 0;
        public static final int ALIGN_MIDDLE = 1;
        public static final int ALIGN_BASELINE = 2;
        public static final int ALIGN_BOTTOM = 3;
    }

    public static class WhiteSpace {
        public static final int NORMAL = 0;
        public static final int WORD_BREAK = 1;
        public static final int PRE_WRAP = 2;
        public static final int NO_WRAP = 3;
    }

    public static class BackgroundRepeat {
        public static final int NONE = 0;
        public static final int REPEAT_X = 1;
        public static final int REPEAT_Y = 2;
        public static final int REPEAT_XY = 3;
    }

    public static class Overflow {
        public static final int VISIBLE = 0;
        public static final int HIDDEN = 1;
        public static final int SCROLL = 2;
    }

    public static class Position {
        public static final int STATIC = 0;
        public static final int RELATIVE = 1;
        public static final int ABSOLUTE = 2;
        public static final int FIXED = 3;
    }

    public static class Display {
        public static final int BLOCK = 0;
        public static final int INLINE_BLOCK = 1;
        public static final int INLINE = 2;
        public static final int NONE = 3;
        public static final int TABLE = 4;
        public static final int INLINE_TABLE = 5;
        public static final int TABLE_ROW = 6;
        public static final int TABLE_CELL = 7;
        public static final int FLEX = 8;
        public static final int INLINE_FLEX = 9;
    }

    public static class Visibility {
        public static final int VISIBLE = 0;
        public static final int HIDDEN = 1;
    }

    public static class Units {
        public static final int px = 0;
        public static final int percent = 1;
        public static final int em = 2;
        public static final int rem = 3;
        public static final int vw = 4;
        public static final int vh = 5;
        public static final int vmin = 6;
        public static final int vmax = 7;
    }

    public class DynamicValue {
        public DynamicValue(double value, int unit, String expr) {
            length = new CssLength(value, unit);
            expression = expr;
        }

        public CssLength length;
        public String expression;
    }

    public int max_width = -1;
    public int max_height = -1;

    public int min_width = -1;
    public int min_height = -1;

    public double max_width_percent = -1;
    public double max_height_percent = -1;

    public int width;
    public int height;
    public int[] borderWidth = {0, 0, 0, 0};
    public int borderRadius;
    public int[] arc = {0, 0, 0, 0};
    public Color color = Color.BLACK;
    public int[] borderType = {0, 0, 0, 0};
    public int borderClipMode = 0;
    public Color[] borderColor = {Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK};
    public String fontFamily = "Tahoma";
    public int fontSize = 14;

    public boolean text_bold = false;
    public boolean text_italic = false;
    public boolean text_underline = false;
    public boolean text_strikethrough = false;

    public Color textShadowColor = null;
    public double[] textShadowOffset = {1, 1};
    public int textShadowBlur = 0;

    public int text_align = 0;
    public int positioning = 0;
    public int overflow = 0;
    public int vertical_align = 2;
    public boolean sharp = false;
    public float alpha = 1.0f;

    public boolean isImage = false;
    public boolean isMedia = false;
    public String mediaSource = null;

    public Form form;
    public int inputType = 0;
    public String inputValue = "";
    public String defaultInputValue = "";
    public FormEntry formEntry;

    public String imgSrc = "";

    public boolean has_animation = false;
    private ImageFrame[] animation_frames;
    private int current_frame = 0;
    private long last_frame_displayed = 0;

    public Cursor cursor;

    public int colspan = 1;
    public int rowspan = 1;

    public int bg_clip_x = -1;
    public int bg_clip_y = -1;

    public int shadow_x = 0;
    public int shadow_y = 0;
    public int shadow_size = 0;
    public int shadow_blur = 0;
    public Color shadow_color;

    public int zIndex = 0;
    private boolean zIndexAuto = true;

    public boolean select_enabled = true;

    public String href;
    
    public Color linkColor = Color.BLUE;
    public int linksUnderlineMode = 0;

    public boolean hasParentLink = false;
    public Color default_color = Color.BLACK;

    public int list_item_type = 0;
    private static Vector<String> list_types = new Vector<String>();

    protected BufferedImage buffer;
    protected BufferedImage text_shadow_buffer;

    public JPanel text_layer;
    public JPanel text_shadow_layer;

    public int viewport_width;
    public int viewport_height;

    public double aspect_ratio = -1;

    JScrollBar scrollbar_x;
    JScrollBar scrollbar_y;

    public int scroll_x = 0;
    public int scroll_y = 0;

    public int scroll_left = 0;
    public int scroll_top = 0;

    public int pref_size = 0;
    public int min_size = 0;

    private RoundedBorder border;

    private Layouter layouter;
    volatile TableLayout table;

    private LinkedList<Block> layer_list = new LinkedList<Block>();

    public int border_spacing = 2;
    public boolean border_collapse = false;

    public boolean transform = false;

    public boolean has_shadow = false;

    public HashMap<String, DynamicValue> dimensions = new HashMap<String, DynamicValue>();

    public HashMap<String, String> rules_for_recalc;
    public LinkedHashMap<String, Object> originalStyles;
    public LinkedHashMap<String, String> cssStyles;

    public String selected_text;
    public Line line;
    public Block parent;
    public WebDocument document;
    public WebDocument childDocument;

    private long last_click = 0;

    public int type = 0;

    public int cut = 0;

    public int pos = 0;
    public Vector<Block> parts = new Vector<Block>();
    public Block original;

    public boolean special = false;

    public String id = "";

    public static class Cut {
        public static final int NONE = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int BOTH = 3;
    }

    public static class NodeTypes {
        public static final int ELEMENT = 0;
        public static final int TEXT = 1;
    }

    Vector<Block> children = new Vector<Block>();
    Vector<Line> lines = new Vector<Line>();

    public Block beforePseudoElement;
    public Block afterPseudoElement;

    public void setBeforePseudoElement(Block block) {
        if (beforePseudoElement != null && block == null) {
            document.root.remove(beforePseudoElement);
            for (int i = 0; i < beforePseudoElement.parts.size(); i++) {
                document.root.remove(beforePseudoElement.parts.get(i));
            }
        }
        beforePseudoElement = block;
    }

    public void setAfterPseudoElement(Block block) {
        if (afterPseudoElement != null && block == null) {
            document.root.remove(afterPseudoElement);
            for (int i = 0; i < afterPseudoElement.parts.size(); i++) {
                document.root.remove(afterPseudoElement.parts.get(i));
            }
        }
        afterPseudoElement = block;
    }

    public boolean isPseudoElement() {
        Block b = original != null ? original : this;
        return b.parent != null && (b == b.parent.beforePseudoElement || b == b.parent.afterPseudoElement);
    }

    public void replaceWith(Block b) {
        b.parent = parent;

        boolean isRoot = (this == document.root);

        Block old_root = document.root;

        document.ready = false;

        if (isRoot) {
            document.setRoot(b);
        }

        Vector<Block> blocks = (Vector<Block>) children.clone();

        for (int i = 0; i < blocks.size(); i++) {
            Block child = blocks.get(i);
            old_root.remove(child);
            b.addElement(child, true);
        }

        if (parent != null) {
            for (int i = 0; i < parent.children.size(); i++) {
                if (parent.children.get(i) == this) {
                    old_root.remove(parent.children.get(i));
                    parent.addElement(b, i);
                    break;
                }
            }
        }

        if (scrollbar_x != null) removeScrollbarX();
        if (scrollbar_y != null) removeScrollbarY();

        if (isRoot) {
            document.ready = true;
            b.performLayout();
            b.forceRepaint();
            getParent().repaint();
            return;
        }

        int index = document.root.getComponentZOrder(this);
        if (parts.size() > 0) {
            index = document.root.getComponentZOrder(parts.get(0));
        }

        document.root.remove(this);
        for (int i = 0; i < parts.size(); i++) {
            document.root.remove(parts.get(i));
        }
        document.root.add(b, index);

        Block block = b;
        while (block.display_type == Display.INLINE) {
            block = block.parent;
        }

        document.ready = true;

        if (block != null) {
            Block b0 = block.doIncrementLayout();
            block.setNeedRestoreSelection(true);
            b0.forceRepaint();
        } else {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
        document.repaint();
    }

    public void replaceSubtreeWith(Block b) {
        removeAllElements();
        b.parent = parent;

        if (scrollbar_x != null) removeScrollbarX();
        if (scrollbar_y != null) removeScrollbarY();

        if (b.type != Block.NodeTypes.ELEMENT) {
            b.parent.children.clear();
        }
        if (document == null) document = b.document;
        document.processSubtree(b);
    }

    @Override
    public Block clone() {
        Block b = new Block(this.document);

        b.parent = this.parent;
        b.display_type = this.display_type;
        b.positioning = this.positioning;

        b.type = this.type;
        b.textContent = this.textContent;
        b.isImage = isImage;

        b._x_ = this._x_;
        b._y_ = this._y_;

        b.width = this.width;
        b.height = this.height;
        b.orig_width = orig_width;

        b.min_width = this.min_width;
        b.min_height = this.min_height;
        b.max_width = this.max_width;
        b.max_height = this.max_height;

        b.viewport_width = viewport_width;
        b.viewport_height = viewport_height;

        b.left = this.left;
        b.right = this.right;
        b.top = this.top;
        b.bottom = this.bottom;

        b.aspect_ratio = aspect_ratio;

        b.href = href;
        b.hasParentLink = hasParentLink;
        b.linksUnderlineMode = linksUnderlineMode;

        b.fontSize = this.fontSize;
        b.fontFamily = this.fontFamily;
        b.text_bold = this.text_bold;
        b.text_italic = this.text_italic;
        b.text_underline = this.text_underline;
        b.text_strikethrough = this.text_strikethrough;

        b.textShadowColor = this.textShadowColor;
        b.textShadowOffset = this.textShadowOffset;
        b.textShadowBlur = this.textShadowBlur;

        b.letter_spacing = this.letter_spacing;
        b.word_spacing = this.word_spacing;

        b.vertical_align = this.vertical_align;

        b.border = this.border;
        b.borderWidth = new int[4];
        b.borderWidth[0] = borderWidth[0];
        b.borderWidth[1] = borderWidth[1];
        b.borderWidth[2] = borderWidth[2];
        b.borderWidth[3] = borderWidth[3];
        b.borderColor = new Color[4];
        b.borderColor[0] = borderColor[0];
        b.borderColor[1] = borderColor[1];
        b.borderColor[2] = borderColor[2];
        b.borderColor[3] = borderColor[3];
        b.borderRadius = borderRadius;

        b.arc = new int[4];
        for (int i = 0; i < 4; i++) {
            b.arc[i] = arc[i];
        }

        b.borderClipMode = borderClipMode;

        b.border = new RoundedBorder(b, b.borderWidth, b.arc, b.borderColor, b.borderType);

        b.background = background;
        if (background != null) {
            b.has_animation = has_animation;
            b.animation_frames = animation_frames;
        }
        b.color = this.color;
        b.linkColor = linkColor;
        b.select_enabled = select_enabled;
        b.cursor = cursor;

        b.margins = new int[4];
        b.margins[0] = margins[0];
        b.margins[1] = margins[1];
        b.margins[2] = margins[2];
        b.margins[3] = margins[3];

        b.paddings = new int[4];
        b.paddings[0] = paddings[0];
        b.paddings[1] = paddings[1];
        b.paddings[2] = paddings[2];
        b.paddings[3] = paddings[3];

        b.auto_width = this.auto_width;
        b.auto_height = this.auto_height;
        b.auto_x_margin = this.auto_x_margin;
        b.auto_y_margin = this.auto_y_margin;

        b.flex_grow = flex_grow;
        b.flex_shrink = flex_shrink;
        b.flex_basis = flex_basis;
        b.flex_basis_mode = flex_basis_mode;
        b.flex_wrap = flex_wrap;
        b.flex_justify = flex_justify;
        b.flex_direction = flex_direction;
        b.flex_align_content = flex_align_content;
        b.flex_align_items = flex_align_items;
        b.flex_gap = flex_gap;

        b.transform = transform;

        b.zIndex = this.zIndex;
        b.zIndexAuto = this.zIndexAuto;

        b.childDocument = this.childDocument;

        b.originalStyles = originalStyles;
        b.cssStyles = cssStyles;

        b.node = node;
        b.builder = builder;
        b.special = special;
        b.rtl = rtl;

        b.dimensions = dimensions;

        b.sel = sel;
        b.needToRestoreSelection = needToRestoreSelection;

        if (original == null) {
            b.children = cloneChildren();
            for (int i = 0; i < b.children.size(); i++) {
                b.children.get(i).parent = b;
            }
            b.original = this;
        } else {
            b.children = copyChildren();
            b.original = original;
        }

        return b;
    }

    public Vector<Block> cloneChildren() {
        Vector<Block> ch = new Vector<Block>();
        for (int i = 0; i < children.size(); i++) {
            ch.add(children.get(i).clone());
        }
        return ch;
    }

    public Vector<Block> copyChildren() {
        Vector<Block> ch = new Vector<Block>();
        for (int i = 0; i < children.size(); i++) {
            ch.add(children.get(i));
        }
        return ch;
    }

    @Override
    public int _getWidth() {
        return viewport_width;
    }

    @Override
    public int _getHeight() {
        return viewport_height;
    }

    private int isFullySelected(Rectangle sel, Rectangle el) {
        if (sel.contains(el)) return 1;
        if (sel.intersects(el)) return 0;
        return -1;
    }


    public void mouseClicked(MouseEvent e) {

        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }

        if (childDocument != null) {
            childDocument.getRoot().mouseClicked(e);
            return;
        }

        Block[] blocks = new Block[document.root.layer_list.size()];
        document.root.layer_list.toArray(blocks);

        if (beforePseudoElement != null) {
            for (int i = 0; i < beforePseudoElement.parts.size(); i++) {
                beforePseudoElement.parts.get(i).mouseClicked(e);
            }
        }

        if (!(children.size() > 0 && children.get(0).type == NodeTypes.TEXT)) {
            for (int i = 0; i < blocks.length; i++) {
                Block b = blocks[i].original != null ? blocks[i].original : blocks[i];
                if (children.contains(b)) {
                    //MouseEvent evt = new MouseEvent((Block)d, 0, 0, 0, e.getX() - b._x_, e.getY() - b._y_, 1, false);
                    blocks[i].mouseClicked(e);
                }
            }
        }

        if (afterPseudoElement != null) {
            for (int i = 0; i < afterPseudoElement.parts.size(); i++) {
                afterPseudoElement.parts.get(i).mouseClicked(e);
            }
        }

        boolean isSVG = getComponents().length == 1 && getComponents()[0] instanceof JSVGCanvas;

        long last = original == null ? last_click : original.last_click;

        if (isMouseInside(e.getX(), e.getY()) && !isPseudoElement() || isSVG && isMouseInside(_x_ + e.getX(), _y_ + e.getY())) {
            fireEventForNode(e, node, null, "click");
            if (System.currentTimeMillis() - last < 320) {
                fireEventForNode(e, node, null, "doubleClick");
            }
            if (node != null && document.eventsFired.get(node) != null) {
                document.eventsFired.get(node).remove("mouseDown");
                document.eventsFired.get(node).remove("mouseUp");
                document.eventsFired.get(node).remove("click");
                document.eventsFired.get(node).remove("doubleClick");
            }
        }
        
        if (isMouseInside(e.getX(), e.getY()) && href != null || hasParentLink) {
            if (document.active_block != null && document.active_block.node != null) {
                document.active_block.node.states.remove("active");
                document.active_block.resetStyles();
                document.active_block.applyStateStyles();
                document.active_block.applyStylesBatch();
            }
            
            node.states.add("active");

            if (inputType >= Input.RADIO && inputType <= Input.CHECKBOX) {
                if (node.getAttribute("name") != null && inputType == 4) {
                    Vector<Block> group = findBlocksByName(document.root, node.getAttribute("name"));
                    for (int i = 0; i < group.size(); i++) {
                        group.get(i).node.states.remove("checked");
                        if (group.get(i) == this) continue;
                        if (group.get(i).inputType >= Input.RADIO && group.get(i).inputType <= Input.CHECKBOX) {
                            Component[] c = group.get(i).getComponents();
                            if (c.length > 0 && c[0] instanceof JToggleButton) {
                                ((JToggleButton)c[0]).getModel().setSelected(false);
                            }
                            ((Block)c[i]).checked = false;
                        }
                    }
                }

                checked = inputType == Input.CHECKBOX ? !checked : true;
                if (checked) node.states.add("checked");
                else node.states.remove("checked");
            }
            if (inputType == Input.RADIO && getComponents().length > 0) {
                ((JToggleButton)getComponents()[0]).getModel().setSelected(true);
            }

            document.active_block = this;

            if (node != null && node.defaultPrevented) {
                node.defaultPrevented = false;
            } else if (href != null) {
                openBrowser(href);
                node.states.add("visited");
            } else {
                Block b = this.parent;
                while (b != null && b.href == null) {
                    b = b.parent;
                }
                if (b != null && b.href != null) {
                    openBrowser(b.href);
                    node.states.add("visited");
                }
            }

            document.active_block.resetStyles();
            document.active_block.applyStateStyles();

            return;
        }
        if (textRenderingMode == 0 && (text_layer == null || text_layer.getComponents().length == 0)) {
            return;
        }

        if (System.currentTimeMillis() - last < 320 && select_enabled) {
            int x = e.getX();
            int y = e.getY();
            if (textRenderingMode == 1) {
                if (!(children.size() > 0 && children.get(0).type == NodeTypes.TEXT)) {
                    last_click = System.currentTimeMillis();
                }
                for (int i = 0; i < lines.size(); i++) {
                    Line l = lines.get(i);
                    for (int j = 0; j < l.elements.size(); j++) {
                        Drawable d = l.elements.get(j);
                        if (d instanceof Character &&
                            x >= _x_ + d._getX() && x <= _x_ + d._getX() + d._getWidth() &&
                            y >= _y_ + ((Character)d).line.top + d._getY() && y <= _y_ + ((Character)d).line.getOffsetTop() + d._getY() + d._getHeight()) {
                            //System.err.println("Found char: " + ((Character)d).getText());
                            if (sel == null) {
                                sel = new int[2];
                            }
                            sel[0] = j; sel[1] = j;
                            selectCharacter((Character)l.elements.get(j));
                            while (sel[0] > 0 && !((Character)l.elements.get(sel[0]-1)).getText().matches("\\s+")) {
                                sel[0]--;
                                selectCharacter((Character)l.elements.get(sel[0]));
                                l.elements.get(sel[0]).selected(true);
                            }
                            while (sel[1] < l.elements.size()-1 && !((Character)l.elements.get(sel[1]+1)).getText().matches("\\s+")) {
                                sel[1]++;
                                selectCharacter((Character)l.elements.get(sel[1]));
                                l.elements.get(sel[1]).selected(true);
                            }
                            //System.err.println("From: " + sel[0] + ", To: " + sel[1]);
                        }
                    }
                }
                forceRepaint();
                document.repaint();
                getSelectedText();
                last_click = System.currentTimeMillis();
                return;
            }
            clearSelection();
            Color sel_color = WebDocument.active_document == document ? this.selection_color : this.selection_inactive_color;
            for (int i = 0; i < text_layer.getComponents().length; i++) {
                JLabel c = (JLabel)text_layer.getComponents()[i];
                if (x >= c.getX() && x <= c.getX() + c.getWidth() &&
                    y >= c.getY() && y <= c.getY() + c.getHeight()) {
                    int i1 = i;
                    while (i1 >= 0 && ((JLabel)text_layer.getComponents()[i1]).getText().matches("\\w")) {
                        ((JLabel)text_layer.getComponents()[i1]).setBackground(sel_color);
                        ((JLabel)text_layer.getComponents()[i1]).setForeground(Color.WHITE);
                        ((JLabel)text_layer.getComponents()[i1]).setOpaque(true);
                        i1--;
                    }
                    if (i1 < 0 || !((JLabel)text_layer.getComponents()[i1]).getText().matches("\\w")) {
                        i1++;
                    }
                    int i2 = i;
                    while (i2 <= text_layer.getComponents().length-1 && ((JLabel)text_layer.getComponents()[i2]).getText().matches("\\w")) {
                        ((JLabel)text_layer.getComponents()[i2]).setBackground(sel_color);
                        ((JLabel)text_layer.getComponents()[i2]).setForeground(Color.WHITE);
                        ((JLabel)text_layer.getComponents()[i2]).setOpaque(true);
                        i2++;
                    }
                    if (i2 > text_layer.getComponents().length-1 || !((JLabel)text_layer.getComponents()[i2]).getText().matches("\\w")) {
                        i2--;
                    }

                    if (sel == null) {
                        sel = new int[2];
                    }
                    sel[0] = i1;
                    sel[1] = i2;
                    for (int j = i1; j <= i2; j++) {
                        v.get(j).selected(true);
                    }
                    needToRestoreSelection = true;
                    forceRepaint();
                    text_layer.repaint();
                    needToRestoreSelection = false;
                    getSelectedText();
                    return;
                }
            }
        }
        if (original == null) {
            last_click = System.currentTimeMillis();
        } else {
            original.last_click = System.currentTimeMillis();
        }
    }

    public void mousePressed(MouseEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        Block d = this;
        while (d.parent != null || d.document.getParentDocument() != null) {
            if (d.parent != null) d = d.parent;
            else d = d.document.getParentDocumentBlock();
        }
        WebDocument.active_document = document;
        //d.clearSelection();
        d.forceRepaint();
        document.repaint();
        if (document != null) {
            for (WebDocument doc: document.child_documents) {
                doc.root.updateTextSelectionColor();
                doc.repaint();
            }
            WebDocument doc = document.parent_document;
            while (doc != null) {
                doc.root.updateTextSelectionColor();
                doc = doc.parent_document;
            }
        }
        from_x = -1;
        from_y = -1;
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).elements.size(); j++) {
                if (lines.get(i).elements.get(j) instanceof Block) {
                    ((Block)lines.get(i).elements.get(j)).from_x = -1;
                    ((Block)lines.get(i).elements.get(j)).from_y = -1;
                }
            }
        }
        fireEventForNode(e, node, null, "mouseDown");
    }

    public void mouseReleased(MouseEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        getSelectedText();
        fireEventForNode(e, node, null, "mouseUp");
    }

    public void mouseEntered(MouseEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        if (e.getSource() instanceof JTextField || e.getSource() instanceof JTextArea) {
            document.panel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
    }

    public void mouseExited(MouseEvent e) {
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        if (e.getSource() instanceof JTextField || e.getSource() instanceof JTextArea) {
            document.panel.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        if (display_type == Display.NONE || visibility == Visibility.HIDDEN) {
            return;
        }
        if (sel == null) sel = new int[2];
        int x = e.getX();
        int y = e.getY();
        if (from_x < 0 || from_y < 0) {
            from_x = x;
            from_y = y;
        }

        int x1 = Math.min(from_x, x);
        int y1 = Math.min(from_y, y);
        int x2 = Math.max(from_x, x);
        int y2 = Math.max(from_y, y);

        Rectangle selection = new Rectangle(x1, y1, x2-x1, y2-y1);

        for (int i = 0; i < v.size(); i++) {
            Drawable d = v.get(i);
            int offset = (d instanceof Block && ((Block)d).line != null ? ((Block)d).line.getHeight()-1 : 10);
            if (d instanceof Block && offset >= ((Block)d).viewport_height - 3) {
                offset = 10;
            }

            Rectangle bounds = new Rectangle(d._getX() - scroll_x, d._getY() - scroll_y, d._getWidth(), d._getHeight());

            if (d != null && d instanceof Block) {
                Block b = (Block)d;
                boolean flag = b._y_ - scroll_y + b.viewport_height < y2 &&
                               !(e.isShiftDown() && b._x_ - scroll_x >= x1);
                boolean none = b._x_ - scroll_x > x1 && b._x_ - scroll_x > x2 && flag ||
                            b._x_ - scroll_x + b.viewport_width < x1 && b._x_ - scroll_x + b.viewport_width < x2 && flag ||
                            b._y_ - scroll_y > y1 && b._y_ - scroll_y > y2 ||
                            b._y_ - scroll_y + b.viewport_height < y1 && b._y_ - scroll_y + b.viewport_height < y2;
                if (!none && !flag) {
                    ((Block)d).from_x = from_x;
                    ((Block)d).from_y = from_y;
                    ((Block)d).mouseDragged(e);
                    if (i < sel[0] || sel[0] == -1) sel[0] = i;
                    if (i > sel[1] || sel[1] == -1) sel[1] = i;
                } else {

                    //System.err.println(sel[0] + ", " + sel[1]);

                    boolean c1 = b._y_ - scroll_y + offset >= y1 && b._y_ - scroll_y + b.viewport_height - offset <= y2 &&
                                 b._x_ - scroll_x >= x1 && b._x_ - scroll_x + b.viewport_width <= x2;
                    boolean c2 = b.line != null && y2 > b.line.getY() - scroll_y + b.line.getHeight() &&
                                 x1 <= b._x_ - scroll_x + b.viewport_width && x2 >= b._x_ - scroll_x + b.viewport_width;
                    boolean c3 = b._y_ - scroll_y + b.viewport_height >= y1 && b._y_ - scroll_y + b.viewport_height + offset <= y2 && (!e.isShiftDown() || b._x_ - scroll_x + b.viewport_width <= x2);
                    if (c1 || c2 || c3) {
                        b.selectAll();
                        if (i < sel[0] || sel[0] == -1) sel[0] = i;
                        if (i > sel[1] || sel[1] == -1) sel[1] = i;
                    } else if (!(e.isShiftDown() && b._y_ - scroll_y + b.viewport_height - offset <= y2)) {
                        //b.clearSelection();
                        //b.forceRepaint();
                    }
                }
            } else if (d instanceof Character) {
                Block b = ((Character)d).line.parent;
                if (!b.select_enabled) return;
                for (int k = 0; k < b.lines.size(); k++) {
                    Line line = b.lines.get(k);
                    if (!e.isShiftDown()) {
                        if (y1 < b._y_ + line.getY() + line.getHeight() - scroll_y && y2 > b._y_ + line.getY() - scroll_y) {
                            boolean full = y2 > b._y_ + line.getY() + line.getHeight() - scroll_y;
                            for (int j = 0; j < line.elements.size(); j++) {
                                boolean contains = x1 <= b._x_ + line.elements.get(j)._getX() + line.elements.get(j)._getWidth() / 2 - scroll_x &&
                                                   x2 > b._x_ + line.elements.get(j)._getX() + line.elements.get(j)._getWidth() / 2 - scroll_x;
                                if (full || contains) {
                                    if (sel[0] > j || sel[0] == -1) sel[0] = j;
                                    if (sel[1] < j || sel[1] == -1) sel[1] = j;
                                    selectCharacter((Character)line.elements.get(j));
                                    line.elements.get(j).selected(true);
                                } else {
                                    if (sel[0] <= j && sel[1] >= j) sel[0] = j+1;
                                    if (sel[1] >= j && sel[0] <= j) sel[1] = j-1;
                                    if (sel[0] < 0 || sel[1] >= v.size() || sel[0] > sel[1]) {
                                        sel[0] = sel[1] = -1;
                                    }
                                    unselectCharacter((Character)line.elements.get(j));
                                    line.elements.get(j).selected(false);
                                }
                                javax.swing.JLabel glyph = ((Character)line.elements.get(j)).glyph;
                                if (glyph != null && !glyph.isVisible() || glyph == null) {
                                    ((Character)line.elements.get(j)).line.parent.forceRepaint();
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < line.elements.size(); j++) {
                            boolean contains = x1 < b._x_ + line.elements.get(j)._getX() + line.elements.get(j)._getWidth() / 2 - scroll_x &&
                                    x2 > b._x_ + line.elements.get(j)._getX() + line.elements.get(j)._getWidth() / 2 - scroll_x &&
                                    y2 > b._y_ + line.getY() - scroll_y;
                            if (contains) {
                                selectCharacter((Character)line.elements.get(j));
                                line.elements.get(j).selected(true);
                            } else {
                                unselectCharacter((Character)line.elements.get(j));
                                line.elements.get(j).selected(false);
                            }
                            javax.swing.JLabel glyph = ((Character)line.elements.get(j)).glyph;
                            if (glyph != null && !glyph.isVisible() || glyph == null) {
                                ((Character)line.elements.get(j)).line.parent.forceRepaint();
                            }
                        }
                    }
                }
            }
        }
        if (v.size() > 0 && v.get(0) instanceof Character && sel[0] == 0 && sel[1] == v.size()-1) {
            selected = true;
        }
        if (original != null) {
            original.sel = sel;
        }
        repaint();
        if (overflow != Overflow.HIDDEN || content_x_max <= viewport_width - borderWidth[3] - borderWidth[1] &&
                content_y_max <= viewport_height - borderWidth[0] - borderWidth[2]) {
            if (text_layer != null) {
                //text_layer.invalidate();
                //document.repaint();
            }
        }
    }

    public void updateTextSelectionColor() {
        if (textRenderingMode == 0 && text_layer != null) {
            Component[] c = text_layer.getComponents();
            for (int i = 0; i < c.length; i++) {
                c[i].setBackground(WebDocument.active_document == document ? selection_color : selection_inactive_color);
            }
        } else {
            forceRepaint();
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).updateTextSelectionColor();
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).updateTextSelectionColor();
        }
    }

    private void selectCharacter(Character c) {
        Color sel_color = WebDocument.active_document == document ? this.selection_color : this.selection_inactive_color;
        if (c.glyph == null) {
            c.setBackground(sel_color);
            c.setColor(Color.WHITE);
            return;
        }
        if (!c.glyph.isOpaque()) {
            //System.out.println("Selected line element '" + c.getText() + "'");
            c.glyph.setOpaque(true);
            c.glyph.setForeground(Color.WHITE);
            c.glyph.setBackground(sel_color);
        }
    }

    private void unselectCharacter(Character c) {
        if (c.glyph == null) {
            c.setBackground(null);
            c.setColor(color);
            return;
        }
        if (c.glyph.isOpaque()) {
            //System.out.println("Unselected line element '" + c.getText() + "'");
            c.glyph.setOpaque(false);
            c.glyph.setForeground(color);
            c.glyph.setBackground(null);
        }
    }

    @Override
    public void selected(boolean value) {
        selected = value;
        if (original != null) {
            original.selected = value;
        }
        for (Block part: parts) {
            part.selected = value;
        }
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).elements.size(); j++) {
                lines.get(i).elements.get(j).selected(value);
            }
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).selected(value);
        }
    }

    protected static char[] lm = { ' ', '\u25CF', '\u25E6', '\u25AA', '\u25AB', '\u2666', '\u25CA' };

    {

        list_types.add("none");
        list_types.add("circle");
        list_types.add("disk");
        list_types.add("square");
        list_types.add("square2");
        list_types.add("rhombus");

    }

    private void drawSubtree(Graphics g, int x, int y) {
        forceRepaint();
        if (parts.size() > 0) {
            for (int i = 0; i < parts.size(); i++) {
                g.drawImage(parts.get(i).buffer, parts.get(i)._x_ - x, parts.get(i)._y_ - y, parts.get(i).width, parts.get(i).height, null);
                if (parts.get(i).text_layer != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform t = AffineTransform.getTranslateInstance(-x, -y);
                    g2d.setTransform(t);
                    parts.get(i).text_layer.paint(g2d);
                    g2d.setTransform(AffineTransform.getTranslateInstance(0, 0));
                }
            }
        } else {
            g.drawImage(buffer, _x_ - x, _y_ - y, width, height, null);
            if (text_layer != null) {
                text_layer.paint(g);
            }
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).drawSubtree(g, x, y);
        }
    }

    public void takeScreenshot(String path) {
        BufferedImage img = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D gc = (Graphics2D) img.getGraphics();
        if (background != null && background.bgcolor != null) gc.setBackground(background.bgcolor);
        gc.clearRect(0, 0, document.root.width, document.root.height);
        drawSubtree(gc, _x_, _y_);
        
        try {
            ImageIO.write(img, "png", new File(path + ".png"));
        } catch (IOException ex) {
            System.err.println("Image output error!");
        }
    }

    public void startWatcher() {
        if (w != null) return;
        w = new Watcher();
        w.start();
    }

    public void stopWatcher() {
        if (w != null) w.stop();
    }

    class Watcher {

        public Watcher() {
            timer = new Timer(50, listener);
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
            stop = true;
        }

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (document == null || document.is_video_fullscreen) return;
                long time = System.currentTimeMillis();
                if (has_animation && time > nextFrameDisplayTime()) {
                    displayNextFrame();
                }
            }
        };

        private Timer timer;

        public volatile boolean stop = false;

    }

    public void applyLinkStyles(boolean is_hovered) {
        Color col = is_hovered && (hasParentLink || href.length() > 0) ? linkColor : default_color;
        
        if (children.size() > 0 && children.get(0).type == NodeTypes.TEXT) {
            color = col;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        if (parts.size() == 0) {
            boolean isSVG = getComponents().length == 1 && getComponents()[0] instanceof JSVGCanvas;

            if (isSVG) {
                updateStates(e, x + _x_, y + _y_);
            } else {
                boolean was_hovered = hovered;
                updateStates(e, x, y);
                processLinks(was_hovered, x, y);
            }
        }

        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).mouseMoved(e);
        }

        Block b = this.original == null ? this : this.original;
        if (b.children.size() == 1 && b.children.get(0) instanceof YouTubeThumb) {
            boolean was_hovered = b.children.get(0).hovered;
            b.children.get(0).updateStates(e, x, y);
            b.children.get(0).processLinks(was_hovered, x, y);
        } else {
            for (int i = 0; i < b.children.size(); i++) {
                if (b.children.get(i).type == NodeTypes.ELEMENT) {
                    b.children.get(i).mouseMoved(e);
                }
            }
        }

        if (this == document.root) {
            applyStylesBatchRecursive();
            document.eventsFired.clear();
            document.repaint();
        }
    }

    private void processLinks(boolean was_hovered, int x, int y) {
        boolean flag = false;
        if (x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height &&
              (hasParentLink || href != null) && !was_hovered && !hovered) {
            hovered = true;
            if (!originalStyles.containsKey("text_underline")) {
                originalStyles.put("text_underline", text_underline);
                originalStyles.put("text_color", color);
            }
            text_underline = true;
            color = linkColor;
            document.panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            flag = true;
        } else if (!(x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height) &&
                (!hasParentLink && href == null || linksUnderlineMode == 1) && originalStyles.containsKey("text_underline")) {
            hovered = false;
            text_underline = (Boolean) (originalStyles.get("text_underline"));
            color = (Color) originalStyles.get("text_color");
            originalStyles.remove("text_underline");
            originalStyles.remove("text_color");
            document.panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            flag = true;
        } else if (!(x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height) && was_hovered && (hasParentLink || href != null)) {
            hovered = false;
            document.panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            flag = true;
        }
        if (flag) {
            if (text_layer != null) {
                Component[] c = text_layer.getComponents();
                for (int i = 0; i < c.length; i++) {
                    JLabel label = (JLabel) c[i];

                    int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                    Font font = new Font(fontFamily, style, fontSize);

                    Color col = hasParentLink || href != null ? linkColor : color;
                    label.setForeground(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)Math.round(col.getAlpha() * alpha)));

                    boolean underline = text_underline || (hasParentLink || href != null) && linksUnderlineMode == 0;
                    boolean strikethrough = text_strikethrough;

                    if (underline || strikethrough) {
                        Map attributes = font.getAttributes();
                        if (underline) attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        if (strikethrough) attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                        label.setFont(font.deriveFont(attributes));
                    } else {
                        label.setFont(font);
                    }
                }
                
            }
            if (textRenderingMode == 1) forceRepaint();
        }
    }

    private void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (os.indexOf("mac") >= 0) rt.exec("open " + url);
            else {
                String[] browsers = { "google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                                 "netscape", "opera", "links", "lynx" };
                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++)
                    if(i == 0)
                        cmd.append(String.format(    "%s \"%s\"", browsers[i], url));
                    else
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));

                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }
        } catch (IOException ex) {}
    }

    private HashMap<String, String> getEventData(KeyEvent e, String type) {
        HashMap<String, String> data = new HashMap<String, String>();
        type = type.toLowerCase();
        data.put("type", '"' + type + '"');
        data.put("ctrlKey", e.isControlDown() ? "true" : "false");
        data.put("shiftKey", e.isShiftDown() ? "true" : "false");
        data.put("altKey", e.isAltDown() ? "true" : "false");
        data.put("metaKey", e.isMetaDown() ? "true" : "false");
        return data;
    }

    private HashMap<String, String> getEventData(MouseEvent e, String type) {
        HashMap<String, String> data = new HashMap<String, String>();
        type = type.toLowerCase();
        if (type.equals("doubleclick")) type = "dblclick";
        data.put("clientX", e.getX() + "");
        data.put("clientY", e.getY() + "");
        data.put("pageX", e.getX() + scroll_x + "");
        data.put("pageY", e.getY() + scroll_y + "");
        data.put("type", '"' + type + '"');
        data.put("ctrlKey", e.isControlDown() ? "true" : "false");
        data.put("shiftKey", e.isShiftDown() ? "true" : "false");
        data.put("altKey", e.isAltDown() ? "true" : "false");
        data.put("metaKey", e.isMetaDown() ? "true" : "false");
        return data;
    }

    private void fireEventForNode(KeyEvent e, htmlparser.Node node, htmlparser.Node related_node, String event_type) {
        if (node == null || node.tagName.startsWith("::") || node.tagName.isEmpty()) return;
        boolean was_fired = document != null ? document.eventWasFired(node, event_type) : false;
        if (!was_fired) {
            HashMap<String, String> data = getEventData(e, event_type);
            node.fireEvent(event_type, "render", data, related_node);
        }
        if (document != null) document.fireEventForNode(node, event_type);
    }

    private void fireEventForNode(MouseEvent e, htmlparser.Node node, htmlparser.Node related_node, String event_type) {
        if (node == null || node.tagName.startsWith("::") || node.tagName.isEmpty()) return;
        boolean was_fired = document != null ? document.eventWasFired(node, event_type) : false;
        if (!was_fired) {
            HashMap<String, String> data = getEventData(e, event_type);
            node.fireEvent(event_type, "render", data, related_node);
        }
        if (document != null) document.fireEventForNode(node, event_type);
    }

    private boolean isInnermostBlockForEvent(int x, int y) {
        boolean isInside = isMouseInside(x, y);
        if (isInside && (children.size() == 0 || children.get(0).type == NodeTypes.TEXT)) {
            return true;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).isMouseInside(x, y)) return false;
        }
        return true;
    }

    private void processMouseOverEvent(MouseEvent e, htmlparser.Node node, int x, int y) {
        if (parts.size() > 0) return;
        if (node == null || node.tagName.startsWith("::")) return;
        if (document.eventWasFired(node, "mouseOut")) return;
        if (isInnermostBlockForEvent(x, y) && (document.hovered_block == null || document.hovered_block.node != node)) {
            htmlparser.Node relatedNode = document.hovered_block != null ? document.hovered_block.node : null;
            if (document.hovered_block != null && document.hovered_block.node != null) {
                fireEventForNode(e, document.hovered_block.node, node, "mouseOut");
            }
            fireEventForNode(e, node, relatedNode, "mouseOver");
            document.hovered_block = this;
        }
    }

    private void processMouseOutEvent(MouseEvent e, htmlparser.Node node, int x, int y) {
        if (node == null || node.tagName.startsWith("::")) return;
        if (document.eventWasFired(node, "mouseOver")) return;
        if (isInnermostBlockForEvent(x, y)) {
            htmlparser.Node relatedNode = document.hovered_block != null ? document.hovered_block.node : null;
            fireEventForNode(e, node, relatedNode, "mouseOut");
        }
    }

    private void processMouseMoveEvent(MouseEvent e, htmlparser.Node node, int x, int y) {
        if (node == null || node.tagName.startsWith("::") || node.tagName.isEmpty()) return;
        if (isInnermostBlockForEvent(x, y)) {
            fireEventForNode(e, node, null, "mouseMove");
        }
    }

    private void updateStates(MouseEvent e, int x, int y) {
        Block last_hovered_block = document != null ? document.hovered_block : null;
        htmlparser.Node last_hovered_node = (document != null && document.hovered_block != null) ? document.hovered_block.node : null;
        
        if (x >= _x_ && x < _x_ + viewport_width && y >= _y_ && y < _y_ + viewport_height) {
            processMouseOverEvent(e, node, x, y);
            if (!hovered) {
                if (last_hovered_node != null && last_hovered_node != node && !last_hovered_block.isMouseInside(x, y)) {
                    fireEventForNode(e, last_hovered_node, node, "mouseLeave");
                }
                fireEventForNode(e, node, last_hovered_node, "mouseEnter");
                last_hovered_block = document.hovered_block;
            }
            hovered = true;
            if (last_hovered_node == node) processMouseMoveEvent(e, node, x, y);
            if (node != null && !node.states.contains("hover")) {
                node.states.add("hover");
                applyStateStyles();
                if (cursor != null) {
                    document.panel.setCursor(cursor);
                    if (text_layer != null) text_layer.setCursor(cursor);
                    //System.out.println("Cursor set");
                }
                //System.err.println(node.tagName + " Hovered!");
            }
        } else if (!(x >= _x_ && x < _x_ + viewport_width && y >= _y_ && y < _y_ + viewport_height)) {
            //processMouseOutEvent(node, x, y);
            if (hovered) {
                htmlparser.Node relatedNode = document.hovered_block != null ? document.hovered_block.node : null;
                fireEventForNode(e, node, relatedNode, "mouseLeave");
            }
            hovered = false;
            if (node != null && node.states.contains("hover")) {
                 node.states.remove("hover");
                 document.no_immediate_apply = true;
                 resetStyles();
                 applyStateStyles();
                 document.no_immediate_apply = false;
                 if (cursor != null) {
                     document.panel.setCursor(Cursor.getDefaultCursor());
                     if (text_layer != null) text_layer.setCursor(Cursor.getDefaultCursor());
                     //System.out.println("Cursor unset");
                 }
                 //System.err.println(node.tagName + " Out!");
             }
        }
    }

    public void applyStylesBatchRecursive() {
        applyStylesBatch();

        Vector<Block> blocks = copyChildren();
        if (beforePseudoElement != null) {
            blocks.add(0, beforePseudoElement);
        }
        if (afterPseudoElement != null) {
            blocks.add(afterPseudoElement);
        }
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).applyStylesBatchRecursive();
        }
    }

    public void applyStylesBatch() {
        if (builder == null || type != NodeTypes.ELEMENT) return;
        LinkedHashMap<String, String> newStyles = builder.targetStyles.get(this);
        int count = 0;
        boolean changed_display = false;
        if (newStyles != null) {
            Set<String> keys = newStyles.keySet();
            for (String key: keys) {
                if (cssStyles.containsKey(key) && cssStyles.get(key).equals(newStyles.get(key))) continue;
                boolean isAnimated = isPropertyAnimated(key);
                if (isAnimated) {
                    TransitionInfo info = transitions.get(key) != null ? transitions.get(key) : transitions.get("all");
                    Transition t = new Transition(this, info, key, null, newStyles.get(key));
                    //System.out.println("CSS Transition started ( " + key + ": " + newStyles.get(key) + " )");
                    t.start();
                } else {
                    boolean ready = document.ready;
                    document.ready = false;
                    int old_display_type = display_type;
                    setProp(key, newStyles.get(key));
                    if (key.equals("display") && old_display_type == Display.NONE && display_type != Display.NONE) {
                        Block root = this;
                        while (root.parent != null) {
                            root = root.parent;
                        }
                        document.ready = true;
                        parent.addToLayout(this, parent.children.indexOf(this), root);
                        changed_display = true;
                    }
                    else if (key.equals("display") && old_display_type != Display.NONE && display_type == Display.NONE) {
                        cancelTransitions();
                        removeTextLayers();
                        clearBuffer();
                        for (Block part: parts) {
                            part.cancelTransitions();
                            part.clearBuffer();
                        }
                        document.ready = true;
                        parent.removeFromLayout(this);
                        changed_display = true;
                    }
                    if (!key.matches("(-[a-z]+-)?transition|cursor")) {
                        count++;
                    }
                    document.ready = ready;
                }
            }
            builder.targetStyles.remove(this);
        }
        if (count > 0) {
            Block b = doIncrementLayout();
            if (b == this && changed_display && b.parent != null) {
                b = b.parent;
            }
            if (b != null) b.forceRepaint();
            document.repaint();
        }
    }

    public boolean isPropertyAnimated(String property) {
        if (property.matches("cursor|(-[a-z]+-)?transition|display|visibility|font-family")) {
            return false;
        }
        return transitions.containsKey("all") || transitions.containsKey(property);
    }

    public void cancelTransitions() {
        for (Transition t: (Vector<Transition>) activeTransitions.clone()) {
            t.stop();
        }
        activeTransitions.clear();
    }

    public void setMediaPlayer(MediaPlayer player) {
        mp = player;
    }

    public void setScaleBorder(boolean value) {
        scale_borders = value;
    }

    public HashMap<String, TransitionInfo> transitions = new HashMap<String, TransitionInfo>();
    public volatile Vector<Transition> activeTransitions = new Vector<Transition>();

    public boolean rtl = false;

    public Timer animator;
    public boolean passiveTransitionMode = true;

    public final Color DEFAULT_INPUT_BORDER_COLOR = new Color(118, 118, 123);
    public final Color DEFAULT_INPUT_BACKGROUND_COLOR = new Color(255, 255, 255);
    public final Color DEFAULT_INPUT_TEXT_COLOR = new Color(34, 34, 36);

    public Background background;
    public Background target_background;
    public double backgroundState = 0;

    public boolean selected = false;
    public boolean checked = false;
    public boolean defaultChecked = false;
    private boolean hovered;
    private Watcher w;
    private MediaPlayer mp;

    Vector<Drawable> v = new Vector<Drawable>();
    public int textRenderingMode = 0;
    public boolean scale_borders = true;

}
