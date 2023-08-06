package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSInt;
import com.alstarsoft.tinybrowser.jsparser.JSFloat;
import com.alstarsoft.tinybrowser.jsparser.JSParser;
import com.alstarsoft.tinybrowser.jsparser.DataView;
import com.alstarsoft.tinybrowser.jsparser.Expression;
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
public class DataViewTest {

    public DataViewTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JSParser jp = new JSParser("var a = new ArrayBuffer(5); a[0] = 3; a[1] = 4; a[2] = 3; a[3] = 2; a[4] = 3; var dataView = new DataView(a)");
        Expression exp = Expression.create(jp.getHead()).eval();
        instance = (DataView) Expression.getVar("dataView", exp);
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of getUInt8 method, of class TypedArray.
     */
    @Test
    public void testGetUInt8() {
        assertEquals(3, instance.getUint8(0).getValue());
        assertEquals(4, instance.getUint8(1).getValue());
        assertEquals(3, instance.getUint8(2).getValue());
    }

    /**
     * Test of setUInt8 method, of class TypedArray.
     */
    @Test
    public void testSetUInt8() {
        instance.setUint8(2, new JSInt(5));
        assertEquals(5, instance.getUint8(2).getValue());
    }

    /**
     * Test of getUInt16 method, of class TypedArray.
     */
    @Test
    public void testGetUInt16() {
        assertEquals(515, instance.getUint16(2).getValue());
        assertEquals(770, instance.getUint16(3).getValue());
    }

    /**
     * Test of setUInt16 method, of class TypedArray.
     */
    @Test
    public void testSetUInt16() {
        instance.setUint16(2, new JSInt(517));
        assertEquals(5, instance.getUint8(2).getValue());
        assertEquals(2, instance.getUint8(3).getValue());
    }

    /**
     * Test of getFloat64 method, od class DataView.
     */
    @Test
    public void testFloat64Array() {
        JSParser jp = new JSParser("var a = new Float64Array(5); a[0] = 1.5; a[1] = 3.75; a[2] = 5; var dataView = new DataView(a.buffer); var x = dataView.getFloat64(0); var y = dataView.getFloat64(8); var z = dataView.getFloat64(16)");
        Expression exp = Expression.create(jp.getHead()).eval();
        assertEquals("1.5", Expression.getVar("x", exp).toString());
        assertEquals("3.75", Expression.getVar("y", exp).toString());
        assertEquals("5.0", Expression.getVar("z", exp).toString());

        jp = new JSParser("var a = new Float64Array(3); a[0] = 1.5; a[1] = 3.75; a[2] = 5; var dataView = new DataView(a.buffer); dataView.setFloat64(0, 0.75); var x = dataView.getFloat64(0); var y = dataView.getFloat64(8); var z = dataView.getFloat64(16)");
        exp = Expression.create(jp.getHead()).eval();
        assertEquals("0.75", Expression.getVar("x", exp).toString());
    }

    /**
     * Test of asBool method, of class TypedArray.
     */
    @Test
    public void testAsBool() {
        assertEquals(true, instance.asBool().getValue());
    }

    /**
     * Test of asInt method, of class TypedArray.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(null, result);
    }

    /**
     * Test of asFloat method, of class TypedArray.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(null, result);
    }

    /**
     * Test of asString method, of class TypedArray.
     */
    @Test
    public void testAsString() {
        assertEquals("[object DataView]", instance.asString().getValue());
    }

    /**
     * Test of getType method, of class TypedArray.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Object");
    }

    /**
     * Test of toString method, of class TypedArray.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private DataView instance;
}
