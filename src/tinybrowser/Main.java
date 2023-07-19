package tinybrowser;

import bridge.CustomElement;
import bridge.Document;
import cssparser.QuerySelector;
import htmlparser.HTMLParser;
import htmlparser.Node;
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
import jsparser.Expression;
import jsparser.JSParser;
import mediaplayer.MediaController;
import mediaplayer.NoMediaPlayerException;
import network.FormEntry;
import network.Request;
import render.Block;
import render.WebDocument;

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

    class CustomPlayer extends CustomElement {

        public CustomPlayer(WebDocument document, Node node) {
            super(document, node);
        }

        @Override
        public void initialize() {
            MediaController mc = new MediaController();
            mc.setMediaPlayer(new mediaplayer.MediaPlayer());
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
        testBuilder("html" + File.separator + "table.htm");
    }

    private static String installPath;

    private final static String VERSION  ="0.9.6";

}
