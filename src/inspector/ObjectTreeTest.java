package inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
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
import jsparser.Function;
import jsparser.HTMLElement;
import jsparser.JSArray;
import jsparser.JSInt;
import jsparser.JSObject;
import jsparser.JSString;
import jsparser.JSValue;
import jsparser.Null;

/**
 *
 * @author Alex
 */
public class ObjectTreeTest {

    private static JSObject prepareTree() {
        JSObject obj = new JSObject();
        obj.set("x", new JSInt(1));
        obj.set("y", new JSInt(1));

        JSArray users = new JSArray();
        obj.set("users", users);

        JSObject user = new JSObject();
        user.set("login", new JSString("Alex654"));
        user.set("password", new JSString("123456"));

        JSObject info = new JSObject();
        info.set("city", new JSString("Moscow"));
        info.set("job", new JSString("Developer"));
        info.set("firstName", new JSString("Alex"));
        info.set("lastName", new JSString("Popov"));
        user.set("bio", info);
        
        users.push(user);

        JSObject user2 = new JSObject();
        user2.set("login", new JSString("admin"));
        user2.set("password", new JSString("test"));
        user2.set("bio", Null.getInstance());

        users.push(user2);

        return obj;
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
            String result = label;
            if (val instanceof Function) {
                result += ": { Function }";
            }
            else if (val instanceof JSArray) {
                result += ": Array[]";
            }
            else if (val instanceof HTMLElement) {
                result += ": HTMLElement[" + ((HTMLElement)val).node.tagName + "]";
            }
            else if (val instanceof JSObject && !val.getType().matches("String|Integer|Float|Number|Boolean|null|undefined")) {
                result += ": Object";
            }
            else result += ": " + val.toString();

            return result;
        }

        private String label;
        private JSValue val;
    }

    public static void processChildren(DefaultMutableTreeNode node, boolean show_prototypes) {
        JSValue val = ((JSValueWrapper)node.getUserObject()).getValue();
        if (val instanceof JSArray) {
            Vector<JSValue> items = ((JSArray)val).getItems();
            for (int i = 0; i < items.size(); i++) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper("[" + i + "]", items.get(i)));
                node.add(newNode);
                processChildren(newNode, show_prototypes);
            }
        } else if (!(val instanceof Function) && !val.getType().matches("Boolean|Integer|Float|Number|String|null|undefined") && val instanceof JSObject) {
            HashMap<String, JSValue> props = ((JSObject)val).getProperties();
            Set<String> keys = props.keySet();
            for (String key: keys) {
                if (!show_prototypes && key.equals("__proto__")) continue;
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new JSValueWrapper(key, props.get(key)));
                node.add(newNode);
                if (!key.equals("__proto__")) {
                    processChildren(newNode, show_prototypes);
                } else {
                    newNode.setAllowsChildren(false);
                }
            }
        } else {
            node.setAllowsChildren(false);
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        final JSValue root = prepareTree();
        if (root == null) return;

        final JFrame frame = new JFrame("Object Inspector");
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        frame.setContentPane(cp);
        cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new JSValueWrapper("obj", root));
        processChildren(rootNode, false);

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode, true);
        final JTree tree = new JTree(treeModel);

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
