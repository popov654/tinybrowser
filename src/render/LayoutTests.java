package render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class LayoutTests extends WebDocument {

    public LayoutTests() {
        super("Simple component test");
        JPanel cp = new JPanel();
        cp.setLayout(null);
        bp = new JPanel();
        bp.setLayout(null);
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        borderSize = 1;
        setContentPane(cp);
        cp.add(panel);
        cp.add(bp);
        panel.setBounds(9, 10, 474, 300);
        width = 474;
        height = 300;
        root.setBounds(1, 1, width-2, height-2);
        root.setWidth(-1);
        root.setHeight(height-2);
        root.addMouseListeners();
        
        bp.setBounds(9, 283, 474, 86);

        root.height = height-2;
        root.viewport_height = root.height;
        root.orig_height = root.height;
        root.max_height = root.height;
        root.setBounds(root.getX(), root.getY(), root.width, root.height);

        //System.out.println();
        //d.getLayouter().printLines(d, 0);
        //System.out.println();

        basicTest();
        //testNormal(d2);
        //testPreWrap(d2);
        //testWordBreak(d2);
        //testInlineBlocks(d2);
        //testRelativePositioning(d2);
        //testAbsolutePositioning(d2);
        //testZIndex();
        //testLists(d2, 2);
        

        //testTextAlign(d, Block.TextAlign.ALIGN_CENTER);

        panel.repaint();
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

    public void basicTest() {
        ready = false;

        Block d = new Block(this, null, 136, 92, 1, 7, Color.MAGENTA);
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


        Block d01 = new Block(this, d, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setDisplayType(Block.Display.INLINE_BLOCK);
        d01.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d01.addText("Test ");
        d.addElement(d01);

        Block d02 = new Block(this, d, -1, -1, 0, 0, Color.BLACK);
        d02.setPositioning(Block.Position.STATIC);
        d02.setDisplayType(Block.Display.INLINE_BLOCK);
        d02.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d02.addText("\u00A0");
        d.addElement(d02);

        Block d03 = new Block(this, d, -1, -1, 0, 0, Color.BLACK);
        d03.setPositioning(Block.Position.STATIC);
        d03.setDisplayType(Block.Display.INLINE_BLOCK);
        d03.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d03.addText("text");
        d03.setBackgroundColor(new Color(255, 210, 0));
        d03.setFontSize(24);
        //d03.setMargins(0, 4);
        d.addElement(d03);

        Block d04 = new Block(this, d, -1, -1, 0, 0, Color.BLACK);
        d04.setPositioning(Block.Position.STATIC);
        d04.setDisplayType(Block.Display.INLINE_BLOCK);
        d04.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d04.addText("\u00A0");
        d.addElement(d04);

        Block d05 = new Block(this, d, -1, -1, 0, 0, Color.BLACK);
        d05.setPositioning(Block.Position.STATIC);
        d05.setDisplayType(Block.Display.INLINE_BLOCK);
        d05.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d05.addText(" and some more");
        d.addElement(d05);

        root.addElement(d);

        Block d2 = new Block(this, null, 136, 92, 1, 7, Color.MAGENTA);
        d2.setPositioning(Block.Position.STATIC);
        //d2.setDisplayType(Drawable.Display.INLINE_BLOCK);
        d2.setMargins(4);
        d2.setPaddings(15, 17, 15, 17);
        d2.setWidth(-1);
        d2.setHeight(80);
        d2.setBorderWidth(2);
        d2.setTextColor(new Color(0, 0, 0));
        d2.setLinearGradient(c, p, -64);

        //Color col[] = {Color.RED, Color.GREEN, Color.RED, Color.GREEN};
        //d2.setBorderColor(col);
        //d2.setBorderRadius(15);
        //d2.setBorderWidth(10);
        //int[] t = {0, RoundedBorder.DOTTED, 0, RoundedBorder.DOTTED};
        //int[] t = {RoundedBorder.DOTTED, 0, RoundedBorder.DOTTED, 0};
        //d2.setBorderType(t);
        //d2.setProp("border-right", "2px dashed #0f0");
        //d2.setProp("border-bottom", "2px dotted #0cf");

        Block d06 = new Block(this, null, -1, -1, 0, 0, Color.BLACK);
        d06.setPositioning(Block.Position.STATIC);
        d06.setDisplayType(Block.Display.INLINE_BLOCK);
        d06.addText("Text");
        d2.addElement(d06);

        root.addElement(d2);

        //d2.setBold(true);
        //d2.setItalic(true);

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

        //d.display_type = Block.Display.NONE;
        //d.visibility = Block.Visibility.HIDDEN;

        ready = true;

        root.performLayout();
        root.forceRepaintAll();
    }

    public void testRelativePositioning(Block b) {
        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        Block block = new Block(this, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text");
        b.addElement(block);

        block.setPositioning(Block.Position.RELATIVE);
        block.setLeft(10, Block.Units.px);
        block.setTop(10, Block.Units.px);

        root.performLayout();
        root.forceRepaintAll();
    }

    public void testAbsolutePositioning(Block b) {
        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        Block block = new Block(this, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text1");
        b.addElement(block);

        Block block2 = new Block(this, null, -1, -1, 0, 0, Color.BLACK);
        block2.setPositioning(Block.Position.STATIC);
        block2.setDisplayType(Block.Display.INLINE_BLOCK);
        block2.addText("Text2");
        b.addElement(block2);

        block2.setPositioning(Block.Position.ABSOLUTE);
        block2.setLeft(50, Block.Units.percent);
        block2.setTop(50, Block.Units.percent);
        block2.setProp("margin-left", -block.width / 2, Block.Units.px);
        block2.setProp("margin-top", -block.height / 2, Block.Units.px);

        root.performLayout();
        root.forceRepaintAll();
    }

    public void testZIndex() {
        //root.setPositioning(Block.Position.RELATIVE);
        root.removeAllElements();

        Block b1 = new Block(this, null, 186, 140, 0, 0, Color.BLACK);
        b1.setPositioning(Block.Position.STATIC);
        b1.setDisplayType(Block.Display.BLOCK);
        b1.setBackgroundColor("#b3ecf9");
        b1.setMargins(10);
        root.addElement(b1);

        Block b2 = new Block(this, null, 100, 58, 0, 0, Color.BLACK);
        b2.setPositioning(Block.Position.STATIC);
        b2.setDisplayType(Block.Display.BLOCK);
        b2.setBackgroundColor("#ffd7b3");
        b2.setMargins(40, 42);
        b1.addElement(b2);

        Block b3 = new Block(this, null, 186, 100, 0, 0, Color.BLACK);
        b3.setPositioning(Block.Position.STATIC);
        b3.setDisplayType(Block.Display.BLOCK);
        b3.setBackgroundColor("#b3ecb3");
        b3.setMargins(4, 10, 10, -50);
        root.addElement(b3);

        //b1.setZIndex("0");
        //b1.setPositioning(Block.Position.RELATIVE);

        //b2.setZIndex("2");
        //b2.setPositioning(Block.Position.RELATIVE);

        //b3.setZIndex("1");
        //b3.setPositioning(Block.Position.RELATIVE);
        b3.setLeft(-50, Block.Units.px);
        //b3.setTop(-6, Block.Units.px);
        b1.float_type = Block.FloatType.LEFT;
        b3.setDisplayType(Block.Display.INLINE_BLOCK);
        b3.addText("Test");
        b3.setPaddings(18);
        //b3.float_type = Block.FloatType.LEFT;
        //b3.clear_type = Block.ClearType.BOTH;

        //b2.display_type = Block.Display.NONE;

        root.performLayout();
        root.forceRepaintAll();
    }

    public void testInlineBlocks(Block b) {
        b.setPositioning(Block.Position.STATIC);
        b.removeAllElements();
        b.setHeight(-1);
        Block b1 = new Block(this);
        b1.display_type = Block.Display.INLINE;
        b1.addText("Test");
        Block b2 = new Block(this);
        b2.display_type = Block.Display.INLINE;
        b2.addText("and another one");
        b2.setBackgroundColor(new Color(255, 210, 0));
        b2.setMargins(0, 3);
        b2.setPaddings(0, 3);
        Block b3 = new Block(this);
        b3.display_type = Block.Display.INLINE;
        b3.addText("test");
        b.setWidth(120);
        b.addElement(b1);
        b.addElement(b2);
        b.addElement(b3);

        root.performLayout();
        root.forceRepaint();
    }

    public void testLists(Block b, int type) {
        b.removeAllElements();
        Block b1 = new Block(this);
        b1.addText("Item 1");
        Block b2 = new Block(this);
        b2.addText("Item 2");
        Block b3 = new Block(this);
        b3.addText("Item 3");
        b.setWidth(120);
        b.addElement(b1);
        b.addElement(b2);
        b.addElement(b3);
        b.no_draw = false;

        for (int i = 0; i < b.children.size(); i++) {
            b.children.get(i).list_item_type = type;
        }
        
        root.performLayout();
        root.forceRepaintAll();
    }

    public void testNormal(Block b) {
        b.removeAllElements();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.NORMAL);
        root.performLayout();
        root.forceRepaintAll();
    }

    public void testPreWrap(Block b) {
        b.removeAllElements();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abra\n\ncadabra");
        b.setWhiteSpace(Block.WhiteSpace.PRE_WRAP);
        root.performLayout();
        root.forceRepaintAll();
    }

    public void testWordBreak(Block b) {
        b.removeAllElements();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.WORD_BREAK);
        root.performLayout();
        root.forceRepaintAll();
    }

    public void testTextAlign(Block b, int value) {
        b.setTextAlign(value);
    }

    @Override
    public void updateUI(int last_width, int last_height, int width, int height) {
        bp.setSize(bp.getWidth() + width - last_width, bp.getHeight());
        bp.setBounds(bp.getX(), bp.getY()+height-last_height, bp.getWidth(), bp.getHeight());
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
    }

    public void setPanelSize(int width, int height) {
        panel.setBounds(9, 10, width, height);
    }

    public static void main(String[] args) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        LayoutTests lt = new LayoutTests();
        lt.setVisible(true);
        lt.setPanelSize(474, 300);
    }

    private JPanel bp;
    private JButton btn;
}
