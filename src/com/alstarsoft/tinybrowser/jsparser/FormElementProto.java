package com.alstarsoft.tinybrowser.jsparser;

import com.alstarsoft.tinybrowser.bridge.Mapper;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FormElementProto extends JSObject {

    class submitFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            HTMLElement element = (HTMLElement) context;
            if (Mapper.get(element.node).form != null) {
                Mapper.get(element.node).form.submit();
            }
            return Undefined.getInstance();
        }
    }

    class resetFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            HTMLElement element = (HTMLElement) context;
            if (Mapper.get(element.node).form != null) {
                Mapper.get(element.node).form.reset();
            }
            return Undefined.getInstance();
        }
    }

    private FormElementProto() {
        items.put("__proto__", HTMLElementProto.getInstance());

        items.put("submit", new submitFunction());
        items.put("reset", new resetFunction());
    }

    public static FormElementProto getInstance() {
        if (instance == null) {
            instance = new FormElementProto();
        }
        return instance;
    }

    private String type = "Object";
    private static FormElementProto instance = null;
}
