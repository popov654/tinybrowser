package jsparser;

/**
 *
 * @author Alex
 */
public class JSInt extends JSObject implements Comparable {
    public JSInt(String val) {
        items.put("__proto__", NumberProto.getInstance());
        try {
            if (val.matches("[0-9]+")) {
                value = Long.parseLong(val);
            } else {
                value = (int)Float.parseFloat(val);
            }
        } catch(NumberFormatException e) {
            value = 0;
        }
    }
    public JSInt(int val) {
        items.put("__proto__", NumberProto.getInstance());
        value = val;
    }
    public JSInt(long val) {
        items.put("__proto__", NumberProto.getInstance());
        value = val;
    }
    public JSInt(float val) {
        items.put("__proto__", NumberProto.getInstance());
        value = (long)val;
    }
    public JSInt(double val) {
        items.put("__proto__", NumberProto.getInstance());
        value = (long)val;
    }

    public boolean isNaN() {
        return false;
    }

    @Override
    public JSBool asBool() {
        return new JSBool(value != 0);
    }

    @Override
    public JSInt asInt() {
        return new JSInt(value);
    }

    @Override
    public JSFloat asFloat() {
        return new JSFloat(value);
    }

    @Override
    public JSString asString() {
        return new JSString(String.valueOf(value));
    }


    public long getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj.equals(Infinity.getInstance(true))) {
            return -1;
        }
        if (obj.equals(Infinity.getInstance(false))) {
            return 1;
        }
        JSInt i = (obj instanceof JSInt) ? (JSInt)obj : ((JSValue)obj).asInt();
        return (int)(value - i.getValue());
    }

    @Override
    public JSInt clone() {
        return new JSInt(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private long value;
    private String type = "Integer";
}
