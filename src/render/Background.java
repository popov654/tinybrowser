package render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.imageio.ImageIO;
import render.Block.CssLength;

/**
 *
 * @author Alex
 */
public class Background {

    public void setBackgroundImage(String path) {
        if (path == null || path.isEmpty()) {
            bgImage = null;
            imgSrc = "";
            return;
        }
        try {
            File f;
            if (path.equals(imgSrc)) return;
            imgSrc = path;
            if (path.startsWith("http")) {
                bgImage = ImageIO.read(new URL(path));
                String[] str = path.split("/");
                f = File.createTempFile("tmp_", str[str.length-1]);
                ImageIO.write(bgImage, "png", f);
            } else {
                f = new File(path);
                bgImage = ImageIO.read(f);
            }
        } catch (IOException ex) {}
    }

    public void setBackgroundColor(Color color) {
        bgcolor = color;
    }

    public void setLinearGradient(Vector<Color> colors, Vector<Float> positions, int angle) {
        int n = Math.max(colors.size(), positions.size());
        Gradient.ColorStop[] cs = new Gradient.ColorStop[n];
        Color c = new Color(0, 0, 0, 0);
        float p = 0f;
        for (int i = 0; i < n; i++) {
            if (i < colors.size()) {
                c = colors.get(i);
            }
            if (i < positions.size()) {
                p = positions.get(i);
            }
            cs[i] = new Gradient.ColorStop(c, p);
        }
        gradient = new Gradient(angle, cs);
    }

    public void setLinearGradientWithUnits(Block b, Vector<Color> colors, Vector<String> positions, double angle) {
        int n = Math.max(colors.size(), positions.size());
        Gradient.ColorStop[] cs = new Gradient.ColorStop[n];
        Color c = new Color(0, 0, 0, 0);
        for (int i = 0; i < n; i++) {
            if (i < colors.size()) {
                c = colors.get(i);
            }
            CssLength p = null;
            if (i < positions.size()) {
                p = b.parseValueString(positions.get(i));
            }
            cs[i] = new Gradient.ColorStop(c, (float) p.value, p.unit);
        }
        gradient = new Gradient(angle, cs);
    }

    public void setRadialGradient(int[] center, double[] radius, Vector<Color> colors, Vector<Float> positions) {
        int n = Math.max(colors.size(), positions.size());
        Gradient.ColorStop[] cs = new Gradient.ColorStop[n];
        Color c = new Color(0, 0, 0, 0);
        float p = 0f;
        for (int i = 0; i < n; i++) {
            if (i < colors.size()) {
                c = colors.get(i);
            }
            if (i < positions.size()) {
                p = positions.get(i);
            }
            cs[i] = new Gradient.ColorStop(c, p);
        }
        gradient = new Gradient(0, cs);
        gradient.setType(Gradient.RADIAL);
        gradient.setRadialParams(center[0], center[1], radius[0], radius[1]);
    }

    public void setRadialGradientWithUnits(Block b, int[] center, double[] size, Vector<Color> colors, Vector<String> positions) {
        int n = Math.max(colors.size(), positions.size());
        Gradient.ColorStop[] cs = new Gradient.ColorStop[n];
        Color c = new Color(0, 0, 0, 0);
        for (int i = 0; i < n; i++) {
            if (i < colors.size()) {
                c = colors.get(i);
            }
            CssLength p = null;
            if (i < positions.size()) {
                p = b.parseValueString(positions.get(i));
            }
            cs[i] = new Gradient.ColorStop(c, (float) p.value, p.unit);
        }
        gradient = new Gradient(0, cs);
        gradient.setType(Gradient.RADIAL);
        gradient.setRadialParams(center[0], center[1], size[0], size[1]);
    }

    @Override
    public Background clone() {
        Background background = new Background();
        background.gradient = gradient;
        background.bgcolor = bgcolor;
        background.bgImage = bgImage;
        background.imgSrc = imgSrc;

        background.bg_alpha = bg_alpha;
        background.background_repeat = background_repeat;
        background.background_size_x = background_size_x;
        background.background_size_y = background_size_y;
        background.background_size_x_auto = background_size_x_auto;
        background.background_size_y_auto = background_size_y_auto;
        background.background_pos_x = background_pos_x;
        background.background_pos_x = background_pos_x;

        return background;
    }
    

    public Gradient gradient;
    public Color bgcolor;
    public BufferedImage bgImage;
    public String imgSrc;


    public float bg_alpha = 1.0f;
    public int background_repeat = 0;
    public int background_size_x = -1;
    public int background_size_y = -1;
    public boolean background_size_x_auto = true;
    public boolean background_size_y_auto = true;
    public int background_pos_x = 0;
    public int background_pos_y = 0;

}
