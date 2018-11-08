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
public class JSValueTest {

    public JSValueTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of create method, of class JSValue.
     */
    @Test
    public void testCreate() {
        String type1 = "Integer";
        String type2 = "Float";
        String type3 = "Boolean";
        String type4 = "String";
        JSValue val1 = JSValue.create(type1, "3");
        JSValue val2 = JSValue.create(type2, "4.5");
        JSValue val3 = JSValue.create(type3, "false");
        JSValue val4 = JSValue.create(type4, "my string");
        assertEquals(type1, val1.getType());
        assertEquals(type2, val2.getType());
        assertEquals(type3, val3.getType());
        assertEquals(type4, val4.getType());
        assertEquals(3, ((JSInt)val1).getValue());
        assertEquals(4.5, ((JSFloat)val2).getValue(), 0.0001);
        assertEquals(false, ((JSBool)val3).getValue());
        assertEquals("my string", ((JSString)val4).getValue());
    }

}