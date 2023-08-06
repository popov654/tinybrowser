package com.alstarsoft.tinybrowser.jsparser;

import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class TypedArray extends JSArray {

    public TypedArray() {
        _items.put("__proto__", TypedArrayProto.getInstance());
    }

    public TypedArray(int bits, int length) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
        buffer = new ArrayBuffer(length * bytes);
        _items.put("__proto__", TypedArrayProto.getInstance());
    }

    public TypedArray(int bits, ArrayBuffer buf) {
        bytes = bits / 8;
        type = "UInt" + bits + "Array";
        buffer = buf;
        _items.put("__proto__", TypedArrayProto.getInstance());
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
        _items.put("__proto__", TypedArrayProto.getInstance());
    }

    public void setIsFloat() {
        isFloat = true;
        type = "Float64Array";
        bytes = 8;
    }

    public void setIsClamped() {
        isClamped = true;
        type = type.replaceAll("(?<=\\d+)Array", "ClampedArray");
    }

    @Override
    public JSValue get(int index) {
        long result = 0;
        byte[] data = buffer.data;

        if (buffer.start + bytes * index <= buffer.end - bytes) {
            for (int i = 0; i < bytes; i++) {
                int sh1 = (8 * i) & 0x0F;
                int sh2 = ((8 * i) & 0xF0) >>> 4;
                int res = (data[buffer.start + bytes * index + i] & 0xFF);
                long comp = 0;
                if (sh2 > 0) {
                    comp = res;
                    for (int j = 0; j < sh2; j++) {
                        comp = comp << 0x10;
                    }
                    comp <<= sh1;
                } else {
                    comp = res << sh1;
                }
                result = result | comp;
            }
        }
        if (isFloat) {
            return new JSFloat(Double.longBitsToDouble(result));
        }

        return new JSInt(result);
    }

    @Override
    public JSValue get(JSInt index) {
        return get((int)index.getValue());
    }

    @Override
    public boolean set(int index, JSValue value) {
        long val = 0;
        if (isFloat) {
            val = Double.doubleToLongBits(value.asFloat().getValue());
        } else {
            if (isClamped) {
                val = Math.max(0, Math.min((long) Math.pow(2, bytes * 8) - 1, value.asInt().getValue()));
            } else {
                val = value.asInt().getValue();
            }
        }
        byte[] data = buffer.data;

        if (bytes > 4) {
            for (int i = 0; i < bytes; i++) {
                data[buffer.start + bytes * (index+1) - 1 - i] = (byte)(val >>> 56);
                val <<= 8;
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                data[buffer.start + bytes * index + i] = (byte)(val & 0xFF);
                val >>>= 8;
            }
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
        if (key.equals("byteLength")) {
            return new JSInt(buffer.end - buffer.start);
        }
        if (key.equals("BYTES_PER_ELEMENT")) {
            return new JSInt(bytes);
        }
        return super.get(key);
    }

    @Override
    public JSValue get(JSString str) {
        return get(str.getValue());
    }

    @Override
    public JSArray slice() {
        return new TypedArray(bytes * 8, buffer.clone());
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

    public JSArray subArray(JSInt from, JSInt to) {
        int fromIndex = (int) from.getValue() * bytes;
        int toIndex = (int) Math.min(to.getValue() * bytes, buffer.data.length);

        return new TypedArray(bytes * 8, new ArrayBuffer(buffer.data, fromIndex, toIndex));
    }

    public JSArray subArray(JSInt from) {
        int fromIndex = (int) from.getValue() * bytes;
        int toIndex = buffer.end;
        
        return new TypedArray(bytes * 8, new ArrayBuffer(buffer.data, fromIndex, toIndex));
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
        for (int i = 0; i < (buffer.end - buffer.start) / bytes; i++) {
            if (i > 0) result += ", ";
            result += get(i).toString();
        }
        return type + "[" + result + "]";
    }

    public String type = "UInt8Array";

    public boolean isFloat = false;
    public boolean isClamped = false;

    public int bytes = 1;
    public ArrayBuffer buffer;
}
