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
public class ExpressionTest {

    public ExpressionTest() {
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
    public void testSingleValueInBraces() {
        JSParser jp = new JSParser("(2)");
        System.out.print("(2) = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueIncrementBefore() {
        JSParser jp = new JSParser("++2");
        System.out.print("++2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueIncrementAfter() {
        JSParser jp = new JSParser("2++");
        System.out.print("2++ = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueIncrementBeforeAndAfter() {
        JSParser jp = new JSParser("++2++");
        System.out.print("++2++ = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testIncrementBeforeLiteral() {
        JSParser jp = new JSParser("5 + ++3");
        System.out.print("5 + ++3 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("9", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testIncrementAfterLiteral() {
        JSParser jp = new JSParser("5 + 3++");
        System.out.print("5 + 3++ = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testMuliplicationAndSum() {
        JSParser jp = new JSParser("4 + 1.5 * 2");
        System.out.print("4 + 1.5 * 2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testMuliplicationAndSumWithBraces() {
        JSParser jp = new JSParser("4 * (5 + 2)");
        System.out.print("4 * (5 + 2) = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("28", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testDivisionByZero() {
        JSParser jp = new JSParser("5 + 3 / 0");
        System.out.print("5 + 3 / 0 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Number", exp.getValue().getType());
        assertEquals("+Infinity", exp.getValue().toString());

        jp = new JSParser("5 + 0 / 0");
        System.out.print("5 + 0 / 0 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("NaN", exp.getValue().getType());
        assertEquals("NaN", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testNumberPlusString() {
        JSParser jp = new JSParser("5 + \" apples\"");
        System.out.print("5 + \" apples\" = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("String", exp.getValue().getType());
        assertEquals("5 apples", ((JSString)exp.getValue()).getValue());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testOctBaseNumber() {
        JSParser jp = new JSParser("052");
        System.out.print("052 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("42", exp.getValue().toString());
    }

        /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testHexBaseNumber() {
        JSParser jp = new JSParser("0xAD");
        System.out.print("0xAD = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("173", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueUnaryMinusWithBraces() {
        JSParser jp = new JSParser("(-2)");
        System.out.print("(-2) = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-2", exp.getValue().toString());
    }

        /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueUnaryPlusWithBraces() {
        JSParser jp = new JSParser("(+2)");
        System.out.print("(+2) = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSingleValueUnaryPlus() {
        JSParser jp = new JSParser("+2");
        System.out.print("+2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testUnaryMinus() {
        JSParser jp = new JSParser("5 + -2");
        System.out.print("5 + -2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testUnaryPlus() {
        JSParser jp = new JSParser("7 - +2");
        System.out.print("7 - +2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testUnaryMinusBeforeBraces() {
        JSParser jp = new JSParser("-(5 * 3)");
        System.out.println("-(5 * 3)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-15", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testBitwiseOperators() {
        JSParser jp = new JSParser("3 & 2");
        System.out.print("3 & 2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
        jp = new JSParser("3 | 2");
        System.out.print("3 | 2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("3 ^ 2");
        System.out.print("3 ^ 2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("~7");
        System.out.print("~7 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-8", exp.getValue().toString());
        jp = new JSParser("4 + ~2");
        System.out.print("4 + ~2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testUnaryOperatorInBraces() {
        JSParser jp = new JSParser("5 + (~2)");
        System.out.println("5 + (~2)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
        jp = new JSParser("4 + (!0)");
        System.out.println("4 + (!0)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testLogicalOperators() {
        JSParser jp = new JSParser("5 && 3");
        System.out.print("5 && 3 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("5 || 3");
        System.out.print("5 || 3 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
        jp = new JSParser("0 && 5");
        System.out.print("0 && 5 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("0", exp.getValue().toString());
        jp = new JSParser("0 || 5");
        System.out.print("0 || 5 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());

        jp = new JSParser("5 && 3 || 0 && 5");
        System.out.print("5 && 3 || 0 && 5 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("(5 && 3) || (0 && 5)");
        System.out.print("(5 && 3) || (0 && 5) = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("(5 || 0) && (3 || 5)");
        System.out.print("(5 || 0) && (3 || 5) = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("5 > 3 && 7 - 3 > 0");
        System.out.print("5 > 3 && 7 - 3 > 0 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("a && a.b");
        System.out.println("a && a.b");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
        assertNull(((Block)exp).error);
        
        jp = new JSParser("a || a.b");
        System.out.println("a || a.b");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("null", exp.getValue().toString());
        assertNotNull(((Block)exp).error);
        
        jp = new JSParser("a && a.func()");
        System.out.println("a && a.func()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
        assertNull(((Block)exp).error);

        jp = new JSParser("a || a.func()");
        System.out.println("a || a.func()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("null", exp.getValue().toString());
        assertNotNull(((Block)exp).error);
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testComma() {
        JSParser jp = new JSParser("3, 1, 5");
        System.out.print("3, 1, 5 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
        jp = new JSParser("(2, 4), 3, (1, 5, 7)");
        System.out.print("(2, 4), 3, (1, 5, 7) = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testLogicalNot() {
        JSParser jp = new JSParser("!0");
        System.out.print("!0 is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("!!1");
        System.out.print("!!1 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("!(3 > 5)");
        System.out.print("!(3 > 5) is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testConditionalOperator() {
        JSParser jp = new JSParser("5 > 3 ? 7 - 6 : 8");
        System.out.print("5 > 3 ? 7 - 6 : 8 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("5 < 3 ? 1 > 0 ? 7 - 6 : 3 : 8");
        System.out.print("5 < 3 ? 1 > 0 ? 7 - 6 : 3 : 8 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
        jp = new JSParser("5 < 3 ? (1 > 0 ? 7 - 6 : 3) : 8");
        System.out.print("5 < 3 ? (1 > 0 ? 7 - 6 : 3) : 8 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
        jp = new JSParser("var s = \"string\"; var i = 3; s[5 > 3 ? 0 : 2]");
        System.out.println("var s = \"string\"; var i = 3; s[5 > 3 ? 0 : 2]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("s", ((JSString)exp.getValue()).getValue());
        jp = new JSParser("var test = function(n) { return n+1 } test(5 > 3 ? 9 - 6 : 2)");
        System.out.println("var test = function(n) { return n+1 } test(5 > 3 ? 9 - 6 : 2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("4", exp.getValue().toString());
    }
    
    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testProcessing() {
        JSParser jp = new JSParser("a = 5, b = a + 3");
        System.out.println("a = 5, b = a + 3");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
        jp = new JSParser("5 < 3 ? (4 >= 1 ? a : b) : 8");
        System.out.println("5 < 3 ? (4 >= 1 ? a : b) : 8");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testArrayInitialization() {
        JSParser jp = new JSParser("a = 3, b = [1, 2, a, a+1]");
        System.out.println("a = 3, b = [1, 2, a, a+1]");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 3, 4]", exp.getValue().toString());
    }
    
    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testObjectInitialization() {
        JSParser jp = new JSParser("a = 3, b = { x : a + 7, y: a }");
        System.out.println("a = 3, b = { x : a + 7, y: a }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 10, y: 3}", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testShorthandAssignment() {
        JSParser jp = new JSParser("a = 3; b = { a, c() {} }");
        System.out.println("a = 3; b = { a, c() {} }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{a: 3, c: function () {}}", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testClasses() {
        JSParser jp = new JSParser("class Test { a = 1; b() { return this.a } }; var t = new Test(); t.b()");
        System.out.println("class Test { a = 1; b() { return this.a } }; var t = new Test(); t.b()");
        Expression exp = Expression.create(jp.getHead());
        System.out.println(((jsparser.Function)Expression.getVar("Test", exp)).getBody().scope);
        exp.eval();
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testJSON() {
        JSParser jp = new JSParser("JSON.stringify({ x: 1, y: \"my string\", z: [1, 2, 3] })");
        System.out.println("JSON.stringify({ x: 1, y: \"my string\", z: [1, 2, 3] })");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"{\"x\":1,\"y\":\"my string\",\"z\":[1,2,3]}\"", exp.getValue().toString());
        jp = new JSParser("JSON.parse(\"{ x: 1, y: \\\"my string\\\", z: [1, 2, 3] }\")");
        System.out.println("JSON.parse(\"{ x: 1, y: \\\"my string\\\", z: [1, 2, 3] }\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1, y: \"my string\", z: [1, 2, 3]}", exp.getValue().toString());
        assertEquals("Object", exp.getValue().getType());
        assertEquals("Integer", ((JSObject)exp.getValue()).get("x").getType());
        assertEquals("String", ((JSObject)exp.getValue()).get("y").getType());
        assertEquals("Array", ((JSObject)exp.getValue()).get("z").getType());
        assertEquals(      1, ((JSInt)((JSObject)exp.getValue()).get("x")).getValue());
        assertEquals("my string", ((JSString)((JSObject)exp.getValue()).get("y")).getValue());
        assertEquals(      3, ((JSArray)((JSObject)exp.getValue()).get("z")).length().getValue());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testDate() {
        JSParser jp = new JSParser("Date(2015, 11, 28, 0, 15, 38)");
        System.out.println("Date(2015, 11, 28, 0, 15, 38)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Mon, Dec 28 2015 00:15:38 GMT+0300", exp.getValue().toString());
        jp = new JSParser("Date(\"Mon, Feb 26 2018 02:48:12 GMT+0300\").toGMTString()");
        System.out.println("Date(\"Mon, Feb 26 2018 02:48:12 GMT+0300\").toGMTString()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"Sun, Feb 25 2018 23:48:12 GMT\"", exp.getValue().toString());
        jp = new JSParser("Date(\"Mon, Feb 26 2018 02:48:12 GMT+0300\").toISOString()");
        System.out.println("Date(\"Mon, Feb 26 2018 02:48:12 GMT+0300\").toISOString()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"2018-02-25T23:48:12.000Z\"", exp.getValue().toString());
        jp = new JSParser("+new Date(\"Sun, Feb 25 2018 23:48:12 GMT\")");
        System.out.println("+new Date(\"Sun, Feb 25 2018 23:48:12 GMT\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1519591692000", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testConsole() {
        JSParser jp = new JSParser("console.log(\"Test\")");
        System.out.println("console.log(\"Test\")");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals(1, ((Block)exp).getConsole().getData().size());
        assertEquals("Test", ((Block)exp).getConsole().getData().get(0));
        jp = new JSParser("console.clear()");
        System.out.println("console.clear()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals(0, ((Block)exp).getConsole().getData().size());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testAccessProperties() {
        JSParser jp = new JSParser("[1, 2, 3][2]");
        System.out.println("[1, 2, 3][2]");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("{ a: 1 }.a");
        System.out.println("{ a: 1 }.a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("{ a: 1 }[\"a\"]");
        System.out.println("{ a: 1 }[\"a\"]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("var s = \"string\"; s[3]");
        System.out.println("var s = \"string\"; s[3]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("i", ((JSString)exp.getValue()).getValue());
        jp = new JSParser("var s = \"string\"; var i = 3; s[i-1]");
        System.out.println("var s = \"string\"; var i = 3; s[i-1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("r", ((JSString)exp.getValue()).getValue());
        jp = new JSParser("[1, 2, 3][0] + [1, 2, 3][1]");
        System.out.println("[1, 2, 3][0] + [1, 2, 3][1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("a = [1, 2, 3]; a[1] + a[2]");
        System.out.println("a = [1, 2, 3]; a[1] + a[2]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
        jp = new JSParser("a = [1, 2, 3]; a[1] = 5");
        System.out.println("a = [1, 2, 3]; a[1] = 5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", ((JSArray)Expression.getVar("a", exp)).get(1).toString());
        jp = new JSParser("a = [1, [2, 3, 4], 5]; a[1][1]");
        System.out.println("a = [1, [2, 3, 4], 5]; a[1][1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("a = [1, [2, 3, 4], 5]; a[1][1] = 1; a[1]");
        System.out.println("a = [1, [2, 3, 4], 5]; a[1][1] = 1; a[1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[2, 1, 4]", ((JSArray)Expression.getVar("a", exp)).get(1).toString());
        jp = new JSParser("d = { a: [3, 5, 9] }; d.a[1] = 7; d");
        System.out.println("d = { a: [3, 5, 9] }; d.a[1] = 7; d");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{a: [3, 7, 9]}", exp.getValue().toString());
        jp = new JSParser("a = [1, 5, 9]; b = [0, 2]; a[b[1]]");
        System.out.println("a = [1, 5, 9]; b = [0, 2]; a[b[1]]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("9", exp.getValue().toString());
        jp = new JSParser("a = [1, 5, 9]; b = [0, 2]; a[b[0]] + a[b[1]]");
        System.out.println("a = [1, 5, 9]; b = [0, 2]; a[b[0]] + a[b[1]]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("10", exp.getValue().toString());
        jp = new JSParser("var a = { b: 3, c: 4 }; (a.b) + (a.c)");
        System.out.println("var a = { b: 3, c: 4 }; (a.b) + (a.c)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", exp.getValue().toString());
        jp = new JSParser("var a = { b: \"c\" }; var x = { c: \"value\" }; x[a.b]");
        System.out.println("var a = { b: \"c\" }; var x = { c: \"value\" }; x[a.b]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"value\"", exp.getValue().toString());
        jp = new JSParser("var a = { b: \"c\" }; var x = { c: \"value\" }; x.(a.b)");
        System.out.println("var a = { b: \"c\" }; var x = { c: \"value\" }; x.(a.b)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"value\"", exp.getValue().toString());
        jp = new JSParser("var a = { b: null }; a.b?.c");
        System.out.println("var a = { b: null }; a.b?.c");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
        jp = new JSParser("var a = { b: null }; a.b?.['c']");
        System.out.println("var a = { b: null }; a.b?.['c']");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testArrays() {
        JSParser jp = new JSParser("[1, \"one\"] + [2, \"two\"]");
        System.out.print("[1, \"one\"] + [2, \"two\"] = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[1, \"one\", 2, \"two\"]", exp.getValue().toString());
        jp = new JSParser("[1, [2, 3]]");
        System.out.print("[1, [2, 3]] = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[1, [2, 3]]", exp.getValue().toString());
        jp = new JSParser("Array(3, 5)");
        System.out.println("Array(3, 5)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[3, 5]", exp.getValue().toString());
        jp = new JSParser("Array(1, 2, 3).concat([4, 5])");
        System.out.println("Array(1, 2, 3).concat([4, 5])");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[1, 2, 3, 4, 5]", exp.getValue().toString());
        jp = new JSParser("Array(1, 2, 3).fill(0)");
        System.out.println("Array(1, 2, 3).fill(0)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[0, 0, 0]", exp.getValue().toString());
        jp = new JSParser("Array(5, 3, 1).sort()");
        System.out.println("Array(5, 3, 1).sort()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[1, 3, 5]", exp.getValue().toString());
        jp = new JSParser("Array(1, 2, 3).reverse()");
        System.out.println("Array(1, 2, 3).reverse()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[3, 2, 1]", exp.getValue().toString());
        jp = new JSParser("Array(5, 3, 8, 1).slice(1, 3)");
        System.out.println("Array(5, 3, 8, 1).slice(1, 3)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Array", exp.getValue().getType());
        assertEquals("[3, 8]", exp.getValue().toString());
        jp = new JSParser("Array(5, 3, 8, 1).join('-')");
        System.out.println("Array(5, 3, 8, 1).join('-')");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("String", exp.getValue().getType());
        assertEquals("\"5-3-8-1\"", exp.getValue().toString());

        jp = new JSParser("var a = [1, 2, 3]; function inc(a) { for (var i = 0; i < a.length; i++) ++a[i]; return a }; inc(a)");
        System.out.println("var a = [1, 2, 3]; function inc(a) { for (var i = 0; i < a.length; i++) ++a[i]; return a }; inc(a)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[2, 3, 4]", Expression.getVar("a", exp).toString());
        jp = new JSParser("var a = [1, 2, 3]; a.map(function(i) { return i+1 })");
        System.out.println("var a = [1, 2, 3]; a.map(function(i) { return i+1 })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 3]", Expression.getVar("a", exp).toString());
        assertEquals("[2, 3, 4]", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.filter(function(i) { return i % 2 == 0 })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.filter(function(i) { return i % 2 == 0 })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 3, 4, 5]", Expression.getVar("a", exp).toString());
        assertEquals("[2, 4]", exp.getValue().toString());

        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("15", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b }, 8)");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b }, 8)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("23", exp.getValue().toString());
        jp = new JSParser("var a = []; a.reduce(function(a, b) { return a + b })");
        System.out.println("var a = []; a.reduce(function(a, b) { return a + b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[]", exp.getValue().toString());
        
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a + b })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a + b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("15", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a - b })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a - b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-5", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a - b }, 6)");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduceRight(function(a, b) { return a - b }, 6)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-9", exp.getValue().toString());
        jp = new JSParser("var a = []; a.reduceRight(function(a, b) { return a - b })");
        System.out.println("var a = []; a.reduceRight(function(a, b) { return a - b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[]", exp.getValue().toString());

        jp = new JSParser("var a = [1, 2, 3]; a.push(4)");
        System.out.println("var a = [1, 2, 3]; a.push(4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 3, 4]", Expression.getVar("a", exp).toString());
        assertEquals("4", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4]; a.pop()");
        System.out.println("var a = [1, 2, 3, 4]; a.pop()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 3]", Expression.getVar("a", exp).toString());
        assertEquals("4", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3]; a.unshift(0)");
        System.out.println("var a = [1, 2, 3]; a.unshift(0)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[0, 1, 2, 3]", Expression.getVar("a", exp).toString());
        assertEquals("4", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4]; a.shift()");
        System.out.println("var a = [1, 2, 3, 4]; a.shift()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[2, 3, 4]", Expression.getVar("a", exp).toString());
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.splice(1, 3)");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.splice(1, 3)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 5]", Expression.getVar("a", exp).toString());
        assertEquals("[2, 3, 4]", exp.getValue().toString());
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.splice(1, 3, 9)");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.splice(1, 3, 9)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 9, 5]", Expression.getVar("a", exp).toString());
        assertEquals("[2, 3, 4]", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testStrings() {
        JSParser jp = new JSParser("String(\"abc\")");
        System.out.println("String(\"abc\")");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("String", exp.getValue().getType());
        assertEquals("\"abc\"", exp.getValue().toString());
        jp = new JSParser("String(\"abc\").charAt(1)");
        System.out.println("String(\"abc\").charAt(1)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"b\"", exp.getValue().toString());
        jp = new JSParser("String(\"Hello\").toUpperCase()");
        System.out.println("String(\"Hello\").toUpperCase()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"HELLO\"", exp.getValue().toString());
        jp = new JSParser("String(\"Hello\").toLowerCase()");
        System.out.println("String(\"Hello\").toLowerCase()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"hello\"", exp.getValue().toString());
        jp = new JSParser("String(\"Hello World\").split(\" \")[1]");
        System.out.println("String(\"Hello World\").split(\" \")[1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"World\"", exp.getValue().toString());
        
        jp = new JSParser("\" \\\"\\\" \"");
        System.out.println("\" \\\"\\\" \"");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\" \"\" \"", exp.getValue().toString());
        jp = new JSParser("\" \\\\ \"");
        System.out.println("\" \\\\ \"");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\" \\ \"", exp.getValue().toString());
        jp = new JSParser("\"[{}]\"");
        System.out.println("\"[{}]\"");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"[{}]\"", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testObjects() {
        JSParser jp = new JSParser("{ \"one\": 1, \"two\": 2 }");
        System.out.print("{ \"one\": 1, \"two\": 2 } = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Object", exp.getValue().getType());
        assertEquals("{one: 1, two: 2}", exp.getValue().toString());
        jp = new JSParser("{ \"one\": 1, \"two\": [2, 3] }");
        System.out.print("{ \"one\": 1, \"two\": [2, 3] } = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Object", exp.getValue().getType());
        assertEquals("{one: 1, two: [2, 3]}", exp.getValue().toString());
        jp = new JSParser("{ \"name\": \"User\", \"info\": {\"login\": \"username\", \"password\": \"*****\"}}");
        System.out.print("{ \"name\": \"User\", \"info\": {\"login\": \"username\", \"password\": \"*****\"}} = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Object", exp.getValue().getType());
        assertEquals("{name: \"User\", info: {login: \"username\", password: \"*****\"}}", exp.getValue().toString());
        jp = new JSParser("{ x: 1 }.hasOwnProperty(\"x\")");
        System.out.println("{ x: 1 }.hasOwnProperty(\"x\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("{ x: 1 }.hasOwnProperty(\"toString\")");
        System.out.println("{ x: 1 }.hasOwnProperty(\"toString\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1 }; Object.freeze(obj); obj.y = 2");
        System.out.println("var obj = { x: 1 }; Object.freeze(obj); obj.y = 2");
        exp = Expression.create(jp.getHead());
        exp.eval();
        JSValue val = ((JSObject) Expression.getVar("obj", exp)).get("y");
        assertEquals(val, Undefined.getInstance());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testInOperator() {
        JSParser jp = new JSParser("var obj = { x: 5 }; \"x\" in obj");
        System.out.println("var obj = { x: 5 }; \"x\" in obj");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 5 }; \"y\" in obj");
        System.out.println("var obj = { x: 5 }; \"y\" in obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testForInCycle() {
        JSParser jp = new JSParser("var a = [1, 2, 3]; for (var i in a) { i }");
        System.out.println("var a = [1, 2, 3]; for (var i in a) { i }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{\n  var a = [1, 2, 3];\n  for (var i in a) {\n    i;\n  }\n}", exp.toString());
        assertEquals("2", exp.getValue().toString());
        assertEquals("2", Expression.getVar("i", exp).toString());
        jp = new JSParser("var obj = { x: 1, y: 2 }; for (let i in obj) { i }");
        System.out.println("var obj = { x: 1, y: 2 }; for (let i in obj) { i }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{\n  var obj = {x: 1, y: 2}\n  for (let i in obj) {\n    i;\n  }\n}", exp.toString());
        assertEquals("\"y\"", exp.getValue().toString());
        assertEquals("undefined", Expression.getVar("i", exp).toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testReusableExpressions() {
        JSParser jp = new JSParser("i++");
        int n = 3;
        System.out.println("i++ (3 times)");
        Expression exp = Expression.create(jp.getHead());
        exp.setReusable(true);
        Expression.setVar("i", jsparser.JSValue.create("Integer", "0"), exp, 0);
        for (int i = 0; i < n; i++) exp.eval();
        assertEquals("2", exp.getValue().toString());
        assertEquals("3", Expression.getVar("i", exp).toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testWindowObject() {
        JSParser jp = new JSParser("var i = 0; window.j = 1");
        System.out.println("var i = 0; window.j = 1");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", Expression.getVar("j", exp).toString());
        assertEquals("Object", Expression.getVar("window", exp).getType());
        assertEquals("0", ((JSObject)Expression.getVar("window", exp)).get("i").toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testFunctions() {
        JSParser jp = new JSParser("test = function(n) { return n+1 } test(4)");
        System.out.println("test = function(n)  { return n+1 } test(4)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
        jp = new JSParser("test = function() { return } test()");
        System.out.println("test = function()  { return } test()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
        jp = new JSParser("var f = (x => x * x); f(2)");
        System.out.println("var f = (x => x * x); f(2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("4", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1, f: () => this.x = 2 }; obj.f(); obj.x");
        System.out.println("var obj = { x: 1, f: () => this.x = 2 }; obj.f(); obj.x");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("var f = Function(); f.name");
        System.out.println("var f = Function(); f.name");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"anonymous\"", exp.getValue().toString());
        jp = new JSParser("var f = Function(\"a\", \"return a*5\"); f(2)");
        System.out.println("var f = Function(\"a\", \"return a*5\"); f(2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("10", exp.getValue().toString());
        jp = new JSParser("function() { return 3; }()");
        System.out.println("function() { return 3; }()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("function func(x) { \"test\" }");
        System.out.println("function func(x) { \"test\" }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", ((Function)Expression.getVar("func", exp)).get("length").toString());
        assertEquals("func", ((JSString)((Function)Expression.getVar("func", exp)).get("name")).getValue());
        jp = new JSParser("function() { return [3, 4, 5]; }()[2]");
        System.out.println("function() { return [3, 4, 5]; }()[2]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1, f: function(n) { this.x = n; } }; obj.f(3); obj.x");
        System.out.println("var obj = { x: 1, f: function(n) { this.x = n; } }; obj.f(3); obj.x");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; f(3); x");
        System.out.println("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; f(3); x");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        assertEquals("3", Expression.getVar("x", exp).toString());
        jp = new JSParser("var obj = { x: 1, f: function(n) { if (n) this.x = n; } }; obj.f()");
        System.out.println("var obj = { x: 1, f: function(n) { if (n) this.x = n; } }; obj.f()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        assertEquals("undefined", Expression.getVar("x", exp).toString());
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.call(obj, 3, 4)");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.call(obj, 3, 4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.apply(obj, [3, 4])");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.apply(obj, [3, 4])");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f = f.bind(obj, 3); f(4)");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f = f.bind(obj, 3); f(4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.bind(obj, 3).bind(obj, 4)()");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.bind(obj, 3).bind(obj, 4)()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", ((JSObject)Expression.getVar("obj", exp)).get("x").toString());
        
        jp = new JSParser("Number(\"3.75\")");
        System.out.println("Number(\"3.75\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3.75", exp.getValue().toString());
        jp = new JSParser("var n = 3.754; n.toPrecision(2)");
        System.out.println("var n = 3.754; n.toPrecision(2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3.75", exp.getValue().toString());
        jp = new JSParser("var n = 3.754; n.toPrecision(1)");
        System.out.println("var n = 3.754; n.toPrecision(1)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3.8", exp.getValue().toString());

        jp = new JSParser("Array(String(\"1\"), String(\"2\"))");
        System.out.println("Array(String(\"1\"), String(\"2\"))");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[\"1\", \"2\"]", exp.getValue().toString());
        jp = new JSParser("Array(new String(\"1\"), new String(\"2\"))");
        System.out.println("Array(new String(\"1\"), new String(\"2\"))");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[\"1\", \"2\"]", exp.getValue().toString());
    }
    
   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testConstructorMode() {
        JSParser jp = new JSParser("function f() { this.x = 1 }; var obj = new f()");
        System.out.println("function f() { this.x = 1 }; var obj = new f()");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1}", exp.getValue().toString());
        jp = new JSParser("function f() { this.x = 1 }; var p = { y: 2 }; f.prototype = p; var obj = new f()");
        System.out.println("function f() { this.x = 1 }; var p = { y: 2 }; f.prototype = p; var obj = new f()");
        exp = Expression.create(jp.getHead());
        exp.setSilent(true);
        exp.eval();
        ((jsparser.JSObject)exp.getValue()).print_proto = true;
        System.out.println(exp.getValue());
        //assertEquals("{__proto__: {y: 2, constructor: {Function}}, x: 1}", exp.getValue().toString());
        ((jsparser.JSObject)exp.getValue()).print_proto = false;
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testYield() {
        JSParser jp = new JSParser("yield 1; yield 2; return 3");
        System.out.println("yield 1; yield 2; return 3");
        Expression exp = Expression.create(jp.getHead());
        ((jsparser.Block)exp).is_gen = true;
        for (int i = 1; i <= 3; i++) {
            exp.eval();
            assertEquals(String.valueOf(i), exp.getValue().toString());
            System.out.println("Done: " + ((jsparser.Block)exp).done);
        }
        jp = new JSParser("let a = yield 1; return a*2");
        System.out.println("let a = yield 1; return a*2");
        exp = Expression.create(jp.getHead());
        ((jsparser.Block)exp).is_gen = true;
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        System.out.println("Done: " + ((jsparser.Block)exp).done);
        ((Block)exp).yt_value = new JSInt(5);
        ((Block)exp).state = Block.NORMAL;
        exp.eval();
        assertEquals("10", exp.getValue().toString());
        System.out.println("Done: " + ((jsparser.Block)exp).done);
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testGenerators() {
        JSParser jp = new JSParser("function* gen() { yield 1; yield 2; return 3 } var g = gen(); g.next()");
        System.out.println("function* gen() { yield 1; yield 2; return 3 } var g = gen(); g.next()");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{value: 1, done: false}", exp.getValue().toString());
        jp = new JSParser("function* gen() { var x = yield 1; yield x + 1; return 3 } var g = gen(); g.next(); g.next()");
        System.out.println("function* gen() { var x = yield 1; yield x + 1; return 3 } var g = gen(); g.next(); g.next()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{value: 2, done: false}", exp.getValue().toString());
        jp = new JSParser("function* gen() { yield 1; yield 2; yield* gen2(); return 3 } function* gen2() { yield 4; yield 5 } var g = gen(); var a = []; for (var i = 0; i < 5; i++) a.push(g.next().value); a");
        System.out.println("function* gen() { yield 1; yield 2; yield* gen2(); return 3 } function* gen2() { yield 4; yield 5 } var g = gen(); var a = []; for (var i = 0; i < 5; i++) a.push(g.next().value); a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("[1, 2, 4, 5, 3]", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testTryCatchThrow() {
        JSParser jp = new JSParser("try { [1, 2, 3].map(null) } catch() { \"Caught!\" }");
        System.out.println("try { [1, 2, 3].map(null) } catch() { \"Caught!\" }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"Caught!\"", exp.getValue().toString());
        jp = new JSParser("try { [1, 2, 3].map(null) } catch() { \"Something weird happened\" } finally { 3 }");
        System.out.println("try { [1, 2, 3].map(null) } catch() { \"Something weird happened\" } finally { 3 }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("try { [1, 2, 3].map(null) } catch() { throw { value: \"My exception\" }; \"Something\" } catch (e) { e.value }");
        System.out.println("try { [1, 2, 3].map(null) } catch() { throw { value: \"My exception\" }; \"Something\" } catch (e) { e.value }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"My exception\"", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    public void testWindowFunctions() {
        JSParser jp = new JSParser("var n = parseInt(\"25\")");
        System.out.println("var n = parseInt(\"25\")");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Integer", Expression.getVar("n", exp).getType());
        assertEquals("25", Expression.getVar("n", exp).toString());
        jp = new JSParser("var n = parseFloat(\"7.4\")");
        System.out.println("var n = parseFloat(\"7.4\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Float", Expression.getVar("n", exp).getType());
        assertEquals("7.4", Expression.getVar("n", exp).toString());
        jp = new JSParser("eval(\"function test() {}\")");
        System.out.println("eval(\"function test() {}\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("Function", Expression.getVar("test", exp).getType());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testMathFunctions() {
        JSParser jp = new JSParser("Math.round(2.5)");
        System.out.println("Math.round(2.5)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("Math.ceil(2.5)");
        System.out.println("Math.ceil(2.5)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("Math.floor(2.5)");
        System.out.println("Math.floor(2.5)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
        jp = new JSParser("Math.abs(-2.75)");
        System.out.println("Math.abs(-2.75)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2.75", exp.getValue().toString());
        jp = new JSParser("Math.pow(2, 3)");
        System.out.println("Math.pow(2, 3)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
        jp = new JSParser("3 - Math.sin(Math.PI / 2 - 0)");
        System.out.println("3 - Math.sin(Math.PI / 2 - 0)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
        jp = new JSParser("Math.sin(Math.PI)");
        System.out.println("Math.sin(Math.PI)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("0", exp.getValue().toString());
        jp = new JSParser("Math.cos(Math.PI / 2)");
        System.out.println("Math.cos(Math.PI / 2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("0", exp.getValue().toString());
        jp = new JSParser("Math.cos(Math.PI)");
        System.out.println("Math.cos(Math.PI)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-1", exp.getValue().toString());
        jp = new JSParser("Math.tan(Math.PI / 2)");
        System.out.println("Math.tan(Math.PI / 2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("+Infinity", exp.getValue().toString());
        jp = new JSParser("Math.tan(-Math.PI / 2)");
        System.out.println("Math.tan(-Math.PI / 2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-Infinity", exp.getValue().toString());
        jp = new JSParser("Math.tan(Math.PI / 4)");
        System.out.println("Math.tan(Math.PI / 4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1", exp.getValue().toString());
        jp = new JSParser("Math.tan(-Math.PI / 4)");
        System.out.println("Math.tan(-Math.PI / 4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-1", exp.getValue().toString());

        jp = new JSParser("var a = 3; a.isFinite()");
        System.out.println("var a = 3; a.isFinite()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("var a = Number.POSITIVE_INFINITY; a.isFinite()");
        System.out.println("var a = Number.POSITIVE_INFINITY; a.isFinite()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());

        jp = new JSParser("Number.MAX_SAFE_INTEGER");
        System.out.println("Number.MAX_SAFE_INTEGER");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("9007199254740991", exp.getValue().toString());
        jp = new JSParser("Number.MAX_VALUE");
        System.out.println("Number.MAX_VALUE");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("1.7976931348623157E308", exp.getValue().toString());

        jp = new JSParser("Number.POSITIVE_INFINITY");
        System.out.println("Number.POSITIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("+Infinity", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY");
        System.out.println("Number.NEGATIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-Infinity", exp.getValue().toString());

        jp = new JSParser("Number.NEGATIVE_INFINITY + 5");
        System.out.println("Number.NEGATIVE_INFINITY + 5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-Infinity", exp.getValue().toString());
        jp = new JSParser("Number.POSITIVE_INFINITY - 5");
        System.out.println("Number.POSITIVE_INFINITY - 5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("+Infinity", exp.getValue().toString());
        jp = new JSParser("Number.POSITIVE_INFINITY * 2.5");
        System.out.println("Number.POSITIVE_INFINITY * 2.5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("+Infinity", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY / 2.5");
        System.out.println("Number.NEGATIVE_INFINITY / 2.5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("-Infinity", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY * Number.NEGATIVE_INFINITY");
        System.out.println("Number.NEGATIVE_INFINITY * Number.NEGATIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("+Infinity", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY / Number.POSITIVE_INFINITY");
        System.out.println("Number.NEGATIVE_INFINITY / Number.POSITIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("NaN", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY + Number.POSITIVE_INFINITY");
        System.out.println("Number.NEGATIVE_INFINITY + Number.POSITIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("0", exp.getValue().toString());
        jp = new JSParser("Number.NEGATIVE_INFINITY - Number.NEGATIVE_INFINITY");
        System.out.println("Number.NEGATIVE_INFINITY - Number.NEGATIVE_INFINITY");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("0", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testClosures() {
        JSParser jp = new JSParser("var f = function(x) { return function() { window.x = 5 + x } }; f(3)()");
        System.out.println("var f = function(x) { return function() { window.x = 5 + x } }; f(3)()");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", Expression.getVar("x", exp).toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testIf() {
        JSParser jp = new JSParser("if (5 > 3) 5 + 3");
        System.out.println("if (5 > 3) 5 + 3");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("8", exp.getValue().toString());
        jp = new JSParser("if (5 != 5) 5 + 3; else 3");
        System.out.println("if (5 != 5) 5 + 3; else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("3", exp.getValue().toString());
        jp = new JSParser("if (5 == 5) { a = 5 + 3; a * 3 } else 3");
        System.out.println("if (5 == 5) { a = 5 + 3; a * 3 } else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("24", exp.getValue().toString());
        jp = new JSParser("if (5 > 5) 1; else if (5 <= 5) 2; else 3");
        System.out.println("if (5 > 5) 1; else if (5 <= 5) 2; else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("2", exp.getValue().toString());
        jp = new JSParser("if (5 == 5) { if (false) \"ok\"; else \"not ok\" }");
        System.out.println("if (5 == 5) { if (false) \"ok\"; else \"not ok\" }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"not ok\"", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testSwitch() {
        JSParser jp = new JSParser("switch(3) { case 1: \"one\"; break; }");
        System.out.println("switch(3) { case 1: \"one\"; break; }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("null", exp.getValue().toString());
        jp = new JSParser("switch(3) { case 3: \"three\"; break; }");
        System.out.println("switch(3) { case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"three\"", exp.getValue().toString());
        jp = new JSParser("switch(1) { case 1: \"one\"; break; case 3: \"three\"; break; }");
        System.out.println("switch(1) { case 1: \"one\"; break; case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"one\"", exp.getValue().toString());
        jp = new JSParser("switch(2) { case 1: \"one\"; break; case 2: \"two\"; case 3: \"three\"; break; }");
        System.out.println("switch(2) { case 1: \"one\"; break; case 2: \"two\"; case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"three\"", exp.getValue().toString());
    }

   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testCycles() {
        JSParser jp = new JSParser("for (i = 0; i < 3; i++) { \"str\" + (i+1) }");
        System.out.println("for (i = 0; i < 3; i++) { \"str\" + (i+1) }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"str3\"", exp.getValue().toString());
        assertEquals("3", Expression.getVar("i", exp).toString());
        
        jp = new JSParser("var i = 0; while (i < 3) { \"str\" + (++i); }");
        System.out.println("var i = 0; while (i < 3) { \"str\" + (++i); }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"str3\"", exp.getValue().toString());
        assertEquals("3", Expression.getVar("i", exp).toString());

        jp = new JSParser("var i = 0; for ( ; i < 3 ; ) { \"str\" + (++i); }");
        System.out.println("var i = 0; for ( ; i < 3 ; ) { \"str\" + (++i); }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"str3\"", exp.getValue().toString());

        jp = new JSParser("var i = 0; do { \"str\" + (++i); } while (i > 1)");
        System.out.println("var i = 0; do { \"str\" + (++i); } while (i > 1)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"str1\"", exp.getValue().toString());
        
        jp = new JSParser("var i = 0; do { \"str\" + (++i); } while (i < 2)");
        System.out.println("var i = 0; do { \"str\" + (++i); } while (i < 2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"str2\"", exp.getValue().toString());

        jp = new JSParser("var s = 0; for (i = 1; i <= 5; i++) s += i");
        System.out.println("var s = 0; for (i = 1; i <= 5; i++) s += i");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("15", Expression.getVar("s", exp).toString());

        jp = new JSParser("var s = 0; for (i = 1; i <= 5; i++) { if (i == 2) continue; s += i }");
        System.out.println("var s = 0; for (i = 1; i <= 5; i++) { if (i == 2) continue; s += i }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("13", Expression.getVar("s", exp).toString());

        jp = new JSParser("var s = 0; for (i = 1; i <= 5; i++) { if (i == 4) break; s += i }");
        System.out.println("var s = 0; for (i = 1; i <= 5; i++) { if (i == 4) break; s += i }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("6", Expression.getVar("s", exp).toString());
    }
    
    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testWithKeyword() {
        JSParser jp = new JSParser("var obj = { x: 1, y: 2 }; with(obj) { y = 3 }");
        System.out.println("var obj = { x: 1, y: 2 }; with(obj) { y = 3 }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1, y: 3}", Expression.getVar("obj", exp).toString());
        jp = new JSParser("var obj = { x: 1, y: 2 }; with(obj) { if (1) { y = 3 } }");
        System.out.println("var obj = { x: 1, y: 2 }; with(obj) { if (1) { y = 3 } }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1, y: 3}", Expression.getVar("obj", exp).toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testDeleteKeyword() {
        JSParser jp = new JSParser("var a = 1; delete a; a");
        System.out.println("var a = 1; delete a; a");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("undefined", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1, y: 2 }; delete obj.y; obj");
        System.out.println("var obj = { x: 1, y: 2 }; delete obj.y; obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1}", exp.getValue().toString());
        jp = new JSParser("var obj = { x: 1, y: 2 }; with(obj) { delete y }; obj");
        System.out.println("var obj = { x: 1, y: 2 }; with(obj) { delete y }; obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("{x: 1}", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testTypeOf() {
        JSParser jp = new JSParser("typeof 1");
        System.out.println("typeof 1");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"number\"", exp.getValue().toString());
        jp = new JSParser("typeof { x: 1 }");
        System.out.println("typeof { x: 1 }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"object\"", exp.getValue().toString());
        jp = new JSParser("typeof [1, 2, 3]");
        System.out.println("typeof [1, 2, 3]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("\"array\"", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testInstanceOf() {
        JSParser jp = new JSParser("1 instanceof Number");
        System.out.println("1 instanceof Number");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("1 instanceof Object");
        System.out.println("1 instanceof Object");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("\"1\" instanceof Object");
        System.out.println("\"1\" instanceof Object");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("\"1\" instanceof String");
        System.out.println("\"1\" instanceof String");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("[1] instanceof Object");
        System.out.println("[1] instanceof Object");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("[1] instanceof Array");
        System.out.println("[1] instanceof Array");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testVariables() {
        JSParser jp = new JSParser("a = 5");
        System.out.println("a = 5");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", Expression.getVar("a", exp).toString());
        jp = new JSParser("a = 5; a += 2");
        System.out.println("a = 5; a += 2");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("7", Expression.getVar("a", exp).toString());
        jp = new JSParser("a = 5; b = a + 7");
        System.out.println("a = 5; b = a + 7");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("5", Expression.getVar("a", exp).toString());
        assertEquals("12", Expression.getVar("b", exp).toString());
    }
    
   /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testVar() {
        JSParser jp = new JSParser("{ a = 1 }");
        System.out.println("{ a = 1 }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertTrue(((Block)exp).scope.containsKey("a"));
        assertFalse(((Block)((Block)exp).children.get(0)).scope.containsKey("a"));
        jp = new JSParser("{ let a = 1 }");
        System.out.println("{ let a = 1 }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertFalse(((Block)exp).scope.containsKey("a"));
        assertTrue(((Block)((Block)exp).children.get(0)).scope.containsKey("a"));
        jp = new JSParser("{ var a = 1 }");
        System.out.println("{ var a = 1 }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertTrue(((Block)exp).scope.containsKey("a"));
        assertFalse(((Block)((Block)exp).children.get(0)).scope.containsKey("a"));
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testCompareInt() {
        JSParser jp = new JSParser("5 >= 5");
        System.out.print("5 >= 5 is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("5 < 5");
        System.out.print("5 < 5 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("5 > 3");
        System.out.print("5 > 3 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("5 <= 3");
        System.out.print("5 <= 3 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("5 == 5");
        System.out.print("5 == 5 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("5 == 3");
        System.out.print("5 == 3 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("5 != 3");
        System.out.print("5 != 3 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("5 != 5");
        System.out.print("5 != 5 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testCompareFloat() {
        JSParser jp = new JSParser("6.8 >= 6.3");
        System.out.print("6.8 >= 6.3 is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("6.8 < 6.3");
        System.out.print("6.8 < 6.3 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("6.8 == 6.8");
        System.out.print("6.8 == 6.8 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testCompareString() {
        JSParser jp = new JSParser("\"AC\" >= \"AB\"");
        System.out.print("\"AC\" >= \"AB\" is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("\"AC\" < \"AB\"");
        System.out.print("\"AC\" < \"AB\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("\"A\" < \"AB\"");
        System.out.print("\"A\" < \"AB\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
    }

    /**
     * Test of eval method, of class Expression.
     */
    @Test
    public void testCompareMixed() {
        JSParser jp = new JSParser("5.8 >= 5");
        System.out.print("5.8 >= 5 is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("6 > \"5.8\"");
        System.out.print("6 > \"5.8\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("6 > \"5\"");
        System.out.print("6 > \"5\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        
        jp = new JSParser("5 == \"5\"");
        System.out.print("5 == \"5\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
        jp = new JSParser("5 === \"5\"");
        System.out.print("5 === \"5\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("false", exp.getValue().toString());
        jp = new JSParser("5 !== \"5\"");
        System.out.print("5 !== \"5\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        assertEquals("true", exp.getValue().toString());
    }

}