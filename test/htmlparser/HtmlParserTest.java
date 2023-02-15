/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htmlparser;

import java.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alex
 */
public class HtmlParserTest {

    public HtmlParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        hp = new HTMLParser();
        hp.setData("<!DOCTYPE html><html><head><title>Test</title></head><body><!-- Comment --><p>This is a paragraph with a <a href=\"#\" class=\"link red\" id=\"link\">link</a> inside and <span>another</span> <a href=\"#\">one</a>.</p></body></html>");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void basicTest() {
        System.out.println("----------------------------------");
        hp.printTree();
        System.out.println();
        System.out.println("----------------------------------");

        assertEquals("First child has Comment type", 8, hp.getRootNode().nthElementChild(2).firstChild().nodeType);
        assertEquals("First element child has no previous element sibling", null, hp.getRootNode().firstElementChild().previousElementSibling());
        assertEquals("Last element child has no next element sibling", null, hp.getRootNode().lastElementChild().nextElementSibling());

        String innerHTML = hp.getRootNode().nthElementChild(2).firstElementChild().getInnerHTML();
        System.out.println("Testing innerHTML mehod: " + innerHTML + " [OK]");
        System.out.println();
        assertEquals("Testing innerHTML mehod", "This is a paragraph with a <a href=\"#\" class=\"link red\" id=\"link\">link</a> inside and <span>another</span> <a href=\"#\">one</a>.", innerHTML);

        String outerHTML = hp.getRootNode().nthElementChild(2).firstElementChild().firstElementChild().getOuterHTML();
        System.out.println("Testing outerHTML mehod: " + outerHTML + " [OK]");
        System.out.println();
        assertEquals("Testing outerHTML mehod", "<a href=\"#\" class=\"link red\" id=\"link\">link</a>", outerHTML);
    }

    @Test
    public void basicIndex() {
        System.out.println("Removing first child in <body> element");
        hp.getRootNode().nthElementChild(2).removeChild(1);
        System.out.println("Replacing innerHTML of <p> element");
        hp.getRootNode().nthElementChild(2).firstElementChild().replaceInnerHTML("New paragraph with a <a href=\"#\" class=\"link red\" id=\"link\">link</a> inside and <span>another</span> <a href=\"#\">one</a>.");
        System.out.println("----------------------------------");
        System.out.println();
        hp.printTree();
        System.out.println("\r\nChecking index:");
        System.out.println("First element with class \"link\" has tag <" + hp.getClassIndex().get("link").get(0).tagName + ">");
        System.out.println();
        assertEquals("First child with \"link\" class should be <a> element", "a", hp.getClassIndex().get("link").get(0).tagName);
    }

    @Test
    public void indexTest() {
        Vector<Node> v = null;
        System.out.println();
        
        Node n = hp.getElementById("link");
        assertNotNull("Element with id \"link\" should exist");
        assertEquals("Element with id \"link\" should be <a> element", "a", n.tagName);
        assertNull("Element with id \"block\" should NOT exist", hp.getElementById("block"));

        n.setId("new_link");
        assertNull("Now element with id \"link\" should NOT exist anymore", hp.getElementById("link"));
        
        n.attributes.put("id", "something");
        assertNull("Don't ever do it this way, but at least it should work too", hp.getElementById("new_link"));

        System.out.println("Checking id index: OK");

        v = hp.getElementsByTagName("span");
        assertEquals("There should be exactly one <span>", 1, v.size());

        System.out.println("Checking find element by tag name: OK");

        Node span = v.get(0);

        v = hp.getElementsByName("test");
        assertEquals("There should be nothing with name \"test\"", 0, v.size());

        span.setAttribute("name", "test");
        v = hp.getElementsByName("test");
        assertEquals("There should be one element with name \"test\" now", 1, v.size());

        System.out.println("Checking name index: OK");

        n.setAttribute("class", "red warning");
        v = hp.getElementsByClassName("link");
        assertEquals("There should be no elements with class \"link\" now", 0, v.size());

        v = hp.getElementsByClassName("warning");
        assertEquals("There should be one element with class \"warning\" now", 1, v.size());

        n.removeClass("warning");
        v = hp.getElementsByClassName("warning");
        assertEquals("There should be no elements with class \"warning\" now", 0, v.size());

        n.addClass("red");
        v = hp.getElementsByClassName("red");
        assertEquals("There should be one element with class \"red\" now", 1, v.size());
        assertEquals("There should be no duplicates in index", 1, hp.getClassIndex().get("red").size());

        System.out.println("Checking class index: OK");
    }

    private static HTMLParser hp;
}
