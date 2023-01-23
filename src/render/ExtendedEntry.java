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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 *
 * @author Alex
 */
public class ExtendedEntry extends javax.swing.JPanel {

    /** Creates new form Entry */
    public ExtendedEntry() {
        initComponents();
    }

    public ExtendedEntry(Node node) {
        this.node = node;
        initComponents();
        initEvents();
    }

    public ExtendedEntry(Node node, WebDocument document) {
        this.node = node;
        this.document = document;
        initComponents();
        initEvents();
    }

    public ExtendedEntry(Node node, Block block) {
        this.node = node;
        this.block = block;
        this.document = block.document;
        Mapper.add(node, block);
        initComponents();
        initEvents();
        doLayout();
    }

    private void addAttributes() {
        final ExtendedEntry entry = this;
        callback = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                Attribute attr = (Attribute)e.getSource();
                if (command.equals("added") || command.equals("changed")) {
                    node.attributes.put(attr.getNameField(), attr.getValueField());
                }
                else if (command.equals("replaced")) {
                    node.attributes.remove(attr.getOriginalName());
                    node.attributes.put(attr.getNameField(), attr.getValueField());
                }
                else if (command.equals("removed")) {
                    node.attributes.remove(((Attribute)e.getSource()).getNameField());
                    attributes.remove(attr);
                }
                if (command.equals("added")) {
                    String text = ((Attribute)e.getSource()).getEditorText();
                    int pos = text.indexOf('=');
                    if (pos <= 0) return;
                    String name = text.substring(0, pos);
                    String value = text.substring(pos+1);
                    if (value.matches("\".*\"")) {
                        value = value.substring(1, value.length()-1);
                    }
                    Attribute a = new Attribute(entry, name, value, this);
                    int index = 0;
                    Component[] c = entry.attributes.getComponents();
                    for (int i = 0; i < c.length; i++) {
                        if (c[i] == e.getSource()) {
                            index = i;
                            break;
                        }
                    }
                    entry.attributes.add(a, index);
                    Dimension dim = attributes.getPreferredSize();
                    entry.attributes.setPreferredSize(new Dimension(dim.width + attr.getWidth(), dim.height));
                }
            }
        };
        Set<String> keys = node.attributes.keySet();
        attributes.setPreferredSize(new Dimension(0, line_height));
        int index = 0;
        for (String key: keys) {
            Attribute attr = new Attribute(this, key, node.attributes.get(key), callback);
            attributes.add(attr);
            Dimension dim = attributes.getPreferredSize();
            attributes.setPreferredSize(new Dimension(dim.width + attr.getWidth(), dim.height));
        }
        int max_width = Math.max(attributes.getPreferredSize().width + tag_header.getWidth() + tag_header_2.getWidth() + 38, getPreferredSize().width);
        header.setMinimumSize(new Dimension(max_width, line_height));
        footer.setMinimumSize(new Dimension(max_width, line_height));
        setMinimumSize(new Dimension(max_width, line_height));
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

    public void addChild(ExtendedEntry child, int pos) {
        content.add(child, pos);
        //content.validate();
    }

    public void inflate(int width) {
        if (node == null) return;
        if (node.nodeType == 1) {
            boolean isPaired = !TagLibrary.tags.containsKey(node.tagName.toLowerCase()) ||
                                TagLibrary.tags.get(node.tagName.toLowerCase());
            if (!isPaired) {
                tag_header.setText("<" + node.tagName.toLowerCase());
                tag_header_2.setText(" />");
                three_dots.setText("");
                tag_header_3.setText("");
                content.setVisible(false);
                footer.setVisible(false);
                marker.setVisible(false);
            } else {
                tag_header.setText("<" + node.tagName.toLowerCase());
                tag_header_2.setText(">");
                tag_header_3.setText("</" + node.tagName.toLowerCase() + ">");
                tag_footer.setText("</" + node.tagName.toLowerCase() + ">");
            }

            addAttributes();

            int w = Math.max(Math.max(header.getMinimumSize().width, min_width), width - margin);

            content.removeAll();
            //System.out.println(getWidth());
            for (int i = 0; i < node.children.size(); i++) {
                ExtendedEntry e = new ExtendedEntry(node.children.get(i), document);
                content.add(e);
                e.inflate(w);
                //content.setSize(e.getSize());
            }
            content.doLayout();
            if (node.children.size() > 0) {
                open();
            } else {
                close();
            }

            w = Math.max(Math.max(header.getMinimumSize().width, min_width), width - margin);
            if (content.getPreferredSize().width > w) {
                w = content.getPreferredSize().width;
            }
            setPreferredSize(new Dimension(w, getPreferredSize().height));

            int height = line_height * 2 + content.getPreferredSize().height;
            if (opened) {
                setSize(w, height);
            }
            //setMaximumSize(new Dimension(width - margin * level, 26 * 2 + content.getPreferredSize().height));
            //System.out.println(getParent().getWidth() + "x" + height);
            //System.out.println(width - margin * level);
            header.setMinimumSize(new Dimension(w, line_height));
            footer.setMinimumSize(new Dimension(w, line_height));
            content.setPreferredSize(new Dimension(w, content.getPreferredSize().height));
            ExtendedEntry last = this;
            Component c = getParent();
            while (c != null && c.getParent() != null && c.getParent() instanceof ExtendedEntry && c.getParent().getPreferredSize().width < w) {
                //c.setPreferredSize(new Dimension(w, c.getPreferredSize().height));
                Component[] children = ((JPanel)c).getComponents();
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof ExtendedEntry && children[i] != last) {
                        ((ExtendedEntry)children[i]).setWidth(w);
                    } else {
                        children[i].setSize(w, children[i].getMaximumSize().height);
                        children[i].setMaximumSize(new Dimension(w, children[i].getMaximumSize().height));
                    }
                }
                int h = line_height * 2 + ((ExtendedEntry)c.getParent()).content.getPreferredSize().height;
                c.getParent().setSize(w, h);
                //((ExtendedEntry)c.getParent()).header.setMinimumSize(new Dimension(w, line_height));
                //((ExtendedEntry)c.getParent()).footer.setMinimumSize(new Dimension(w, line_height));
                last = (ExtendedEntry)c.getParent();
                c = c.getParent().getParent();
            }
            if (c != null) c.validate();
            
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
            //textarea.setSize(content.getPreferredSize().width, textarea.getPreferredSize().height);
            content.setOpaque(false);
            //System.out.println(getParent().getWidth() + "x" + height);
            //content.setMinimumSize(new Dimension(getParent().getWidth(), height));

            //setPreferredSize(new Dimension(getParent().getWidth(), height));
            
            header.setMinimumSize(new Dimension(width, line_height));
            footer.setMinimumSize(new Dimension(width, line_height));

            int w = Math.max(min_width, width - margin);
            content.setPreferredSize(new Dimension(w, content.getPreferredSize().height));

            content.validate();
        } else {
            setVisible(false);
            content.removeAll();
        }
        
    }

    public void setWidth(int width) {
        int w = Math.max(Math.max(header.getMinimumSize().width, min_width), width - margin);
        setPreferredSize(new Dimension(w, getPreferredSize().height));
        header.setMinimumSize(new Dimension(w, line_height));
        footer.setMinimumSize(new Dimension(w, line_height));
        content.setPreferredSize(new Dimension(w, content.getPreferredSize().height));
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof ExtendedEntry) {
                ((ExtendedEntry)c[i]).setWidth(w);
            } else {
                c[i].setSize(w, c[i].getMaximumSize().height);
                c[i].setMaximumSize(new Dimension(w, c[i].getMaximumSize().height));
            }
        }
    }

    ActionListener callback;

    public static final int min_width = 280;
    public static final int line_height = 26;
    public static final int margin = 30;

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
            if (c[i] instanceof ExtendedEntry) {
                ((ExtendedEntry)c[i]).updateChildren(value);
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
        tag_header_3.setVisible(false);
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
        tag_header_3.setVisible(true);
        opened = false;
    }

    public void openAll() {
        open();
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof ExtendedEntry) {
                ((ExtendedEntry) c[i]).openAll();
            }
        }
    }

    public void closeAll() {
        close();
        Component[] c = content.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof ExtendedEntry) {
                ((ExtendedEntry) c[i]).closeAll();
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
        attributes = new javax.swing.JPanel();
        tag_header_2 = new javax.swing.JLabel();
        three_dots = new javax.swing.JLabel();
        tag_header_3 = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        footer = new javax.swing.JPanel();
        margin_footer = new javax.swing.JPanel();
        tag_footer = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        header.setBackground(new java.awt.Color(255, 255, 255));
        header.setAlignmentX(0.0F);
        header.setMaximumSize(new java.awt.Dimension(32767, 26));
        header.setMinimumSize(new java.awt.Dimension(280, 26));
        header.setOpaque(false);
        header.setPreferredSize(new java.awt.Dimension(280, 26));
        header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 2));

        margin_header.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 5));
        margin_header.setMaximumSize(new java.awt.Dimension(30, 26));
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
        tag_header.setText("<body");
        header.add(tag_header);

        attributes.setMaximumSize(new java.awt.Dimension(32767, 26));
        attributes.setOpaque(false);
        attributes.setPreferredSize(new java.awt.Dimension(0, 26));
        attributes.setLayout(new javax.swing.BoxLayout(attributes, javax.swing.BoxLayout.LINE_AXIS));
        header.add(attributes);

        tag_header_2.setFont(new java.awt.Font("Arial", 1, 16));
        tag_header_2.setForeground(new java.awt.Color(102, 0, 153));
        tag_header_2.setText(">");
        header.add(tag_header_2);

        three_dots.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        three_dots.setText("...");
        three_dots.setPreferredSize(new java.awt.Dimension(19, 20));
        header.add(three_dots);

        tag_header_3.setFont(new java.awt.Font("Arial", 1, 16));
        tag_header_3.setForeground(new java.awt.Color(102, 0, 153));
        tag_header_3.setText("</body>");
        header.add(tag_header_3);

        add(header);

        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));
        content.setAlignmentX(0.0F);
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.PAGE_AXIS));
        add(content);

        footer.setBackground(new java.awt.Color(255, 255, 255));
        footer.setAlignmentX(0.0F);
        footer.setMaximumSize(new java.awt.Dimension(32767, 26));
        footer.setOpaque(false);
        footer.setPreferredSize(new java.awt.Dimension(91, 26));
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
    private javax.swing.JPanel attributes;
    private javax.swing.JPanel content;
    private javax.swing.JPanel footer;
    private javax.swing.JPanel header;
    private javax.swing.JPanel margin_footer;
    private javax.swing.JPanel margin_header;
    private javax.swing.JLabel marker;
    private javax.swing.JLabel tag_footer;
    private javax.swing.JLabel tag_header;
    private javax.swing.JLabel tag_header_2;
    private javax.swing.JLabel tag_header_3;
    private javax.swing.JLabel three_dots;
    // End of variables declaration//GEN-END:variables

}
