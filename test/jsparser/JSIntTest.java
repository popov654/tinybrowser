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
public class JSIntTest {

    public JSIntTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new JSInt("5");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isNaN method, of class JSInt.
     */
    @Test
    public void testIsNaN() {
        assertEquals(false, instance.isNaN());
    }

    /**
     * Test of asBool method, of class JSInt.
     */
    @Test
    public void testAsBool() {
        JSInt zero = new JSInt("0");
        assertEquals(true, instance.asBool().getValue());
        assertEquals(false, zero.asBool().getValue());
    }

    /**
     * Test of asInt method, of class JSInt.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(5, result.getValue());
    }

    /**
     * Test of asFloat method, of class JSInt.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(5.0, result.getValue(), 0.0001);
    }

    /**
     * Test of asString method, of class JSInt.
     */
    @Test
    public void testAsString() {
        assertEquals("5", instance.asString().getValue());
    }

    /**
     * Test of getValue method, of class JSInt.
     */
    @Test
    public void testGetValue() {
        assertEquals(5, instance.getValue());
    }

    /**
     * Test of getType method, of class JSInt.
     */
    @Test
    public void testGetType() {
        assertEquals("Integer", instance.getType());
    }

    /**
     * Test of toString method, of class JSInt.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private JSInt instance;
}