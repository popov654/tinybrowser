package jsparser;

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
        JSParser jp = new JSParser("var a = new UInt16Array(); a.push(1); a.push(2); a.push(3); a[3] = 4; a[4] = 5;");
        Expression exp = Expression.create(jp.getHead()).eval();
        instance = (TypedArray) Expression.getVar("a", exp);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of slice method, of class JSFloat.
     */
    @Test
    public void testSlice_JSInt_JSInt() {
        assertEquals("UInt16Array[2, 3]", instance.slice(new JSInt(1), new JSInt(3)).toString());
    }

    /**
     * Test of slice method, of class JSFloat.
     */
    @Test
    public void testSlice() {
        JSArray a = instance.slice();
        assertEquals("UInt16Array[1, 2, 3, 4, 5]", a.toString());
        assertFalse(instance.equals(a));
    }

   /**
     * Test of push method, of class JSFloat.
     */
    @Test
    public void testPush() {
        assertEquals("UInt16Array[1, 2, 3, 4, 5, 6]", instance.slice().push(new JSInt(6)).toString());
    }

   /**
     * Test of pop method, of class JSFloat.
     */
    @Test
    public void testPop() {
        assertEquals("UInt16Array[1, 2, 3, 4]", instance.slice().pop().toString());
    }

       /**
     * Test of shift method, of class JSFloat.
     */
    @Test
    public void testShift() {
        assertEquals("UInt16Array[2, 3, 4, 5]", instance.slice().shift().toString());
    }

   /**
     * Test of unshift method, of class JSFloat.
     */
    @Test
    public void testUnshift() {
        assertEquals("UInt16Array[0, 1, 2, 3, 4, 5]", instance.slice().unshift(new JSInt(0)).toString());
    }

    /**
     * Test of slice method, of class JSFloat.
     */
    @Test
    public void testSlice_JSInt() {
        assertEquals("UInt16Array[3, 4, 5]", instance.slice(new JSInt(2)).toString());
    }

    /**
     * Test of asBool method, of class JSFloat.
     */
    @Test
    public void testAsBool() {
        assertEquals(true, instance.asBool().getValue());
    }

    /**
     * Test of asInt method, of class JSFloat.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(null, result);
    }

    /**
     * Test of asFloat method, of class JSFloat.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(null, result);
    }

    /**
     * Test of asString method, of class JSFloat.
     */
    @Test
    public void testAsString() {
        assertEquals("UInt16Array[1, 2, 3, 4, 5]", instance.asString().getValue());
    }

    /**
     * Test of getType method, of class JSFloat.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Array");
    }

    /**
     * Test of toString method, of class JSFloat.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private JSArray instance;
}
