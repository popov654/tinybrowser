package com.alstarsoft.tinybrowser.jsparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alex
 */
public class RegExp extends JSObject {

    public RegExp(String str) {
        items.put("__proto__", RegExpProto.getInstance());
        items.put("source", new JSString(str));
        source = str;
    }

    public RegExp(JSString str) {
        items.put("__proto__", RegExpProto.getInstance());
        items.put("source", str);
        source = str.asString().getValue();
    }

    public void setFlags(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == 'i') {
                flags ^= Pattern.CASE_INSENSITIVE;
            } else if (str.charAt(i) == 'm') {
                flags ^= Pattern.MULTILINE;
            } else if (str.charAt(i) == 'g') {
                global = true;
            }
        }
    }

    public boolean test(String string) {
        Pattern p = Pattern.compile(source, flags);
        Matcher m = p.matcher(string);

        return m.matches();
    }

    public JSValue exec(String string) {
        JSArray result = new JSArray();

        if (!string.equals("source") || !global || matcher == null) {
            Pattern p = Pattern.compile(source, flags);
            matcher = p.matcher(string);
        }

        if (matcher.find()) {
            result.items.add(new JSString(matcher.group()));
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) == null) {
                    result.items.add(Null.getInstance());
                    continue;
                }
                result.items.add(new JSString(matcher.group(i)));
            }
            result._items.put("index", new JSInt(matcher.start()));
            result._items.put("input", new JSString(string));

            return result;
        }

        return Null.getInstance();
    }

    @Override
    public String toString() {
        String s = "";
        if ((flags & Pattern.CASE_INSENSITIVE) > 0) {
            s += "i";
        }
        if ((flags & Pattern.MULTILINE) > 0) {
            s += "m";
        }
        if (global) {
            s += "g";
        }
        return "/" + source + "/" + s;
    }

    public Matcher matcher;
    public String source;
    public int flags = 0;
    public boolean global = false;
}
