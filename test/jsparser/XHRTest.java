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
public class XHRTest {

    public XHRTest() {
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
     * Test of eval method, of class Expression.
     */
    @Test
    public void testXmlHttpRequest() {
        JSParser jp = new JSParser("var xhr = new XMLHttpRequest(); xhr.open(\"GET\", \"https://google.com\", false);  var result = \"\"; xhr.onload = function() { result = this.response.slice(2, 9) }; xhr.send(null)");
        System.out.println("var xhr = new XMLHttpRequest(); xhr.open(\"GET\", \"https://google.com\", false); var result = \"\"; xhr.onload = function() { result = this.response.slice(2, 9) }; xhr.send(null)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"doctype\"", Expression.getVar("result", exp).toString());

        jp = new JSParser("var xhr = new XMLHttpRequest(); xhr.open(\"POST\", \"https://www.w3schools.com/jquery/demo_test_post.asp\", false);  var result = \"\"; xhr.onload = function() { result = this.response }; xhr.send(\"name=John Smith&city=London\")");
        System.out.println("var xhr = new XMLHttpRequest(); xhr.open(\"POST\", \"https://www.w3schools.com/jquery/demo_test_post.asp\", false); var result = \"\"; xhr.onload = function() { result = this.response }; xhr.send(\"name=John Smith&city=London\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"Dear John Smith. Hope you live well in London.\"", Expression.getVar("result", exp).toString());
    }
}
