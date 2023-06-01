package jsparser;

import bridge.Mapper;
import java.util.LinkedHashMap;
import java.util.Vector;
import network.FormEntry;

/**
 *
 * @author Alex
 */
public class FormData extends JSObject {

    public FormData(HTMLDocument doc) {
        url = Mapper.get(((HTMLElement) doc.items.get("body")).node).document.baseUrl;
        method = "GET";
        encoding = "application/x-www-form-urlencoded";
        charset = "UTF-8";
        initMethods();
    }

    public FormData(HTMLElement el) {
        render.Block b = Mapper.get(el.node);
        if (b.form != null) {
            url = b.form.url;
            method = b.form.method;
            encoding = b.form.multipart ? "multipart/formdata" : "application/x-www-form-urlencoded";
            charset = b.node.hasAttribute("charset") ? b.node.getAttribute("charset") : "UTF-8";
        }
        for (render.Block input: b.form.inputs) {
            if (input.formEntries == null) continue;
            for (FormEntry entry: input.formEntries) {
                if (entry.isFile && entry.textValue.matches("\\[filename=\"[^\"]+\"\\]")) {
                    String filename = entry.textValue.substring(11, entry.textValue.length()-2);
                    params.put(entry.key, new File(new java.io.File(filename)));
                } else {
                    params.put(entry.key, new JSString(entry.textValue));
                }
            }
        }
        initMethods();
    }

    public void initMethods() {
        items.put("add", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() != 2) {
                    JSError e = new JSError(null, "Arguments error: wrong arguments number", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!(args.get(0) instanceof JSString)) {
                    JSError e = new JSError(null, "Type error: argument 1 is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (!(args.get(1) instanceof JSString || args.get(1) instanceof File)) {
                    JSError e = new JSError(null, "Type error: argument 2 is not a string or a file", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                if (args.get(1) instanceof File) {
                    encoding = "multipart/formdata";
                }
                params.put(args.get(0).asString().getValue(), args.get(1));
                return new JSBool(true);
            }
        });

        items.put("remove", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                if (args.size() < 1) {
                    JSError e = new JSError(null, "Arguments error: wrong arguments number", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                for (JSValue arg: args) {
                    if (arg instanceof JSString) {
                        params.remove(((JSString)arg).getValue());
                    }
                }
                
                return new JSBool(true);
            }
        });

        items.put("clear", new Function() {
            @Override
            public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
                params.clear();

                return new JSBool(true);
            }
        });
    }

    public void add(String key, JSValue value) {
        if (!(value instanceof File)) {
            value = value.asString();
        }
        params.put(key, value);
    }

    public void remove(String key) {
        params.remove(key);
    }

    public void clear() {
        params.clear();
    }

    public String url;
    public String method;
    public String encoding;
    public String charset;

    public LinkedHashMap<String, JSValue> params = new LinkedHashMap<String, JSValue>();

}
