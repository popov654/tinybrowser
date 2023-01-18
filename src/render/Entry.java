package render;

import bridge.Mapper;
import htmlparser.Node;
import htmlparser.TagLibrary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 *
 * @author Alex
 */
public class Entry extends javax.swing.JPanel {

    /** Creates new form Entry */
    public Entry() {
        initComponents();
    }

    public Entry(Node node) {
        this.node = node;
        initComponents();
        initEvents();
    }

    public Entry(Node node, WebDocument document) {
        this.node = node;
        this.document = document;
        initComponents();
        initEvents();
    }

    public Entry(Node node, Block block) {
        this.node = node;
        this.block = block;
        this.document = block.document;
        Mapper.add(node, block);
        initComponents();
        initEvents();
    }

    private void initEvents() {
        marker.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!opened) open();
                else close();
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

        });

        addMouseListener(listener);
    }

    public void addChild(Entry child, int pos) {
        content.add(child, pos);
        //content.validate();
    }

    public void removeChild(int pos) {
        content.remove(pos);
        //content.validate();
    }

    public void inflate() {
        if (node == null) return;
        if (node.nodeType == 1) {
            boolean isPaired = !TagLibrary.tags.containsKey(node.tagName.toLowerCase()) ||
                                TagLibrary.tags.get(node.tagName.toLowerCase());
            if (!isPaired) {
                tag_header.setText("<" + node.tagName.toLowerCase() + " />");
                three_dots.setText("");
                tag_header_2.setText("");
                content.setVisible(false);
                footer.setVisible(false);
                marker.setVisible(false);
            } else {
                tag_header.setText("<" + node.tagName.toLowerCase() + ">");
                tag_header_2.setText("</" + node.tagName.toLowerCase() + ">");
                tag_footer.setText("</" + node.tagName.toLowerCase() + ">");
            }

            content.removeAll();
            //System.out.println(getWidth());
            for (int i = 0; i < node.children.size(); i++) {
                Entry e = new Entry(node.children.get(i), document);
                content.add(e);
                e.inflate();
                //content.setSize(e.getSize());
            }
            content.doLayout();
            if (node.children.size() > 0) {
                open();
            } else {
                close();
            }

            int height = 26 * 2 + content.getPreferredSize().height;
            //setMaximumSize(new Dimension(490, 26 * 2 + content.getPreferredSize().height));
            System.out.println(getParent().getWidth() + "x" + height);
            content.setPreferredSize(new Dimension(480, content.getPreferredSize().height));
            //setPreferredSize(new Dimension(490, height));
            
            content.validate();
        } else if (node.nodeType == 3 && !node.nodeValue.matches("\\s*")) {
            content.removeAll();
            header.setVisible(false);
            footer.setVisible(false);
            JTextArea textarea = new JTextArea();
            textarea.setText(node.nodeValue);
            textarea.setEditable(false);
            textarea.setOpaque(false);
            textarea.setBackground(new Color(255, 255, 255, 0));
            textarea.setColumns(180);
            textarea.setFont(new Font("Tahoma", Font.PLAIN, 16));
            int rows = node.nodeValue.split("\n").length;
            textarea.setRows(rows);
            textarea.addMouseListener(listener);

            int height = getFontMetrics(textarea.getFont()).getHeight() * rows;

            content.add(textarea);
            content.setOpaque(false);
            //System.out.println(getParent().getWidth() + "x" + height);
            //content.setPreferredSize(new Dimension(490, height));
            //content.setMinimumSize(new Dimension(getParent().getWidth(), height));

            //setPreferredSize(new Dimension(getParent().getWidth(), height));

            content.validate();
        } else {
            setVisible(false);
            content.removeAll();
        }
        
    }

    MouseListener listener = new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {
            // TODO: select
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {
            //System.out.println("Entered");
            node.states.add("highlighted");
            updateChildren(true);
            repaint();
            if (document != null) {
                document.root.forceRepaintAll();
                document.repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //System.out.println("Exited");
            node.states.remove("highlighted");
            updateChildren(false);
            repaint();
            if (document != null) {
                document.root.forceRepaintAll();
                document.repaint();
            }
        }
    };

    private void updateChildren(boolean value) {
        hovered = value;
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof Entry) {
                ((Entry)c[i]).updateChildren(value);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (hovered) {
            g.clearRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(190, 230, 255, 93));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.clearRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        //super.paintComponent(g);
    }

    private boolean hovered = false;

    public void open() {
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle.png")));
        three_dots.setVisible(false);
        tag_header_2.setVisible(false);
        content.setVisible(true);
        footer.setVisible(true);
        opened = true;
    }

    public void close() {
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle2.png")));
        content.setVisible(false);
        footer.setVisible(false);
        boolean has_children = node.children.size() > 0;
        three_dots.setVisible(has_children);
        marker.setVisible(has_children);
        tag_header_2.setVisible(true);
        opened = false;
    }

    public void openAll() {
        open();
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof Entry) {
                ((Entry) c[i]).openAll();
            }
        }
    }

    public void closeAll() {
        close();
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof Entry) {
                ((Entry) c[i]).closeAll();
            }
        }
    }

    public boolean opened = false;

    public Block block;
    public Node node;
    public WebDocument document;

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        header = new javax.swing.JPanel();
        margin_header = new javax.swing.JPanel();
        marker = new javax.swing.JLabel();
        tag_header = new javax.swing.JLabel();
        three_dots = new javax.swing.JLabel();
        tag_header_2 = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        footer = new javax.swing.JPanel();
        margin_footer = new javax.swing.JPanel();
        tag_footer = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(100, 26));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        header.setBackground(new java.awt.Color(255, 255, 255));
        header.setAlignmentX(1.0F);
        header.setMaximumSize(new java.awt.Dimension(32767, 26));
        header.setMinimumSize(new java.awt.Dimension(280, 26));
        header.setOpaque(false);
        header.setPreferredSize(null);
        header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 2));

        margin_header.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 5));
        margin_header.setOpaque(false);
        margin_header.setPreferredSize(new java.awt.Dimension(30, 26));

        marker.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle.png"))); // NOI18N
        marker.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        marker.setPreferredSize(new java.awt.Dimension(22, 22));

        javax.swing.GroupLayout margin_headerLayout = new javax.swing.GroupLayout(margin_header);
        margin_header.setLayout(margin_headerLayout);
        margin_headerLayout.setHorizontalGroup(
            margin_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(margin_headerLayout.createSequentialGroup()
                .addComponent(marker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        margin_headerLayout.setVerticalGroup(
            margin_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(margin_headerLayout.createSequentialGroup()
                .addComponent(marker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        header.add(margin_header);

        tag_header.setFont(new java.awt.Font("Arial", 1, 16));
        tag_header.setForeground(new java.awt.Color(102, 0, 153));
        tag_header.setText("<body>");
        header.add(tag_header);

        three_dots.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        three_dots.setText("...");
        three_dots.setPreferredSize(new java.awt.Dimension(19, 20));
        header.add(three_dots);

        tag_header_2.setFont(new java.awt.Font("Arial", 1, 16));
        tag_header_2.setForeground(new java.awt.Color(102, 0, 153));
        tag_header_2.setText("</body>");
        header.add(tag_header_2);

        add(header);

        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));
        content.setAlignmentX(1.0F);
        content.setMaximumSize(new java.awt.Dimension(32767, 32767));
        content.setMinimumSize(null);
        content.setOpaque(false);
        content.setPreferredSize(null);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.PAGE_AXIS));
        add(content);

        footer.setBackground(new java.awt.Color(255, 255, 255));
        footer.setAlignmentX(1.0F);
        footer.setMaximumSize(new java.awt.Dimension(32767, 26));
        footer.setOpaque(false);
        footer.setPreferredSize(null);
        footer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 2));

        margin_footer.setOpaque(false);
        margin_footer.setPreferredSize(new java.awt.Dimension(30, 26));

        javax.swing.GroupLayout margin_footerLayout = new javax.swing.GroupLayout(margin_footer);
        margin_footer.setLayout(margin_footerLayout);
        margin_footerLayout.setHorizontalGroup(
            margin_footerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        margin_footerLayout.setVerticalGroup(
            margin_footerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );

        footer.add(margin_footer);

        tag_footer.setFont(new java.awt.Font("Arial", 1, 16));
        tag_footer.setForeground(new java.awt.Color(102, 0, 153));
        tag_footer.setText("</body>");
        footer.add(tag_footer);

        add(footer);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel content;
    private javax.swing.JPanel footer;
    private javax.swing.JPanel header;
    private javax.swing.JPanel margin_footer;
    private javax.swing.JPanel margin_header;
    private javax.swing.JLabel marker;
    private javax.swing.JLabel tag_footer;
    private javax.swing.JLabel tag_header;
    private javax.swing.JLabel tag_header_2;
    private javax.swing.JLabel three_dots;
    // End of variables declaration//GEN-END:variables

}
