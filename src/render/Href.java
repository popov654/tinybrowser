/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package render;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.JPanel;

/**
 *
 * @author Alex
 */
public class Href extends Block {

    public Href(WebDocument document) {
        super(document);
        clickPanel = new JPanel();
        clickPanel.setOpaque(false);
        add(clickPanel);
        clickPanel.setBounds(_x_, _y_, width, height);
        clickPanel.addMouseListener(this);
    }

    public void setURL(String url) {
        href = url;
        if (url != null) {
            clickPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            clickPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public synchronized void performLayout(boolean no_rec, boolean no_viewport_reset) {
        super.performLayout(no_rec, no_viewport_reset);
        int w = width;
        if (_x_ - parent.scroll_x < 0) w -= parent.scroll_x - _x_;
        if (_x_ - parent.scroll_x + width > parent.width) w -= _x_ - parent.scroll_x + width - parent.width;
        int h = height;
        if (_y_ - parent.scroll_y < 0) h -= parent.scroll_y - _y_;
        if (_y_ - parent.scroll_y + height > parent.height) h -= _y_ - parent.scroll_y + height - parent.height;
        clickPanel.setBounds(_x_ - parent.scroll_x, _y_ - parent.scroll_y, w, h);
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (href == null) return;
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) rt.exec("rundll32 url.dll,FileProtocolHandler " + href);
            else if (os.indexOf("mac") >= 0) rt.exec("open " + href);
            else {
                String[] browsers = { "google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                                 "netscape", "opera", "links", "lynx" };
                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++)
                    if(i == 0)
                        cmd.append(String.format(    "%s \"%s\"", browsers[i], href));
                    else
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], href));

                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }
        } catch (IOException ex) {}
    }

    JPanel clickPanel;
    String href;
}
