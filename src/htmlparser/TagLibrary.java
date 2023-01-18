package htmlparser;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TagLibrary {
    public static void init() {
        if (init) return;

        tags.put("br", false);
        tags.put("hr", false);
        tags.put("link", false);
        tags.put("img", false);
        tags.put("a", true);
        tags.put("span", true);
        tags.put("div", true);
        tags.put("p", true);
        tags.put("sub", true);
        tags.put("sup", true);
        tags.put("b", true);
        tags.put("i", true);
        tags.put("u", true);
        tags.put("s", true);
        tags.put("strong", true);
        tags.put("em", true);
        tags.put("quote", true);
        tags.put("cite", true);
        tags.put("table", true);
        tags.put("thead", true);
        tags.put("tbody", true);
        tags.put("cite", true);
        tags.put("head", true);
        tags.put("body", true);

        leaves.add("style");
        leaves.add("script");

        init = true;
    }

    private static boolean init = false;

    public static Hashtable<String, Boolean> tags = new Hashtable<String, Boolean>();
    public static Vector<String> leaves = new Vector<String>();
}
