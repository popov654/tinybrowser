package render;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.Timer;
import network.Blob;
import network.Request;

/**
 *
 * @author Alex
 */
public class Util {

    public static HashMap<String, String> getFields(Block block, List<String> exclude) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (Field field : block.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(block);
                if (value != null && !exclude.contains(field.getName())) {
                    //System.out.println(field.getName() + "=" + value);
                    String str = value.toString();
                    if (value != null && value.getClass().isArray()) {
                        if (value instanceof int[]) {
                            int[] a = (int[]) value;
                            str = "";
                            for (int i = 0; i < a.length; i++) {
                                if (i > 0) str += ", ";
                                str += a[i];
                            }
                            str = "[" + str + "]";
                        }
                        if (value instanceof Color[]) {
                            Color[] a = (Color[]) value;
                            str = "";
                            for (int i = 0; i < a.length; i++) {
                                if (i > 0) str += ", ";
                                Color col = (Color) a[i];
                                str = "color[" + col.getRed() + ", " + col.getGreen() + ", " + col.getBlue() + ", " + col.getAlpha() + "]";
                            }
                            str = "[" + str + "]";
                        }
                        //System.out.println(value.getClass().getComponentType());
                    }
                    if (value instanceof Color) {
                        Color col = (Color) value;
                        str = "color[" + col.getRed() + ", " + col.getGreen() + ", " + col.getBlue() + ", " + col.getAlpha() + "]";
                    }
                    if (value instanceof BufferedImage && value != null) {
                        BufferedImage img = (BufferedImage) value;
                        str = "BufferedImage[" + img.getWidth() + "x" + img.getHeight() + "]";
                    }
                    result.put(field.getName(), str);
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static void compareFieldsets(HashMap<String, String> fields1, HashMap<String, String> fields2) {
        String result = "";
        Set keys = fields1.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (!fields1.get(key).equals(fields2.get(key))) {
                result += key + ": " + fields1.get(key) + " <-> " + fields2.get(key) + "\n";
            }
        }
        System.out.println(result.length() > 0 ? result : "Objects are equal");
    }

    public static void compareTrees(final Block b1, final Block b2) {
        Timer t = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<String> exclude = new ArrayList(Arrays.asList("lm", "parentListener", "border", "document", "layouter"));

                HashMap<String, String> fields1 = render.Util.getFields(b1, exclude);
                HashMap<String, String> fields2 = render.Util.getFields(b2, exclude);

                render.Util.compareFieldsets(fields1, fields2);

            }

        });
        t.setRepeats(false);
        t.start();
    }

    public static void cacheFile(String src, String cached_name) {
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

    public static void deleteFile(String path) {
        File f = new File(path);
        if (f.exists()) f.delete();
    }

    public static byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        int total = 0;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while (read != -1) {
                read = ios.read(buffer);
                if (read == -1) break;
                total += read;
                ous.write(buffer, 0, read);
                if (total >= 10000000) {
                    break;
                }
            }
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {}

            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {}
        }
        return ous.toByteArray();
    }

    public static void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (os.indexOf("mac") >= 0) rt.exec("open " + url);
            else {
                String[] browsers = { "google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                                 "netscape", "opera", "links", "lynx" };
                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++)
                    if(i == 0)
                        cmd.append(String.format(    "%s \"%s\"", browsers[i], url));
                    else
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));

                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }
        } catch (IOException ex) {}
    }

    public static void downloadFile(final WebDocument document, final String url) {
        if (url.startsWith("blob://")) {
            String guid = url.substring(7);
            URL document_url = null;
            try {
                if (document != null) {
                    String docURL = document.baseUrl;
                    if (!docURL.matches("^(https?|ftp|file)://.*")) {
                        docURL = "file:///" + docURL;
                    }
                    document_url = new URL(docURL);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
            Blob blob = Request.getBlob(document_url, guid);
            if (blob != null) {
                downloadFile(document, blob, guid);
            }
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = Request.makeDownloadRequest(url);
                saveFile(document, file, url);
            }
        });
        t.start();
    }

    public static void downloadFile(final WebDocument document, final Blob blob, final String guid) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (blob == null) return;
                    File file = File.createTempFile("tmp_", "");
                    FileOutputStream fos = new FileOutputStream(file);
                    while (blob.hasNextChunk()) {
                        fos.write(blob.nextChunk());
                    }
                    fos.close();
                    String fileName = guid == null ? file.getName() : guid;
                    saveFile(document, file, fileName);
                } catch (IOException ex) {
                    Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    public static void downloadFile(final WebDocument document, final Blob blob) {
        downloadFile(document, blob, null);
    }

    public static void saveFile(WebDocument document, File file, String url) {
        File dir = new File(System.getProperty("user.home"));
        boolean ask = !Util.getParameter("download_ask").matches("false|0");
        String[] parts = url.split("/");
        if (ask) {
            String path = Util.getParameter("download_dir");
            if (path != null) {
                path = path.replaceAll("%(USER_?HOME|USER_?PROFILE)%", System.getProperty("user.home").replace("\\", "\\\\"));
                dir = new File(path);
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(dir);
            //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Save File As");
            fileChooser.setApproveButtonText("Save");

            File dest = new File(path + File.separatorChar + parts[parts.length-1]);
            fileChooser.setSelectedFile(dest);

            int result = fileChooser.showSaveDialog(document);
            if (result == JFileChooser.APPROVE_OPTION) {
                dir = fileChooser.getSelectedFile();
                if (!Util.getParameter("download_remember_last_dir").matches("(false|0)")) {
                    Util.setParameter("download_dir", path);
                }
            } else {
                file.delete();
                return;
            }
        } else {
            String path = Util.getParameter("download_dir");
            dir = new File(path + File.separatorChar + parts[parts.length-1]);
        }
        try {
            File copy = new File(dir.getAbsolutePath());
            if (copy.exists() && copy.length() > 0) {
                int input = javax.swing.JOptionPane.showConfirmDialog(document, "File already exists. Do you want to overwrite it?", "Warning", javax.swing.JOptionPane.YES_NO_OPTION);
                if (input == javax.swing.JOptionPane.NO_OPTION) {
                    int n = 1;
                    String name = copy.getName().substring(0, copy.getName().lastIndexOf("."));
                    String ext = copy.getName().substring(copy.getName().lastIndexOf("."));
                    String filename = name + " (" + n + ")" + (!ext.isEmpty() ? ext : "");
                    copy = new File(dir.getParent() + File.separatorChar + filename);
                    while (copy.exists() && copy.length() > 0) {
                        n++;
                        filename = name + " (" + n + ")" + (!ext.isEmpty() ? ext : "");
                        copy = new File(dir.getParent() + File.separatorChar + filename);
                    }
                }
            }
            long size = file.length();

            FileChannel dest = (new RandomAccessFile(copy.getAbsolutePath(), "rw")).getChannel();
            dest.truncate(0);
            FileChannel src = (new RandomAccessFile(file.getAbsolutePath(), "r")).getChannel();
            dest.transferFrom(src, 0, src.size());
            dest.close();
            src.close();
            file.delete();

            String[] p = url.split("/");
            System.out.println(String.format("File %s (%d bytes) was saved to %s", p[p.length-1], size, copy.getAbsolutePath()));
        } catch (IOException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getInstallPath() {
        if (installPath == null) {
            URL location = Util.class.getProtectionDomain().getCodeSource().getLocation();
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
        return installPath;
    }

    private static void readSettings() {
        getInstallPath();
        settings = new LinkedHashMap<String, String>();
        try {
            FileReader fr = new FileReader(installPath + "settings");
            String s = "";
            int ch;
            while ((ch = fr.read()) != -1) {
                s += (char) ch;
            }
            Scanner sc = new Scanner(s);
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split("=");
                if (p.length > 1) {
                    settings.put(p[0].trim(), p[1].trim());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeSettings() {
        try {
            FileWriter fw = new FileWriter(installPath + "settings");
            Set<String> keys = settings.keySet();
            for (String key: keys) {
                fw.write(key + "=" + settings.get(key) + "\n");
            }
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getParameter(String key) {
        if (settings == null) {
            readSettings();
        }
        return settings.get(key);
    }

    public static void setParameter(String key, String value) {
        settings.put(key, value);
        writeSettings();
    }

    public static String installPath;
    private static HashMap<String, String> settings;
}
