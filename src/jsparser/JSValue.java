package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public abstract class JSValue {

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "null";
    }

    public static String getType(String value) {
        if (value.equals("[]")) {
            return "Array";
        }
        if (value.equals("{}")) {
            return "Object";
        }
        if (value.matches("^([\"']).*\\1$")) {
            return "String";
        }
        if (value.matches("^[+-]?[0-9]+$")) {
            return "Integer";
        }
        if (value.matches("^[+-]?[0-9]+(\\.[0-9]+([eE]-?[0-9]+)?)?$")) {
            return "Float";
        }
        if (value.toLowerCase().matches("^(true|false)$")) {
            return "Boolean";
        }
        if (value.matches("^[+-]?Infinity$")) {
            return "Number";
        }
        if (value.equals("NaN")) {
            return "NaN";
        }
        if (value.toLowerCase().equals("null")) {
            return "null";
        }
        if (value.toLowerCase().equals("undefined")) {
            return "undefined";
        }
        return "null";
    }

    public static JSValue create(String type, String value) {
        if (type.charAt(0) == 'S') {
            if (value.matches("^\".*\"$")) {
                value = value.substring(1, value.length()-1);
            }
            return new JSString(value);
        }
        if (type.charAt(0) == 'I') {
            if (value.toLowerCase().equals("true")) return new JSInt(1);
            else if (value.toLowerCase().equals("false")) return new JSInt(0);
            else return new JSInt(value);
        }
        if (type.charAt(0) == 'F') {
            if (value.toLowerCase().equals("true")) return new JSFloat(1);
            else if (value.toLowerCase().equals("false")) return new JSFloat(0);
            else return new JSFloat(value);
        }
        if (type.charAt(0) == 'B') {
            return new JSBool(value);
        }
        if (type.equals("Number")) {
            if (value.equals("NaN")) return NaN.getInstance();
            else return Infinity.getInstance(value.charAt(0) != '-');
        }
        if (type.charAt(0) == 'N') {
            return NaN.getInstance();
        }
        if (type.equals("null")) {
            return Null.getInstance();
        }
        if (type.equals("undefined")) {
            return Undefined.getInstance();
        }
        return null;
    }

    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        System.err.println("The instance is not callable");
        return Undefined.getInstance();
    }

    public JSValue call(JSObject context, Vector<JSValue> args) {
        return call(context, args, false);
    }

    public JSValue call(Vector<JSValue> args) {
        return call(null, args);
    }

    public abstract JSString asString();
    public abstract JSInt asInt();
    public abstract JSFloat asFloat();
    public abstract JSBool asBool();

    private String type;
}
