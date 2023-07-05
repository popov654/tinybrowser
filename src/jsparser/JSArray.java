package jsparser;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSArray extends JSObject {

    public JSArray() {
        _items = super.items;
        _items.put("__proto__", ArrayProto.getInstance());
    }

    public JSArray(JSValue val) {
        _items = super.items;
        _items.put("__proto__", ArrayProto.getInstance());
        items.add(val);
    }

    public JSArray(Vector<JSValue> val) {
        _items = super.items;
        _items.put("__proto__", ArrayProto.getInstance());
        for (int i = 0; i < val.size(); i++) {
            items.add(val.get(i));
        }
    }

    public JSArray(Token head, Expression exp) {
        _items = super.items;
        _items.put("__proto__", ArrayProto.getInstance());
        if (head.getType() != Token.ARRAY_START) {
            return;
        }
        int level = 1;
        int level2 = 0;
        Token t = head.next;
        while (t != null && level > 0) {
            if (t.getType() == Token.ARRAY_START && level2 == 0) {
                items.add(new JSArray(t, exp));
            }
            if (t.getType() == Token.OBJECT_START && level2 == 0) {
                items.add(new JSObject(t, exp));
            }
            if (t.getType() == Token.ARRAY_START) level++;
            if (t.getType() == Token.OBJECT_START) level2++;
            if (t.getType() == Token.ARRAY_END) level--;
            if (t.getType() == Token.OBJECT_END) level2--;
            if (t.getType() == Token.ARRAY_END && level == 0) {
                break;
            }
            if ((t.getType() == Token.VAR_NAME ||
                    (t.getType() == Token.OP && !t.getContent().equals(","))
                    || t.getType() == Token.VALUE) && level == 1 && level2 == 0) {
                if (t.getType() == Token.VALUE && (t.next.getType() == Token.OP && t.next.getContent().equals(",") ||
                        t.next.getType() == Token.ARRAY_END)) {
                    items.add(JSValue.create(JSValue.getType(t.getContent()), t.getContent()));
                } else {
                    Token ct = t;
                    while (ct != null && (ct.next.getType() != Token.OP || !ct.next.getContent().equals(",")) &&
                        ct.next.getType() != Token.ARRAY_END) {
                        ct = ct.next;
                    }
                    Token tt = null;
                    if (ct != null) {
                        tt = ct.next;
                        ct.next = null;
                    }
                    Expression e = new Expression(t, exp.parent_block);
                    t = ct;
                    e.silent = true;
                    e.eval();
                    JSValue val = e.getValue();
                    items.add(val != null ? val : Undefined.getInstance());
                    if (ct != null) {
                        ct.next = tt;
                    }
                    t = ct;
                }
            }
            t = t.next;
        }
    }

    public JSValue get(JSInt index) {
        try {
            return items.get((int)index.getValue());
        } catch (ArrayIndexOutOfBoundsException ex) {
            return Undefined.getInstance();
        }
    }

    public JSValue get(int index) {
        try {
            return items.get(index);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return Undefined.getInstance();
        }
    }

    public boolean set(JSInt index, JSValue value) {
        items.set((int)index.getValue(), value);
        return true;
    }

    public boolean set(int index, JSValue value) {
        items.set(index, value);
        return true;
    }

    @Override
    public JSValue get(JSString str) {
        if (str.getValue().equals("length")) {
            return new JSInt(items.size());
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public JSValue get(String str) {
        if (str.equals("length")) {
            return new JSInt(items.size());
        }
        if (str.equals("__proto__")) {
            return _items.get("__proto__");
        }
        return ((JSObject)_items.get("__proto__")).get(str);
    }

    @Override
    public void set(JSString str, JSValue value) {}

    @Override
    public void set(String str, JSValue value) {}

    public JSInt length() {
        return new JSInt(items.size());
    }

    public JSArray push(JSValue v) {
        if (isFrozen) {
            return this;
        }
        items.add(v);
        v.incrementRefCount();
        return this;
    }

    public JSArray pop() {
        items.lastElement().decrementRefCount();
        items.remove(items.size()-1);
        return this;
    }

    public JSArray shift() {
        items.firstElement().decrementRefCount();
        items.remove(0);
        return this;
    }

    public JSArray unshift(JSValue v) {
        if (isFrozen) {
            return this;
        }
        items.insertElementAt(v, 0);
        v.incrementRefCount();
        return this;
    }

    public JSArray concat(JSArray a) {
        JSArray b = new JSArray(items);
        for (int i = 0; i < a.length().getValue(); i++) {
            b.push(a.get(i));
        }
        return b;
    }

    public JSArray slice(JSInt from, JSInt to) {
        Vector<JSValue> items2 = new Vector<JSValue>(items.subList((int)from.getValue(), (int)to.getValue()));
        return new JSArray(items2);
    }

    public JSArray slice(JSInt from) {
        return slice(from, new JSInt(items.size()));
    }

    public JSArray slice() {
        return new JSArray((Vector<JSValue>)items.clone());
    }

    public JSArray splice(JSInt from, JSInt count, Vector<JSValue> ins) {
        if (isFrozen) {
            return this;
        }
        Vector<JSValue> n = new Vector<JSValue>();
        for (int i = 0; i < count.getValue(); i++) {
            n.add(items.get((int)from.getValue()));
            items.remove((int)from.getValue());
        }
        for (int i = 0; i < ins.size(); i++) {
            items.insertElementAt(ins.get(i), (int)from.getValue());
        }
        return new JSArray(n);
    }

    public JSArray insert(int index, JSValue value) {
        if (isFrozen) {
            return this;
        }
        items.insertElementAt(value, index);
        return this;
    }

    public JSArray sort() {
        JSValue[] items2 = new JSValue[items.size()];
        items.toArray(items2);
        Arrays.sort(items2);
        Vector v = new Vector<JSValue>();
        for (int i = 0; i < items2.length; i++) {
            v.add(items2[i]);
        }
        return new JSArray(v);
    }

    public JSString join(JSString str) {
        String s = "";
        if (items.size() == 0) return new JSString(s);
        s += items.get(0).toString();
        for (int i = 1; i < items.size(); i++) {
            s += str.getValue();
            s += items.get(i).toString();
        }
        return new JSString(s);
    }

    public JSString join() {
        return join(new JSString(","));
    }

    public Vector<JSValue> getItems() {
        return (Vector<JSValue>)items.clone();
    }

    @Override
    public JSString asString() {
        return new JSString(toString());
    }

    @Override
    public JSInt asInt() {
        return null;
    }

    @Override
    public JSFloat asFloat() {
        return null;
    }

    @Override
    public JSBool asBool() {
        return new JSBool(true);
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) result += ", ";
            result += items.get(i).toString();
        }
        return "[" + result + "]";
    }

    @Override
    public JSArray clone() {
        return new JSArray(items);
    }

    @Override
    public String getType() {
        return type;
    }

    protected Vector<JSValue> items = new Vector<JSValue>();
    protected LinkedHashMap<String, JSValue> _items = new LinkedHashMap<String, JSValue>();
    private String type = "Array";

}
