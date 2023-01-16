package service;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import render.Util;

/**
 *
 * @author Alex
 */
public class FontManager {

   public static void registerFont(String path, String name) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            String[] s = path.split("/");
            Util.cacheFile(path.replaceAll("/", File.separator), s[s.length-1]);
            path = "cache" + File.separatorChar + s[s.length-1];
        }
        path = path.replace("/", File.separator);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(path));
            ge.registerFont(font);
        } catch (FontFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (font != null) {
            if (name == null) name = font.getFontName();
            fontMap.put(name, font.getFontName());
        }
    }

    public static HashMap<String, String> fontMap = new HashMap<String, String>();
}
