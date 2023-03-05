package inspector;

import bridge.Builder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.JTextComponent;
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

    public boolean allowSelection = true;
    public int line_height = 24;

    public void insertConsole(final JFrame frame, Container c, final WebDocument document) {
        final Block root = document.root;
        int width = c.getPreferredSize().width;

        c.add(Box.createRigidArea(new Dimension(0, 10)));

        final JPanel consolepane = new JPanel();
        consolepane.setBackground(Color.WHITE);
        consolepane.setOpaque(true);
        consolepane.setPreferredSize(new Dimension(width, 110));
        consolepane.setBorder(BorderFactory.createLineBorder(new Color(136, 138, 143), 1));

        consolepane.setLayout(new BoxLayout(consolepane, BoxLayout.PAGE_AXIS));

        console = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                int height = 0;
                Component[] c = getComponents();
                for (int i = 0; i < c.length; i++) {
                    height += c[i].getPreferredSize().height;
                }
                return new Dimension(getParent().getWidth(), height);
            }
        };
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

        JPanel separator = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getSize().width, 1);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        separator.setBackground(new Color(228, 228, 235));
        consolepane.add(separator);

        consoleInput = new JTextArea() {
            @Override
            public Dimension getPreferredSize() {
                int rows = Math.max(1, getRows());
                return new Dimension(getParent().getSize().width, rows * line_height + 2);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        //consoleInput.setPreferredSize(new Dimension(width, line_height + 2));
        //consoleInput.setMaximumSize(new Dimension(width, line_height + 2));
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
                int key_code = e.getKeyCode();
                if (key_code == KeyEvent.VK_BACK_SPACE || key_code == KeyEvent.VK_DELETE ||
                      key_code == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown())) {
                    int n = consoleInput.getText().split("\n").length;
                    Frame frame = (Frame) SwingUtilities.getWindowAncestor(consoleInput);
                    Dimension dim = frame.getSize();
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        consoleInput.setText(consoleInput.getText() + "\n");
                        frame.setSize(dim.width, dim.height + line_height);
                        consoleInput.setRows(n + 1);
                        consolepane.setPreferredSize(new Dimension(consolepane.getPreferredSize().width, consolepane.getSize().height + line_height));
                    } else {
                        int pos = consoleInput.getCaretPosition();
                        if (pos > 0 && key_code == KeyEvent.VK_BACK_SPACE && consoleInput.getText().charAt(pos-1) == '\n' ||
                              pos < consoleInput.getText().length() - 1 && key_code == KeyEvent.VK_DELETE && consoleInput.getText().charAt(pos+1) == '\n') {
                            frame.setSize(dim.width, dim.height - line_height);
                            consoleInput.setRows(n - 1);
                            consolepane.setPreferredSize(new Dimension(consolepane.getPreferredSize().width, consolepane.getSize().height - line_height));
                        }
                        timer.start();
                    }
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (suggestions != null && suggestions.selectedItem != null) {
                        String text = ((JLabel)((Container)suggestions.selectedItem).getComponent(0)).getText();
                        updateCurrentToken(((JTextArea)e.getSource()), text);
                        e.consume();
                        return;
                    }
                    String code = ((JTextArea)e.getSource()).getText();
                    JSParser jp = new JSParser(code);
                    jsparser.Block b = (jsparser.Block) Expression.create(jp.getHead());
                    if (root.node != null) {
                        b.setDocument(root.node.document);
                        Builder builder = document.root.builder;
                        b.scope = scope = builder.scope;
                        if (scope == null) {
                            b.setDocument(root.node.document);
                            scope = b.scope;
                        }
                        b.setConsole((jsparser.Console)scope.get("console"));
                    }
                    Frame parent = frame.getOwner() != null ? (Frame) frame.getOwner() : frame;
                    b.setWindowFrame(parent);

                    int pos = 0;
                    Vector<String> data = null;

                    JSValue c = b.scope.get("console");
                    if (!(c instanceof Undefined)) {
                        data = ((Console)c).getData();
                        pos = data.size();
                    }

                    if (data != null) {
                        for (int i = pos; i < data.size(); i++) {
                            addEntry(data.get(i));
                        }
                    }

                    b.eval();

                    JSValue result = b.getValue();

                    addEntry(((JTextArea)e.getSource()).getText(), false);
                    if (result.getType().matches("Boolean|Integer|Float|Number|String|null|undefined") || result instanceof Function) {
                        addEntry(b.getValue().toString());
                    } else {
                        addObjectEntry(result);
                    }

                    ((JTextArea)e.getSource()).setText("");
                    if (suggestions != null) {
                        suggestions.setVisible(false);
                    }

                    consoleInput.setRows(0);
                    e.consume();
                    return;
                }
                if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) && suggestions != null && suggestions.isVisible()) {
                    Suggestion s = (Suggestion) suggestions.getSelectedItem();
                    if (s == null) {
                        suggestions.setSelectedItem(suggestions.getComponent(0));
                    } else {
                        suggestions.setSelectedItem(e.getKeyCode() == KeyEvent.VK_UP ? s.prev : s.next);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (timer.isRunning()) timer.stop();
                if (e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_RIGHT &&
                      e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
                    findCurrentTokenSuggestions((JTextComponent) e.getSource());
                }
            }

            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    findCurrentTokenSuggestions(consoleInput);
                }
            });

        });

        initPopupMenu();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Builder builder = document.root.builder;
                if (builder.scripts.size() > 0) {
                    JSValue con = Expression.getVar("console", builder.scripts.get(0).getBody());
                    if (con != null && con instanceof Console) {
                        Vector<String> data = ((Console)con).getData();
                        for (String line: data) {
                            addEntry(line);
                        }
                        ((Console)con).addListener(new Console.Listener() {
                            @Override
                            public void log(String message) {
                                addEntry(message);
                            }

                            @Override
                            public void clear() {
                                clearConsole();
                            }
                        });
                    }
                }
                consoleInput.requestFocus();
            }
        });

        c.add(consolepane);

        consolepane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                //final JPanel consolepane = (JPanel) evt.getSource();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollpane2.setPreferredSize(new Dimension(consolepane.getWidth(), consolepane.getHeight() - consoleInput.getSize().height - 4));
                        console.setPreferredSize(new Dimension(console.getWidth(), Math.max(console.getPreferredSize().height, consolepane.getHeight() - consoleInput.getSize().height - 4)));
                    }
                });
            }
        });
    }

    private void initPopupMenu() {
        consoleMenu = new JPopupMenu();
        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.setMargin(new Insets(2, 10, 2, -10));
        clearItem.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        clearItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearConsole();
            }
        });
        consoleMenu.add(clearItem);
        console.addMouseListener(contextMenuListener);
        consoleInput.addMouseListener(contextMenuListener);
    }

    private MouseListener contextMenuListener = new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                consoleMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    };

    private void addEntry(String str) {
        addEntry(str, true);
    }

    private void addEntry(String str, boolean isResult) {
        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
        resultPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        JLabel text = new JLabel(str, JLabel.LEFT);
        text.setFont(new Font("Consolas", Font.PLAIN, 16));
        if (isResult) {
            text.setForeground(new Color(120, 25, 0));
        } else {
            text.setFont(new Font("Consolas", Font.ITALIC, 16));
        }
        FontMetrics fm = text.getFontMetrics(text.getFont());

        Vector<String> lines = new Vector<String>();

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

        int height = (line_height + 2) * lines.size() + 2;

        if (!allowSelection) {
            for (String s: lines) {
                JLabel label = new JLabel(s, JLabel.LEFT);
                label.setPreferredSize(new Dimension(Math.max(console.getWidth(), fm.stringWidth(s)), line_height - 2));
                resultPanel.add(label);
                label.addMouseListener(contextMenuListener);
            }

            resultPanel.setPreferredSize(new Dimension(console.getWidth(), height));
        } else {
            JTextArea textarea = new JTextArea() {
                @Override
                public FontMetrics getFontMetrics(Font font) {
                    return new FontMetricsWrapper(super.getFontMetrics(font)) {
                        @Override
                        public int getHeight() {
                            return line_height + 1;
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
            textarea.addMouseListener(contextMenuListener);
        }

        console.add(resultPanel);
        resultPanel.addMouseListener(contextMenuListener);

    }

    private void addObjectEntry(JSValue val) {
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

    }

    public void clearConsole() {
        console.removeAll();
        console.revalidate();
        console.repaint();
        JSParser jp = new JSParser("null");
        Expression exp = Expression.create(jp.getHead());
        JSValue c = ((jsparser.Block)exp).scope.get("console");
        if (!(c instanceof Undefined)) {
            ((Console)c).getData().clear();
        }
    }

    class SuggestionsList extends JPopupMenu {

        public SuggestionsList(JTextComponent c) {
            owner = c;
            setOpaque(true);
            setBackground(Color.WHITE);
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        }

        public void add(Suggestion c) {
            super.add((Component)c);
            if (first == null) {
                first = c;
                last = c;
            }
            last.next = c;
            c.prev = last;
            last = c;
            first.prev = last;
            last.next = first;
        }
        
        public void add(Suggestion c, int pos) {
            Suggestion old = (Suggestion) getComponent(pos);
            super.add((Component)c, pos);
            if (pos == 0) {
                first = c;
            }
            c.prev = old.prev;
            old.prev.next = c;
            old.prev = c;
            c.next = old;
        }

        @Override
        public void removeAll() {
            super.removeAll();
            first = last = null;
        }

        @Override
        public void paintComponent(final Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            int rows = Math.min(5, getComponents().length);
            return new Dimension(owner.getWidth() + 2, rows == 1 ? height + 4 : rows * (height + 1) + 2);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Component getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(Component c) {
            if (selectedItem != null) {
                ((Suggestion)selectedItem).hovered = false;
            }
            selectedItem = c;
            ((Suggestion)c).hovered = true;
            super.setSelected(c);
            super.repaint();
        }

        public Component selectedItem;
        Suggestion first;
        Suggestion last;
        public JTextComponent owner;
        int height = 24;
    };

    public void showSuggestions(JTextComponent c, Vector<String> options) {
        if (options.size() == 0) return;
        if (suggestions == null) {
            suggestions = new SuggestionsList(c);
        } else {
            suggestions.removeAll();
            suggestions.selectedItem = null;
            suggestions.setVisible(false);
        }
        int height = 24;
        for (String s: options) {
            Suggestion item = new Suggestion(s, c);
            suggestions.add(item);
        }
        suggestions.setVisible(true);
        
        suggestions.show(c, -1, c.getHeight());

        suggestions.revalidate();
        suggestions.repaint();

        c.requestFocus();
    }

    private void findCurrentTokenSuggestions(JTextComponent input) {
        String str = input.getText();
        if (str.isEmpty()) {
            if (suggestions != null) {
                suggestions.setVisible(false);
            }
            return;
        }
        int pos = input.getCaretPosition();
        int from = pos;
        int to = pos;
        while (from > 0 && (Character.isLetter(str.charAt(from-1)) || Character.isDigit(str.charAt(from-1)))) {
            from--;
        }
        while (to < str.length() && (Character.isLetter(str.charAt(to)) || Character.isDigit(str.charAt(to)))) {
            to++;
        }
        String token = str.substring(from, to);
        if (token.isEmpty()) {
            if (suggestions != null) {
                suggestions.setVisible(false);
            }
            return;
        }
        //System.out.println("Current token: " + token);
        Vector<String> words = new Vector(Arrays.asList("body", "childNodes", "children", "classList", "className", "console", "document", "getElementById", "getElementsByTagName", "getElementsByClassName", "getElementsByName", "parentNode", "nextSibling", "nextElementSibling", "previousSibling", "previousElementSibling", "length", "push", "pop", "slice", "splice", "shift", "unshift", "substring", "screen", "tagName", "window"));
        Vector<String> list = new Vector<String>();
        for (String word: words) {
            if (word.startsWith(token) && token.length() < word.length()) {
                list.add(word);
            }
        }
        if (list.isEmpty() && suggestions != null) {
            suggestions.setVisible(false);
            input.requestFocus();
        } else {
            showSuggestions(input, list);
        }
    }

    private void updateCurrentToken(JTextComponent input, String token) {
        String str = input.getText();
        if (str.isEmpty()) return;
        int pos = input.getCaretPosition();
        int from = pos;
        int to = pos;
        while (from > 0 && (Character.isLetter(str.charAt(from-1)) || Character.isDigit(str.charAt(from-1)))) {
            from--;
        }
        while (to < str.length() && (Character.isLetter(str.charAt(to)) || Character.isDigit(str.charAt(to)))) {
            to++;
        }
        String text = str.substring(0, from) + token + str.substring(to);
        input.setText(text);
        input.setCaretPosition(from + token.length());
        if (suggestions != null) {
            suggestions.setVisible(false);
        }
        input.requestFocus();
    }

    class Suggestion extends JPanel {
        Suggestion(String text, final JTextComponent owner) {
            super();
            setOpaque(true);
            setBackground(Color.WHITE);
            FlowLayout fl = new FlowLayout();
            fl.setAlignment(FlowLayout.LEADING);
            fl.setVgap(2);
            fl.setHgap(2);
            setLayout(fl);
            label = new JLabel(text);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setFont(new Font("Consolas", Font.PLAIN, 16));
            add(label);
            
            addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {
                    updateCurrentToken(owner, label.getText());
                }

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    if (getParent() instanceof SuggestionsList) {
                        ((SuggestionsList)getParent()).setSelectedItem((Component)e.getSource());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

            });
        }

        private JLabel label;
        private int height = 24;

        @Override
        public Dimension getPreferredSize() {
            if (getParent() != null) {
                return new Dimension(getParent().getWidth(), height);
            }
            return new Dimension(100, height);
        }
        
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public void paintComponent(Graphics g) {
            int index = ((JPopupMenu)getParent()).getSelectionModel().getSelectedIndex();
            if (hovered || index >= 0 && getParent().getComponent(index) == this) {
                setBackground(new Color(228, 235, 238));
            } else {
                setBackground(null);
            }
            super.paintComponent(g);
        }

        public boolean hovered = false;
        Suggestion prev;
        Suggestion next;
    }

    JPanel console;
    JTextArea consoleInput;
    JPopupMenu consoleMenu;
    SuggestionsList suggestions;
    HashMap<String, JSValue> scope;


    class TreeExpandListener implements TreeWillExpandListener, TreeExpansionListener {

        @Override
        public void treeWillExpand(final TreeExpansionEvent e) throws ExpandVetoException {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            if (node.getChildCount() == 0) {
                loadNodeDirectChildren(node, false);
            }
        }

        @Override
        public void treeWillCollapse(final TreeExpansionEvent e) {}

        @Override
        public void treeExpanded(TreeExpansionEvent e) {
            revalidateContentHeight((JTree)e.getSource());
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent e) {
            revalidateContentHeight((JTree)e.getSource());
        }
    }

    private void revalidateContentHeight(final JTree tree) {
        ((JPanel)tree.getParent()).revalidate();
    }

    public class FontMetricsWrapper extends FontMetrics {

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

    public class JSValueWrapper {

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

    private JPanel createObjectTree(JSObject obj) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new JSValueWrapper(null, obj));
        loadNodeDirectChildren(rootNode, false);

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);
        final JTree tree = new JTree(treeModel);
        TreeExpandListener expansionListener = new TreeExpandListener();
        tree.addTreeWillExpandListener(expansionListener);
        tree.addTreeExpansionListener(expansionListener);

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
        tree.addMouseListener(contextMenuListener);

        return contentpane;
    }

    private void processNodeChildren(DefaultMutableTreeNode node, boolean show_prototypes) {
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

    private void loadNodeDirectChildren(DefaultMutableTreeNode node, boolean show_prototypes) {
        JSValue val = ((JSValueWrapper)node.getUserObject()).getValue();
        if (val instanceof JSArray) {
            Vector<JSValue> items = ((JSArray)val).getItems();
            for (int i = 0; i < items.size(); i++) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper("[" + i + "]", items.get(i)));
                node.add(newNode);
                if (items.get(i) instanceof Function || items.get(i).getType().matches("Boolean|Integer|Float|Number|String|null|undefined")) {
                    newNode.setAllowsChildren(false);
                }
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
