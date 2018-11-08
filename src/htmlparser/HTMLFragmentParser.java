/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htmlparser;

/**
 *
 * @author Alex
 */
public class HTMLFragmentParser {

    public HTMLFragmentParser(String data) {
        p = new HTMLParser();
        p.setData(data);
        root = p.getRootNode();
    }

    public Node getRootNode() {
        return root;
    }

    private Node root;

    private HTMLParser p;
}
