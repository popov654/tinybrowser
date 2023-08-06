package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSArray;
import com.alstarsoft.tinybrowser.jsparser.JSInt;
import com.alstarsoft.tinybrowser.jsparser.JSFloat;
import com.alstarsoft.tinybrowser.jsparser.JSParser;
import com.alstarsoft.tinybrowser.jsparser.JSValue;
import com.alstarsoft.tinybrowser.jsparser.Expression;
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
public class JSArrayTest {

    public JSArrayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JSParser jp = new JSParser("[1, 2, 3, 4, 5]");
        instance = (JSArray)Expression.create(jp.getHead()).eval().getValue();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of slice method, of class JSArray.
     */
    @Test
    public void testSlice_JSInt_JSInt() {
        assertEquals("[2, 3]", instance.slice(new JSInt(1), new JSInt(3)).toString());
    }

    /**
     * Test of slice method, of class JSArray.
     */
    @Test
    public void testSlice() {
        JSArray a = instance.slice();
        assertEquals("[1, 2, 3, 4, 5]", a.toString());
        assertFalse(instance.equals(a));
    }

    /**
     * Test of splice method, of class JSArray.
     */
    @Test
    public void testSplice() {
        Vector<JSValue> v = new Vector<JSValue>();
        v.add(new JSInt(7));
        JSArray b = instance.splice(new JSInt(1), new JSInt(3), v);
        assertEquals("[2, 3, 4]", b.toString());
        assertEquals("[1, 7, 5]", instance.toString());
    }

   /**
     * Test of push method, of class JSArray.
     */
    @Test
    public void testPush() {
        assertEquals("[1, 2, 3, 4, 5, 6]", instance.slice().push(new JSInt(6)).toString());
    }

   /**
     * Test of pop method, of class JSArray.
     */
    @Test
    public void testPop() {
        assertEquals("[1, 2, 3, 4]", instance.slice().pop().toString());
    }

       /**
     * Test of shift method, of class JSArray.
     */
    @Test
    public void testShift() {
        assertEquals("[2, 3, 4, 5]", instance.slice().shift().toString());
    }

   /**
     * Test of unshift method, of class JSArray.
     */
    @Test
    public void testUnshift() {
        assertEquals("[0, 1, 2, 3, 4, 5]", instance.slice().unshift(new JSInt(0)).toString());
    }

    /**
     * Test of slice method, of class JSArray.
     */
    @Test
    public void testSlice_JSInt() {
        assertEquals("[3, 4, 5]", instance.slice(new JSInt(2)).toString());
    }

    /**
     * Test of asBool method, of class JSArray.
     */
    @Test
    public void testAsBool() {
        assertEquals(true, instance.asBool().getValue());
    }

    /**
     * Test of asInt method, of class JSArray.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(null, result);
    }

    /**
     * Test of asFloat method, of class JSArray.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(null, result);
    }

    /**
     * Test of asString method, of class JSArray.
     */
    @Test
    public void testAsString() {
        assertEquals("[1, 2, 3, 4, 5]", instance.asString().getValue());
    }

    /**
     * Test of freeze deepClone, of class JSArray.
     */
    @Test
    public void testDeepClone() {
        JSParser jp = new JSParser("[{ x: 1 }, \"data\"]");
        JSArray arr = (JSArray)Expression.create(jp.getHead()).eval().getValue();
        JSArray clone = arr.deepClone();
        assertFalse(arr.get(0).equals(clone.get(0)));
        assertEquals("[{x: 1}, \"data\"]", clone.toString());
    }

    /**
     * Test of getType method, of class JSArray.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Array");
    }

    /**
     * Test of toString method, of class JSArray.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private JSArray instance;
}