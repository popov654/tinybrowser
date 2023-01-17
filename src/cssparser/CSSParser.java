package cssparser;

import htmlparser.HTMLParser;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import service.FontManager;

/**
 *
 * @author Alex
 */
public class CSSParser {

    public CSSParser(HTMLParser parser) {
        hp = parser;
        global_rules = new HashMap<String, HashMap<String, String>>();
    }

    public Vector<QuerySelector> parseString(String str) {
        Vector<QuerySelector> result = new Vector<QuerySelector>();
        str = str.trim();
        int pos = 0;
        while (pos < str.length()) {
            if (!comment && !open && str.charAt(pos) == '@') {
                at_rule = true;
                last_start = pos+1;
                pos++;
                continue;
            }
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
                if (!at_rule) {
                    result.add(new QuerySelector(current_query, hp, block));
                } else {
                    global_rules.put(current_query, parseRules(block));
                    at_rule = false;
                }
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

    public void applyGlobalRules(String baseUrl) {
        Set<String> keys = global_rules.keySet();
        for (String key: keys) {
            if (key.equals("font-face")) {
                String fontFamily = global_rules.get(key).get("font-family");
                String src = global_rules.get(key).get("src");
                if (src != null && !src.isEmpty()) {
                    if (src.matches("url\\(\'[^\']+\'\\)") || src.matches("url\\(\"[^\"]+\"\\)")) {
                        src = src.substring(5, src.length()-2);
                    } else if (src.matches("url\\([^\\(\\)]+\\)")) {
                        src = src.substring(4, src.length()-1);
                    }
                    FontManager.registerFont(baseUrl + src, fontFamily);
                }
            }
        }
    }

    public static LinkedHashMap<String, String> parseRules(String rules) {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        int pos = 0;
        String rule = "";
        char quote = '\0';
        while (pos < rules.length()) {
            char ch = rules.charAt(pos);
            if (ch == ';' && quote == '\0') {
                String[] p = rule.split("\\s*:\\s*");
                if (p[0].trim().matches("^[a-z-]{2,}[a-z]$") && !p[1].trim().isEmpty()) {
                    String val = p[1].trim();
                    if (val.matches("\".*\"") || val.matches("\'.*\'")) {
                        val = val.substring(1, val.length()-1);
                    }
                    result.put(p[0].trim().toLowerCase(), val);
                }
                rule = "";
                pos++;
                continue;
            } else if (ch == quote) {
                quote = '\0';
            } else if ((ch == '"' || ch == '\'') && quote == '\0') {
                quote = ch;
            }
            rule += ch;
            pos++;
        }
        if (rule.length() > 0) {
            String[] p = rule.split("\\s*:\\s*");
            if (p[0].trim().matches("^[a-z-]{2,}[a-z]$") && !p[1].trim().isEmpty()) {
                String val = p[1].trim();
                if (val.matches("\".*\"") || val.matches("\'.*\'")) {
                    val = val.substring(1, val.length()-1);
                }
                result.put(p[0].trim().toLowerCase(), val);
            }
        }
        return result;
    }

    HashMap<String, HashMap<String, String>> global_rules;
    
    String current_query = "";
    int last_start = 0;
    int comment_start = 0;
    boolean open = false;
    boolean quotes = false;
    boolean comment = false;
    boolean at_rule = false;
    char quote;

    HTMLParser hp;
}
