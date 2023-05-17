package jsparser;

import network.Request;

/**
 *
 * @author Alex
 */
public class File extends JSObject {

    public File(java.io.File f) {
        file = f;
        if (file != null) {
            items.put("name", new JSString(file.getName()));
            items.put("type", new JSString(Request.getMimeType(file.getName())));
            items.put("size", new JSInt(file.length()));
            items.put("lastModified", new JSInt(file.lastModified()));
            items.put("lastModifiedDate", new JSDate(file.lastModified()));
        }
    }

    @Override
    public String toString() {
        return "File " + super.toString();
    }

    public java.io.File file;
}
