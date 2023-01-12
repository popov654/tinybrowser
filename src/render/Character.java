package render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.JLabel;

/**
 *
 * @author Alex
 */
public class Character extends JPanel implements Drawable {

    public Character(Line line) {
        this.line = line;
        setBackground(null);
    }

    public Character(Line line, char ch) {
        this.line = line;
        setText(ch);
        setBackground(null);
    }

    public Character(Line line, String str) {
        this.line = line;
        textContent = str;
        setBackground(null);
    }

    public void doPaint(Graphics g) {
        forceRepaint();
    }

    public void forceRepaint() {
        buffer = null;
        draw();
    }

    public void forceRepaint(Graphics g) {
        buffer = null;
        //draw(g);
    }

    public void draw() {
        if (textContent == null) return;
        if (buffer == null && width > 0 && height > 0) {
            buffer = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        if (buffer == null) return;
        Graphics g = buffer.getGraphics();
        draw(g);
    }

    protected void draw(Graphics g) {
        //setBounds(parent.getOffsetLeft()+left, parent.getOffsetTop()+top, width, height);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setFont(font);

        Shape rect = new Rectangle2D.Double(left, top, width, height);

//        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1f);
//        g2d.setColor(line.block.bgcolor);
//        g2d.setComposite(composite);
//
//        g2d.fill(rect);
//
//        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
//        g2d.setComposite(composite);


        if (getBackground() != null) {
            g2d.setColor(getBackground());
            g2d.fillRect(left, top, width, height);
        }

        Block block = line.parent;
        if (block.original != null) block = block.original;
        Color col = block.hasParentLink || block.href != null ? block.linkColor : color;
        g2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)Math.round(col.getAlpha() * block.alpha)));

        //g2d.setColor(color);
        @SuppressWarnings("unchecked")
        Map<String, String> desktopHints = (Map<String, String>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (font.getSize() >= 21) {
//            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
//                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
        }
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        if (desktopHints != null) {
            g2d.addRenderingHints(desktopHints);
        }
        FontMetrics fn = this.getFontMetrics(font);
        int cw = fn.stringWidth(textContent);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.drawString(textContent, left, top + fn.getHeight() - 5);
        if (block.text_underline || block.hasParentLink && block.linksUnderlineMode == 0) {
            if (line.elements.lastElement() == this) cw -= 1;
            g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2d.drawLine(left, top + fn.getHeight() - 2, left + cw, top + fn.getHeight() - 2);
        }
        if (block.text_strikethrough) {
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2d.drawLine(left, top + fn.getHeight() / 2 + 1, left + cw, top + fn.getHeight() / 2 + 1);
        }
        //System.out.println(left + ", " + top);
    }

    @Override
    public int getWidth() {
        return getFontMetrics(font).stringWidth(textContent);
    }

    public String getText() {
        return textContent;
    }

    public void setText(char ch) {
        setText(String.valueOf(ch));
    }

    public void setText(String str) {
        textContent = str;
        //super.setText(textContent);
        if (font != null) {
            width = getFontMetrics(font).stringWidth(textContent);
            height = getFontMetrics(font).getHeight();
            setBounds(left, top, width, height);
        }
    }

    public void setColor(Color c) {
        color = c;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font f) {
        font = f;
        super.setFont(font);
        if (textContent != null) {
            width = getFontMetrics(font).stringWidth(textContent);
            height = getFontMetrics(font).getHeight();
            setBounds(left, top, width, height);
        }
    }

    public int getOffsetLeft() {
        return (line == null) ? left : line.getOffsetLeft() + left;
    }

    public int getOffsetTop() {
        return (line == null) ? top : line.getOffsetTop() + top;
    }

    @Override
    public int _getX() {
        return left;
    }

    @Override
    public int _getY() {
        return top;
    }

    public void setX(int value) {
        left = value;
        setBounds(left, top, width, height);
    }

    public void setY(int value) {
        top = value;
        setBounds(left, top, width, height);
    }
    
    public int _getWidth() {
        return width;
    }

    public int _getHeight() {
        return height;
    }

    public void setWidth(int value) {
        width = value;
        setBounds(left, top, width, height);
    }

    public void setHeight(int value) {
        height = value;
        setBounds(left, top, width, height);
    }

    public void setLine(Line l) {
        this.line = l;
    }

    @Override
    public String toString() {
        for (int i = 0; i < line.elements.size(); i++) {
            if (line.elements.get(i) == this) {
                return textContent + "[" + i + "]";
            }
        }
        return textContent;
    }

    public String textContent;

    public int left;
    public int top;

    public int width;
    public int height;

    private Font font;
    private Color color;
    public Line line;
    public JLabel glyph;

    private BufferedImage buffer;

}
