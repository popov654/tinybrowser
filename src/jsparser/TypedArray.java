package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TypedArray extends JSArray {

    public TypedArray() {}

    public TypedArray(int bits) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
    }

    @Override
    public JSValue get(int index) {
        long result = 0;

        if (bytes * index <= data.size() - bytes) {
            for (int i = 0; i < bytes; i++) {
                long comp = (data.get(bytes * index + i) & 0xFF) << 8 * i;
                result = result | comp;
            }
        }

        return new JSInt(result);
    }

    @Override
    public JSValue get(JSInt index) {
        return get((int)index.getValue());
    }

    @Override
    public boolean set(int index, JSValue value) {
        long val = value.asInt().getValue();
        if (bytes * index == data.size()) {
            for (int i = 0; i < bytes; i++) {
                data.add((byte) 0);
            }
        }
        for (int i = 0; i < bytes; i++) {
            data.set(bytes * index + i, (byte)(val & 0xFF));
            val >>>= 8;
        }
        return true;
    }

    @Override
    public boolean set(JSInt index, JSValue value) {
        return set((int)index.getValue(), value);
    }

    @Override
    public JSArray slice() {
        TypedArray copy = new TypedArray(bytes * 8);
        for (int i = 0; i < data.size(); i++) {
            copy.data.add(data.get(i));
        }
        return copy;
    }

    @Override
    public JSArray slice(JSInt from, JSInt to) {
        TypedArray copy = new TypedArray(bytes * 8);
        int fromIndex = (int) from.getValue() * bytes;
        int toIndex = (int) Math.min(to.getValue() * bytes, data.size());
        for (int i = fromIndex; i < toIndex; i++) {
            copy.data.add(data.get(i));
        }
        return copy;
    }

    @Override
    public JSArray slice(JSInt from) {
        return slice(from, new JSInt(data.size() / bytes));
    }

    @Override
    public JSArray push(JSValue v) {
        set(data.size() / bytes, v);
        return this;
    }

    @Override
    public JSArray pop() {
        for (int i = 0; i < bytes; i++) {
            data.remove(data.size()-1);
        }
        return this;
    }

    @Override
    public JSArray shift() {
        for (int i = 0; i < bytes; i++) {
            data.remove(0);
        }
        return this;
    }

    @Override
    public JSArray unshift(JSValue value) {
        if (!value.getType().equals("Integer")) {
            return this;
        }
        long val = value.asInt().getValue();
        if (val >= Math.pow(256, bytes)) {
            return this;
        }
        for (int i = 0; i < bytes; i++) {
            data.add(0, (byte)(val & 0xFF));
            val >>>= 8;
        }
        return this;
    }

    @Override
    public JSInt length() {
        return new JSInt(data.size() / bytes);
    }

    @Override
    public JSString asString() {
        return new JSString(toString());
    }

    @Override
    public String toString() {
        //String shortString = type + "[]";
        String result = "";
        for (int i = 0; i < data.size() / bytes; i++) {
            if (i > 0) result += ", ";
            result += get(i).toString();
        }
        return type + "[" + result + "]";
    }

    public String type = "UInt8Array";

    public int bytes = 1;
    public Vector<Byte> data = new Vector<Byte>();
}
