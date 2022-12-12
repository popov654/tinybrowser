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


public class Test extends JFrame {

    public Test() {
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
        cp.width = 474;
        cp.height = 300;
        cp.panel.setBounds(9, 10, cp.width, cp.height);
        cp.root.setBounds(1, 1, cp.width-2, cp.height-2);
        cp.root.setWidth(-1);
        cp.root.setHeight(cp.height-2);
        cp.root.setBackgroundColor(new Color(0, 0, 0, 0));
        bp.setBounds(10, 280, 472, 86);
        Block d = new Block(document, null, 136, 90, 1, 7, Color.MAGENTA);
        d.setWidth(120);
        d.setHeight(90);
        d.setPositioning(Block.Position.ABSOLUTE);
        d.setLeft(100, Block.Units.px);
        d.setTop(100, Block.Units.px);
        //d.setBounds(100, 100, 120, 90);
        d.setTextColor(new Color(0, 0, 0));
        d.setPaddings(15, 17, 15, 17);
        Vector<Color> c = new Vector<Color>();
        c.add(new Color(0, 100, 192, 134));
        c.add(new Color(235, 235, 235, 245));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(0.52f);
        d.setLinearGradient(c, p, -54);
        //d.setBackgroundColor(new Color(228, 223, 226));
        d.addText("Test text test text test text");
        document.root.addElement(d);
        
        Block d2 = new Block(document, null, 136, 80, 1, 7, new Color(0, 180, 0));
        d2.setWidth(120);
        d2.setHeight(80);
        d2.setPositioning(Block.Position.ABSOLUTE);
        d2.setLeft(210, Block.Units.px);
        d2.setTop(130, Block.Units.px);
        //d2.setBounds(216, 130, 120, 80);
        d2.setTextColor(new Color(0, 0, 0));
        d2.setBackgroundImage("400.jpg");
        d2.setBackgroundRepeat(Block.BackgroundRepeat.REPEAT_XY);
        document.root.addElement(d2);
        //root.setComponentZOrder(d2, 1);

        document.root.performLayout();
        document.root.forceRepaint();

        document.panel.repaint();
        
        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(btn);
        cp.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setSize(500, 400);
        
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
        new Test().setVisible(true);
    }

    private WebDocument document;

    private JPanel bp;
    private JButton btn;
}
