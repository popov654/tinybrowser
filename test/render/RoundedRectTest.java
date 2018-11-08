package render;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
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
public class RoundedRectTest {

    public RoundedRectTest() {
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
     * Test of isNaN method, of class JSFloat.
     */
    @Test
    public void testRoundRect() {
        RoundedRect r = new RoundedRect(0, 0, 200, 80, 10, 0, 0, 10);
        Rectangle2D r1 = new Rectangle2D.Double(2, 3, 80, 60);
        Rectangle2D r2 = new Rectangle2D.Double(2, 4, 80, 60);
        Rectangle2D r3 = new Rectangle2D.Double(2, 5, 80, 60);
        assertFalse(r.contains(r1));
        assertTrue(r.contains(r2));
        assertTrue(r.contains(r3));
        assertTrue(r.intersects(r1));
        assertTrue(r.intersects(r2));
        assertTrue(r.intersects(r3));
        assertFalse(r.contains(new Point2D.Double(2, 3)));
        assertTrue(r.contains(new Point2D.Double(2, 4)));
    }

}
