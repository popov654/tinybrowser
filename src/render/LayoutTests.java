package render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import mediaplayer.MediaController;
import mediaplayer.NoMediaPlayerException;


public class LayoutTests extends JFrame {

    public LayoutTests() {
        super("Simple component test");
        document = new WebDocument();
        JPanel cp = new JPanel();
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
        bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));

        document.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        document.setBorderSize(1);

        document.panel.setBackground(Color.WHITE);
        setContentPane(cp);

        cp.add(document);
        cp.add(Box.createRigidArea(new Dimension(0, 15)));
        cp.add(bp);

        document.width = 478;
        document.height = 300;
        //document.panel.setBounds(0, 0, cp.width, cp.height);
        //document.root.setBounds(1, 1, cp.width-2, cp.height-2);
        //document.root.addMouseListeners();
        
        //bp.setBounds(9, 283, 474, 86);

        Block root = document.root;
        root.setId("root");

        root.setBounds(1, 1, document.width, document.height);
        root.setWidth(-1);
        root.height = document.height-2;
        root.viewport_height = root.height;
        root.orig_height = root.height;
        root.max_height = root.height;
        root.auto_height = false;

        document.debug = true;

        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(Box.createHorizontalGlue());
        bp.add(btn);
        bp.add(Box.createHorizontalGlue());
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        cp.setPreferredSize(new Dimension(494, 370));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                //System.err.println("Resize triggered");
                document.resized();
            }
        });
    }

    public void prepareBlock() {
        
        document.ready = false;
        document.root.removeAllElements();

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

        document.root.setId("root");
        document.root.addElement(d);
        document.ready = true;

    }

    public void testBorders() {
        document.ready = false;

        document.root.removeAllElements();

        Block d = new Block(document, null, 136, 92, 1, 7, Color.MAGENTA);
        d.setPositioning(Block.Position.STATIC);
        d.setBorderRadius(0, 17, 0, 17);
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
        d.setId("d1");

        Block d01 = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setDisplayType(Block.Display.INLINE_BLOCK);
        d01.addText("Text");
        d.addElement(d01);
        //d.setBackgroundColor(new Color(228, 223, 226));

        Block d2 = new Block(document, null, 136, 92, 1, 7, Color.MAGENTA);
        d2.setPositioning(Block.Position.STATIC);
        d2.setDisplayType(Block.Display.INLINE_BLOCK);
        d2.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d2.setBorderRadius(35);
        d2.setMargins(4);
        d2.setPaddings(0);
        d2.setWidth(70);
        d2.setHeight(70);
        d2.setBorderWidth(12);
        d2.setTextColor(new Color(0, 0, 0));
        d2.setBorderColor(new Color[] {Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN});

        d2.setId("d2");

        Block d3 = d2.clone();
        d3.setBorderRadius(0);
        Color col = new Color(228, 228, 228);
        d3.setBorderColor(new Color[] {col, col, new Color(0, 0, 0, 0), col});
        d3.setWidth(60);
        d3.setHeight(60);
        d3.setMargins(0, 0, 0, 18);
        d3.setId("d3");

        Block d4 = d2.clone();
        d4.setMargins(0, 0, 0, 22);
        d4.setId("d4");
        d4.setBorderClipMode(2);

        document.root.addElement(d);
        document.root.addElement(d2);
        document.root.addElement(d3);
        document.root.addElement(d4);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testGradients() {
        document.ready = false;

        document.root.removeAllElements();

        int angle = 45;

        Block d = new Block(document, null, 136, 92, 0, 1, Color.BLACK);
        d.setPositioning(Block.Position.STATIC);
        d.setDisplayType(Block.Display.INLINE_BLOCK);
        d.setMargins(6);
        d.setPaddings(15, 17, 15, 17);
        d.setWidth(80);
        d.setHeight(80);
        //d.setBorderWidth(0);
        d.setTextColor(new Color(0, 0, 0));
        Vector<Color> c = new Vector<Color>();
        c.add(new Color(0, 0, 0, 255));
        c.add(new Color(160, 160, 160, 255));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(1f);
        d.setLinearGradient(c, p, angle);
        d.setId("d1");

        Block d2 = d.clone();
        d2.setLinearGradient(c, p, angle + 90);
        d.setId("d2");

        Block d3 = d.clone();
        d3.setLinearGradient(c, p, angle + 180);
        d.setId("d3");

        Block d4 = d.clone();
        d4.setLinearGradient(c, p, angle + 45);
        d.setId("d4");

        Block d5 = d.clone();
        d5.setLinearGradient(c, p, angle - 45);
        d.setId("d5");

        document.root.addElement(d);
        document.root.addElement(d2);
        document.root.addElement(d3);
        document.root.addElement(d4);
        document.root.addElement(d5);

        document.root.setLinearGradientFromCSS("linear-gradient(to right, #23ee82 0px, #3cd356 40%, #3dd356 100%)");

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
            document.repaint();
        }
    }

    public void basicTest() {
        document.ready = false;

        document.root.removeAllElements();

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
        c.add(new Color(0, 100, 192, 156));
        c.add(new Color(85, 146, 213, 105));
        c.add(new Color(235, 235, 238, 193));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(0.21f);
        p.add(0.45f);
        d.setLinearGradient(c, p, -45);
        d.setId("d1");

        //d.setRadialGradient(new int[] {120, 64}, new double[] {120, 64}, c, p);
        //d.setRadialGradientFromCSS("radial-gradient(circle at 100%, #333, #333 50%, #eee 75%, #333 75%)");
        //d.setRadialGradientFromCSS("radial-gradient(closest-corner at top, #e66465, #9198e5)");
        //d.setRadialGradientFromCSS("radial-gradient(at top, #e66465, #9198e5)");
        //d.setRadialGradientFromCSS("radial-gradient(farthest-side at bottom, #e66465, #9198e5)");
        
        //d.setBackgroundColor(new Color(228, 223, 226));

        Block d01 = new Block(document, d, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setDisplayType(Block.Display.INLINE);
        d01.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d01.addText("Test ");
        d.addElement(d01, true);
        //d01.setHref("http://popov654.pp.ru");
        //d01.underlineLinksMode = 1;

        Block d02 = new Block(document, d, -1, -1, 0, 0, Color.BLACK);
        d02.setPositioning(Block.Position.STATIC);
        d02.setDisplayType(Block.Display.INLINE);
        d02.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d02.addText("text");
        d02.setBackgroundColor(new Color(255, 210, 0));
        d02.setFontSize(24);
        //d02.setMargins(0, 4);
        d.addElement(d02, true);

        Block d03 = new Block(document, d, -1, -1, 0, 0, Color.BLACK);
        d03.setPositioning(Block.Position.STATIC);
        d03.setDisplayType(Block.Display.INLINE);
        d03.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d03.addText(" and some more");
        d.addElement(d03, true);

        document.root.addElement(d);

        Block d2 = new Block(document, null, 136, 92, 1, 7, Color.MAGENTA);
        d2.setPositioning(Block.Position.STATIC);
        //d2.setDisplayType(Drawable.Display.INLINE_BLOCK);
        d2.setMargins(4);
        d2.setPaddings(15, 17, 15, 17);
        d2.setWidth(-1);
        d2.setHeight(80);
        d2.setBorderWidth(2);
        d2.setTextColor(new Color(0, 0, 0));
        d2.setLinearGradient(c, p, -64);
        d2.setId("d2");

        //Color col[] = {Color.RED, Color.GREEN, Color.RED, Color.GREEN};
        //d2.setBorderColor(col);
        //d2.setBorderRadius(15);
        //d2.setBorderWidth(10);
        //int[] t = {0, RoundedBorder.DOTTED, 0, RoundedBorder.DOTTED};
        //int[] t = {RoundedBorder.DOTTED, 0, RoundedBorder.DOTTED, 0};
        //d2.setBorderType(t);
        //d2.setProp("border-right", "2px dashed #0f0");
        //d2.setProp("border-bottom", "2px dotted #0cf");

        Block d04 = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        d04.setPositioning(Block.Position.STATIC);
        d04.setDisplayType(Block.Display.INLINE_BLOCK);
        d04.addText("Text");
        d2.addElement(d04);

        document.root.addElement(d2);

        /*

        document.root.setTextShadow(new Color(123, 123, 128, 120), 1, 1, 0);
        Timer t1 = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                document.root.setTextShadow(new Color(123, 123, 128, 120), 1, 1, 2);
            }
        });
        t1.setRepeats(false);
        t1.start();
        Timer t2 = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                document.root.setTextShadow(new Color(123, 123, 128, 110), 1, 1, 4);
            }
        });
        t2.setRepeats(false);
        t2.start();

        */

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

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
            document.repaint();
        }
    }

    public void testLinks() {
        prepareBlock();

        Block b = document.root.children.get(0);

        Block d01 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setDisplayType(Block.Display.INLINE);
        d01.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d01.addText("Site Link");
        b.addElement(d01);
        d01.setHref("http://popov654.pp.ru");
        d01.linkColor = new Color(6, 66, 162);
        d01.linksUnderlineMode = 1;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
            document.repaint();
        }
    }

    public void testImages(int width, int height) {
        document.root.removeAllElements();

        document.ready = false;

        Block img = new Block(document, document.root, -1, -1, 0, 0, Color.BLACK);
        img.width = -1;
        img.isImage = true;
        img.setBackgroundImage("image.jpg");
        document.root.addElement(img);
        //if (width > 0 && height < 0) img.setWidth(width, false);
        //if (width > 0 && height < 0) img.setHeight(height, false);
        img.setWidthHeight(width, height);
        img.auto_x_margin = true;
        img.auto_y_margin = true;

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
            document.repaint();
        }
    }

    public void testForms() {
        prepareBlock();

        Block b = document.root.children.get(0);

        document.ready = false;

        b.setHeight(-1);

        Block l01 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        l01.setPositioning(Block.Position.STATIC);
        l01.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        l01.addText("Your name:");
        l01.setFontSize(12);
        l01.setMargins(0, 0, 2);
        Block d01 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        d01.setPositioning(Block.Position.STATIC);
        d01.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d01.setWidth(160);
        d01.setHeight(18);
        d01.setPaddings(1, 2, 2, 2);
        d01.setBackgroundColor(new Color(209, 217, 213, 160));
        d01.setBorderColor(new Color(118, 118, 123));
        d01.setScaleBorder(false);
        d01.setBorderWidth(1);
        d01.setBorderRadius(2);
        d01.setTextColor(new Color(35, 35, 43));
        d01.setFontSize(12);
        d01.formType = 1;
        b.addElement(l01, true);
        b.addElement(d01, true);

        Block l02 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        l02.setPositioning(Block.Position.STATIC);
        l02.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        l02.addText("About you:");
        l02.setFontSize(12);
        l02.setMargins(8, 0, 2);
        Block d02 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        d02.setPositioning(Block.Position.STATIC);
        d02.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d02.setWidth(200);
        d02.setHeight(48);
        d02.setPaddings(1, 2, 2, 2);
        d02.setBackgroundColor(new Color(209, 217, 213, 160));
        d02.setBorderColor(new Color(118, 118, 123));
        d02.setScaleBorder(false);
        d02.setBorderWidth(1);
        d02.setBorderRadius(2);
        d02.setTextColor(new Color(35, 35, 43));
        d02.setFontSize(12);
        d02.formType = 2;

        b.addElement(l02, true);
        b.addElement(d02, true);

        Block d03 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        d03.setPositioning(Block.Position.STATIC);
        d03.setDisplayType(Block.Display.INLINE_BLOCK);
        d03.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d03.setBackgroundColor(new Color(209, 217, 213, 168));
        d03.setWidth(13);
        d03.setHeight(13);
        d03.setMargins(10, 0, 6);
        d03.formType = 5;
        d03.checked = true;

        b.addElement(d03, true);

        Block l03 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        l03.setDisplayType(Block.Display.INLINE_BLOCK);
        l03.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        l03.setFontSize(12);
        l03.setMargins(0, 2, 0, 5);
        l03.addText("Label");

        b.addElement(l03, true);

        Block d04 = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        d04.setPositioning(Block.Position.STATIC);
        //d04.setDisplayType(Block.Display.INLINE_BLOCK);
        d04.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        d04.setWidth(60);
        d04.setHeight(18);
        d04.setMargins(7, 0, 2);
        d04.setPaddings(3);
        //d04.setBackgroundColor(new Color(207, 210, 218));

        d04.setTextColor(new Color(35, 35, 43));
        d04.setScaleBorder(false);
        d04.setBorderWidth(1);
        d04.setBorderRadius(2);
        d04.setBorderColor(new Color(108, 108, 113));
        d04.setFontSize(12);
        d04.addText("Submit");
        d04.formType = 3;

        b.addElement(d04, true);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
            document.repaint();
        }
    }

    public void testReplacedContent() {
        prepareBlock();

        Block b = document.root.children.get(0);

        //JLabel label = new JLabel("Test");
        //label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        //Block replaced_player = new ReplacedBlock(document, label);

        MediaController mc = new MediaController();
        mc.setMediaPlayer(new mediaplayer.MediaPlayer());
        try {
            mc.openSource("http://popov654.pp.ru/xplayer/domestic_technology_-_miss_june.mp3");
            mc.setSongTitle("Domestic Technology - Miss June");
        } catch (NoMediaPlayerException ex) {
            ex.printStackTrace();
        }
        Block replaced_player = new ReplacedBlock(document, mc);

        replaced_player.setWidth(236);
        replaced_player.setHeight(48);
        b.addElement(replaced_player);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testRelativePositioning() {
        prepareBlock();

        Block b = document.root.children.get(0);

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

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testAbsolutePositioning() {
        prepareBlock();

        Block b = document.root.children.get(0);

        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        Block block = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text1");
        b.addElement(block);

        Block block2 = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        block2.setPositioning(Block.Position.STATIC);
        block2.setDisplayType(Block.Display.INLINE_BLOCK);
        block2.addText("Text2");
        b.addElement(block2);

        block2.setPositioning(Block.Position.ABSOLUTE);
        block2.setLeft(50, Block.Units.percent);
        block2.setTop(50, Block.Units.percent);
        block2.setProp("margin-left", -block.width / 2, Block.Units.px);
        block2.setProp("margin-top", -block.height / 2, Block.Units.px);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testAutoMargins(int left, int right) {
        prepareBlock();

        Block b = document.root.children.get(0);

        b.auto_x_margin = true;
        b.width = 180;
        b.viewport_width = b.width;
        b.auto_width = false;
        b.no_draw = true;
        b.setLeft(left, Block.Units.px);
        b.setRight(right, Block.Units.px);

        if (left > 0 || right > 0) {
            b.setPositioning(Block.Position.ABSOLUTE);
        }
        b.removeAllElements();

        Block block = new Block(document, null, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.INLINE_BLOCK);
        block.addText("Text");
        b.addElement(block);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testZIndex() {
        //root.setPositioning(Block.Position.RELATIVE);
        document.root.removeAllElements();

        document.ready = false;

        Block b1 = new Block(document, null, 186, 140, 0, 0, Color.BLACK);
        b1.setPositioning(Block.Position.STATIC);
        b1.setDisplayType(Block.Display.BLOCK);
        b1.setBackgroundColor("#b3ecf9");
        b1.setMargins(10);
        document.root.addElement(b1);

        Block b2 = new Block(document, null, 100, 58, 0, 0, Color.BLACK);
        b2.setPositioning(Block.Position.STATIC);
        b2.setDisplayType(Block.Display.BLOCK);
        b2.setBackgroundColor("#ffd7b3");
        b2.setMargins(40, 42);
        b1.addElement(b2);

        Block b3 = new Block(document, null, 186, 100, 0, 0, Color.BLACK);
        b3.setPositioning(Block.Position.STATIC);
        b3.setDisplayType(Block.Display.BLOCK);
        b3.setBackgroundColor("#b3ecb3");
        b3.setMargins(4, 10, 10, -50);
        document.root.addElement(b3);

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

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testTables() {
        prepareBlock();

        Block b = document.root.children.get(0);

        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        document.ready = false;

        Color c = new Color(178, 118, 28);

        Block block = new Block(document, null, -1, -1, 1, 0, c);
        block.setPositioning(Block.Position.STATIC);
        block.setDisplayType(Block.Display.TABLE);

        block.auto_width = false;
        block.width = 200;
        //block.border_collapse = true;

        Block r = new Block(document, null, -1, -1, 0, 0, c);
        r.setPositioning(Block.Position.STATIC);
        r.setDisplayType(Block.Display.BLOCK);

        Block c1 = new Block(document, null, -1, -1, 1, 0, c);
        c1.setPositioning(Block.Position.STATIC);
        c1.setDisplayType(Block.Display.BLOCK);
        c1.addText("Table");
        Arrays.fill(c1.paddings, 1);
        c1.auto_width = false;
        c1.width = 120;

        Block c2 = new Block(document, null, -1, -1, 1, 0, c);
        c2.setPositioning(Block.Position.STATIC);
        c2.setDisplayType(Block.Display.BLOCK);
        c2.addText("Test");
        Arrays.fill(c2.paddings, 1);

        r.addElement(c1);
        r.addElement(c2);
        
        Block r2 = new Block(document, null, -1, -1, 0, 0, c);
        r2.setPositioning(Block.Position.STATIC);
        r2.setDisplayType(Block.Display.BLOCK);

        Block c3 = new Block(document, null, -1, -1, 1, 0, c);
        c3.setPositioning(Block.Position.STATIC);
        c3.setDisplayType(Block.Display.BLOCK);
        c3.addText("Looks Fine");
        Arrays.fill(c3.paddings, 1);
        //c3.auto_width = false;
        //c3.width = 120;

        Block c4 = new Block(document, null, -1, -1, 1, 0, c);
        c4.setPositioning(Block.Position.STATIC);
        c4.setDisplayType(Block.Display.BLOCK);
        c4.addText("Fine");
        Arrays.fill(c4.paddings, 1);

        c2.rowspan = 2;
        //c3.colspan = 2;
        c3.id = "test";
        r2.addElement(c3);
        //r2.addElement(c4);

        block.addElement(r);
        block.addElement(r2);

        b.addElement(block);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testInternalFrames() {
        prepareBlock();

        Block b = document.root.children.get(0);

        b.setPositioning(Block.Position.RELATIVE);
        b.removeAllElements();

        document.ready = false;

        Block block = new Block(document, b, -1, -1, 0, 0, Color.BLACK);
        block.setPositioning(Block.Position.ABSOLUTE);
        block.setDisplayType(Block.Display.BLOCK);

        block.setWidth(200);
        block.setHeight(100);
        block.setLeft(50, Block.Units.percent);
        block.setTop(50, Block.Units.percent);
        block.setProp("margin-left", Math.floor(-block.width / 2 / block.ratio), Block.Units.px);
        block.setProp("margin-top", Math.floor(-block.height / 2 / block.ratio), Block.Units.px);
        block.setBackgroundColor(Color.WHITE);

        b.addElement(block);
        block.id = "test";

//        WebDocument child = new WebDocument();
//        child.width = block.width;
//        child.height = block.height;
//        child.root.width = block.width;
//        child.root.height = block.height;
//        child.setBounds(block._x_ - block.width / 2, block._y_ - block.height / 2, block.width, block.height);
//        block.addChildDocument(child);
//        child.root.addText("test");

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testInlineBlocks() {
        prepareBlock();

        document.ready = false;

        Block b = document.root.children.get(0);
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
        //b.setWidth(120);
        b.addElement(b1, true);
        b.addElement(b2, true);
        b.addElement(b3, true);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testLists(int type) {
        prepareBlock();

        document.ready = false;

        Block b = document.root.getChildren().get(0);
        Block b1 = new Block(document);
        b1.addText("Item 1");
        Block b2 = new Block(document);
        b2.addText("Item 2");
        Block b3 = new Block(document);
        b3.addText("Item 3");
        b.setWidth(120);
        b.addElement(b1);
        b.addElement(b2);
        b.addElement(b3);
        b.no_draw = false;

        for (int i = 0; i < b.children.size(); i++) {
            b.children.get(i).list_item_type = type;
        }
        
        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testNormal() {

        prepareBlock();

        Block b = document.root.children.get(0);
        
        b.removeAllElements();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.NORMAL);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testPreWrap() {

        prepareBlock();

        Block b = document.root.children.get(0);

        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abra\n\ncadabra");
        b.setWhiteSpace(Block.WhiteSpace.PRE_WRAP);

        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testWordBreak() {

        prepareBlock();

        Block b = document.root.children.get(0);
        b.removeAllElements();
        b.setWidth(126);
        b.setHeight(-1);
        b.addText("Abracadabracadabracadabracadabra");
        b.setWhiteSpace(Block.WhiteSpace.WORD_BREAK);
        document.ready = true;

        if (this.isVisible()) {
            document.root.performLayout();
            document.root.forceRepaintAll();
        }
    }

    public void testTextAlign(Block b, int value) {
        b.setTextAlign(value);
    }

    public void updateUI(int last_width, int last_height, int width, int height) {
        bp.setBounds(bp.getX(), bp.getY() + height - last_height, bp.getWidth() + width - last_width, bp.getHeight());
        //btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
    }

    public WebDocument getDocument() {
        return document;
    }

    public static void main(String[] args) {
        int test = 0;
        int list_type = 2;

        if (args.length > 0) {
            if (args[0].equals("basic")) test = 0;
            else if (args[0].equals("links")) test = 1;
            else if (args[0].equals("images")) test = 2;
            else if (args[0].equals("replaced")) test = 3;
            else if (args[0].equals("tables")) test = 4;
            else if (args[0].equals("forms")) test = 5;
            else if (args[0].equals("iframes")) test = 6;
            else if (args[0].equals("overflow")) test = 7;
            else if (args[0].equals("prewrap")) test = 8;
            else if (args[0].equals("wordbreak")) test = 9;
            else if (args[0].equals("inline-block")) test = 10;
            else if (args[0].equals("relative")) test = 11;
            else if (args[0].equals("absolute")) test = 12;
            else if (args[0].equals("auto-marings")) test = 13;
            else if (args[0].equals("z-index")) test = 14;
            else if (args[0].equals("lists")) test = 15;
            else if (args[0].equals("borders")) test = 16;
            else if (args[0].equals("gradients")) test = 17;

            if (args.length > 1) list_type = Integer.parseInt(args[1]);
        }

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        final LayoutTests lt = new LayoutTests();

        switch (test) {
            case 0:  lt.basicTest();
                     break;
            case 1:  lt.testLinks();
                     break;
            case 2:  lt.testImages(230, 170);
                     break;
            case 3:  lt.testReplacedContent();
                     break;
            case 4:  lt.testTables();
                     break;
            case 5:  lt.testForms();
                     break;
            case 6:  lt.testInternalFrames();
                     break;
            case 7:  lt.testNormal();
                     break;
            case 8:  lt.testPreWrap();
                     break;
            case 9:  lt.testWordBreak();
                     break;
            case 10: lt.testInlineBlocks();
                     break;
            case 11: lt.testRelativePositioning();
                     break;
            case 12: lt.testAbsolutePositioning();
                     break;
            case 13: lt.testAutoMargins(60, 15);
                     break;
            case 14: lt.testZIndex();
                     break;
            case 15: lt.testLists(list_type);
                     break;
            case 16: lt.testBorders();
                     break;
            case 17: lt.testGradients();
                     break;
        }

        lt.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (lt.document.root != null && lt.document.root.getChildren().size() >= 1){
                    lt.document.root.getChildren().get(0).takeScreenshot("screenshot");
                }
            }
            
        });

        //lt.setPanelSize(474, 300);

        //lt.testAutoMargins(60, 15);
        //lt.testZIndex();

        //lt.setVisible(true);

        //try {
        //    Robot robot = new Robot();
        //    java.awt.image.BufferedImage screenShot = robot.createScreenCapture(new Rectangle(lt.getBounds()));
        //    javax.imageio.ImageIO.write(screenShot, "png", new File("snapshot.png"));
        //} catch(Exception ex) {}
    }

    private int[] pad = {9, 10};

    private WebDocument document;

    private JPanel bp;
    private JButton btn;
}
