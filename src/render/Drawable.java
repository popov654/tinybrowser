package render;

import java.awt.Graphics;

/**
 *
 * @author Alex
 */
public interface Drawable {

    public int _getX();
    public int _getY();

    public void draw();
    public void doPaint(Graphics g);
    public void forceRepaint();
    public void forceRepaint(Graphics g);

    public void setX(int value);
    public void setY(int value);

    public int _getWidth();
    public int _getHeight();

    public void setWidth(int value);
    public void setHeight(int value);

    public void setLine(Line line);
    public void selected(boolean value);

}
