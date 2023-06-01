package jsparser;

import network.Blob;

/**
 *
 * @author Alex
 */
public class JSBlob extends JSObject {

    public JSBlob(Blob b) {
        items.put("__proto__", JSBlobProto.getInstance());
        blob = b;
        items.put("type", new JSString(blob.getType()));
        items.put("size", new JSInt(blob.getSize()));
    }

    @Override
    public JSValue get(String key) {
        return super.get(key);
    }

    public String getMimeType() {
        return blob.getType();
    }

    public Blob getBlob() {
        return blob;
    }

    Blob blob;
    public String type = "Blob";
}
