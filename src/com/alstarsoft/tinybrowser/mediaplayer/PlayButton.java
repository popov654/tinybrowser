package com.alstarsoft.tinybrowser.mediaplayer;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;

/**
 *
 * @author Alex
 */
public class PlayButton extends JButton implements MouseListener, MouseMotionListener {

    public PlayButton() {
        setOpaque(false);
        setBorder(null);
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setPreferredSize(new Dimension(size, size));
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } catch (HeadlessException ex) {}

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        //setBorder(BorderFactory.createLineBorder(Color.RED, 1));
    }

    public PlayButton(Color col1, Color col2) {
        this();
        setPrimaryColor(col1);
        setHoveredColor(col2);
    }

    public PlayButton(Color col1, Color col2, Color col3) {
        this();
        setPrimaryColor(col1);
        setHoveredColor(col2);
        setIconColor(col3);
    }

    public void setPrimaryColor(Color col) {
        colors[0] = col;
    }

    public void setHoveredColor(Color col) {
        colors[1] = col;
    }

    public void setIconColor(Color col) {
        colors[2] = col;
    }

    private Color[] colors = {new Color(78, 146, 217, 238), new Color(85, 156, 224, 245), new Color(245, 245, 245)};

    @Override
    protected void paintComponent(Graphics g) {
        
        int width = size;
        int height = size;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Color color = hovered ? new Color(105, 183, 242) : new Color(102, 178, 238);
        Color color = hovered ? colors[1] : colors[0];
        g2d.setColor(color);
        g2d.fillOval(0, 0, width, height);

        if (state > 0) {
            g2d.setColor(new Color(245, 245, 245, 255));
            g2d.fillRect((int)(width * 0.32), (int)(height * 0.3), (int)(width * 0.15), (int)(height * 0.45));
            g2d.fillRect((int)(width * 0.58), (int)(height * 0.3), (int)(width * 0.15), (int)(height * 0.45));
        } else {
            g2d.setColor(new Color(245, 245, 245, 255));
            int[] x = { (int)(width * 0.38), (int)(width * 0.76), (int)(width * 0.38) };
            int[] y = { (int)(width * 0.28), (int)(height * 0.5), (int)(height * 0.73) };
            Polygon p = new Polygon(x, y, 3);
            g2d.fillPolygon(p);
        }
    }

    public boolean hovered = false;
    public int size = 30;

    public int state = 0;

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        hovered = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        hovered = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovered = false;
    }

}
