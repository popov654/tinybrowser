package com.alstarsoft.tinybrowser.jsparser;

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
public class RegExpTest {

    public RegExpTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new RegExp("[a-z]+");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getType method, of class RegExp.
     */
    @Test
    public void testGetType() {
        assertEquals("Object", instance.getType());
    }

    /**
     * Test of test method, of class RegExp.
     */
    @Test
    public void testTest() {
        RegExp regexp = new RegExp("[a-z]+\\s*([a-z]+)?");
        assertTrue(regexp.test("test"));
        assertTrue(regexp.test("test string"));
        assertFalse(regexp.test("test 137"));
        assertFalse(regexp.test("CAPTION"));

        regexp.setFlags("i");
        assertTrue(regexp.test("CAPTION"));

        String result;

        regexp = new RegExp("\\d+$");
        result = regexp.exec("test\n15\ndata").toString();
        System.out.println(result);
        assertEquals("null", result);

        regexp.setFlags("m");
        result = regexp.exec("test\n15\ndata").toString();
        System.out.println(result);
        assertEquals("[\"15\"]", result);
    }

    /**
     * Test of exec method, of class RegExp.
     */
    @Test
    public void testExec() {
        RegExp regexp = new RegExp("[a-z]+\\s*([a-z]+)?");
        String result;

        System.out.println();

        result = regexp.exec("test").toString();
        System.out.println(result);
        assertEquals("[\"test\", null]", result);
        System.out.println(result);
        result = regexp.exec("test string").toString();
        assertEquals("[\"test string\", \"string\"]", result);
        System.out.println(result);
        result = regexp.exec("123").toString();
        System.out.println(result);
        assertEquals("null", result);
    }

    public RegExp instance;
}
