package jsparser;

import htmlparser.HTMLParser;
import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class JSDocument extends JSObject {

    public JSDocument(HTMLParser document) {
        this.document = document;
        items.put("documentElement", JSElement.create(document.getRootNode()));
        items.put("getElementById", new getElementByIdFunction());
        items.put("getElementsByTagName", new getElementsByTagNameFunction());
        items.put("getElementsByName", new getElementsByNameFunction());
        items.put("getElementsByClassName", new getElementsByClassNameFunction());

        Vector<Node> head = document.getElementsByTagName("head");
        items.put("head", head.size() > 0 ? JSElement.create(head.get(0)) : Null.getInstance());
        Vector<Node> body = document.getElementsByTagName("body");
        items.put("body", body.size() > 0 ? JSElement.create(body.get(0)) : Null.getInstance());
    }

    class getElementByIdFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Node node = document.getElementById(args.get(i).asString().getValue());
                return node != null ? JSElement.create(node) : Null.getInstance();
            }
            return Null.getInstance();
        }
    }

    class getElementsByTagNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = document.getElementsByTagName(args.get(i).asString().getValue());
                for (Node node: nodes) {
                    array.push(JSElement.create(node));
                }
            }
            return array;
        }
    }

    class getElementsByNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = document.getElementsByName(args.get(i).asString().getValue());
                for (Node node: nodes) {
                    array.push(JSElement.create(node));
                }
            }
            return array;
        }
    }

    class getElementsByClassNameFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Vector<Node> nodes = document.getElementsByClassName(args.get(i).asString().getValue());
                for (Node node: nodes) {
                    array.push(JSElement.create(node));
                }
            }
            return array;
        }
    }

    public HTMLParser document;
}
