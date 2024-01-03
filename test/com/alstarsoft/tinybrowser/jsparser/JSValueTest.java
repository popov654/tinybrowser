package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSInt;
import com.alstarsoft.tinybrowser.jsparser.JSFloat;
import com.alstarsoft.tinybrowser.jsparser.JSString;
import com.alstarsoft.tinybrowser.jsparser.JSBool;
import com.alstarsoft.tinybrowser.jsparser.JSValue;
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
public class JSValueTest {

    public JSValueTest() {
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
     * Test of create method, of class JSValue.
     */
    @Test
    public void testCreate() {
        String type1 = "Integer";
        String type2 = "Float";
        String type3 = "Boolean";
        String type4 = "String";
        JSValue val1 = JSValue.create(type1, "3", null);
        JSValue val2 = JSValue.create(type2, "4.5", null);
        JSValue val3 = JSValue.create(type3, "false", null);
        JSValue val4 = JSValue.create(type4, "my string", null);
        assertEquals(type1, val1.getType());
        assertEquals(type2, val2.getType());
        assertEquals(type3, val3.getType());
        assertEquals(type4, val4.getType());
        assertEquals(3, ((JSInt)val1).getValue());
        assertEquals(4.5, ((JSFloat)val2).getValue(), 0.0001);
        assertEquals(false, ((JSBool)val3).getValue());
        assertEquals("my string", ((JSString)val4).getValue());
    }

    /**
     * Test of clone method, of class JSValue.
     */
    @Test
    public void testClone() {
        String type1 = "Integer";
        String type2 = "Float";
        String type3 = "Boolean";
        String type4 = "String";
        JSValue val1 = JSValue.create(type1, "3", null);
        JSValue val2 = JSValue.create(type2, "4.5", null);
        JSValue val3 = JSValue.create(type3, "false", null);
        JSValue val4 = JSValue.create(type4, "my string", null);
        JSValue val1c = val1.clone();
        JSValue val2c = val2.clone();
        JSValue val3c = val3.clone();
        JSValue val4c = val4.clone();
        assertEquals(type1, val1c.getType());
        assertEquals(type2, val2c.getType());
        assertEquals(type3, val3c.getType());
        assertEquals(type4, val4c.getType());
        assertEquals(3, ((JSInt)val1c).getValue());
        assertEquals(4.5, ((JSFloat)val2c).getValue(), 0.0001);
        assertEquals(false, ((JSBool)val3c).getValue());
        assertEquals("my string", ((JSString)val4c).getValue());
    }

}