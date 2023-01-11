package render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
