package jsparser;

import java.util.Hashtable;
import java.util.Vector;
import network.HttpsClientC;

/**
 *
 * @author Alex
 */
public class Expression {
    public Expression() {}

    public Expression(Token head, Block block) {
        parent_block = block;
        start = head;
        end = start;
        Token t = start;
        while (t != null) {
            int type = t.getType();
            if (mode > 0 && type == 6 && (t.next == null ||
                    t.next.getType() == 2 && t.next.getContent().matches("[*/%&|^+-]?=") ||
                    t.next.getType() == 18)) {
                Expression.setVar(t.getContent(), Undefined.getInstance(), this, mode);
            }
            if (type == 2 && t.getContent().equals("*") && t.prev.getContent().equals("yield")) {
                t.prev.next = t.next;
                if (t.next != null) {
                    t.next.prev = t.prev;
                }
                t = t.next;
                continue;
            }
            if (type == 2 && t.next != null && t.next.getType() == 2 && (t.next.getContent().equals(",") || t.prev.getContent().equals(",")) &&
                  !block.scope.containsKey(t.getContent())) {
                Expression.setVar(t.getContent(), Undefined.getInstance(), this, mode);
            }
            if (type != 15 && type > 6 && !(type >= 9 && type <= 12 || type == 17)) {
                if ((ret || thr) && (start == null || start.getType() == 14 || start.getType() == 18)) {
                    start = new Token("undefined");
                    start.prev = head.prev;
                    head.prev.next = start;
                }
                break;
            } else {
                if (type == 15 && t.getContent().matches("var|let")) {
                    mode = t.getContent().equals("var") ? var : let;
                    if (t == start && (t.next == null || t.next.getType() != Token.VAR_NAME)) {
                        System.err.println("Syntax error");
                    }
                    if (t == start) {
                        start = t.next;
                    }
                    t = t.next;
                    continue;
                }

                else if (type == 15 && end.getContent().equals("return")) {
                    ret = true;
                    t.prev.next = t.next;
                    if (t.next != null) {
                        t.next.prev = t.prev;
                    }
                    if (t != start) {
                        System.err.println("Syntax error");
                    }
                    if (t == start && t.next != null) {
                        start = t.next;
                    }
                    t = t.next;
                    continue;
                }

                else if (type == 15 && end.getContent().equals("throw")) {
                    thr = true;
                    t.prev.next = t.next;
                    if (t.next != null) {
                        t.next.prev = t.prev;
                    }
                    if (t != start) {
                        System.err.println("Syntax error");
                    }
                    if (t == start) {
                        start = t.next;
                    }
                    t = t.next;
                    continue;
                }

                else if (type == 15 && end.getContent().matches("break|continue")) {
                    if (t != start) {
                        System.err.println("Syntax error");
                    }
                    end = start;
                    t.next = null;
                    break;
                }

                else if (type == 15 && end.getContent().equals("delete")) {
                    del = true;
                    t.prev.next = t.next;
                    if (t.next != null) {
                        t.next.prev = t.prev;
                    }
                    if (t != start) {
                        System.err.println("Syntax error");
                    }
                    if (t == start) {
                        start = t.next;
                    }
                    t = t.next;
                    continue;
                }

                if (type == 15 && end.getContent().matches("new|yield")) {
                    t = t.next;
                    continue;
                }

                end = t;
                t = t.next;

                if (type == 15 && !end.getContent().matches("if|switch|case|var|let|new|yield")) {
                    end.next = null;
                    break;
                }
            }
        }
        updateSource();
    }

    public void updateSource() {
        source = "";
        Token token = start;
        int level = -1;
        Expression b = this;
        while (b != null) {
            level++;
            b = b.parent_block;
        }

        String pad  = "";
        for (int i = 0; i < level; i++) {
            pad += "  ";
        }

        source += pad;

        boolean is_condition = false;

        if (ret && !token.getContent().equals("return")) {
            source += "return ";
        }
        if (getContent().matches("var|let")) {
            source += getContent() + " ";
        }

        while (token != null && token.getType() != Token.SEMICOLON) {
            if (token.getType() == Token.OP && !token.getContent().matches("[!:,]") && !token.getContent().matches("\\+\\+|--") &&
                  !token.getContent().matches("break|continue|return")) {
                source += " ";
            }
            String content = "";
            if (token.val == null) {
                content += !token.getContent().matches("\\{\\{|\\}\\}") ? token.getContent() : token.getContent().charAt(0);
            }
            if (token.val instanceof Function) {
                content += ((Function)token.val).toPaddedString().trim();
            }
            if (token.val != null) {
                content = token.val.toString();
            }
            if (content.equals("if")) {
                is_condition = true;
            }
            if (content.equals("else") && token.next != null && token.next.getContent().equals("if")) {
                content = "} " + content + " ";
            }
            else if (content.equals("else")) {
                content = "} " + content + " {";
                source += content;
                break;
            }
            source += content;
            if (token.getType() == Token.OP && !token.getContent().equals("!") && !token.getContent().matches("\\+\\+|--") || 
                  token.getType() == Token.KEYWORD && !token.getContent().matches("break|continue|return")) {
                source += " ";
            }
            token = token.next;
        }
        source = source.replaceAll(";\\s*", ";\n" + pad);
        if (ret && !source.matches("^\\s*return.*")) {
            source = source.replaceAll("^(\\s*)", "$1return ").replaceAll("return (;|$)", "return$1");
        }
        if (!is_condition && !source.endsWith(";") && !source.endsWith("{") && !source.endsWith("}") && !getContent().equals("switch")) source += ";";
        if (getContent().equals("switch")) {
            source = source + " {";
        } else if (getContent().equals("case")) {
            source = source.replaceAll(";$", ":");
        } else if (parent_block != null && parent_block.getType() == Block.CASE) {
            source = "  " + source;
        }
    }

    public static Expression create(Token head) {
        ObjectC o = new ObjectC();
        ( (JSObject)( (JSObject)o.get("__proto__") ).get("hasOwnProperty") ).set("__proto__", FunctionProto.getInstance());
        ( (JSObject)( (JSObject)o.get("__proto__") ).get("toString") ).set("__proto__", FunctionProto.getInstance());
        ArrayC a = new ArrayC();
        FunctionC f = new FunctionC();
        DateC d = new DateC();
        StringC s = new StringC();
        Console c = Console.getInstance();
        MathObj m = MathObj.getInstance();
        NumberC n = new NumberC();
        JsonObj j = JsonObj.getInstance();
        HttpsClientC h = new HttpsClientC();
        PromiseC p = new PromiseC();
        Block b = new Block(head);
        Window w = new Window(b);
        b.scope.put("window", w);
        b.scope.put("Object", o);
        b.scope.put("Array", a);
        b.scope.put("Function", f);
        b.scope.put("Date", d);
        b.scope.put("String", s);
        b.scope.put("Math", m);
        b.scope.put("Number", n);
        b.scope.put("JSON", j);
        b.scope.put("XMLHttpRequest", h);
        b.scope.put("Promise", p);
        b.scope.put("console", c);
        b.is_func = true;
        b.setConsole(c);
        return b;
    }

    public static boolean equals(JSValue value1, JSValue value2) {
        String types = value1.getType() + "|" + value2.getType();
        String type = getResultType(types);
        boolean result = false;
        if (type.equals("String"))
           result = ((JSString)value1).getValue().compareTo(((JSString)value2).getValue()) == 0;
        else if (type.equals("Float"))
           result = ((JSFloat)value1).getValue() == ((JSFloat)value2).getValue();
        else if (type.equals("Integer"))
           result = ((JSInt)value1).getValue() == ((JSInt)value2).getValue();
        else if (type.equals("NaN"))
           result = types.equals("NaN|NaN");
        return result;
    }

    public static String getResultType(String types) {
        String type = "";
        if (types.contains("Integer")) type = "Integer";
        if (types.contains("Float")) type = "Float";
        if (types.contains("String")) type = "String";
        if (types.contains("Number")) type = "Number";
        if (types.contains("NaN")) type = "NaN";
        if (types.equals("Array|Array")) type = "Array";
        if (types.contains("undefined") && (types.contains("Integer") || types.contains("Float"))) {
            type = "NaN";
        }
        if (type.isEmpty()) type = "Boolean";
        return type;
    }

    private void initArraysAndObjects() {
        Token t = start;
        Token last = null;
        int level1 = 0;
        int level2 = 0;
        while (t != null) {
            if (t.getType() == Token.ARRAY_START &&
                    (t.prev.getType() == Token.VAR_NAME || t.prev.getType() == Token.ARRAY_END ||
                    t.prev.getType() == Token.ARRAY_ENTITY || t.prev.getType() == Token.OBJECT_ENTITY ||
                    t.prev.getType() == Token.BRACE_CLOSE || t.prev.getType() == Token.DOT)) {
                int lvl = level1+1;
                t = t.next;
                while (t != null && lvl > level1) {
                    if (t.getType() == Token.ARRAY_START) lvl++;
                    if (t.getType() == Token.ARRAY_END) lvl--;
                    t = t.next;
                }
                continue;
            }
            if (t.getType() == Token.ARRAY_START ||
                    t.getType() == Token.OBJECT_START) {
                Token t2 = t.next;
                if (t.getType() == Token.ARRAY_START) {
                    level1++;
                    int lvl = level1;
                    while (t2 != null && lvl >= level1) {
                        if (t2.getType() == Token.ARRAY_START) lvl++;
                        if (t2.getType() == Token.ARRAY_END) lvl--;
                        t2 = t2.next;
                    }
                    last = new Token("[]");
                    last.val = new JSArray(t, this);
                } else {
                    level2++;
                    int lvl = level2;
                    while (t2 != null && lvl >= level2) {
                        if (t2.getType() == Token.OBJECT_START) lvl++;
                        if (t2.getType() == Token.OBJECT_END) lvl--;
                        t2 = t2.next;
                    }
                    last = new Token("{}");
                    last.val = new JSObject(t, this);
                }
                last.prev = t.prev;
                t.prev.next = last;
                if (start == t) {
                    start = last;
                }
                last.next = t2;
                if (t2 != null) {
                    t2.prev = last;
                }
                if (t2 == null) break;
                t = t2;
            }
            if (level1 < 0 || level2 < 0) {
                parent_block.error = new JSError(null, "Syntax error: unexpected " + t.getContent() + " token", getStack());
                return;
            }
            t = t.next;
        }
    }

    private void applyBraces() {
        Token t = start;
        int level = 0;
        while (t != null) {
            if (t.getType() == Token.BRACE_OPEN || t.getType() == Token.ARRAY_START) level++;
            if (t.getType() == Token.BRACE_CLOSE || t.getType() == Token.ARRAY_END) level--;
            if (!priorities.containsKey(t.getContent())) {
                t = t.next;
                continue;
            }
            if (t.getType() == Token.OP) {
                t.p = level * 20 + priorities.get(t.getContent());
            }
            t = t.next;
        }
    }

    private void process() {
        Token t = start;
        Token last = t;
        Vector<Expression> e = new Vector<Expression>();
        int level1 = 0;
        int level2 = 0;
        while (t != null) {
            if (t.getType() == Token.ARRAY_START) level1++;
            if (t.getType() == Token.OBJECT_START) level2++;
            if (t.getType() == Token.ARRAY_END) level1--;
            if (t.getType() == Token.OBJECT_END) level2--;
            if (t.next == null || t.getType() == Token.OP && t.p == priorities.get(",") &&
                    level1 == 0 && level2 == 0) {
                if (t.next != null) {
                    t.prev.next = null;
                }
                if (last != start || last.prev.getType() != Token.KEYWORD) {
                    Token t2 = new Token("");
                    last.prev = t2;
                    t2.next = last;
                }
                Expression exp = new Expression(last, parent_block);
                exp.silent = true;
                e.add(exp);
                if (t != null) {
                    last = t.next;
                }
            }
            t = t.next;
        }
        if (e.size() > 1) {
            for (int i = 0; i < e.size(); i++) {
                e.get(i).eval();
                if (parent_block.error != null) {
                    return;
                }
            }
            Expression exp = e.get(e.size()-1);
            Token t2 = new Token(exp.getValue().toString());
            t2.val = exp.getValue();
            start.prev.next = t2;
            t2.next = null;
            start = t2;
        }
        processCond();
    }

    private void processCond() {
        Token t = start;
        int level1 = 0;
        int level2 = 0;
        while (t != null) {
            if (t.getType() == Token.ARRAY_START) level1++;
            if (t.getType() == Token.OBJECT_START) level2++;
            if (t.getType() == Token.ARRAY_END) level1--;
            if (t.getType() == Token.OBJECT_END) level2--;
            if (t.getType() == Token.OP && t.p == priorities.get("?") &&
                    level1 == 0 && level2 == 0) {
                if (t.getContent().equals("?")) {
                    t.prev.next = null;
                    Token th = new Token("");
                    start.prev = th;
                    th.next = start;
                    Expression exp = new Expression(start, parent_block);
                    exp.silent = true;
                    exp.eval();

                    Token t2 = end;
                    level1 = 0;
                    level2 = 0;
                    if (t2.getType() == Token.ARRAY_START) level1++;
                    if (t2.getType() == Token.OBJECT_START) level2++;
                    if (t2.getType() == Token.ARRAY_END) level1--;
                    if (t2.getType() == Token.OBJECT_END) level2--;
                    while (t2.getType() != Token.OP || t2.p != priorities.get(":") ||
                            !t2.getContent().equals(":") || level1 != 0 || level2 != 0) {
                        if (t2.getType() == Token.ARRAY_START) level1++;
                        if (t2.getType() == Token.OBJECT_START) level2++;
                        if (t2.getType() == Token.ARRAY_END) level1--;
                        if (t2.getType() == Token.OBJECT_END) level2--;
                        t2 = t2.prev;
                        if (t2 == t) break;
                    }
                    if (t2 != t) {
                        Expression e = null;
                        if (exp.getValue().asBool().getValue()) {
                            t2.prev.next = null;
                            th = new Token("");
                            t2.next = t.next;
                            t.next.prev = th;
                            e = new Expression(t.next, parent_block);
                        } else {
                            th = new Token("");
                            th.next = t2.next;
                            t2.next.prev = th;
                            e = new Expression(t2.next, parent_block);
                        }
                        if (e != null) {
                            e.silent = true;
                            e.eval();
                            th = new Token(e.getValue().toString());
                            th.val = e.getValue();
                            start.prev.next = th;
                            th.next = null;
                            start = th;
                        }
                        t = start;
                        break;
                    } else {
                        System.err.println("Syntax error");
                        return;
                    }
                }
            }
            t = t.next;
        }
    }

    private void accessObjectProperties(Token t) {
        if (t.getType() == Token.BRACE_OPEN) {
            Token t2 = t;
            int level = 0;
            while (t2 != null && (t2.getType() != Token.BRACE_CLOSE || level > 1)) {
                if (t2.getType() == Token.BRACE_OPEN) level++;
                if (t2.getType() == Token.BRACE_CLOSE) level--;
                t2 = t2.next;
            }
            accessObjectProperties(t.next);
            t = t2.next;
            if (t == null) return;
        }
        while (t.getType() == Token.KEYWORD && t.getContent().matches("new|yield")) {
            if (t.getContent().equals("yield") && t.next == null) {
                if (t.next == null) {
                    parent_block.error = new JSError(null, "Syntax error: value after yield expected", getStack());
                    return;
                }
                if (t.next.getType() == Token.VALUE) {
                    t.next.val = JSValue.create(JSValue.getType(t.next.getContent()), t.next.getContent());
                }
                checkYield(t.next);
                return;
            }
            t = t.next;
        }
        if (((t.getType() == Token.VALUE && t.val != null && t.val.getType().equals("String")) || t.getType() == Token.VAR_NAME || t.getType() == Token.ARRAY_ENTITY || t.getType() == Token.OBJECT_ENTITY) &&
                t.next != null && (t.next.getType() == Token.DOT ||
                t.next.getType() == Token.ARRAY_START || t.next.getType() == Token.BRACE_OPEN)) {
            Vector<JSValue> v = new Vector<JSValue>();
            JSValue val = t.getType() == Token.VAR_NAME && t.val == null ? Expression.getVar(t.getContent(), this) : t.val;
            if (t.val == null) t.val = val;
            JSValue ctx = Expression.getVar("window", this);
            Token t2 = t.next;
            while (t2 != null && t2.getType() != Token.BRACE_OPEN && t2.getType() != Token.BRACE_CLOSE && t2.getType() != Token.OP) {
                if (t2.getType() == Token.VAR_NAME && t2.prev.getType() == Token.DOT) {
                    v.add(JSValue.create("String", t2.getContent()));
                } else if ((t2.getType() == Token.VAR_NAME || t2.getType() == Token.VALUE) && t2.prev.getType() == Token.ARRAY_START) {
                    if (t2.next.getType() == Token.ARRAY_END) {
                        if (t2.getType() == Token.VAR_NAME) {
                            v.add(Expression.getVar(t2.getContent(), this));
                        } else {
                            v.add(JSValue.create(JSValue.getType(t2.getContent()), t2.getContent()));
                        }
                    } else {
                        Token t3 = t2.next;
                        int level = 1;
                        while (t3 != null && !(t3.getType() == Token.ARRAY_END && level == 1)) {
                            if (t3.getType() == Token.ARRAY_START) level++;
                            if (t3.getType() == Token.ARRAY_END) level--;
                            t3 = t3.next;
                        }
                        if (t3 == null) {
                            parent_block.error = new JSError(null, "Syntax error: ] expected, but end of line found", getStack());
                            return;
                        }
                        Token th = new Token("");
                        th.next = t2;
                        t2.prev = th;
                        t3.prev.next = null;
                        v.add(new Expression(t2, parent_block).eval().getValue());
                        t2 = t3;
                        t3 = null;
                    }
                } else if (t2.getType() == Token.VALUE) {
                    v.add(JSValue.create(JSValue.getType(t2.getContent()), t2.getContent()));
                } else if (t2.getType() == Token.DOT && t2.next != null &&
                        (t2.next.getType() == Token.ARRAY_START || t2.next.getType() == Token.ARRAY_END)) {
                    parent_block.error = new JSError(null, "Syntax error: " + t2.next.getContent()  + " is not allowed after .", getStack());
                    return;
                } else if (t2.getType() == Token.DOT && t2.next == null) {
                    parent_block.error = new JSError(null, "Syntax error: unexpected end of expression after .", getStack());
                    return;
                } else if (t2.getType() == Token.DOT && t2.next != null && t2.next.getType() == Token.BRACE_OPEN) {
                    Token t3 = t2.next;
                    int level = 0;
                    while (t3 != null && !(t3.getType() == Token.BRACE_CLOSE && level == 1)) {
                        if (t3.getType() == Token.BRACE_OPEN) level++;
                        if (t3.getType() == Token.BRACE_CLOSE) level--;
                        t3 = t3.next;
                    }
                    if (t3 == null && t3.prev.getType() != Token.BRACE_CLOSE) {
                        parent_block.error = new JSError(null, "Syntax error: ) expected, but end of line found", getStack());
                        return;
                    }
                    Token tn1 = new Token("[");
                    Token tn2 = new Token("]");
                    t2.prev.next = tn1;
                    tn1.prev = t2.prev;
                    t2.next.next.prev = tn1;
                    tn1.next = t2.next.next;
                    t3.prev.next = tn2;
                    tn2.prev = t3.prev;
                    tn2.next = t3.next;
                    if (t3.next != null) {
                        t3.next.prev = tn2;
                    }
                    t2 = tn1;
                }
                t2 = t2.next;
            }
            boolean error = false;
            int last = 0;
            for (int i = 0; i < v.size(); i++) {
                if (!val.getType().matches("Array|Integer|Float|Number|String|Object|Function")) {
                    error = true;
                    break;
                }
                if (v.get(i) == null) return;
                if (v.get(i).getType().equals("Integer")) {
                    JSValue index = v.get(i);
                    if (index instanceof JSString) {
                        index = ((JSString)index).parseInt();
                        if (index instanceof NaN) {
                            error = true;
                            break;
                        }
                    } else {
                        index = index.asInt();
                    }
                    ctx = val;
                    if (val.getType().equals("Array")) {
                        val = ((JSArray)val).get((int)((JSInt)index).getValue());
                    } else {
                        val = ((JSString)val).get((int)((JSInt)index).getValue());
                    }
                    if (val instanceof Undefined) {
                        if (i < v.size()-1) error = true;
                        break;
                    }
                }
                else if (v.get(i).getType().equals("String")) {
                    JSString index = (JSString)v.get(i);
                    ctx = val;

                    if (val.getType().equals("Array")) {
                        val = ((JSArray)val).get(index);
                    }
                    else if (val.getType().equals("Integer")) val = ((JSInt)val).get(index);
                    else if (val.getType().equals("Float")) val = ((JSFloat)val).get(index);
                    else if (val.getType().equals("String")) val = ((JSString)val).get(index);
                    else if (val.getType().equals("Function")) val = ((Function)val).get(index);
                    else val = ((JSObject)val).get(index);
                    
                    if (index.getValue().equals("eval") &&
                            val.equals(((JSObject)Expression.getVar("window", this)).get("eval"))) {
                        ((DynamicContext)val).setContext(this.parent_block);
                    }
                    if (val instanceof Undefined) {
                        if (i < v.size()-1) error = true;
                        last = i;
                        break;
                    }
                }
            }
            if (error) {
                if (parent_block.strict_mode) {
                    parent_block.error = new JSError(null, "Incorrect field access: " + v.get(last) + " does not exist", getStack());
                } else {
                    Token nt = new Token("x");
                    nt.prev = t.prev;
                    t.prev.next = nt;
                    if (t2 != null) {
                        t2.prev = nt;
                    }
                    nt.next = t2;
                    nt.index = null;
                    if (t == start) {
                        start = nt;
                    }
                    nt.val = Undefined.getInstance();
                    nt.ctx = ctx;
                }
                return;
            }
            Token nt = null;
            if (v.size() > 0) {
                nt = new Token("x");
                nt.prev = t.prev;
                t.prev.next = nt;
                if (t2 != null) {
                    t2.prev = nt;
                }
                nt.next = t2;
                nt.index = v.lastElement();
                if (t == start) {
                    start = nt;
                }
                nt.val = val;
                nt.ctx = ctx;
            } else {
                nt = t;
            }
            if (nt.next != null && nt.next.getType() == Token.BRACE_OPEN) {
                if (nt.next.next != null &&
                      (nt.next.next.getType() == Token.VAR_NAME && nt.next.next.val == null ||
                      nt.next.next.getType() == Token.KEYWORD && nt.next.next.getContent().equals("new") &&
                      nt.next.next.next.val == null)) {
                    //accessObjectProperties(nt.next.next);
                }
                functionCall(nt.next);
            } else {
                checkYield(nt);
                removeBraces(nt);
            }
        }
    }

    private void checkYield(Token t) {
        if (t.prev.getType() != Token.KEYWORD || !t.prev.getContent().equals("yield")) {
            return;
        }
        if (t.next != null &&
            !(t.next.getContent().equals(",") || t.next.getType() == Token.BRACE_CLOSE)) {
            accessObjectProperties(t);
            return;
        }
        if (t.val == null) {
            if (t.getType() == Token.VALUE) {
                t.val = JSValue.create(JSValue.getType(t.getContent()), t.getContent());
            } else if (t.getType() == Token.VAR_NAME) {
                t.val = Expression.getVar(t.getContent(), this);
            }
        }
        Block b = parent_block;
        b.state = Block.RETURN;
        while (b != null && !b.is_gen) {
            b = b.parent_block;
            if (b != null) {
                b.state = Block.RETURN;
            }
        }
        if (b == null) {
            parent_block.error = new JSError(null, "Yield cannot be used in normal code blocks", getStack());
            return;
        } else if (!(t.val instanceof Generator)) {
            b.return_value = t.val;
            if (b.is_gen) b.done = false;
            t.prev.prev.next = t;
            t.prev = t.prev.prev;
            if (t.prev == start) {
                start = t;
            }
            yt = t;
        } else {
            b.return_value = ((JSObject)((Function)((Generator)t.val).get("next")).call(null, new Vector<JSValue>(), false)).get("value");
            if (b.is_gen) b.done = false;
            if (((Generator)t.val).isDone()) {
                t.prev.prev.next = t.next;
                if (t.next != null) {
                    t.next.prev = t.prev.prev;
                    if (t.prev == start) {
                        start = t.next;
                    }
                }
                if (t.prev == start) {
                    String str = b.return_value.toString();
                    if (b.return_value.getType().equals("Array")) str = "[]";
                    else if (b.return_value.getType().equals("Object")) str = "{}";
                    else if (b.return_value.getType().equals("Function")) str = "{Function}";
                    Token tt = new Token(str);
                    tt.val = b.return_value;
                    t.prev.prev.next = tt;
                    tt.prev = t.prev.prev;
                    start = tt;
                }
            }
            yt = t;
        }
    }

    public void setYieldValue(JSValue value) {
        if (value == null || yt == null) return;
        String str = value.toString();
        if (value.getType().equals("Array")) str = "[]";
        else if (value.getType().equals("Object")) str = "{}";
        else if (value.getType().equals("Function")) str = "{Function}";
        Token t = new Token(str);
        t.val = value;
        t.prev = yt.prev;
        yt.prev.next = t;
        if (yt == start) {
            start = t;
        }
        t.next = yt.next;
        if (yt.next != null) {
            yt.next.prev = t;
        }
        yt = null;
    }

    private void removeBraces(Token t) {
        if ((t.prev.prev == null || (t.prev.prev.getType() != Token.VAR_NAME &&
                  t.prev.prev.getType() != Token.OBJECT_ENTITY)) &&
               t.prev.getType() == Token.BRACE_OPEN &&
               t.next.getType() == Token.BRACE_CLOSE) {
            t.prev.prev.next = t;
            if (t.prev == start) {
                start = t;
            }
            t.prev = t.prev.prev;
            if (t.next.next == null) {
                t.next = null;
                end = t;
            } else {
                t.next.next.prev = t;
                t.next = t.next.next;
            }
        }
        checkForUnaryMinus(t);
    }

    private void checkForUnaryMinus(Token t) {
        if (t.prev.getType() != Token.OP || !t.prev.getContent().matches("\\+|-")) {
            return;
        }
        if (t.prev.prev.getType() == Token.EMPTY || t.prev.prev.getType() == Token.BRACE_OPEN ||
            (t.prev.prev.getType() == Token.OP && t.prev.prev.getContent().matches("\\+\\+|--"))) {
            if (t.val == null && (t.getType() == Token.KEYWORD && t.getContent().matches("new|yield") || t.getType() == Token.VAR_NAME)) {
                if (t.next != null && (t.getType() == Token.KEYWORD && t.getContent().matches("new|yield") || t.getType() == Token.VAR_NAME && t.next.getType() == Token.DOT ||
                    t.next.getType() == Token.ARRAY_START || t.getType() == Token.BRACE_OPEN)) {
                    accessObjectProperties(t);
                    return;
                } else {
                    t.val = Expression.getVar(t.getContent(), this);
                }
            }
            if (t.val != null && t.val.getType().equals("Float") && t.prev.getContent().equals("-")) {
                t.val = new JSFloat(((JSFloat)t.val).getValue() * (-1));
                t.setContent(t.val.toString());
            } else if (t.val != null && t.val.getType().equals("Integer") && t.prev.getContent().equals("-")) {
                t.val = new JSInt(((JSInt)t.val).getValue() * (-1));
                t.setContent(t.val.toString());
            } else if (t.val != null && !t.val.getType().matches("Integer|Float")) {
                t.val = t.val.asInt();
                if (t.prev.getContent().equals("-")) {
                    t.val = new JSInt(((JSInt)t.val).getValue() * (-1));
                }
                t.setContent(t.val.toString());
            }
            t.prev.prev.next = t;
            if (t.prev == start) {
                start = t;
            }
            t.prev = t.prev.prev;
        }
    }

    private void functionCall(Token t) {
        while (t != null && t.getType() != Token.BRACE_OPEN) {
            t = t.prev;
        }
        if (t.prev.getType() != Token.VAR_NAME &&
                t.prev.getType() != Token.OBJECT_ENTITY) {
            String name = t.prev.index != null ? t.prev.index.asString().getValue() : t.prev.getContent();
            Token ts = t;
            t = t.next;
            while (t.getType() != Token.BRACE_CLOSE) {
                t = t.next;
            }
            ts.prev.next = t.next;
            if (t.next != null) {
                if (t.next == end) {
                    end = ts.prev;
                }
                t.next.prev = ts.prev;
            }
            JSError e = new JSError(null, name + " is not a function", getStack());
            parent_block.error = e;
            thr = true;
            return;
        }
        Token ts = t;
        if (t.prev.getType() != Token.OBJECT_ENTITY && t.prev.prev.getType() != Token.EMPTY && t.prev.prev.getType() != Token.SEMICOLON
                && t.prev.prev.getType() != Token.OP && t.prev.prev.getType() != Token.KEYWORD && t.prev.prev.getType() != Token.BRACE_OPEN) {
            int level = 0;
            while (t.getType() != Token.EMPTY && t.getType() != Token.OP && level <= 0 ||
                    t.getType() == Token.OP && level < 0) {
                if (t.prev.getType() == Token.BRACE_OPEN) level++;
                if (t.prev.getType() == Token.BRACE_CLOSE) level--;
                t = t.prev;
            }
            accessObjectProperties(t.next);
            return;
        }
        if (t.prev.getType() == Token.VAR_NAME && t.prev.val == null) {
            t.prev.val = Expression.getVar(t.prev.getContent(), this);
        }
        if (t == null || t.prev.getType() == Token.EMPTY ||
                t.prev.getType() == Token.SEMICOLON || t.prev.getType() == Token.VALUE) {
            JSError e = new JSError(null, "SyntaxError: function call error", parent_block.getStack());
            parent_block.error = e;
            System.err.println("SyntaxError: function call error");
            thr = true;
            return;
        }
        Vector<JSValue> params = new Vector<JSValue>();
        t = t.next;
        while (t != null && t.getType() != Token.BRACE_CLOSE) {
            if (t.getType() == Token.VALUE) {
                params.add(JSValue.create(JSValue.getType(t.getContent()), t.getContent()));
            } else if ((t.getType() == Token.VAR_NAME || t.getType() == Token.BRACE_OPEN || t.getType() == Token.KEYWORD && t.getContent().matches("new|yield")) && t.val == null) {
                if (!(t.next.getType() == Token.OP && t.next.getContent().equals(",")) && t.next.getType() != Token.BRACE_CLOSE) {
                    accessObjectProperties(t);
                    t = t.prev.next;
                    params.add(t.val);
                } else {
                    params.add(Expression.getVar(t.getContent(), this));
                }
            } else if (t.val != null) {
                params.add(t.val);
            }
            t = t.next;
        }
        if (t == null) {
            JSError e = new JSError(null, "SyntaxError: missing ')' after function arguments list", parent_block.getStack());
            parent_block.error = e;
            System.err.println("SyntaxError: missing ')' after function arguments list");
            thr = true;
            return;
        }
        if (t.prev.getType() == Token.OP && t.prev.getContent().equals(",")) {
            JSError e = new JSError(null, "Hanging comma is not allowed in function arguments list", parent_block.getStack());
            parent_block.error = e;
            System.err.println("Hanging comma is not allowed in function arguments list");
            thr = true;
            return;
        }
        boolean as_constr = false;
        ((Function)ts.prev.val).setCaller(parent_block);
        if (ts.prev.ctx == null) ts.prev.ctx = Expression.getVar("window", this); 
        if (ts.prev.prev.getType() == Token.KEYWORD && ts.prev.prev.getContent().equals("new")) {
            as_constr = true;
            ts.prev.prev.prev.next = ts.prev;
            if (ts.prev.prev == start) {
                start = ts.prev;
            }
            ts.prev.prev = ts.prev.prev.prev;
        }
        JSValue result = ts.prev.val.call((JSObject)ts.prev.ctx, params, as_constr);
        if (ts.prev.getContent().equals("setTimeout") && ((JSObject)ts.prev.ctx).equals(getVar("window", this))) {
            silent = true;
            if (display_timers) {
                System.out.println(">Timer " + result + " set");
            }
        }
        String str = result.toString();
        if (result.getType().equals("Array")) str = "[]";
        if (result.getType().equals("Object")) str = "{}";
        if (result.getType().equals("Function")) str = "{Function}";
        Token rt = new Token(str);
        rt.val = result;
        ts.prev.prev.next = rt;
        rt.prev = ts.prev.prev;
        if (ts.prev == start) {
            start = rt;
        }
        if (t.next != null) t.next.prev = rt;
        rt.next = t.next;
        if (t.next != null && (t.next.getType() == Token.ARRAY_START || t.next.getType() == Token.DOT)) {
            accessObjectProperties(rt);
        }
        else if (t.next != null && (t.next.getType() == Token.BRACE_OPEN)) {
            functionCall(t.next);
        }
        else {
            checkYield(rt);
            removeBraces(rt);
        }
    }

    private String getResultType(String types, String op) {
        String type = "";
        if (types.contains("Integer")) type = "Integer";
        if (types.contains("Float")) type = "Float";
        if (types.contains("String")) type = "String";
        if (types.contains("Number")) type = "Number";
        if (types.contains("NaN")) type = "NaN";
        if (type.equals("String") && op.matches("[*/%-]")) {
            System.err.println("Operator " + op + " cannot be applied to type String");
            return "error";
        }
        if (types.equals("Array|Array")) type = "Array";
        if (types.equals("null|null")) type = "null";
        if (types.contains("undefined") && (types.contains("Integer") || types.contains("Float"))) {
            type = "NaN";
        }
        if (type.isEmpty()) type = "Boolean";
        return type;
    }

    @Override
    public Expression clone() {
       Token t2prev = new Token("");
       Token th = t2prev;
       Token t = start;
       t.prev = start.prev;
       while (t != null) {
           String value = t.getContent();
           if (t.getType() == Token.FIELD_NAME) {
               value = "<" + value + ">";
           }
           Token t2 = new Token(value);
           if (t.val != null && t.val.getType().equals("Function")) {
               t2.val = ((Function)t.val).clone();
           }
           t2.prev = t2prev;
           t2prev.next = t2;
           t = t.next;
           t2prev = t2;
       }
       Expression e = new Expression(th.next, parent_block);
       e.silent = silent;
       e.ret = ret;
       e.thr = thr;
       e.source = source;
       e.mode = mode;
       return e;
    }

    public Expression eval() {
        if (isKeyword() && !getContent().matches("if|switch|case")) {
            if (start.getContent().equals("break")) {
                parent_block.state = Block.BREAK;
            }
            if (start.getContent().equals("continue")) {
                parent_block.state = Block.CONTINUE;
            }
            if (start.getContent().equals("return")) {
                parent_block.state = Block.RETURN;
            }
            return this;
        }
        if (reusable) {
            exec = this.clone();
            return exec.eval();
        }
        n = 0;
        applyBraces();
        process();
        if (parent_block.error != null) {
            return this;
        }
        initArraysAndObjects();

        source = "";
        updateSource();

        if (parent_block.error != null) {
            return this;
        }
        if (start.getContent().matches("if|switch|case")) {
            start = start.next;
        }
        while (start.next != null) {
            n++;
            if (n > 100) {
                System.out.println();
                System.err.println("Infinite loop detected");
                return this;
            }
            if (yt != null) return this;
            Token t = start;
            int p = 0;
            Token op = t;
            while (t != null) {
                if (t.getType() == Token.OP) {
                    if (t.p > p) {
                        p = t.p;
                        op = t;
                    }
                }
                t = t.next;
            }
            // No operators in chain
            if (p == 0) break;
            String c = op.getContent();
            if (c.equals("++") || c.equals("--")) {
                //++5, ++a
                if (op.next != null && (op.next.getType() == Token.VALUE || op.next.getType() == Token.VAR_NAME)) {
                    if (op.next.getType() == Token.VALUE) {
                        op.next.val = JSValue.create(JSValue.getType(op.next.getContent()), op.next.getContent());
                        if (!op.next.val.getType().equals("Integer") && !op.next.val.getType().equals("Float")) {
                            System.err.println("Invalid " + c + " operator");
                            break;
                        }
                        if (op.next.val.getType().equals("Integer")) {
                            op.next.val = c.equals("++") ? new JSInt(((JSInt)op.next.val).getValue()+1) :
                                                           new JSInt(((JSInt)op.next.val).getValue()-1);
                        } else {
                            op.next.val = c.equals("++") ? new JSFloat(((JSFloat)op.next.val).getValue()+1) :
                                                           new JSFloat(((JSFloat)op.next.val).getValue()-1);
                        }
                        op.next.setContent(op.next.val.toString());
                    } else if (op.next != null &&
                            (op.next.getType() == Token.VAR_NAME || op.next.getType() == Token.ARRAY_ENTITY || op.next.getType() == Token.OBJECT_ENTITY) &&
                            op.next.next != null && (op.next.next.getType() == Token.DOT ||
                            op.next.next.getType() == Token.ARRAY_START)) {
                        
                        accessObjectProperties(op.next);

                        JSValue result = c.equals("++") ? new JSInt(((JSInt)op.next.val).getValue() + 1) :
                                new JSInt(((JSInt)op.next.val).getValue() - 1);

                        if (op.next.getType() == Token.VAR_NAME && op.next.index != null) {
                            JSValue index = op.next.index;
                            if (index == null || op.next.ctx == null) {
                                break;
                            }
                            if (op.next.ctx.getType().equals("Array")) {
                                if (index instanceof JSString) {
                                    index = ((JSString)index).parseInt();
                                    if (index instanceof NaN) {
                                        System.err.println("Incorrect field access");
                                        break;
                                    }
                                } else {
                                    index = index.asInt();
                                }
                                ((JSArray)op.next.ctx).set((int)((JSInt)index).getValue(), result);
                            }
                            if (op.next.ctx.getType().matches("Object|Function")) {
                                ((JSObject)op.next.ctx).set(index.asString().getValue(), result);
                            }
                            op.next.val = result;
                            op.next.index = null;
                        } else {
                            System.err.println("Assignment error");
                        }
                    } else {
                        JSValue val = Expression.getVar(op.next.getContent(), this);
                        if (!val.getType().equals("Integer")) {
                            System.err.println("Incorrect increment operator");
                        } else {
                            if (c.equals("++")) {
                                Expression.setVar(op.next.getContent(), new JSInt(((JSInt)val).getValue() + 1), this, mode);
                            } else {
                                Expression.setVar(op.next.getContent(), new JSInt(((JSInt)val).getValue() - 1), this, mode);
                            }
                        }
                    }
                    if (op.prev != null) {
                        op.prev.next = op.next;
                    }
                    op.next.prev = op.prev;
                    if (start == op) start = op.next;
                    // (a) -> a
                    removeBraces(op.next);
                    op = null;
                }
                else {  //5++, a++
                    if (op.prev.getType() == Token.VAR_NAME) {
                        JSValue val = Expression.getVar(op.prev.getContent(), this);
                        if (!val.getType().equals("Integer")) {
                            System.err.println("Incorrect increment operator");
                        } else {
                            if (c.equals("++")) {
                                Expression.setVar(op.prev.getContent(), new JSInt(((JSInt)val).getValue() + 1), this, mode);
                            } else {
                                Expression.setVar(op.prev.getContent(), new JSInt(((JSInt)val).getValue() - 1), this, mode);
                            }
                            op.prev.val = val;
                        }
                    } else if (op.prev.getType() != Token.VALUE) {
                        if (op.prev.val == null && (op.prev.getType() == Token.VAR_NAME || op.prev.getType() == Token.ARRAY_ENTITY || op.prev.getType() == Token.OBJECT_ENTITY) &&
                            op.prev != start && op.prev.prev.getType() == Token.DOT ||
                            op.prev.getType() == Token.ARRAY_END) {
                            Token ct = op.prev;
                            int level = 0;
                            while (!(ct.getType() == Token.OP && level == 0 ||
                                    ct.getType() == Token.EMPTY || ct.getType() == Token.SEMICOLON)) {
                                if (ct.getType() == Token.BRACE_OPEN) level++;
                                if (ct.getType() == Token.BRACE_CLOSE) level--;
                                ct = ct.prev;
                            }
                            accessObjectProperties(ct.next);

                            JSValue result = c.equals("++") ? new JSInt(((JSInt)op.prev.val).getValue() + 1) :
                                    new JSInt(((JSInt)op.prev.val).getValue() - 1);

                            if (op.prev.getType() == Token.VAR_NAME && op.prev.index != null) {
                                JSValue index = op.prev.index;
                                if (index == null || op.prev.ctx == null) {
                                    break;
                                }
                                if (op.prev.ctx.getType().equals("Array")) {
                                    if (index instanceof JSString) {
                                        index = ((JSString)index).parseInt();
                                        if (index instanceof NaN) {
                                            System.err.println("Incorrect field access");
                                            break;
                                        }
                                    } else {
                                        index = index.asInt();
                                    }
                                    ((JSArray)op.prev.ctx).set((int)((JSInt)index).getValue(), result);
                                }
                                if (op.prev.ctx.getType().matches("Object|Function")) {
                                    ((JSObject)op.prev.ctx).set(index.asString().getValue(), result);
                                }
                                op.prev.index = null;
                            } else {
                                System.err.println("Assignment error");
                            }
                        }
                    }
                    op.prev.next = op.next;
                    // (a) -> a
                    removeBraces(op.prev);
                    op = null;
                }
            }
            if (c.equals("~")) {
                while (op.next != null && op.next.getType() == Token.OP) {
                    if (!op.next.getContent().matches("[+-!~]")) {
                        System.err.println("Unexpected " + op.next.getContent() + " operator");
                        break;
                    }
                    op = op.next;
                }
                if (op.next.getType() != Token.VALUE && op.next.getType() != Token.VAR_NAME) {
                    System.err.println("Invalid " + c + " operator");
                    break;
                }
                if (op.next.getType() == Token.VALUE) {
                    op.next.val = JSValue.create(JSValue.getType(op.next.getContent()), op.next.getContent());
                    op.next.val = new JSInt(~(op.next.val.asInt()).getValue());
                    op.next.setContent(op.next.val.toString());
                }
                if (op.prev != null) {
                    op.prev.next = op.next;
                }
                op.next.prev = op.prev;
                if (start == op) start = op.next;
            }
            if (c.equals("!")) {
                while (op.next != null && op.next.getType() == Token.OP) {
                    if (!op.next.getContent().matches("[!~+-]")) {
                        System.err.println("Unexpected " + op.next.getContent() + " operator");
                        break;
                    }
                    op = op.next;
                }
                if (op.next.getType() != Token.VALUE && op.next.getType() != Token.VAR_NAME) {
                    System.err.println("Invalid " + c + " operator");
                    break;
                }
                if (op.next.getType() == Token.VALUE) {
                    op.next.val = JSValue.create(JSValue.getType(op.next.getContent()), op.next.getContent());
                    op.next.val = new JSBool(!(op.next.val.asBool()).getValue());
                    op.next.setContent(op.next.val.toString());
                } else {
                    if (((op.next.getType() == Token.VAR_NAME || op.next.getType() == Token.ARRAY_ENTITY || op.next.getType() == Token.OBJECT_ENTITY ||
                      (op.next.getType() == Token.KEYWORD && op.next.getContent().matches("new|yield"))) &&
                       op.next.next != null && (op.next.next.getType() == Token.DOT ||
                       op.next.next.getType() == Token.ARRAY_START) || op.next.getType() == Token.BRACE_OPEN)) {
                        accessObjectProperties(op.next);
                    }
                    op.next.val = new JSBool(!(op.next.val.asBool()).getValue());
                    op.next.setContent(op.next.val.toString());
                }
                if (op.prev != null) {
                    op.prev.next = op.next;
                }
                op.next.prev = op.prev;
                if (start == op) start = op.next;
            }
            if (c.equals("typeof")) {
                while (op.next != null && op.next.getType() == Token.OP) {
                    if (!op.next.getContent().matches("[!~+-]|typeof")) {
                        System.err.println("Unexpected " + op.next.getContent() + " operator");
                        break;
                    }
                    op = op.next;
                }
                if (((op.next.getType() == Token.VAR_NAME || op.next.getType() == Token.ARRAY_ENTITY || op.next.getType() == Token.OBJECT_ENTITY ||
                      (op.next.getType() == Token.KEYWORD && op.next.getContent().matches("new|yield"))) &&
                       op.next.next != null && (op.next.next.getType() == Token.DOT ||
                       op.next.next.getType() == Token.ARRAY_START) || op.next.getType() == Token.BRACE_OPEN)) {
                        accessObjectProperties(op.next);
                }
                else if (op.next.getType() == Token.VAR_NAME && op.next.val == null) {
                    op.next.val = Expression.getVar(op.next.getContent(), this);
                } else if (op.next.getType() == Token.VALUE) {
                    op.next.val = JSValue.create(JSValue.getType(op.next.getContent()), op.next.getContent());
                }
                Token tn;
                if (op.next.val == null) {
                    tn = new Token("\"undefined\"");
                    tn.val = new JSString("undefined");
                } else {
                    String type = op.next.val.getType().toLowerCase();
                    if (type.matches("integer|float")) {
                        type = "number";
                    }
                    tn = new Token(type);
                    tn.val = new JSString(type);
                }
                op.prev.next = tn;
                tn.prev = op.prev;
                if (op == start) {
                    start = tn;
                }
                if (op.next.next != null) {
                    op.next.next.prev = tn;
                }
                tn.next = op.next.next; 
            }
            if (c.matches("!|~|typeof")) {
                // (a) -> a
                removeBraces(op.next);
                op = null;
            }
            if (c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/") || c.equals("%") ||
                 c.equals(">") || c.equals("<") || c.matches("[!<>=]=") || c.matches("[!=]==") ||
                 c.matches("<<|>>|>>>|&|\\^|\\|") || c.matches("&&|\\|\\||\\?|:|,") ||
                 c.matches("[\\*/%&|^=+-]?=") || c.equals("in") || c.equals("instanceof")) {
                if (c.matches("\\+|-") && (op.prev.getType() == Token.OP && !op.prev.getContent().matches("\\+\\+|--") ||
                        op.prev.getType() == Token.EMPTY || op.prev.getType() == Token.BRACE_OPEN)) {
                    op.p = 0;
                    continue;
                }
                if (c.equals(",") && op.prev.prev != null && op.prev.prev.getType() == Token.BRACE_OPEN &&
                        op.prev.prev.prev != null && (op.prev.prev.prev.getType() == Token.VAR_NAME ||
                        op.prev.prev.prev.getType() == Token.OBJECT_ENTITY)) {
                    functionCall(op.prev.prev);
                    continue;
                }
                if (c.equals(",")) {
                    Token ct = op.prev;
                    int level = 0;
                    while (ct != null && ct.getType() != Token.BRACE_OPEN && level <= 0) {
                        if (ct.getType() == Token.BRACE_OPEN) level++;
                        if (ct.getType() == Token.BRACE_CLOSE) level--;
                        if (level > 0) {
                            break;
                        }
                        ct = ct.prev;
                    }
                    
                    if (op.next == null) {
                        parent_block.error = new JSError(null, "Hanging " + c + "operator is not allowed", getStack());
                        break;
                    }

                    if (ct.prev != null && ct.prev != null && ct.prev.getType() != Token.OP && ct.prev.getType() != Token.EMPTY) {
                        functionCall(ct);
                        continue;
                    }
                }
                if (op.prev == null || op.next == null) {
                    System.err.println("Syntax error");
                    break;
                }
                String types = JSValue.getType(op.prev.getContent()) + "|" + JSValue.getType(op.next.getContent());
                String type = getResultType(types, c);
                if (type.equals("error")) {
                    break;
                }
                if (!type.equals("Array") && !type.equals("Object")) {
                    if (op.prev.val == null && op.prev.getType() == Token.VALUE) op.prev.val = JSValue.create(type, op.prev.getContent());
                    if (op.next.val == null && op.next.getType() == Token.VALUE) op.next.val = JSValue.create(type, op.next.getContent());
                    if (op.prev.val == null && !op.getContent().equals(",") && (op.prev.getType() == Token.VAR_NAME || op.prev.getType() == Token.ARRAY_ENTITY || op.prev.getType() == Token.OBJECT_ENTITY) &&
                            op.prev != start && op.prev.prev.getType() == Token.DOT ||
                            op.prev.getType() == Token.ARRAY_END ||
                            op.prev.val == null && !op.getContent().equals(",") && op.prev.getType() == Token.BRACE_CLOSE) {
                        Token ct = op.prev;
                        int level = 0;
                        while (!((ct.getType() == Token.OP || ct.getType() == Token.BRACE_OPEN) && level == 0 ||
                                ct.getType() == Token.EMPTY || ct.getType() == Token.SEMICOLON)) {
                            if (ct.getType() == Token.BRACE_OPEN) level++;
                            if (ct.getType() == Token.BRACE_CLOSE) level--;
                            ct = ct.prev;
                        }
                        //We need to travel until the head of the expression, it may be after "(",
                        //so we need to know whether the "(" symbol is part of the expression or is it the
                        //boundary
                        while (op.prev.getType() == Token.BRACE_CLOSE && ct.next.getType() == Token.BRACE_OPEN && ct.getType() != Token.EMPTY &&
                                ct.getType() != Token.SEMICOLON && ct.getType() == Token.OP && ct.getType() == Token.BRACE_OPEN) {
                            level = 0;
                            ct = ct.prev;
                            while (!((ct.getType() == Token.OP || ct.getType() == Token.BRACE_OPEN) && level == -1 ||
                                    ct.getType() == Token.EMPTY || ct.getType() == Token.SEMICOLON)) {
                                if (ct.getType() == Token.BRACE_OPEN) level++;
                                if (ct.getType() == Token.BRACE_CLOSE) level--;
                                ct = ct.prev;
                            }
                        }
                        accessObjectProperties(ct.next);
                        types = (op.prev.val != null ? op.prev.val.getType() : "null") + "|" + (op.next.val != null ? op.next.val.getType() : JSValue.getType(op.next.getContent()));
                        type = getResultType(types, c);
                    }
                    if (op.next.next != null && op.next.next.getType() == Token.BRACE_OPEN) {
                        functionCall(op.next.next);
                    }
                    if (op.next != null && op.next.getType() == Token.KEYWORD && op.next.getContent().equals("new") &&
                            op.next.next.next != null && op.next.next.next.getType() == Token.BRACE_OPEN) {
                        functionCall(op.next.next.next);
                    }
                    if (op.next != null && !op.getContent().matches("&&|\\|\\||,") &&
                            ((op.next.getType() == Token.VAR_NAME || op.next.getType() == Token.ARRAY_ENTITY || op.next.getType() == Token.OBJECT_ENTITY ||
                             (op.next.getType() == Token.KEYWORD && op.next.getContent().matches("new|yeild"))) &&
                            op.next.next != null && (op.next.next.getType() == Token.DOT ||
                            op.next.next.getType() == Token.ARRAY_START) || op.next.getType() == Token.BRACE_OPEN)) {
                        accessObjectProperties(op.next);
                        types = types.split("\\|")[0] + "|" + op.next.val.getType();
                        type = getResultType(types, c);
                    }
                    if (op.next != null && op.next.getType() == Token.KEYWORD && op.next.getContent().equals("yield") &&
                            op.next.next != null && op.next.next.getType() == Token.VALUE) {
                        checkYield(op.next.next);
                        return this;
                    }
                    if (!op.getContent().equals("=") && op.prev.val == null && op.prev.getType() == Token.VAR_NAME) {
                        JSValue val = Expression.getVar(op.prev.getContent(), this);
                        if (val == null) {
                            break;
                        }
                        if (!op.getContent().matches("[\\*/%&|^+-]?=")) {
                            Token t2 = new Token(val.toString());
                            t2.val = val;
                            t2.prev = op.prev.prev;
                            t2.next = op;
                            if (start == op.prev) {
                                start = t2;
                            }
                            if (op.prev.prev != null) {
                                op.prev.prev.next = t2;
                            }
                            op.prev = t2;
                            types = op.prev.val.getType() + "|" + types.split("\\|")[1];
                            type = getResultType(types, c);
                        } else {
                            op.prev.val = val;
                        }
                    }
                    if (op.next.val == null && op.next.getType() == Token.VAR_NAME && !op.getContent().matches("&&|\\|\\||,")) {
                        JSValue val = Expression.getVar(op.next.getContent(), this);
                        if (val == null) {
                            break;
                        }
                        Token t2 = new Token(val.toString());
                        t2.val = val;
                        t2.next = op.next.next;
                        t2.prev = op;
                        if (end == op.next) {
                            end = t2;
                        }
                        if (op.next.next != null) {
                            op.next.next.prev = t2;
                        }
                        op.next = t2;
                        types = types.split("\\|")[0] + "|" + op.next.val.getType();
                        type = getResultType(types, c);
                        if (c.equals("in") || c.equals("instanceof")) {
                            type = "Boolean";
                        }
                    }
                    if (c.equals("=") && op.next.getType() != Token.VAR_NAME && op.next.val != null) {
                        type = op.next.val.getType();
                    } else if (c.equals("=") && (op.next.getType() == Token.VAR_NAME || op.next.getType() == Token.VALUE) && op.next.val == null) {
                        JSValue val = Expression.getVar(op.next.getContent(), this);
                        if (val != null) type = val.getType();
                    } else if (c.matches("[\\*/%&|^+-]=")) {
                        types = op.prev.val.getType() + "|" + op.next.val.getType();
                        type = getResultType(types, c);
                    }
                }
                if (op.prev.val != null && !op.prev.val.getType().equals(type) && !type.equals("Boolean")) {
                    if (op.prev.val instanceof JSDate) {
                        if (type.equals("Integer")) op.prev.val = op.prev.val.asInt();
                        else if (type.equals("Float")) op.prev.val = op.prev.val.asFloat();
                        else if (type.equals("String")) op.prev.val = op.prev.val.asString();
                    } else {
                        op.prev.val = JSValue.create(type, op.prev.val.asString().getValue());
                    }
                }
                if (!c.equals("=") && op.next.val != null && !op.next.val.getType().equals(type) && !type.equals("Boolean") && !type.equals("Number")) {
                    if (op.next.val instanceof JSDate) {
                        if (type.equals("Integer")) op.next.val = op.next.val.asInt();
                        else if (type.equals("Float")) op.next.val = op.next.val.asFloat();
                        else if (type.equals("String")) op.next.val = op.next.val.asString();
                    } else {
                        op.next.val = JSValue.create(type, op.next.val.asString().getValue());
                    }
                }
                JSValue result = null;
                if (c.matches("\\+=?")) {
                    if (type.equals("Array"))
                       result = ((JSArray)op.prev.val).concat((JSArray)op.next.val);
                    else if (type.equals("String"))
                       result = JSValue.create(type, ((JSString)op.prev.val).getValue() + ((JSString)op.next.val).getValue());
                    else if (type.equals("Float"))
                       result = JSValue.create(type, String.valueOf(((JSFloat)op.prev.val).getValue() + ((JSFloat)op.next.val).getValue()));
                    else if (type.equals("Integer"))
                       result = JSValue.create(type, String.valueOf(((JSInt)op.prev.val).getValue() + ((JSInt)op.next.val).getValue()));
                    else if (type.equals("Number")) {
                       if (op.prev.val instanceof Infinity && !(op.next.val instanceof Infinity) ||
                           !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity ||
                             op.prev.val instanceof Infinity && op.next.val instanceof Infinity && !(((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive()))
                          result = op.prev.val instanceof Infinity ? op.prev.val : op.next.val;
                       else if (op.prev.val instanceof Infinity && op.next.val instanceof Infinity && (((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive())) {
                          type = "Integer";
                          result = new JSInt(0);
                       }
                    } else if (type.equals("NaN"))
                       result = NaN.getInstance();
                }
                else if (c.matches("-=?")) {
                    if (type.equals("Float"))
                       result = JSValue.create(type, String.valueOf(((JSFloat)op.prev.val).getValue() - ((JSFloat)op.next.val).getValue()));
                    else if (type.equals("Integer"))
                       result = JSValue.create(type, String.valueOf(((JSInt)op.prev.val).getValue() - ((JSInt)op.next.val).getValue()));
                    else if (type.equals("Number")) {
                        if (op.prev.val instanceof Infinity && !(op.next.val instanceof Infinity) ||
                           !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity ||
                             op.prev.val instanceof Infinity && op.next.val instanceof Infinity && (((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive()))
                          result = op.prev.val instanceof Infinity ? op.prev.val : op.next.val;
                       else if (op.prev.val instanceof Infinity && op.next.val instanceof Infinity && !(((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive())) {
                          type = "Integer";
                          result = new JSInt(0);
                       }
                    } else if (type.equals("NaN"))
                       result = NaN.getInstance();
                }
                else if (c.matches("\\*=?")) {
                    if (type.equals("Float"))
                       result = JSValue.create(type, String.valueOf(((JSFloat)op.prev.val).getValue() * ((JSFloat)op.next.val).getValue()));
                    else if (type.equals("Integer"))
                       result = JSValue.create(type, String.valueOf(((JSInt)op.prev.val).getValue() * ((JSInt)op.next.val).getValue()));
                    else if (type.equals("Number"))
                       if (!(op.prev.val instanceof Infinity) && op.prev.val.asFloat().getValue() == 0 ||
                           !(op.next.val instanceof Infinity) && op.next.val.asFloat().getValue() == 0) {
                           type = "NaN";
                           result = NaN.getInstance();
                       } else {
                           boolean s1 = (op.prev.val instanceof Infinity && ((Infinity)op.prev.val).isPositive()) || !(op.prev.val instanceof Infinity) && op.prev.val.asFloat().getValue() > 0;
                           boolean s2 = (op.next.val instanceof Infinity && ((Infinity)op.next.val).isPositive()) || !(op.next.val instanceof Infinity) && op.next.val.asFloat().getValue() > 0;
                           result = Infinity.getInstance(!(s1 ^ s2));
                       }
                    else if (type.equals("NaN"))
                       result = new NaN();
                }
                else if (c.matches("/=?")) {
                    if (op.next.getContent().equals("0")) {
                        if (op.prev.getContent().equals("0")) {
                            type = "NaN";
                            result = NaN.getInstance();
                        } else {
                            boolean sign = (op.prev.val instanceof Infinity && ((Infinity)op.prev.val).isPositive()) || !(op.prev.val instanceof Infinity) && op.prev.val.asFloat().getValue() > 0;
                            type = "Number";
                            result = Infinity.getInstance(sign);
                        }
                    }
                    
                    else if (type.equals("Float"))
                       result = JSValue.create(type, String.valueOf(((JSFloat)op.prev.val).getValue() / ((JSFloat)op.next.val).getValue()));
                    else if (type.equals("Integer"))
                       result = JSValue.create(type, String.valueOf(((JSInt)op.prev.val).getValue() / ((JSInt)op.next.val).getValue()));
                    else if (type.equals("Number"))
                       if (op.prev.val instanceof Infinity && op.next.val instanceof Infinity) {
                           result = NaN.getInstance();
                       } else if (!(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity) {
                           result = new JSInt(0);
                       } else if (op.prev.val instanceof Infinity && !(op.next.val instanceof Infinity)) {
                           result = op.prev.val;
                       }
                    else if (type.equals("NaN"))
                       result = NaN.getInstance();
                }
                else if (c.matches("%=?")) {
                    if (op.next.getContent().equals("0")) { result = new NaN(); type = "NaN"; }

                    else if (type.equals("Float"))
                       result = JSValue.create(type, String.valueOf(((JSFloat)op.prev.val).getValue() % ((JSFloat)op.next.val).getValue()));
                    else if (type.equals("Integer"))
                       result = JSValue.create(type, String.valueOf(((JSInt)op.prev.val).getValue() % ((JSInt)op.next.val).getValue()));
                    else if (type.equals("Number"))
                       result = op.prev.val instanceof Infinity ? NaN.getInstance() : JSValue.create(op.prev.val.getType(), String.valueOf(op.prev.val.getType().charAt(0) == 'I' ? ((JSInt)op.prev.val).getValue() : ((JSFloat)op.prev.val).getValue()));
                    else if (type.equals("NaN"))
                       result = NaN.getInstance();
                }
                else if (c.equals(">")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) > 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() > ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() > ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool(op.prev.val instanceof Infinity && ((Infinity)op.prev.val).isPositive() && (!(op.next.val instanceof Infinity) || !((Infinity)op.next.val).isPositive()) ||
                                         !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity && !((Infinity)op.next.val).isPositive());
                    else if (type.equals("NaN"))
                       result = new JSBool(false);
                }
                else if (c.equals(">=")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) >= 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() >= ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() >= ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool(op.prev.val instanceof Infinity && ((Infinity)op.prev.val).isPositive() && (!(op.next.val instanceof Infinity) || !((Infinity)op.next.val).isPositive()) ||
                                         !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity && !((Infinity)op.next.val).isPositive());
                    else if (type.equals("NaN"))
                       result = new JSBool(false);
                }
                else if (c.equals("<")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) < 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() < ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() < ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool(op.prev.val instanceof Infinity && !((Infinity)op.prev.val).isPositive() && (!(op.next.val instanceof Infinity) || ((Infinity)op.next.val).isPositive()) ||
                                         !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity && ((Infinity)op.next.val).isPositive());
                    else if (type.equals("NaN"))
                       result = new JSBool(false);
                }
                else if (c.equals("<=")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) <= 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() <= ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() <= ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool(op.prev.val instanceof Infinity && !((Infinity)op.prev.val).isPositive() && (!(op.next.val instanceof Infinity) || ((Infinity)op.next.val).isPositive()) ||
                                         !(op.prev.val instanceof Infinity) && op.next.val instanceof Infinity && ((Infinity)op.next.val).isPositive());
                    else if (type.equals("NaN"))
                       result = new JSBool(false);
                }
                else if (c.equals("==")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) == 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() == ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() == ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool((op.prev.val instanceof Infinity && op.next.val instanceof Infinity &&
                               !(((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive())) ||
                               (!(op.prev.val instanceof Infinity) && !(op.next.val instanceof Infinity)));
                    else if (type.equals("NaN"))
                       result = new JSBool(types.equals("NaN|NaN"));
                }
                else if (c.equals("!=")) {
                    if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) != 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() != ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() != ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool(!(op.prev.val instanceof Infinity && op.next.val instanceof Infinity &&
                               !(((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive())) &&
                               !(!(op.prev.val instanceof Infinity) && !(op.next.val instanceof Infinity)));
                    else if (type.equals("NaN"))
                       result = new JSBool(!types.equals("NaN|NaN"));
                }
                else if (c.equals("===")) {
                    String[] orig_types = types.split("|");
                    if (!orig_types[0].equals(orig_types[1])) {
                        result = new JSBool(false);
                    } else if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) == 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() == ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() == ((JSInt)op.next.val).getValue());
                    else if (type.equals("Number"))
                       result = new JSBool((op.prev.val instanceof Infinity && op.next.val instanceof Infinity &&
                               !(((Infinity)op.prev.val).isPositive() ^ ((Infinity)op.next.val).isPositive())) ||
                               (!(op.prev.val instanceof Infinity) && !(op.next.val instanceof Infinity)));
                    else if (type.equals("NaN"))
                       result = new JSBool(false);
                }
                else if (c.equals("!==")) {
                    String[] orig_types = types.split("\\|");
                    if (!orig_types[0].equals(orig_types[1])) {
                        result = new JSBool(true);
                    } else if (type.equals("String"))
                       result = new JSBool(((JSString)op.prev.val).getValue().compareTo(((JSString)op.next.val).getValue()) != 0);
                    else if (type.equals("Float"))
                       result = new JSBool(((JSFloat)op.prev.val).getValue() != ((JSFloat)op.next.val).getValue());
                    else if (type.equals("Integer"))
                       result = new JSBool(((JSInt)op.prev.val).getValue() != ((JSInt)op.next.val).getValue());
                    else if (type.equals("NaN"))
                       result = new JSBool(!types.equals("NaN|NaN"));
                }
                else if (c.equals("instanceof")) {
                    if (!op.next.val.getType().equals("Function")) {
                        parent_block.error = new JSError(null, "Second argument of instanceof is not a function", getStack());
                        break;
                    }
                    boolean value = false;
                    if (op.prev.val.getType().matches("Integer|Float")) {
                        value = op.next.val.equals(Expression.getVar("Number", this));
                    } else if (op.prev.val.getType().matches("Array")) {
                        value = op.next.val.equals(Expression.getVar("Array", this));
                    } else if (op.prev.val.getType().matches("String")) {
                        value = op.next.val.equals(Expression.getVar("String", this));
                    } else {
                        JSObject obj = (JSObject)((JSObject)op.prev.val).get("__proto__");
                        while (obj != ObjectProto.getInstance()) {
                            if (obj.get("constructor").equals(op.next.val)) {
                                value = true;
                                break;
                            }
                            obj = (JSObject)obj.get("__proto__");
                        }
                        if (obj == ObjectProto.getInstance()) {
                            value = op.next.val.equals(Expression.getVar("Object", this));
                        }
                    }
                    result = new JSBool(value);
                }
                else if (c.equals("in")) {
                    if (!op.next.val.getType().matches("Array|Object")) {
                        parent_block.error = new JSError(null, "Second argument of instanceof is not a function", getStack());
                        break;
                    }
                    boolean value = false;
                    if (op.prev.val.getType().matches("Integer|String")) {
                        if (op.next.val instanceof JSArray && op.prev.val.getType().equals("Integer")) {
                            JSArray a = ((JSArray)op.next.val);
                            int index = (int)((JSInt)op.prev.val).getValue();
                            value = index < a.length().getValue() && a.get(index) != Undefined.getInstance();
                        }
                        if (op.next.val instanceof JSObject && op.prev.val.getType().equals("String")) {
                            JSObject o = ((JSObject)op.next.val);
                            String index = ((JSString)op.prev.val).getValue();
                            value = o.get(index) != Undefined.getInstance();
                        }
                    } else {
                        value = false;
                    }
                    result = new JSBool(value);
                }
                if (c.matches("(<<|>>|>>>|&|\\^=|\\|)=?|~|!")) {
                    String[] orig_types = types.split("\\|");
                    Token[] a = new Token[2];
                    a[0] = op.prev;
                    a[1] = op.next;
                    for (int i = 0; i < 2; i++) {
                        if (orig_types[i].equals("String")) {
                            int value = 0;
                            try {
                                value = Integer.parseInt(a[i].getContent());
                            } catch(NumberFormatException e) {}
                            a[i].val = new JSInt(value);
                            a[i].setContent(String.valueOf(value));
                        } else if (orig_types[i].equals("Float")) {
                            int value = 0;
                            try {
                                value = (int)Float.parseFloat(a[i].getContent());
                            } catch(NumberFormatException e) {}
                            a[i].val = new JSInt(value);
                            a[i].setContent(String.valueOf(value));
                        }
                    }
                    type = "Integer";
                }
                if (c.matches("[*/%&|^+-]?=")) {
                    while (op.next.next != null && op.next.next.getType() == Token.OP &&
                            op.next.next.getContent().matches("[*/%&|^+-]?=")) {
                        op = op.next.next;
                    }
                    if (c.equals("=")) result = op.next.val;
                    if (op.prev.getType() == Token.VAR_NAME && op.prev.index != null) {
                        JSValue index = op.prev.index;
                        if (index == null || op.prev.ctx == null) {
                            break;
                        }
                        if (op.prev.ctx.getType().equals("Array")) {
                            if (index instanceof JSString) {
                                index = ((JSString)index).parseInt();
                                if (index instanceof NaN) {
                                    parent_block.error = new JSError(null, "Incorrect field access", getStack());
                                    break;
                                }
                            } else {
                                index = index.asInt();
                            }
                            ((JSArray)op.prev.ctx).set((int)((JSInt)index).getValue(), result);
                        }
                        if (op.prev.ctx.getType().matches("Object|Function")) {
                            JSValue old_val = ((JSObject)op.prev.ctx).get(index.asString().getValue());
                            if (!(old_val != null && old_val == result)) {
                                if (old_val != null) {
                                    old_val.decrementRefCount();
                                }
                            }
                            ((JSObject)op.prev.ctx).set(index.asString().getValue(), result);
                        }
                        op.prev.val = result;
                        op.prev.index = null;
                    } else {
                        if (op.prev.getType() != Token.VAR_NAME) {
                            parent_block.error = new JSError(null, "Assignment error", getStack());
                        } else {
                            JSValue val = Expression.getVar(op.prev.getContent(), this);
                            if (val != result) {
                                if (val != Undefined.getInstance()) {
                                    val.decrementRefCount();
                                }
                                result.incrementRefCount();
                            }
                            Expression.setVar(op.prev.getContent(), result, this, mode);
                        }
                    }
                }
                if (c.equals(">>")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() >> ((JSInt)op.next.val).getValue());
                }
                else if (c.equals("<<")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() << ((JSInt)op.next.val).getValue());
                }
                else if (c.equals(">>>")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() >>> ((JSInt)op.next.val).getValue());
                }
                else if (c.equals("&")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() & ((JSInt)op.next.val).getValue());
                }
                else if (c.equals("|")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() | ((JSInt)op.next.val).getValue());
                }
                else if (c.equals("^")) {
                    result = new JSInt(((JSInt)op.prev.val).getValue() ^ ((JSInt)op.next.val).getValue());
                }
                if (c.equals("&&") || c.equals("||") || c.equals(">") || c.equals("<") ||
                        c.matches("[!<>=]=") || c.matches("[!=]==")) {
                    type = "Boolean";
                }
                if (c.equals("&&") || c.equals("||")) {
                    if (op.prev.val == null) {
                        op.prev.val = JSValue.create(JSValue.getType(op.prev.getContent()), op.prev.getContent());
                    }
                    boolean val = op.prev.val.asBool().getValue();
                    Token ct = op.next;
                    Token ct2 = op.prev;
                    int level = 0;
                    while (ct != null) {
                        if (ct.getType() == Token.BRACE_OPEN) level++;
                        if (ct.getType() == Token.BRACE_CLOSE) level--;
                        if (ct.getType() == Token.VALUE || ct.getType() == Token.VAR_NAME) {
                            if (ct.val == null && (c.equals("&&") && val || c.equals("||") && !val)) {
                                Token tt = ct.prev;
                                accessObjectProperties(ct);
                                ct = tt.next;
                            }
                            if (c.equals("&&") && val && !ct.val.asBool().getValue()) {
                                val = false;
                            }
                            if (c.equals("||") && !val && ct.val.asBool().getValue()) {
                                ct2 = ct;
                                val = true;
                            }
                            if (c.equals("&&") && val) ct2 = ct;
                        }
                        if (ct.getType() == Token.OP && ct.p < p ||
                                ct.getType() == Token.BRACE_CLOSE && level == -1) {
                            break;
                        }
                        ct = ct.next;
                    }
                    op.prev.val = ct2.val;
                    op.prev.setContent(ct2.getContent());
                    op.prev.next = ct;
                    if (ct != null) {
                        ct.prev = op.prev;
                    }
                    if (ct == null) {
                        end = op.prev;
                    }
                    // (a) -> a
                    removeBraces(op.prev);
                    continue;
                }

                if (c.equals("?")) {
                    Token op2 = null;
                    Token ct = op.next;
                    int level = 0;
                    if (ct == null) {
                        parent_block.error = new JSError(null, "Incorrect operator " + c, getStack());
                        break;
                    }
                    while (ct.next != null) {
                        if (ct.getType() == Token.BRACE_OPEN) level++;
                        if (ct.getType() == Token.BRACE_CLOSE) level--;
                        if (ct.getType() == Token.OP && ct.getContent().equals(":") && ct.p == op.p) {
                            op2 = ct;
                        }
                        if (ct.getType() == Token.OP && ct.p < op.p ||
                                ct.getType() == Token.BRACE_CLOSE && level == -1) {
                            if (ct.getType() == Token.BRACE_CLOSE) ct = ct.prev;
                            break;
                        }
                        ct = ct.next;
                    }
                    if (op2 == null) {
                        parent_block.error = new JSError(null, "Incorrect operator " + c, getStack());
                        break;
                    }
                    if (op.prev.val == null) {
                        op.prev.val = JSValue.create(JSValue.getType(op.prev.getContent()), op.prev.getContent());
                    }
                    if (op.prev.val.asBool().getValue()) {
                        op.prev.prev.next = op.next;
                        op.next.prev = op.prev.prev;
                        if (op.prev == start) {
                            start = op.next;
                        }
                        op2.prev.next = ct.next;
                        if (ct.next != null) {
                            ct.next.prev = op2.prev;
                        }
                    } else {
                        op2.prev.prev.next = op2.next;
                        op2.next.prev = op.prev.prev;
                        if (op.prev == start) {
                            start = op2.next;
                        }
                    }
                    // (a) -> a
                    removeBraces(op.prev);
                    continue;
                }

                if (c.equals(",")) {
                    boolean is_func_call = false;

                    Token ct = op.prev;
                    int level = 0;
                    while (ct != null && ct.getType() != Token.BRACE_OPEN && level <= 0) {
                        if (ct.getType() == Token.BRACE_OPEN) level++;
                        if (ct.getType() == Token.BRACE_CLOSE) level--;
                        if (level > 0) {
                            if (ct.prev.getType() == Token.ARRAY_END || ct.prev.getType() == Token.BRACE_CLOSE ||
                                   ct.prev.getType() == Token.OBJECT_ENTITY || ct.prev.getType() == Token.VAR_NAME) {
                                is_func_call = true;
                            }
                            break;
                        }
                        ct = ct.prev;
                    }
                    if (ct.prev != null && ct.prev != null && ct.prev.getType() != Token.OP && ct.prev.getType() != Token.EMPTY) {
                        functionCall(ct);
                    }
                    
                    ct = op.next;
                    Token op2 = op;
                    if (ct == null) {
                        parent_block.error = new JSError(null, "Hanging " + c + "operator is not allowed", getStack());
                        break;
                    }
                    
                    if (!is_func_call) {
                        level = 0;
                        while (ct.next != null) {
                            if (ct.getType() == Token.BRACE_OPEN) level++;
                            if (ct.getType() == Token.BRACE_CLOSE) level--;
                            if (ct.getType() == Token.OP && ct.getContent().equals(",") && ct.p == op.p) {
                                op2 = ct;
                            }
                            if (ct.getType() == Token.OP && ct.p < op.p ||
                                    ct.getType() == Token.BRACE_CLOSE && level == -1) {
                                ct = ct.prev;
                                break;
                            }
                            ct = ct.next;
                        }
                        if (op.prev.prev != null) {
                            op.prev.prev.next = op2.next;
                        }
                        op2.next.prev = op.prev.prev;
                        if (op.prev == start) {
                            start = op2.next;
                        }
                        // (a) -> a
                        removeBraces(op.prev);
                    }
                    continue;
                }

                if (result != null) {
                    if (!c.equals("=") && !type.equals("Array") && !type.equals("Object") && !type.equals("Function")) {
                        op.prev.val = JSValue.create(type, result.toString());
                        op.prev.setContent(result.toString());
                    } else {
                        op.prev.val = result;
                    }
                    op.prev.next = op.next.next;
                    if (op.next.next != null) {
                        op.next.next.prev = op.prev;
                    }
                    // (a) -> a
                    removeBraces(op.prev);
                    op = null;
                } else {
                    parent_block.error = new JSError(null, "Operator " + c + " is not supported", getStack());
                    return this;
                }
            }
        }
        if (start.val == null && start.getType() == Token.OP && start.getContent().matches("\\+|-")) {
            checkForUnaryMinus(start.next);
        }
        else if (start.val == null && start.getType() == Token.KEYWORD && start.getContent().equals("yield") &&
                start.next == null) {
            parent_block.error = new JSError(null, "Syntax error: value after yield expected", getStack());
            return this;
        }
        else if (start.val == null && start.getType() == Token.KEYWORD && start.getContent().equals("yield") &&
                start.next != null && (start.next.getType() == Token.VALUE || (start.next.val != null && start.next.val instanceof Generator))) {
            checkYield(start.next);
            return this;
        }
        else if (start.val == null && (start.getType() == Token.KEYWORD && start.getContent().matches("new|yield") ||
                start.getType() == Token.BRACE_OPEN)) {
            accessObjectProperties(start);
            if (yt != null) return this;
        }
        if (start.val == null) {
            while (start.getType() == Token.BRACE_OPEN && end.getType() == Token.BRACE_CLOSE) {
                start = start.next;
                end = end.prev;
            }
            if (start.getType() != Token.VALUE && start.getType() != Token.VAR_NAME) {
                System.err.println("Syntax Error");
                return this;
            }
            if (start.getType() == Token.VALUE) {
                start.val = JSValue.create(JSValue.getType(start.getContent()), start.getContent());
            } else if (start.getType() == Token.VAR_NAME) {
                start.val = Expression.getVar(start.getContent(), this);
            }
        }
        if (start.val.getType().equals("Float")) {
            double val = ((JSFloat)start.val).getValue();
            if (val == (int)val) {
                start.val = new JSInt((int)val);
            }
        }
        if (start.val.getType().matches("Array|Integer|Float|Number|Object|String|Function")) {
            accessObjectProperties(start);
        }
        while (!thr && start.next != null && start.next.getType() == Token.BRACE_OPEN) {
            functionCall(start.next);
        }
        if (!silent && parent_block.error == null) {
            System.out.println(start.val.toString());
        }
        if (ret) {
            Block b = parent_block;
            while (b != null && !b.is_func) {
                b = b.parent_block;
            }
            if (b != null) {
                b.return_value = start.val;
                if (b.is_gen) b.done = true;
            }
        } else if (thr) {
            JSError e = null;
            if (parent_block.last_error == null || parent_block.exc_var_name == null ||
                    !parent_block.exc_var_name.equals(start.getContent())) {
                e = new JSError(start.val, "Exception", getStack());
            } else {
                e = parent_block.last_error;
            }
            if (parent_block != null) parent_block.error = e;
        }
        if (del && start.getType() == Token.VAR_NAME) {
            if (start.index == null &&
                    (!parent_block.scope.containsKey(start.getContent()) || parent_block.parent_block == null)) {
                JSObject wo = parent_block.getWithObject();
                if (wo == null) {
                    ((JSObject)Expression.getVar("window", this)).removeProperty(start.getContent());
                } else {
                    wo.removeProperty(start.getContent());
                }
            } else if (start.index != null) {
                if (start.ctx.getType().matches("Object|Function")) {
                    ((JSObject)start.ctx).removeProperty((JSString)start.index);
                } else if (start.ctx.getType().equals("Array")) {
                    if (start.index.getType().equals("Integer")) {
                        ((JSArray)start.ctx).set((JSInt)start.index, Undefined.getInstance());
                    } else if (start.index.getType().equals("String")) {
                        ((JSArray)start.ctx).removeProperty((JSString)start.index);
                    }
                }
            }
        }
        return this;
    }

    public JSValue getValue() {
        return reusable ? exec.getValue() : start.val;
    }

    public Vector<String> getStack() {
        Vector<String> v = new Vector<String>();
        Block b = this instanceof Block ? (Block)this : parent_block;
        while (b != null) {
            if (b.is_func && b.parent_block != null) {
                v.add(b.func.getName());
                b = b.func.getCaller();
                continue;
            }
            b = b.parent_block;
        }
        return v;
    }

    public boolean isKeyword() {
        return (start.next == null || start.getContent().matches("if|switch|case")) && start.getType() == Token.KEYWORD;
    }

    public String getContent() {
        return start.prev != null && start.getType() != Token.KEYWORD && start.prev.getType() == Token.KEYWORD ? start.prev.getContent() : start.getContent();
    }

    private static Hashtable<String, Integer> priorities = new Hashtable<String, Integer>();

    static {
        priorities.put("++", 17);
        priorities.put("--", 17);
        priorities.put("~", 17);
        priorities.put("!", 17);
        priorities.put("typeof", 17);
        priorities.put("*", 16);
        priorities.put("/", 16);
        priorities.put("%", 16);
        priorities.put("+", 15);
        priorities.put("-", 15);
        priorities.put(">>", 14);
        priorities.put("<<", 14);
        priorities.put(">>>", 14);
        priorities.put("<", 13);
        priorities.put(">", 13);
        priorities.put(">=", 13);
        priorities.put("<=", 13);
        priorities.put("in", 13);
        priorities.put("instanceof", 13);
        priorities.put("==", 12);
        priorities.put("!=", 12);
        priorities.put("===", 12);
        priorities.put("!==", 12);
        priorities.put("&", 11);
        priorities.put("^", 10);
        priorities.put("|", 9);
        priorities.put("&&", 8);
        priorities.put("||", 7);
        priorities.put("?", 6);
        priorities.put(":", 6);
        priorities.put("+=", 5);
        priorities.put("-=", 5);
        priorities.put("*=", 5);
        priorities.put("/=", 5);
        priorities.put("%=", 5);
        priorities.put("&=", 5);
        priorities.put("^=", 5);
        priorities.put("|=", 5);
        priorities.put("=", 5);
        priorities.put(",", 4);
    }

    public static JSValue getVar(String name, Expression exp) {
        return Block.getVar(name, exp);
    }

    public static void setVar(String name, JSValue val, Expression exp, int mode) {
        Block.setVar(name, val, exp, mode);
    }

    public void setReusable(boolean val) {
        reusable = val;
    }

    public void setSilent(boolean val) {
        silent = val;
    }

    public boolean isReturn() {
        return ret;
    }

    public void incrementRefCount() {
        ref_count++;
        Block b = parent_block;
        while (b != null) {
            b.ref_count++;
            b = b.parent_block;
        }
    }

    public void decrementRefCount() {
        ref_count--;
        if (ref_count == 0 && this instanceof Block) {
            //System.err.println("Clearing scope");
            ((Block)this).scope.clear();
        }
        Block b = parent_block;
        while (b != null) {
            b.ref_count--;
            if (b.ref_count == 0 && b instanceof Block) {
                //System.err.println("Clearing scope");
                ((Block)b).scope.clear();
            }
            b = b.parent_block;
        }
    }

    @Override
    public String toString() {
        return source;
    }

    public String toString(int level) {
        String pad  = "";
        if (!getContent().equals("case") && parent_block != null && parent_block.getType() == Block.CASE) level++;
        for (int i = 0; i < level; i++) {
            pad += "  ";
        }
        return source.replaceAll("(?<=^|\\n)\\s+", pad);
    }

    private int n = 0;
    public int ref_count = 0;

    private Token start;
    private Token end;

    public boolean reusable = false;
    private Expression exec;
    public static int let = 1;
    public static int var = 2;
    private int mode = 0;
    private boolean ret;
    private boolean del;
    private boolean thr;

    public boolean is_cond;

    public Block parent_block;
    public Token yt = null;

    public String source = "";

    public boolean silent = false;
    public boolean display_timers = false;
}
