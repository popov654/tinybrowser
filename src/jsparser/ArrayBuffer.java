package jsparser;

/**
 *
 * @author Alex
 */
public class ArrayBuffer extends JSArray {

    public ArrayBuffer() {}

    public ArrayBuffer(int length) {
        data = new byte[length];
    }

    public ArrayBuffer(byte[] byteData) {
        data = byteData;
    }

    @Override
    public JSValue get(JSString str) {
        if (str.getValue().equals("length")) {
            return new JSInt(data.length);
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(data.length);
        }
        if (str.equals("__proto__")) {
            return _items.get("__proto__");
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(int index) {
        if (index >= 0 && index < data.length) {
            return new JSInt(data[index]);
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
        if (index < 0 || index >= data.length) {
            return false;
        }
        data[index] = (byte)(val & 0xFF);
        return true;
    }

    @Override
    public boolean set(JSInt index, JSValue value) {
        return set((int)index.getValue(), value);
    }

    @Override
    public JSInt length() {
        return new JSInt(data != null ? data.length : 0);
    }

    @Override
    public JSString asString() {
        return new JSString(toString());
    }

    @Override
    public String toString() {
        return "[object ArrayBuffer]";
    }

    public byte[] data;
}
