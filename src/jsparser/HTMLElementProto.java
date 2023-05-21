package jsparser;

import bridge.Mapper;
import cssparser.CSSParser;
import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLElementProto extends JSObject {

    class appendChildFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "ArgumentsError: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof HTMLNode)) {
                JSError e = new JSError(null, "TypeError: argument is not a node", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node node = ((HTMLElement)context).node;
            Node newNode = ((HTMLNode)args.get(0)).node;
            render.Block block = Mapper.get(newNode);
            boolean result = node.addChild(newNode);

            if (result) {
                block.builder.assignDocument(node);
                block.builder.processSpecialElement(node);
                render.Block parent_block = Mapper.get(node);
                if (newNode.nodeType == 1) {
                    parent_block.addElement(block, true);
                } else if (newNode.nodeType == 3) {
                    parent_block.addText(newNode.nodeValue);
                }
                args.get(0).incrementRefCount();
            }

            ((HTMLElement)context).updateChildren();

            return Undefined.getInstance();
        }
    }

    class insertBeforeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "ArgumentsError: 2 arguments expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof HTMLNode)) {
                JSError e = new JSError(null, "TypeError: argument 1 is not a node", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(1) instanceof HTMLNode)) {
                JSError e = new JSError(null, "TypeError: argument 2 is not a node", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node newNode = ((HTMLNode)args.get(0)).node;
            render.Block block = Mapper.get(newNode);
            boolean result = ((HTMLElement)context).node.insertChild(newNode, ((HTMLNode)args.get(1)).node);

            if (result) {
                Node node = ((HTMLElement)context).node;
                block.builder.assignDocument(node);
                block.builder.processSpecialElement(node);
                int pos = ((HTMLElement)context).node.children.indexOf(newNode);
                render.Block parent_block = Mapper.get(node);
                if (newNode.nodeType == 1) {
                    parent_block.addElement(block, pos, true);
                } else if (newNode.nodeType == 3) {
                    parent_block.addText(newNode.nodeValue, pos);
                }
                args.get(0).incrementRefCount();
            }

            ((HTMLElement)context).updateChildren();

            return Undefined.getInstance();
        }
    }

    class removeChildFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "ArgumentsError: 1 argument expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof HTMLElement)) {
                JSError e = new JSError(null, "TypeError: argument is not an element", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            render.Block block = Mapper.get(((HTMLNode)args.get(0)).node);
            boolean result = ((HTMLElement)context).node.removeChild(((HTMLNode)args.get(0)).node);

            if (result) {
                ((HTMLNode)args.get(0)).document = null;
                Node node = ((HTMLElement)context).node;
                render.Block parent_block = Mapper.get(node);
                parent_block.removeElement(block);
                args.get(0).decrementRefCount();
            }

            ((HTMLElement)context).updateChildren();

            return Undefined.getInstance();
        }
    }

    class addEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required, but only 1 present", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(1) instanceof Function)) {
                JSError e = new JSError(null, "Argument 2 type error: Function expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            boolean capture_phase = args.size() > 2 && args.get(2).asBool().getValue();
            String event_type = args.get(0).asString().getValue();
            Vector<Function> funcs = ((HTMLElement)context).listeners.get(event_type);
            if (funcs == null) {
                funcs = new Vector<Function>();
                if (!capture_phase) ((HTMLElement)context).listeners.put(event_type, funcs);
                else ((HTMLElement)context).listeners_0.put(event_type, funcs);
            }
            funcs.add((Function) args.get(1));
            args.get(1).incrementRefCount();
            return Undefined.getInstance();
        }
    }

    class removeEventListenerFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required, but only 1 present", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(1) instanceof Function)) {
                JSError e = new JSError(null, "Argument 2 type error: Function expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            boolean capture_phase = args.size() > 2 && args.get(2).asBool().getValue();
            String event_type = args.get(0).asString().getValue();
            Vector<Function> funcs = !capture_phase ? ((HTMLElement)context).listeners.get(event_type) : ((HTMLElement)context).listeners_0.get(event_type);
            for (int i = 0; i < funcs.size(); i++) {
                if (funcs.get(i) == args.get(1)) {
                    funcs.remove(i);
                    args.get(1).decrementRefCount();
                    if (items.get("on" + event_type) == args.get(1)) {
                        items.remove("on" + event_type);
                    }
                    break;
                }
            }
            return Undefined.getInstance();
        }
    }

    class addClassFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            ((HTMLElement)context).node.addClass(args.get(0).asString().getValue());

            return new JSBool(true);
        }
    }

    class hasClassFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            String classes = ((HTMLElement)context).node.getAttribute("class");
            if (classes == null) {
                return new JSBool(false);
            }
            return new JSBool(classes.matches("(^|\\s)" + args.get(0).asString().getValue() + "(\\s|$)"));
        }
    }

    class removeClassFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            if (!(args.get(0) instanceof JSString)) {
                JSError e = new JSError(null, "Argument 1 type error: string expected", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            ((HTMLElement)context).node.removeClass(args.get(0).asString().getValue());

            return new JSBool(true);
        }
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
                Node n = ((HTMLElement)context).node;
                Node node = n.document.getElementById(n, args.get(i).asString().getValue(), true);
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
                Node n = ((HTMLElement)context).node;
                Vector<Node> nodes = n.document.getElementsByTagName(n, args.get(i).asString().getValue(), true);
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
                Node n = ((HTMLElement)context).node;
                Vector<Node> nodes = n.document.getElementsByName(n, args.get(i).asString().getValue(), true);
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
                Node n = ((HTMLElement)context).node;
                Vector<Node> nodes = n.document.getElementsByClassName(n, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(HTMLElement.create(node));
                }
            }
            return array;
        }
    }

    class querySelectorFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Node n = ((HTMLElement)context).node;
                CSSParser parser = new CSSParser(n.document);
                return HTMLElement.create(parser.documentQuerySelector(args.get(i).asString().getValue(), n));
            }
            return null;
        }
    }

    class querySelectorAllFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSArray array = new JSArray();
            for (int i = 0; i < args.size(); i++) {
                if (!(args.get(i) instanceof JSString)) {
                    JSError e = new JSError(null, "TypeError: argument is not a string", getCaller().getStack());
                    getCaller().error = e;
                    return Undefined.getInstance();
                }
                Node n = ((HTMLElement)context).node;
                CSSParser parser = new CSSParser(n.document);
                Vector<Node> nodes = parser.documentQuerySelectorAll(args.get(i).asString().getValue(), n);
                for (Node node: nodes) {
                    HTMLElement element = HTMLElement.create(node);
                    if (!array.items.contains(element)) {
                        array.push(element);
                    }
                }
            }
            return array;
        }
    }

    class getAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node node = ((HTMLElement)context).node;
            return new JSString(node.getAttribute(args.get(0).asString().getValue()));
        }
    }

    class hasAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node node = ((HTMLElement)context).node;
            return new JSBool(node.getAttribute(args.get(0).asString().getValue()) != null);
        }
    }

    class setAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) {
                JSError e = new JSError(null, "Arguments size error: 2 arguments required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            String key = args.get(0).asString().getValue();
            if (!key.matches("[a-z0-9_-]+")) {
                JSError e = new JSError(null, "Uncaught DOMException: '" + key + "' is not a valid attribute name", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            HTMLElement element = (HTMLElement) context;
            Node node = element.node;
            node.setAttribute(key, args.get(1).asString().getValue());
            element.updateAttributesList();
            element.updateClassList();
            if (key.startsWith("data-")) {
                element.getDataset().items.put(key.substring(5), args.get(1).asString());
            }
            if (key.equals("style")) {
                element.updateStyles();
            }
            return Undefined.getInstance();
        }
    }

    class removeAttributeFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 1) {
                JSError e = new JSError(null, "Arguments size error: 1 argument required", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            HTMLElement element = (HTMLElement) context;
            Node node = element.node;
            String key = args.get(0).asString().getValue();
            node.removeAttribute(key);
            element.updateAttributesList();
            element.updateClassList();
            if (key.startsWith("data-")) {
                element.getDataset().items.remove(key.substring(5));
            }
            if (key.equals("style")) {
                element.updateStyles();
            }
            return Undefined.getInstance();
        }
    }

    private HTMLElementProto() {
        items.put("__proto__", HTMLNodeProto.getInstance());
        
        items.put("getElementById", new getElementByIdFunction());
        items.put("getElementsByTagName", new getElementsByTagNameFunction());
        items.put("getElementsByName", new getElementsByNameFunction());
        items.put("getElementsByClassName", new getElementsByClassNameFunction());
        items.put("querySelector", new querySelectorFunction());
        items.put("querySelectorAll", new querySelectorAllFunction());

        items.put("getAttribute", new getAttributeFunction());
        items.put("hasAttribute", new hasAttributeFunction());
        items.put("setAttribute", new setAttributeFunction());
        items.put("removeAttribute", new removeAttributeFunction());

        items.put("appendChild", new appendChildFunction());
        items.put("insertBefore", new insertBeforeFunction());
        items.put("removeChild", new removeChildFunction());

        items.put("addEventListener", new addEventListenerFunction());
        items.put("removeEventListener", new removeEventListenerFunction());
    }

    public static HTMLElementProto getInstance() {
        if (instance == null) {
            instance = new HTMLElementProto();
        }
        return instance;
    }

    private String type = "Object";
    private static HTMLElementProto instance = null;

}
