package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class ArrayBufferC extends ArrayC {

    public ArrayBufferC() {
        super();
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 1 && args.get(0).getType().equals("Array")) {
            return new TypedArray(32, (JSArray)args.get(0)).buffer;
        } else if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            int length = (int)((JSInt)args.get(0)).getValue();
            return new ArrayBuffer(length);
        } else {
            return new ArrayBuffer();
        }
    }

}
