package jsparser;

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
public class JSStringTest {

    public JSStringTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new JSString("My String");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of toLowerCase method, of class JSString.
     */
    @Test
    public void testToLowerCase() {
        assertEquals("my string", instance.toLowerCase().getValue());
    }

    /**
     * Test of toUpperCase method, of class JSString.
     */
    @Test
    public void testToUpperCase() {
        assertEquals("MY STRING", instance.toUpperCase().getValue());
    }

    /**
     * Test of length method, of class JSString.
     */
    @Test
    public void testLength() {
        assertEquals(9, instance.length().getValue());
    }

    /**
     * Test of slice method, of class JSString.
     */
    @Test
    public void testSlice_JSInt() {
        assertEquals("ing", instance.slice(new JSInt(6)).getValue());
    }

    /**
     * Test of slice method, of class JSString.
     */
    @Test
    public void testSplit() {
        assertEquals("String", ((JSString)instance.split(new JSString(" ")).get(1)).getValue());
        assertEquals("S", ((JSString)instance.split(new JSString("")).get(3)).getValue());
    }

        /**
     * Test of slice method, of class JSString.
     */
    @Test
    public void testSlice_JSInt_JSInt() {
        assertEquals("Str", instance.slice(new JSInt(3), new JSInt(6)).getValue());
    }

    /**
     * Test of isNaN method, of class JSString.
     */
    @Test
    public void testIsNaN() {
        assertEquals(false, instance.isNaN());
    }

    /**
     * Test of asBool method, of class JSString.
     */
    @Test
    public void testAsBool() {
        assertEquals(true, instance.asBool().getValue());
        assertEquals(false, new JSString("").asBool().getValue());
    }

    /**
     * Test of asInt method, of class JSString.
     */
    @Test
    public void testAsInt() {
        assertEquals(0, instance.asInt().getValue());
        assertEquals(1, new JSString("1").asInt().getValue());
        assertEquals(1, new JSString("1.5").asInt().getValue());
    }

    /**
     * Test of asFloat method, of class JSString.
     */
    @Test
    public void testAsFloat() {
        assertEquals(0, instance.asInt().getValue());
        assertEquals(1, new JSString("1").asFloat().getValue(), 0.0001);
        assertEquals(1.5, new JSString("1.5").asFloat().getValue(), 0.0001);
    }

    /**
     * Test of asString method, of class JSString.
     */
    @Test
    public void testAsString() {
        assertEquals(instance.getValue(), instance.asString().getValue());
    }

    /**
     * Test of getValue method, of class JSString.
     */
    @Test
    public void testGetValue() {
        assertEquals("My String", instance.getValue());
    }

    /**
     * Test of getType method, of class JSString.
     */
    @Test
    public void testGetType() {
        assertEquals("String", instance.getType());
    }

    /**
     * Test of toString method, of class JSString.
     */
    @Test
    public void testToString() {
        assertEquals("\"My String\"", instance.toString());
    }

    private JSString instance;
}