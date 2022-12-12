/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class NaNTest {

    public NaNTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new NaN();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isNaN method, of class NaN.
     */
    @Test
    public void testIsNaN() {
        assertTrue(instance.isNaN());
    }

    /**
     * Test of asBool method, of class NaN.
     */
    @Test
    public void testAsBool() {
        boolean result = instance.asBool().getValue();
        assertEquals(true, result);
    }

    /**
     * Test of asInt method, of class NaN.
     */
    @Test
    public void testAsInt() {
        JSInt result = instance.asInt();
        assertEquals(null, result);
    }

    /**
     * Test of asFloat method, of class NaN.
     */
    @Test
    public void testAsFloat() {
        JSFloat result = instance.asFloat();
        assertEquals(null, result);
    }

    /**
     * Test of asString method, of class NaN.
     */
    @Test
    public void testAsString() {
        String result = instance.asString().getValue();
        assertEquals("NaN", result);
    }

    /**
     * Test of getType method, of class NaN.
     */
    @Test
    public void testGetType() {
        assertEquals("NaN", instance.getType());
    }

    /**
     * Test of toString method, of class NaN.
     */
    @Test
    public void testToString() {
        testAsString();
    }

    private NaN instance;

}