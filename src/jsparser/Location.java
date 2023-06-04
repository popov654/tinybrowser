package jsparser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class Location extends JSObject {

    public Location() {
        items.put("href", new JSString(""));
        items.put("origin", new JSString(""));
        items.put("protocol", new JSString(""));
        items.put("host", new JSString(""));
        items.put("pathname", new JSString(""));
        items.put("file", new JSString(""));
        items.put("query", new JSString(""));
        items.put("hash", new JSString(""));
    }

    public Location(String url) {
        setURL(url);
    }

    public void setURL(String url) {
        try {
            if (!url.matches("^(https?|ftp|file):.*")) {
                url = "file:///" + url;
            }
            URL uri = new URL(url);
            items.put("href", new JSString(url));
            items.put("origin", new JSString(uri.getAuthority()));
            items.put("protocol", new JSString(uri.getProtocol()));
            items.put("host", new JSString(uri.getHost()));
            items.put("pathname", new JSString(uri.getPath()));
            items.put("file", new JSString(uri.getFile()));
            items.put("query", new JSString(uri.getQuery()));
            items.put("hash", new JSString(uri.getRef()));
        } catch (MalformedURLException ex) {}
    }

    private String type = "Object";
}
