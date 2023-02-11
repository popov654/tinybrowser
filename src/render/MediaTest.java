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
        
        prepareBlock();
        
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

        document.root.height = document.height;
        document.root.viewport_height = document.root.height;
        document.root.orig_height = document.root.height;
        document.root.max_height = document.root.height;
        document.root.setBounds(document.root.getX(), document.root.getY(), document.root.width, document.root.height);

        testVideoPlayer();
        //testAudioPlayer();

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
                document.resized();
            }
        });
    }

    private void prepareBlock() {
        document.root.removeAllElements();
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
        document.ready = true;
    }

    public void testAudioPlayer() {
        Block b = document.root.children.get(0);
        Block pc = new Block(document);
        pc.setWidth(160);
        pc.setHeight(22);
        pc.auto_height = true;
        pc.height = -1;
        b.addElement(pc);
        MediaPlayer mp = new MediaPlayer(pc);
        mp.open("sound.wav");
        //mp.open("file:///D:/Inception_trailer_48fps.mkv");
    }

    public void testVideoPlayer() {
        Block b = document.root.children.get(0);
        Block pc = new Block(document);
        int player_width = 320;
        int player_height = 205;
        pc.setWidth(player_width);
        pc.auto_height = true;
        pc.height = -1;
        //pc.height = pc.max_height = Math.max(MediaPlayer.min_height, player_height);
        b.addElement(pc);
        pc.setAutoXMargin();
        pc.setAutoYMargin();
        MediaPlayer mp = new MediaPlayer(pc, player_width, player_height);
        mp.open("file:///D:/Inception_trailer_48fps.mkv");
        //pc.setWidth(300);
        //mp.open("sound.wav");
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
