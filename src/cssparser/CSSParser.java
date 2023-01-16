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
            if (comment && pos < str.length()-1 && str.substring(pos, pos+2).equals("*/")) {
                comment = false;
                str = str.substring(0, comment_start) + str.substring(pos+2);
                pos = comment_start;
                continue;
            } else if (comment) {
                pos++;
                continue;
            }
            if (!comment && pos < str.length()-1 && str.substring(pos, pos+2).equals("/*")) {
                comment = true;
                comment_start = pos;
                pos += 2;
                continue;
            }
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
    int comment_start = 0;
    boolean open = false;
    boolean quotes = false;
    boolean comment = false;
    char quote;

    HTMLParser hp;
}
