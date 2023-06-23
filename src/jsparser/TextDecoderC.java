package jsparser;

import java.nio.charset.Charset;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TextDecoderC extends Function {

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
        
        return new TextDecoder(charset);
    }

    class TextDecoder extends JSObject {

        TextDecoder(String enc) {
            charset = enc;
            items.put("charset", new JSString(charset));
            items.put("decode", new Function() {
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
                    if (!args.get(0).getType().equals("Array")) {
                        JSError e = new JSError(null, "Type error: argument 1 must be an array", getCaller().getStack());
                        getCaller().error = e;
                        return Undefined.getInstance();
                    }
                    
                    JSArray a = (JSArray) args.get(0);
                    byte[] bytes = new byte[(int) a.get("length").asInt().getValue()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = (byte) (a.get(i).asInt().getValue() & 0xFF);
                    }

                    return new JSString(new String(bytes, Charset.forName(charset)));
                }
            });
        }

        @Override
        public String toString() {
            return "TextDecoder { charset: '" + charset + "' }";
        }

        public String charset;
    }

}
