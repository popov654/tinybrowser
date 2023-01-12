package render;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.Timer;
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

        this.ratio = (double)java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 96;

        fontSize = (int)Math.round(fontSize * ratio);

        this.width = width > 0 ? (int)Math.round(width * ratio) : -1;
        if (this.width < 0) this.auto_width = true;
        if (height >= 0) {
            auto_height = false;
            this.height = (int)Math.round(height * ratio);
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
        this.border = new RoundedBorder(this, this.borderWidth, this.arc[0], this.borderColor);
        platform = Float.parseFloat(System.getProperties().getProperty("os.version"));

        rules_for_recalc = new HashMap<String, String>();
        originalStyles = new HashMap<String, Object>();
        cssStyles = new HashMap<String, String>();

        orig_width = width;
        orig_height = height;

        if (width < 0 && document != null && parent == null) {
            width = document.width;
        }
        if (document != null && parent == null) {
            setBounds(document.borderSize, document.borderSize, document.width-document.borderSize*2, document.height-document.borderSize*2);
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
            result = (x >= _x_ && x <= _x_ + viewport_width && y >= _y_ && y <= _y_ + viewport_height);
        } else {
            if (x >= _x_ && x <= _x_ + viewport_width && y >= _y_ && y <= _y_ + viewport_height) {
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
        if (positioning == Position.ABSOLUTE) {
            return margins[3] + left;
        }
        return (parent == null) ? _x_ : _x_ - parent._x_;
    }

    public int getOffsetTop() {
        if (positioning == Position.ABSOLUTE) {
            return margins[0] + top;
        }
        return (parent == null) ? _y_ : _y_ - parent._y_;
    }

    public synchronized void forceRepaint() {
        if (document == null || document.root.width < 0 || !document.ready || document.inLayout || this == document.root && document.isPainting) {
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

        if (childDocument != null) {
            childDocument.getRoot().paintComponent(g);
            return;
        }

        int dx = 0;
        int dy = 0;

        if (parent != null) {
            dx += parent.scroll_x;
            dy += parent.scroll_y;
        }
        if (display_type == 2) dy += 1;
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
            int px = _x_-(w-width)/2;
            int py = _y_-(w-height)/2;
            if (height < 20) {
                px -= 4;
                py += 2;
            }
            g.drawImage(buffer, px, py, this);
        }
        else if (has_shadow) {
            int x0 = -shadow_x + shadow_blur + shadow_size;
            int y0 = -shadow_y + shadow_blur + shadow_size;
            g.drawImage(buffer, _x_ - x0 - dx, _y_ - y0 - dy, this);
        } else {
            g.setClip(null);
            g.drawImage(buffer, _x_ - dx, _y_ - dy, this);
        }
        
        if (text_layer != null) {
            text_layer.setBounds(-scroll_x, -scroll_y, text_layer.getWidth(), text_layer.getHeight());
        }

        super.paintComponent(g);
    }

    public void clearBuffer() {

        if (buffer == null) return;

        Shape rect = new RoundedRect(0, 0, width, height, arc[0], arc[1], arc[2], arc[3]);

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
        if (buffer == null && width > 0 && height > 0) {
            int w = width;
            int h = height;
            if (children.size() > 0 && children.lastElement().type == NodeTypes.TEXT && text_italic) w += 2;
            buffer = new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        if (buffer == null) return;
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

        if (parent == null) {
            g2d.setColor(bgcolor);
            g2d.fillRect(0, 0, bw, bh);
        }
        if (parent != null && parent.bgcolor != null && parent.gradient == null && parent.bgImage == null) {
            g2d.setColor(parent.bgcolor);
            g2d.fillRect(0, 0, bw, bh);
        }

        int x0 = has_shadow ? -shadow_x + shadow_blur + shadow_size : 0;
        int y0 = has_shadow ? -shadow_y + shadow_blur + shadow_size : 0;

        if (parent != null) {
            //x0 -= parent.scroll_x;
            //y0 -= parent.scroll_y;
        }

        scroll_x = (parent != null ? parent.scroll_x : 0) + scroll_left;
        scroll_y = (parent != null ? parent.scroll_y : 0) + scroll_top;

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

            //AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1f);
            //g2d.setComposite(composite);

            //g2d.fill(rect);

            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(composite);

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
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            g2d.setTransform(tx);
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
            g2d.setClip(clip_rect);

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

        if (gradient != null) {
            Point2D[] p = Gradient.getPoints(gradient.getPositions(), gradient.getAngle(), 0, 0, width, height);
            Point2D start = p[0];
            Point2D end = p[1];
            start.setLocation(start.getX() + x0, start.getY() + y0);
            end.setLocation(end.getX() + x0, end.getY() + y0);
            Color[] colors = gradient.getColors();
            float[] dist = gradient.getPositions();
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
        }
        else if (bgcolor != null) {
            g2d.setColor(bgcolor);
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
            //if (display_type == 2) System.err.println(width + "x" + height);
        }
        if (bgImage != null) {
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
            if (bg_alpha < 1.0f) {
                int type = AlphaComposite.SRC_OVER;
                AlphaComposite composite = AlphaComposite.getInstance(type, alpha * bg_alpha);
                g2d.setComposite(composite);
            }
            if (background_repeat == BackgroundRepeat.NONE) {
                int x = x0 + borderWidth[3] + background_pos_x;
                int y = y0 + borderWidth[0] + background_pos_y;
                int iw = background_size_x < 0 ? bgImage.getWidth() : background_size_x;
                int ih = background_size_y < 0 ? bgImage.getHeight() : background_size_y;
                //Image scaledImage = bgImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(bgImage, x, y, x+iw, y+ih, 0, 0, bgImage.getWidth(), bgImage.getHeight(), this);
            } else {
                int x = x0 + borderWidth[3] + background_pos_x;
                int y = y0 + borderWidth[0] + background_pos_y;

                int iw = background_size_x < 1 ? bgImage.getWidth() : background_size_x;
                int ih = background_size_y < 1 ? bgImage.getHeight() : background_size_y;

                int w = x + iw;
                int h = y + ih;

                if ((background_repeat & BackgroundRepeat.REPEAT_X) > 0) {
                    while (x > x0 + borderWidth[3]) x -= iw;
                    w = width - borderWidth[1];
                }
                if ((background_repeat & BackgroundRepeat.REPEAT_Y) > 0) {
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
                        g2d.drawImage(bgImage, x1, y1, x2, y2, x_from, y_from, x_from+x2-x1, y_from+y2-y1, this);
                        x += iw;
                    }
                    y += ih;
                    x = x0 + borderWidth[3] + background_pos_x;
                }
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
        bgImage = animation_frames[current_frame].getImage();
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
//                if (c.glyph != null) {
//                    c.glyph.setOpaque(true);
//                    c.glyph.setForeground(Color.WHITE);
//                    c.glyph.setBackground(selection_color);
//                }
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
            if (scrollbar_y != null) sw -= scrollbar_y.getPreferredSize().width;
            scrollbar_x.setBounds(_x_ + borderWidth[3], _y_ + viewport_height - borderWidth[2] - scrollbar_x.getPreferredSize().height, sw, scrollbar_x.getPreferredSize().height);
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
            width = viewport_width + (scrollbar_y != null ? scrollbar_y.getPreferredSize().width : 0);
            //width = Math.max(width, w);
            //width = w;
        }
    }

    private void addScrollbarY() {
        if (overflow == Overflow.SCROLL) {
            if (scrollbar_y != null) {
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

        if (!no_viewport_reset && auto_width) viewport_width = 0;

        if (document.debug) {
            System.out.println("Layout started for block " + toString());
            System.out.println();
        }

        if (isImage) {
            if (bgImage == null) {
                if (width < 0) width = viewport_width = (int) Math.round(16 * ratio);
                if (height < 0) height = viewport_height = (int) Math.round(16 * ratio);
                setBorderColor("#8a8a9f");
                borderWidth = new int[] {1, 1, 1, 1};
                this.border = new RoundedBorder(this, this.borderWidth, this.arc[0], this.borderColor, this.borderType);
                if (bgcolor == null || bgcolor.equals(new Color(0, 0, 0, 0))) bgcolor = new Color(85, 85, 85, 75);
                setBackgroundImage(Util.getInstallPath() + File.separatorChar + "res" + File.separatorChar + "photo_16.png");
                this.background_pos_x = 1;
                this.background_size_x = 16;
                this.background_size_y = 16;
                this.background_size_x_auto = false;
                this.background_size_y_auto = false;
                setBackgroundRepeat(BackgroundRepeat.NONE);
                special = true;
                return;
            } else {
                if (width < 0 && height < 0) {
                    width = viewport_width = bgImage.getWidth();
                    height = viewport_height = bgImage.getHeight();
                    auto_width = true;
                    auto_height = true;
                } else {
                    double aspect_ratio = (double) bgImage.getWidth() / bgImage.getHeight();
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
            }
            background_pos_x = 0;
            background_pos_y = 0;
            background_size_x = viewport_width;
            background_size_y = viewport_height;
            return;
        }

        if (childDocument != null) {
            Block root = childDocument.getRoot();
            root.width = root.viewport_width = width;
            root.height = root.viewport_height = height;
            document.panel.add(childDocument, 0);
            childDocument.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
            //childDocument.setOpaque(true);
            childDocument.setBackground(Color.RED);
            childDocument.root.setBounds(0, 0, width, height);
            root.performLayout();
            return;
        }

        if (children.size() == 0 && getComponents().length == 1) {
            if (getComponents()[0] instanceof MediaPlayer.VideoRenderer) {
                int delta = parent.width - width;
                width = parent.width;
                height = parent.height - parent.children.get(1).height;
                Block progress = parent.children.get(1).children.get(1);
                progress.width = progress.max_width = progress.children.get(0).width = progress.width + delta;
                getComponents()[0].setBounds(_x_ - scroll_x, _y_ - scroll_y, width, height);
            }
            return;
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

        Vector<Block> floats = new Vector<Block>();

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

            if (el.getOffsetLeft() - borderWidth[3] + el.viewport_width > content_x_max) {
                content_x_max = el.getOffsetLeft() - borderWidth[3] + el.viewport_width;
                if (el.positioning != Position.ABSOLUTE && el.margins[1] > 0) {
                    content_x_max += el.margins[1];
                }
                if (el.positioning != Position.ABSOLUTE) {
                    content_x_max += paddings[1];
                }
            }
            if (el.getOffsetTop() - borderWidth[0] + el.viewport_height > content_y_max) {
                content_y_max = el.getOffsetTop() - borderWidth[0] + el.viewport_height;
                if (el.positioning != Position.ABSOLUTE && el.margins[2] > 0) {
                    content_y_max += el.margins[2];
                }
                if (el.positioning != Position.ABSOLUTE) {
                    content_y_max += paddings[2];
                }
            }

            if (content_y_max > height - borderWidth[0] - borderWidth[2] && !auto_height && overflow == Overflow.SCROLL) {
                addScrollbarY();
                performLayout(no_rec, true);
                return;
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

        Vector<Block> blocks = ((Vector<Block>)children.clone());
        blocks.removeAll(floats);

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

        for (int i = 0; i < blocks.size(); i++) {
            Block el = blocks.get(i);

            if (el.type == NodeTypes.TEXT) {
                if (el.white_space != WhiteSpace.PRE_WRAP && el.textContent.matches("^\\s*$")) {
                    continue;
                }
                el.textContent = replaceEntities(el.textContent);
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
                        addScrollbarY();
                        performLayout(no_rec, true);
                        return;
                    }
                }
                layouter.last_element = -1;
                Line last = lines.lastElement();
                if (auto_height) {
                    viewport_height = height = last.getY() + last.getHeight() + paddings[2] + borderWidth[2];
                }
                if (is_table_cell && auto_width) width = pref_size;
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

                if (el.getOffsetLeft() - borderWidth[3] + el.viewport_width > content_x_max) {
                    if (el.margins[1] < 0) {
                        el.margins[1] = 0;
                        el.margins[3] = 0;
                    }
                    content_x_max = el.getOffsetLeft() - borderWidth[3] + el.viewport_width;
                    if (el.positioning != Position.ABSOLUTE) {
                        content_x_max += el.margins[1] + paddings[1];
                    }
                }
                if (el.getOffsetTop() - borderWidth[0] + el.viewport_height > content_y_max) {
                    if (el.auto_y_margin && el.margins[0] < 0) {
                        el.margins[0] = 0;
                        el.margins[2] = 0;
                    }
                    content_y_max = el.getOffsetTop() - borderWidth[0] + el.viewport_height;
                    if (el.positioning != Position.ABSOLUTE) {
                        content_y_max += el.margins[2] + paddings[2];
                    }
                }

                if (content_y_max > height - borderWidth[0] - borderWidth[2] && !auto_height && overflow == Overflow.SCROLL) {
                    boolean had_scroll = scrollbar_y != null;
                    addScrollbarY();
                    if (!had_scroll) {
                        performLayout(no_rec, true);
                        return;
                    }
                }
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
        }
        if (lines.size() > 0) {
            Line last = lines.lastElement();
            while (display_type != Display.INLINE && !last.elements.isEmpty() && last.elements.lastElement() instanceof Character &&
                    ((Character)last.elements.lastElement()).getText().matches("\\s+")) {
                last.cur_pos -= ((Character)last.elements.lastElement()).getWidth();
                last.elements.remove(last.elements.size()-1);
            }
            if (Layouter.stack.isEmpty() && width == 0) {
                last.setWidth(last.cur_pos);
                width = borderWidth[3] + paddings[3] + lines.get(0).getWidth() + paddings[1] + borderWidth[1];
                height = borderWidth[0] + paddings[0] + last.getY() + last.getHeight() + paddings[2] + borderWidth[2];
                viewport_width = width;
                viewport_height = height;
                /*if (overflow != Overflow.VISIBLE) {
                    setBounds(_x_, _y_, width, height);
                }*/
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
            /*if (overflow != Overflow.VISIBLE) {
                setBounds(_x_, _y_, width, height);
            }*/
            viewport_height = height;
        } else {
            if (children.size() == 1 && children.get(0).auto_y_margin) {
                children.get(0).setAutoYMargin(true);
            }
        }
        if (content_x_max > viewport_width - borderWidth[1] - borderWidth[3]) {
            addScrollbarX();
            if (content_y_max > viewport_height - borderWidth[0] - borderWidth[2] && !auto_height) {
                addScrollbarY();
            } else if (content_y_max > viewport_height - borderWidth[0] - borderWidth[2] && auto_height) {
                viewport_height += scrollbar_x.getPreferredSize().height;
                height += scrollbar_x.getPreferredSize().height;
                if (scrollbar_y != null) {
                    document.panel.remove(scrollbar_y);
                    viewport_height = height;
                    scroll_y = 0;
                    scroll_top = 0;
                }
            } else if (content_y_max <= viewport_height - borderWidth[0] - borderWidth[2]) {
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
            if (text_align != TextAlign.ALIGN_LEFT) {
                Layouter.applyHorizontalAlignment(this);
            }
            sortBlocks();
            return;
        }
        if (content_x_max <= viewport_width - borderWidth[1] - borderWidth[3] && scrollbar_x != null) {
            document.panel.remove(scrollbar_x);
            scrollbar_x = null;
            scroll_x = 0;
            scroll_left = 0;
            viewport_height = height;
            if (children.size() == 1 && children.get(0).type == NodeTypes.ELEMENT && children.get(0).auto_y_margin) {
                children.get(0).setAutoYMargin();
                children.get(0)._y_ = children.get(0).margins[0];
                lines.lastElement().top = children.get(0).margins[0];
                if (children.get(0).margins[0] < 0) {
                    children.get(0).margins[0] = children.get(0).margins[2] = 0;
                    children.get(0)._y_ = 0;
                    lines.lastElement().top = 0;
                    content_y_max = children.get(0).height;
                }
            }
            if (scrollbar_y != null) {
                scrollbar_y.setBounds(viewport_width, 0, scrollbar_y.getPreferredSize().width, height);
            }
        }
        if (content_y_max <= viewport_height - borderWidth[0] - borderWidth[2] && scrollbar_y != null) {
            document.panel.remove(scrollbar_y);
            scrollbar_y = null;
            scroll_y = 0;
            scroll_top = 0;
            viewport_width = width;
            performLayout(no_rec, true);
            return;
        }
        if (childDocument != null) {
            childDocument.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
            childDocument.setBounds(_x_, _y_, viewport_width, viewport_height);
            childDocument.root.setBounds(0, 0, viewport_width, viewport_height);
            return;
        }
        if (text_align != TextAlign.ALIGN_LEFT) {
            Layouter.applyHorizontalAlignment(this);
        }
        sortBlocks();
        if (this == document.root && this.children.size() > 0) {
            setZIndices();
            clipScrollbars();
        }
        
        if (document.debug) {
            System.out.println();
            System.out.println("Layout ended for block " + toString());
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
                    if ((b.positioning == Position.ABSOLUTE ||
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

    class BlockSort implements Comparator<Block> {

        @Override
        public int compare(Block a, Block b) {
            if (a._y_ != b._y_) return a._y_ - b._y_;
            return a._x_ - b._x_;
        }
    }

    public void renderContent(Graphics g) {

        last_list_index = 0;

        if (text_layer == null || list_item_type > 0 || textRenderingMode == 1) {
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
                if (b.positioning == Block.Position.ABSOLUTE || b.float_type != Block.FloatType.NONE ||
                      b.display_type == Display.TABLE_ROW || b.display_type == Display.TABLE_CELL) {
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
        int offset = (this == document.root) ? list.size()-1 : getParent().getComponentZOrder(this);
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
        b = b.children.get(0);
        int psc = 0;
        while (b != null) {
            /* Do not include descendants of floats and inline-blocks except elements
               generating their own contexts */
            boolean z_auto = block.zIndexAuto || (block.parent != null && block.positioning == Position.STATIC);
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
                    b = b.children.get(0);
                    pos = 0;
                } else {
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
                        b = null;
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
                pos++;
                b = b.parent.children.get(pos);
            } else {
                b = null;
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

        boolean render_chars = text_layer == null || textRenderingMode == 1;

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
                else if (render_chars) processTextChar(g, c);
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
                    if (render_chars) processTextChar(g, c);
                }
            }
        }
        if (render_chars) {
            for (int i = 0; i < lines.size(); i++) {
                for (int j = 0; j < lines.get(i).elements.size(); j++) {
                    if (lines.get(i).elements.get(j) instanceof Character) {
                        Character c = (Character)lines.get(i).elements.get(j);
                        processTextChar(g, c);
                    }
                }
            }
            if (needToRestoreSelection) {
                restoreSelection(g);
            }
        }
        //invalidate();
        document.repaint();
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

    private void processTextChar(Graphics g, Character c) {
        if (textRenderingMode > 0) {
            c.draw(g);
            return;
        }
        int style = (text_bold || text_italic) ? ((text_bold ? Font.BOLD : 0) | (text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font font = new Font(fontFamily, style, fontSize);

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
        Block b = this;
        while (b.parent != null) {
            b = b.parent;
        }

        if (text_layer == null) {
            text_layer = new JPanel();
            text_layer.setLayout(null);
            add(text_layer);
            text_layer.setBounds(0, 0, b.getWidth(), b.getHeight());
            text_layer.setOpaque(false);
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

        if (transform || (hidden && flag)) {
            if (clipping_block != null) {

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

        if (from_element > -1 && to_element > -1) {

            for (int k = 0; k < v.size(); k++) {
                if (!(v.get(k) instanceof Character)) continue;
                Character c = (Character)v.get(k);
                if (!c.selected) continue;
                JLabel label = c.glyph;

                if (label != null) {
                    label.setForeground(Color.WHITE);
                    if (!text_italic) {
                        label.setBackground(selection_color);
                        label.setOpaque(true);
                    } else {
                        g.setColor(selection_color);
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

    Block clipping_block = null;

    public String replaceEntities(String str) {
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

    public void setTextColor(Color col) {
        color = col;
        forceRepaint();
    }

    public void setBackgroundColor(Color col) {
        bgcolor = col;
        forceRepaint();
    }

    public void setBackgroundImage(String path) {
        if (path == null || path.isEmpty()) {
            bgImage = null;
            forceRepaint();
            return;
        }
        try {
            File f;
            if (path.startsWith("http")) {
                bgImage = ImageIO.read(new URL(path));
                String[] str = path.split("/");
                f = File.createTempFile("tmp_", str[str.length-1]);
                ImageIO.write(bgImage, "png", f);
            } else {
                f = new File(path);
                bgImage = ImageIO.read(f);
            }
            ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
            ir.setInput(ImageIO.createImageInputStream(f));
            if (ir.getNumImages(true) > 1) {
                readGIF(ir);
                has_animation = true;
                startWatcher();
            } else {
                stopWatcher();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        forceRepaint();
    }

    public void setLinearGradient(Vector<Color> colors, Vector<Float> positions, int angle) {
        int n = Math.max(colors.size(), positions.size());
        Gradient.ColorStop[] cs = new Gradient.ColorStop[n];
        Color c = new Color(0, 0, 0, 0);
        float p = 0f;
        for (int i = 0; i < n; i++) {
            if (i < colors.size()) {
                c = colors.get(i);
            }
            if (i < positions.size()) {
                p = positions.get(i);
            }
            cs[i] = new Gradient.ColorStop(c, p);
        }
        gradient = new Gradient(angle, cs);
        forceRepaint();
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

    public void setFontFamily(String value) {
        fontFamily = value;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setFontFamily(value);
        }
        performLayout();
        forceRepaint();
    }

    public void setFontSize(int value) {
        fontSize = (int)Math.round(value * ratio);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setFontSize(value);
        }
        performLayout();
        forceRepaint();
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
            for (Block part: parts) {
                part.setBackgroundColor(col);
            }
            setTextColor(col);
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setTextColor(col);
        }
        forceRepaint();
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
        this.border = new RoundedBorder(this, borderWidth, arc[0], borderColor, borderType);
        forceRepaint();
    }

    public void setBorderWidth(int value) {
        int bw = WebDocument.scale_borders ? (int)Math.round(value*ratio) : value;
        for (Block part: parts) {
            for (int i = 0; i < 4; i++) {
                part.borderWidth[i] = bw;
            }
        }
        for (int i = 0; i < 4; i++) {
            borderWidth[i] = bw;
        }
        this.border = new RoundedBorder(this, bw, arc[0], borderColor, borderType);
        updateAbsolutePositionedChildren();
        forceRepaint();
    }

    public void setBorderRadius(int radius) {
        for (int i = 0; i < 4; i++) {
            arc[i] = (int)(radius * 2 * ratio);
        }
        forceRepaint();
    }

    public void setBorderRadius(int r1, int r2) {
        arc[0] = (int)(r1 * 2 * ratio);
        arc[1] = (int)(r2 * 2 * ratio);
        arc[2] = (int)(r1 * 2 * ratio);
        arc[3] = (int)(r2 * 2 * ratio);
        forceRepaint();
    }

    public void setBorderRadius(int r1, int r2, int r3) {
        arc[0] = (int)(r1 * 2 * ratio);
        arc[1] = (int)(r2 * 2 * ratio);
        arc[2] = (int)(r3 * 2 * ratio);
        arc[3] = (int)(r2 * 2 * ratio);
        forceRepaint();
    }

    public void setBorderRadius(int r1, int r2, int r3, int r4) {
        arc[0] = (int)(r1 * 2 * ratio);
        arc[1] = (int)(r2 * 2 * ratio);
        arc[2] = (int)(r3 * 2 * ratio);
        arc[3] = (int)(r4 * 2 * ratio);
        forceRepaint();
    }

    public void setBorderColor(Color[] col) {
        borderColor = col;
        this.border = new RoundedBorder(this, borderWidth, arc[0], borderColor, borderType);
        forceRepaint();
    }

    public void setBorderWidth(int[] value) {
        if (WebDocument.scale_borders) {
            for (int i = 0; i < 4; i++) {
                value[i] = (int)Math.round(value[i]*ratio);
            }
        }
        borderWidth = value;
        this.border = new RoundedBorder(this, borderWidth, arc[0], borderColor, borderType);
        document.root.performLayout();
        forceRepaint();
    }

    public void setBorderType(int value) {
        for (int i = 0; i < 4; i++) {
            borderType[i] = value;
        }
        this.border = new RoundedBorder(this, borderWidth, arc[0], borderColor, borderType);
        forceRepaint();
    }

    public void setBorderType(int[] value) {
        borderType = value;
        this.border = new RoundedBorder(this, borderWidth, arc[0], borderColor, borderType);
        forceRepaint();
    }

    public void updateAbsolutePositionedChildren() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).positioning == Position.ABSOLUTE) {
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

        doIncrementLayout(viewport_width, viewport_height, false);
        forceRepaint();
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

        doIncrementLayout(viewport_width, viewport_height, false);
        forceRepaint();
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

    public void setWidth(int w, boolean no_recalc) {
        int old_width = viewport_width;
        int old_height = viewport_height;

        if (isImage) {
            width = viewport_width = w >= 0 ? (int)Math.round(w*ratio) : -1;
            orig_width = w;
            if (max_width > 0 && width > max_width) {
                width = max_width;
            }
            auto_width = width < 0;
            doIncrementLayout(old_width, old_height, no_recalc);
            return;
        }

        if (w < 0) {
            if (parent != null) {
                if (line == null) {
                    _x_ = margins[3];
                    width = parent.width-parent.borderWidth[1]-parent.borderWidth[3]-margins[1]-_x_-parent.paddings[3]-parent.paddings[1];
                } else {
                    width = line.getWidth()-margins[1]-margins[3];
                }
                orig_width = (int)Math.round(width / ratio);
            } else if (document != null) {
                _x_ = margins[3];
                width = document.width-document.borderSize*2-margins[1]-_x_;
                orig_width = (int)Math.round(width / ratio);
            }
            if (max_width > 0 && width > max_width) width = max_width;
            auto_width = true;
            rules_for_recalc.put("width", "auto");
        } else {
            width = (int)Math.round(w*ratio);
            orig_width = w;
            if (max_width > 0 && width > max_width) width = max_width;
            rules_for_recalc.remove("width");
            auto_width = false;
            viewport_width = width;
            if (scrollbar_y != null) {
                viewport_width = width - scrollbar_y.getPreferredSize().width;
            }
        }
        if (auto_x_margin && !auto_width) {
            setAutoXMargin();
        }

        doIncrementLayout(old_width, old_height, no_recalc);

        if (!no_draw) {
            forceRepaint();
        }
    }

    public void setWidth(int w) {
        setWidth(w, false);
    }

    public void setWidth(int w, int units) {
        int value = 0;
        if (units == Units.px) {
            value = w;
        }
        else if (units == Units.percent) {
            value = (int)Math.round((double) viewport_width / 100 * w);
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * w);
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
        if (max_width > 0 && width > max_width) width = max_width;
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

        doIncrementLayout(old_width, old_height, false);

        if (!no_draw) {
            forceRepaint();
        }
    }

    public boolean no_draw = false;
    public boolean no_layout = false;

    public void setHeight(int h, boolean no_recalc) {
        int old_width = viewport_width;
        int old_height = viewport_height;

        if (isImage) {
            height = viewport_height = (int)Math.round(h*ratio);
            viewport_height = height;
            orig_height = h;
            max_height = height;
            auto_height = false;
            doIncrementLayout(old_width, old_height, no_recalc);
            return;
        }
        
        if (h < 0 && document != null) {
            if (textContent == null) {
                height = borderWidth[0] + paddings[0] + paddings[2] + borderWidth[2];
            } else {
                height = content_y_max + paddings[2] + borderWidth[2];
            }
            orig_height = (int)Math.round(height / ratio);
            auto_height = true;
        } else {
            height = (int)Math.round(h*ratio);
            viewport_height = height;
            orig_height = h;
            max_height = height;
            auto_height = false;
            viewport_height = height;
            if (scrollbar_x != null) {
                viewport_height = height - scrollbar_x.getPreferredSize().height;
            }
        }
        if (auto_y_margin) {
            setAutoYMargin();
        }
        
        doIncrementLayout(old_width, old_height, no_recalc);
        
        if (!no_draw) {
            forceRepaint();
        }
    }

    public void setHeight(int h) {
        setHeight(h, false);
    }

    public void setHeight(int h, int units) {
        int value = 0;
        if (units == Units.px) {
            value = h;
        }
        else if (units == Units.percent) {
            value = (int)Math.round((double) viewport_width / 100 * h);
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * h);
        }
        setHeight(value, false);
    }

    public void doIncrementLayout(int old_width, int old_height, boolean no_recalc) {
        if (document == null || document.inLayout) return;

        double old_pos_x = old_width > 0 ? (double) background_pos_x / old_width : 0;
        double old_pos_y = old_height > 0 ? (double) background_pos_y / old_height : 0;
        double old_size_x = old_width > 0 ? (double) background_size_x / old_width : 1;
        double old_size_y = old_height > 0 ? (double) background_size_y / old_height : 1;

        if (layouter != null) {
            performLayout(true);
            if (viewport_height != old_height && bgImage != null) {
                background_pos_x = (int) (old_pos_x * viewport_width);
                background_pos_y = (int) (old_pos_y * viewport_height);
                background_size_x = (int) Math.max(0, old_size_x * viewport_width);
                background_size_y = (int) Math.max(0, old_size_y * viewport_height);
            }
            Block b = this;
            while (old_width != b.viewport_width && old_height != b.viewport_height && b != null && !no_recalc) {
                b = b.parent;
                old_width = b.viewport_width;
                old_height = b.viewport_height;
                b.performLayout(true);
            }
            // This will be called inside performLayout() for root element
            if (b != document.root) b.setZIndices();

            //document.root.setZIndices();
            document.root.clipScrollbars();

        } else if (document.ready) {
            document.root.performLayout();
            document.root.forceRepaint();
            if (viewport_height != old_height && bgImage != null) {
                background_pos_x = (int) (old_pos_x * viewport_width);
                background_pos_y = (int) (old_pos_y * viewport_height);
                background_size_x = (int) Math.max(0, old_size_x * viewport_width);
                background_size_y = (int) Math.max(0, old_size_y * viewport_height);
            }
        }
        document.repaint();
    }

    public void setBackgroundPositionX(double val, int units) {
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
        }
        else if (units == Units.percent) {
            int bw = background_size_x < 0 && bgImage != null ? bgImage.getWidth() : background_size_x;
            value = (int)Math.round((width-borderWidth[1]-borderWidth[3]-bw)*(val/100));
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * val);
        }
        background_pos_x = value;
        forceRepaint();
    }

    public void setBackgroundPositionY(double val, int units) {
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
        }
        else if (units == Units.percent) {
            int bh = background_size_y < 0 && bgImage != null ? bgImage.getHeight() : background_size_y;
            value = (int)Math.round((height-borderWidth[0]-borderWidth[2]-bh)*(val/100));
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * val);
        }
        background_pos_y = value;
        forceRepaint();
    }

    public void setBackgroundSizeX(double val, int units) {
        if (val < 0) {
            background_size_x_auto = true;
            forceRepaint();
            return;
        }
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
        }
        else if (units == Units.percent) {
            value = (int)Math.ceil((width-borderWidth[1]-borderWidth[3])*(val/100));
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * val);
        }
        background_size_x = value;
        background_size_x_auto = false;
        if (background_size_y_auto && bgImage != null && bgImage.getHeight() > 0) {
            background_size_y = (int)Math.round(background_size_x / ((double) bgImage.getWidth() / bgImage.getHeight()));
        }
        forceRepaint();
    }

    public void setBackgroundSizeY(double val, int units) {
        if (val < 0) {
            background_size_y_auto = true;
            forceRepaint();
            return;
        }
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
        }
        else if (units == Units.percent) {
            value = (int)Math.ceil((height-borderWidth[0]-borderWidth[2])*(val/100));
        }
        else if (units == Units.em) {
            value = (int)Math.round(14 * ratio * val);
        }
        background_size_y = value;
        background_size_y_auto = false;
        if (background_size_x_auto && bgImage != null) {
            background_size_x = (int)Math.round(background_size_y * ((double) bgImage.getWidth() / bgImage.getHeight()));
        }
        forceRepaint();
    }

    public void setBackgroundSizeXY(double val_x, double val_y, int units) {
        if (val_x < 0) background_size_x_auto = true;
        if (val_y < 0) background_size_y_auto = true;

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
            value_x = (int)Math.round(14 * ratio * value_x);
            value_y = (int)Math.round(14 * ratio * value_y);
        }
        background_size_x = value_x;
        background_size_y = value_y;
        background_size_x_auto = val_x < 0;
        background_size_y_auto = val_y < 0;

        if (!background_size_x_auto && background_size_y_auto && bgImage != null && bgImage.getHeight() > 0) {
            background_size_y = (int)Math.round(background_size_x / ((double) bgImage.getWidth() / bgImage.getHeight()));
        }
        else if (background_size_x_auto && !background_size_y_auto && bgImage != null && bgImage.getHeight() > 0) {
            background_size_x = (int)Math.round(background_size_y * ((double) bgImage.getWidth() / bgImage.getHeight()));
        }
        else if (background_size_x_auto && background_size_y_auto && bgImage != null && bgImage.getHeight() > 0) {
            background_size_x = bgImage.getWidth();
            background_size_y = bgImage.getHeight();
        }

        forceRepaint();
    }

    public void setBackgroundSizeAuto() {
        setBackgroundSizeXY(-1f, -1f, Units.px);
    }

    public void setBackgroundContain() {
        if (width < 0 || height < 0) return;
        double r1 = (bgImage != null && bgImage.getHeight() > 0) ? (double) bgImage.getWidth() / bgImage.getHeight() : 0;
        double r2 = height > 0 ? (double) width / height : 0;
        if (r1 > r2) {
            background_size_y_auto = true;
            setBackgroundSizeX(100, Units.percent);
        } else {
            background_size_x_auto = true;
            setBackgroundSizeY(100, Units.percent);
        }
        setBackgroundPositionX(50, Units.percent);
        setBackgroundPositionY(50, Units.percent);
        forceRepaint();
    }

    public void setBackgroundCover() {
        double r1 = (bgImage != null && bgImage.getHeight() > 0) ? (double) bgImage.getWidth() / bgImage.getHeight() : 0;
        double r2 = height > 0 ? (double) width / height : 0;
        if (r1 < r2) {
            background_size_y_auto = true;
            setBackgroundSizeX(100, Units.percent);
        } else {
            background_size_x_auto = true;
            setBackgroundSizeY(100, Units.percent);
        }
        setBackgroundPositionX(50, Units.percent);
        setBackgroundPositionY(50, Units.percent);
        forceRepaint();
    }

    public void setBackgroundRepeat(int value) {
        background_repeat = value;
        forceRepaint();
    }

    public void setPositioning(int value) {
        positioning = value;
        doIncrementLayout(viewport_width, viewport_height, false);
    }

    public void setDisplayType(int value) {
        display_type = value;
        if (display_type != Display.BLOCK && (auto_width || display_type == Display.INLINE)) {
            width = borderWidth[3] + paddings[3] + paddings[1] + borderWidth[1];
            if (height < 0 || display_type == Display.INLINE) {
                height = fontSize + borderWidth[0] + paddings[0] + paddings[2] + borderWidth[2];
            }
        }
        if (display_type == Display.INLINE_BLOCK && auto_width) {
            width = 0;
            viewport_width = 0;
        }
        if (display_type == Display.NONE) {
            removeTextLayers();
        }
        if (document != null && document.ready) {
            doIncrementLayout(viewport_width, viewport_height, false);
            forceRepaint();
            document.validate();
        }
    }

    private void removeTextLayers() {
        if (text_layer != null) {
            remove(text_layer);
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).removeTextLayers();
            for (int j = 0; j < children.get(i).parts.size(); j++) {
                children.get(i).parts.get(j).removeTextLayers();
            }
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

    public void setProp(String prop, String value) {
        if (original != null) {
            original.setProp(prop, value);
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

        if (prop.equals("color")) {
            setTextColor(value);
            return;
        }

        if (prop.equals("background")) {
            String[] parts = value.split("\\s+");
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
            if (value.matches("^[0-9.]+[0-9](px|em|%)$")) {
                int[] val = parseValueString(value);
                setBackgroundPositionX(val[0], val[1]);
            }
        }

        if (prop.equals("background-position-y")) {
            if (value.matches("^[0-9.]+[0-9](px|em|%)$")) {
                int[] val = parseValueString(value);
                setBackgroundPositionY(val[0], val[1]);
            }
        }

        if (prop.equals("background-size-x")) {
            if (value.matches("^[0-9.]+[0-9](px|em|%)$")) {
                int[] val = parseValueString(value);
                setBackgroundSizeX(val[0], val[1]);
            }
        }

        if (prop.equals("background-size-y")) {
            if (value.matches("^[0-9.]+[0-9](px|em|%)$")) {
                int[] val = parseValueString(value);
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


        if (prop.matches("border(-left|-right|-bottom|-top)?")) {
            String[] s = value.split("\\s+");
            HashMap<String, Integer> types = new HashMap<String, Integer>();
            types.put("solid", 0);
            types.put("dashed", 1);
            types.put("dotted", 2);
            for (int i = 0; i < s.length; i++) {
                if (s[i].matches("^[0-9]+.*(px|em)$")) {
                    String ch = s[i].substring(0, 1);
                    String n = "";
                    int index = 0;
                    while (ch.matches("[0-9.]")) {
                        n += ch;
                        index++;
                        ch = s[i].substring(index, index+1);
                    }
                    String u = s[i].substring(index);
                    if (!u.matches("px|em")) return;
                    int val = (int)Math.round(Float.parseFloat(n) * ratio);
                    if (u.equals("em")) val = (int)Math.round(Integer.parseInt(n) * 14 * ratio);

                    if (prop.contains("-left") || prop.equals("border")) this.borderWidth[3] = val;
                    if (prop.contains("-right") || prop.equals("border")) this.borderWidth[1] = val;
                    if (prop.contains("-bottom") || prop.equals("border")) this.borderWidth[2] = val;
                    if (prop.contains("-top") || prop.equals("border")) this.borderWidth[0] = val;
                } else if (s[i].matches("^(#|rgba?).*") || !types.containsKey(s[i])) {
                    Color col = parseColor(s[i]);
                    if (prop.contains("-left") || prop.equals("border")) this.borderColor[3] = col;
                    if (prop.contains("-right") || prop.equals("border")) this.borderColor[1] = col;
                    if (prop.contains("-bottom") || prop.equals("border")) this.borderColor[2] = col;
                    if (prop.contains("-top") || prop.equals("border")) this.borderColor[0] = col;
                } else {
                    if (types.containsKey(s[i])) {
                        int t = types.get(s[i]);
                        if (prop.contains("-left") || prop.equals("border")) this.borderType[3] = t;
                        if (prop.contains("-right") || prop.equals("border")) this.borderType[1] = t;
                        if (prop.contains("-bottom") || prop.equals("border")) this.borderType[2] = t;
                        if (prop.contains("-top") || prop.equals("border")) this.borderType[0] = t;
                    }
                }
            }
            this.border = new RoundedBorder(this, this.borderWidth, this.arc[0], this.borderColor, this.borderType);
            forceRepaint();
            return;
        }

        if (prop.endsWith("z-index")) {
            setZIndex(value);
            return;
        }

        if (prop.endsWith("user-select")) {
            setSelectionEnabled(!value.equals("none"));
            return;
        }

        if (prop.endsWith("cursor")) {
            setCursor(value);
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
            int value = 0;
            if (units == Units.px) {
                value = (int)Math.round(val);
                rules_for_recalc.remove(prop);
            }
            else if (units == Units.percent) {
                if (parent != null) {
                    value = (int)Math.round(parent.width * (val / 100));
                } else {
                    value = (int)Math.round(document.width * (val / 100));
                }
                rules_for_recalc.put(prop, val + "%");
            }
            else if (units == Units.em) {
                value = (int)Math.round(16 * val);
                rules_for_recalc.remove(prop);
            }
            if (prop.matches("margin(-left)?")) {
                margins[3] = (int)Math.round(value * ratio);
            }
            if (prop.matches("margin(-right)?")) {
                margins[1] = (int)Math.round(value * ratio);
            }
            if (prop.matches("margin(-top)?")) {
                margins[0] = (int)Math.round(value * ratio);
            }
            if (prop.matches("margin(-bottom)?")) {
                margins[2] = (int)Math.round(value * ratio);
            }
            doIncrementLayout(viewport_width, viewport_height, false);
            return;
        }
        if (prop.matches("border(-left|-right|-top|-bottom)-width")) {
            if (prop.contains("-left")) this.borderWidth[3] = (int)val;
            if (prop.contains("-right")) this.borderWidth[1] = (int)val;
            if (prop.contains("-top")) this.borderWidth[0] = (int)val;
            if (prop.contains("-bottom")) this.borderWidth[2] = (int)val;
            if (WebDocument.scale_borders) {
                for (int i = 0; i < 4; i++) {
                    this.borderWidth[i] = (int)Math.round(this.borderWidth[i]*ratio);
                }
            }
            this.border = new RoundedBorder(this, this.borderWidth, this.arc[0], this.borderColor, this.borderType);
            forceRepaint();
            return;
        }
    }

    private int[] parseValueString(String value) {
        if (value.matches("^[0-9.]+[0-9](px|em|%)$")) {
            String ch = value.substring(0, 1);
            String n = "";
            int index = 0;
            while (ch.matches("[0-9.]")) {
                n += ch;
                index++;
                ch = value.substring(index, index+1);
            }
            String u = value.substring(index);
            int val = (int)Math.round(Float.parseFloat(n) * ratio);
            int units = u.equals("px") ? Units.px : (u.equals("em") ? Units.em : Units.percent);
            return new int[] {val, units};
        }
        return null;
    }

    public void setLeft(double val, int units) {
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
            rules_for_recalc.remove("left");
        }
        else if (units == Units.percent) {
            if (parent != null) {
                Block p = parent;
                while (p != null && !p.isRoot()) {
                    p = p.parent;
                }
            }
            int w = parent != null ? parent.width - parent.borderWidth[1] - parent.borderWidth[3] :
                                     document.width - document.borderSize * 2;
            value = (int)Math.round(w * (val / 100));
            rules_for_recalc.put("left", val + "%");
        }
        else if (units == Units.em) {
            value = (int)Math.round(16 * val);
            rules_for_recalc.remove("left");
        }
        if (!auto_width && auto_right) {
            right = left + width;
        }
        if (auto_width && !auto_right) {
            width = right - left;
        }
        auto_left = false;
        left = units != Units.percent ? (int)Math.round(value * ratio) : value;
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
                forceRepaint();
            }
            else if (positioning == Block.Position.ABSOLUTE) {
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
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
            rules_for_recalc.remove("right");
        }
        else if (units == Units.percent) {
            if (parent != null) {
                Block p = parent;
                while (p != null && !p.isRoot()) {
                    p = p.parent;
                }
            }
            int w = parent != null ? parent.width - parent.borderWidth[1] - parent.borderWidth[3] :
                                     document.width - document.borderSize * 2;
            value = (int)Math.round(w * (1 - (val / 100)));
            rules_for_recalc.put("right", val + "%");
        }
        else if (units == Units.em) {
            value = (int)Math.round(16 * val);
            rules_for_recalc.remove("right");
        }
        if (!auto_width && auto_left) {
            left = right - width;
        }
        if (auto_width && !auto_left) {
            width = right - left;
        }
        auto_right = false;
        right = units != Units.percent ? (int)Math.round(value * ratio) : value;
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE) {
                if (parent != null) {
                    setX(parent.borderWidth[3] + margins[3] + left);
                } else {
                    setX(left);
                }
            }
            forceRepaint();
            while (b.parent != null) b = b.parent;
            b.repaint();
        }
    }

    public void setTop(double val, int units) {
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
            rules_for_recalc.remove("top");
        }
        else if (units == Units.percent) {
            if (parent != null) {
                Block p = parent;
                while (p != null && !p.isRoot()) {
                    p = p.parent;
                }
            }
            int h = parent != null ? parent.height - parent.borderWidth[0] - parent.borderWidth[2] :
                                     document.height - document.borderSize * 2;
            value = (int)Math.round(h * (val / 100));
            rules_for_recalc.put("top", val + "%");
        }
        else if (units == Units.em) {
            value = (int)Math.round(16 * val);
            rules_for_recalc.remove("top");
        }
        auto_top = false;
        top = units != Units.percent ? (int)Math.round(value * ratio) : value;
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE) {
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
        int value = 0;
        if (units == Units.px) {
            value = (int)Math.round(val);
            rules_for_recalc.remove("bottom");
        }
        else if (units == Units.percent) {
            if (parent != null) {
                Block p = parent;
                while (p != null && !p.isRoot()) {
                    p = p.parent;
                }
            }
            int h = parent != null ? parent.height - parent.borderWidth[0] - parent.borderWidth[2] :
                                     document.height - document.borderSize * 2;
            value = (int)Math.round(h * (1 - (val / 100)));
            rules_for_recalc.put("bottom", val + "%");
        }
        else if (units == Units.em) {
            value = (int)Math.round(16 * val);
            rules_for_recalc.remove("bottom");
        }
        auto_bottom = false;
        bottom = units != Units.percent ? (int)Math.round(value * ratio) : value;
        Block b = this;
        if (!no_draw && positioning != Block.Position.STATIC) {
            if (positioning == Block.Position.RELATIVE && parent != null) {
                parent.performLayout(true);
            }
            else if (positioning == Block.Position.ABSOLUTE) {
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

    public void addText(String text) {
        addText(text, children.size());
    }

    public void addText(String text, int pos) {
        Block tb = new Block(document, this, 0, 0, 0, 0, Color.BLACK);
        tb.display_type = 2;
        tb.textContent = text;
        tb.type = NodeTypes.TEXT;
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
        //if (!no_layout) performLayout();
        //forceRepaint();
    }

    public void addElement(Block d) {
        addElement(d, children.size(), false);
    }

    public void addElement(Block d, boolean preserve_style) {
        addElement(d, children.size(), preserve_style);
    }

    public void addElement(Block d, int pos) {
        addElement(d, children.size(), false);
    }

    public void addElement(Block d, int pos, boolean preserve_style) {
        type = NodeTypes.ELEMENT;
        if (children.contains(d)) {
            System.err.println("Duplicate block insert");
            return;
        }
        if (pos < children.size()) {
            children.add(pos, d);
        } else {
            children.add(d);
        }
        
        document.root.add(d, 0);
        d.pos = getComponentCount()-1;

        Block b = this;
        while (b.parent != null) {
            if (b.hasParentLink || b.href != null) {
                d.hasParentLink = true;
            }
            b = b.parent;
        }
        if (b.width <= 0 || b.height <= 0) {
            b = document.root;
        }
        d.setBounds(0, 0, b.width, b.height);

        d.document = document;

        d.parent = this;
        d.alpha = alpha;
        if (!preserve_style) {
            d.color = color;
            d.fontSize = fontSize;
            d.fontFamily = fontFamily;
            d.text_bold = text_bold;
            d.text_italic = text_italic;
            d.text_underline = text_underline;
            d.text_strikethrough = text_strikethrough;
            d.linksUnderlineMode = linksUnderlineMode;
        }
        if (document.prevent_mixed_content) {
            normalizeContent();
        }
        //performLayout();
        //forceRepaint();
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
                    b.addText(children.get(i).textContent);
                    children.remove(i);
                    addElement(b, i, true);
                }
            }
        }
    }

    public void addChildDocument(WebDocument d) {
        removeAllElements();
        childDocument = d;
        d.parent_document = this.document;
        d.parent_document_block = this;
    }

    public void removeChildDocument() {
        if (childDocument == null) return;
        childDocument.getRoot().removeAllElements();
        childDocument.parent_document = null;
        childDocument.parent_document_block = null;
        childDocument = null;
    }

    public void removeElement(Block d) {
        if (d == null || !(d instanceof Block) || !children.contains(d)) {
            System.err.println("Child not found");
            return;
        }
        d.hasParentLink = false;
        d.removeTextLayers();
        d.removeAllElements();
        if (document != null) {
            document.root.remove(d);
            document.root.forceRepaint();
        }
        children.remove(d);
        //performLayout();
        //forceRepaint();
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
        //performLayout();
        //forceRepaint();
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
        String[] types = new String[] {"block", "inline-block", "inline", "none", "table", "inline-table", "table-row", "table-cell"};
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

    public int display_type = 0;
    public int visibility = 0;
    public int float_type = 0;
    public int clear_type = 0;

    public int letter_spacing = 0;
    public int word_spacing = 0;
    private Color selection_color = new Color(0, 0, 196, 186);
    private int[] sel = null;
    private volatile int from_x = -1;
    private volatile int from_y = -1;

    private int content_x_max = 0;
    private int content_y_min = Integer.MAX_VALUE;
    private int content_y_max = 0;

    public htmlparser.Node node;
    public bridge.Builder builder;


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
    }

    public static class Visibility {
        public static final int VISIBLE = 0;
        public static final int HIDDEN = 1;
    }

    public static class Units {
        public static final int px = 0;
        public static final int percent = 1;
        public static final int em = 2;
    }

    public int max_width;
    public int max_height;

    public int width;
    public int height;
    public int[] borderWidth = {0, 0, 0, 0};
    public int borderRadius;
    public int[] arc = {0, 0, 0, 0};
    public Color color = Color.BLACK;
    public Color bgcolor;
    public int[] borderType = {0, 0, 0, 0};
    public Color[] borderColor = {Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK};
    public String fontFamily = "Tahoma";
    public int fontSize = 14;

    public boolean text_bold = false;
    public boolean text_italic = false;
    public boolean text_underline = false;
    public boolean text_strikethrough = false;

    public int text_align = 0;
    public int positioning = 0;
    public int overflow = 0;
    public int vertical_align = 2;
    public boolean sharp = false;
    public float alpha = 1.0f;
    
    public float bg_alpha = 1.0f;
    public BufferedImage bgImage = null;
    public int background_repeat = 0;
    public int background_size_x = -1;
    public int background_size_y = -1;
    public boolean background_size_x_auto = true;
    public boolean background_size_y_auto = true;
    public int background_pos_x = 0;
    public int background_pos_y = 0;
    public Gradient gradient = null;
    public boolean isImage = false;

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

    public JPanel text_layer;

    public int viewport_width;
    public int viewport_height;

    JScrollBar scrollbar_x;
    JScrollBar scrollbar_y;

    protected int scroll_x = 0;
    protected int scroll_y = 0;

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

    public HashMap<String, String> rules_for_recalc;
    public HashMap<String, Object> originalStyles;
    public HashMap<String, String> cssStyles;

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

        b.viewport_width = viewport_width;
        b.viewport_height = viewport_height;

        b.left = this.left;
        b.right = this.right;
        b.top = this.top;
        b.bottom = this.bottom;

        b.href = href;
        b.hasParentLink = hasParentLink;
        b.linksUnderlineMode = linksUnderlineMode;

        b.fontSize = this.fontSize;
        b.fontFamily = this.fontFamily;
        b.text_bold = this.text_bold;
        b.text_italic = this.text_italic;
        b.text_underline = this.text_underline;
        b.text_strikethrough = this.text_strikethrough;

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

        b.bgImage = this.bgImage;
        if (bgImage != null) {
            b.background_pos_x = this.background_pos_x;
            b.background_pos_y = this.background_pos_y;
            b.background_size_x = this.background_size_x;
            b.background_size_y = this.background_size_y;

            b.has_animation = has_animation;
            b.animation_frames = animation_frames;
        }
        b.gradient = this.gradient;
        b.bgcolor = this.bgcolor;
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

        b.zIndex = this.zIndex;
        b.zIndexAuto = this.zIndexAuto;

        b.childDocument = this.childDocument;

        b.originalStyles = originalStyles;
        b.cssStyles = cssStyles;

        b.node = node;
        b.builder = builder;
        b.special = special;

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

        if (childDocument != null) {
            childDocument.getRoot().mouseClicked(e);
            return;
        }

        Block[] blocks = new Block[document.root.layer_list.size()];
        document.root.layer_list.toArray(blocks);

        if (!(children.size() == 1 && children.get(0).type == NodeTypes.TEXT)) {
            for (int i = 0; i < blocks.length; i++) {
                Block b = blocks[i].original != null ? blocks[i].original : blocks[i];
                if (children.contains(b)) {
                    //MouseEvent evt = new MouseEvent((Block)d, 0, 0, 0, e.getX() - b._x_, e.getY() - b._y_, 1, false);
                    blocks[i].mouseClicked(e);
                }
            }
        }
        
        if (isMouseInside(e.getX(), e.getY()) && href != null || hasParentLink) {
            if (document.active_block != null && document.active_block.node != null) {
                document.active_block.node.states.remove("active");
                document.active_block.resetStyles();
                document.active_block.applyStateStyles();
            }
            
            node.states.add("active");
            document.active_block = this;

            if (href != null) {
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
        if (System.currentTimeMillis() - last_click < 320 && select_enabled) {
            int x = e.getX();
            int y = e.getY();
            if (textRenderingMode == 1) {
                if (!(children.size() == 1 && children.get(0).type == NodeTypes.TEXT)) {
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
            for (int i = 0; i < text_layer.getComponents().length; i++) {
                JLabel c = (JLabel)text_layer.getComponents()[i];
                if (x >= c.getX() && x <= c.getX() + c.getWidth() &&
                    y >= c.getY() && y <= c.getY() + c.getHeight()) {
                    int i1 = i;
                    while (i1 >= 0 && ((JLabel)text_layer.getComponents()[i1]).getText().matches("\\w")) {
                        ((JLabel)text_layer.getComponents()[i1]).setBackground(selection_color);
                        ((JLabel)text_layer.getComponents()[i1]).setForeground(Color.WHITE);
                        ((JLabel)text_layer.getComponents()[i1]).setOpaque(true);
                        i1--;
                    }
                    if (i1 < 0 || !((JLabel)text_layer.getComponents()[i1]).getText().matches("\\w")) {
                        i1++;
                    }
                    int i2 = i;
                    while (i2 <= text_layer.getComponents().length-1 && ((JLabel)text_layer.getComponents()[i2]).getText().matches("\\w")) {
                        ((JLabel)text_layer.getComponents()[i2]).setBackground(selection_color);
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
        last_click = System.currentTimeMillis();
    }

    public void mousePressed(MouseEvent e) {
        Block d = this;
        while (d.parent != null || d.document.getParentDocument() != null) {
            if (d.parent != null) d = d.parent;
            else d = d.document.getParentDocumentBlock();
        }
        d.clearSelection();
        d.forceRepaint();
        document.repaint();
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
    }

    public void mouseReleased(MouseEvent e) {
        getSelectedText();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {
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

    private void selectCharacter(Character c) {
        if (c.glyph == null) {
            c.setBackground(selection_color);
            c.setColor(Color.WHITE);
            return;
        }
        if (!c.glyph.isOpaque()) {
            //System.out.println("Selected line element '" + c.getText() + "'");
            c.glyph.setOpaque(true);
            c.glyph.setForeground(Color.WHITE);
            c.glyph.setBackground(selection_color);
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
        gc.setBackground(bgcolor);
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
        
        if (children.size() == 1 && children.get(0).type == NodeTypes.TEXT) {
            color = col;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        for (int i = 0; i < v.size(); i++) {
            Drawable d = v.get(i);

            Rectangle bounds = new Rectangle(d._getX() - scroll_x, d._getY() - scroll_y, d._getWidth(), d._getHeight());

            if (d != null && d instanceof Block) {
                Block b = (Block) d;
                b.processLinks(x, y);
                b.updateStates(x, y);
            }
        }
        Block b = this.original == null ? this : this.original;
        if (b.children.size() == 1 && (b.children.get(0).type == NodeTypes.TEXT || b.children.get(0) instanceof YouTubeThumb)) {
            b.children.get(0).processLinks(x, y);
            b.children.get(0).updateStates(x, y);
        } else {
            for (int i = 0; i < b.children.size(); i++) {
                b.children.get(i).mouseMoved(e);
            }
        }
        if (this == document.root) document.repaint();
    }

    private void processLinks(int x, int y) {
        boolean flag = false;
        if (parts.size() > 0) {
            for (int j = 0; j < parts.size(); j++) {
                Block p = parts.get(j);
                if (x >= p._x_ && x <= p._x_ + p.width && y >= p._y_ && y <= p._y_ + p.height &&
                      (p.hasParentLink || p.href != null) && !p.hovered) {
                    p.hovered = true;
                    p.originalStyles.put("text_underline", p.text_underline);
                    p.originalStyles.put("text_color", p.color);
                    p.text_underline = true;
                    p.color = p.linkColor;
                    document.panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    flag = true;
                } else if (!(x >= p._x_ && x <= p._x_ + p.width && y >= p._y_ && y <= p._y_ + p.height) &&
                        (!p.hasParentLink && p.href == null || p.linksUnderlineMode == 1) && p.originalStyles.containsKey("text_underline")) {
                     p.hovered = false;
                     p.text_underline = (Boolean) (p.originalStyles.get("text_underline"));
                     p.color = (Color) p.originalStyles.get("text_color");
                     p.originalStyles.remove("text_underline");
                     p.originalStyles.remove("text_color");
                     document.panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                     flag = true;
                }
            }
        } else {
            if (x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height &&
                  (hasParentLink || href != null) && !hovered) {
                hovered = true;
                originalStyles.put("text_underline", text_underline);
                originalStyles.put("text_color", color);
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
            }
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

    private void updateStates(int x, int y) {
        if (parts.size() > 0) {
            for (int j = 0; j < parts.size(); j++) {
                Block p = parts.get(j);
                if (x >= p._x_ && x <= p._x_ + p.width && y >= p._y_ && y <= p._y_ + p.height) {
                    p.hovered = true;
                    if (p.node != null && !p.node.states.contains("hover")) {
                        p.node.states.add("hover");
                        p.applyStateStyles();
                        if (cursor != null) {
                            document.panel.setCursor(cursor);
                            //System.out.println("Cursor set");
                        }
                        //System.err.println(node.tagName + " Hovered!");
                    }
                } else if (!(x >= p._x_ && x <= p._x_ + p.width && y >= p._y_ && y <= p._y_ + p.height)) {
                     p.hovered = false;
                     if (p.node != null && p.node.states.contains("hover")) {
                         p.node.states.remove("hover");
                         p.resetStyles();
                         p.applyStateStyles();
                         if (cursor != null) {
                             document.panel.setCursor(Cursor.getDefaultCursor());
                             //System.out.println("Cursor unset");
                         }
                         //System.err.println(node.tagName + " Out!");
                     }
                }
            }
        } else {
            if (x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height) {
                hovered = true;
                if (node != null && !node.states.contains("hover")) {
                    node.states.add("hover");
                    applyStateStyles();
                    if (cursor != null) {
                        document.panel.setCursor(cursor);
                        //System.out.println("Cursor set");
                    }
                    //System.err.println(node.tagName + " Hovered!");
                }
            } else if (!(x >= _x_ && x <= _x_ + width && y >= _y_ && y <= _y_ + height)) {
                 hovered = false;
                 if (node != null && node.states.contains("hover")) {
                     node.states.remove("hover");
                     resetStyles();
                     applyStateStyles();
                     if (cursor != null) {
                         document.panel.setCursor(Cursor.getDefaultCursor());
                         //System.out.println("Cursor unset");
                     }
                     //System.err.println(node.tagName + " Out!");
                 }
            }
        }
    }


    public boolean selected = false;
    private boolean hovered;
    private Watcher w;

    Vector<Drawable> v = new Vector<Drawable>();
    public int textRenderingMode = 0;

}
