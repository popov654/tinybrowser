package jsparser;

import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class TypedArray extends JSArray {

    public TypedArray() {}

    public TypedArray(int bits, int length) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
        buffer = new ArrayBuffer(length * bytes);
    }

    public TypedArray(int bits, ArrayBuffer buf) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
        buffer = buf;
    }

    public TypedArray(int bits, JSArray array) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
        byte[] data = new byte[array.items.size() * bytes];
        for (int i = 0; i < array.items.size(); i++) {
            long val = array.get(i).asInt().getValue();
            for (int j = 0; j < bytes; j++) {
                data[bytes * i + j] = (byte)(val & 0xFF);
                val >>>= 8;
            }
        }
        buffer = new ArrayBuffer(data);
    }

    @Override
    public JSValue get(int index) {
        long result = 0;
        byte[] data = buffer.data;

        if (bytes * index <= data.length - bytes) {
            for (int i = 0; i < bytes; i++) {
                long comp = (data[bytes * index + i] & 0xFF) << 8 * i;
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
        byte[] data = buffer.data;
        
        for (int i = 0; i < bytes; i++) {
            data[bytes * index + i] = (byte)(val & 0xFF);
            val >>>= 8;
        }
        return true;
    }

    @Override
    public boolean set(JSInt index, JSValue value) {
        return set((int)index.getValue(), value);
    }

    @Override
    public JSValue get(String key) {
        if (key.equals("buffer")) {
            return buffer;
        }
        return super.get(key);
    }

    @Override
    public JSValue get(JSString str) {
        return get(str.getValue());
    }

    @Override
    public JSArray slice() {
        return new TypedArray(bytes * 8, buffer);
    }

    @Override
    public JSArray slice(JSInt from, JSInt to) {
        int fromIndex = (int) from.getValue() * bytes;
        int toIndex = (int) Math.min(to.getValue() * bytes, buffer.data.length);
        byte[] copyBytes = Arrays.copyOfRange(buffer.data, fromIndex, toIndex);

        return new TypedArray(bytes * 8, new ArrayBuffer(copyBytes));
    }

    @Override
    public JSArray slice(JSInt from) {
        int fromIndex = (int) from.getValue() * bytes;
        int toIndex = buffer.data.length;
        byte[] copyBytes = Arrays.copyOfRange(buffer.data, fromIndex, toIndex);

        return new TypedArray(bytes * 8, new ArrayBuffer(copyBytes));
    }

    @Override
    public JSArray push(JSValue v) {
        return this;
    }

    @Override
    public JSArray pop() {
        return this;
    }

    @Override
    public JSArray shift() {
        return this;
    }

    @Override
    public JSArray unshift(JSValue value) {
        return this;
    }

    @Override
    public JSInt length() {
        return new JSInt(buffer.data.length / bytes);
    }

    @Override
    public JSString asString() {
        return new JSString(toString());
    }

    @Override
    public String toString() {
        //String shortString = type + "[]";
        String result = "";
        byte[] data = buffer.data;
        for (int i = 0; i < data.length / bytes; i++) {
            if (i > 0) result += ", ";
            result += get(i).toString();
        }
        return type + "[" + result + "]";
    }

    public String type = "UInt8Array";

    public int bytes = 1;
    public ArrayBuffer buffer;
}
