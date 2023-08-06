package com.alstarsoft.tinybrowser.jsparser;

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
public class TokenTest {

    public TokenTest() {
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
     * Test of getType method, of class Token.
     */
    @Test
    public void testValueToken() {
        Token t = new Token("5.48");
        assertEquals(Token.VALUE, t.getType());
        t = new Token("5.481458e2");
        assertEquals(Token.VALUE, t.getType());
        t = new Token("+5");
        assertEquals(Token.VALUE, t.getType());
        assertEquals("+5", t.getContent());
        t = new Token("-5");
        assertEquals(Token.VALUE, t.getType());
        assertEquals("-5", t.getContent());
        
    }
    /**
     * Test of getType method, of class Token.
     */
    @Test
    public void testOperatorToken() {
        Token t = new Token("++");
        assertEquals(Token.OP, t.getType());
        assertEquals("++", t.getContent());
        t = new Token("--");
        assertEquals(Token.OP, t.getType());
        assertEquals("--", t.getContent());
        t = new Token("!");
        assertEquals(Token.OP, t.getType());
        assertEquals("!", t.getContent());
        t = new Token("~");
        assertEquals(Token.OP, t.getType());
        assertEquals("~", t.getContent());
        t = new Token("?");
        assertEquals(Token.OP, t.getType());
        assertEquals("?", t.getContent());
        t = new Token(":");
        assertEquals(Token.OP, t.getType());
        assertEquals(":", t.getContent());
        t = new Token(",");
        assertEquals(Token.OP, t.getType());
        assertEquals(",", t.getContent());

        t = new Token(".");
        assertEquals(Token.DOT, t.getType());
        assertEquals(".", t.getContent());
        t = new Token(";");
        assertEquals(Token.SEMICOLON, t.getType());
        assertEquals(";", t.getContent());

        t = new Token("{");
        assertEquals(Token.BLOCK_START, t.getType());
        assertEquals("{", t.getContent());
        t = new Token("{{");
        assertEquals(Token.OBJECT_START, t.getType());
        assertEquals("{{", t.getContent());
        t = new Token("}");
        assertEquals(Token.BLOCK_END, t.getType());
        assertEquals("}", t.getContent());
        t = new Token("}}");
        assertEquals(Token.OBJECT_END, t.getType());
        assertEquals("}}", t.getContent());

        t = new Token("[");
        assertEquals(Token.ARRAY_START, t.getType());
        assertEquals("[", t.getContent());
        t = new Token("]");
        assertEquals(Token.ARRAY_END, t.getType());
        assertEquals("]", t.getContent());
    }

}