package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileReaderC extends Function {

    public FileReaderC() {
        items.put("prototype", FileReaderProto.getInstance());
        FileReaderProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        return new FileReader();
    }

}

