package jsparser;

import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLNodeProto extends JSObject {

    private HTMLNodeProto() {}

    class getParentFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node parent = node != null ? node.parent : null;
            return parent != null ? (parent.nodeType == 1 ? HTMLElement.create(parent) : HTMLNode.create(parent)) : Null.getInstance();
        }
    }

    class getPreviousSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousSibling : null;
            return prev != null ? (prev.nodeType == 1 ? HTMLElement.create(prev) : HTMLNode.create(prev)) : Null.getInstance();
        }
    }

    class getNextSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextSibling : null;
            return next != null ? (next.nodeType == 1 ? HTMLElement.create(next) : HTMLNode.create(next)) : Null.getInstance();
        }
    }

    class getPreviousElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousElementSibling() : null;
            return prev != null ? (prev.nodeType == 1 ? HTMLElement.create(prev) : HTMLNode.create(prev)) : Null.getInstance();
        }
    }

    class getNextElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextElementSibling() : null;
            return next != null ? (next.nodeType == 1 ? HTMLElement.create(next) : HTMLNode.create(next)) : Null.getInstance();
        }
    }

    class childNodesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            return array;
        }
    }

    class childrenFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            Node node = ((HTMLNode)context).node;
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            return array;
        }
    }

    public static HTMLNodeProto getInstance() {
        if (instance == null) {
            instance = new HTMLNodeProto();
        }
        return instance;
    }

    private String type = "Object";
    private static HTMLNodeProto instance = null;
}
