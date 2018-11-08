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
public class JSBoolTest {

    public JSBoolTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instanceTrue = new JSBool(true);
        instanceFalse = new JSBool(false);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of asBool method, of class JSBool.
     */
    @Test
    public void testAsBool() {
        boolean result1 = instanceTrue.asBool().getValue();
        boolean result2 = instanceFalse.asBool().getValue();
        assertEquals(true, result1);
        assertEquals(false, result2);
    }

    /**
     * Test of asInt method, of class JSBool.
     */
    @Test
    public void testAsInt() {
        int result1 = (int)instanceTrue.asInt().getValue();
        int result2 = (int)instanceFalse.asInt().getValue();
        assertEquals(1, result1);
        assertEquals(0, result2);
    }

    /**
     * Test of asFloat method, of class JSBool.
     */
    @Test
    public void testAsFloat() {
        double result1 = instanceTrue.asFloat().getValue();
        double result2 = instanceFalse.asFloat().getValue();
        assertEquals(1.0, result1, 0);
        assertEquals(0.0, result2, 0);
    }

    /**
     * Test of asString method, of class JSBool.
     */
    @Test
    public void testAsString() {
        String result1 = instanceTrue.asString().getValue();
        String result2 = instanceFalse.asString().getValue();
        assertEquals("true", result1);
        assertEquals("false", result2);
    }

    /**
     * Test of getValue method, of class JSBool.
     */
    @Test
    public void testGetValue() {
        boolean result1 = instanceTrue.getValue();
        boolean result2 = instanceFalse.getValue();
        assertEquals(true, result1);
        assertEquals(false, result2);
    }

    /**
     * Test of getType method, of class JSBool.
     */
    @Test
    public void testGetType() {
        assertEquals("Boolean", instanceTrue.getType());
        assertEquals("Boolean", instanceFalse.getType());
    }

    /**
     * Test of toString method, of class JSBool.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private JSBool instanceTrue;
    private JSBool instanceFalse;
}