package com.alstarsoft.tinybrowser.jsparser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class DateProto  extends JSObject {

    class getDateFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.DAY_OF_MONTH));
        }
    }

    class getMonthFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.MONTH));
        }
    }

    class getYearFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.YEAR));
        }
    }

    class getFullYearFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.YEAR)+1900);
        }
    }

    class getHoursFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.HOUR_OF_DAY));
        }
    }

    class getMinutesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.MINUTE));
        }
    }

    class getSecondsFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.SECOND));
        }
    }

    class getMilliecondsFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((JSDate)context).getValue()));
            return new JSInt(c.get(Calendar.MILLISECOND));
        }
    }

    class setDateFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.DAY_OF_MONTH, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setMonthFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.MONTH, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setYearFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.YEAR, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setHoursFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.HOUR_OF_DAY, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setMinutesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.MINUTE, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setSecondsFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.SECOND, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class setMilliecondsFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(((JSDate)context).getValue()));
                c.set(Calendar.MILLISECOND, (int)args.get(0).asInt().getValue());
                ((JSDate)context).setValue(c.getTimeInMillis());
            }
            return context;
        }
    }

    class getTimeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSInt(((JSDate)context).getValue());
        }
    }

    class toGMTStringFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Date d = new Date(((JSDate)context).getValue());
            DateFormat format = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss 'GMT'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return new JSString(format.format(d));
        }
    }

    class toISOStringFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Date d = new Date(((JSDate)context).getValue());
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return new JSString(format.format(d));
        }
    }

    class nowFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSInt(System.currentTimeMillis());
        }
    }

    private DateProto() {
        items.put("getDate", new getDateFunction());
        items.put("getMonth", new getMonthFunction());
        items.put("getYear", new getYearFunction());
        items.put("getFullYear", new getFullYearFunction());
        items.put("getHours", new getHoursFunction());
        items.put("getMinutes", new getMinutesFunction());
        items.put("getSeconds", new getSecondsFunction());
        items.put("getMillieconds", new getMilliecondsFunction());
        items.put("setDate", new setDateFunction());
        items.put("setMonth", new setMonthFunction());
        items.put("setYear", new setYearFunction());
        items.put("setHours", new setHoursFunction());
        items.put("setMinutes", new setMinutesFunction());
        items.put("setSeconds", new setSecondsFunction());
        items.put("setMillieconds", new setMilliecondsFunction());
        items.put("getTime", new getTimeFunction());
        items.put("toGMTString", new toGMTStringFunction());
        items.put("toUTCString", new toGMTStringFunction());
        items.put("toISOString", new toISOStringFunction());
        items.put("now", new nowFunction());
    }

    public static DateProto getInstance() {
        if (instance == null) {
            instance = new DateProto();
        }
        return instance;
    }

    @Override
    public void set(JSString str, JSValue value) {
        set(str.getValue(), value);
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.equals("constructor")) {
            super.set(str, value);
        }
    }

    @Override
    public String toString() {
        String result = "";
        Set keys = items.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            if (result.length() > 0) result += ", ";
            String str = (String)it.next();
            result += str + ": " + items.get(str).toString();
        }
        return "{" + result + "}";
    }

    @Override
    public String getType() {
        return type;
    }

    private String type = "Object";
    private static DateProto instance = null;
}
