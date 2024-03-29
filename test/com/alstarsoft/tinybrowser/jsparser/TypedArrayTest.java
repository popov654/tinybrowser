package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.jsparser.JSArray;
import com.alstarsoft.tinybrowser.jsparser.JSInt;
import com.alstarsoft.tinybrowser.jsparser.JSFloat;
import com.alstarsoft.tinybrowser.jsparser.TypedArray;
import com.alstarsoft.tinybrowser.jsparser.JSParser;
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
public class TypedArrayTest {

    public TypedArrayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JSParser jp = new JSParser("var a = new UInt16Array(5); a[0] = 3; a[1] = 4; a[2] = 5; a[3] = 65536");
        Expression exp = Expression.create(jp.getHead()).eval();
        instance = (TypedArray) Expression.getVar("a", exp);
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of slice method, of class TypedArray.
     */
    @Test
    public void testSlice() {
        JSArray a = instance.slice();
        assertEquals("UInt16Array[3, 4, 5, 0, 0]", a.toString());
        assertFalse(instance.equals(a));
    }

    /**
     * Test of slice method, of class TypedArray.
     */
    @Test
    public void testSlice_JSInt() {
        TypedArray copy = (TypedArray) instance.slice(new JSInt(2));
        assertFalse(copy.buffer.data == instance.buffer.data);
        assertEquals("UInt16Array[5, 0, 0]", copy.toString());
    }

    /**
     * Test of slice method, of class TypedArray.
     */
    @Test
    public void testSlice_JSInt_JSInt() {
        TypedArray copy = (TypedArray) instance.slice(new JSInt(1), new JSInt(3));
        assertFalse(copy.buffer.data == instance.buffer.data);
        assertEquals("UInt16Array[4, 5]", copy.toString());
    }

    /**
     * Test of subArray method, of class TypedArray.
     */
    @Test
    public void testSubarray_JSInt() {
        TypedArray copy = (TypedArray) instance.subArray(new JSInt(2));
        assertTrue(copy.buffer.data == instance.buffer.data);
        assertEquals("UInt16Array[5, 0, 0]", copy.toString());
    }

    /**
     * Test of subArray method, of class TypedArray.
     */
    @Test
    public void testSubarray_JSInt_JSInt() {
        TypedArray copy = (TypedArray) instance.subArray(new JSInt(1), new JSInt(3));
        assertTrue(copy.buffer.data == instance.buffer.data);
        assertEquals("UInt16Array[4, 5]", copy.toString());
    }

    /**
     * Test of set method, of class TypedArray.
     */
    @Test
    public void testSet() {
        JSParser jp = new JSParser("var a = new UInt16Array(5); a[0] = 3; a[1] = 4; a[2] = 5; var b = new UInt8Array(3); b.set(a); var c = new UInt8Array(3); c.set(a, 1);");
        Expression exp = Expression.create(jp.getHead()).eval();
        assertEquals("UInt8Array[3, 4, 5]", Expression.getVar("b", exp).toString());
        assertEquals("UInt8Array[0, 3, 4]", Expression.getVar("c", exp).toString());
    }

    /**
     * Test of buffer property, of class TypedArray.
     */
    @Test
    public void testArrayBuffer() {
        JSParser jp = new JSParser("var a = new UInt16Array(5); a[0] = 3; a[1] = 4; a[2] = 5; var b = new UInt8Array(a.buffer); var c = new UInt8Array(a.buffer, 2, 3)");
        Expression exp = Expression.create(jp.getHead()).eval();
        assertEquals("UInt8Array[3, 0, 4, 0, 5, 0, 0, 0, 0, 0]", Expression.getVar("b", exp).toString());
        assertEquals("UInt8Array[4, 0, 5]", Expression.getVar("c", exp).toString());
    }

    /**
     * Test of float64 TypedArray.
     */
    @Test
    public void testFloat64Array() {
        JSParser jp = new JSParser("var a = new Float64Array(5); a[0] = 1.5; a[1] = 3.75; a[2] = 5; var x = a[0]; var y = a[1]; var z = a[2]");
        Expression exp = Expression.create(jp.getHead()).eval();
        assertEquals("1.5", Expression.getVar("x", exp).toString());
        assertEquals("3.75", Expression.getVar("y", exp).toString());
        assertEquals("5.0", Expression.getVar("z", exp).toString());
    }

    /**
     * Test of clamped TypedArray.
     */
    @Test
    public void testClampedArray() {
        JSParser jp = new JSParser("var a = new UInt8ClampedArray(5); a[0] = 238; a[1] = 260; a[2] = -3");
        Expression exp = Expression.create(jp.getHead()).eval();
        assertEquals("UInt8ClampedArray[238, 255, 0, 0, 0]", Expression.getVar("a", exp).toString());
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
        assertEquals("UInt16Array[3, 4, 5, 0, 0]", instance.asString().getValue());
    }

    /**
     * Test of getType method, of class TypedArray.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Array");
    }

    /**
     * Test of toString method, of class TypedArray.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private TypedArray instance;
}
