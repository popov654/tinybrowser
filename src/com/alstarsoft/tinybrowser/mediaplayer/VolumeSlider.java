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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

/**
 *
 * @author Alex
 */
public class VolumeSlider extends JProgressBar implements MouseListener, MouseMotionListener {

    public VolumeSlider() {
        //setMinimumSize(new Dimension(size, size));
        //setMaximumSize(new Dimension(size, size));
        //setPreferredSize(new Dimension(size, size));
        setBorder(null);
        setBackground(new Color(183, 183, 188));
        setForeground(new Color(78, 146, 217));
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } catch (HeadlessException ex) {
            Logger.getLogger(VolumeSlider.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        //setBorder(BorderFactory.createLineBorder(Color.RED, 1));
    }

    public VolumeSlider(Color col1, Color col2) {
        this();
        setForeground(col1);
        setBackground(col2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Color color = hovered ? new Color(105, 183, 242) : new Color(102, 178, 238);
        //Color color = hovered ? new Color(85, 156, 224, 245) : new Color(78, 146, 217, 245);

        g.setColor(new Color(78, 146, 217));

        int count = (int) Math.max(1, Math.floor((double)(width - 1 + w) / (2 * w - 1)));
        int h = w;
        int delta = count > 1 ? (int)Math.floor((double)(getHeight() - h) / (count-1)) : 0;
        int x = width - (count * w  + (count - 1) * (w - 1)) - 1;
        if (count % 2 == 0) x += w;

        double p = getMaximum() / count;

        int n = (int)Math.floor(getValue() / (getMaximum() / count));

        v.clear();

        for (int i = 0; i < count; i++) {
            if (i > n) {
                g.setColor(getBackground());
            }
            if (i == n) {
                for (int j = 0; j < w; j++) {
                    if (j >= (int)Math.floor((getValue() % p) / p * w)) {
                        g.setColor(new Color(178, 178, 178));
                    }
                    g.fillRect(x + j, height - h - delta * i, 1, h + delta * i);
                }
            } else {
                g.fillRect(x, height - h - delta * i, w, h + delta * i);
            }
            v.add(x);
            x += 2 * w - 1;
        }
    }

    public int getSegmentSize() {
        return w;
    }

    public Vector<Integer> v = new Vector<Integer>();

    private int percent = 100;
    private int w = 4;


    public boolean hovered = false;

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        hovered = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        setValue((int) (getMaximum() * (double) e.getX() / getWidth()));
    }

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
