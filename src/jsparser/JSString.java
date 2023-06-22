package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSString extends JSObject implements Comparable {
    public JSString() {
        items.put("__proto__", StringProto.getInstance());
    }
    public JSString(String val) {
        if (val == null) val = "";
        items.put("__proto__", StringProto.getInstance());
        value = val;
    }

    public JSString toLowerCase() {
        return new JSString(value.toLowerCase());
    }

    public JSString toUpperCase() {
        return new JSString(value.toUpperCase());
    }

    public JSInt length() {
        return new JSInt(value.length());
    }

    public JSString slice() {
        return new JSString(value);
    }

    public JSString slice(JSInt start) {
        return new JSString(value.substring((int)start.getValue()));
    }

    public JSString slice(JSInt start, JSInt end) {
        return new JSString(value.substring((int)start.getValue(), (int)end.getValue()));
    }

    public JSArray split(JSString delimiter) {
        Vector<JSValue> v = new Vector<JSValue>();
        if (delimiter.getValue().isEmpty()) {
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                v.add(new JSString(String.valueOf(chars[i])));
            }
        } else {
            String[] parts = value.split(delimiter.getValue());
            for (int i = 0; i < parts.length; i++) {
                v.add(new JSString(parts[i]));
            }
        }
        return new JSArray(v);
    }

    public JSArray split() {
        return new JSArray(new JSString(value));
    }

    public JSString charAt(JSInt index) {
        return new JSString(String.valueOf(value.charAt((int)index.getValue())));
    }

    public JSString get(int index) {
        return charAt(new JSInt(index));
    }

    @Override
    public JSValue get(JSString str) {
        if (str.getValue().equals("length")) {
            return new JSInt(value.length());
        }
        if (str.asInt().toString().matches("[0-9][1-9]*")) {
            return new JSString(value.charAt(Integer.parseInt(str.toString())) + "");
        }
        return ((JSObject)items.get("__proto__")).get(str);
    }
    
    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(value.length());
        }
        if (str.matches("[0-9][1-9]*")) {
            return new JSString(value.charAt(Integer.parseInt(str)) + "");
        }
        return ((JSObject)items.get("__proto__")).get(str);
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    public boolean isNaN() {
        return false;
    }

    @Override
    public JSBool asBool() {
        return new JSBool(!value.isEmpty() && !value.toLowerCase().equals("false"));
    }

    @Override
    public JSInt asInt() {
        return new JSInt(value);
    }

    @Override
    public JSFloat asFloat() {
        return new JSFloat(value);
    }

    public JSValue parseInt() {
        try {
            return new JSInt(Integer.parseInt(value));
        } catch(Exception ex) {
            return new NaN();
        }
    }

    public JSValue parseFloat() {
        try {
            return new JSInt(Float.parseFloat(value));
        } catch(Exception ex) {
            return new NaN();
        }
    }

    @Override
    public JSString asString() {
        return new JSString(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        JSString s = (obj instanceof JSString) ? (JSString)obj : ((JSValue)obj).asString();
        return value.compareTo(s.getValue());
    }

    @Override
    public JSString clone() {
        return new JSString(value);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    private String value;
    private String type = "String";
}
