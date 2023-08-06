/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.alstarsoft.tinybrowser.cssparser;

import com.alstarsoft.tinybrowser.cssparser.QuerySelector;
import com.alstarsoft.tinybrowser.htmlparser.HTMLParser;
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
public class QuerySelectorTest {

    public QuerySelectorTest() {
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
        hp.setData("<!DOCTYPE html><html><head><title>Test</title></head><body><p>This is a paragraph with a <a href=\"#\" class=\"link red\" id=\"link\">link</a> inside and <span>another</span> <a href=\"#\">one</a>.</p></body></html>");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testQueries() {

        System.out.println("Checking queries:");

        QuerySelector qs = new QuerySelector(".link.red", hp);
        System.out.print("Result of matching .link.red is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector(".link.blue", hp);
        System.out.print("Result of matching .link.blue is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("#link", hp);
        System.out.print("Result of matching #link is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("body .link", hp);
        System.out.print("Result of matching body .link is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);
        
        qs = new QuerySelector("body > .link", hp);
        System.out.print("Result of matching body > .link is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body span.link", hp);
        System.out.print("Result of matching body span.link is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());
        
        qs = new QuerySelector("body #some.link", hp);
        System.out.print("Result of matching body #some.link is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body .link#some", hp);
        System.out.print("Result of matching body .link#some is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body a#some", hp);
        System.out.print("Result of matching body a#some is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body.main a", hp);
        System.out.print("Result of matching body.main a is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body > p", hp);
        System.out.print("Result of matching body > p is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("p", qs.getElements().get(0).tagName);

        qs = new QuerySelector("body a", hp);
        System.out.print("Result of matching body a is ");
        qs.printResults();
        assertEquals(2, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        System.out.print("\n");

        qs = new QuerySelector("body a:first-child", hp);
        System.out.print("Result of matching body a:first-child is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("body a:nth-child(2)", hp);
        System.out.print("Result of matching body a:nth-child(2) is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        qs = new QuerySelector("body:first-child p", hp);
        System.out.print("Result of matching body:first-child p is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());
        

        qs = new QuerySelector("body:last-child p", hp);
        System.out.print("Result of matching body:last-child p is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("p", qs.getElements().get(0).tagName);

        System.out.print("\n");

        qs = new QuerySelector("a[id=\"link\"]", hp);
        System.out.print("Result of matching a[id=\"link\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size(), 1);
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("a[id=\"red\"]", hp);
        System.out.print("Result of matching a[id=\"red\"] is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        System.out.print("\n");

        qs = new QuerySelector("a[id^=\"link\"]", hp);
        System.out.print("Result of matching a[id^=\"link\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("a[id$=\"link\"]", hp);
        System.out.print("Result of matching a[id$=\"link\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        System.out.print("\n");

        qs = new QuerySelector("a[id^=\"li\"]", hp);
        System.out.print("Result of matching a[id^=\"li\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("a[id^=\"nk\"]", hp);
        System.out.print("Result of matching a[id^=\"nk\"] is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size(), 0);

        qs = new QuerySelector("a[id$=\"nk\"]", hp);
        System.out.print("Result of matching a[id$=\"nk\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("a[id$=\"li\"]", hp);
        System.out.print("Result of matching a[id$=\"li\"] is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());
        
        qs = new QuerySelector("a[id~=\"in\"]", hp);
        System.out.print("Result of matching a[id~=\"in\"] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);

        qs = new QuerySelector("a[id~=\"im\"]", hp);
        System.out.print("Result of matching a[id~=\"im\"] is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        System.out.print("\n");

        qs = new QuerySelector(".link[href]", hp);
        System.out.print("Result of matching .link[href] is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);
        assertTrue(qs.getElements().get(0).getAttribute("class").contains("link"));
        
        qs = new QuerySelector(".link[href=\"/home\"]", hp);
        System.out.print("Result of matching .link[href=\"/home\"] is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());

        System.out.print("\n");

        qs = new QuerySelector("body a.link + a", hp);
        System.out.print("Result of matching body a.link + a is ");
        qs.printResults();
        assertEquals(0, qs.getElements().size());
        
        qs = new QuerySelector("body a.link ~ a", hp);
        System.out.print("Result of matching body a.link ~ a is ");
        qs.printResults();
        assertEquals(1, qs.getElements().size());
        assertEquals("a", qs.getElements().get(0).tagName);
        assertEquals("one", qs.getElements().get(0).firstChild().nodeValue);
    }

    private HTMLParser hp;

}