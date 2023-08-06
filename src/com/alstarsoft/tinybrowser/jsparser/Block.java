package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.htmlparser.HTMLParser;
import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Block extends Expression {

    public Block() {}

    private Block(Vector<Expression> ch) {
        for (int i = 0; i < ch.size(); i++) {
            Expression e = ch.get(i).clone();
            e.parent_block = this;
            children.add(e);
        }
    }

    public Block(Token head) {
        this(head, Block.BASIC);
    }

    public Block(Token head, int bt) {
        this(head, bt, null);
    }
    
    private Block(Token head, int bt, Block parent) {
        parent_block = parent;
        block_type = bt;
        Token start = null;
        Token end = head;
        int type;
        int level = 0;
        while (end != null) {
            type = end.getType();
            if (((type <= 6 || type >= 9 && type <= 12) && start == null) ||
                  type == 6 || type == 15 && end.getContent().matches("let|var|if|break|continue|return|throw|delete|new|yield|await")) {
                boolean needSplit = type == Token.KEYWORD && !end.getContent().matches("new|yield|await") ||
                                    type == Token.VAR_NAME && end.prev != null && (end.prev.getContent().matches("\\)|}") || end.prev.getType() == Token.VALUE);
                if (end.prev != null && end.prev.getType() != Token.EMPTY && needSplit) {
                    end.prev.next = null;
                }
                if (start == null || needSplit) {
                    if (start != null) {
                        addChildExpression(start, end);
                    }
                    start = end;
                }
            }
            if (start != null && (end.next == null || type == Token.SEMICOLON ||
                    (type != Token.BLOCK_START && type != Token.BLOCK_END && end.next.getType() == Token.KEYWORD && !end.next.getContent().matches("let|var|break|continue|return|throw|delete|function|class|new|yield|await")) ||
                    (type == Token.BLOCK_START || type == Token.BLOCK_END) && level == 0 && func_start == null)) {
                
                if (cycle_exp != null) {
                    Token ts = new Token("{");
                    Token te = new Token("}");
                    ts.prev = start.prev;
                    ts.next = start;
                    start.prev.next = ts;
                    te.prev = end;
                    te.next = end.next;
                    end.next = te;
                    end = ts;
                    start = null;
                    continue;
                }
                addChildExpression(start, end);

                if (end.next == null) return;

                if (type != Token.BLOCK_START && type != Token.BLOCK_END) end = end.next;
                while (end.next != null && end.getType() == Token.SEMICOLON) {
                    end = end.next;
                }
                start = null;
                continue;
            }
            if (type == Token.KEYWORD && end.getContent().equals("if")) {
                if (end.next != null && end.next.getType() == Token.BRACE_OPEN) {
                    Token t = end.next.next;
                    int lvl = 0;
                    while (t != null && (t.getType() != Token.BRACE_CLOSE || lvl > 0)) {
                        if (t.getType() == Token.BRACE_OPEN) lvl++;
                        if (t.getType() == Token.BRACE_CLOSE) lvl--;
                        t = t.next;
                    }
                    if (t == null || t.prev == end.next || t.next == null) {
                        System.err.println("Syntax error");
                        return;
                    }
                    Token th = t.next;
                    t.next = null;

                    Expression if_exp = new Expression(end, this);
                    children.add(if_exp);
                    
                    end = th;
                    start = null;

                    continue;
                } else {
                    System.err.println("Syntax error");
                    return;
                }
            }
            else if (type == Token.KEYWORD && end.getContent().equals("else")) {
                int n = children.size()-2;
                if (n < 0 || !children.get(n).isKeyword() || !children.get(n).getContent().equals("if")) {
                    System.err.println("Unexpected token else");
                    return;
                }
                if (end.next != null) {
                    Token t = end.next;
                    Expression else_exp = new Expression(end, this);
                    children.add(else_exp);
                    end = t;
                    continue;
                } else {
                    System.err.println("Syntax error");
                    return;
                }
            }
            else if (type == Token.KEYWORD && end.getContent().equals("switch")) {
                if (end.next != null && end.next.getType() == Token.BRACE_OPEN) {
                    Token t = end.next.next;
                    int lvl = 0;
                    while (t != null && (t.getType() != Token.BRACE_CLOSE || lvl > 0)) {
                        if (t.getType() == Token.BRACE_OPEN) lvl++;
                        if (t.getType() == Token.BRACE_CLOSE) lvl--;
                        t = t.next;
                    }
                    if (t == null || t.prev == end.next || t.next == null ||
                            t.next.getType() != Token.BLOCK_START) {
                        System.err.println("Syntax error");
                        return;
                    }

                    Token th = t.next;
                    t.next = null;

                    switch_exp = new Expression(end, this);
                    children.add(switch_exp);

                    end = th;
                    continue;
                } else {
                    System.err.println("Syntax error");
                    return;
                }
            }
            else if (type == Token.KEYWORD && end.getContent().equals("case")) {
                if (block_type != Block.CASE) {
                    System.err.println("Unexpected token case");
                    return;
                }
                Token th = end.next.next;
                if (th == null || !th.getContent().equals(":")) {
                    System.err.println("Syntax error");
                    return;
                }
                end.next.next = null;

                Expression exp = new Expression(end, this);
                children.add(exp);
                end = th.next;

                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("for")) {
                Token t = end.next;
                if (t == null || t.getType() != Token.BRACE_OPEN) {
                    System.err.println("Syntax error in for declaration");
                    return;
                }
                t = t.next;
                if (t.getType() == Token.VAR_NAME && t.next != null &&
                        t.next.getType() == Token.OP && t.next.getContent().equals("in")) {
                    Token tt = new Token("var");
                    tt.prev = t.prev;
                    t.prev.next = tt;
                    tt.next = t;
                    t.prev = tt;
                    t = tt;
                }
                if (t.next.getType() == Token.VAR_NAME && t.next.next != null &&
                        t.next.next.getType() == Token.OP && t.next.next.getContent().equals("in")) {
                    if (t.next.next.next == null || t.next.next.next.next == null ||
                            t.next.next.next.next.getType() != Token.BRACE_CLOSE) {
                        System.err.println("Syntax error in for declaration");
                        return;
                    }
                    for_in_varscope = t.getContent().equals("let") ? Expression.let : Expression.var;
                    t = t.next;
                    for_in_var = t.getContent();
                    for_in_obj = t.next.next.getContent();
                    end = t.next.next.next.next;
                    continue;
                }
                Token th = null;
                int lvl = 0;
                cycle_exp = new Expression[3];
                for (int i = 0; i < 3; i++) {
                    while (t != null && t.getType() != Token.SEMICOLON &&
                            !(t.getType() == Token.BRACE_CLOSE && lvl == 0)) {
                        if (t.getType() == Token.BRACE_OPEN) lvl++;
                        if (t.getType() == Token.BRACE_CLOSE) lvl--;
                        if (th == null) th = t;
                        t = t.next;
                    }
                    if (t == null || t.getType() == Token.BRACE_CLOSE && lvl == 0 && i < 2) {
                        System.err.println("Syntax error in for declaration");
                        return;
                    }
                    if (th != null) {
                        t.prev.next = null;
                        cycle_exp[i] = new Expression(th, this);
                        th = null;
                    }
                    t = t.next;
                }
                if (t == null) {
                    System.err.println("Syntax error: no block found after for");
                    return;
                }

                end.prev.next = t;
                t.prev = end.prev;

                end = t;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("while")) {
                Token t = end.next;
                if (t == null || t.getType() != Token.BRACE_OPEN) {
                    System.err.println("Syntax error in while declaration");
                    return;
                }
                t = t.next;
                Token th = null;
                int lvl = 0;
                cycle_exp = new Expression[3];

                while (t != null && !(t.getType() == Token.BRACE_CLOSE && lvl == 0)) {
                    if (t.getType() == Token.BRACE_OPEN) lvl++;
                    if (t.getType() == Token.BRACE_CLOSE) lvl--;
                    if (th == null) th = t;
                    t = t.next;
                }
                if (t == null) {
                    System.err.println("Syntax error in while declaration");
                    return;
                }
                if (th != null) {
                    t.prev.next = null;
                    cycle_exp[1] = new Expression(th, this);
                    th = null;
                    t = t.next;
                } else {
                    System.err.println("Syntax error in while declaration");
                    return;
                }

                cycle_exp[0] = null;
                cycle_exp[2] = null;
                
                if (t == null && !post_cycle) {
                    System.err.println("Syntax error: no block found after while");
                    return;
                }

                if (post_cycle) {
                    Expression e = children.lastElement();
                    if (e instanceof Block) {
                        ((Block)e).block_type = CYCLE;
                        ((Block)e).setReusable(true);
                        ((Block)e).setPostCheck(true);
                        ((Block)e).setCycleExp(cycle_exp);
                        cycle_exp = null;
                        post_cycle = false;
                    }
                }

                end.prev.next = t;
                if (t != null) t.prev = end.prev;

                if (end == start) start = t;

                end = t;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("do")) {
                if (end.next == null) {
                    System.err.println("Syntax error: no block found after do");
                    return;
                }
                post_cycle = true;
                end = end.next;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("with")) {
                Token t = end.next;
                if (t == null || t.getType() != Token.BRACE_OPEN) {
                    System.err.println("Syntax error in with declaration");
                    return;
                }
                t = t.next;
                Token th = null;
                int lvl = 0;

                while (t != null && !(t.getType() == Token.BRACE_CLOSE && lvl == 0)) {
                    if (t.getType() == Token.BRACE_OPEN) lvl++;
                    if (t.getType() == Token.BRACE_CLOSE) lvl--;
                    if (th == null) th = t;
                    t = t.next;
                }
                if (t == null) {
                    System.err.println("Syntax error in with declaration");
                    return;
                }
                if (th != null) {
                    t.prev.next = null;
                    with_exp = new Expression(th, this);
                    th = null;
                    t = t.next;
                } else {
                    System.err.println("Syntax error in with declaration");
                    return;
                }

                end.prev.next = t;
                if (t != null) t.prev = end.prev;

                if (end == start) start = t;

                end = t;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("try")) {
                try_flag = true;
                end.prev.next = end.next;
                end.next.prev = end.prev;
                end = end.next;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("catch")) {
                catch_flag = true;
                Token t = end.next;
                if (t == null || t.getType() != Token.BRACE_OPEN) {
                    System.err.println("Syntax error in catch declaration");
                    return;
                }
                t = t.next;
                int lvl = 0;

                while (t != null && !(t.getType() == Token.BRACE_CLOSE && lvl == 0)) {
                    if (t.getType() == Token.BRACE_OPEN) lvl++;
                    if (t.getType() == Token.BRACE_CLOSE) lvl--;
                    if (catch_exp == null) catch_exp = t.getContent();
                    t = t.next;
                }
                if (t == null || t.next == null) {
                    System.err.println("Syntax error in catch declaration");
                    return;
                }
                t = t.next;

                end.prev.next = t;
                if (t != null) t.prev = end.prev;

                if (end == start) start = t;

                end = t;
                continue;
            }
            else if (type == Token.KEYWORD && end.getContent().equals("finally")) {
                finally_flag = true;
                end.prev.next = end.next;
                end.next.prev = end.prev;
                end = end.next;
                continue;
            }
            boolean is_func_start = false;
            if (type == Token.KEYWORD && end.getContent().matches("function|class")) {
                is_func_start = true;
                func_named = false;
                func_class = end.getContent().equals("class");
                async_func = end.prev != null && end.prev.getContent().equals("async");
            }
            else if (type == Token.FIELD_NAME && (end.prev == null || end.prev.getType() == Token.EMPTY || end.prev.getContent().equals(",")) && end.next != null && end.next.getType() == Token.BRACE_OPEN ||
                     type == Token.VAR_NAME && end.next != null && end.next.getType() == Token.BRACE_OPEN && parent != null && parent.func_class) {
                int lvl = 0;
                Token t = end.next;
                while (t.next != null) {
                    if (t.getType() == Token.BRACE_OPEN) lvl++;
                    if (t.getType() == Token.BRACE_CLOSE) lvl--;
                    if (lvl == 0 && t.next.getType() == Token.BLOCK_START) {
                        is_func_start = true;
                        func_name = end.getContent();
                        func_named = true;
                        break;
                    }
                    t = t.next;
                }
            }
            if (is_func_start) {
                func_start = end;
                end = end.next;
                if (end.getType() == Token.OP && end.getContent().equals("*")) {
                    func_gen = true;
                    end.prev.next = end.next;
                    end.next.prev = end.prev;
                    end = end.next;
                }
                if (end != null && (end.getType() == Token.VAR_NAME || end.getType() == Token.FIELD_NAME)) {
                    if (end.next == null || (!func_class && end.next.getType() != Token.BRACE_OPEN || func_class && end.next.getType() != Token.BLOCK_START)) {
                        System.err.println("Syntax error in function declaration");
                        return;
                    }
                    if (end.getType() == Token.VAR_NAME) {
                        func_name = end.getContent();
                    }
                    if (!func_class) end = end.next.next;
                } else {
                    if (end == null || end.getType() != Token.BRACE_OPEN) {
                        System.err.println("Syntax error in function declaration");
                        return;
                    }
                    end = end.next;
                }
                func_args = new Vector<String>();
                while (end != null && (!func_class && end.getType() != Token.BRACE_CLOSE || func_class && end.next.getType() != Token.BLOCK_START)) {
                    if (end.getType() == Token.VAR_NAME) {
                        if (!(end.prev.getType() == Token.OP && end.prev.getContent().equals(",") || end.prev.getType() == Token.BRACE_OPEN)
                                || !(end.next.getType() == Token.OP && end.next.getContent().equals(",") || end.next.getType() == Token.BRACE_CLOSE)) {
                            System.err.println("Syntax error in function declaration");
                            return;
                        }
                        func_args.add(end.getContent());
                    }
                    end = end.next;
                }
                if (end == null || end.prev.getType() == Token.OP && end.prev.getContent().equals(",") ||
                        end.next == null || end.next.getType() != Token.BLOCK_START) {
                    System.err.println("Syntax error in function declaration");
                    return;
                }
                end = end.next;
                continue;
            }
            if (type == Token.OP && end.getContent().equals("=>")) {
                Token t = end;
                Token t2 = t;
                if (end.prev == null || end.next == null) {
                    System.err.println("Syntax error in function declaration");
                    return;
                }
                if (end.prev.getType() == Token.BRACE_CLOSE) {
                    t = t.prev;
                    int lvl = 0;
                    while (t != null && (t.getType() != Token.BRACE_OPEN || lvl < 0)) {
                        if (t.prev.getType() == Token.BRACE_OPEN) lvl++;
                        if (t.prev.getType() == Token.BRACE_CLOSE) lvl--;
                        t = t.prev;
                    }
                    t2 = t;
                    if (t == null) {
                        System.err.println("Syntax error in function declaration");
                        return;
                    }
                    func_args = new Vector<String>();
                    lvl = 0;
                    while (t2 != null && (t2.getType() != Token.BRACE_CLOSE || lvl > 0)) {
                        if (t2.getType() == Token.VAR_NAME) {
                            if (!(t2.prev.getType() == Token.OP && t2.prev.getContent().equals(",") || t2.prev.getType() == Token.BRACE_OPEN)
                                    || !(t2.next.getType() == Token.OP && t2.next.getContent().equals(",") || t2.next.getType() == Token.BRACE_CLOSE)) {
                                System.err.println("Syntax error in function declaration");
                                return;
                            }
                            func_args.add(t2.getContent());
                        }
                        if (t2.next.getType() == Token.BRACE_OPEN) lvl++;
                        if (t2.next.getType() == Token.BRACE_CLOSE) lvl--;
                        t2 = t2.next;
                    }
                    Token ts = new Token("function");
                    ts.prev = t.prev;
                    t.prev.next = ts;
                    ts.next = t;
                    t.prev = ts;
                    func_start = ts;
                } else if (end.prev.getType() == Token.VAR_NAME) {
                    func_args = new Vector<String>();
                    func_args.add(end.prev.getContent());
                    t = new Token("(");
                    t2 = new Token(")");
                    end.prev.prev.next = t;
                    t.prev = end.prev.prev;
                    end.prev.prev = t;
                    t.next = end.prev;
                    end.prev.next = t2;
                    t2.prev = end.prev;
                    t2.next = end;
                    end.prev = t2;
                    Token ts = new Token("function");
                    ts.prev = t.prev;
                    t.prev.next = ts;
                    ts.next = t;
                    t.prev = ts;
                    func_start = ts;
                } else {
                    System.err.println("Syntax error in function declaration");
                    return;
                }
                end.next.prev = end.prev;
                end.prev.next = end.next;
                if (end.next.getType() != Token.BLOCK_START) {
                    Token tt = end;
                    t = new Token("{");
                    t2 = new Token("}");
                    Token t3 = new Token("return");
                    t3.prev = t;
                    t.next = t3;
                    int lvl = 0;
                    while (tt.next != null && tt.next.getType() != Token.SEMICOLON &&
                            !(tt.next.getType() != Token.OP && tt.getContent().equals(",")) &&
                            (tt.next.getType() != Token.BRACE_CLOSE || lvl > 0) &&
                            tt.next.getType() != Token.ARRAY_END &&
                            tt.next.getType() != Token.OBJECT_END) {
                        if (tt.next.getType() == Token.BRACE_OPEN) lvl++;
                        if (tt.next.getType() == Token.BRACE_CLOSE) lvl--;
                        tt = tt.next;
                    }
                    if (tt == end) {
                        System.err.println("Syntax error in function declaration");
                        return;
                    }
                    end.prev.next = t;
                    t.prev = end.prev;
                    t3.next = end.next;
                    end.next.prev = t3;
                    t2.prev = tt;
                    t2.next = tt.next;
                    if (tt.next != null) {
                        tt.next.prev = t2;
                    }
                    tt.next = t2;
                    end = t.prev;
                }
                func_lmb = true;
            }
            if (type == Token.BLOCK_END && level == 0) {
                updateSource();
                return;
            }
            else if (type == Token.BLOCK_START && level == 0 && end.next != null) {
                Token h = end;
                level++;
                while (end.next != null && level > 0) {
                    if (end.next.getType() == Token.BLOCK_START) level++;
                    if (end.next.getType() == Token.BLOCK_END) level--;
                    end = end.next;
                }
                if (post_cycle && !(end.next.getType() == Token.KEYWORD && end.next.getContent().equals("while"))) {
                    System.err.println("Syntax error: while expected");
                    post_cycle = false;
                    return;
                }
                h.prev = null;
                int t = Block.BASIC;
                if (switch_exp != null) t = Block.CASE;
                else if (cycle_exp != null || for_in_obj != null) {
                    t = Block.CYCLE;
                }
                Block b = new Block(h.next, t, this);
                if (switch_exp != null) {
                    b.setSwitchExp(switch_exp);
                    switch_exp = null;
                }
                if (cycle_exp != null) {
                    b.setCycleExp(cycle_exp);
                    b.setReusable(true);
                    cycle_exp = null;
                    post_cycle = false;
                }
                if (for_in_obj != null) {
                    b.setReusable(true);
                    b.f_in_var = for_in_var;
                    b.f_in_obj = for_in_obj;
                    b.f_in_vsc = for_in_varscope;
                    for_in_var = null;
                    for_in_obj = null;
                    for_in_varscope = 2;
                }
                if (with_exp != null) {
                    b.w_exp = with_exp;
                    with_exp = null;
                }
                if (try_flag) {
                    b.is_try = true;
                    try_flag = false;
                }
                if (catch_flag) {
                    Expression e = !children.isEmpty() ? children.lastElement() : null;
                    if (e == null || !(e instanceof Block) || !((Block)e).is_try && !((Block)e).is_catch) {
                        System.err.println("Unexpected catch block");
                        return;
                    }
                    b.is_catch = true;
                    catch_flag = false;
                    if (catch_exp != null) {
                        b.exc_var_name = catch_exp;
                        catch_exp = null;
                    }
                }
                if (finally_flag) {
                    Expression e = !children.isEmpty() ? children.lastElement() : null;
                    if (e == null || !(e instanceof Block) || !((Block)e).is_catch) {
                        System.err.println("Unexpected finally block");
                        return;
                    }
                    b.is_finally = true;
                    finally_flag = false;
                }
                if (func_start != null) {
                    if (func_name != null && !func_named || parent != null && parent.func_class) {
                        Function f = new Function(func_args, b, func_name);
                        scope.put(func_name, f);
                        if (func_gen) {
                            f.setAsGenerator();
                        }
                        f.setAsync(async_func);
                        ((Function)scope.get(func_name)).setIsClass(func_class);
                    } else {
                        Token tc = func_start;
                        if (tc != null) {
                            Token tf = new Token("{}");
                            tf.val = new Function(func_args, b, "");
                            if (func_named) ((Function)tf.val).setName(func_name);
                            ((Function)tf.val).setIsClass(func_class);
                            if (func_lmb) {
                                ((Function)tf.val).setAsLambda();
                            }
                            if (func_gen) {
                                ((Function)tf.val).setAsGenerator();
                            }
                            tc.prev.next = tf;
                            tf.prev = tc.prev;
                            if (end.next != null) {
                                end.next.prev = tf;
                            }
                            if (tc.prev.getType() == Token.EMPTY || tc.prev.getType() == Token.SEMICOLON) {
                                start = tf;
                            }
                            tf.next = end.next;
                            if (end.next != null && (end.next.getType() == Token.KEYWORD ||
                                end.next.getType() == Token.VAR_NAME ||
                                end.next.getType() == Token.VALUE)) {
                                Token tt = new Token(";");
                                tt.prev = tf;
                                tf.next = tt;
                                tt.next = end.next;
                                end.next.prev = tt;
                                end = tf;
                            }
                            if (tf.prev.getType() == Token.BRACE_OPEN && tf.next.getType() == Token.BRACE_CLOSE &&
                                    tf.prev.prev.getType() == Token.OP) {
                                tf.prev.prev.next = tf;
                                tf.prev = tf.prev.prev;
                                if (tf.next.next != null) {
                                    tf.next.next.prev = tf;
                                }
                                tf.next = tf.next.next;
                            }
                        }
                    }
                }
                if (func_start == null) {
                    children.add(b);
                } else {
                    func_start = null;
                    func_name = null;
                    func_args = null;
                    func_lmb = false;
                    func_gen = false;
                    func_named = false;
                    func_class = false;
                }
                end = end.next;
                continue;
            }
            else {
                end = end.next;
            }
        }
    }

    public void addChildExpression(Token head, Token last) {
        Token t = new Token("");
        head.prev = t;
        t.next = head;

        if (last.getType() == Token.SEMICOLON || last.getType() == Token.BLOCK_START || last.getType() == Token.BLOCK_END) {
            last.prev.next = null;
        }

        Expression e = new Expression(head, this);
        e.silent = silent;
        children.add(e);
        if (parent_block != null && parent_block.func_class && e.start.next != null && e.start.next.getContent().equals("=")) {
            String name = e.start.getContent();
            e.start = e.start.next.next;
            e.silent = true;
            e.eval();
            Expression.setVar(name, e.getValue(), e, Block.let);
        }
    }
    
    @Override
    public Expression clone() {
        Block b = new Block(children);
        b.setReusable(reusable);
        b.silent = silent;
        b.parent_block = parent_block;
        return b;
    }

    @Override
    public Expression eval() {
        last_error = error;
        if (is_gen && done) {
            return this;
        }
        if (is_catch && error == null) {
            return this;
        }
        if (is_catch) {
            error = null;
        }
        if (w_exp != null) {
            String res_type = w_exp.eval().getValue().getType();
            if (res_type.matches("Object|Function")) {
                with_obj = ((JSObject)w_exp.getValue());
            }
            w_exp = null;
        }
        if (block_type == Block.CASE && sw_exp != null) sw_exp.eval();
        if (block_type == Block.CYCLE && c_exp != null) {
            if (c_exp[0] != null) c_exp[0].eval();
            if (!post_check && c_exp[1] != null && !c_exp[1].eval().getValue().asBool().getValue()) {
                return this;
            }
        }
        Block b = this;
        while (b != null && !b.is_gen && b.yt_value == null && b.at_value == null) {
            b = b.parent_block;
        }

        int from = 0;
        if (last != -1) {
            from = last;
        }
        if (yt_value == null && last != -1 && is_gen && !(lastExpr.yt != null && lastExpr.yt.val instanceof Generator && !((Generator)lastExpr.yt.val).isDone())) {
            lastExpr = null;
            from++;
        }

        Vector<JSValue> keys = new Vector<JSValue>();
        JSValue obj = null;
        if (f_in_obj != null) {
            obj = Block.getVar(f_in_obj, this);
        }
        int cur_index = 0;
        if (f_in_obj != null) {
            if (obj.getType().equals("Array")) {
                JSArray a = (JSArray)obj;
                for (int i = 0; i < (int)a.length().getValue(); i++) {
                    if (a.get(i) != Undefined.getInstance()) {
                        keys.add(new JSInt(i));
                    }
                }
            }
            else if (obj.getType().equals("Object")) {
                JSObject o = (JSObject)obj;
                Set<String> props = o.getProperties().keySet();
                Iterator it = props.iterator();
                while (it.hasNext()) {
                    String str = (String)it.next();
                    if (!str.matches("__proto__|constructor")) {
                        keys.add(new JSString(str));
                    }
                }
            }
        }
        for (int i = from; i < children.size(); i++) {
            if (block_type == Block.CYCLE && i == 0 && f_in_obj != null) {
                if (f_in_vsc == Expression.let || parent_block == null) {
                    scope.put(f_in_var, keys.get(cur_index));
                } else {
                    parent_block.scope.put(f_in_var, keys.get(cur_index));
                }
                cur_index++;
            }
            Expression e = children.get(i);
            if (last != -1 && i == from && lastExpr != null && (yt_value != null || at_value != null)) {
                e = lastExpr;
            }
            if (!(e instanceof Block) && e.isKeyword()) {
                if (e.getContent().equals("if")) {
                    if (i == children.size()-1) return this;
                    JSValue v = e.eval().getValue().asBool();
                    if (((JSBool)v).getValue() && i + 1 < children.size()) {
                        last = i+1;
                        children.get(i+1).eval();
                        i += 1;
                        if (i+1 < children.size()) {
                            e = children.get(i+1);
                            if (!(e instanceof Block)
                                && e.isKeyword() && e.getContent().equals("else")) {
                                i += 2;
                            }
                        }
                        continue;
                    }
                    i += 1;
                    if (i+1 < children.size()) {
                        e = children.get(i+1);
                        if (!(e instanceof Block)
                            && e.isKeyword() && e.getContent().equals("else")) {
                            i++;
                        }
                    }
                    continue;
                }
                else if (block_type == Block.CASE && e.getContent().equals("case")) {
                    if (sw_exp == null) {
                        continue;
                    }
                    JSValue val = e.eval().getValue();
                    if (Expression.equals(sw_exp.getValue(), val)) sw_flag = true;
                    continue;
                }
            }
            if (!(e instanceof Block) && e.isKeyword() && e.getContent().equals("break") &&
                    (block_type != Block.CASE || sw_flag) || state == Block.BREAK) {
                if (block_type == Block.BASIC) {
                    parent_block.state = Block.BREAK;
                }
                return this;
            }
            if (!(e instanceof Block) && e.isKeyword() && e.getContent().equals("continue") &&
                    (block_type != Block.CASE || sw_flag) || state == Block.CONTINUE) {
                if (block_type != Block.CYCLE) {
                    parent_block.state = Block.CONTINUE;
                } else {
                    state = Block.NORMAL;
                    if (c_exp[2] != null) {
                        c_exp[2].eval();
                    }
                    if (c_exp[1] != null && c_exp[1].eval().getValue().asBool().getValue()) {
                        i = -1;
                        continue;
                    } else {
                        return this;
                    }
                }
            }
            if (block_type != Block.CASE || sw_flag) {
                if (is_gen && error != null) {
                    error = new JSError(error.getValue(), error.getText(), e.getStack());
                }
                if (!(e instanceof Block) && e.yt != null && yt_value != null) {
                    if (e.yt.prev == e.start && (!(e.yt.val instanceof Generator) || ((Generator)e.yt.val).isDone())) {
                        from++;
                        continue;
                    }
                    e.setYieldValue(yt_value);
                    e.updateSource();
                    yt_value = null;
                }
                if (!(e instanceof Block) && e.at != null && at_value != null) {
                    if (e.at.prev == e.start) {
                        from++;
                        continue;
                    }
                    e.setAwaitValue(at_value);
                    e.updateSource();
                    at_value = null;
                }
                e.yt = null;
                e.at = null;
                e.eval();
                if (is_gen) {
                    state = e.yt != null ? RETURN : NORMAL;
                }
                if (error == null) last = i;
                if (i == children.size()-1 && is_gen) done = true;
                if (state == RETURN) {
                    if (parent_block == null && func == null) {
                        JSValue w = Block.getVar("window", this);
                        if (w != Undefined.getInstance()) {
                            ((Window)w).runPromises();
                        }
                    }
                    resumeParent();
                    return this;
                }
            }
            if (!(e instanceof Block) && e.isReturn() &&
                    (block_type != Block.CASE || sw_flag) || state == Block.RETURN) {
                if (!is_func && parent_block != null) {
                    parent_block.state = Block.RETURN;
                }
                if (state == Block.RETURN && is_gen) {
                    state = Block.NORMAL;
                    if (!silent) {
                        System.out.println(return_value);
                    }
                }
                onFinished();
                return this;
            }
            if (error != null) {
                if (children.get(i) instanceof Block &&
                     (((Block)children.get(i)).is_try || ((Block)children.get(i)).is_catch) &&
                     i < children.size()-1 && children.get(i+1) instanceof Block &&
                        ((Block)children.get(i+1)).is_catch) {
                    Block next = (Block)children.get(i+1);
                    next.error = error;
                    next.scope.put(next.exc_var_name, error.getValue());
                    error = null;
                } else if (children.get(i) instanceof Block && ((Block)children.get(i)).is_catch &&
                     i < children.size()-1 && children.get(i+1) instanceof Block &&
                        ((Block)children.get(i+1)).is_finally) {
                    Block next = (Block)children.get(i+1);
                    next.error = error;
                } else if (parent_block != null) {
                    parent_block.error = error;
                    onFinished();
                    return this;
                } else {
                    System.err.println(error.getText());
                    String stack = error.printStack();
                    if (!stack.isEmpty()) {
                        System.err.println(error.printStack());
                    }
                    onFinished();
                    return this;
                }
            }
            if (i == children.size()-1) {
                if (block_type == Block.CYCLE) {
                    if (f_in_obj != null) {
                        if (cur_index < keys.size()) {
                            i = -1;
                        }
                    } else {
                        if (c_exp[2] != null) {
                            c_exp[2].eval();
                        }
                        if (c_exp[1] != null && c_exp[1].eval().getValue().asBool().getValue()) {
                            i = -1;
                        }
                    }
                }
            }
        }
        onFinished();
        resumeParent();
        return this;
    }

    private void onFinished() {
        if (func != null) {
            JSValue document = Block.getVar("document", this);
            if (document != null && document instanceof HTMLDocument) {
                JSValue body = ((HTMLDocument)document).get("body");
                if (body instanceof HTMLElement) {
                    Node node = ((HTMLElement)body).node;
                    node.fireEvent("JSFuncFinished", "jsparser");
                }
            }
        }
        if (parent_block == null) {
            JSValue w = Block.getVar("window", this);
            if (w != Undefined.getInstance()) {
                ((Window)w).runPromises();
            }
        }
    }

    private void resumeParent() {
        Block b = func == null ? parent_block : func.getCaller();
        if (b != null && b.lastExpr != null && !(b.lastExpr instanceof Block) && b.lastExpr.at != null) {
            if (!(return_value instanceof Promise)) {
                b.lastExpr.setAwaitValue(return_value);
                b.state = Block.NORMAL;
                b.eval();
            } else {
                Token t = b.lastExpr.at;
                while (t != null && t.getType() != Token.OP && t.getType() != Token.BLOCK_END) {
                    t = t.next;
                }
                if (t != null) {
                    t.prev = b.lastExpr.at;
                }
                b.lastExpr.at.next = t;
            }
        }
    }

    public static void resumeAsyncFunction(Promise promise) {
        if (promise.state > 0 && promise.next.isEmpty()) {
            promise.asyncCallerFuncBlock.at_value = promise.result;
            promise.asyncCallerFuncBlock.eval();
        }
    }

    public void setType(int type) {
        block_type = type;
    }

    public void setSwitchExp(Expression exp) {
        sw_exp = exp;
    }

    public void setCycleExp(Expression[] exp) {
        if (exp.length < 3) return;
        c_exp = new Expression[3];
        for (int i = 0; i < 3; i++) {
            c_exp[i] = exp[i];
            if (c_exp[i] != null) {
                c_exp[i].silent = true;
                c_exp[i].setReusable(true);
            }
        }
    }

    public void setPostCheck(boolean value) {
        post_check = value;
    }

    @Override
    public JSValue getValue() {
        return last >= 0 ? (is_gen ? return_value : children.get(last).getValue()) : Null.getInstance();
    }

    public JSObject getWithObject() {
        JSObject obj = this.with_obj;
        Block b = this;
        while (obj == null && b.parent_block != null) {
            b = b.parent_block;
            obj = b.with_obj;
        }
        return obj;
    }

    public static JSValue getVar(String name, Expression exp) {
        int sign = 1;
        if (name.charAt(0) == '-') {
            sign = -1;
        }
        Block b = exp instanceof Block ? (Block)exp : exp.parent_block;
        JSValue result = null;
        JSObject wo = b.getWithObject();
        if (wo != null && wo.hasOwnProperty(name)) {
            result = wo.get(name);
        }
        while (b != null && (result == null || result == Undefined.getInstance())) {
            result = b.scope.get(name);
            b = b.parent_block;
        }
        if (result != null && result.getType().equals("Integer") && sign < 0) {
            result = new JSInt(((JSInt)result).getValue() * sign);
        }
        if (result != null && result.getType().equals("Float") && sign < 0) {
            result = new JSFloat(((JSFloat)result).getValue() * sign);
        }
        if (result != null && name.equals("eval") && b == null) {
            ((DynamicContext)result).setContext(exp.parent_block);
        }
        if (result == null) {
            Block block = exp instanceof Block ? (Block)exp : exp.parent_block;
            if (block.strict_mode) {
                block.error = new JSError(null, "Reference error: variable " + name + " not found", exp.getStack());
            }
        }
        return result != null ? result : Undefined.getInstance();
    }

    public static void setVar(String name, JSValue val, Expression exp, int mode) {
        setVar(name, val, exp, mode, false);
    }

    public static void setVar(String name, JSValue val, Expression exp, int mode, boolean force) {
        if (name.charAt(0) == '-') {
            Block block = exp instanceof Block ? (Block)exp : exp.parent_block;
            block.error = new JSError(null, "Syntax error in assignment", exp.getStack());
            name = name.substring(1);
        }
        Block b = exp instanceof Block ? (Block)exp : exp.parent_block;
        JSObject wo = b.getWithObject();
        if (wo != null && wo.hasOwnProperty(name)) {
            wo.set(name, val);
        }
        if (mode != Expression.let) {
            boolean found = false;
            while (b.parent_block != null) {
                found = b.is_func && mode == Expression.var || b.scope.containsKey(name);
                if (found) break;
                b = b.parent_block;
            }
        }
        if (b.parent_block == null && name.equals("window") && !force) return;
        b.scope.put(name, val);
    }

    @Override
    public void setReusable(boolean val) {
        reusable = val;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setReusable(val);
        }
    }

    @Override
    public void setSilent(boolean val) {
        silent = val;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setSilent(val);
        }
        Set keys = scope.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            JSValue value = scope.get((String)it.next());
            if (value.getType().equals("Function")) {
                ((Function)value).setSilent(val);
            }
        }
    }

    public void setStrictMode(boolean val) {
        strict_mode = val;
        for (int i = 0; i < children.size(); i++) {
            Expression exp = children.get(i);
            if (exp instanceof Block) {
                ((Block)exp).setStrictMode(val);
            }
        }
    }
    
    public void setDocument(HTMLParser document) {
        JSValue w = scope.get("window");
        if (w != null) {
            ((Window) w).setDocument(document);
        }
    }

    public void setWindowFrame(java.awt.Frame window) {
        JSValue w = scope.get("window");
        if (w != null) {
            ((Window) w).setWindowFrame(window);
        }
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console c) {
        console = c;
    }

    public void replaceDocumentObject(HTMLDocument doc) {
        scope.put("document", doc);
    }

    public int getType() {
        return block_type;
    }

    @Override
    public void updateSource() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).updateSource();
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }
    
    @Override
    public String toString(int level) {
        String result = "";
        boolean open_else = false;
        for (int i = 0; i < children.size(); i++) {
            String str = children.get(i).toString(level+1);
            if (i > 0 && children.get(i-1) instanceof Block && children.get(i).getContent().equals("else")) {
                result = result.replaceAll("\\s+$", "");
                str = str.replaceAll("^\\s+", " ");
            }
            result += str;
            if (str.matches("\\s*\\}?\\s*else\\s*\\{?")) {
                open_else = true;
            }
            if (i < children.size()-1 && children.get(i+1) instanceof Block && (open_else || children.get(i).start.getContent().matches("(for|while|switch)"))) {
                result += " ";
            }
            else if (i < children.size()-1 && (children.get(i) instanceof Block || !children.get(i).getContent().equals("switch"))) result += "\n";
            if (i < children.size()-1 && children.get(i) instanceof Block && children.get(i+1).getContent().equals("else") || open_else) {
                result = result.replaceAll("\\s+$", " ");
            }
        }

        result += "\n";

        String pad = "";
        for (int k = 0; k < level; k++) {
            pad += "  ";
        }
        result += pad;

        result = (block_type != Block.CASE ? "{\n" : "\n") + result + "}";
        if (c_exp != null || f_in_obj != null) {
            result = pad + (!post_check ? getLoopHeader() + " " + result : "do " + result + " " + getLoopHeader());
        }
        
        return result;
    }

    private String getLoopHeader() {
        String result = "";
        if (c_exp != null || f_in_obj != null) {

            if (c_exp != null) {
                for (int i = 0; i < c_exp.length; i++) {
                    if (c_exp[i] != null) {
                        result += c_exp[i].toString().trim() + " ";
                    }
                }
                result = result.replaceAll(";\\s*$", "").trim();
                if (c_exp[0] != null) {
                    result = "for (" + result + ")";
                } else {
                    result = "while (" + result + ")";
                }
            } else {
                result = "for (" + (f_in_vsc == Block.let ? "let" : "var") + " " + f_in_var + " in " + f_in_obj + ")";
            }
        }
        return result;
    }


    public int state;

    public boolean is_func = false;
    public boolean is_gen = false;
    public boolean done = false;
    public JSValue yt_value;
    public JSValue at_value;
    public Function func = null;
    public JSValue return_value = Undefined.getInstance();

    public HashMap<String, JSValue> scope = new HashMap<String, JSValue>();

    protected Vector<Expression> children = new Vector<Expression>();

    protected int last = -1;
    protected Expression lastExpr;

    protected Console console = null;

    private Expression switch_exp = null;
    private Expression[] cycle_exp = null;
    private Expression with_exp = null;
    private boolean post_cycle = false;
    private String for_in_var = null;
    private String for_in_obj = null;
    private int for_in_varscope = 2;
    private boolean try_flag = false;
    private boolean catch_flag = false;
    private String catch_exp = null;
    private boolean finally_flag = false;

    public boolean is_try = false;
    public boolean is_catch = false;
    public boolean is_finally = false;
    public String exc_var_name = null;

    private int block_type = BASIC;
    private boolean sw_flag = false;
    private Expression sw_exp = null;
    private Expression[] c_exp = null;
    private Expression w_exp = null;
    private String f_in_var = null;
    private String f_in_obj = null;
    private int f_in_vsc = 2;
    private boolean post_check = false;
    private boolean async_func = false;
    private String func_name = null;
    private Vector<String> func_args = null;
    private Token func_start = null;
    private boolean func_lmb = false;
    private boolean func_gen = false;
    private boolean func_named = false;
    private boolean func_class = false;
    private JSObject with_obj = null;
    public JSError error = null;
    public JSError last_error = null;
    public boolean strict_mode = false;

    public static int BASIC = 4;
    public static int CASE = 5;
    public static int CYCLE = 6;

    protected static int NORMAL = 0;
    protected static int BREAK = 1;
    protected static int CONTINUE = 2;
    protected static int RETURN = 3;
}
