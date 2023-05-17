package jsparser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Alex
 */
public class JSDate extends JSObject implements Comparable {
    public JSDate() {
        items.put("__proto__", DateProto.getInstance());
        value = System.currentTimeMillis();
    }
    public JSDate(long val) {
        items.put("__proto__", DateProto.getInstance());
        value = val;
    }
    public JSDate(String val) {
        Locale locale = JSParser.getLocale();
        items.put("__proto__", DateProto.getInstance());
        try {
            DateFormat format = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss", locale);
            value = format.parse(val).getTime();
        } catch (ParseException ex1) {
            try {
                DateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", locale);
                value = format.parse(val).getTime();
            } catch (ParseException ex2) {
                try {
                    DateFormat format = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", locale);
                    value = format.parse(val).getTime();
                } catch (ParseException ex3) {
                    System.err.println("Date parse error");
                }
            }
        }
    }

    @Override
    public JSValue get(JSString str) {
        return ((JSObject)items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(String str) {
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
        return new JSBool(true);
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
        return new JSString(toString());
    }

    public long getValue() {
        return value;
    }

    public void setValue(long val) {
        value = val;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object obj) {
        JSDate d = (obj instanceof JSDate) ? (JSDate)obj : new JSDate(((JSValue)obj).asInt().getValue());
        long delta = (long)(value - d.getValue());
        return delta < 0 ? -1 : (delta > 0 ? 1 : 0);
    }

    public String format(String f) {
        DateFormat format = new SimpleDateFormat(f, Locale.US);
        return format.format(new Date(value));
    }

    public void setTimezone(String timezone) {
        tz = TimeZone.getTimeZone(timezone);
    }

    @Override
    public String toString() {
        DateFormat format = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.US);
        if (tz != null) format.setTimeZone(tz);
        return format.format(new Date(value));
    }

    private long value;
    TimeZone tz;
    private String type = "Object";
}
