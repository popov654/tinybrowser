package jsparser;

import network.Blob;
import network.Request;

/**
 *
 * @author Alex
 */
public class File extends JSBlob {

    public File(java.io.File f) {
        super(new Blob(f));
        if (file != null) {
            file = f;
            items.put("name", new JSString(file.getName()));
            items.put("type", new JSString(Request.getMimeType(file.getName())));
            items.put("size", new JSInt(file.length()));
            items.put("lastModified", new JSInt(file.lastModified()));
            items.put("lastModifiedDate", new JSDate(file.lastModified()));
            mimeType = Request.getMimeType(file.getName());
        } else {
            mimeType = "application/octet-stream";
        }
    }

    public File(JSBlob blob) {
        super(blob.getBlob());
        items.put("__proto__", FileProto.getInstance());
        this.mimeType = "application/octet-stream";
        this.name = "blob";
    }

    public File(JSBlob blob, String name) {
        super(blob.getBlob());
        items.put("__proto__", FileProto.getInstance());
        this.mimeType = "application/octet-stream";
        this.name = name;
    }

    public File(JSBlob blob, String name, String type) {
        super(blob.getBlob());
        items.put("__proto__", FileProto.getInstance());
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
    public String mimeType;
    public String name;
}
