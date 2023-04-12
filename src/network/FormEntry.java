package network;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import render.Util;

/**
 *
 * @author Alex
 */
public class FormEntry {

    public FormEntry(String name, File file) {
        key = name;
        isFile = true;
        textValue = "[filename=\"" + file.getAbsolutePath() + "\"]";
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
    }

    public FormEntry(String name, byte[] value) {
        key = name;
        isBinary = true;
        binaryValue = value;
    }

    public FormEntry(String name, String value) {
        key = name;
        isBinary = false;
        textValue = value;
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

    public void setBlobValue(File file) {
        try {
            textValue = null;
            isBinary = true;
            binaryValue = Util.readFile(file);
        } catch (IOException ex) {
            Logger.getLogger(FormEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFileValue(String path) {
        textValue = "[filename=\"" + path + "\"]";
        isFile = true;
    }

    public String key;
    public boolean isBinary = false;
    public String textValue;
    public byte[] binaryValue;
    public boolean isFile = false;
}
