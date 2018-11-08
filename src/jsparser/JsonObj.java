package jsparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JsonObj extends JSObject {
    
    class stringifyFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return Undefined.getInstance();
            JSValue val = args.get(0);
            if (val.getType().matches("Integer|Float|String")) {
                return new JSString(val.toString());
            }
            String result = "";
            if (val.getType().equals("Array")) {
                Vector<JSValue> items = ((JSArray)val).getItems();
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) result += ",";
                    Vector<JSValue> v = new Vector<JSValue>();
                    v.add(items.get(i));
                    result += ((JSString)call(context, v, false)).getValue();
                }
                return new JSString("[" + result + "]");
            }
            if (val.getType().equals("Object")) {
                HashMap<String, JSValue> items = ((JSObject)val).getProperties();
                Set keys = items.keySet();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    String str = (String)it.next();
                    if (str.matches("__proto__|constructor")) continue;
                    if (result.length() > 0) result += ",";
                    Vector<JSValue> v = new Vector<JSValue>();
                    v.add(items.get(str));
                    result += "\"" + str + "\":" + ((JSString)call(context, v, false)).getValue();
                }
                return new JSString("{" + result + "}");
            }
            return Undefined.getInstance();
        }
    }

    class parseFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return Undefined.getInstance();
            JSParser jp = new JSParser(args.get(0).asString().getValue());
            Expression exp = new Expression(jp.getHead(), getCaller());
            JSObject obj = createObject(jp.getHead(), exp, getCaller());
            return obj != null ? obj : Undefined.getInstance();
        }

        private JSObject createObject(Token head, Expression exp, Block b) {
            int level = 1;
            int level2 = 0;
            Token t = head.next;
            String key = "";
            JSObject obj = new JSObject();
            int state = 0;
            while (t != null && level > 0) {
                if (t.getType() == Token.OBJECT_START && state == 1) {
                    level++;
                    obj.set(key, createObject(t, exp, b));
                }
                if (t.getType() == Token.ARRAY_START && state == 1) {
                    level2++;
                    obj.set(key, createArray(t, exp, b));
                }
                if (t.getType() == Token.OBJECT_END) level--;
                if (t.getType() == Token.ARRAY_END) level2--;
                if (t.getType() == Token.OP && t.getContent().equals(":")) {
                    state = 1;
                }
                if (t.getType() == Token.OP && t.getContent().equals(",")) {
                    state = 0;
                }
                if (t.getType() == Token.FIELD_NAME && state == 0) {
                    key = t.getContent();
                }
                if ((t.getType() == Token.VALUE ||
                        (t.getType() == Token.OP && !t.getContent().equals(":") && !t.getContent().equals(",")))
                        && state == 1 && level == 1 && level2 == 0) {
                    if (t.getType() == Token.VALUE && (t.next.getType() == Token.OP && t.next.getContent().equals(",") ||
                            t.next.getType() == Token.OBJECT_END)) {
                        obj.set(key, JSValue.create(JSValue.getType(t.getContent()), t.getContent()));
                    } else {
                        b.error = new JSError(null, "Object parse error", b.getStack());
                        return null;
                    }

                    key = "";
                }
                t = t.next;
            }
            return obj;
        }

        private JSArray createArray(Token head, Expression exp, Block b) {
            int level = 1;
            int level2 = 0;
            Token t = head.next;
            JSArray a = new JSArray();
            while (t != null && level > 0) {
                if (t.getType() == Token.ARRAY_START) {
                    level++;
                    a.push(createArray(t, exp, b));
                }
                if (t.getType() == Token.OBJECT_START) {
                    level2++;
                    a.push(createObject(t, exp, b));
                }
                if (t.getType() == Token.ARRAY_END) level--;
                if (t.getType() == Token.OBJECT_END) level2--;
                if ((t.getType() == Token.VALUE ||
                        (t.getType() == Token.OP && !t.getContent().equals(","))) && level == 1 && level2 == 0) {
                    if (t.getType() == Token.VALUE && (t.next.getType() == Token.OP && t.next.getContent().equals(",") ||
                            t.next.getType() == Token.ARRAY_END)) {
                        a.push(JSValue.create(JSValue.getType(t.getContent()), t.getContent()));
                    } else {
                        b.error = new JSError(null, "Object parse error", b.getStack());
                        return null;
                    }
                }
                t = t.next;
            }
            return a;
        }
    }

    private JsonObj() {
        items.put("stringify", new stringifyFunction());
        items.put("parse", new parseFunction());
    }

    public static JsonObj getInstance() {
        if (instance == null) {
            instance = new JsonObj();
        }
        return instance;
    }

    private String type = "Object";
    private static JsonObj instance = null;
}
