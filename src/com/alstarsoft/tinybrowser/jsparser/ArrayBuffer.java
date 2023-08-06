package com.alstarsoft.tinybrowser.jsparser;

import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class ArrayBuffer extends JSArray {

    public ArrayBuffer() {}

    public ArrayBuffer(int length) {
        data = new byte[length];
        end = length;
    }

    public ArrayBuffer(byte[] byteData) {
        data = byteData;
        end = data.length;
    }

    public ArrayBuffer(byte[] byteData, int from, int to) {
        data = byteData;
        start = from;
        end = Math.min(to, data.length);
    }

    @Override
    public JSValue get(JSString str) {
        if (str.getValue().equals("length")) {
            return new JSInt(end - start);
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(end - start);
        }
        if (str.equals("__proto__")) {
            return _items.get("__proto__");
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(int index) {
        if (index >= 0 && index < end - start) {
            return new JSInt(data[start + index]);
        }

        return Undefined.getInstance();
    }

    @Override
    public JSValue get(JSInt index) {
        return get((int)index.getValue());
    }

    @Override
    public boolean set(int index, JSValue value) {
        long val = value.asInt().getValue();
        if (index < 0 || index >= end) {
            return false;
        }
        data[start + index] = (byte)(val & 0xFF);
        return true;
    }

    @Override
    public boolean set(JSInt index, JSValue value) {
        return set((int)index.getValue(), value);
    }

    @Override
    public JSInt length() {
        return new JSInt(data != null ? end - start : 0);
    }

    @Override
    public JSString asString() {
        return new JSString(toString());
    }

    @Override
    public String toString() {
        return "[object ArrayBuffer]";
    }

    @Override
    public ArrayBuffer clone() {
        return new ArrayBuffer(Arrays.copyOf(data, data.length), start, end);
    }

    public int start = 0;
    public int end = 0;

    public byte[] data;
}
