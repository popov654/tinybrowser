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
public class TimerTest {

    public TimerTest() {
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
     * Test of addTimer method, of class Window.
     */
    @Test
    public void testAddRemoveTimer() {
        JSParser jp = new JSParser("var i = 0; function f() { i++; }");
        System.out.println("var i = 0; function f() { i++; }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        Window w = (jsparser.Window)Expression.getVar("window", exp);
        w.addTimer((jsparser.Function)Expression.getVar("f", exp), 1000, true, new Vector<jsparser.JSValue>());
        assertEquals(1, w.getTimers().size());
        assertEquals(1, w.getTimers().get(0).getId());
        w.removeTimer(1);
        assertEquals(0, w.getTimers().size());
    }
}
