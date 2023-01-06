package render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MediaTest extends JFrame {

    public MediaTest() {
        super("Media player test");
        WebDocument doc = new WebDocument();
        JPanel cp = new JPanel();
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
        setContentPane(cp);
        
        document = doc;
        bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));
        document.setBorder(BorderFactory.createLineBorder(Color.black));
        document.borderSize = 1;
        cp.add(document);
        cp.add(Box.createRigidArea(new Dimension(0, 15)));
        cp.add(bp);

        //document.width = 500;
        //document.height = 360;
        document.width = 640;
        document.height = 404;

        document.root.height = document.height-2;
        document.root.viewport_height = document.root.height;
        document.root.orig_height = document.root.height;
        document.root.max_height = document.root.height;
        document.root.setBounds(document.root.getX(), document.root.getY(), document.root.width, document.root.height);

        document.panel.setBounds(9, 10, document.width, document.height);
        document.root.setBounds(1, 1, document.width-2, document.height-2);
        document.root.setWidth(-1);
        document.root.setHeight(document.height-2);
        document.root.addMouseListeners();
        document.repaint();
        
        document.ready = false;
        Block d = new Block(document, null, -1, -1, 1, 7, Color.MAGENTA);
        d.setPositioning(Block.Position.STATIC);
        //d.setDisplayType(Drawable.Display.INLINE_BLOCK);
        d.setMargins(4);
        d.setPaddings(15, 17, 15, 17);
        d.setWidth(-1);
        d.setHeight(-1);
        //d.setHeight(80);
        d.setBorderWidth(2);
        d.setTextColor(new Color(0, 0, 0));
        d.setBackgroundColor(new Color(127, 186, 241));
        d.overflow = Block.Overflow.SCROLL;

        document.root.addElement(d);
        
        //d.has_shadow = true;
        //d.shadow_x = 1;
        //d.shadow_y = -1;
        //d.shadow_blur = 2;
        //d.shadow_size = 0;
        //d.shadow_color = new Color(0, 0, 0, 114);
        //d.setProp("box-shadow", "0px 0px 3px 1px #c0c0c0");
        
        //d.setAlpha(0.42f);

        //root.setBackgroundImage("400.jpg");
        //root.setBackgroundRepeat(Block.BackgroundRepeat.REPEAT_XY);

        document.root.height = document.height-2;
        document.root.viewport_height = document.root.height;
        document.root.orig_height = document.root.height;
        document.root.max_height = document.root.height;
        document.root.setBounds(document.root.getX(), document.root.getY(), document.root.width, document.root.height);
        
        document.ready = true;
        document.root.performLayout();

        document.root.forceRepaintAll();

        Block pc = new Block(document);
        //MediaPlayer mp = new MediaPlayer(pc);
        //mp.open("sound.wav");
        int player_width = 404 - d.borderWidth[3] - d.borderWidth[1];
        int player_height = 210 - d.borderWidth[0] - d.borderWidth[2];
        pc.width = pc.max_width = player_width;
        pc.height = -1;
        //pc.height = pc.max_height = Math.max(MediaPlayer.min_height, player_height);
        pc.auto_height = false;
        pc.auto_width = true;
        pc.margins[0] = 10;
        pc.margins[1] = 10;
        pc.margins[2] = 10;
        pc.margins[3] = 10;
        d.addElement(pc);
        pc.setAutoXMargin();
        MediaPlayer mp = new MediaPlayer(pc, player_width, player_height);
        mp.container.children.get(0).width = player_width;
        //mp.container.children.get(0).height = pc.height + 100;
        mp.container.children.get(0).viewport_width = player_width;
        mp.container.children.get(0).max_width = player_width;
        pc.performLayout();
        document.root.forceRepaintAll();
        mp.open("file:///D:/Inception_trailer_48fps.mkv");
        
        document.panel.repaint();
        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bp.add(Box.createHorizontalGlue());
        bp.add(btn);
        bp.add(Box.createHorizontalGlue());
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        cp.setPreferredSize(new Dimension(document.width + 20, document.height + 60));
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

    public void testTextAlign(Block b, int value) {
        b.setTextAlign(value);
    }

    public void updateUI(int last_width, int last_height, int width, int height) {
        bp.setSize(bp.getWidth() + width - last_width, bp.getHeight());
        bp.setBounds(bp.getX(), bp.getY()+height-last_height, bp.getWidth(), bp.getHeight());
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
    }

    public void setPanelSize(int width, int height) {
        width *= document.root.ratio;
        height *= document.root.ratio;
        document.panel.setBounds(9, 10, width, height);
        bp.setBounds(9, height - 28, width, 86);
    }

    public static void main(String[] args) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        MediaTest lt = new MediaTest();
        lt.setVisible(true);
        lt.setPanelSize(474, 270);
    }

    private WebDocument document;

    private JPanel bp;
    private JButton btn;
}
