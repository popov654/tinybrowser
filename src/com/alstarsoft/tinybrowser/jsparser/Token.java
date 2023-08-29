package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Token {

    public Token(String str) {
        this.str = str;
        if (str.length() == 1 && (str.charAt(0) == '(' || str.charAt(0) == ')')) {
            type = str.charAt(0) == '(' ? BRACE_OPEN : BRACE_CLOSE;
        } else if (str.length() == 1 && (str.charAt(0) == '{' || str.charAt(0) == '}')) {
            type = str.charAt(0) == '{' ? BLOCK_START : BLOCK_END;
        } else if (str.matches("[,<>~^|&*/%!?:+=-]{1,3}|typeof|in|instanceof")) {
            type = OP;
            p = Expression.priorities.get(str);
        } else if (str.matches("^\\(.*\\)$")) {
            type = FUNC_ARGS;
        } else if (str.matches("^[a-zA-Z$][a-zA-Z0-9$_-]*$")) {
            type = str.matches("true|false|NaN|undefined|null") ? VALUE : (keywords.contains(str) ? KEYWORD : VAR_NAME);
            if (str.equals("throw$")) this.str = "throw";
        } else if (str.matches("^<[\\w0-9 _-]+>$")) {
            type = FIELD_NAME;
            this.str = str.substring(1, str.length()-1);
        } else if (str.length() == 1 && (str.charAt(0) == '[' || str.charAt(0) == ']') || str.equals("?.[")) {
            type = str.charAt(0) == '[' || str.equals("?.[") ? ARRAY_START : ARRAY_END;
        } else if (str.length() == 2 && (str.equals("{{") || str.equals("}}"))) {
            type = str.equals("{{") ? OBJECT_START : OBJECT_END;
        } else if (str.equals("[]")) {
            type = ARRAY_ENTITY;
        } else if (str.matches("\\{(Function)?\\}")) {
            type = OBJECT_ENTITY;
        } else if (str.matches("/.*/[a-z]*")) {
            type = REGEXP;
        } else if (str.length() == 1 && str.equals(",")) {
            type = COMMA;
        } else if (str.matches("\\??\\.")) {
            type = DOT;
        } else if (str.length() == 1 && str.equals(";")) {
            type = SEMICOLON;
        } else if (str.length() > 0) {
            type = VALUE;
        } else {
            type = EMPTY;
        }
    }

    public String getContent() {
        return str;
    }

    public void setContent(String newstr) {
        str = newstr;
    }

    public int getType() {
        return type;
    }

    public void setType(int value) {
        type = value;
    }

    @Override
    public String toString() {
        return "SyntaxToken['" + str + "']";
    }

    private String str;
    private int type;

    public Token prev;
    public Token next;

    public JSValue val;
    public JSValue ctx;
    public JSValue index;
    public int p;
    public Expression exp;

    public static Vector<String> keywords = new Vector<String>();

    static {
        keywords.add("let");
        keywords.add("var");
        keywords.add("const");
        keywords.add("new");
        keywords.add("yield");
        keywords.add("function");
        keywords.add("class");
        keywords.add("return");
        keywords.add("async");
        keywords.add("await");
        keywords.add("if");
        keywords.add("else");
        keywords.add("for");
        keywords.add("do");
        keywords.add("while");
        keywords.add("switch");
        keywords.add("case");
        keywords.add("break");
        keywords.add("continue");
        keywords.add("with");
        keywords.add("delete");
        keywords.add("try");
        keywords.add("catch");
        keywords.add("throw");
        keywords.add("finally");
    }

    public static int VALUE = 1;
    public static int OP = 2;
    public static int BRACE_OPEN = 3;
    public static int BRACE_CLOSE = 4;
    public static int FIELD_NAME = 5;
    public static int VAR_NAME = 6;
    public static int FUNC_ARGS = 7;
    public static int STRING = 8;
    public static int ARRAY_START = 9;
    public static int ARRAY_END = 10;
    public static int OBJECT_START = 11;
    public static int OBJECT_END = 12;
    public static int BLOCK_START = 13;
    public static int BLOCK_END = 14;
    public static int KEYWORD = 15;
    public static int COMMA = 16;
    public static int DOT = 17;
    public static int SEMICOLON = 18;
    public static int EMPTY = 19;
    public static int ARRAY_ENTITY = 20;
    public static int OBJECT_ENTITY = 21;
    public static int REGEXP = 22;
}
