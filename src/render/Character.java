package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
    }

    public Character(Line line, char ch) {
        this.line = line;
        setText(ch);
    }

    public Character(Line line, String str) {
        this.line = line;
        textContent = str;
    }

    public void doPaint(Graphics g) {
        forceRepaint();
    }

    public void forceRepaint() {
        buffer = null;
        //draw();
    }

    public void forceRepaint(Graphics g) {
        buffer = null;
        //draw(g);
    }

    protected void draw() {
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
        g2d.setColor(color);
        @SuppressWarnings("unchecked")
        Map<String, String> desktopHints = (Map<String, String>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (font.getSize() >= 21) {
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
        FontMetrics fn = this.getFontMetrics(font);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.drawString(textContent, left, top + fn.getHeight() - 3);
        System.out.println(left + ", " + top);
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
