package com.alstarsoft.tinybrowser.mediaplayer;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author Alex
 */
public class Slider extends JProgressBar implements MouseListener, MouseMotionListener {

    public Slider() {
        //setMinimumSize(new Dimension(size, size));
        //setMaximumSize(new Dimension(size, size));
        //setPreferredSize(new Dimension(size, size));
        setBorder(null);
        setBackground(new Color(183, 183, 188));
        setForeground(new Color(78, 146, 217));
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } catch (HeadlessException ex) {
            Logger.getLogger(Slider.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        //setBorder(BorderFactory.createLineBorder(Color.RED, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = this.height;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Color color = hovered ? new Color(105, 183, 242) : new Color(102, 178, 238);
        Color color = hovered ? new Color(85, 156, 224, 245) : new Color(78, 146, 217, 245);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, width, height, radius, radius);
        
        int w = (int) (width * ((double) getValue() / getMaximum()));

        g2d.setColor(color);

        int w1 = Math.min(radius * 2, w);
        if (w == 0) return;
        g2d.fillRoundRect(0, 0, w1, height, radius, radius);

        int r = 0;

        if (w > w1) {
            if (w > width - radius) {
                r = w - (width - radius);
                w = width - radius;
            }
            g2d.fillRect(w1-2, 0, w-radius-w1+2, height);
        }
        int w2 = Math.min(radius * 2, width);
        g2d.fillRoundRect(w - w2 / 2 - 2, 0, w2 + 2, height, radius, radius);
    }

    public boolean hovered = false;
    public int height = 5;
    public int radius = 6;

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
