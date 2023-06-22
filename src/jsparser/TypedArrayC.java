package jsparser;

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
        if (args.size() == 1 && args.get(0).getType().equals("Array")) {
            Vector<JSValue> v = ((JSArray)args.get(0)).items;
            TypedArray a = new TypedArray(bits);
            for (int i = 0; i < v.size(); i++) {
                a.push(v.get(i).asInt());
            }
            return a;
        } else if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            int length = (int)((JSInt)args.get(0)).getValue();
            TypedArray a = new TypedArray(bits);
            for (int i = 0; i < length; i++) {
                a.push(new JSInt(0));
            }
            return a;
        } else {
            return new TypedArray(bits);
        }
    }

    public int bits = 8;
    
}
