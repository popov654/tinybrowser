package network;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import render.Util;

/**
 *
 * @author Alex
 */
public class FormEntry implements Entry {

    public FormEntry(String name, File file) {
        key = name;
        isFile = true;
        textValue = "[filename=\"" + file.getAbsolutePath() + "\"]";
        filename = file.getName();
    }

    public FormEntry(String name, File file, boolean read) {
        key = name;
        if (!read) {
            textValue = "[filename=\"" + file.getAbsolutePath() + "\"]";
        }
        try {
            textValue = "[filename=\"" + file.getAbsolutePath() + "\"]";
            binaryValue = Util.readFile(file);
        } catch (IOException ex) {
            Logger.getLogger(FormEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = file.getName();
    }

    public FormEntry(String name, jsparser.File file) {
        key = name;
        isFile = true;
        textValue = "[filename=\"" + file.get("name").asString().getValue() + "\"]";
        filename = file.get("name").asString().getValue();
        blob = file.blob.getBlob();
    }

    public FormEntry(String name, byte[] value) {
        key = name;
        isBinary = true;
        binaryValue = value;
    }

    public FormEntry(String name, byte[] value, String fileName) {
        key = name;
        isBinary = true;
        binaryValue = value;
        isFile = true;
        filename = fileName;
    }

    public FormEntry(String name, String value) {
        key = name;
        isBinary = false;
        textValue = value;
        if (value.matches("\\[filename=\"[^\"]+\"\\]")) {
            isFile = true;
            String[] path = value.substring(11, value.length()-2).split(File.separator.replace("\\", "\\\\"));
            filename = path[path.length-1];
        }
    }

    public void setTextValue(String value) {
        isBinary = false;
        binaryValue = null;
        textValue = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setBlobValue(byte[] value) {
        isBinary = true;
        binaryValue = value;
        textValue = null;
    }

    public void setBlobValue(byte[] value, String fileName) {
        isBinary = true;
        binaryValue = value;
        textValue = null;
        isFile = true;
        filename = fileName;
    }

    public void setBlobValue(File file) {
        try {
            textValue = null;
            isBinary = true;
            binaryValue = Util.readFile(file);
            filename = file.getName();
        } catch (IOException ex) {
            Logger.getLogger(FormEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFileValue(String path) {
        textValue = "[filename=\"" + path + "\"]";
        String[] p = path.split(File.separator.replace("\\", "\\\\"));
        filename = p[p.length-1];
        isFile = true;
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String getValue() {
        return !isBinary ? textValue : null;
    }

    @Override
    public Object setValue(Object value) {
        if (value instanceof byte[]) {
            setBlobValue((byte[]) value);
        } else if (value instanceof File) {
            setBlobValue((File) value);
        } else if (value instanceof String) {
            setTextValue((String) value);
        }

        return value;
    }

    public Object setValue(Object value, String fileName) {
        if (value instanceof byte[]) {
            setBlobValue((byte[]) value);
        } else if (value instanceof File) {
            setBlobValue((File) value);
        } else if (value instanceof String) {
            setTextValue((String) value);
        }
        filename = fileName;

        return value;
    }

    public String key;
    public boolean isBinary = false;
    public String textValue;
    public String filename;
    public Blob blob;
    public byte[] binaryValue;
    public boolean isFile = false;
}
