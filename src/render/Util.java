package render;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Timer;

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

    public static byte[] read(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
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

    public static String installPath;
}
