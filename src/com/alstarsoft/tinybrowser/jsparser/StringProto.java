package com.alstarsoft.tinybrowser.jsparser;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class StringProto  extends JSObject {

    class sliceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSString)context).slice();
            } else if (args.size() == 1) {
                return ((JSString)context).slice(args.get(0).asInt());
            } else {
                return ((JSString)context).slice(args.get(0).asInt(), args.get(1).asInt());
            }
        }
    }

    class splitFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                return ((JSString)context).split();
            } else {
                return ((JSString)context).split(args.get(0).asString());
            }
        }
    }

    class toUpperCaseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((JSString)context).toUpperCase();
        }
    }

    class toLowerCaseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return ((JSString)context).toLowerCase();
        }
    }

    class charAtFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSInt index = new JSInt(0);
            if (args.size() > 0) index = args.get(0).asInt();
            return ((JSString)context).charAt(index);
        }
    }

    class matchFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0 || !(args.get(0) instanceof RegExp)) {
                JSError e = new JSError(null, "Type error: argument 1 is not a regular expression", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            return ((RegExp)args.get(0)).exec(((JSString)context).getValue());
        }
    }

    class replaceFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1 || !(args.get(0) instanceof RegExp || args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Type error: argument 1 is not a string or regular expression", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (args.size() < 2 || !(args.get(1) instanceof JSString || args.get(1) instanceof Function)) {
                JSError e = new JSError(null, "Type error: argument 1 is not a string or function", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (args.get(0) instanceof JSString && args.get(1) instanceof JSString) {
                return ((JSString)context).replace(args.get(0).asString(), args.get(1).asString());
            }
            if (args.get(0) instanceof RegExp && args.get(1) instanceof JSString) {
                RegExp regexp = (RegExp) args.get(0);
                return regexp.global ? ((JSString)context).replaceRegexpAll(regexp.source, args.get(1).asString()) : ((JSString)context).replaceRegexp(regexp.source, args.get(1).asString());
            }
            if (args.get(1) instanceof Function) {
                Vector<JSValue> params = new Vector<JSValue>();
                if (args.get(0) instanceof JSString) {
                    String needle = args.get(0).asString().getValue();
                    String source = ((JSString)context).getValue();
                    // String last = source;

                    // int limit = 10;
                    // int n = 0;

                    int start = source.indexOf(needle);
                    while (start >= 0) {
                        params.add(new JSString(needle));
                        params.add(new JSInt(start));
                        params.add(new JSString(source));

                        JSValue res = ((Function)args.get(1)).call(params);
                        source = source.substring(0, start) + res.asString().getValue() + source.substring(start + needle.length());

                        return new JSString(source);

                        /* if (source.equals(last)) {
                            n++;
                        }
                        if (n == limit) {
                            return new JSString(source);
                        }

                        start = source.indexOf(needle); */
                    }
                } else if (args.get(0) instanceof RegExp) {
                    int limit = 10;
                    int n = 0;
                    String source, last;
                    source = last = ((JSString)context).getValue();
                    int count = 0;
                    while (true) {
                        JSValue match = ((RegExp)args.get(0)).exec(source);
                        if (match instanceof Null) {
                            return new JSString(source);
                        }
                        JSArray data = (JSArray) match;
                        for (int i = 0; i < data.items.size(); i++) {
                            params.add(data.items.get(i).asString());
                        }
                        params.add(data._items.get("index"));
                        params.add(new JSString(source));

                        String needle = data.items.get(0).asString().getValue();
                        int start = (int) data._items.get("index").asInt().getValue();
                        
                        JSValue res = ((Function)args.get(1)).call(params);
                        source = source.substring(0, start) + res.asString().getValue() + source.substring(start + needle.length());

                        if (source.equals(last)) {
                            n++;
                        }
                        if (n == limit || !((RegExp)args.get(0)).global && count > 0) {
                            return new JSString(source);
                        }
                        count++;
                    }
                }
            }
            return new JSString(((JSString)context).getValue());
        }
    }

    private StringProto() {
        items.put("slice", new sliceFunction());
        items.put("split", new splitFunction());
        items.put("toUpperCase", new toUpperCaseFunction());
        items.put("toLowerCase", new toLowerCaseFunction());
        items.put("charAt", new charAtFunction());
        items.put("match", new matchFunction());
        items.put("replace", new replaceFunction());
    }

    public static StringProto getInstance() {
        if (instance == null) {
            instance = new StringProto();
        }
        return instance;
    }

    @Override
    public JSValue get(JSString str) {
        JSValue val = items.get(str.getValue());
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public JSValue get(String str) {
        JSValue val = items.get(str);
        return val != null ? val : Undefined.getInstance();
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

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
    private static StringProto instance = null;
}

