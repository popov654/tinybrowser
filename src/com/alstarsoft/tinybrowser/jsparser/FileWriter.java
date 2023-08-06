package com.alstarsoft.tinybrowser.jsparser;

/**
 *
 * @author Alex
 */
public class FileWriter extends JSObject {

    public FileWriter() {
        items.put("__proto__", FileWriterProto.getInstance());
    }

    @Override
    public String toString() {
        return "FileWriter " + super.toString();
    }
}
