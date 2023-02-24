package jsparser;

import htmlparser.HTMLParser;
import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLDocument extends JSObject {

    public HTMLDocument(HTMLParser document) {
        this.document = document;
        items.put("documentElement", HTMLElement.create(document.getRootNode()));
        items.put("getElementById", new getElementByIdFunction());
        items.put("getElementsByTagName", new getElementsByTagNameFunction());
        items.put("getElementsByName", new getElementsByNameFunction());
        items.put("getElementsByClassName", new getElementsByClassNameFunction());
        items.put("createElement", new createElementFunction());
        items.put("createTextNode", new createTextNodeFunction());

        Vector<Node> head = document.getElementsByTagName("head");
        items.put("head", head.size() > 0 ? HTMLElement.create(head.get(0)) : Null.getInstance());
        Vector<Node> body = document.getElementsByTagName("body");
        items.put("body", body.size() > 0 ? HTMLElement.create(body.get(0)) : Null.getInstance());
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
                return node != null ? HTMLElement.create(node) : Null.getInstance();
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
                    array.push(HTMLElement.create(node));
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
                    array.push(HTMLElement.create(node));
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
                    array.push(HTMLElement.create(node));
                }
            }
            return array;
        }
    }

    class createElementFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "ArgumentsError: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node n = new Node(1);
            n.tagName = args.get(0).asString().getValue();
            n.document = document;
            Vector<Node> root = document.getElementsByTagName("body");
            if (root.size() > 0 && bridge.Mapper.get(root.get(0)) != null) {
                render.Block root_block = bridge.Mapper.get(root.get(0));
                root_block.builder.buildElement(root_block.document, null, n);
            }
            return HTMLElement.create(n);
        }
    }

    class createTextNodeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "ArgumentsError: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node n = new Node(3);
            n.nodeValue = args.get(0).asString().getValue();
            n.document = document;
            Vector<Node> root = document.getElementsByTagName("body");
            if (root.size() > 0 && bridge.Mapper.get(root.get(0)) != null) {
                render.Block root_block = bridge.Mapper.get(root.get(0));
                root_block.builder.buildElement(root_block.document, null, n);
            }
            return HTMLNode.create(n);
        }
    }

    public HTMLParser document;
}
