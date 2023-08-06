package com.alstarsoft.tinybrowser.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;


/**
 *
 * @author Alex
 */
public class TreeTest {

    private static ValueMap prepareTree() {
        ValueMap obj = new ValueMap();
        obj.set("x", new Value(1));
        obj.set("y", new Value(1));

        ValueArray users = new ValueArray();
        obj.set("users", users);

        ValueMap user = new ValueMap();
        user.set("login", new Value("Alex654"));
        user.set("password", new Value("123456"));

        ValueMap info = new ValueMap();
        info.set("city", new Value("Moscow"));
        info.set("job", new Value("Developer"));
        info.set("firstName", new Value("Alex"));
        info.set("lastName", new Value("Popov"));
        user.set("bio", info);

        users.push(user);

        ValueMap user2 = new ValueMap();
        user2.set("login", new Value("admin"));
        user2.set("password", new Value("test"));
        user2.set("bio", new Value(null));

        users.push(user2);

        return obj;
    }

    static class Value {

        Value() {
            value = null;
        }

        Value(Object val) {
            this.value = val;
        }

        @Override
        public String toString() {
            if (value == null) {
                return "null";
            } else if (value instanceof Integer || value instanceof Double) {
                return value + "";
            } else {
                return "\"" + value.toString() + "\"";
            }
        }

        Object value;
    }

    static class ValueArray extends Value {

        ValueArray() {
            items = new ArrayList<Value>();
        }

        ValueArray(Value val) {
            items = new ArrayList<Value>();
            items.add(val);
        }

        void push(Value val) {
            items.add(val);
        }

        ArrayList<Value> items;
    }

    static class ValueMap extends Value {

        ValueMap() {
            items = new HashMap<String, Value>();
        }

        Value get(String key) {
            return items.get(key);
        }

        void put(String key, Value val) {
            items.put(key, val);
        }

        void set(String key, Value val) {
            items.put(key, val);
        }

        HashMap<String, Value> items;
    }

    public static class ValueWrapper {

        ValueWrapper(String label, Value val) {
            this.label = label;
            this.val = val;
        }

        String getLabel() {
            return label;
        }

        Value getValue() {
            return val;
        }

        @Override
        public String toString() {
            String result = label;
            if (val instanceof ValueArray) {
                result += ": Array[]";
            }
            else if (val instanceof ValueMap) {
                result += ": Object";
            }
            else result += ": " + val.toString();

            return result;
        }

        private String label;
        private Value val;
    }

    public static class SimpleTreeNode extends DefaultMutableTreeNode {
        private final Comparator comparator;

        public SimpleTreeNode(Object userObject, Comparator comparator) {
            super(userObject);
            this.comparator = comparator;
        }

        public SimpleTreeNode(Object userObject) {
            this(userObject,null);
        }

        @Override
        public void add(MutableTreeNode newChild) {
            super.add(newChild);
            if (this.comparator != null)
            {
                Collections.sort(this.children, this.comparator);
            }
        }
    }

    public static class AscKeysComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().split(":")[0].compareTo(o2.toString().split(":")[0]);
        }
        
    }

    public static void processChildren(DefaultMutableTreeNode node) {
        Value val = ((ValueWrapper)node.getUserObject()).getValue();
        if (val instanceof ValueArray) {
            ArrayList<Value> items = ((ValueArray)val).items;
            for (int i = 0; i < items.size(); i++) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new ValueWrapper("[" + i + "]", items.get(i)));
                node.add(newNode);
                processChildren(newNode);
            }
        } else if (val instanceof ValueMap) {
            HashMap<String, Value> props = ((ValueMap)val).items;
            SortedSet<String> keys = new TreeSet(props.keySet());
            for (String key: keys) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new ValueWrapper(key, props.get(key)));
                node.add(newNode);
                if (!key.equals("__proto__")) {
                    processChildren(newNode);
                }
            }
        } else {
            node.setAllowsChildren(false);
        }
    }

    static class FontMetricsWrapper extends FontMetrics {

        protected final FontMetrics target;

        public FontMetricsWrapper(FontMetrics target) {
            super(target.getFont());
            this.target = target;
        }

        @Override
        public int bytesWidth(byte[] data, int off, int len) {
            return target.bytesWidth(data, off, len);
        }

        @Override
        public int charWidth(char ch) {
            return target.charWidth(ch);
        }

        @Override
        public int charWidth(int codePoint) {
            return target.charWidth(codePoint);
        }

        @Override
        public int charsWidth(char data[], int off, int len) {
            return target.stringWidth(new String(data, off, len));
        }

        @Override
        public int stringWidth(String str) {
            return target.stringWidth(str);
        }
    }


    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        final ValueMap root = prepareTree();
        if (root == null) return;

        final JFrame frame = new JFrame("Object Inspector");
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        frame.setContentPane(cp);
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ValueWrapper("obj", root));
        processChildren(rootNode);

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);
        final JTree tree = new JTree(treeModel) {
            @Override
            public FontMetrics getFontMetrics(Font font) {
                return new FontMetricsWrapper(super.getFontMetrics(font)) {
                    @Override
                    public int getHeight() {
                        return target.getHeight() + 3;
                    }
                };
            }
        };

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setFont(new Font("Tahoma", Font.PLAIN, 18));
        renderer.setLeafIcon(null);
        renderer.setIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        renderer.setMinimumSize(new Dimension(0, 20));
        tree.setCellRenderer(renderer);
        tree.setRowHeight(23);

        final JPanel contentpane = new JPanel();
        contentpane.setBackground(Color.WHITE);
        contentpane.setOpaque(true);
        contentpane.setLayout(new BorderLayout());
        contentpane.add(tree);

        //contentpane.setBounds(0, 0, 490, 380);
        final int width = 490, height = 418;
        cp.setPreferredSize(new Dimension(width, height));

        final JScrollPane scrollpane = new JScrollPane(contentpane);
        scrollpane.setOpaque(false);
        scrollpane.getInsets();

        cp.add(scrollpane);
        scrollpane.setBackground(Color.WHITE);
        scrollpane.setOpaque(true);
        //setSize(518, 420);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
