package com.alstarsoft.tinybrowser.render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class BackgroundModes extends JFrame {

    public BackgroundModes() {
        super("Simple component test");
        JPanel cp = new JPanel();
        document = new WebDocument();
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
        op = createOptionsPanel();
        bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));

        document.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        document.borderSize = 1;
        document.borderColor = Color.black;
        document.panel.setBackground(Color.WHITE);
        setContentPane(cp);
        
        cp.add(document);
        cp.add(op);
        cp.add(bp);
        
        document.width = 478;
        document.height = 268;

        document.setPreferredSize(new Dimension(document.width+2, document.height+2));
        //document.setBounds(9, 10, document.width+2, document.height+2);
        //op.setBounds(9, 282, 480, 36);
        //bp.setBounds(9, 303, 480, 86);

        Block root = document.root;

        root.setBounds(1, 1, document.width, document.height);

        root.setWidth(-1);
        root.setHeight(document.height);

        root.height = document.height;
        root.viewport_height = root.height;
        root.orig_height = (int) (root.height / root.ratio);
        root.max_height = root.height;
        //root.setBounds(root.getX(), root.getY(), root.width, root.height);

        image = new Block(document, null, -1, 92, 1, 0, Color.GRAY);
        image.setPositioning(Block.Position.STATIC);
        //image.setMargins(4);
        //image.setPaddings(6, 11, 6, 11);
        image.setWidth(140);
        image.setHeight(140);
        image.setAutoXMargin();
        image.setAutoYMargin();
        root.performLayout();

        if (coverMode.isSelected() || containMode.isSelected()) {
            image.setBackgroundImage("image.jpg");
        } else {
            image.setBackgroundImage("smiley.gif");
        }
        if (coverMode.isSelected()) {
            image.setBackgroundCover();
        }
        else if (containMode.isSelected()) {
            image.setBackgroundContain();
        }
        else if (animation.isSelected()) {
            image.setBackgroundSizeX(48, Block.Units.px);
            image.setBackgroundSizeY(48, Block.Units.px);
        }
        //image.setBackgroundSizeX(130, Block.Units.percent);
        //image.setBackgroundSizeY(130, Block.Units.percent);
        //image.setBackgroundCover();
        //image.setBackgroundContain();
        image.setBackgroundPositionX(50, Block.Units.percent);
        image.setBackgroundPositionY(50, Block.Units.percent);
        image.setBackgroundColor(new Color(197, 214, 231));
        root.addElement(image);

        root.performLayout();
        root.forceRepaint();

        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(Box.createHorizontalGlue());
        bp.add(btn);
        bp.add(Box.createHorizontalGlue());
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        setSize(518, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                document.resized();
            }
        });
    }

    public JPanel createOptionsPanel() {
        JPanel panel = new JPanel();
        ButtonGroup radioGroup = new ButtonGroup();
        coverMode = new JRadioButton("Cover");
        containMode = new JRadioButton("Contain");
        animation = new JRadioButton("Animation");
        radioGroup.add(coverMode);
        radioGroup.add(containMode);
        radioGroup.add(animation);
        coverMode.setSelected(true);
        panel.add(coverMode);
        panel.add(containMode);
        panel.add(animation);

        ActionListener l = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (coverMode.isSelected() || containMode.isSelected()) {
                    image.setBackgroundImage("image.jpg");
                } else {
                    image.setBackgroundImage("smiley.gif");
                    image.setBackgroundSizeAuto();
                    image.setBackgroundPositionX(50, Block.Units.percent);
                    image.setBackgroundPositionY(50, Block.Units.percent);
                }
                if (coverMode.isSelected()) {
                    image.setBackgroundCover();
                }
                else if (containMode.isSelected()) {
                    image.setBackgroundContain();
                }
                image.draw();
                image.repaint();
            }

        };

        coverMode.addActionListener(l);
        containMode.addActionListener(l);
        animation.addActionListener(l);

        return panel;
    }


    public void updateUI(int last_width, int last_height, int width, int height) {
        //System.out.println(width + " , " + height);
        document.setBounds(pad[0], pad[1], width, height);
    }

    public void adjustSize() {
        int w = document.getWidth();
        int h = document.getHeight();
        Insets insets = getInsets();
        if (insets != null) {
            w -= insets.left + insets.right;
            h -= insets.top + insets.bottom;
        }
        document.setBounds(9, 10, w, h);
        op.setBounds(9, op.getBounds().y - (insets.top - 20), op.getWidth() - (insets.left + insets.right), op.getHeight());
        bp.setBounds(9, bp.getBounds().y - (insets.top - 20), bp.getWidth() - (insets.left + insets.right), bp.getHeight());
    }

    public static void main(String[] args) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        BackgroundModes lt = new BackgroundModes();
        lt.setVisible(true);
    }

    private int[] pad = {9, 10};

    private JRadioButton coverMode;
    private JRadioButton containMode;
    private JRadioButton animation;

    private Block image;

    private WebDocument document;

    private JPanel op;
    private JPanel bp;
    private JButton btn;
}
