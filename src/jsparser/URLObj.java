package jsparser;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.Request;

/**
 *
 * @author Alex
 */
public class URLObj extends JSObject {

    class createObjectURLFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) {
                JSError e = new JSError(null, "Arguments error: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof File || args.get(0) instanceof JSBlob)) {
                JSError e = new JSError(null, "Type error: argument 1 must be a File or a Blob", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSBlob blob = (JSBlob) args.get(0);

            JSObject window = (JSObject) Block.getVar("window", getCaller());
            JSObject location = (JSObject) window.get("location");
            String url = location.get("href").asString().getValue();
            
            URL uri = null;
            try {
                if (!url.isEmpty() && !url.matches("^(https?|ftp|file):")) {
                    url = "file:///" + url;
                }
                uri = new URL(url);
            } catch (MalformedURLException ex) {}
            final String uuid = Request.addBlob(uri, blob.blob).toString();
            
            return new JSString("blob://" + uuid);
        }
    }

    private URLObj() {
        items.put("createObjectURL", new createObjectURLFunction());
    }

    public static URLObj getInstance() {
        if (instance == null) {
            instance = new URLObj();
        }
        return instance;
    }

    private String type = "Object";
    private static URLObj instance = null;

}
