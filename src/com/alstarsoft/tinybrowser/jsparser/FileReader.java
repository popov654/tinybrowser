package com.alstarsoft.tinybrowser.jsparser;

/**
 *
 * @author Alex
 */
public class FileReader extends JSObject {

    public FileReader() {
        items.put("__proto__", FileReaderProto.getInstance());
    }

    @Override
    public String toString() {
        return "FileReader " + super.toString();
    }
}
