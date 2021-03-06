/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinybrowser;

import cssparser.QuerySelector;
import htmlparser.HTMLParser;
import jsparser.Expression;
import jsparser.JSParser;

/**
 *
 * @author Alex
 */
public class Main {

    public static void testHTMLParser() {
        HTMLParser hp = new HTMLParser("test.htm");
        System.out.println("----------------------------------");
        hp.traverseTree();
        System.out.println();
        System.out.println("----------------------------------");
        System.out.println("Replacing innerHTML of <p> element");
        hp.getRootNode().nthElementChild(2).firstElementChild().replaceInnerHTML("This is a paragraph with a <a href=\"#\" class=\"link red\" id=\"link\">link</a> inside.");
        System.out.println("----------------------------------");
        System.out.println();
        hp.traverseTree();
        System.out.println("\r\nChecking index:");
        System.out.println("First element with class \"link\" has tag <" + hp.getClassIndex().get("link").get(0).tagName + ">");
        System.out.println();
        System.out.println("Checking queries:");
        QuerySelector qs = new QuerySelector(".link.red", hp);
        System.out.print("Result of matching .link.red is ");
        qs.printResults();
        qs = new QuerySelector(".link.blue", hp);
        System.out.print("Result of matching .link.blue is ");
        qs.printResults();
        qs = new QuerySelector("#link", hp);
        System.out.print("Result of matching #link is ");
        qs.printResults();
        qs = new QuerySelector("body .link", hp);
        System.out.print("Result of matching body .link is ");
        qs.printResults();
        qs = new QuerySelector("body > .link", hp);
        System.out.print("Result of matching body > .link is ");
        qs.printResults();
        qs = new QuerySelector("body span.link", hp);
        System.out.print("Result of matching body span.link is ");
        qs.printResults();
        qs = new QuerySelector("body #some.link", hp);
        System.out.print("Result of matching body #some.link is ");
        qs.printResults();
        qs = new QuerySelector("body .link#some", hp);
        System.out.print("Result of matching body .link#some is ");
        qs.printResults();
        qs = new QuerySelector("body a#some", hp);
        System.out.print("Result of matching body a#some is ");
        qs.printResults();
        qs = new QuerySelector("body.main a", hp);
        System.out.print("Result of matching body.main a is ");
        qs.printResults();
        qs = new QuerySelector("body > p", hp);
        System.out.print("Result of matching body > p is ");
        qs.printResults();
        qs = new QuerySelector("body a", hp);
        System.out.print("Result of matching body a is ");
        qs.printResults();
        qs = new QuerySelector("body a:first-child", hp);
        System.out.print("Result of matching body a:first-child is ");
        qs.printResults();
        qs = new QuerySelector("body a:nth-child(2)", hp);
        System.out.print("Result of matching body a:nth-child(2) is ");
        qs.printResults();
        qs = new QuerySelector("body:first-child p", hp);
        System.out.print("Result of matching body:first-child p is ");
        qs.printResults();
        qs = new QuerySelector("body:last-child p", hp);
        System.out.print("Result of matching body:last-child p is ");
        qs.printResults();
    }

    public static void testJSParser() {
        /* JSParser jp = new JSParser("(5 + 3.5 * 2) + \"test\"");
        System.out.println("(5 + 3.5 * 2) + \"test\"");
        jp.printTokenChain();
        System.out.println();
        jp = new JSParser("[1, \"one\"] + [2, \"two\"]");
        System.out.println("[1, \"one\"] + [2, \"two\"]");
        jp.printTokenChain();
        System.out.println();
        jp = new JSParser("{ a: 10, b: \"str\", c: { x: true } }");
        System.out.println("{ a: 10, b: \"str\", c: { x: true } }");
        jp.printTokenChain();
        System.out.println();
        jp = new JSParser("{ a: 10, b: \"str\", c: [3, 5, 7] }");
        System.out.println("{ a: 10, b: \"str\", c: [3, 5, 7] }");
        jp.printTokenChain();
        System.out.println();
        jp = new JSParser("{ a: 10, b: a + 5 }");
        System.out.println("{ a: 10, b: a + 5 }");
        jp.printTokenChain(); */
        /* JSParser jp = new JSParser("4 + 1.5 * 2");
        System.out.print("4 + 1.5 * 2 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("4 * (5 + 2)");
        System.out.print("4 * (5 + 2) = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("-(5 * 3)");
        System.out.println("-(5 * 3)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 >= 5");
        System.out.print("5 >= 5 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 !== \"5\"");
        System.out.print("5 !== \"5\" is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("3 & 2");
        System.out.print("3 & 2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("3 | 2");
        System.out.print("3 | 2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("3 ^ 2");
        System.out.print("3 ^ 2 = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("5 + 3++");
        System.out.print("5 + 3++ = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 + ++3");
        System.out.print("5 + ++3 = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("!0");
        System.out.print("!0 is ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("!!1");
        System.out.print("!!1 is ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("!(3 > 5)");
        System.out.print("!(3 > 5) is ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("5 && 3");
        System.out.print("5 && 3 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 || 3");
        System.out.print("5 || 3 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("0 && 5");
        System.out.print("0 && 5 = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("0 || 5");
        System.out.print("0 || 5 = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("5 && 3 || 0 && 5");
        System.out.print("5 && 3 || 0 && 5 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 > 3 && 7 - 3 > 0");
        System.out.print("5 > 3 && 7 - 3 > 0 is ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("5 > 3 ? 7 - 6 : 8");
        System.out.print("5 > 3 ? 7 - 6 : 8 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("5 < 3 ? 1 > 0 ? 7 - 6 : 3 : 8");
        System.out.print("5 < 3 ? 1 > 0 ? 7 - 6 : 3 : 8 = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("5 < 3 ? (1 > 0 ? 7 - 6 : 3) : 8");
        System.out.print("5 < 3 ? (1 > 0 ? 7 - 6 : 3) : 8 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("3, 1, 5");
        System.out.print("3, 1, 5 = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("(2, 4), 3, (1, 5, 7)");
        System.out.print("(2, 4), 3, (1, 5, 7) = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("[1, \"one\"] + [2, \"two\"]");
        System.out.print("[1, \"one\"] + [2, \"two\"] = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("[1, [2, 3]]");
        System.out.print("[1, [2, 3]] = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("{ \"one\": 1, \"two\": 2 }");
        System.out.print("{ \"one\": 1, \"two\": 2 } = ");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("{ \"one\": 1, \"two\": [2, 3] }");
        System.out.print("{ \"one\": 1, \"two\": [2, 3] } = ");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("{ \"name\": \"User\", \"info\": {\"login\": \"username\", \"password\": \"*****\"}}");
        System.out.print("{ \"name\": \"User\", \"info\": {\"login\": \"username\", \"password\": \"*****\"}} = ");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("{ 7 + 5; { 8 == 3; 5 * 4 } }");
        System.out.println("{ 7 + 5; { 8 == 3; 5 * 4 } }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("a = 5");
        System.out.println("a = 5");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        System.out.println("a is now " + Expression.getVar("a", exp));
        jp = new JSParser("a = 5; a += 2");
        System.out.println("a = 5; a += 2");
        exp = Expression.create(jp.getHead());
        exp.eval();
        System.out.println("a is now " + Expression.getVar("a", exp));
        jp = new JSParser("a = 5; b = a + 7");
        System.out.println("a = 5; b = a + 7");
        exp = Expression.create(jp.getHead());
        exp.eval();
        System.out.println("a = " + Expression.getVar("a", exp) + ", b = " + Expression.getVar("b", exp)); */
        //JSParser jp = new JSParser("a = 5, b = a + 3");
        //System.out.println("a = 5, b = a + 3");
        //Expression exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("5 < 3 ? (4 >= 1 ? a : b) : 8");
        //System.out.println("5 < 3 ? (4 >= 1 ? a : b) : 8");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        /*JSParser jp = new JSParser("a = 3, b = { x : a + 7 }");
        System.out.println("a = 3, b = { x : a + 7 }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = 3, b = [1, 2, a]");
        System.out.println("a = 3, b = [1, 2, a]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("[1, 2, 3][2]");
        System.out.println("[1, 2, 3][2]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("{ a: 1 }.a");
        System.out.println("{ a: 1 }.a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("{ a: 1 }[\"a\"]");
        System.out.println("{ a: 1 }[\"a\"]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("[1, 2, 3][0] + [1, 2, 3][1]");
        System.out.println("[1, 2, 3][0] + [1, 2, 3][1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = [1, 2, 3]; a[1] + a[2]");
        System.out.println("a = [1, 2, 3]; a[1] + a[2]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = [1, 2, 3]; a[1] = 5");
        System.out.println("a = [1, 2, 3]; a[1] = 5");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = [1, [2, 3, 4], 5]; a[1][1]");
        System.out.println("a = [1, [2, 3, 4], 5]; a[1][1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = [1, [2, 3, 4], 5]; a[1][1] = 1; a[1]");
        System.out.println("a = [1, [2, 3, 4], 5]; a[1][1] = 1; a[1]");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("d = { a: [3, 5, 9] }; d.a[1] = 7; d");
        System.out.println("d = { a: [3, 5, 9] }; d.a[1] = 7; d");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("i++");
        System.out.println("i++ (3 times)");
        Expression exp = Expression.create(jp.getHead());
        exp.setReusable(true);
        Expression.setVar("i", jsparser.JSValue.create("Integer", "0"), exp);
        for (int i = 0; i < 3; i++) exp.eval(); */
        /* JSParser jp = new JSParser("if (5 > 3) 5 + 3");
        System.out.println("if (5 > 3) 5 + 3");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("if (5 != 5) 5 + 3; else 3");
        System.out.println("if (5 != 5) 5 + 3; else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("if (5 == 5) { a = 5 + 3; a * 3 } else 3");
        System.out.println("if (5 == 5) { a = 5 + 3; a * 3 } else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("if (5 > 5) 1; else if (5 <= 5) 2; else 3");
        System.out.println("if (5 > 5) 1; else if (5 <= 5) 2; else 3");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("if (5 == 5) { if (false) \"ok\"; else \"not ok\" }");
        System.out.println("if (5 == 5) { if (false) \"ok\"; else \"not ok\" }");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("switch(3) { case 1: \"one\"; break; }");
        System.out.println("switch(3) { case 1: \"one\"; break; }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("switch(3) { case 3: \"three\"; break; }");
        System.out.println("switch(3) { case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("switch(1) { case 1: \"one\"; break; case 3: \"three\"; break; }");
        System.out.println("switch(1) { case 1: \"one\"; break; case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("switch(2) { case 1: \"one\"; break; case 2: \"two\"; case 3: \"three\"; break; }");
        System.out.println("switch(2) { case 1: \"one\"; break; case 2: \"two\"; case 3: \"three\"; break; }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("for (i = 0; i < 3; i++) { \"str\" + (i+1) }");
        System.out.println("for (i = 0; i < 3; i++) { \"str\" + (i+1) }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var i = 0; while (i < 3) { \"str\" + (++i); }");
        System.out.println("var i = 0; while (i < 3) { \"str\" + (++i); }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var i = 0; window; window.j = 1; window; j");
        System.out.println("var i = 0; window; window.j = 1; window; j");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        /* JSParser jp = new JSParser("a = [1, 5, 9]; b = [0, 2]; a[b[1]]");
        System.out.println("a = [1, 5, 9]; b = [0, 2]; a[b[1]]");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("a = [1, 5, 9]; b = [0, 2]; a[b[0]] + a[b[1]]");
        System.out.println("a = [1, 5, 9]; b = [0, 2]; a[b[0]] + a[b[1]]");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        //JSParser jp = new JSParser("function test() { \"function run\" } test()");
        //System.out.println("function test() { \"function run\" } test()");
        //Expression exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("test = function() { \"function run\" } test()");
        //System.out.println("test = function() { \"function run\" } test()");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("test = function(n) { return n+1 } test(4)");
        //System.out.println("test = function(n)  { return n+1 } test(4)");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("function() { return [3, 4, 5]; }()[2]");
        //System.out.println("function() { return [3, 4, 5]; }()[2]");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("var obj = { x: 1, f: function(n) { this.x = n; } }; obj.f(3); obj.x");
        //System.out.println("var obj = { x: 1, f: function(n) { this.x = n; } }; obj.f(3); obj.x");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; f(3); x");
        //System.out.println("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; ; f(3); x");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        //jp = new JSParser("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; f.call(obj, 3); obj.x");
        //System.out.println("var obj = { x: 1, f: function(n) { this.x = n; } }; var f = obj.f; f.call(obj, 3); obj.x");
        //exp = Expression.create(jp.getHead());
        //exp.eval();
        /* JSParser jp = new JSParser("var obj = { x: 1, f: function(a, b) { return a * b } }; var f = obj.f; f(3, 3) + f(3, 2)");
        System.out.println("var obj = { x: 1, f: function(a, b) { return a * b } }; var f = obj.f; f(3, 3) + f(3, 2)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.call(obj, 3, 4); obj.x");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.call(obj, 3, 4); obj.x");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.apply(obj, [3, 4]); obj.x");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.apply(obj, [3, 4]); obj.x");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f = f.bind(obj, 3); f(4)");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f = f.bind(obj, 3); f(4)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.bind(obj, 3).bind(obj, 4)()");
        System.out.println("var obj = { x: 1, f: function(a, b) { this.x = a + b; } }; var f = obj.f; f.bind(obj, 3).bind(obj, 4)()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = function() { \"function run\" }; function(cbk) { cbk() }(f)");
        System.out.println("var f = function() { \"function run\" }; function(cbk) { cbk() }(f)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = function() { return function() { \"function run\" } }; f()()");
        System.out.println("var f = function() { return function() { \"function run\" } }; f()()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = function(x) { return function() { x = 5 + x } }; f(3)()");
        System.out.println("var f = function(x) { return function() { x = 5 + x } }; f(3)()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = (x => x * x); f(2)");
        System.out.println("var f = (x => x * x); f(2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("Array(3, 5)");
        System.out.println("Array(3, 5)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("Array(5, 3, 1).sort()");
        System.out.println("Array(5, 3, 1).sort()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("Array(5, 3, 8, 1).slice(1, 3)");
        System.out.println("Array(5, 3, 8, 1).slice(1, 3)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("Array(5, 3, 8, 1).join('-')");
        System.out.println("Array(5, 3, 8, 1).join('-')");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("String(\"abc\")");
        System.out.println("String(\"abc\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("String(\"abc\").charAt(1)");
        System.out.println("String(\"abc\").charAt(1)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("String(\"Hello\").toUpperCase()");
        System.out.println("String(\"Hello\").toUpperCase()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("String(\"Hello\").toLowerCase()");
        System.out.println("String(\"Hello\").toLowerCase()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("String(\"Hello World\").split(\" \")[1]");
        System.out.println("String(\"Hello World\").split(\" \")[1]");
        exp = Expression.create(jp.getHead());
        exp.eval(); */
        JSParser jp = new JSParser("var a = [1, 2, 3]; function inc(a) { for (var i = 0; i < a.length; i++) ++a[i]; return a }; inc(a)");
        System.out.println("var a = [1, 2, 3]; function inc(a) { for (var i = 0; i < a.length; i++) ++a[i]; return a }; inc(a)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3]; a.map(function(i) { return i+1 })");
        System.out.println("var a = [1, 2, 3]; a.map(function(i) { return i+1 })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.filter(function(i) { return i % 2 == 0 })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.filter(function(i) { return i % 2 == 0 })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b })");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.reduce(function(a, b) { return a + b })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3]; a.push(4); a; a.pop(); a");
        System.out.println("var a = [1, 2, 3]; a.push(4); a; a.pop(); a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3]; a.unshift(0); a; a.shift(); a");
        System.out.println("var a = [1, 2, 3]; a.unshift(0); a; a.shift(); a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.splice(1, 3); a");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.splice(1, 3); a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = [1, 2, 3, 4, 5]; a.splice(1, 3, 9); a");
        System.out.println("var a = [1, 2, 3, 4, 5]; a.splice(1, 3, 9); a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("function f() { var a = 1; eval(\"a = 3\"); a }; f()");
        System.out.println("function f() { var a = 1; eval(\"a = 3\"); a }; f()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, y: 2 }; with(obj) { y = 3 }; obj");
        System.out.println("var obj = { x: 1, y: 2 }; with(obj) { y = 3 }; obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var a = 1; delete a; a");
        System.out.println("var a = 1; delete a; a");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, y: 2 }; delete obj.y; obj");
        System.out.println("var obj = { x: 1, y: 2 }; delete obj.y; obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var obj = { x: 1, y: 2 }; with(obj) { delete y }; obj");
        System.out.println("var obj = { x: 1, y: 2 }; with(obj) { delete y }; obj");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("function f() { 2() }; f()");
        System.out.println("function f() { 2() }; f()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("try { [1, 2, 3].map(null) } catch() { \"Something weird happened\" } finally { 3 }");
        System.out.println("try { [1, 2, 3].map(null) } catch() { \"Something weird happened\" } finally { 3 }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("try { [1, 2, 3].map(null) } catch() { throw { value: \"My exception\" }; \"Something weird happened\" } catch (e) { e.value }");
        System.out.println("try { [1, 2, 3].map(null) } catch() { throw { value: \"My exception\" }; \"Something weird happened\" } catch (e) { e.value }");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = Function(); f.name");
        System.out.println("var f = Function(); f.name");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("var f = Function(\"return a*5\", \"a\"); f(2)");
        System.out.println("var f = Function(\"return a*5\", \"a\"); f(2)");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("function f() { this.x = 1 }; var obj = new f()");
        System.out.println("function f() { this.x = 1 }; var obj = new f()");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("function f() { this.x = 1 }; var p = { y: 2 }; f.prototype = p; var obj = new f()");
        System.out.println("function f() { this.x = 1 }; var p = { y: 2 }; f.prototype = p; var obj = new f()");
        exp = Expression.create(jp.getHead());
        exp.setSilent(true);
        exp.eval();
        ((jsparser.JSObject)exp.getValue()).print_proto = true;
        System.out.println(exp.getValue());
        ((jsparser.JSObject)exp.getValue()).print_proto = false;

        //jp = new JSParser("var xhr = new XMLHttpRequest(); xhr.open(\"GET\", \"https://google.com\", true); xhr.onload = function() { this.response.slice(0, 100) + \"..\" }; xhr.send(null)");
        //System.out.println("var xhr = new XMLHttpRequest(); xhr.open(\"GET\", \"https://google.com\", true); xhr.onload = function() { this.response.slice(0, 100) + \"..\" }; xhr.send(null)");
        //exp = Expression.create(jp.getHead());
        //exp.eval();

        //testInterval();

        jp = new JSParser("JSON.stringify({ x: 1, y: \"my string\", z: [1, 2, 3] })");
        System.out.println("JSON.stringify({ x: 1, y: \"my string\", z: [1, 2, 3] })");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("JSON.parse(\"{ x: 1, y: \\\"my string\\\", z: [1, 2, 3] }\")");
        System.out.println("JSON.parse(\"{ x: 1, y: \\\"my string\\\", z: [1, 2, 3] }\")");
        exp = Expression.create(jp.getHead());
        exp.eval();
        jp = new JSParser("new Date()");
        System.out.println("new Date()");
        exp = Expression.create(jp.getHead());
        exp.eval();

        jp = new JSParser("yield 1; yield 2; return 3");
        System.out.println("yield 1; yield 2; return 3");
        exp = Expression.create(jp.getHead());
        ((jsparser.Block)exp).is_gen = true;
        for (int i = 0; i < 3; i++) {
            exp.eval();
            System.out.println("Done: " + ((jsparser.Block)exp).done);
        }

        jp = new JSParser("function* gen() { yield 1; yield 2; return 3 } var g = gen(); for (var i = 0; i < 4; i++) g.next()");
        System.out.println("function* gen() { yield 1; yield 2; return 3 } var g = gen(); for (var i = 0; i < 4; i++) g.next()");
        exp = Expression.create(jp.getHead());
        exp.eval();

        jp = new JSParser("function* gen() { yield 1; yield 2; yield* gen2(); return 3 } function* gen2() { yield 4; yield 5 } var g = gen(); var a = []; for (var i = 0; i < 5; i++) a.push(g.next().value); a");
        System.out.println("function* gen() { yield 1; yield 2; yield* gen2(); return 3 } function* gen2() { yield 4; yield 5 } var g = gen(); var a = []; for (var i = 0; i < 5; i++) a.push(g.next().value); a");
        exp = Expression.create(jp.getHead());
        exp.eval();

        testPromises();
    }

    public static void testTimeout() {
        JSParser jp = new JSParser("function f() { 5 }; setTimeout(f, 1200)");
        System.out.println("function f() { 5 }; setTimeout(f, 1200)");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
    }

    public static void testInterval() {
        JSParser jp = new JSParser("var i = 0; function f() { i+=10; if (i >= 50) clearInterval(t); }; var t = setInterval(f, 500)");
        System.out.println("var i = 0; function f() { i+=10; if (i >= 50) clearInterval(t); }; var t = setInterval(f, 500)");
        Expression exp = Expression.create(jp.getHead());
        exp.setSilent(true);
        exp.eval();
        ((jsparser.Function)Expression.getVar("f", exp)).setSilent(false);
    }

    public static void testPromises() {
        /*JSParser jp = new JSParser("function cbk(str) { \"Promise fulfilled: \" + str } function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 1500) }");
        System.out.println();
        System.out.println("function cbk(str) { \"Promise fulfilled: \" + str }");
        System.out.println("function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 1500) }");
        System.out.println();
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        jsparser.Function f = (jsparser.Function)Expression.getVar("f", exp);
        f.setSilent(true);
        jsparser.Promise p = new jsparser.Promise(f);
        p.then((jsparser.Function)Expression.getVar("cbk", exp));*/

//        JSParser jp = new JSParser("function cbk1(str) { \"Promise 1 fulfilled: \" + str; return str } function cbk2(str) { setTimeout(str => { \"Promise 2 fulfilled: \" + str }, 1000); throw \"ERROR\" } function cbk3(str) { \"Promise 3 fulfilled: \" + str; return str } function err(str) { \"An error has occured: \" + str } function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 300) }; (new Promise(f)).then(cbk1).then(cbk2).then(cbk3, err)");
//        System.out.println();
//        System.out.println("function cbk1(str) { \"Promise 1 fulfilled: \" + str; return str }");
//        System.out.println("function cbk2(str) { setTimeout(str => { \"Promise 2 fulfilled: \" + str }, 1000); throw \"ERROR\" }");
//        System.out.println("function cbk3(str) { \"Promise 3 fulfilled: \" + str; return str }");
//        System.out.println("function err(str) { \"An error has occured: \" + str }");
//        System.out.println("function f(resolve, reject) { setTimeout(function() { resolve(\"OK\") }, 300) }");
//        System.out.println("(new Promise(f)).then(cbk1).then(cbk2).then(cbk3, err)");
//        System.out.println();
        JSParser jp = new JSParser("function cbk(str) { setTimeout(function() { \"Promise 1 fulfilled: \" + str; return str }, 500) } function err(str) { setTimeout(function() { \"An error has occured: \" + str }, 500) } function f1(resolve, reject) { setTimeout(function() { resolve(\"OK1\") }, 300) }; function f2(resolve, reject) { setTimeout(function() { resolve(\"OK2\") }, 400) }; Promise.all([new Promise(f1), new Promise(f2)]).then(cbk, err)");
        System.out.println();
        System.out.println("function cbk(str) { setTimeout(function() { \"Promise fulfilled: \" + str; return str }, 500) }");
        System.out.println("function err(str) { setTimeout(function() { \"An error has occured: \" + str }, 500) }");
        System.out.println("function f1(resolve, reject) { setTimeout(function() { resolve(\"OK1\") }, 300) }");
        System.out.println("function f2(resolve, reject) { setTimeout(function() { resolve(\"OK2\") }, 1400) }");
        System.out.println("Promise.all([new Promise(f1), new Promise(f2)]).then(cbk, err)");
        System.out.println();
        Expression exp = Expression.create(jp.getHead());
        //((jsparser.Function)Expression.getVar("f", exp)).setSilent(true);
        //((jsparser.Function)Expression.getVar("cbk2", exp)).setSilent(true);
        exp.eval();
        /*jsparser.Function f = (jsparser.Function)Expression.getVar("f", exp);
        f.setSilent(true);
        ((jsparser.Function)Expression.getVar("cbk2", exp)).setSilent(true);
        jsparser.Promise p = new jsparser.Promise(f);
        p.then((jsparser.Function)Expression.getVar("cbk1", exp))
                .then((jsparser.Function)Expression.getVar("cbk2", exp))
                .then((jsparser.Function)Expression.getVar("cbk3", exp),
                      (jsparser.Function)Expression.getVar("err", exp));*/
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        testJSParser();
    }

}
