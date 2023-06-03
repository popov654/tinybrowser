package jsparser;

import java.util.Vector;
import network.Request;

/**
 *
 * @author Alex
 */
public class File extends JSObject {

    public File(java.io.File f) {
        file = f;
        items.put("__proto__", FileProto.getInstance());
        if (file != null) {
            items.put("name", new JSString(file.getName()));
            items.put("type", new JSString(Request.getMimeType(file.getName())));
            items.put("size", new JSInt(file.length()));
            items.put("lastModified", new JSInt(file.lastModified()));
            items.put("lastModifiedDate", new JSDate(file.lastModified()));
        }
    }

    public File(JSBlob blob) {
        items.put("__proto__", FileProto.getInstance());
        this.blob = blob;
        this.mimeType = "application/octet-stream";
        this.name = "blob";
    }

    public File(JSBlob blob, String name) {
        items.put("__proto__", FileProto.getInstance());
        this.blob = blob;
        this.mimeType = "application/octet-stream";
        this.name = name;
    }

    public File(JSBlob blob, String name, String type) {
        items.put("__proto__", FileProto.getInstance());
        this.blob = blob;
        this.mimeType = type;
        this.name = name;
    }

    @Override
    public JSValue get(String key) {
        if (key.equals("name") && file == null) {
            return new JSString(name);
        }
        if (key.equals("type") && file == null) {
            return new JSString(mimeType);
        }
        return super.get(key);
    }

    @Override
    public String toString() {
        return "File " + super.toString();
    }

    public java.io.File file;
    public JSBlob blob = null;
    public String mimeType;
    public String name;
}
