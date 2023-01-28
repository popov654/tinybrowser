package inspector;

import bridge.Mapper;
import htmlparser.Node;
import htmlparser.TagLibrary;
import render.Block;
import render.WebDocument;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JPanel;
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

    private void addAttributes() {
        final Entry entry = this;
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
                    entry.updateHeaderWidth();
                }
            }
        };
        Set<String> keys = node.attributes.keySet();
        int index = 0;
        for (String key: keys) {
            Attribute attr = new Attribute(this, key, node.attributes.get(key), callback);
            attributes.add(attr);
            //System.err.println(attr.getNameField() + ": " + attr.getValueField() + " -> " + (dim.width + attr.getWidth()));
        }
        if (!attributesEnabled) {
            attributes.setVisible(false);
        }
        updateHeaderWidth();
    }

    public void updateHeaderWidth() {
        int max_width = Math.max(attributes.getPreferredSize().width + headerTag.getSize().width + headerTag2.getSize().width + Entry.margin, getPreferredSize().width);
        if (max_width > 2000) max_width += 120;
        header.setSize(new Dimension(max_width, line_height));
        footer.setSize(new Dimension(max_width, line_height));
        setMinimumSize(new Dimension(max_width, line_height));
        header.setPreferredSize(new Dimension(max_width, line_height));
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

    public void inflate(int width) {
        if (node == null) return;
        if (node.parent == null && getParent() != null && getParent().getMouseListeners().length == 0) {
            getParent().addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {
                    getRootPane().requestFocus();
                }

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }
        if (node.nodeType == 1) {
            boolean isPaired = !TagLibrary.tags.containsKey(node.tagName.toLowerCase()) ||
                                TagLibrary.tags.get(node.tagName.toLowerCase());
            if (!isPaired) {
                headerTag.setText("<" + node.tagName.toLowerCase());
                headerTag2.setText(" />");
                threeDots.setText("");
                headerTag3.setText("");
                content.setVisible(false);
                footer.setVisible(false);
                marker.setVisible(false);
            } else {
                headerTag.setText("<" + node.tagName.toLowerCase());
                headerTag2.setText(">");
                headerTag3.setText("</" + node.tagName.toLowerCase() + ">");
                footerTag.setText("</" + node.tagName.toLowerCase() + ">");
            }

            addAttributes();

            int w = Math.max(Math.max(header.getMinimumSize().width - margin, min_width), width - margin);

            content.removeAll();
            for (int i = 0; i < node.children.size(); i++) {
                Entry e = new Entry(node.children.get(i), document);
                content.add(e);
                e.inflate(w);
            }
            content.doLayout();
            if (node.children.size() > 0) {
                open();
            } else if (node.nodeType == 1) {
                close();
            }

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

            int height = getFontMetrics(textarea.getFont()).getHeight() * rows + 4;

            content.add(textarea);
            //textarea.setSize(content.getPreferredSize().width, textarea.getPreferredSize().height);
            content.setOpaque(false);

            int w = Math.max(Math.max(header.getMinimumSize().width, min_width), width);
            
            header.setSize(new Dimension(w, line_height));
            footer.setSize(new Dimension(w, line_height));

            content.setPreferredSize(new Dimension(w, height));
            opened = true;

            content.validate();
        } else {
            setVisible(false);
            content.removeAll();
            opened = false;
            return;
        }

        int w = Math.max(Math.max(Math.max(content.getPreferredSize().width, header.getPreferredSize().width), min_width), width);

        if (attributesEnabled) updateWidth(w);
        
    }

    @Override
    public Dimension getPreferredSize() {
        int height = opened ? line_height * 2 + content.getPreferredSize().height : line_height;
        if (node.nodeType == 3 && !node.nodeValue.matches("\\s*")) {
            int rows = node.nodeValue.split("\n").length;
            height = getFontMetrics(content.getComponents()[0].getFont()).getHeight() * rows + 3;
        }
        int w = Math.max(Math.max(header.getPreferredSize().width, content.getPreferredSize().width), min_width);
        //if (getParent().getSize().width > w) w = getParent().getSize().width;
        return new Dimension(w, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(32767, getPreferredSize().height);
    }

    public void setWidth(int width) {
        int w = Math.max(Math.max(header.getMinimumSize().width, min_width), width);
        if (getSize().width > w) return;
        setSize(new Dimension(w, getSize().height));
    }

    private void updateWidth(int width) {
        int w = Math.max(Math.max(header.getSize().width, min_width), width);
        if (content.getSize().width > w) {
            w = content.getSize().width;
        }
        if (getSize().width > w) return;
        setSize(new Dimension(w, getSize().height));

        Entry last = this;
        Component c = getParent();
        while (c != null && c.getParent() != null && c.getParent() instanceof Entry) {
            //c.setPreferredSize(new Dimension(w, c.getPreferredSize().height));
            Component[] children = ((JPanel)c).getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Entry && children[i] != last && children[i].getSize().width < w) {
                    ((Entry)children[i]).setWidth(w);
                } else if (!(children[i] instanceof Entry) && children[i].getParent() != last && children[i].getSize().width < w) {
                    children[i].setSize(w, children[i].getMaximumSize().height);
                    children[i].setMaximumSize(new Dimension(w, children[i].getMaximumSize().height));
                }
            }
            w += margin;
            w = Math.max(c.getParent().getSize().width, w);
            //if (((Entry)c.getParent()).node.tagName.equals("body")) System.err.println("Root entry width: " + w);
            int h = line_height * 2 + ((Entry)c.getParent()).content.getSize().height;
            c.getParent().setSize(w, h);
            ((Entry)c.getParent()).header.setSize(new Dimension(w, line_height));
            ((Entry)c.getParent()).footer.setSize(new Dimension(w, line_height));
            //((Entry)c.getParent()).content.setSize(new Dimension(w, ((Entry)c.getParent()).getSize().height - line_height * 2));
            last = (Entry)c.getParent();
            c = c.getParent().getParent();
        }
        if (c != null) c.validate();

        content.validate();
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
            updateChildren(true);
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
    }

    private boolean hovered = false;

    public void open() {
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle.png")));
        threeDots.setVisible(false);
        headerTag3.setVisible(false);
        content.setVisible(true);
        footer.setVisible(true);
        opened = true;

        int max_width = Math.max(attributes.getPreferredSize().width + headerTag.getSize().width + headerTag2.getSize().width + headerMargin.getSize().width + 18, getPreferredSize().width);
        header.setSize(new Dimension(max_width, line_height));
        footer.setSize(new Dimension(max_width, line_height));
        setMinimumSize(new Dimension(max_width, line_height));
        header.setPreferredSize(new Dimension(max_width, line_height));

        if (getParent().getParent() != null) {
            getParent().getParent().validate();
        }
    }

    public void close() {
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle2.png")));
        content.setVisible(false);
        footer.setVisible(false);
        boolean has_children = node.children.size() > 0;
        threeDots.setVisible(has_children);
        marker.setVisible(has_children);
        headerTag3.setVisible(true);
        opened = false;

        int max_width = Math.max(attributes.getPreferredSize().width + headerTag.getSize().width + headerTag2.getSize().width + headerMargin.getSize().width + headerTag3.getSize().width + threeDots.getSize().width, getPreferredSize().width);
        header.setSize(new Dimension(max_width, line_height));
        footer.setSize(new Dimension(max_width, line_height));
        setMinimumSize(new Dimension(max_width, line_height));
        header.setPreferredSize(new Dimension(max_width, line_height));

        if (getParent().getParent() != null) {
            getParent().getParent().validate();
        }
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
    public boolean attributesEnabled = true;

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
        headerMargin = new javax.swing.JPanel();
        marker = new javax.swing.JLabel();
        headerTag = new javax.swing.JLabel();
        attributes = new javax.swing.JPanel();
        headerTag2 = new javax.swing.JLabel();
        threeDots = new javax.swing.JLabel();
        headerTag3 = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        footer = new javax.swing.JPanel();
        footerMargin = new javax.swing.JPanel();
        footerTag = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        header.setBackground(new java.awt.Color(255, 255, 255));
        header.setAlignmentX(0.0F);
        header.setMaximumSize(new java.awt.Dimension(32767, 26));
        header.setMinimumSize(new java.awt.Dimension(280, 26));
        header.setOpaque(false);
        header.setPreferredSize(new java.awt.Dimension(280, 26));
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.LINE_AXIS));

        headerMargin.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 5));
        headerMargin.setMaximumSize(new java.awt.Dimension(30, 26));
        headerMargin.setOpaque(false);
        headerMargin.setPreferredSize(new java.awt.Dimension(30, 26));

        marker.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        marker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangle.png"))); // NOI18N
        marker.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        marker.setPreferredSize(new java.awt.Dimension(22, 22));

        javax.swing.GroupLayout headerMarginLayout = new javax.swing.GroupLayout(headerMargin);
        headerMargin.setLayout(headerMarginLayout);
        headerMarginLayout.setHorizontalGroup(
            headerMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerMarginLayout.createSequentialGroup()
                .addComponent(marker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        headerMarginLayout.setVerticalGroup(
            headerMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerMarginLayout.createSequentialGroup()
                .addComponent(marker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        header.add(headerMargin);

        headerTag.setFont(new java.awt.Font("Arial", 1, 16));
        headerTag.setForeground(new java.awt.Color(102, 0, 153));
        headerTag.setText("<body");
        header.add(headerTag);

        attributes.setAlignmentY(0.5F);
        attributes.setMaximumSize(new java.awt.Dimension(32767, 26));
        attributes.setOpaque(false);
        attributes.setPreferredSize(new java.awt.Dimension(0, 26));
        attributes.setLayout(new javax.swing.BoxLayout(attributes, javax.swing.BoxLayout.LINE_AXIS));
        header.add(attributes);

        headerTag2.setFont(new java.awt.Font("Arial", 1, 16));
        headerTag2.setForeground(new java.awt.Color(102, 0, 153));
        headerTag2.setText(">");
        header.add(headerTag2);

        threeDots.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        threeDots.setText("...");
        threeDots.setPreferredSize(new java.awt.Dimension(19, 20));
        header.add(threeDots);

        headerTag3.setFont(new java.awt.Font("Arial", 1, 16));
        headerTag3.setForeground(new java.awt.Color(102, 0, 153));
        headerTag3.setText("</body>");
        header.add(headerTag3);

        header.add(Box.createHorizontalGlue());

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

        footerMargin.setOpaque(false);
        footerMargin.setPreferredSize(new java.awt.Dimension(30, 26));

        javax.swing.GroupLayout footerMarginLayout = new javax.swing.GroupLayout(footerMargin);
        footerMargin.setLayout(footerMarginLayout);
        footerMarginLayout.setHorizontalGroup(
            footerMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        footerMarginLayout.setVerticalGroup(
            footerMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );

        footer.add(footerMargin);

        footerTag.setFont(new java.awt.Font("Arial", 1, 16));
        footerTag.setForeground(new java.awt.Color(102, 0, 153));
        footerTag.setText("</body>");
        footer.add(footerTag);

        add(footer);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributes;
    private javax.swing.JPanel content;
    private javax.swing.JPanel footer;
    private javax.swing.JPanel footerMargin;
    private javax.swing.JLabel footerTag;
    private javax.swing.JPanel header;
    private javax.swing.JPanel headerMargin;
    private javax.swing.JLabel headerTag;
    private javax.swing.JLabel headerTag2;
    private javax.swing.JLabel headerTag3;
    private javax.swing.JLabel marker;
    private javax.swing.JLabel threeDots;
    // End of variables declaration//GEN-END:variables

}
