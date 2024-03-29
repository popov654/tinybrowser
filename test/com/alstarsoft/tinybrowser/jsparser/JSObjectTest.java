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
public class JSObjectTest {

    public JSObjectTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JSParser jp = new JSParser("{ value: \"value\" }");
        instance = (JSObject)Expression.create(jp.getHead()).eval().getValue();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getType method, of class JSObject.
     */
    @Test
    public void testGetType() {
        assertEquals("Object", instance.getType());
    }

    /**
     * Test of entires method, of class JSObject.
     */
    @Test
    public void testEntries() {
        JSParser jp = new JSParser("var obj = { value: \"value\", number: 42 }; var entries = obj.entries()");
        System.out.println("var obj = { value: \"value\", number: 42 }; var entries = obj.entries()");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        JSValue obj = (JSValue) Expression.getVar("entries", exp);
        assertTrue(obj instanceof JSArray);
        assertEquals("[[\"value\", \"value\"], [\"number\", 42]]", ((JSArray)obj).toString());
    }

    /**
     * Test of freeze method, of class JSObject.
     */
    @Test
    public void testFreeze() {
        JSParser jp = new JSParser("var obj = { value: \"value\" }; Object.freeze(obj); obj.test = \"123\"; delete obj.test");
        System.out.println("var obj = { value: \"value\" }; Object.freeze(obj); obj.test = \"123\"; delete obj.test");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        JSObject obj = (JSObject)Expression.getVar("obj", exp);
        assertTrue(obj.isFrozen);
        assertEquals("{value: \"value\"}", obj.toString());
    }

    /**
     * Test of seal method, of class JSObject.
     */
    @Test
    public void testSeal() {
        JSParser jp = new JSParser("var obj = { value: \"value\" }; Object.seal(obj); obj.test = \"123\"; delete obj.test; obj.value = \"123\"");
        System.out.println("var obj = { value: \"value\" }; Object.seal(obj); obj.test = \"123\"; delete obj.test; obj.value = \"123\"");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        JSObject obj = (JSObject)Expression.getVar("obj", exp);
        assertTrue(obj.isSealed);
        assertEquals("{value: \"123\"}", obj.toString());
    }

    /**
     * Test of freeze deepClone, of class JSObject.
     */
    @Test
    public void testDeepClone() {
        JSParser jp = new JSParser("{ value: \"value\", obj: { x: 1, y: 2 }, array: [{ z: 3 }, \"data\"] }");
        JSObject obj = (JSObject)Expression.create(jp.getHead()).eval().getValue();
        JSObject clone = obj.deepClone();
        assertFalse(obj.get("obj").equals(clone.get("obj")));
        assertFalse(obj.get("array").equals(clone.get("array")));
        assertFalse(((JSArray)obj.get("array")).get(0).equals(((JSArray)clone.get("array")).get(0)));
        assertEquals("{value: \"value\", obj: {x: 1, y: 2}, array: [{z: 3}, \"data\"]}", clone.toString());
    }

    /**
     * Test of custom properties.
     */
    @Test
    public void testCustomProperties() {
        JSParser jp = new JSParser("var obj = { x: 1 }; " +
                "Object.defineProperty(obj, 'y', { value: 2, writable: false, configurable: false }); " +
                "console.log('Enumerable: ' + (obj.propertyIsEnumerable('y') ? 'yes' : 'no')); " +
                "delete obj.y; obj.y = 3");
        Expression exp = Expression.create(jp.getHead()).eval();
        JSObject obj = (JSObject) Expression.getVar("obj", exp);
        assertTrue(obj.hasOwnProperty("y"));
        assertTrue(((JSInt)obj.get("y")).getValue() == 2);
    }

    /**
     * Test of getType method, of class JSObject.
     */
    @Test
    public void testToString() {
        assertEquals("{value: \"value\"}", instance.toString());
        instance.print_proto = true;
        assertEquals("{__proto__: ObjectPrototype, value: \"value\"}", instance.toString());
        JSObject.print_protos = true;
        assertEquals("{__proto__: {__proto__: ObjectPrototype, entries: function (), propertyIsEnumerable: function (), hasOwnProperty: function (), toString: function (), constructor: function ()}, value: \"value\"}", instance.toString());
        instance.print_proto = false;
        JSObject.print_protos = false;
    }

    private JSObject instance;
}
