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
public class JSObjectTest {

    public JSObjectTest() {
    }

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
     * Test of getType method, of class JSFloat.
     */
    @Test
    public void testGetType() {
        assertEquals(instance.getType(), "Object");
    }

    /**
     * Test of getType method, of class JSFloat.
     */
    @Test
    public void testToString() {
        assertEquals(instance.toString(), "{value: \"value\"}");
        instance.print_proto = true;
        assertEquals(instance.toString(), "{__proto__: ObjectPrototype, value: \"value\"}");
        JSObject.print_protos = true;
        assertEquals(instance.toString(), "{__proto__: {__proto__: ObjectPrototype, hasOwnProperty: {Function}, toString: {Function}, constructor: {Function}}, value: \"value\"}");
        instance.print_proto = false;
        JSObject.print_protos = false;
    }

    private JSObject instance;
}