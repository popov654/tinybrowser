package com.alstarsoft.tinybrowser.jsparser;

/**
 *
 * @author Alex
 */
public class DataView extends JSObject {

    public DataView(ArrayBuffer buf) {
        buffer = buf;
        items.put("__proto__", DataViewProto.getInstance());
    }

    public DataView(JSArray array) {
        int bytes = 4;
        byte[] data = new byte[array.items.size() * bytes];
        for (int i = 0; i < array.items.size(); i++) {
            long val = array.get(i).asInt().getValue();
            for (int j = 0; j < bytes; j++) {
                data[bytes * i + j] = (byte)(val & 0xFF);
                val >>>= 8;
            }
        }
        buffer = new ArrayBuffer(data);
        items.put("__proto__", DataViewProto.getInstance());
    }

    public JSInt getUint8(int index) {
        TypedArray t = new TypedArray(8, buffer);
        buffer.start = index;
        JSInt result = t.get(0).asInt();
        buffer.start = 0;
        return result;
    }

    public void setUint8(int index, JSInt value) {
        TypedArray t = new TypedArray(8, buffer);
        buffer.start = index;
        t.set(0, value);
        buffer.start = 0;
    }

    public JSInt getUint16(int index) {
        TypedArray t = new TypedArray(16, buffer);
        buffer.start = index;
        JSInt result = t.get(0).asInt();
        buffer.start = 0;
        return result;
    }

    public void setUint16(int index, JSInt value) {
        TypedArray t = new TypedArray(16, buffer);
        buffer.start = index;
        t.set(0, value);
        buffer.start = 0;
    }

    public JSInt getUint32(int index) {
        TypedArray t = new TypedArray(32, buffer);
        buffer.start = index;
        JSInt result = t.get(0).asInt();
        buffer.start = 0;
        return result;
    }

    public void setUint32(int index, JSInt value) {
        TypedArray t = new TypedArray(32, buffer);
        buffer.start = index;
        t.set(0, value);
        buffer.start = 0;
    }

    public JSFloat getFloat64(int index) {
        TypedArray t = new TypedArray(64, buffer);
        t.setIsFloat();
        buffer.start = index;
        JSFloat result = t.get(0).asFloat();
        buffer.start = 0;
        return result;
    }

    public void setFloat64(int index, JSFloat value) {
        TypedArray t = new TypedArray(64, buffer);
        t.setIsFloat();
        buffer.start = index;
        t.set(0, value);
        buffer.start = 0;
    }

    @Override
    public String toString() {
        return "[object DataView]";
    }

    public ArrayBuffer buffer;
}
