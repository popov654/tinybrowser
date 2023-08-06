package com.alstarsoft.tinybrowser.jsparser;

import java.nio.charset.Charset;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TextEncoderC extends Function {

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        String charset = "utf-8";
        if (args.size() >= 1 && args.get(0).getType().equals("String")) {
            charset = args.get(0).asString().getValue();
        }
        
        return new TextEncoder(charset);
    }

    class TextEncoder extends JSObject {

        TextEncoder(String enc) {
            charset = enc;
            items.put("charset", new JSString(charset));
            items.put("encode", new Function() {
                @Override
                public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                    return call(context, args);
                }

                @Override
                public JSValue call(JSObject context, Vector<JSValue> args) {
                    if (args.size() == 0) {
                        JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                        getCaller().error = e;
                        return Undefined.getInstance();
                    }
                    if (!args.get(0).getType().equals("String")) {
                        JSError e = new JSError(null, "Type error: argument 1 must be a string", getCaller().getStack());
                        getCaller().error = e;
                        return Undefined.getInstance();
                    }
                    String text = args.get(0).asString().getValue();
                    byte[] bytes = new String(text.getBytes(), Charset.forName(charset)).getBytes();

                    return new TypedArray(8, new ArrayBuffer(bytes));
                }
            });
        }

        @Override
        public String toString() {
            return "TextEncoder { charset: '" + charset + "' }";
        }

        public String charset;
    }

}
