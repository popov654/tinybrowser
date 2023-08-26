package com.alstarsoft.tinybrowser.jsparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSObject extends JSValue {

    public JSObject() {
        if (!(this instanceof JSObjectProto)) {
            items.put("__proto__", JSObjectProto.getInstance());
        } else {
            items.put("__proto__", this);
        }
    }

    public JSObject(boolean is_proto) {}

    public JSObject(JSString str, JSValue val) {
        items.put("__proto__", JSObjectProto.getInstance());
        items.put(str.getValue(), val);
    }

    public JSObject(Token head, Expression exp, TokenPointer pointer) {
        items.put("__proto__", JSObjectProto.getInstance());
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
                TokenPointer p = new TokenPointer(t);
                items.put(key, new JSObject(t, exp, p));
                t = p.token;
            }
            if (t.getType() == Token.ARRAY_START && state == 1 && level2 == 0) {
                TokenPointer p = new TokenPointer(t);
                items.put(key, new JSArray(t, exp, p));
                t = p.token;
            }
            if (t.getType() == Token.OBJECT_START) level++;
            if (t.getType() == Token.ARRAY_START) level2++;
            if (t.getType() == Token.OBJECT_END) level--;
            if (t.getType() == Token.ARRAY_END) level2--;
            if (t.getType() == Token.OBJECT_END && level == 0) {
                if (pointer != null) {
                    pointer.token = t.next;
                }
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
        if (val == null) {
            val = getCustomPropertyValue(str);
        }
        if (val == null && items.containsKey("__proto__") &&
                items.get("__proto__") instanceof JSObject) {
            return ((JSObject)items.get("__proto__")).get(str);
        }
        return val != null ? val : Undefined.getInstance();
    }

    public void set(JSString str, JSValue value) {
        set(str.getValue(), value);
    }

    public void set(String str, JSValue value) {
        if (!items.containsKey(str) && isFrozen) {
            return;
        }
        if (customProperties.containsKey(str)) {
            setCustomPropertyValue(str, value);
            if (customProperties.get(str).value == value) {
                value.incrementRefCount();
            }
            return;
        }
        items.put(str, value);
        if (!(str.startsWith("on") && this instanceof HTMLElement && value instanceof Function) && !str.equals("constructor")) {
            value.incrementRefCount();
        }
    }


    public void defineCustomProperty(String key, JSObject descriptor) {
        customProperties.put(key, new CustomProperty(descriptor));
    }

    public JSValue getCustomPropertyValue(String key) {
        if (customProperties.containsKey(key)) {
            CustomProperty p = customProperties.get(key);
            if (p.get != null) {
                return p.get.call(new Vector<JSValue>());
            }
            return p.value;
        }
        return null;
    }

    public void setCustomPropertyValue(String key, JSValue value) {
        if (customProperties.containsKey(key)) {
            CustomProperty p = customProperties.get(key);
            if (!p.writable) return;
            if (p.set != null) {
                Vector<JSValue> args = new Vector<JSValue>();
                args.add(value);
                p.set.call(args);
                return;
            }
            p.value = value;
        }
    }

    public void removeCustomProperty(String key) {
        CustomProperty p = customProperties.get(key);
        if (p != null && p.configurable) {
            customProperties.remove(key);
        }
    }

    public HashMap<String, JSValue> getProperties() {
        LinkedHashMap<String, JSValue> props = (LinkedHashMap<String, JSValue>) items.clone();
        LinkedHashMap<String, JSValue> customProps = new LinkedHashMap<String, JSValue>();
        Set<String> keys = customProperties.keySet();
        for (String key: keys) {
            CustomProperty p = customProperties.get(key);
            if (!p.enumerable) continue;
            customProps.put(key, getCustomPropertyValue(key));
        }
        props.putAll(customProps);
        return props;
    }

    public JSValue getOwnProperty(String key) {
        JSValue val = items.get(key);
        if (val == null) {
            val = getCustomPropertyValue(key);
        }
        return val != null ? val : Undefined.getInstance();
    }

    public boolean hasOwnProperty(JSString str) {
        return hasOwnProperty(str.getValue());
    }

    public boolean hasOwnProperty(String str) {
        return items.containsKey(str) || customProperties.containsKey(str);
    }

    public void removeProperty(JSString str) {
        removeProperty(str.getValue());
    }

    public void removeProperty(String str) {
        removeCustomProperty(str);
        items.remove(str);
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
        LinkedHashSet keys = new LinkedHashSet(items.keySet());
        keys.addAll(customProperties.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (str.equals("__proto__") && !print_proto && !print_protos) continue;
            if (result.length() > 0) result += ", ";
            String value = (print_proto || print_protos) && str.equals("__proto__") ?
                ((this.equals(JSObjectProto.getInstance()) || !print_protos && getOwnProperty(str).equals(JSObjectProto.getInstance())) ?
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

    public JSObject deepClone() {
        JSObject copy = new JSObject();
        copy.items = new LinkedHashMap<String, JSValue>();
        Set<String> keys = items.keySet();
        for (String key: keys) {
            JSValue val = items.get(key);
            if (key.matches("__proto__|prototype|constructor")) {
                copy.items.put(key, val);
            } else if (val.getType().matches("Object|Array")) {
                copy.items.put(key, ((JSObject)val).deepClone());
            } else {
                copy.items.put(key, JSValue.create(val.getType(), val.toString()));
            }
        }
        return copy;
    }

    protected LinkedHashMap<String, JSValue> items = new LinkedHashMap<String, JSValue>();
    protected LinkedHashMap<String, CustomProperty> customProperties = new LinkedHashMap<String, CustomProperty>();
    private String type = "Object";
    public boolean isFrozen = false;
    public boolean print_proto = false;
    public static boolean print_protos = false;
}
