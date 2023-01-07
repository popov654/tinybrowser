package cssparser;

import htmlparser.HTMLParser;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class CSSParser {

    public CSSParser(HTMLParser parser) {
        hp = parser;
    }

    public Vector<QuerySelector> parseString(String str) {
        Vector<QuerySelector> result = new Vector<QuerySelector>();
        str = str.trim();
        int pos = 0;
        while (pos < str.length()) {
            if (str.charAt(pos) == '{' && !quotes) {
                open = true;
                current_query = str.substring(last_start, pos).trim();
                last_start = pos + 1;
            } else if (str.charAt(pos) == '}' && !quotes) {
                open = false;
                String block = str.substring(last_start, pos-1).trim();
                result.add(new QuerySelector(current_query, hp, block));
                current_query = "";
                last_start = pos + 1;
            } else if ((str.charAt(pos) == '\'' || str.charAt(pos) == '"') && !quotes) {
                quote = str.charAt(pos);
                quotes = true;
            } else if (str.charAt(pos) == quote) {
                quote = '\0';
                quotes = false;
            }
            pos++;
        }
        return result;
    }

    String current_query = "";
    int last_start = 0;
    boolean open = false;
    boolean quotes = false;
    char quote;

    HTMLParser hp;
}
