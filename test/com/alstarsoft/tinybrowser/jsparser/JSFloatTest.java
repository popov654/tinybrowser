package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSInt;
import com.alstarsoft.tinybrowser.jsparser.JSFloat;
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
public class JSFloatTest {

    public JSFloatTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new JSFloat("5.8");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isNaN method, of class JSFloat.
     */
    @Test
    public void testIsNaN() {
        boolean result = instance.isNaN();
        assertEquals(false, result);
    }

    /**
     * Test of asBool method, of class JSFloat.
     */
    @Test
    public void testAsBool() {
        JSFloat zero = new JSFloat("0");
        assertEquals(true, instance.asBool().getValue());
        assertEquals(false, zero.asBool().getValue());
    }

    /**
     * Test of asInt method, of class JSFloat.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(5, result.getValue());
    }

    /**
     * Test of asFloat method, of class JSFloat.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(5.8, result.getValue(), 0.0001);
    }

    /**
     * Test of asString method, of class JSFloat.
     */
    @Test
    public void testAsString() {
        assertEquals("5.8", instance.asString().getValue());
    }

    /**
     * Test of getValue method, of class JSFloat.
     */
    @Test
    public void testGetValue() {
        assertEquals(5.8, instance.getValue(), 0.0001);
    }

    /**
     * Test of getType method, of class JSFloat.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Float");
    }

    /**
     * Test of toString method, of class JSFloat.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private JSFloat instance;
}