package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class NumberC extends Function {

    public NumberC() {
        items.put("prototype", NumberProto.getInstance());
        items.put("MAX_SAFE_INTEGER", new JSInt(9007199254740991L));
        items.put("MIN_SAFE_INTEGER", new JSInt(-9007199254740991L));
        items.put("MAX_VALUE", new JSFloat(Double.MAX_VALUE));
        items.put("MIN_VALUE", new JSFloat(Double.MIN_VALUE));
        items.put("POSITIVE_INFINITY", Infinity.getInstance(true));
        items.put("NEGATIVE_INFINITY", Infinity.getInstance(false));
        NumberProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() >= 1) {
            JSValue val = args.get(0);
            String type = val.getType();
            if (type.equals("Float")) return new JSFloat(((JSFloat)val).getValue());
            if (type.equals("Integer")) return new JSInt(((JSInt)val).getValue());
            if (type.equals("null")) return new JSInt(0);
            if (type.equals("String")) {
               String s = ((JSString)val).getValue();
               if (s.matches("[0-9]+")) {
                   try {
                       return new JSInt(Integer.parseInt(s));
                   } catch (NumberFormatException e) {
                       return NaN.getInstance();
                   }
               }
               try {
                   return new JSFloat(Double.parseDouble(s));
               } catch (NumberFormatException e) {
                   return NaN.getInstance();
               }
            }
            return NaN.getInstance();
        }
        return new JSInt(0);
    }

}
