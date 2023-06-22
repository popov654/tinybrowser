package jsparser;

import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TypedArrayC extends ArrayC {

    public TypedArrayC() {
        super();
    }

    public TypedArrayC(int bits) {
        super();
        this.bits = bits;
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() >= 1 && args.get(0) instanceof ArrayBuffer) {
            if (args.size() == 1) {
                return new TypedArray(bits, (ArrayBuffer)args.get(0));
            }
            ArrayBuffer buffer = (ArrayBuffer)args.get(0);
            int from = 0;
            int to = buffer.data.length;
            if (args.size() >= 2) {
                from = Math.min(Math.max(0, (int) args.get(1).asInt().getValue()), buffer.data.length-1);
            }
            if (args.size() >= 3) {
                to = Math.min(Math.max(from, from + (int) args.get(2).asInt().getValue()), buffer.data.length);
            }
            byte[] data = Arrays.copyOfRange(buffer.data, from, to);
            return new TypedArray(bits, new ArrayBuffer(data));
        } else if (args.size() == 1 && args.get(0).getType().equals("Array")) {
            return new TypedArray(bits, (JSArray)args.get(0));
        } else if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            int length = (int)((JSInt)args.get(0)).getValue();
            return new TypedArray(bits, length);
        } else {
            return new TypedArray(bits, 0);
        }
    }

    public int bits = 8;
    
}
