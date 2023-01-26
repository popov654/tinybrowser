package inspector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;

/**
 *
 * @author Alex
 */
public class Attribute extends javax.swing.JPanel {

    /** Creates new form Attribute */
    public Attribute(Entry entry, String name, String value) {
        initComponents();
        this.entry = entry;
        name_field.setText(name);
        if (value == null || value.isEmpty()) {
            value_field.setText("");
            eq.setVisible(false);
            quote1.setVisible(false);
            quote2.setVisible(false);
            value_field.setVisible(false);
        } else {
            value_field.setText(value);
        }
        //setOpaque(true);
        //setBackground(Color.RED);
        setSize(name_field.getPreferredSize().width + value_field.getPreferredSize().width + 36, Entry.line_height);
        initEvents();
        originalName = name;
    }

    public Attribute(Entry entry, String name, String value, ActionListener listener) {
        this(entry, name, value);
        this.listener = listener;
    }

    private void initEvents() {
        value_field.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (value_editor.isOpaque() || full_editor.isOpaque()) return;
                value_editor.setOpaque(true);
                value_editor.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                int width = value_editor.getFontMetrics(value_editor.getFont()).stringWidth(value_field.getText());
                value_editor.setPreferredSize(new Dimension(width, value_editor.getPreferredSize().height));
                value_editor.setText(value_field.getText());
                value_field.setVisible(false);
                value_editor.requestFocus();
                value_editor.selectAll();
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
        value_editor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                if (value_editor.isOpaque()) closeValueEditor();
            }

        });

        MouseListener l = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (full_editor.isOpaque()) return;
                if (value_editor.isOpaque()) closeValueEditor();
                is_new = e.getSource() == full_editor;
                full_editor.setOpaque(true);
                full_editor.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                String text = is_new ? "" : name_field.getText() + "=\"" + value_field.getText() + "\"";
                //int width = full_editor.getFontMetrics(full_editor.getFont()).stringWidth(text);
                int width = name_field.getWidth() + value_field.getWidth() + 20;
                full_editor.setPreferredSize(new Dimension(width, full_editor.getPreferredSize().height));
                //validate();
                //full_editor.repaint();
                full_editor.setText(text);
                full_editor.getParent().setComponentZOrder(full_editor, 0);

                name_field.setVisible(false);
                eq.setVisible(false);
                quote1.setVisible(false);
                quote2.setVisible(false);
                value_field.setVisible(false);
                full_editor.requestFocus();
                //full_editor.selectAll();
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

        };

        full_editor.addMouseListener(l);
        name_field.addMouseListener(l);

        full_editor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (full_editor.getText().isEmpty()) {
                    is_new = true;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (full_editor.isOpaque()) closeFullEditor();
            }

        });

        if (entry.node.parent == null) {
            entry.getParent().addMouseListener(new MouseListener() {

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

        if (entry != null) entry.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                getRootPane().requestFocus();
                if (value_editor.isOpaque()) closeValueEditor();
                else if (full_editor.isOpaque()) closeFullEditor();
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

        });
    }

    private void closeValueEditor() {
        value_editor.setBorder(null);
        value_editor.setPreferredSize(new Dimension(0, value_editor.getPreferredSize().height));
        value_field.setText(value_editor.getText());
        value_field.setVisible(true);
        value_editor.setOpaque(false);

        value_editor.setText("");

        if (listener != null) {
            String command = "changed";
            listener.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, command));
        }

        setSize(name_field.getSize().width + value_field.getSize().width + eq.getSize().width + quote1.getSize().width + quote2.getSize().width, Entry.line_height);
    }

    private void closeFullEditor() {
        full_editor.setBorder(null);
        full_editor.setPreferredSize(new Dimension(full_editor_width, value_editor.getPreferredSize().height));

        name_field.setVisible(true);
        eq.setVisible(true);
        quote1.setVisible(true);
        quote2.setVisible(true);
        value_field.setVisible(true);
        full_editor.setOpaque(false);

        parseFullValue();

        if (listener != null) {
            String command = is_new ? "added" : "changed";
            if (!name_field.getText().equals(originalName)) command = "replaced";
            if (name_field.getText().isEmpty()) command = "removed";
            listener.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, command));
        }

        full_editor.setText("");
        full_editor.setPreferredSize(new Dimension(6, 22));

        is_new = false;

        setSize(name_field.getSize().width + value_field.getSize().width + eq.getSize().width + quote1.getSize().width + quote2.getSize().width, Entry.line_height);
    }

    private void parseFullValue() {
        if (is_new) return;
        int pos = full_editor.getText().indexOf('=');
        if (pos <= 0) {
            name_field.setText("");
            value_field.setText("");
            name_field.setVisible(false);
            value_field.setVisible(false);
            eq.setVisible(false);
            quote1.setVisible(false);
            quote2.setVisible(false);
        } else {
            String name = full_editor.getText().substring(0, pos);
            String value = full_editor.getText().substring(pos+1);
            if (value.matches("\".*\"")) {
                value = value.substring(1, value.length()-1);
            }
            name_field.setText(name);
            value_field.setText(value);
        }
    }

    public String getEditorText() {
        return full_editor.getText();
    }

    public String getNameField() {
        return name_field.getText();
    }

    public String getValueField() {
        return value_field.getText();
    }

    public String getOriginalName() {
        return originalName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        full_editor = new javax.swing.JTextField();
        name_field = new javax.swing.JLabel();
        eq = new javax.swing.JLabel();
        quote1 = new javax.swing.JLabel();
        value_editor = new javax.swing.JTextField();
        value_field = new javax.swing.JLabel();
        quote2 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(150000, 26));
        setMinimumSize(new java.awt.Dimension(84, 26));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(400, 26));
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 2));

        full_editor.setBorder(null);
        full_editor.setMinimumSize(new java.awt.Dimension(0, 22));
        full_editor.setOpaque(false);
        full_editor.setPreferredSize(new java.awt.Dimension(6, 22));
        add(full_editor);

        name_field.setForeground(new java.awt.Color(102, 0, 102));
        name_field.setText("class");
        name_field.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(name_field);

        eq.setForeground(new java.awt.Color(102, 0, 102));
        eq.setText("=");
        add(eq);

        quote1.setForeground(new java.awt.Color(153, 153, 153));
        quote1.setText("\"");
        add(quote1);

        value_editor.setBorder(null);
        value_editor.setMinimumSize(new java.awt.Dimension(0, 22));
        value_editor.setOpaque(false);
        value_editor.setPreferredSize(new java.awt.Dimension(0, 22));
        add(value_editor);

        value_field.setForeground(new java.awt.Color(0, 51, 204));
        value_field.setText("link");
        add(value_field);

        quote2.setForeground(new java.awt.Color(153, 153, 153));
        quote2.setText("\"");
        add(quote2);
    }// </editor-fold>//GEN-END:initComponents

    private final int full_editor_width = 8;
    private boolean is_new = false;

    private String originalName;
    private Entry entry;
    private ActionListener listener;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel eq;
    private javax.swing.JTextField full_editor;
    private javax.swing.JLabel name_field;
    private javax.swing.JLabel quote1;
    private javax.swing.JLabel quote2;
    private javax.swing.JTextField value_editor;
    private javax.swing.JLabel value_field;
    // End of variables declaration//GEN-END:variables

}
