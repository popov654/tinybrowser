package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSParser;
import com.alstarsoft.tinybrowser.jsparser.Token;
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
public class JSParserTest {

    public JSParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of class JSParser.
     */
    @Test
    public void test1() {
        String str = "(5 + 3.5 * 2) + \"test\"";
        jp = new JSParser(str);
        System.out.println(str);
        jp.printTokenChain();
        System.out.println();

        Token t = jp.getHead();
        assertEquals(Token.BRACE_OPEN, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("5", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals("+", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("3.5", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals("*", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("2", t.getContent());
        t = t.next;
        assertEquals(Token.BRACE_CLOSE, t.getType());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals("+", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("\"test\"", t.getContent());
    }

    /**
     * Test of of class JSParser.
     */
    @Test
    public void test2() {
        jp = new JSParser("[1, \"one\"] + [2, \"two\"]");
        System.out.println("[1, \"one\"] + [2, \"two\"]");
        jp.printTokenChain();
        System.out.println();

        Token t = jp.getHead();
        assertEquals(Token.ARRAY_START, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("1", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("\"one\"", t.getContent());
        t = t.next;
        assertEquals(Token.ARRAY_END, t.getType());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals("+", t.getContent());
        t = t.next;
        assertEquals(Token.ARRAY_START, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("2", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("\"two\"", t.getContent());
        t = t.next;
        assertEquals(Token.ARRAY_END, t.getType());
    }

    /**
     * Test of of class JSParser.
     */
    @Test
    public void test3() {
        jp = new JSParser("{ a: 10, b: \"str\", c: { x: true } }");
        System.out.println("{ a: 10, b: \"str\", c: { x: true } }");
        jp.printTokenChain();
        System.out.println();

        Token t = jp.getHead();
        assertEquals(Token.OBJECT_START, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("a", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("10", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("b", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("\"str\"", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("c", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.OBJECT_START, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("x", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("true", t.getContent());
        t = t.next;
        assertEquals(Token.OBJECT_END, t.getType());
        t = t.next;
        assertEquals(Token.OBJECT_END, t.getType());
    }

    /**
     * Test of of class JSParser.
     */
    @Test
    public void test4() {
        jp = new JSParser("{ a: 10, b: \"str\", c: [3, 5, 7] }");
        System.out.println("{ a: 10, b: \"str\", c: [3, 5, 7] }");
        jp.printTokenChain();
        System.out.println();

        Token t = jp.getHead();
        assertEquals(Token.OBJECT_START, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("a", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("10", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("b", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("\"str\"", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("c", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(t.getType(), Token.ARRAY_START);
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("3", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("5", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("7", t.getContent());
        t = t.next;
        assertEquals(Token.ARRAY_END, t.getType());
        t = t.next;
        assertEquals(Token.OBJECT_END, t.getType());
    }

    /**
     * Test of of class JSParser.
     */
    @Test
    public void test5() {
        jp = new JSParser("{ a: 10, b: x + 5 }");
        System.out.println("{ a: 10, b: x + 5 }");
        jp.printTokenChain();
        System.out.println();

        Token t = jp.getHead();
        assertEquals(Token.OBJECT_START, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("a", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("10", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        t = t.next;
        assertEquals(Token.FIELD_NAME, t.getType());
        assertEquals("b", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = t.next;
        assertEquals(Token.VAR_NAME, t.getType());
        assertEquals("x", t.getContent());
        t = t.next;
        assertEquals(Token.OP, t.getType());
        assertEquals("+", t.getContent());
        t = t.next;
        assertEquals(Token.VALUE, t.getType());
        assertEquals("5", t.getContent());
        t = t.next;
        assertEquals(Token.OBJECT_END, t.getType());
    }

    private JSParser jp;

}