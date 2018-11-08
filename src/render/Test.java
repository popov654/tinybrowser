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


public class Test extends WebDocument {

    public Test() {
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
        root.setBackgroundColor(new Color(0, 0, 0, 0));
        bp.setBounds(10, 280, 472, 86);
        Block d = new Block(this, null, 136, 90, 1, 7, Color.MAGENTA);
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
        root.addElement(d);
        
        Block d2 = new Block(this, null, 136, 80, 1, 7, new Color(0, 180, 0));
        d2.setWidth(120);
        d2.setHeight(80);
        d2.setPositioning(Block.Position.ABSOLUTE);
        d2.setLeft(210, Block.Units.px);
        d2.setTop(130, Block.Units.px);
        //d2.setBounds(216, 130, 120, 80);
        d2.setTextColor(new Color(0, 0, 0));
        d2.setBackgroundImage("400.jpg");
        d2.setBackgroundRepeat(Block.BackgroundRepeat.REPEAT_XY);
        root.addElement(d2);
        //root.setComponentZOrder(d2, 1);

        root.performLayout();
        root.forceRepaint();

        panel.repaint();
        
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
        new Test().setVisible(true);
    }

    private JPanel bp;
    private JButton btn;
}
