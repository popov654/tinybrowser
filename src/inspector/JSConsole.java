package inspector;

import bridge.Builder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import jsparser.Console;
import jsparser.Expression;
import jsparser.Function;
import jsparser.HTMLElement;
import jsparser.HTMLNode;
import jsparser.JSArray;
import jsparser.JSObject;
import jsparser.JSParser;
import jsparser.JSValue;
import jsparser.Undefined;
import render.Block;
import render.Util;
import render.WebDocument;
import service.FontManager;

/**
 *
 * @author Alex
 */
public class JSConsole {

    public static boolean allowSelection = true;
    public static int line_height = 24;

    public static void insertConsole(final JFrame frame, Container c, final WebDocument document) {
        final Block root = document.root;
        int width = c.getPreferredSize().width;

        c.add(Box.createRigidArea(new Dimension(0, 10)));

        final JPanel consolepane = new JPanel();
        consolepane.setBackground(Color.WHITE);
        consolepane.setOpaque(true);
        consolepane.setPreferredSize(new Dimension(width, 100));
        consolepane.setBorder(BorderFactory.createLineBorder(new Color(136, 138, 143), 1));

        consolepane.setLayout(new BoxLayout(consolepane, BoxLayout.PAGE_AXIS));

        console = new JPanel();
        console.setBackground(Color.WHITE);
        console.setLayout(new BoxLayout(console, BoxLayout.PAGE_AXIS));

        final JScrollPane scrollpane2 = new JScrollPane(console);
        scrollpane2.getVerticalScrollBar().setUnitIncrement(20);
        scrollpane2.getHorizontalScrollBar().setUnitIncrement(20);
        scrollpane2.getInsets();
        scrollpane2.setBorder(null);

        consolepane.add(scrollpane2);
        scrollpane2.setBackground(Color.WHITE);
        scrollpane2.setOpaque(true);
        scrollpane2.setPreferredSize(new Dimension(width, 100 - line_height - 6));
        scrollpane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        consolepane.add(scrollpane2);

        JPanel separator = new JPanel();
        separator.setBackground(new Color(228, 228, 235));
        separator.setPreferredSize(new Dimension(width - 20, 1));
        consolepane.add(separator);

        final JTextArea consoleInput = new JTextArea();
        consoleInput.setPreferredSize(new Dimension(width, line_height));
        consoleInput.setBorder(null);
        consolepane.add(consoleInput);

        try {
            FontManager.registerFont(Util.getInstallPath() + "fonts/consola.ttf", "Consolas");
        } catch (Exception e) {}

        consoleInput.setFont(new Font("Consolas", Font.PLAIN, 16));
        consoleInput.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        consoleInput.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String code = ((JTextArea)e.getSource()).getText();
                    JSParser jp = new JSParser(code);
                    Expression exp = Expression.create(jp.getHead());
                    if (root.node != null) {
                        ((jsparser.Block)exp).setDocument(root.node.document);
                    }
                    Frame parent = frame.getOwner() != null ? (Frame) frame.getOwner() : frame;
                    ((jsparser.Block)exp).setWindowFrame(parent);

                    int pos = 0;
                    Vector<String> data = null;

                    JSValue c = ((jsparser.Block)exp).scope.get("console");
                    if (!(c instanceof Undefined)) {
                        data = ((Console)c).getData();
                        pos = data.size();
                    }

                    exp.eval();

                    if (data != null) {
                        for (int i = pos; i < data.size(); i++) {
                            addEntry(console, data.get(i));
                        }
                    }

                    JSValue result = exp.getValue();

                    if (result.getType().matches("Boolean|Integer|Float|Number|String|null|undefined") || result instanceof Function) {
                        addEntry(console, exp.getValue().toString());
                    } else {
                        addObjectEntry(console, result);
                    }

                    ((JTextArea)e.getSource()).setText("");
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

        });

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Builder builder = document.root.builder;
                if (builder.compiledScripts.size() > 0) {
                    JSValue con = Expression.getVar("console", builder.compiledScripts.get(0));
                    if (con != null && con instanceof Console) {
                        Vector<String> data = ((Console)con).getData();
                        for (String line: data) {
                            addEntry(console, line);
                        }
                    }
                }
            }
        });

        c.add(consolepane);

        consolepane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollpane2.setPreferredSize(new Dimension(consolepane.getWidth(), consolepane.getHeight() - line_height - 6));
                        console.setPreferredSize(new Dimension(console.getWidth(), Math.max(console.getPreferredSize().height, consolepane.getHeight() - line_height - 6)));
                    }
                });
            }
        });
    }

    private static void addEntry(JPanel console, String str) {
        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
        resultPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        JLabel text = new JLabel(str, JLabel.LEFT);
        text.setFont(new Font("Consolas", Font.PLAIN, 16));
        text.setForeground(new Color(120, 25, 0));
        FontMetrics fm = text.getFontMetrics(text.getFont());

        final Vector<String> lines = new Vector<String>();

        int width = console.getWidth() - 30;

        if (fm.stringWidth(str) > width) {
            String[] words = str.split("((?<=\\s+)|(?=\\s+))");
            String line = "";
            for (int i = 0; i < words.length; i++) {
                if (words[i].isEmpty()) continue;
                if (fm.stringWidth(line + words[i]) > width || words[i].endsWith("\n")) {
                    if (words[i].endsWith("\n")) i++;

                    int n = 0;
                    while (i < words.length && words[i].matches("\\s*")) {
                        n++;
                        i++;
                    }
                    if (n > 1 && !line.matches("\\s+")) {
                        line += " ";
                    }

                    if (line.isEmpty()) {
                        line += words[i];
                    } else {
                        i--;
                    }
                    lines.add(line);
                    line = "";
                    
                    continue;
                }
                line += words[i];
                if (line.endsWith("\n")) {
                    lines.add(line);
                    line = "";
                }
            }
            if (!line.isEmpty()) {
                lines.add(line);
            }
        } else {
            lines.add(str);
        }

        int height = (line_height + 3) * lines.size() + 2;

        if (!allowSelection) {
            for (String s: lines) {
                JLabel label = new JLabel(s, JLabel.LEFT);
                label.setPreferredSize(new Dimension(Math.max(console.getWidth(), fm.stringWidth(s)), line_height - 2));
                resultPanel.add(label);
            }

            resultPanel.setPreferredSize(new Dimension(console.getWidth(), height));
        } else {
            JTextArea textarea = new JTextArea() {
                @Override
                public FontMetrics getFontMetrics(Font font) {
                    return new FontMetricsWrapper(super.getFontMetrics(font)) {
                        @Override
                        public int getHeight() {
                            return line_height + 2;
                        }
                    };
                }
            };
            String txt = "";
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0 && !lines.get(i-1).endsWith("\n")) txt += "\n";
                txt += lines.get(i);
            }
            textarea.setText(txt);
            textarea.setFont(text.getFont());
            textarea.setForeground(text.getForeground());
            textarea.setEditable(false);
            textarea.setMargin(new java.awt.Insets(0, 2, 0, 2));

            //textarea.setSize(new Dimension(width, height));
            height = lines.size() == 1 ? line_height : (line_height + 3) * lines.size();
            textarea.setMinimumSize(new Dimension(width, height));
            textarea.setPreferredSize(new Dimension(width, height));
            textarea.setMaximumSize(new Dimension(width, height));
            //textarea.setBackground(new Color(230, 236, 238));
            resultPanel.add(textarea);
        }

        console.add(resultPanel);

        recalculateContentHeight();
    }

    private static void addObjectEntry(JPanel console, JSValue val) {
        if (!(val instanceof JSArray) && (!(val instanceof JSObject) || val instanceof Function)) {
            return;
        }
        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
        resultPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel tree = createObjectTree((JSObject)val);
        resultPanel.add(tree);

        console.add(resultPanel);

        recalculateContentHeight();
    }

    static void recalculateContentHeight() {
        int height = 0;
        Component[] c = console.getComponents();
        for (int i = 0; i < c.length; i++) {
            height += c[i].getPreferredSize().height;
        }
        console.setPreferredSize(new Dimension(console.getWidth(), height));
        console.getParent().validate();
        //console.getParent().setPreferredSize(new Dimension(console.getWidth(), height));
    }

    static JPanel console;

    static class TreeExpandListener implements TreeWillExpandListener {

        @Override
        public void treeWillExpand(final TreeExpansionEvent e) throws ExpandVetoException {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            if (node.getChildCount() == 0) {
                loadNodeDirectChildren(node, false);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ((JTree)e.getSource()).invalidate();
                        ((JTree)e.getSource()).getParent().validate();
                        recalculateContentHeight();
                    }
                });
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent e) {}

        public void treeExpanded(TreeExpansionEvent e) {}

        public void treeCollapsed(TreeExpansionEvent e) {}
    }

    public static class FontMetricsWrapper extends FontMetrics {

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

    public static class JSValueWrapper {

        public JSValueWrapper(String label, JSValue val) {
            this.label = label;
            this.val = val;
        }

        public String getLabel() {
            return label;
        }

        public JSValue getValue() {
            return val;
        }

        @Override
        public String toString() {
            String result = (label != null && !label.isEmpty()) ? label + ": " : "";
            if (val instanceof Function) {
                result += "{ Function }";
            }
            else if (val instanceof JSArray) {
                result += "Array[]";
            }
            else if (val instanceof HTMLElement) {
                result += "HTMLElement[" + ((HTMLElement)val).node.tagName + "]";
            }
            else if (val instanceof HTMLNode && ((HTMLNode)val).node.nodeType == 3) {
                result += "TextNode \"" + ((HTMLNode)val).node.nodeValue + "\"";
            }
            else if (val instanceof HTMLNode && ((HTMLNode)val).node.nodeType == 8) {
                result += "CommentNode \"" + ((HTMLNode)val).node.nodeValue + "\"";
            }
            else if (val instanceof JSObject && !val.getType().matches("String|Integer|Float|Number|Boolean|null|undefined")) {
                result += "Object";
            }
            else result += val.toString();

            return result;
        }

        private String label;
        private JSValue val;
    }

    private static JPanel createObjectTree(JSObject obj) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new JSValueWrapper(null, obj));
        loadNodeDirectChildren(rootNode, false);

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);
        final JTree tree = new JTree(treeModel);
        TreeExpandListener expansionListener = new TreeExpandListener();
        tree.addTreeWillExpandListener(expansionListener);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setFont(new Font("Consolas", Font.PLAIN, 16));
        renderer.setLeafIcon(null);
        renderer.setIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        renderer.setMinimumSize(new Dimension(0, 20));
        tree.setForeground(new Color(120, 25, 0));
        tree.setCellRenderer(renderer);
        tree.setRowHeight(23);

        final JPanel contentpane = new JPanel();
        contentpane.setBackground(Color.WHITE);
        contentpane.setOpaque(true);
        contentpane.setLayout(new BorderLayout());
        contentpane.add(tree);

        return contentpane;
    }

    private static void processNodeChildren(DefaultMutableTreeNode node, boolean show_prototypes) {
        JSValue val = ((JSValueWrapper)node.getUserObject()).getValue();
        if (val instanceof JSArray) {
            Vector<JSValue> items = ((JSArray)val).getItems();
            for (int i = 0; i < items.size(); i++) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper("[" + i + "]", items.get(i)));
                node.add(newNode);
                processNodeChildren(newNode, show_prototypes);
            }
        } else if (!(val instanceof Function) && !val.getType().matches("Boolean|Integer|Float|Number|String|null|undefined") && val instanceof JSObject) {
            HashMap<String, JSValue> props = ((JSObject)val).getProperties();
            Set<String> keys = props.keySet();
            for (String key: keys) {
                if (!show_prototypes && key.equals("__proto__")) continue;
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper(key, props.get(key)));
                node.add(newNode);
                if (!key.equals("__proto__")) {
                    processNodeChildren(newNode, show_prototypes);
                } else {
                    newNode.setAllowsChildren(false);
                }
            }
        } else {
            node.setAllowsChildren(false);
        }
    }

    private static void loadNodeDirectChildren(DefaultMutableTreeNode node, boolean show_prototypes) {
        JSValue val = ((JSValueWrapper)node.getUserObject()).getValue();
        if (val instanceof JSArray) {
            Vector<JSValue> items = ((JSArray)val).getItems();
            for (int i = 0; i < items.size(); i++) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper("[" + i + "]", items.get(i)));
                node.add(newNode);
            }
        } else if (!(val instanceof Function) && !val.getType().matches("Boolean|Integer|Float|Number|String|null|undefined") && val instanceof JSObject) {
            HashMap<String, JSValue> props = ((JSObject)val).getProperties();
            Set<String> keys = props.keySet();
            for (String key: keys) {
                if (!show_prototypes && key.equals("__proto__")) continue;
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper(key, props.get(key)));
                node.add(newNode);
                if (props.get(key) instanceof Function || props.get(key).getType().matches("Boolean|Integer|Float|Number|String|null|undefined")) {
                    newNode.setAllowsChildren(false);
                }
                if (key.equals("__proto__")) {
                    newNode.setAllowsChildren(false);
                }
            }
        } else {
            node.setAllowsChildren(false);
        }
    }

}
