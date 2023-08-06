package com.alstarsoft.tinybrowser;

import com.alstarsoft.tinybrowser.bridge.CustomElement;
import com.alstarsoft.tinybrowser.bridge.Document;
import com.alstarsoft.tinybrowser.cssparser.QuerySelector;
import com.alstarsoft.tinybrowser.htmlparser.HTMLParser;
import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import com.alstarsoft.tinybrowser.jsparser.Expression;
import com.alstarsoft.tinybrowser.jsparser.JSParser;
import com.alstarsoft.tinybrowser.mediaplayer.MediaController;
import com.alstarsoft.tinybrowser.mediaplayer.NoMediaPlayerException;
import com.alstarsoft.tinybrowser.network.FormEntry;
import com.alstarsoft.tinybrowser.network.Request;
import com.alstarsoft.tinybrowser.render.Block;
import com.alstarsoft.tinybrowser.render.WebDocument;

/**
 *
 * @author Alex
 */
public class Main {

    public static void detectInstallPath() {
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        String programPath = location.getPath().substring(1).replace('/', File.separatorChar);
        try {
            programPath = URLDecoder.decode(programPath, "utf8");
        } catch (Exception e) {}
        int slashes = 3;
        if (programPath.endsWith(".jar")) {
            slashes--;
        }
        for (int i = 0; i < slashes; i++) {
            int index = programPath.lastIndexOf(File.separatorChar);
            programPath = programPath.substring(0, index);
        }
        programPath += File.separatorChar;
        installPath = programPath;
    }

    public static String getInstallPath() {
        if (installPath == null) detectInstallPath();
        return installPath;
    }

    public static String getVersion() {
        return VERSION;
    }

        public static void testBuilder(String path) {
        Reader reader = new Reader();
        reader.addCustomElement("player", CustomPlayer.class);
        Document document = reader.readDocument(path);
        visualBuilderTest(reader, document);

        //System.out.println();
        //render.Util.compareTrees(root, visualBuilderSyntheticTest());

        //getSyntheticTree();
    }

    public static void visualBuilderTest(Reader reader, Document document) {
        reader.displayDocument(document, "Render Test", 480, 380);
        //documentResizeTest(frame, panel, document, 1000, 400);
    }

    public static void testRequests() {

        try {
            URL url = new URL("http://popov654.pp.ru");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setRequestProperty("User-Agent", "TinyBrowser");
            http.setDoOutput(true);
            Vector<FormEntry> params = new Vector<FormEntry>();
            params.add(new FormEntry("name", "Alex654"));
            params.add(new FormEntry("message", "Hello there!"));
            params.add(new FormEntry("attachment", "[filename=\"C:\\Users\\Alex\\Desktop\\attachment.txt\"]"));
            //byte[] out = Request.prepareBody(http, params, Request.defaultCharset, true);
            String response = Request.makeRequest("http://popov654.pp.ru/files.php", "POST", params, true, true);
            System.out.println(response);
            //prepareBody(HttpURLConnection http, HashMap<String, String> params, String charset, boolean multipart)
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void testTimeout() {
        JSParser jp = new JSParser("function f() { 5 }; setTimeout(f, 1200)");
        System.out.println("function f() { 5 }; setTimeout(f, 1200)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
    }

    public static void testInterval() {
        JSParser jp = new JSParser("var i = 0; function f() { i+=10; if (i >= 50) clearInterval(t); }; var t = setInterval(f, 500)");
        System.out.println("var i = 0; function f() { i+=10; if (i >= 50) clearInterval(t); }; var t = setInterval(f, 500)");
        Expression exp = Expression.create(jp.getHead());
        exp.setSilent(true);
        exp.eval();
        ((com.alstarsoft.tinybrowser.jsparser.Function)Expression.getVar("f", exp)).setSilent(false);
    }

    public static void testPromises() {
//        JSParser jp = new JSParser("function cbk1(str) { \"Promise 1 fulfilled: \" + str; return str } function cbk2(str) { setTimeout(str => { \"Promise 2 fulfilled: \" + str }, 1000); throw \"ERROR\" } function cbk3(str) { \"Promise 3 fulfilled: \" + str; return str } function err(str) { \"An error has occured: \" + str } function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 300) }; (new Promise(f)).then(cbk1).then(cbk2).then(cbk3, err)");
//        System.out.println();
//        System.out.println("function cbk1(str) { \"Promise 1 fulfilled: \" + str; return str }");
//        System.out.println("function cbk2(str) { setTimeout(str => { \"Promise 2 fulfilled: \" + str }, 1000); throw \"ERROR\" }");
//        System.out.println("function cbk3(str) { \"Promise 3 fulfilled: \" + str; return str }");
//        System.out.println("function err(str) { \"An error has occured: \" + str }");
//        System.out.println("function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 300) }");
//        System.out.println("(new Promise(f)).then(cbk1).then(cbk2).then(cbk3, err)");
//        System.out.println();
        JSParser jp = new JSParser("function cbk(str) { setTimeout(function() { \"Promise 1 fulfilled: \" + str; return str }, 500) } function err(str) { setTimeout(function() { \"An error has occured: \" + str }, 500) } function f1(resolve, reject) { setTimeout(function() { resolve(\"OK1\") }, 300) }; function f2(resolve, reject) { setTimeout(function() { resolve(\"OK2\") }, 400) }; Promise.all([new Promise(f1), new Promise(f2)]).then(cbk, err)");
        System.out.println();
        System.out.println("function cbk(str) { setTimeout(function() { \"Promise fulfilled: \" + str; return str }, 500) }");
        System.out.println("function err(str) { setTimeout(function() { \"An error has occured: \" + str }, 500) }");
        System.out.println("function f1(resolve, reject) { setTimeout(function() { resolve(\"OK1\") }, 300) }");
        System.out.println("function f2(resolve, reject) { setTimeout(function() { resolve(\"OK2\") }, 1400) }");
        System.out.println("Promise.all([new Promise(f1), new Promise(f2)]).then(cbk, err)");
        System.out.println();
        Expression exp = Expression.create(jp.getHead());
        //((jsparser.Function)Expression.getVar("f", exp)).setSilent(true);
        //((jsparser.Function)Expression.getVar("cbk2", exp)).setSilent(true);
        exp.eval();
        /*jsparser.Function f = (jsparser.Function)Expression.getVar("f", exp);
        f.setSilent(true);
        ((jsparser.Function)Expression.getVar("cbk2", exp)).setSilent(true);
        jsparser.Promise p = new jsparser.Promise(f);
        p.then((jsparser.Function)Expression.getVar("cbk1", exp))
                .then((jsparser.Function)Expression.getVar("cbk2", exp))
                .then((jsparser.Function)Expression.getVar("cbk3", exp),
                      (jsparser.Function)Expression.getVar("err", exp));*/
    }

    public static void testAwait() {
        JSParser jp = new JSParser("function f() { var x = 3; return await g() } function g() { return \"world\" } f()");
        System.out.println();
        System.out.println("function f() { var x = 3; return await g() } function g() { return \"world\" } f()");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        
        jp = new JSParser("function f() { var x = 3; return \"Hello \" + await g() } async function g() { return new Promise(function(resolve, reject) { resolve(\"world\") }, null) } f()");
        System.out.println();
        System.out.println("function f() { var x = 3; return \"Hello \" + await g() } async function g() { return new Promise(function(resolve, reject) { resolve(\"world\") }, null) } f()");
        exp = Expression.create(jp.getHead());
        exp.eval();

        jp = new JSParser("function f() { var x = 3; return \"Hello \" + await g() } async function g() { return new Promise(function(resolve, reject) { resolve(\"world\") }, null) } await f(); console.log(\"end\")");
        System.out.println();
        System.out.println("function f() { var x = 3; return \"Hello \" + await g() } async function g() { return new Promise(function(resolve, reject) { resolve(\"world\") }, null) } await f(); console.log(\"end\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
    }

    class CustomPlayer extends CustomElement {

        public CustomPlayer(WebDocument document, Node node) {
            super(document, node);
        }

        @Override
        public void initialize() {
            MediaController mc = new MediaController();
            mc.setMediaPlayer(new com.alstarsoft.tinybrowser.mediaplayer.MediaPlayer());
            try {
                String url = "";
                String title = "Song Title";

                if (node.hasAttribute("src")) {
                    url = node.getAttribute("src");
                    if (node.hasAttribute("title")) {
                        title = node.getAttribute("title");
                    } else {
                        String[] p = url.split("/");
                        title = capitalizeString(p[p.length-1].replaceAll("\\.[a-z0-9]+$", "").replaceAll("_", " ").replaceFirst("(\\w)-(\\w)", "$1 - $2"));
                    }
                }

                mc.openSource(url);
                mc.setSongTitle(title);
            } catch (NoMediaPlayerException ex) {
                ex.printStackTrace();
            }
            mc.setBorder(null);
            setComponent(mc);
            width = 230;
            height = 48;
        }

        public String capitalizeString(String string) {
            char[] chars = string.toLowerCase().toCharArray();
            boolean found = false;
            for (int i = 0; i < chars.length; i++) {
                if (!found && Character.isLetter(chars[i])) {
                  chars[i] = Character.toUpperCase(chars[i]);
                  found = true;
                } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                  found = false;
                }
            }
            return String.valueOf(chars);
        }

        public Block createBlock(Node node) {
            return null;
        }
    }

    public static Block visualBuilderSyntheticTest() {
        JFrame frame = new JFrame("Render Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        final WebDocument document = new WebDocument();

        Block root = document.root;
        root.setId("root");

        document.debug = true;

        document.setPreferredSize(new Dimension(460, 240));
        document.width = 460;
        document.height = 240;

        document.panel.setBackground(Color.WHITE);
        document.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        document.setBorderSize(1);

        //document.ready = false;

        root.setBounds(0, 0, document.width, document.height);
        root.setWidth(-1);
        root.height = document.height;
        root.viewport_height = root.height;
        root.orig_height = root.height;
        root.max_height = root.height;
        root.auto_height = false;

        Block paragraph = new Block(document, root, -1, -1, 0, 0, Color.BLACK);
        paragraph.setMargins(0, 0, 12, 0);
        paragraph.addText("This is a paragraph");
        root.addElement(paragraph);

        root.getChildren().get(0).setBackgroundColor(Color.CYAN);

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        document.ready = true;

        panel.add(document);
        frame.add(panel);

        //panel.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        panel.setPreferredSize(new Dimension(document.width + 18, document.height + 18));

        frame.pack();
        //frame.setLocationRelativeTo(null);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                document.resized();
            }
        });

        frame.setVisible(true);

        return root;
    }

    private static Block getSyntheticTree() {
        final WebDocument document = new WebDocument();

        Block root = document.root;
        root.setId("root");

        document.setPreferredSize(new Dimension(460, 240));
        document.width = 460;
        document.height = 240;

        //document.ready = false;

        root.setBounds(1, 1, document.width, document.height);
        root.setWidth(-1);
        root.height = document.height-2;
        root.viewport_height = root.height;
        root.orig_height = root.height;
        root.max_height = root.height;
        root.auto_height = false;

        Block paragraph = new Block(document, root, -1, -1, 0, 0, Color.BLACK);
        paragraph.setMargins(0, 0, 12, 0);
        paragraph.addText("This is a paragraph");
        root.addElement(paragraph);

        root.getChildren().get(0).setBackgroundColor(Color.CYAN);

        return root;
    }

    private static void documentResizeTest(final JFrame frame, final JPanel panel, final WebDocument document, int timeout, final int width) {
        Timer t = new Timer(timeout, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                document.setWidth(width);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.setPreferredSize(new Dimension(document.width + 18, document.height + 18));
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                    }
                });
            }
        });
        t.setRepeats(false);
        t.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //testBuilder("html" + File.separator + "table.htm");
        testAwait();
    }

    private static String installPath;

    private final static String VERSION  ="0.9.6";

}
