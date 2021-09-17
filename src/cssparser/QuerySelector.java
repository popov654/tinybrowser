/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cssparser;

import htmlparser.HTMLParser;
import htmlparser.Node;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Alex
 */
public class QuerySelector {
    public QuerySelector(String query, HTMLParser hp) {
        if (query.contains(",")) {
            String[] a = query.split(",\\s*");
            QuerySelector q = new QuerySelector(a[0], hp);
            for (int i = 1; i < a.length; i++) {
                q.combine(new QuerySelector(a[i], hp));
            }
        }
        this.query = query;
        this.hp = hp;
        parse();
    }

    public void combine(QuerySelector q) {
        resultSet.addAll(q.getElements());
    }

    public Vector<Node> getElements() {
        return resultSet;
    }

    private void parse() {
        query = query.trim().replaceAll("\\s{2,}", " ");
        orig_query = query;
        query = query.replaceAll(" > ", ">");
        query = query.replaceAll(" + ", "+");
        query = query.replaceAll(" ~ ", "~");
        query = query.replaceAll("::", ":");
        parts = query.split("((?<=[> +~])|(?=[> +~]))");
        int pos = -1;
        boolean failed = false;
        pos = tryToFindID();
        if (pos >= 0) {
            resultSet = new Vector<Node>();
            Node n = hp.getElementById(getId(parts[pos]).substring(1));
            if (n != null) {
                String tag = getTag(parts[pos]);
                if (!tag.isEmpty() && !resultSet.get(0).tagName.equals(tag)) {
                    return;
                }
                String classes = getClasses(parts[pos]);
                if (!classes.isEmpty() && !hp.getElementsByClassName(resultSet.get(0).parent, classes, false)
                     .contains(resultSet.get(0))) {
                    return;
                }
                resultSet.add(n);
                applyPseudoClasses(parts[pos]);
                if (resultSet.isEmpty()) return;
            } else {
                return;
            }
        }
        else if (pos < 0) {
            pos = tryToFindClassName();
            if (pos >= 0) {
                String str = getClasses(parts[pos]);
                resultSet = hp.getElementsByClassName(str);
                String tag = getTag(parts[pos]);
                if (!tag.isEmpty()) {
                    for (int i = 0; i < resultSet.size(); i++) {
                        if (!resultSet.get(i).tagName.equals(tag)) {
                            resultSet.remove(i);
                        }
                    }
                }
                applyPseudoClasses(parts[pos]);
                if (resultSet.isEmpty()) return;
            }
        }
        if (pos > 0 && (parts[pos-1].equals("+") || parts[pos-1].equals("~"))) pos = -1;
        for (int i = pos-1; i >= 0; i-=2) {
            stepLeft(i);
        }
        if (pos == -1) pos--;
        for (int i = pos+1; i < parts.length; i+=2) {
            stepRight(i);
        }
        if (resultSet == null) {
            resultSet = new Vector<Node>();
        }
    }

    private void applyPseudoClasses(String expr) {
        String psc = getPseudoClasses(expr);
        psc = psc.replaceAll("^:", "");
        if (!psc.isEmpty()) {
            String[] pseudoclasses = psc.split(":");
            for (int j = 0; j < resultSet.size(); j++) {
                for (int i = 0; i < pseudoclasses.length; i++) {
                    if (pseudoclasses[i].equals("first-child") && !resultSet.get(j).isFirstElementChild()) {
                        resultSet.remove(j);
                        break;
                    }
                    if (pseudoclasses[i].equals("last-child") && !resultSet.get(j).isLastElementChild()) {
                        resultSet.remove(j);
                        break;
                    }
                    if (pseudoclasses[i].equals("nth-child(odd)") && !resultSet.get(j).isOddElementChild()) {
                        resultSet.remove(j);
                        break;
                    }
                    if (pseudoclasses[i].equals("nth-child(even)") && !resultSet.get(j).isEvenElementChild()) {
                        resultSet.remove(j);
                        break;
                    }
                    if (pseudoclasses[i].startsWith("nth-child(")) {
                        Matcher m = Pattern.compile("\\(([1-9][0-9]*)\\)$").matcher(pseudoclasses[i]);
                        if (m.find()) {
                            int num = Integer.parseInt(m.group(1));
                            if (!resultSet.get(j).isNthElementChild(num)) {
                                resultSet.remove(j);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkPseudoClassesOfAncestor(Node ancestor, String expr) {
        String psc = getPseudoClasses(expr);
        psc = psc.replaceAll("^:", "");
        if (!psc.isEmpty()) {
            String[] pseudoclasses = psc.split(":");
            for (int j = 0; j < resultSet.size(); j++) {
                for (int i = 0; i < pseudoclasses.length; i++) {
                    if (pseudoclasses[i].equals("first-child") && !ancestor.isFirstElementChild()) {
                        return false;
                    }
                    if (pseudoclasses[i].equals("last-child") && !ancestor.isLastElementChild()) {
                        return false;
                    }
                    if (pseudoclasses[i].equals("nth-child(odd)") && !ancestor.isOddElementChild()) {
                        return false;
                    }
                    if (pseudoclasses[i].equals("nth-child(even)") && !ancestor.isEvenElementChild()) {
                        return false;
                    }
                    if (pseudoclasses[i].startsWith("nth-child(")) {
                        Matcher m = Pattern.compile("\\(([1-9][0-9]*)\\)$").matcher(pseudoclasses[i]);
                        if (m.find()) {
                            int num = Integer.parseInt(m.group(1));
                            if (!ancestor.isNthElementChild(num)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private String[] split(String expr) {
        return expr.split("((?<=[\\.#])|(?=[\\.#]))");
    }

    private String getTag(String str) {
        str = str.replaceAll("(\\.|#|:)[^\\.#:]+", "");
        return str;
    }

    private String getId(String str) {
        str = str.replaceAll("^[^\\.#:]+", "");
        str = str.replaceAll("\\.[^\\.#:]+", "");
        str = str.replaceAll(":[^:]+", "");
        return str;
    }

    private String getClasses(String str) {
        str = str.replaceAll("^[^\\.#:]+", "");
        str = str.replaceAll("#[^\\.#:]+", "");
        str = str.replaceAll(":[^:]+", "");
        return str;
    }

    private String getPseudoClasses(String str) {
        Matcher m = Pattern.compile(":[^:]+").matcher(str);
        String result = "";
        while (m.find()) {
            result += str.substring(m.start(), m.end());
        }
        return result;
    }

    private int tryToFindID() {
        for (int i = parts.length-1; i >= 0; i--) {
            if (parts[i].trim().contains("#")) {
                return i;
            }
        }
        return -1;
    }

    private int tryToFindClassName() {
        for (int i = parts.length-1; i >= 0; i--) {
            if (parts[i].trim().contains(".")) {
                return i;
            }
        }
        return -1;
    }

    private void stepLeft(int pos) {
        if (pos == 0) return;
        for (int i = 0; i < resultSet.size(); i++) {
            String str = parts[pos-1];
            boolean matches = true;
            if (parts[pos].equals(">")) {
                matches = checkAncestor(resultSet.get(i), str, i);
            } else if (parts[pos].equals(" ")) {
                matches = checkAllAncestors(resultSet.get(i), str, i);
            }
            if (!matches) {
                resultSet.remove(i--);
            }
        }
    }

    private boolean checkAncestor(Node node, String expr, int index) {
        node = node.parent;
        if (node == null) {
            return false;
        }
        String id = getId(expr);
        String classes = getClasses(expr);
        String tag = getTag(expr);
        if (!classes.isEmpty() && !hp.getElementsByClassName(node.parent, classes, false).contains(node)) {
            return false;
        }
        if (!id.isEmpty() && !node.attributes.get("id").equals(id.substring(1))) {
            return false;
        }
        if (!node.tagName.equals(tag)) {
            return false;
        }
        if (!checkPseudoClassesOfAncestor(node, expr)) {
            return false;
        }
        return true;
    }

    private boolean checkAllAncestors(Node node, String expr, int index) {
        while (node != null) {
            if (checkAncestor(node, expr, index)) {
                return true;
            }
            node = node.parent;
        }
        return false;
    }

    private void stepRight(int pos) {
        String expr = parts[pos+1];
        String id = getId(expr);
        String classes = getClasses(expr);
        String tag = getTag(expr);

        if (pos < 0) {
            resultSet = new Vector<Node>();
            boolean checked = false;
            if (!id.isEmpty()) {
                resultSet.add(hp.getElementById(id));
                checked = true;
            }
            if (!classes.isEmpty()) {
                Vector<Node> v = hp.getElementsByClassName(classes);
                if (!checked) resultSet.addAll(v);
                else resultSet.retainAll(v);
            }
            if (!tag.isEmpty()) {
                Vector<Node> v = hp.getElementsByTagName(tag);
                if (!checked) resultSet.addAll(v);
                else resultSet.retainAll(v);
            }
            applyPseudoClasses(expr);
            return;
        }
        if (parts[pos].equals(">") || parts[pos].equals(" ")) {
            Vector<Node> v = new Vector<Node>();
            boolean checked = false;
            for (int i = 0; i < resultSet.size(); i++) {
                Vector<Node> vs = new Vector<Node>();
                checked = false;
                if (!id.isEmpty()) {
                    vs.add(hp.getElementById(resultSet.get(i), id, parts[pos].equals(" ")));
                    checked = true;
                }
                if (!classes.isEmpty()) {
                    Vector<Node> vx = hp.getElementsByClassName(resultSet.get(i), classes, parts[pos].equals(" "));
                    if (!checked) vs.addAll(vx);
                    else vs.retainAll(vx);
                }
                if (!tag.isEmpty()) {
                    Vector<Node> vx = hp.getElementsByTagName(resultSet.get(i), tag, parts[pos].equals(" "));
                    if (!checked) vs.addAll(vx);
                    else vs.retainAll(vx);
                }
                v.addAll(vs);
            }
            resultSet = v;
            applyPseudoClasses(expr);
        }
    }

    public void printResults() {
        for (int i = 0; i < resultSet.size(); i++) {
            System.out.print("<" + resultSet.get(i).tagName + ">");
            if (i < resultSet.size()-1) System.out.print(", ");
            else System.out.println();
        }
        if (resultSet.size() == 0) {
            System.out.println("Empty set");
        }
    }

    private String[] parts;
    private String query;
    private String orig_query = query;
    private Vector<Node> resultSet;
    HTMLParser hp;
}
