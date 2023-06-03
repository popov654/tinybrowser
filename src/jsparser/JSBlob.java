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
    }

    @Override
    public JSValue get(String key) {
        if (key.equals("mimeType")) {
            return new JSString(blob.getType());
        }
        return super.get(key);
    }

    Blob blob;
    public String type = "Blob";
}
