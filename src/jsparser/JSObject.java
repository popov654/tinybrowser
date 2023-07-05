package jsparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author Alex
 */
public class JSObject extends JSValue {

    public JSObject() {
        if (!(this instanceof ObjectProto)) {
            items.put("__proto__", ObjectProto.getInstance());
        } else {
            items.put("__proto__", this);
        }
    }

    public JSObject(boolean is_proto) {}

    public JSObject(JSString str, JSValue val) {
        items.put("__proto__", ObjectProto.getInstance());
        items.put(str.getValue(), val);
    }

    public JSObject(Token head, Expression exp) {
        items.put("__proto__", ObjectProto.getInstance());
        if (head.getType() != Token.OBJECT_START) {
            return;
        }
        int level = 1;
        int level2 = 0;
        Token t = head.next;
        String key = "";
        int state = 0;
        while (t != null && level > 0) {
            if (t.getType() == Token.OBJECT_START && state == 1 && level2 == 0) {
                items.put(key, new JSObject(t, exp));
            }
            if (t.getType() == Token.ARRAY_START && state == 1 && level2 == 0) {
                items.put(key, new JSArray(t, exp));
            }
            if (t.getType() == Token.OBJECT_START) level++;
            if (t.getType() == Token.ARRAY_START) level2++;
            if (t.getType() == Token.OBJECT_END) level--;
            if (t.getType() == Token.ARRAY_END) level2--;
            if (t.getType() == Token.OBJECT_END && level == 0) {
                break;
            }
            if (t.getType() == Token.OP && t.getContent().equals(":")) {
                state = 1;
            }
            if (t.getType() == Token.OP && t.getContent().equals(",")) {
                state = 0;
            }
            if (t.getType() == Token.FIELD_NAME && state == 0) {
                key = t.getContent();
                if (t.next.getType() == Token.OP && t.next.getContent().equals(",") || t.next.getType() == Token.OBJECT_END) {
                    JSValue val = Expression.getVar(key, exp);
                    if (val != Undefined.getInstance()) {
                        items.put(key, val);
                    }
                }
            }
            if (t.getType() == Token.OBJECT_ENTITY && t.val instanceof Function && state == 0) {
                items.put(((Function)t.val).getName(), t.val);
            }
            if ((t.getType() == Token.VAR_NAME || t.getType() == Token.OBJECT_ENTITY ||
                    (t.getType() == Token.OP && !t.getContent().equals(":") && !t.getContent().equals(","))
                    || t.getType() == Token.VALUE) && state == 1 && level == 1 && level2 == 0) {
                if (t.getType() == Token.VALUE && (t.next.getType() == Token.OP && t.next.getContent().equals(",") ||
                        t.next.getType() == Token.OBJECT_END)) {
                    items.put(key, JSValue.create(JSValue.getType(t.getContent()), t.getContent()));
                } else if (t.getType() == Token.OBJECT_ENTITY) {
                    t.val.incrementRefCount();
                    if (t.val instanceof Function) {
                        exp.incrementRefCount();
                    }
                    items.put(key, t.val);
                } else {
                    Token ct = t;
                    while (ct != null && (ct.next.getType() != Token.OP || !ct.next.getContent().equals(",")) &&
                        ct.next.getType() != Token.OBJECT_END) {
                        ct = ct.next;
                    }
                    Token tt = null;
                    if (ct != null) {
                        tt = ct.next;
                        ct.next = null;
                    }
                    Expression e = new Expression(t, exp.parent_block);
                    e.silent = true;
                    e.eval();
                    JSValue val = e.getValue();
                    val.incrementRefCount();
                    if (val instanceof Function && exp instanceof Block) {
                        ((Block)exp).incrementRefCount();
                    }
                    items.put(key, (val != null ? val : Undefined.getInstance()));
                    if (ct != null) {
                        ct.next = tt;
                    }
                    t = ct;
                }
                
                key = "";
            }
            t = t.next;
        }
    }

    public JSValue get(JSString str) {
        return get(str.getValue());
    }

    public JSValue get(String str) {
        JSValue val = items.get(str);
        if (val == null && items.containsKey("__proto__") &&
                items.get("__proto__") instanceof JSObject) {
            return ((JSObject)items.get("__proto__")).get(str);
        }
        return val != null ? val : Undefined.getInstance();
    }

    public void set(JSString str, JSValue value) {
        if (!items.containsKey(str.getValue()) && isFrozen) {
            return;
        }
        items.put(str.getValue(), value);
        if (!(str.getValue().startsWith("on") && value instanceof HTMLElement)) {
            value.incrementRefCount();
        }
    }

    public void set(String str, JSValue value) {
        if (!items.containsKey(str) && isFrozen) {
            return;
        }
        items.put(str, value);
        if (!(str.startsWith("on") && this instanceof HTMLElement && value instanceof Function) && !str.equals("constructor")) {
            value.incrementRefCount();
        }
    }

    public HashMap<String, JSValue> getProperties() {
        return (LinkedHashMap<String, JSValue>)items.clone();
    }

    public JSValue getOwnProperty(String str) {
        JSValue val = items.get(str);
        return val != null ? val : Undefined.getInstance();
    }

    public boolean hasOwnProperty(JSString str) {
        return items.containsKey(str.getValue());
    }

    public boolean hasOwnProperty(String str) {
        return items.containsKey(str);
    }

    public JSValue removeProperty(JSString str) {
        return items.remove(str.getValue());
    }

    public JSValue removeProperty(String str) {
        return items.remove(str);
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
        Set keys = items.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (str.equals("__proto__") && !print_proto && !print_protos) continue;
            if (result.length() > 0) result += ", ";
            String value = (print_proto || print_protos) && str.equals("__proto__") ?
                ((this.equals(ObjectProto.getInstance()) || !print_protos && getOwnProperty(str).equals(ObjectProto.getInstance())) ?
                    "ObjectPrototype" :
                    getOwnProperty(str).toString()) :
                getOwnProperty(str).toString();
            result += str + ": " + value;
        }
        return "{" + result + "}";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public JSObject clone() {
        JSObject copy = new JSObject();
        copy.items = (LinkedHashMap<String, JSValue>) items.clone();
        return copy;
    }

    protected LinkedHashMap<String, JSValue> items = new LinkedHashMap<String, JSValue>();
    private String type = "Object";
    public boolean isFrozen = false;
    public boolean print_proto = false;
    public static boolean print_protos = false;
}
