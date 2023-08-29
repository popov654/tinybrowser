package com.alstarsoft.tinybrowser.jsparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.alstarsoft.tinybrowser.CharsetDetector;

/**
 *
 * @author Alex
 */
public class JSParser {

    public JSParser() {}

    public JSParser(String data) {
        this.data = data;
        scan();
    }

    public JSParser(String filename, boolean fromFile) {
        loadFile(filename);
        scan();
    }

    public void printTokenChain() {
        Token t = head.next;
        while (t != null) {
            System.out.println("Type: " + t.getType() + ", Content: " + t.getContent());
            t = t.next;
        }
    }

    public Token getHead() {
        return head.next;
    }

    public void loadFile(String filename) {
        ObjectInputStream is = null;
        Charset charset = CharsetDetector.detectCharset(new java.io.File(filename));
        try {
            FileInputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            char[] buf = new char[4096];
            int result = 0;
            while (result != -1) {
                result = br.read(buf);
                if (result > 0) {
                    data += (new StringBuilder()).append(Arrays.copyOf(buf, result)).toString();
                }
            }
            br.close();
            in.close();
            if (charset.displayName().startsWith("UTF")) {
                data = new String(data.getBytes(), charset);
            }
        } catch (IOException ex) {
            Logger.getLogger(JSParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {
                Logger.getLogger(JSParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(data);
    }

    private boolean checkLevel(char ch) {
        lvl += (ch == '{') ? 1 : -1;
        if (lvl < 0) {
            errorDescription = getErrorMessage("unexpected token");
            System.err.println(errorDescription);
            correct = false;
            return false;
        }
        return true;
    }

    private String getErrorMessage(String msg) {
        return "Syntax error: " + msg + " (" + line + ":" + (pos - lastLineStartPos + 1) + ")";
    }

    private void scan() {
        data.replace("\r\n", "\n");
        while (pos < data.length()) {
            if (pos == 0) {
                head = new Token("");
                cur = head;
                state = READY;
            }
            
            char ch = data.charAt(pos);

            sign = false;

            int substate = stack.size() > 0 ? stack.lastElement() : 0;

            if (state != READ_COMMENT && pos < data.length()-1 && data.substring(pos, pos+2).equals("/*")) {
                state = READ_COMMENT;
                comment_multiline = true;
                pos += 2;
                continue;
            }

            if (state != READ_STRING && state != READ_COMMENT && pos < data.length()-1 && data.substring(pos, pos+2).equals("//")) {
                state = READ_COMMENT;
                comment_multiline = false;
                pos += 2;
                continue;
            }

            else if (state == READ_COMMENT && comment_multiline && pos < data.length()-1 && data.substring(pos, pos+2).equals("*/")) {
                state = READY;
                pos += 2;
                continue;
            }

            else if (state == READ_COMMENT && !comment_multiline && ch == '\n') {
                state = READY;
                pos++;
                line++;
                lastLineStartPos = pos;
                continue;
            }

            else if (state == READ_COMMENT) {
                pos++;
                if (ch == '\n') {
                    line++;
                    lastLineStartPos = pos;
                }
                continue;
            }

            if (ch == '\\') {
                if (!esc && state == READ_STRING) {
                    esc = true;
                    pos++;
                    continue;
                }
                if (esc && state == READ_STRING) {
                    esc = false;
                }
            }

            if (ch == '\n') {
                line++;
                lastLineStartPos = pos+1;
                if (state == READ_STRING && !esc) {
                    errorDescription = getErrorMessage("unexpected new line character");
                    System.err.println(errorDescription);
                    correct = false;
                    return;
                }
            }

            if (state == READY) {
                boolean num = false;
                //Found a number literal
                if ((ch == '+' || ch == '-') && pos < data.length()-1 && data.substring(pos).replaceAll("\r?\n", "").matches("[+-](0?[1-9]+|0x[0-9a-fA-F]+).*") || ch >= '0' && ch <= '9') {
                    boolean flag = false;
                    if (ch >= '0' && ch <= '9') flag = true;
                    else {
                        char c = data.charAt(pos+1);
                        if (c >= '0' && c <= '9') flag = true;
                    }
                    if ((ch == '+' || ch == '-') && cur != null && cur.getType() == Token.VALUE || cur.getType() == Token.VAR_NAME) {
                        flag = false;
                    }
                    if (flag) {
                        last_token = "";
                        state = READ_NUMBER;
                        if (cur.getType() == Token.VALUE && substate == 0) {
                            errorDescription = getErrorMessage("unexpected number value");
                            System.err.println(errorDescription);
                            correct = false;
                            return;
                        }
                        num = true;
                    }
                    if (ch == '+' || ch == '-') {
                        sign = true;
                    }
                }
                //Found an operator
                boolean maybeRegexp = ch == '/' && !esc && cur.getType() != Token.VALUE && cur.getType() != Token.VAR_NAME &&
                    cur.getType() != Token.BRACE_CLOSE && cur.getType() != Token.ARRAY_END && cur.getType() != Token.OBJECT_END;
                if (!num && String.valueOf(ch).matches("[,<>~^|&/*%!?:+=-]") && !maybeRegexp) {
                    last_token = "";
                    state = READ_OPERATOR;
                    String s = pos + 2 < data.length() ? data.substring(pos, pos+2) : data.substring(pos);
                    if (cur.getType() == Token.OP && ch != '~' && ch != '!' && (!s.equals("++") && !s.equals("--") &&
                            !cur.getContent().equals("++") && !cur.getContent().equals("--") ||
                            cur.getContent().equals("++") && (s.equals("++") || s.equals("--")) ||
                            cur.getContent().equals("--") && (s.equals("++") || s.equals("--")))) {
                        System.err.println("Unexpected operator " + ch + " on line " + line);
                        correct = false;
                        return;
                    }
                }
                //Braces
                if (ch == '(' || ch == ')') {
                    state = READY;
                    Token t = new Token(String.valueOf(ch));
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                }
            }

            //Found a variable name or boolean
            if (state == READY) {
                if ((ch =='t' || ch == 'T') && pos < data.length()-4 && data.substring(pos, pos+4).toLowerCase().equals("true")) {
                    if (substate == READ_OBJECT_FIELD) {
                        System.err.println("Reserved word cannot be a key on line " + line);
                        return;
                    }
                    pos += 4;
                    Token t = new Token("true");
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                    continue;
                } else if ((ch =='f' || ch == 'F') && pos < data.length()-5 && data.substring(pos, pos+5).toLowerCase().equals("false")) {
                    if (substate == READ_OBJECT_FIELD) {
                        System.err.println("Reserved word cannot be a key on line " + line);
                        correct = false;
                        return;
                    }
                    pos += 5;
                    Token t = new Token("false");
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                    continue;
                }
                if (String.valueOf(ch).matches("[a-zA-Z$_]")) {
                    state = READ_VAR_NAME;
                }
            }

            //Reading a variable name (cont.)
            if (last_token.length() > 0 && state == READ_VAR_NAME) {
                if (Character.isWhitespace(ch) || String.valueOf(ch).matches("[(){}\\[\\].,<>^|&*/%?;:+=-]") ||
                        substate == READ_OBJECT_FIELD && (ch == ':' || ch == ',') ||
                        substate == READ_OBJECT_VALUE && ch == ',' ||
                        substate == READ_ARRAY && ch == ',') {
                    state = READY;
                    if (substate == READ_OBJECT_FIELD && !Token.keywords.contains(last_token) && cur.getType() != Token.BRACE_CLOSE && cur.getType() != Token.ARRAY_END) {
                        last_token = "<" + last_token + ">";
                    } else if (ch == '(' && (last_token.matches("function|class") || cur.prev != null && cur.prev.getContent().matches("function|class") ||
                          cur.prev != null && cur.prev.getContent().equals("*") && cur.prev.prev != null && cur.prev.prev.getContent().equals("function"))) {
                        stack.add(READ_FUNC_ARGS);
                    } else if (last_token.equals("throw") && ch == '(') {
                        last_token = last_token + "$";
                    }
                    if (ch == ')' && substate == READ_FUNC_ARGS) {
                        stack.removeElementAt(stack.size()-1);
                    }
                    Token t = new Token(last_token);
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                    if (String.valueOf(ch).matches("[(){}\\[\\].,<>^|&*/%?;:+=-]")) {
                        if (pos < data.length()-2 && data.substring(pos, pos+3).equals("?.[")) {
                            last_token = "?.";
                            pos += 2;
                        } else if (pos < data.length()-1 && data.substring(pos, pos+2).equals("?.")) {
                            last_token = "?";
                            pos += 1;
                        }
                        continue;
                    }
                } else if (!String.valueOf(ch).matches("[a-zA-Z0-9$_-]")) {
                    System.err.println("Unexpected character in variable name " + ch + " on line " + line);
                    correct = false;
                    return;
                }
            }

            if (state == READY && (substate == 0 || substate == READ_FUNC_BLOCK) && (ch == '\n' || ch == ';') &&
                    cur.getType() != Token.SEMICOLON && cur.getType() != Token.BLOCK_START) {
                Token t = new Token(";");
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
                pos++;
                continue;
            }

            if (state == READY && ch == '(' && last_token.matches("function|class")) {
                stack.add(READ_FUNC_ARGS);
            } else if (state == READY && ch == ')' && substate == READ_FUNC_ARGS) {
                stack.removeElementAt(stack.size()-1);
            }

            //Found an array declaration
            if ((ch == '[' || ch == ']') && state != READ_STRING && state != READ_REGEXP) {
                if (state != READY) {
                    state = READY;
                    Token t = new Token(last_token);
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                }
                if (substate == READ_OBJECT_FIELD) {
                    System.err.println("Unexpected array value on line " + line);
                    correct = false;
                    return;
                }
                if (ch == '[') stack.add(READ_ARRAY);
                else stack.removeElementAt(stack.size()-1);
                String str = (last_token.equals("?.") ? last_token : "") + ch;
                Token t = new Token(str);
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
            }

            if (substate == READ_OBJECT_FIELD && ch == ':') {
                stack.set(stack.size()-1, READ_OBJECT_VALUE);
            }

            if ((substate == READ_OBJECT_FIELD || substate == READ_OBJECT_VALUE) && ch == ',') {
                stack.set(stack.size()-1, READ_OBJECT_FIELD);
            }

            //Reading an operator (cont.)
            if (last_token.length() > 0 && state == READ_OPERATOR) {
                String op = last_token + ch;
                boolean valid = false;
                for (int i = 0; i < ops.length; i++) {
                    if (ops[i].equals(op)) {
                        valid = true;
                        break;
                    }
                }
                //Anything else can be the next token
                if (!String.valueOf(ch).matches("[,<>~^|&*/%!?:+=-]") || !valid) {
                    state = READY;
                    Token t = new Token(last_token);
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;

                    if (!Character.isWhitespace(ch)) {
                        continue;
                    }
                } /* else {
                    if (!String.valueOf(ch).matches("[<>~^|&*%/?!:=+-]")) {
                        System.err.println("Illegal operator " + (last_token + ch) + " on line " + line);
                        return;
                    }
                    String op = last_token + ch;
                    boolean flag = false;
                    for (int i = 0; i < ops.length; i++) {
                        if (ops[i].equals(op)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        System.err.println("Illegal operator " + op + " on line " + line);
                        return;
                    }
                } */
            }

            //Reading a number literal (cont.)
            if (state == READ_NUMBER && !(ch == '.' || ch == 'E' || ch == 'e' || ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F' || pos > 0 && pos < data.length()-1 && data.substring(pos-1, pos+1).equals("0x")) &&
                    !(sign || (ch == '+' || ch == '-') && pos > 0 && data.substring(pos-1, pos).matches("[Ee]"))) {
                if (Character.isWhitespace(ch) ||
                        substate == READ_OBJECT_VALUE && (ch == ',' || ch == '}') ||
                        substate == READ_ARRAY && ch == ',' ||
                        String.valueOf(ch).matches("[,()<>~^|&*/%!?;:+=-]")) {
                    state = READY;
                    Token t = new Token(last_token);
                    last_token = "";
                    cur.next = t;
                    t.prev = cur;
                    cur = t;
                    if (!Character.isWhitespace(ch)) {
                        continue;
                    }
                } else {
                    System.err.println("Unexpected character '" + ch + "' on line " + line);
                    correct = false;
                    return;
                }
            }

            //Found an object declaration
            if ((ch == '{' || ch == '}') && state != READ_STRING && state != READ_REGEXP) {
                if (state != READY && state != READ_STRING) {
                    System.err.println("Unexpected object value on line " + line);
                    correct = false;
                    return;
                }
                Token t;
                String s = data.substring(pos+1).trim();
                boolean testObj = (s.replaceAll("\r?\n", " ").matches("[a-zA-Z0-9_-]+\\s*(:\\s*[^}:,]+.*|,\\s*\\S+.*|\\s*\\}$)") ||
                                  s.replaceAll("\r?\n", " ").matches("\"[a-zA-Z0-9 _-]+\"\\s*:.*")) &&
                                  !(cur.getType() == Token.KEYWORD && !cur.getContent().matches("throw|return"));
                if (ch == '{' && cur.getContent().matches("\\)|else|do|switch") ||
                        ch == '}' && substate == READ_FUNC_BLOCK) {
                    if (ch == '{') stack.add(READ_FUNC_BLOCK);
                    else stack.removeElementAt(stack.size()-1);
                    t = new Token(String.valueOf(ch));
                    if (!checkLevel(ch)) return;
                } else if (testObj && ch == '{' || ch == '}' && substate > 0 && substate != 9) {
                    if (ch == '{') stack.add(READ_OBJECT_FIELD);
                    else stack.removeElementAt(stack.size()-1);
                    t = new Token(String.valueOf(ch)+String.valueOf(ch));
                } else {
                    t = new Token(String.valueOf(ch));
                    if (!checkLevel(ch)) return;
                }
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
            }

            if (esc && state != READ_STRING && state != READ_REGEXP) {
                 System.err.println("Unexpected escape character " + ch + " on line " + line);
                 correct = false;
                 return;
            }

            if (state == READY && (ch == '"' || ch == '\'') && !esc) {
                state = READ_STRING;
                strToken = ch;
                pos++;
                continue;
            }

            if (state == READ_STRING && ch == strToken && !esc) {
                state = READY;
                String s = last_token;
                if (substate != READ_OBJECT_FIELD) s = "\"" + s + "\"";
                else s = "<" + s + ">";
                Token t = new Token(s);
                strToken = '\0';
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
                pos++;
                continue;
            }

            if (state == READY && (ch == '/') && !esc && cur.getType() != Token.VALUE && cur.getType() != Token.VAR_NAME &&
                    cur.getType() != Token.BRACE_CLOSE && cur.getType() != Token.ARRAY_END && cur.getType() != Token.OBJECT_END) {
                state = READ_REGEXP;
                strToken = ch;
                pos++;
                continue;
            }

            if (state == READ_REGEXP && ch == '/' && !esc) {
                state = READY;
                String flags = "";
                while (pos < data.length()-1 && data.charAt(pos+1) >= 'a' && data.charAt(pos+1) <= 'z') {
                    flags += data.charAt(pos+1);
                    pos++;
                }
                String s = '/' + last_token + '/' + flags;
                Token t = new Token(s);
                strToken = '\0';
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
                pos++;
                continue;
            } else if (state == READ_REGEXP) {
                last_token += ch;
                pos++;
                continue;
            }

            if (esc) esc = false;

            if (state == READY && (ch == ',' || ch == ':' || ch == '.')) {
                String str = (last_token.equals("?") ? last_token : "") + ch;
                Token t = new Token(str);
                last_token = "";
                cur.next = t;
                t.prev = cur;
                cur = t;
            }

            if (state != READY) last_token += ch;

            pos++;

            if (last_token.length() > 0 && pos == data.length() &&
                  !(state == READ_STRING || state == READ_OBJECT_FIELD ||
                    state == READ_OBJECT_VALUE || state == READ_ARRAY || state == READ_REGEXP)) {
                data += " ";
            }
        }

        if (state != READY) {
            if (state == READ_STRING) {
                errorDescription = getErrorMessage("unterminated string");
            }
            if (state == READ_REGEXP) {
                errorDescription = getErrorMessage("unterminated regular expression");
            }
            if (state == READ_OBJECT_FIELD || state == READ_OBJECT_VALUE) {
                errorDescription = getErrorMessage("unterminated object declaration");
            }
            if (state == READ_ARRAY) {
                errorDescription = getErrorMessage("unterminated array declaration");
            }
            correct = false;
            System.err.println(errorDescription);

            return;
        }
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(java.util.Locale loc) {
        locale = loc;
    }

    private static Locale locale = java.util.Locale.US;

    private boolean esc = false;
    private boolean sign = false;

    public boolean correct = true;
    private int lvl = 0;

    private char strToken;
    private String last_token = "";

    private int state;
    private int line = 1;
    private int lastLineStartPos = 0;

    private static String[] ops = new String[37];

    static {
        ops[0] = ">>";
        ops[1] = "<<";
        ops[2] = ">>>";
        ops[3] = "&&";
        ops[4] = "||";
        ops[5] = "++";
        ops[6] = "--";
        ops[7] = "^";
        ops[8] = "&";
        ops[9] = "|";
        ops[10] = "~";
        ops[11] = "!";
        ops[12] = "*";
        ops[13] = "/";
        ops[14] = "%";
        ops[15] = "+";
        ops[16] = "-";
        ops[17] = ">";
        ops[18] = "<";
        ops[19] = ">=";
        ops[20] = "<=";
        ops[21] = "==";
        ops[22] = "!=";
        ops[23] = "===";
        ops[24] = "!==";
        ops[25] = "?";
        ops[26] = ":";
        ops[27] = "+=";
        ops[28] = "-=";
        ops[29] = "*=";
        ops[30] = "/=";
        ops[31] = "%=";
        ops[32] = "&=";
        ops[33] = "|=";
        ops[34] = "^=";
        ops[35] = ",";
        ops[36] = "=>";
    }

    private int READY = 0;
    private int READ_NUMBER = 1;
    private int READ_OPERATOR = 2;
    private int READ_VAR_NAME = 3;
    private int READ_STRING = 4;
    private int READ_ARRAY = 5;
    private int READ_OBJECT_FIELD = 6;
    private int READ_OBJECT_VALUE = 7;
    private int READ_FUNC_ARGS = 8;
    private int READ_FUNC_BLOCK = 9;
    private int READ_COMMENT = 10;
    private int READ_REGEXP = 11;

    private boolean comment_multiline;

    private Vector<Integer> stack = new Vector<Integer>();

    private int pos = 0;
    private Token cur = null;
    private Token head = null;
    public String errorDescription;
    //private Hashtable<String, Node> ids = new Hashtable<String, Node>();
    //private Hashtable<String, Vector<Node>> classes = new Hashtable<String, Vector<Node>>();

    public String data = "";
}
