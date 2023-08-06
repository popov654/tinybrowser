package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FileProto extends JSObject {

    private FileProto() {
        items.put("__proto__", JSBlobProto.getInstance());
    }

    public static FileProto getInstance() {
        if (instance == null) {
            instance = new FileProto();
        }
        return instance;
    }

    private String type = "Object";
    private static FileProto instance = null;
}
