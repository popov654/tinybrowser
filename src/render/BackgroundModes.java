package render;

/**
 *
 * @author Alex
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class BackgroundModes extends WebDocument {

    public BackgroundModes() {
        super("Simple component test");
        JPanel cp = new JPanel();
        cp.setLayout(null);
        bp = new JPanel();
        bp.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        panel.setLayout(null);
        borderSize = 1;
        borderColor = Color.black;
        panel.setBackground(Color.WHITE);
        setContentPane(cp);
        cp.add(panel);
        cp.add(bp);
        panel.setBounds(9, 10, 474, 300);
        width = 474;
        height = 300;
        bp.setBounds(9, 283, 474, 86);
        Block d = new Block(this, null, -1, 92, 1, 0, Color.GRAY);
        d.setPositioning(Block.Position.STATIC);
        //d.setMargins(4);
        //d.setPaddings(6, 11, 6, 11);
        d.setWidth(48);
        d.setHeight(48);
        d.setAutoXMargin();
        d.setAutoYMargin();
        d.setBackgroundImage("ab.gif");
        d.setBackgroundPositionX(50, Block.Units.percent);
        d.setBackgroundPositionY(50, Block.Units.percent);
        d.setBackgroundColor(new Color(197, 214, 231));
        panel.add(d);
        d.draw();
        btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(btn);
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
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
        BackgroundModes lt = new BackgroundModes();
        lt.setVisible(true);
        lt.setPanelSize(474, 300);
    }

    private JPanel bp;
    private JButton btn;
}
