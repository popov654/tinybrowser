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
public class JSDateTest {

    public JSDateTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        JSParser.setLocale(java.util.Locale.US);
        instance = new JSDate("Sun, Feb 25 2018 18:24:16 GMT+0300");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of length method, of class JSString.
     */
    @Test
    public void testGetValue() {
        System.out.println(instance.getValue());
        assertEquals(1519572256000L, instance.getValue());
    }

    /**
     * Test of slice method, of class JSString.
     */
    @Test
    public void testToString() {
        System.out.println(instance.toString());
        assertEquals("Sun, Feb 25 2018 18:24:16 GMT+0300", instance.toString());
    }

    /**
     * Test of slice method, of class JSString.
     */
    @Test
    public void testCompareTo() {
        assertEquals(true, instance.compareTo(new JSDate()) < 0);
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
    }

    /**
     * Test of asInt method, of class JSString.
     */
    @Test
    public void testAsInt() {
        assertEquals(instance.getValue(), instance.asInt().getValue());
    }

    /**
     * Test of asFloat method, of class JSString.
     */
    @Test
    public void testAsFloat() {
        assertEquals((float)instance.getValue(), instance.asFloat().getValue(), 0.00001);
    }

    /**
     * Test of asString method, of class JSString.
     */
    @Test
    public void testAsString() {
        assertEquals(instance.toString(), instance.asString().getValue());
    }

    /**
     * Test of getType method, of class JSString.
     */
    @Test
    public void testGetType() {
        assertEquals("Object", instance.getType());
    }

    private JSDate instance;
}