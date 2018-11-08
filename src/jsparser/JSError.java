package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSError {

    public JSError(JSValue v, String desc, Vector<String> s) {
        if (v != null) {
            this.value = v;
        }
        this.text = desc;
        this.stack = s;
    }

    public String printStack() {
        String result = "";
        if (!stack.isEmpty()) result += "in ";
        for (int i = 0; i < stack.size(); i++) {
            result += (i > 0 ? "   " : "") + stack.get(i) + "()" + (i < stack.size()-1 ? "\n" : "");
        }
        return result;
    }

     public String getText() {
        return text;
    }

    public JSValue getValue() {
        return value;
    }

    private JSValue value = Undefined.getInstance();
    private String text;
    private Vector<String> stack;
}
