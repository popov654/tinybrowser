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
        items.put("prototype", TypedArrayProto.getInstance());
        TypedArrayProto.getInstance().set("constructor", this);
    }

    public TypedArrayC(int bits) {
        super();
        items.put("prototype", TypedArrayProto.getInstance());
        TypedArrayProto.getInstance().set("constructor", this);
        this.bits = bits;
    }

    public TypedArrayC(int bits, boolean isFloat) {
        super();
        items.put("prototype", TypedArrayProto.getInstance());
        TypedArrayProto.getInstance().set("constructor", this);
        this.bits = !isFloat ? bits : 64;
        this.isFloat = isFloat;
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
            TypedArray result = new TypedArray(bits, new ArrayBuffer(data));
            if (isFloat) result.setIsFloat();
            return result;
        } else if (args.size() == 1 && args.get(0).getType().equals("Array")) {
            TypedArray result = new TypedArray(bits, (JSArray)args.get(0));
            if (isFloat) result.setIsFloat();
            return result;
        } else if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            int length = (int)((JSInt)args.get(0)).getValue();
            TypedArray result = new TypedArray(bits, length);
            if (isFloat) result.setIsFloat();
            return result;
        } else {
            TypedArray result = new TypedArray(bits, 0);
            if (isFloat) result.setIsFloat();
            return result;
        }
    }

    public int bits = 8;
    public boolean isFloat = false;
    
}
