package render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MediaTest extends WebDocument {

    public MediaTest() {
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
        ready = false;
        Block d = new Block(this, null, 136, 92, 1, 7, Color.MAGENTA);
        d.setPositioning(Block.Position.STATIC);
        //d.setDisplayType(Drawable.Display.INLINE_BLOCK);
        d.setMargins(4);
        d.setPaddings(15, 17, 15, 17);
        d.setWidth(-1);
        //d.setHeight(80);
        d.setBorderWidth(2);
        d.setTextColor(new Color(0, 0, 0));

        d.setBackgroundColor(new Color(127, 186, 241));

        d.overflow = Block.Overflow.SCROLL;
        

        
        d.setHeight(-1);

        root.addElement(d);

        
        //d2.setTransform(true);
        
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

        root.height = height-2;
        root.viewport_height = root.height;
        root.orig_height = root.height;
        root.max_height = root.height;
        root.setBounds(root.getX(), root.getY(), root.width, root.height);
        
        ready = true;
        root.performLayout();

        root.forceRepaintAll();

        Block pc = new Block(this);
        //MediaPlayer mp = new MediaPlayer(pc);
        //mp.open("sound.wav");
        int w = (int)Math.round(404 / pc.ratio);
        int h = (int)Math.round(210 / pc.ratio);
        pc.width = pc.max_width = 404 - (d.borderWidth[3] - 2) - (d.borderWidth[1] - 2);
        pc.height = pc.max_height = 210 - (d.borderWidth[0] - 2) - (d.borderWidth[2] - 2);
        pc.auto_height = false;
        pc.auto_width = true;
        pc.margins[0] = 10;
        pc.margins[1] = 10;
        pc.margins[2] = 10;
        pc.margins[3] = 10;
        d.addElement(pc);
        pc.setAutoXMargin();
        MediaPlayer mp = new MediaPlayer(pc, pc.width, pc.height);
        //int player_width = (int)Math.round(404 - d.borderWidth[3] * (d.ratio-1) - d.borderWidth[1] * (d.ratio-1));
        //mp.container.children.get(0).width = player_width;
        //mp.container.children.get(0).viewport_width = player_width;
        //mp.container.children.get(0).max_width = player_width;
        pc.performLayout();
        root.forceRepaintAll();
        mp.open("file:///D:/Inception_trailer_48fps.mkv");
        

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
        block.addText("Text");
        b.addElement(block);

        block.setPositioning(Block.Position.ABSOLUTE);
        block.setLeft(50, Block.Units.percent);
        block.setTop(50, Block.Units.percent);
        block.performLayout();
        block.setProp("margin-left", -block.width / 2, Block.Units.px);
        block.setProp("margin-top", -block.height / 2, Block.Units.px);

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
        MediaTest lt = new MediaTest();
        lt.setVisible(true);
        lt.setPanelSize(474, 300);
    }

    private JPanel bp;
    private JButton btn;
}
