package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.border.Border;

/**
 *
 * @author Alex
 */

public class RoundedBorder implements Border {
    
    RoundedBorder(Block b, int[] width, int radius) {
        this.block = b;
        this.radius = radius;
        if (b.arc[0] == 0.0f && b.arc[1] == 0.0f&& b.arc[2] == 0.0f&& b.arc[3] == 0.0f) {
            this.radius = 0;
        }
        this.w = width;
        this.col = new Color[4];
        for (int i = 0; i < 4; i++) {
            this.col[i] = Color.MAGENTA;
        }
    }

    RoundedBorder(Block b, int width[], int radius, Color color) {
        this.block = b;
        this.w = width;
        this.radius = radius;
        this.col = new Color[4];
        for (int i = 0; i < 4; i++) {
            this.col[i] = color;
        }
    }

    RoundedBorder(Block b, int width[], int radius, Color color, int type) {
        this(b, width, radius, color);
        for (int i = 0; i < 4; i++) {
            this.type[i] = type;
        }
    }

    RoundedBorder(Block b, int width[], int radius, Color color, int[] type) {
        this(b, width, radius, color);
        for (int i = 0; i < 4; i++) {
            this.type[i] = type[i];
        }
    }

    RoundedBorder(Block b, int radius, Color color) {
        this(b, null, radius, color);
        for (int i = 0; i < 4; i++) {
            w[i] = 0;
        }
    }

    RoundedBorder(Block b, int[] width, int radius, Color[] color) {
        this.block = b;
        this.w = width;
        this.radius = radius;
        this.col = color;
    }

    RoundedBorder(Block b, int width, int radius, Color[] color) {
        this(b, null, radius, color);
        this.w = new int[4];
        for (int i = 0; i < 4; i++) {
            w[i] = width;
        }
    }

    RoundedBorder(Block b, int width, int radius, Color[] color, int[] type) {
        this(b, width, radius, color);
        for (int i = 0; i < 4; i++) {
            this.type[i] = type[i];
        }
    }

    RoundedBorder(Block b, int width[], int radius, Color[] color, int[] type) {
        this(b, width, radius, color);
        for (int i = 0; i < 4; i++) {
            this.type[i] = type[i];
        }
    }

    RoundedBorder(Block b, int radius, Color[] color) {
        this(b, null, radius, color);
        this.w = new int[4];
        for (int i = 0; i < 4; i++) {
            w[i] = 0;
        }
    }


    public Insets getBorderInsets(Component c) {
        if (radius > 0) return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
        else return new Insets(0, 0, 0, 0);
    }


    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        width = block.viewport_width > 0 ? block.viewport_width + (block.scrollbar_y != null ? block.scrollbar_y.getPreferredSize().width : 0) : block.width;
        height = block.viewport_height > 0 ? block.viewport_height + (block.scrollbar_x != null ? block.scrollbar_x.getPreferredSize().height : 0): block.height;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean flag = false;
        boolean flag2 = false;
        int last_w = w[0];
        Color last_c = col[0];
        for (int i = 0; i < 4; i++) {
            if (w[i] > 0) {
                flag = true;
            }
            if (w[i] != last_w || !col[i].equals(last_c)) {
                flag2 = true;
            }
        }
        if (!flag) return;

        if (!flag2) {
            paintBorder2(c, g, x, y, width, height);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            return;
        }

        if (flag && flag2 && (type[0] != SOLID || type[1] != SOLID || type[2] != SOLID || type[3] != SOLID)) {
            paintBorder3(c, g, x, y, width, height);
            return;
        }

        int r = (int)Math.round(radius / Math.sqrt(2));

        int w0 = Math.max(w[0], r);
        int w1 = Math.max(w[1], r);
        int w2 = Math.max(w[2], r);
        int w3 = Math.max(w[3], r);

        int[][][] d = new int[4][2][4];
        int[] arrayX1 = {x, x+w3, x+width-w1, x+width};
        int[] arrayY1 = {y, y+w0, y+w0, y};
        d[0][0] = arrayX1;
        d[0][1] = arrayY1;
        int[] arrayX2 = {x+width, x+width-w1, x+width-w1, x+width};
        int[] arrayY2 = {y, y+w0, y+height-w2, y+height};
        d[1][0] = arrayX2;
        d[1][1] = arrayY2;
        int[] arrayX3 = {x+width, x+width-w1, x+w3, x};
        int[] arrayY3 = {y+height, y+height-w2, y+height-w2, y+height};
        d[2][0] = arrayX3;
        d[2][1] = arrayY3;
        int[] arrayX4 = {x, x+w3, x+w3, x};
        int[] arrayY4 = {y+height, y+height-w2, y+w0, y};
        d[3][0] = arrayX4;
        d[3][1] = arrayY4;

        Color color, color2, color3;

        for (int j = 0; j < 4; j++) {
            Polygon poly = new Polygon(d[j][0], d[j][1], 4);
            g.setClip(poly);
            color = col[j];
            g.setColor(color);
            color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 88);
            color3 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 58);
            //RoundedRect rect = new RoundedRect(x, y, width-1, height-1, block.arc[0] / 2, block.arc[1] / 2, block.arc[2] / 2, block.arc[3] / 2);
            //((Graphics2D)g).fill(rect);
            RoundedRect rect = null;
            for (int i = 0; i < w[j]; i++) {
                if (block.arc[0] != 0 || block.arc[1] != 0 || block.arc[2] != 0 || block.arc[3] != 0) {

                    //g.fillRoundRect(0, 0, width, height, radius, radius);

                    if (i == w[j]-1) g.setColor(color2);
                    rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2, block.arc[1] / 2, block.arc[2] / 2, block.arc[3] / 2);
                    ((Graphics2D)g).draw(rect);

                    if (i > 0) {
                        if (i < w[j]-1) {
                            g.setColor(color2);
                        } else {
                            g.setColor(color3);
                        }
                        if (i > 1) {
                            g.setColor(color);
                            rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 1, block.arc[1] / 2 - 1, block.arc[2] / 2 - 1, block.arc[3] / 2 - 1);
                            ((Graphics2D)g).fill(rect);
                            break;
                        } else {
                            g.setColor(color3);
                            rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 1, block.arc[1] / 2 - 1, block.arc[2] / 2 - 1, block.arc[3] / 2 - 1);
                            ((Graphics2D)g).draw(rect);
                            rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 2, block.arc[1] / 2 - 2, block.arc[2] / 2 - 2, block.arc[3] / 2 - 2);
                            ((Graphics2D)g).draw(rect);
                            g.setColor(color);
                        }
                    } else {
                        g.setColor(color3);
                        rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 1, block.arc[1] / 2 - 1, block.arc[2] / 2 - 1, block.arc[3] / 2 - 1);
                        ((Graphics2D)g).draw(rect);
                        rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 2, block.arc[1] / 2 - 2, block.arc[2] / 2 - 2, block.arc[3] / 2 - 2);
                        ((Graphics2D)g).draw(rect);
                        rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 3, block.arc[1] / 2 - 3, block.arc[2] / 2 - 3, block.arc[3] / 2 - 3);
                        ((Graphics2D)g).draw(rect);
                        g.setColor(color);
                    }

                    
                } else {
                    g.fillRect(x, y, width, height);
                }
            }
            g.setClip(null);

            if (block.arc[0] != 0 || block.arc[1] != 0 || block.arc[2] != 0 || block.arc[3] != 0) {

                if (w[0] >= 5) {
                    g.setColor(col[0]);

                    g.fillRect((int)(block.arc[0]/2)-2, 1, 2, 1);
                    g.fillRect(w[3]-3, 2, (int)(w[3]*1.2), w[0]-4);
                    g.fillRect(w[3]-2, 3, (int)(w[3]*1.2), w[0]-4);

                    g.fillRect(width - (int)(block.arc[1]/2), 1, 2, 1);
                    g.fillRect(width-w[1]*2, 2, (int)(w[1]*1.2), w[0]-5);
                    g.fillRect(width-w[1]*2-1, 3, (int)(w[1]*1.2), w[0]-5);
                }
                if (w[2] >= 5) {
                    g.setColor(col[2]);

                    g.fillRect((int)(block.arc[3]/2)-2, height-2, 2, 1);
                    g.fillRect(w[3]-2, height-2-(w[2]-4), (int)(w[3]*1.2), w[2]-4);
                    g.fillRect(w[3]-1, height-3-(w[2]-4), (int)(w[3]*1.2), w[2]-4);

                    g.fillRect(width - (int)(block.arc[2]/2), height-2, 2, 1);
                    g.fillRect(width-w[1]*2, height-2-(w[2]-4), (int)(w[1]*1.2), w[2]-5);
                    g.fillRect(width-w[1]*2-1, height-3-(w[2]-4), (int)(w[1]*1.2), w[2]-5);
                }
                if (w[3] >= 5) {
                    g.setColor(col[3]);

                    g.fillRect(1, (int)(block.arc[3]/2) - 1, 1, 4);
                    g.fillRect(2, (int)(block.arc[3]/2) - 2, 1, 4);
                    g.fillRect(3, (int)(block.arc[3]/2) - 3, 1, 4);

                    g.fillRect(1, height - (int)(block.arc[3]/2), 1, 3);
                    g.fillRect(2, height - (int)(block.arc[3]/2) + 1, 1, 3);
                    g.fillRect(3, height - (int)(block.arc[3]/2) + 2, 1, 3);
                }

                if (w[1] >= 5) {
                    g.setColor(col[1]);

                    g.fillRect(width-2, (int)(block.arc[1]/2) - 1, 1, 4);
                    g.fillRect(width-3, (int)(block.arc[1]/2) - 2, 1, 4);
                    g.fillRect(width-4, (int)(block.arc[1]/2) - 3, 1, 4);

                    g.fillRect(width-2, height - (int)(block.arc[1]/2), 1, 3);
                    g.fillRect(width-3, height - (int)(block.arc[1]/2) + 1, 1, 3);
                    g.fillRect(width-4, height - (int)(block.arc[1]/2) + 2, 1, 3);
                }
                
            }

            if (block.arc[0] == 0 && block.arc[1] == 0 && block.arc[2] == 0 && block.arc[3] == 0 && blend_corners) {
                if (w[0] != w[3]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[3].getRed()+col[0].getRed()) / 2),
                                      (int)Math.round((col[3].getGreen()+col[0].getGreen()) / 2),
                                      (int)Math.round((col[3].getBlue()+col[0].getBlue()) / 2) ));
                if (w[0] != w[3]) {
                    g.drawLine(x, y, x+w[3]-1, y+w[0]-1);
                } else {
                    for (int i = 0; i < w[0]; i++) {
                        g.fillRect(x+i, y+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[0] != w[1]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[1].getRed()+col[0].getRed()) / 2),
                                      (int)Math.round((col[1].getGreen()+col[0].getGreen()) / 2),
                                      (int)Math.round((col[1].getBlue()+col[0].getBlue()) / 2) ));
                if (w[0] != w[1]) {
                    g.drawLine(x+width-1, y, x+width-w[1], y+w[0]-1);
                } else {
                    for (int i = 0; i < w[0]; i++) {
                        g.fillRect(x+width-w[1]+i, y+w[0]-1-i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[2] != w[1]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[1].getRed()+col[2].getRed()) / 2),
                                      (int)Math.round((col[1].getGreen()+col[2].getGreen()) / 2),
                                      (int)Math.round((col[1].getBlue()+col[2].getBlue()) / 2) ));
                if (w[2] != w[1]) {
                    g.drawLine(x+width-1, y+height-w[2]+1, x+width-w[1], y+height-1);
                } else {
                    for (int i = 0; i < w[2]; i++) {
                        g.fillRect(x+width-w[1]+i, y+height-w[2]+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[2] != w[3]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[3].getRed()+col[2].getRed()) / 2),
                                      (int)Math.round((col[3].getGreen()+col[2].getGreen()) / 2),
                                      (int)Math.round((col[3].getBlue()+col[2].getBlue()) / 2) ));
                if (w[2] != w[3]) {
                    g.drawLine(x+w[3]-1, y+height-w[2]+1, x, y+height-1);
                } else {
                    for (int i = 0; i < w[2]; i++) {
                        g.fillRect(x+w[3]-1-i, y+height-w[2]+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    public void paintBorder3(Component c, Graphics g, int x, int y, int width, int height) {
        width = block.viewport_width > 0 ? block.viewport_width + (block.scrollbar_y != null ? block.scrollbar_y.getPreferredSize().width : 0) : block.width;
        height = block.viewport_height > 0 ? block.viewport_height + (block.scrollbar_x != null ? block.scrollbar_x.getPreferredSize().height : 0): block.height;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int r = (int)Math.round(radius / Math.sqrt(2));

        int w0 = Math.max(w[0], r);
        int w1 = Math.max(w[1], r);
        int w2 = Math.max(w[2], r);
        int w3 = Math.max(w[3], r);

        int[][][] d = new int[8][2][3];
        int[] arrayX1 = {x, x+w3, x+w3};
        int[] arrayY1 = {y, y+w0, y};
        d[0][0] = arrayX1;
        d[0][1] = arrayY1;
        int[] arrayX2 = {x+width, x+width-w1, x+width-w1};
        int[] arrayY2 = {y, y+w0, y};
        d[1][0] = arrayX2;
        d[1][1] = arrayY2;
        int[] arrayX3 = {x+width, x+width, x+width-w1};
        int[] arrayY3 = {y, y+w0, y+w0};
        d[2][0] = arrayX3;
        d[2][1] = arrayY3;
        int[] arrayX4 = {x+width, x+width-w1, x+width};
        int[] arrayY4 = {y+height-w2, y+height-w2, y+height};
        d[3][0] = arrayX4;
        d[3][1] = arrayY4;
        int[] arrayX5 = {x+width, x+width-w1, x+width-w1};
        int[] arrayY5 = {y+height, y+height, y+height-w2};
        d[4][0] = arrayX5;
        d[4][1] = arrayY5;
        int[] arrayX6 = {x, x+w3, x+w3};
        int[] arrayY6 = {y+height, y+height, y+height-w2};
        d[5][0] = arrayX6;
        d[5][1] = arrayY6;
        int[] arrayX7 = {x, x+w3, x};
        int[] arrayY7 = {y+height-w2, y+height-w2, y+height};
        d[6][0] = arrayX7;
        d[6][1] = arrayY7;
        int[] arrayX8 = {x, x+w3, x};
        int[] arrayY8 = {y, y+w0, y+w0};
        d[7][0] = arrayX8;
        d[7][1] = arrayY8;

        Color color, color2, color3;

        for (int j = 0; j < 8; j++) {
            Polygon poly = new Polygon(d[j][0], d[j][1], 3);
            g.setClip(poly);
            color = col[j / 2];
            g.setColor(color);
            color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 88);
            color3 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 58);
            //RoundedRect rect = new RoundedRect(x, y, width-1, height-1, block.arc[0] / 2, block.arc[1] / 2, block.arc[2] / 2, block.arc[3] / 2);
            //((Graphics2D)g).fill(rect);
            RoundedRect rect = null;
            for (int i = 0; i < w[j/2]; i++) {
                if (block.arc[0] != 0 || block.arc[1] != 0 || block.arc[2] != 0 || block.arc[3] != 0) {

                    //g.fillRoundRect(0, 0, width, height, radius, radius);

                    if (i == w[j/2]-1) g.setColor(color2);

                    if (i > 0) {
                        if (i < w[j/2]-1) {
                            g.setColor(color2);
                        } else {
                            g.setColor(color3);
                        }
                        
                        g.setColor(color);
                        int seg_length = 0;
                        if (j == 0 || j == 7 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 0 && type[0] == DASHED || j == 7 && type[3] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? (int)Math.floor(w[j/2] * 0.6) : w[j/2] * 2;
                            drawSegArc(g, x+(int)(block.arc[0]/1.8), y+(int)(block.arc[0]/1.8), (int)(block.arc[0]/1.8), 90, 180, seg_length, j == 0 ? w[0] : w[3]);
                        } else if (j == 1 || j == 2 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 1 && type[0] == DASHED || j == 2 && type[1] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? (int)Math.floor(w[j/2] * 0.6) : w[j/2] * 2;
                            drawSegArc(g, x+width-(int)(block.arc[1]/1.8), w[j/2] > 3 ? y+(int)(block.arc[1]/1.8)+1 : y+(int)(block.arc[1]/1.8), (int)(block.arc[1]/1.8), w[j/2] > 5 || (j == 1 && type[0] == DASHED || j == 2 && type[1] == DASHED) ? 0 : 18, w[j/2] > 5 ? 95 : 90, seg_length, j == 1 ? w[0] : w[1]);
                        } else if (j == 4 || j == 3 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 3 && type[1] == DASHED || j == 4 && type[2] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? (int)Math.floor(w[j/2] * 0.6) : w[j/2] * 2;
                            drawSegArc(g, x+width-(int)(block.arc[2]/1.8)+1, y+height-(int)(block.arc[2]/1.8), (int)(block.arc[2]/1.8), 270, w[j/2] > 5 ? 360 : 356, seg_length, j == 3 ? w[1] : w[2]);
                        } else if (j == 5 || j == 6 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 5 && type[2] == DASHED || j == 6 && type[3] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? (int)Math.floor(w[j/2] * 0.6) : w[j/2] * 2;
                            drawSegArc(g, x+(int)(block.arc[2]/1.8), y+height-(int)(block.arc[2]/1.8), (int)(block.arc[3]/1.8), 180, 273, seg_length, j == 5 ? w[2] : w[3]);
                        }
                        
                    } else {
                        g.setColor(color2);
                        int seg_length = 0;
                        if (j == 0 || j == 7 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 0 && type[0] == DASHED || j == 7 && type[3] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? w[j/2] : w[j/2] * 2;
                            if (j == 0 && type[0] == SOLID || j == 7 && type[3] == SOLID) {
                                seg_length = (int)Math.round(Math.PI * block.arc[0] / 3.6);
                            }
                            drawSegArc(g, x+(int)(block.arc[0]/1.8), y+(int)(block.arc[0]/1.8), (int)(block.arc[0] / 1.8), 90, 180, seg_length, j == 0 ? w[0] : w[3]);
                        } else if (j == 1 || j == 2 && (w[j/2] <= 3 || type[1] == SOLID)) {
                            seg_length = (j == 1 && type[0] == DASHED || j == 2 && type[1] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? w[j/2] : w[j/2] * 2;
                            if (j == 1 && type[0] == SOLID || j == 2 && type[1] == SOLID) {
                                seg_length = (int)Math.round(Math.PI * block.arc[1] / 3.6);
                            }
                            drawSegArc(g, x+width-(int)(block.arc[1]/1.8), w[j/2] > 3 ? y+(int)(block.arc[1]/1.8)+1 : y+(int)(block.arc[1]/1.8), (int)(block.arc[1]/1.8), 0, w[j/2] > 5 ? 95 : 90, seg_length, j == 1 ? w[0] : w[1]);
                        } else if (j == 4 || j == 3 && (w[j/2] <= 3 || type[1] == SOLID)) {
                            seg_length = (j == 3 && type[1] == DASHED || j == 4 && type[2] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? w[j/2] : w[j/2] * 2;
                            if (j == 3 && type[1] == SOLID || j == 4 && type[2] == SOLID) {
                                seg_length = (int)Math.round(Math.PI * block.arc[2] / 3.6);
                            }
                            drawSegArc(g, x+width-(int)(block.arc[2]/1.8), y+height-(int)(block.arc[2]/1.8), (int)(block.arc[2]/1.8), 270, 360, seg_length, j == 3 ? w[1] : w[2]);
                        } else if (j == 5 || j == 6 && (w[j/2] <= 3 || type[3] == SOLID)) {
                            seg_length = (j == 5 && type[2] == DASHED || j == 6 && type[3] == DASHED) ? (int)Math.floor(w[j/2] * 2) : w[j/2] <= 3 ? w[j/2] : w[j/2] * 2;
                            if (j == 5 && type[2] == SOLID || j == 6 && type[3] == SOLID) {
                                seg_length = (int)Math.round(Math.PI * block.arc[3] / 3.6);
                            }
                            drawSegArc(g, x+(int)(block.arc[2]/1.8), y+height+1-(int)(block.arc[2]/1.8), (int)(block.arc[3]/1.8), 180, 273, seg_length, j == 5 ? w[2] : w[3]);
                        }
                    }

                } else {
                    g.fillRect(x, y, width, height);
                }

                g.setClip(null);

                int seg_length = 0;

                seg_length = type[0] == DASHED ? (int)Math.floor(w[j / 2] * 2.5) : w[j / 2];
                g.setColor(col[0]);
                if (type[0] != SOLID) {
                    drawSegLine(g, x+(int)(block.arc[0]/2), x+width-1-(int)(block.arc[1]/2), y+i, y+i, seg_length, block.arc[0]>0, block.arc[1]>0, w[j / 2] <= 3 && type[0]==DOTTED);
                } else {
                    g.drawLine(x+(int)(block.arc[0]/2), y+i, x+width-1-(int)(block.arc[1]/2), y+i);
                }
                seg_length = type[1] == DASHED ? (int)Math.floor(w[j / 2] * 2.5) : w[j / 2];
                g.setColor(col[1]);
                if (type[1] != SOLID) {
                    drawSegLine(g, x+width-1-i, x+width-1-i, y+(int)(block.arc[1]/2), y+height-1-(int)(block.arc[2]/2), seg_length, block.arc[1]>0, block.arc[2]>0, w[j / 2] <= 3 && type[1]==DOTTED);
                } else {
                    g.drawLine(x+width-1-i, y+(int)(block.arc[1]/2), x+width-1-i, y+height-1-(int)(block.arc[2]/2));
                }
                seg_length = type[2] == DASHED ? (int)Math.floor(w[j / 2] * 2.5) : w[j / 2];
                g.setColor(col[2]);
                if (type[2] != SOLID) {
                    drawSegLine(g, x+(int)(block.arc[3]/2), x+width-1-(int)(block.arc[2]/2), y+height-1-i, y+height-1-i, seg_length, block.arc[3]>0, block.arc[2]>0, w[j / 2] <= 3 && type[2]==DOTTED);
                } else {
                    g.drawLine(x+(int)(block.arc[3]/2), y+height-1-i, x+width-1-(int)(block.arc[2]/2), y+height-1-i);
                }
                seg_length = type[3] == DASHED ? (int)Math.floor(w[j / 2] * 2.5) : w[j / 2];
                g.setColor(col[3]);
                if (type[3] != SOLID) {
                    drawSegLine(g, x+i, x+i, y+(int)(block.arc[0]/2), y+height-1-(int)(block.arc[3]/2), seg_length, block.arc[0]>0, block.arc[3]>0, w[j / 2] <= 3 && type[3]==DOTTED);
                } else {
                    g.drawLine(x+i, y+(int)(block.arc[0]/2), x+i, y+height-1-(int)(block.arc[3]/2));
                }

                g.setClip(poly);
            }
            g.setClip(null);

            if (block.arc[0] != 0 || block.arc[1] != 0 || block.arc[2] != 0 || block.arc[3] != 0) {

                if (w[0] >= 5 && type[0] != DOTTED) {
                    g.setColor(col[0]);

                    g.fillRect((int)(block.arc[0]/2)-2, 1, 2, 1);
                    g.fillRect(w[3]-3, 2, (int)(w[3]*1.2), w[0]-4);
                    g.fillRect(w[3]-2, 3, (int)(w[3]*1.2), w[0]-4);

                    g.fillRect(width - (int)(block.arc[1]/2), 1, 2, 1);
                    g.fillRect(width-w[1]*2, 2, (int)(w[1]*1.2), w[0]-5);
                    g.fillRect(width-w[1]*2-1, 3, (int)(w[1]*1.2), w[0]-5);
                }
                if (w[2] >= 5 && type[2] != DOTTED) {
                    g.setColor(col[2]);

                    g.fillRect((int)(block.arc[3]/2)-2, height-2, 2, 1);
                    g.fillRect(w[3]-2, height-2-(w[2]-4), (int)(w[3]*1.2), w[2]-4);
                    g.fillRect(w[3]-1, height-3-(w[2]-4), (int)(w[3]*1.2), w[2]-4);

                    g.fillRect(width - (int)(block.arc[2]/2), height-2, 2, 1);
                    g.fillRect(width-w[1]*2, height-2-(w[2]-4), (int)(w[1]*1.2), w[2]-5);
                    g.fillRect(width-w[1]*2-1, height-3-(w[2]-4), (int)(w[1]*1.2), w[2]-5);
                }
                if (w[3] >= 5 && type[3] != DOTTED) {
                    g.setColor(col[3]);

                    g.fillRect(1, (int)(block.arc[3]/2) - 1, 1, 4);
                    g.fillRect(2, (int)(block.arc[3]/2) - 2, 1, 4);
                    g.fillRect(3, (int)(block.arc[3]/2) - 3, 1, 4);

                    g.fillRect(1, height - (int)(block.arc[3]/2), 1, 3);
                    g.fillRect(2, height - (int)(block.arc[3]/2) + 1, 1, 3);
                    g.fillRect(3, height - (int)(block.arc[3]/2) + 2, 1, 3);
                }

                if (w[1] >= 5 && type[1] != DOTTED) {
                    g.setColor(col[1]);

                    g.fillRect(width-2, (int)(block.arc[1]/2) - 1, 1, 4);
                    g.fillRect(width-3, (int)(block.arc[1]/2) - 2, 1, 4);
                    g.fillRect(width-4, (int)(block.arc[1]/2) - 3, 1, 4);

                    g.fillRect(width-2, height - (int)(block.arc[1]/2), 1, 3);
                    g.fillRect(width-3, height - (int)(block.arc[1]/2) + 1, 1, 3);
                    g.fillRect(width-4, height - (int)(block.arc[1]/2) + 2, 1, 3);
                }

            }

            if (block.arc[0] == 0 && block.arc[1] == 0 && block.arc[2] == 0 && block.arc[3] == 0 && blend_corners) {
                if (w[0] != w[3]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[3].getRed()+col[0].getRed()) / 2),
                                      (int)Math.round((col[3].getGreen()+col[0].getGreen()) / 2),
                                      (int)Math.round((col[3].getBlue()+col[0].getBlue()) / 2) ));
                if (w[0] != w[3]) {
                    g.drawLine(x, y, x+w[3]-1, y+w[0]-1);
                } else {
                    for (int i = 0; i < w[0]; i++) {
                        g.fillRect(x+i, y+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[0] != w[1]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[1].getRed()+col[0].getRed()) / 2),
                                      (int)Math.round((col[1].getGreen()+col[0].getGreen()) / 2),
                                      (int)Math.round((col[1].getBlue()+col[0].getBlue()) / 2) ));
                if (w[0] != w[1]) {
                    g.drawLine(x+width-1, y, x+width-w[1], y+w[0]-1);
                } else {
                    for (int i = 0; i < w[0]; i++) {
                        g.fillRect(x+width-w[1]+i, y+w[0]-1-i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[2] != w[1]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[1].getRed()+col[2].getRed()) / 2),
                                      (int)Math.round((col[1].getGreen()+col[2].getGreen()) / 2),
                                      (int)Math.round((col[1].getBlue()+col[2].getBlue()) / 2) ));
                if (w[2] != w[1]) {
                    g.drawLine(x+width-1, y+height-w[2]+1, x+width-w[1], y+height-1);
                } else {
                    for (int i = 0; i < w[2]; i++) {
                        g.fillRect(x+width-w[1]+i, y+height-w[2]+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (w[2] != w[3]) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color( (int)Math.round((col[3].getRed()+col[2].getRed()) / 2),
                                      (int)Math.round((col[3].getGreen()+col[2].getGreen()) / 2),
                                      (int)Math.round((col[3].getBlue()+col[2].getBlue()) / 2) ));
                if (w[2] != w[3]) {
                    g.drawLine(x+w[3]-1, y+height-w[2]+1, x, y+height-1);
                } else {
                    for (int i = 0; i < w[2]; i++) {
                        g.fillRect(x+w[3]-1-i, y+height-w[2]+i, 1, 1);
                    }
                }
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    public void paintBorder2(Component c, Graphics g, int x, int y, int width, int height) {
        if (w[0] == 0) return;
        g.setColor(col[0]);
        Color col2 = new Color(col[0].getRed(), col[0].getGreen(), col[0].getBlue(), 88);
        Color col3 = new Color(col[0].getRed(), col[0].getGreen(), col[0].getBlue(), 58);
        Color col4 = new Color(col[0].getRed(), col[0].getGreen(), col[0].getBlue(), 123);

        if (type[0] != SOLID) {
            int seg_length = type[0] == DASHED ? (int)Math.floor(w[0] * 2.3) : (int)Math.floor(w[0] * 0.6);
            drawSegArc(g, x+(int)(block.arc[0]/2), y+(int)(block.arc[0]/2), (int)(block.arc[0]/2), 90, 180, seg_length, w[0]);
            drawSegArc(g, x+width-(int)(block.arc[1]/2), y+(int)(block.arc[1]/2), (int)(block.arc[1]/2), 0, 90, seg_length, w[1]);
            drawSegArc(g, x+width-(int)(block.arc[2]/2), y+height-(int)(block.arc[2]/2), (int)(block.arc[2]/2), 270, 360, seg_length, w[2]);
            drawSegArc(g, x+(int)(block.arc[3]/2), y+height-(int)(block.arc[3]/2), (int)(block.arc[3]/2), 180, 270, seg_length, w[3]);
        }

        for (int i = 0; i < w[0]; i++) {
            g.setColor(col[0]);
            if (i == w[0]-1 && radius > 0 && type[0] == SOLID) g.setColor(col2);
            if (block.arc[0] == block.arc[1] && block.arc[1] == block.arc[2] && block.arc[2] == block.arc[3]) {
                if (type[0] == SOLID) {
                    if (radius > 0) {
                        g.drawRoundRect(x+i, y+i, width-1-i*2, height-1-i*2, radius, radius);
                    } else {
                        g.drawRect(x+i, y+i, width-1-i*2, height-1-i*2);
                    }
                } else {
                    int seg_length = type[0] == DASHED ? (int)Math.floor(w[0] * 2.5) : w[0];
                    drawSegLine(g, x+(int)(block.arc[0]/2), x+width-1-(int)(block.arc[1]/2), y+i, y+i, seg_length, block.arc[0]>0, block.arc[1]>0, w[0] <= 3 && type[0]==DOTTED);
                    drawSegLine(g, x+width-1-i, x+width-1-i, y+(int)(block.arc[1]/2), y+height-1-(int)(block.arc[2]/2), seg_length, block.arc[1]>0, block.arc[2]>0, w[1] <= 3 && type[1]==DOTTED);
                    drawSegLine(g, x+(int)(block.arc[3]/2), x+width-1-(int)(block.arc[2]/2), y+height-1-i, y+height-1-i, seg_length, block.arc[3]>0, block.arc[2]>0, w[2] <= 3 && type[2]==DOTTED);
                    drawSegLine(g, x+i, x+i, y+(int)(block.arc[0]/2), y+height-1-(int)(block.arc[3]/2), seg_length, block.arc[0]>0, block.arc[3]>0, w[3] <= 3 && type[3]==DOTTED);
                }
            } else {
                RoundedRect rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2, block.arc[1] / 2, block.arc[2] / 2, block.arc[3] / 2);
                ((Graphics2D)g).draw(rect);
            }
            if (radius > 0) {
                if (i > 0) {
                    if (((Graphics2D)g).getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_OFF) {
                        if (i < w[0]-1) {
                            g.setColor(col2);
                        } else {
                            g.setColor(col3);
                        }
                    } else {
                        g.setColor(col4);
                    }
                    if (type[0] == SOLID) {
                        if (block.arc[0] == block.arc[1] && block.arc[1] == block.arc[2] && block.arc[2] == block.arc[3]) {
                            g.drawRoundRect(x+i, y+i, width-1-i*2, height-1-i*2, radius-2, radius-2);
                            g.drawRoundRect(x+i, y+i, width-1-i*2, height-1-i*2, radius-3, radius-3);
                        } else {
                            RoundedRect rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 2, block.arc[1] / 2 - 2, block.arc[2] / 2 - 2, block.arc[3]  / 2 - 2);
                            ((Graphics2D)g).draw(rect);
                            rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 3, block.arc[1] / 2 - 3, block.arc[2] / 2 - 3, block.arc[3]  / 2 - 3);
                            ((Graphics2D)g).draw(rect);
                        }
                    } else {
                        int seg_length = type[0] == DASHED ? (int)Math.floor(w[0] * 2.5) : w[0];
                        drawSegLine(g, x+(int)(block.arc[0]/2), x+width-1-(int)(block.arc[1]/2), y+i, y+i, seg_length, block.arc[0]>0, block.arc[1]>0, w[0] <= 3 && type[0]==DOTTED);
                        drawSegLine(g, x+width-1-i, x+width-1-i, y+(int)(block.arc[1]/2), y+height-1-(int)(block.arc[2]/2), seg_length, block.arc[1]>0, block.arc[2]>0, w[1] <= 3 && type[1]==DOTTED);
                        drawSegLine(g, x+(int)(block.arc[3]/2), x+width-1-(int)(block.arc[2]/2), y+height-1-i, y+height-1-i, seg_length, block.arc[3]>0, block.arc[2]>0, w[2] <= 3 && type[2]==DOTTED);
                        drawSegLine(g, x+i, x+i, y+(int)(block.arc[0]/2), y+height-1-(int)(block.arc[3]/2), seg_length, block.arc[0]>0, block.arc[3]>0, w[3] <= 3 && type[3]==DOTTED);
                    }
                    g.setColor(col[0]);
                } else {
                    if (type[0] == SOLID) {
                        g.setColor(col3);
                        if (block.arc[0] == block.arc[1] && block.arc[1] == block.arc[2] && block.arc[2] == block.arc[3]) {
                            g.drawRoundRect(x+i, y+i, width-1-i*2, height-1-i*2, radius-3, radius-3);
                        } else {
                            RoundedRect rect = new RoundedRect(x+i, y+i, width-1-i*2, height-1-i*2, block.arc[0] / 2 - 3, block.arc[1] / 2 - 3, block.arc[2] / 2 - 3, block.arc[3]  / 2 - 3);
                            ((Graphics2D)g).draw(rect);
                        }
                        g.setColor(col[0]);
                    } else {
                        int seg_length = type[0] == DASHED ? (int)Math.floor(w[0] * 2.3) : w[0];
                        drawSegLine(g, x+(int)(block.arc[0]/2), x+width-1-(int)(block.arc[1]/2), y+i, y+i, seg_length, block.arc[0]>0, block.arc[1]>0, w[0] <= 3 && type[0]==DOTTED);
                        drawSegLine(g, x+width-1-i, x+width-1-i, y+(int)(block.arc[1]/2), y+height-1-(int)(block.arc[2]/2), seg_length, block.arc[1]>0, block.arc[2]>0, w[1] <= 3 && type[1]==DOTTED);
                        drawSegLine(g, x+(int)(block.arc[3]/2), x+width-1-(int)(block.arc[2]/2), y+height-1-i, y+height-1-i, seg_length, block.arc[3]>0, block.arc[2]>0, w[2] <= 3 && type[2]==DOTTED);
                        drawSegLine(g, x+i, x+i, y+(int)(block.arc[0]/2), y+height-1-(int)(block.arc[3]/2), seg_length, block.arc[0]>0, block.arc[3]>0, w[3] <= 3 && type[3]==DOTTED);
                    }
                }
            }
        }
    }

    private void drawSegLine(Graphics g, int x_from, int x_to, int y_from, int y_to, float seg_length, boolean cut_from, boolean cut_to, boolean strict) {
        float length = (float)Math.sqrt((x_to - x_from) * (x_to - x_from) + (y_to - y_from) * (y_to - y_from));
        int segs_num = (int)Math.floor(length / seg_length);
        if (length <= 0) return;
        if (length < seg_length) {
            seg_length = length;
            segs_num = 1;
        }
        while (segs_num % 2 == 0 && (!cut_from || !cut_to)) {
            seg_length += (x_from == x_to || y_from == y_to) ? 1 : 0.2;
            segs_num = (int)Math.floor(Math.sqrt((x_to - x_from) * (x_to - x_from) + (y_to - y_from) * (y_to - y_from)) / seg_length);
        }
        boolean flag = false;
        if (segs_num % 2 == 0 && cut_from && cut_to) {
            segs_num++;
            flag = true;
        }
        float[] len = new float[segs_num];
        float s = 0;
        for (int i = 0; i < segs_num; i++) {
            len[i] = seg_length;
            s += len[i];
        }
        if (!strict) {
            if (flag && cut_from && cut_to) {
                s -= seg_length;
                double r = len[0] - Math.floor(len[0] / 2);
                len[0] -= r;
                len[segs_num-1] = (float)r;
            }
            float r = length - s;
            int pos = 0;
            if (r > 0 && x_from != x_to && y_from != y_to) {
                for (int i = 0; i < segs_num; i++) {
                    len[i] += r / segs_num;
                }
                s = length;
            } else if (r > 0) {
                int r2 = (int)Math.floor(r / segs_num);
                if (r2 > 0) {
                    for (int i = 0; i < segs_num; i++) {
                        len[i] += r2;
                    }
                    s += r2 * segs_num;
                }

                int i = 0;
                while (s < length && i < segs_num) {
                    if (s+1 >= length) {
                        len[(segs_num-1) / 2]++;
                        s++;
                        continue;
                    }
                    len[i]++;
                    len[segs_num-1-i]++;
                    s+=2;
                    i++;
                    pos = i;
                }
            }
            if (segs_num > 3) {
                if (cut_from && !cut_to) {
                    int r3 = (int)Math.floor(len[0] * 0.6);
                    len[0] -= r3;

                    int r2 = (int)Math.floor(r3 / (segs_num-1));
                    for (int i = 1; i < segs_num; i++) {
                        len[i] += r2;
                    }
                    r3 -= r2 * (segs_num-1);

                    if (pos >= segs_num / 2 - 1) pos = 0;
                    for (int i = pos; r3 > 0 && i < segs_num-1; i++) {
                        if (r3-2 < 0) {
                            len[(segs_num-1) / 2]++;
                            r3--;
                            continue;
                        }
                        len[1+i]++;
                        len[segs_num-1-i]++;
                        r3-=2;
                        i++;
                    }
                } else if (!cut_from && cut_to) {
                    int r3 = (int)Math.floor(len[segs_num-1] * 0.6);
                    len[segs_num-1] -= r3;

                    int r2 = (int)Math.floor(r3 / (segs_num-1));
                    for (int i = 0; i < segs_num-1; i++) {
                        len[i] += r2;
                    }
                    r3 -= r2 * (segs_num-1);
                    
                    if (pos >= segs_num / 2 - 1) pos = 0;
                    for (int i = pos; r3 > 0 && i < segs_num-1; i++) {
                        if (r3-2 < 0) {
                            len[(segs_num-1) / 2]++;
                            r3--;
                            continue;
                        }
                        len[i]++;
                        len[segs_num-2-i]++;
                        r3-=2;
                        i++;
                    }
                } else if (!flag && cut_from && cut_to) {
                    int r3 = (int)Math.floor(len[0] * 0.6) + (int)Math.floor(len[segs_num-1] * 0.6);
                    len[0] -= (int)Math.floor(len[0] * 0.6);
                    len[segs_num-1] -= (int)Math.floor(len[segs_num-1] * 0.6);

                    int r2 = (int)Math.floor(r3 / (segs_num-2));
                    for (int i = 1; i < segs_num-1; i++) {
                        len[i] += r2;
                    }
                    r3 -= r2 * (segs_num-2);
                    
                    if (pos >= segs_num / 2 - 1) pos = 0;
                    for (int i = pos; r3 > 0 && i < segs_num-2; i++) {
                        if (r3-2 < 0) {
                            len[(segs_num-1) / 2]++;
                            r3--;
                            continue;
                        }
                        len[1+i]++;
                        len[segs_num-2-i]++;
                        r3-=2;
                        i++;
                    }
                }
            }
        }
        float offset = 0;
        for (int i = 0; i < segs_num; i += 2) {
            int dx1 = strict && (y_to == y_from) ? (int)offset : (int)Math.floor(offset / length * (x_to - x_from));
            int dy1 = strict && (x_to == x_from) ? (int)offset : (int)Math.floor(offset / length * (y_to - y_from));
            
            offset += len[i];

            int dx2 = strict && (y_to == y_from) ? (int)offset : (int)Math.floor(offset / length * (x_to - x_from));
            int dy2 = strict && (x_to == x_from) ? (int)offset : (int)Math.floor(offset / length * (y_to - y_from));

            if (dx2 < dx1) dx2++;
            if (dx2 > dx1) dx2--;

            if (dy2 < dy1) dy2++;
            if (dy2 > dy1) dy2--;

            g.drawLine(x_from + dx1, y_from + dy1, x_from + dx2, y_from + dy2);

            if (i < segs_num-1) offset += len[i+1];
        }
    }

    private void drawSegArc(Graphics g, int xc, int yc, int radius, int from, int to, float seg_length, int thickness) {
        float length = (float)(Math.PI * radius * (to - from) / 180);
        int segs_num = (int)Math.floor(length / seg_length);
        if (length <= 0) return;
        if (length < seg_length) {
            seg_length = length;
            segs_num = 1;
        }
        if (seg_length >= length) {
            segs_num = 1;
            seg_length = length;
        }
        while (segs_num % 2 == 0) {
            seg_length += 0.1;
            segs_num = (int)Math.floor(length / seg_length);
        }

        float len = length / segs_num;

        float offset = 0;

        for (int i = 0; i < segs_num; i += 2) {
            int d1 = from + (int)Math.floor(offset / length * (to - from));

            offset += len < thickness * 1.2 ? len * 0.32 : len * 0.92;

            int d2 = from + (int)Math.floor(offset / length * (to - from));

            for (int j = 0; j < thickness; j++) {
                g.drawArc(xc - radius + j, yc - radius + j, 2 * radius - 2*j - 1, 2 * radius - 2*j - 1, d1, d2-d1-1);
            }

            if (i < segs_num-1) offset += len < thickness * 1.83 ? len * 2.46 : len * 1.2;
        }
    }

    private static boolean blend_corners = true;

    public int[] type = {0, 0, 0, 0};

    public static final int SOLID = 0;
    public static final int DASHED = 1;
    public static final int DOTTED = 2;

    private Block block;
    private int radius;
    private Color[] col;
    private int[] w = {0, 0, 0, 0};
}
