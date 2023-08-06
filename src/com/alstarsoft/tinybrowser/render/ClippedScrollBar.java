package com.alstarsoft.tinybrowser.render;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;

/**
 *
 * @author Alex
 */
public class ClippedScrollBar extends JScrollBar {

    public ClippedScrollBar() {
        super();
    }

    public ClippedScrollBar(Shape clip) {
        super();
        this.clip = clip;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (clip != null) g.setClip(clip);
        super.paintComponent(g);
    }

    public void setClip(Shape clip) {
        this.clip = clip;
    }

    Shape clip = null;

    public static void main(String args[]) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("Scrollbar clip test");
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.setContentPane(panel);
        panel.setPreferredSize(new Dimension(340, 280));
        ClippedScrollBar cs = new ClippedScrollBar();
        cs.getModel().setRangeProperties(0, 80, 0, 280, false);
        cs.setClip(new Rectangle(0, 0, cs.getPreferredSize().width, 40));
        panel.add(cs);
        cs.setBounds(160, 50, cs.getPreferredSize().width, 130);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
