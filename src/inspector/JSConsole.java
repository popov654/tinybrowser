package inspector;

import bridge.Builder;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import jsparser.Console;
import jsparser.Expression;
import jsparser.HTMLElement;
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

        final JPanel console = new JPanel();
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

                    addEntry(console, exp.getValue().toString());

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
        if (height > console.getHeight()) {
            if (allowSelection) height = (line_height + 3) * lines.size();
            console.setPreferredSize(new Dimension(console.getWidth(), height));
            console.getParent().validate();
            //console.getParent().setPreferredSize(new Dimension(console.getWidth(), height));
        }
    }

    public static class FontMetricsWrapper extends FontMetrics {

        private final FontMetrics target;

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

}
