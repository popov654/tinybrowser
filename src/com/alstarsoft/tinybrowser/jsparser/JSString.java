package com.alstarsoft.tinybrowser.jsparser;

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
        this(val, null);
    }
    public JSString(String val, Block block) {
        if (val == null) val = "";
        items.put("__proto__", StringProto.getInstance());
        if (val.replaceAll("\n", "").matches("^`.*`$")) {
            val = val.substring(1, val.length()-1);
            originalValue = val;
            dynamic = true;
        } else {
            value = val;
        }
        
    }

    public void evaluate() {
        String val = originalValue;
        int pos = 0;
        int lastPos = 0;
        int lastPosType = 0;
        Vector<String> parts = new Vector<String>();
        Vector<Integer> positions = new Vector<Integer>();
        while (pos < val.length()) {
            if (val.charAt(pos) == '\\') {
                pos++;
                continue;
            }
            if (val.charAt(pos) == '$' && val.charAt(pos+1) == '{') {
                positions.add(pos++);
                continue;
            }
            if (pos > 0 && lastPosType == 0 && val.charAt(pos) == '}' && positions.size() > 0) {
                int start = positions.get(0);
                positions.clear();

                if (lastPos < start) {
                    parts.add(val.substring(lastPos, start));
                }
                
                JSParser jp = new JSParser(val.substring(start+2, pos));
                Expression expr = Expression.create(jp.getHead());
                ((Block)expr).scope = block.scope;

                parts.add(expr.eval().getValue().toString());
                lastPos = pos+1;
            }
            pos++;
        }
        if (pos > lastPos+1) {
            parts.add(val.substring(lastPos, pos));
        }
        value = "";
        for (String p: parts) {
            value += p;
        }
    }

    public JSString toLowerCase() {
        return new JSString(value.toLowerCase(), block);
    }

    public JSString toUpperCase() {
        return new JSString(value.toUpperCase(), block);
    }

    public JSInt length() {
        return new JSInt(value.length());
    }

    public JSString slice() {
        return new JSString(value, block);
    }

    public JSString slice(JSInt start) {
        return new JSString(value.substring((int)start.getValue()), block);
    }

    public JSString slice(JSInt start, JSInt end) {
        return new JSString(value.substring((int)start.getValue(), (int)end.getValue()), block);
    }

    public JSArray split(JSString delimiter) {
        Vector<JSValue> v = new Vector<JSValue>();
        if (delimiter.getValue().isEmpty()) {
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                v.add(new JSString(String.valueOf(chars[i]), block));
            }
        } else {
            String[] parts = value.split(delimiter.getValue());
            for (int i = 0; i < parts.length; i++) {
                v.add(new JSString(parts[i], block));
            }
        }
        return new JSArray(v);
    }

    public JSArray split() {
        return new JSArray(new JSString(value, block));
    }

    public JSString charAt(JSInt index) {
        return new JSString(String.valueOf(value.charAt((int)index.getValue())), block);
    }

    public JSString replace(JSString from, JSString to) {
        return new JSString(value.replace(from.getValue(), to.getValue()), block);
    }

    public JSString replaceRegexp(String regexp, JSString to) {
        return new JSString(value.replace(regexp, to.getValue()), block);
    }

    public JSString replaceRegexpAll(String regexp, JSString to) {
        return new JSString(value.replaceAll(regexp, to.getValue()), block);
    }

    public JSString get(int index) {
        return charAt(new JSInt(index));
    }

    @Override
    public JSValue get(JSString str) {
        if (str.getValue().equals("length")) {
            return new JSInt(value.length());
        }
        return ((JSObject)items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(value.length());
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
        if (dynamic) {
            return new JSString(value, block);
        }
        return new JSString(value, block);
    }

    public String getValue() {
        if (dynamic) evaluate();
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
        JSString str = new JSString(value, block);
        str.originalValue = originalValue;
        str.dynamic = dynamic;
        return str;
    }

    @Override
    public String toString() {
        if (dynamic) {
            evaluate();
        }
        return "\"" + value + "\"";
    }

    boolean dynamic = false;
    private String originalValue;
    private String value;
    private String type = "String";
}
