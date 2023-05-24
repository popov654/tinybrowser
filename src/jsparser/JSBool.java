package jsparser;

/**
 *
 * @author Alex
 */
public class JSBool extends JSValue implements Comparable {
    public JSBool(String val) {
        if (!val.isEmpty() && !val.equals("0") && !val.toLowerCase().equals("false")) {
            value = true;
        } else {
            value = false;
        }
    }
    public JSBool(boolean val) {
        value = val;
    }
    public JSBool(int val) {
        value = (val != 0);
    }

    public JSBool asBool() {
        return new JSBool(value);
    }

    public JSInt asInt() {
        int i = value ? 1 : 0;
        return new JSInt(i);
    }

    public JSFloat asFloat() {
        float f = value ? 1 : 0;
        return new JSFloat(f);
    }

    public JSString asString() {
        String s = value ? "true" : "false";
        return new JSString(s);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        JSBool b = (obj instanceof JSBool) ? (JSBool)obj : ((JSValue)obj).asBool();
        return (int)(asInt().getValue() - b.asInt().getValue());
    }

    @Override
    public JSBool clone() {
        return new JSBool(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private boolean value;
    private String type = "Boolean";
}
