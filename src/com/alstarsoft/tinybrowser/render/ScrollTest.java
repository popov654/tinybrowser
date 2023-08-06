package com.alstarsoft.tinybrowser.render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class ScrollTest extends JFrame {

    public ScrollTest() {
        super("Simple component test");
        WebDocument cp = new WebDocument();
        document = cp;
        cp.setLayout(null);
        bp = new JPanel();
        bp.setLayout(null);
        cp.panel.setLayout(null);
        cp.panel.setBorder(BorderFactory.createLineBorder(Color.black));
        cp.borderSize = 1;
        setContentPane(cp);
        cp.add(cp.panel);
        cp.add(bp);
        cp.panel.setBounds(9, 10, 474, 300);
        cp.width = 474;
        cp.height = 300;
        cp.root.setBounds(1, 1, cp.width-2, cp.height-2);
        cp.root.setWidth(-1);
        cp.root.setHeight(cp.height-2);
        
        bp.setBounds(9, 283, 474, 86);
        document.ready = false;
        Block d = new Block(document, null, 136, 92, 1, 7, Color.MAGENTA);
        d.setPositioning(Block.Position.STATIC);
        //d.setDisplayType(Drawable.Display.INLINE_BLOCK);
        d.setMargins(4);
        d.setPaddings(15, 17, 15, 17);
        d.setWidth(-1);
        d.setHeight(80);
        d.setBorderWidth(2);
        d.setTextColor(new Color(0, 0, 0));
        Vector<Color> c = new Vector<Color>();
        c.add(new Color(0, 100, 192, 134));
        c.add(new Color(235, 235, 235, 245));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(0.52f);
        d.setLinearGradient(c, p, -54);
        //d.setBackgroundColor(new Color(228, 223, 226));

        d.setPositioning(Block.Position.RELATIVE);

        Block d01 = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setDisplayType(Block.Display.INLINE_BLOCK);
        d01.addText("Text");
        d.addElement(d01);
        
        Block d02 = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        d02.setPositioning(Block.Position.STATIC);
        d02.setDisplayType(Block.Display.INLINE_BLOCK);
        d02.addText("Text2");
        d.addElement(d02);

        d02.setPositioning(Block.Position.ABSOLUTE);
        d02.setLeft(97, Block.Units.percent);
        d02.setTop(76, Block.Units.px);
        d02.setBackgroundColor(Color.PINK);
        document.ready = true;
        d02.performLayout();
        document.ready = false;
        d02.setProp("margin-left", -d02.width / 2, Block.Units.px);
        d02.setProp("margin-top", -d02.height / 2, Block.Units.px);
        d.overflow = Block.Overflow.SCROLL;
        d.setBorderRadius(0, 12);

        Block d2 = d.clone();
        d2.children = d.cloneChildren();


//        d.setHeight(-1);
//
//        int[] w = new int[4];
//        w[0] = 15; w[1] = 20; w[2] = 15; w[3] = 20;
//        d.setBorderWidth(w);
//        Color[] cols = new Color[4];
//        cols[0] = new Color(180, 0, 0); cols[1] = new Color(0, 180, 0);
//        cols[2] = new Color(180, 0, 0); cols[3] = new Color(0, 180, 0);
//        d.setBorderColor(cols);
        
        document.root.addElement(d);
        //root.addElement(d2);
        
        //d2.setTransform(true);
        
        //d.has_shadow = true;
        //d.shadow_x = 1;
        //d.shadow_y = -1;
        //d.shadow_blur = 2;
        //d.shadow_size = 0;
        //d.shadow_color = new Color(0, 0, 0, 114);
        //d.setProp("box-shadow", "0px 0px 3px 1px #c0c0c0");
        
        //d.setAlpha(0.42f);
        //d03.setAlpha(0.64f);

        //root.setBackgroundImage("400.jpg");
        //root.setBackgroundRepeat(Block.BackgroundRepeat.REPEAT_XY);

        document.root.height = document.height-2;
        document.root.viewport_height = document.root.height;
        document.root.orig_height = document.root.height;
        document.root.max_height = document.root.height;
        document.root.setBounds(document.root.getX(), document.root.getY(), document.root.width, document.root.height);
        
        document.ready = true;
        document.root.performLayout();
        //d.scroll_top = 10;
        //d.scrollbar_y.setValue(d.scroll_top);
        document.root.forceRepaintAll();
        
        System.out.println();
        d.getLayouter().printLines(d, 0);
        System.out.println();

        document.panel.repaint();
        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(btn);
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        cp.setPreferredSize(new Dimension(494, 370));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    public void testRelativePositioning(Block b) {
        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        Block block = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text");
        b.addElement(block);

        block.setPositioning(Block.Position.RELATIVE);
        block.setLeft(10, Block.Units.px);
        block.setTop(10, Block.Units.px);

        document.root.performLayout();
        document.root.forceRepaintAll();
    }

    public void testAbsolutePositioning(Block b) {
        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        Block block = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text");
        b.addElement(block);

        block.setPositioning(Block.Position.ABSOLUTE);
        block.setLeft(50, Block.Units.percent);
        block.setTop(50, Block.Units.percent);
        block.performLayout();
        block.setProp("margin-left", -block.width / 2, Block.Units.px);
        block.setProp("margin-top", -block.height / 2, Block.Units.px);

        document.root.performLayout();
        document.root.forceRepaintAll();
    }

    public void testInlineBlocks(Block b) {
        b.setPositioning(Block.Position.STATIC);
        b.removeAllElements();
        b.setHeight(-1);
        Block b1 = new Block(document);
        b1.display_type = Block.Display.INLINE;
        b1.addText("Test");
        Block b2 = new Block(document);
        b2.display_type = Block.Display.INLINE;
        b2.addText("and another one");
        b2.setBackgroundColor(new Color(255, 210, 0));
        b2.setMargins(0, 3);
        b2.setPaddings(0, 3);
        Block b3 = new Block(document);
        b3.display_type = Block.Display.INLINE;
        b3.addText("test");
        b.setWidth(120);
        b.addElement(b1);
        b.addElement(b2);
        b.addElement(b3);
        
        document.root.performLayout();
        document.root.forceRepaintAll();
    }

    public void testNormal(Block b) {
        b.children.clear();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.NORMAL);
        document.root.performLayout();
        document.root.forceRepaint();
    }

    public void testPreWrap(Block b) {
        b.children.clear();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abra\ncada brac ada b racadab rac adabra");
        b.setWhiteSpace(Block.WhiteSpace.PRE_WRAP);
        document.root.performLayout();
        document.root.forceRepaint();
    }

    public void testWordBreak(Block b) {
        b.children.clear();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.WORD_BREAK);
        document.root.performLayout();
        document.root.forceRepaint();
    }

    public void testTextAlign(Block b, int value) {
        b.setTextAlign(value);
    }

    public void updateUI(int last_width, int last_height, int width, int height) {
        bp.setSize(bp.getWidth() + width - last_width, bp.getHeight());
        bp.setBounds(bp.getX(), bp.getY()+height-last_height, bp.getWidth(), bp.getHeight());
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
    }

    public void setPanelSize(int width, int height) {
        document.panel.setBounds(9, 10, width, height);
    }

    public static void main(String[] args) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        ScrollTest lt = new ScrollTest();
        lt.setVisible(true);
        lt.setPanelSize(474, 300);
    }

    private WebDocument document;

    private JPanel bp;
    private JButton btn;
}
