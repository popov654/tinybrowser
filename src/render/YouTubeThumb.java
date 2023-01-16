package render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.SwingUtilities;
import network.Request;
import service.WebpConverter;

/**
 *
 * @author Alex
 */
public class YouTubeThumb extends Block {
    
    public YouTubeThumb(WebDocument document, String url, int w, int h) {
        super(document);

        String[] p = url.split("/");
        href = "https://www.youtube.com/watch?v=" + p[p.length-1];

        viewport_width = width = w;
        viewport_height = height = h;

        auto_width = false;
        auto_height = false;

        setLayout(null);

        setVideoPoster(url);
        
        panel = new JPanel(); /* {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                if (image != null) {
                    BufferedImageOp resampler = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS);
                    BufferedImage output = resampler.filter(image, null);
                    g2d.drawImage(output, _x_, _y_, null);
                    super.paintComponent(g);
                }
            }
        };*/

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

        //DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        //String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        //Document doc = impl.createDocument(svgNS, "svg", null);

        StringReader reader = new StringReader("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"100%\" version=\"1.1\" viewBox=\"0 0 68 48\" width=\"100%\"><path class=\"ytp-large-play-button-bg\" stroke=\"#e31518\" stroke-width=\"1.8\" d=\"M66.52,7.74c-0.78-2.93-2.49-5.41-5.42-6.19C55.79,.13,34,0,34,0S12.21,.13,6.9,1.55 C3.97,2.33,2.27,4.81,1.48,7.74C0.06,13.05,0,24,0,24s0.06,10.95,1.48,16.26c0.78,2.93,2.49,5.41,5.42,6.19 C12.21,47.87,34,48,34,48s21.79-0.13,27.1-1.55c2.93-0.78,4.64-3.26,5.42-6.19C67.94,34.95,68,24,68,24S67.94,13.05,66.52,7.74z\" fill=\"#f00\"></path><path d=\"M 45,24 27,14 27,34\" fill=\"#fff\"></path></svg>");
        String uri = "file://test.svg";
        SVGDocument doc = null;
        try {
            doc = f.createSVGDocument(uri, reader);
            //System.out.println(doc.getDocumentElement().getChildNodes().getLength());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        svgCanvas = new JSVGCanvas(null, false, false) {
            @Override
            public void paintComponent(Graphics g) {
                Block clip = parent;
                while (clip.overflow != Block.Overflow.SCROLL) {
                    clip = clip.parent;
                }
                g.setClip(new Rectangle(clip._x_ - _x_ + scroll_x, clip._y_ - _y_ + scroll_y, clip.viewport_width, clip.viewport_height));
                super.paintComponent(g);
            }
        };

        if (doc != null) {
            svgCanvas.setDocument(doc);
        }

        svgCanvas.setPreferredSize(new Dimension(60, 40));
        svgCanvas.setMaximumSize(new Dimension(60, 40));
        svgCanvas.setBackground(new Color(0, 0, 0, 0));

        panel.setOpaque(false);
        panel.setBackground(null);
        panel.setLayout(null);

        //panel.setBorder(BorderFactory.createEmptyBorder(b1, b2, b1, b2));

        panel.setPreferredSize(new Dimension(w, h));
        panel.add(svgCanvas);

        add(panel);
    }

    private void setVideoPoster(String url) {
        //loadInfo(url);
        String[] p = url.split("/");
        imageSrc = "https://i.ytimg.com/vi_webp/" + p[p.length-1] + "/maxresdefault.webp";

        boolean supportsWebp = true;
        try {
            image = ImageIO.read(new URL(imageSrc));
        } catch (IOException ex) {
            supportsWebp = false;
        }

        if (image == null) supportsWebp = false;

        if (!supportsWebp) {
            File f = new File("cache");
            if (!f.exists()) {
                f.mkdir();
            }
            cacheFile(imageSrc, "ytimg.webp");
            String in = Util.getInstallPath() + File.separatorChar + "cache/ytimg.webp";
            String out = Util.getInstallPath() + File.separatorChar + "cache/ytimg.png";
            WebpConverter converter = new WebpConverter(in, out);
            converter.start();
            try {
                converter.join();
            } catch (InterruptedException ex) {}
            imageSrc = "cache/ytimg.png";
        }

        setBackgroundImage(imageSrc);
        setBackgroundCover();

        deleteFile("cache/ytimg.webp");
        deleteFile("cache/ytimg.png");
    }

    private void cacheFile(String src, String cached_name) {
        InputStream in = null;
        FileOutputStream fileOutputStream = null;
        try {
            in = new URL(src).openStream();
            fileOutputStream = new FileOutputStream("cache/" + cached_name);
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            //Files.copy(in, Paths.get("cache/" + cached_name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
                fileOutputStream.close();
            } catch (IOException ex) {}
        }
    }

    private void deleteFile(String path) {
        File f = new File(path);
        if (f.exists()) f.delete();
    }

    @Override
    public void draw(Graphics g) {
        if (svgCanvas != null) {
            int icon_width = (int) (width * 0.18);
            int icon_height = (int) (icon_width / 17 * 12);

            int xpos = (int) ((width - icon_width) / 2);
            int ypos = (int) ((height - icon_height) / 2);
            svgCanvas.setBounds(xpos, ypos, icon_width, icon_height);
        }
        panel.setBounds(_x_ - parent.scroll_x, _y_ - parent.scroll_y, width, height);
        super.draw(g);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = ((JFrame)SwingUtilities.getWindowAncestor(panel));
                if (frame != null) frame.getContentPane().repaint();
            }
        });
    }

    @Override
    public void performLayout(boolean no_rec, boolean no_viewport_reset) {
        if (svgCanvas != null) {
            int icon_width = (int) (width * 0.18);
            int icon_height = (int) (icon_width / 17 * 12);

            int xpos = (int) ((width - icon_width) / 2);
            int ypos = (int) ((height - icon_height) / 2);

            svgCanvas.setBounds(xpos, ypos, icon_width, icon_height);
            svgCanvas.setMaximumSize(new Dimension(icon_width, icon_height));
        }
        panel.setBounds(_x_ - parent.scroll_x, _y_ - parent.scroll_y, width, height);
    }

    private void loadInfo(String url_string) {
        String[] p = url_string.split("/");
        href = "https://youtube.com/watch?v=" + p[p.length-1];
        String response = "";

        try {
            URL url = new URL(url_string);
            URLConnection con = url.openConnection();
            HttpsURLConnection http = (HttpsURLConnection) con;
            http.setRequestMethod("GET");
            http.setRequestProperty("User-Agent", "XM Desktop App");
            http.setDoOutput(true);

            byte[] out = new byte[0];

            http.connect();

            InputStream is = http.getInputStream();
            response = new String(Request.getBytes(is, 10000000), "UTF-8");

        } catch (MalformedURLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println(response);
        String token = "https://i.ytimg.com/";

        int index = response.indexOf(token);

        if (index > 0) {
            int pos = index + 1;
            while (pos < response.length() && response.charAt(pos) != '\\') pos++;
            imageSrc = response.substring(index, pos);
        }
        imageSrc = imageSrc.replace("vi/", "vi_webp/").replace("default.jpg", "maxresdefault.webp");
        System.out.println(imageSrc);
    }

    public static void main(String[] args) {
        try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("YouTube video test");
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEmptyBorder(9, 10, 5, 10));

        final WebDocument document = new WebDocument();

        document.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        document.borderSize = 1;
        document.borderColor = Color.black;
        document.panel.setBackground(Color.WHITE);

        document.width = 478;
        document.height = 358;
        document.setPreferredSize(new Dimension(document.width+2, document.height+2));

        Block root = document.root;

        root.setBounds(1, 1, document.width, document.height);

        root.setWidth(-1);
        root.setHeight(document.height);

        root.height = document.height;
        root.viewport_height = root.height;
        root.orig_height = (int) (root.height / root.ratio);
        root.max_height = root.height;

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(document);

        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));
        bp.setBorder(BorderFactory.createEmptyBorder(12, 3, 3, 3));

        JButton btn = new JButton("Close");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //btn.setBounds((bp.getWidth() - btn.getPreferredSize().width) / 2, (bp.getHeight() - btn.getPreferredSize().height)-10, btn.getPreferredSize().width, btn.getPreferredSize().height);
        bp.add(Box.createHorizontalGlue());
        bp.add(btn);
        bp.add(Box.createHorizontalGlue());

        panel.add(bp);

        document.ready = false;

        Block b = new YouTubeThumb(document, "https://www.youtube.com/embed/-gCxI3G_GkY", 380, 230);
        b.setBorderRadius(10);
        b.setAutoXMargin();
        b.setAutoYMargin();
        document.root.addElement(b);
        document.root.addMouseListeners();

        document.ready = true;

        document.root.performLayout();
        document.root.forceRepaint();

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {}

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                document.resized();
            }
        });

        frame.setVisible(true);
    }

    String imageSrc;

    BufferedImage image;
    BufferedImage icon;

    JPanel panel;
    JSVGCanvas svgCanvas;
}
