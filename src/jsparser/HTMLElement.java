package jsparser;

import bridge.Mapper;
import cssparser.StyleMap;
import htmlparser.Node;
import htmlparser.NodeActionCallback;
import htmlparser.NodeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class HTMLElement extends JSObject {

    private HTMLElement(Node node) {
        this.node = node;

        if (node.nodeType == 1) {
            items.put("getElementById", new getElementByIdFunction());
            items.put("getElementsByTagName", new getElementsByTagNameFunction());
            items.put("getElementsByName", new getElementsByNameFunction());
            items.put("getElementsByClassName", new getElementsByClassNameFunction());

            items.put("getAttribute", new getAttributeFunction());
            items.put("hasAttribute", new hasAttributeFunction());
            items.put("setAttribute", new setAttributeFunction());
            items.put("removeAttribute", new removeAttributeFunction());

            items.put("addEventListener", new addEventListenerFunction());
            items.put("removeEventListener", new removeEventListenerFunction());
        }

        node.addListener(eventListener, node, "any");
    }

    NodeActionCallback eventListener = new NodeActionCallback() {

        @Override
        public void nodeChanged(NodeEvent e, String source) {
            if (!source.split(":")[0].equals("render") && e.target.nodeType == 1) {
                updateClassList();
                updateStyles();
                updateAttributesList();
                updateDataset();
                return;
            }
            if (source.equals("render:layout")) {
                updateSize();
                return;
            }
            HashMap<String, String> data = e.getData();
            //if (data != null) System.out.println(data.get("pageX") + ", " + data.get("pageY"));
            Vector<HTMLElement> parents = getParents();
            if (data != null) data.put("bubbles", parents.size() > 1 ? "true" : "false");
            JSEvent event = new JSEvent(HTMLElement.create(node), e.relatedTarget != null ? HTMLElement.create(e.relatedTarget) : null, data);
            Vector<JSValue> args = new Vector<JSValue>();
            args.add(event);
            String type = data.get("type").replaceAll("(^\"|\"$)", "");
            for (int i = parents.size()-1; i >= 0; i--) {
                if (event.get("cancelBubble").asBool().getValue()) break;
                if (parents.get(i).listeners_0.get(type) == null) continue;
                Vector<Function> funcs = (Vector<Function>) parents.get(i).listeners_0.get(type).clone();
                for (Function func: funcs) {
                    func.call(args);
                }
            }
            for (int i = 0; i < parents.size(); i++) {
                if (event.get("cancelBubble").asBool().getValue()) break;
                if (parents.get(i).listeners.get(type) == null) continue;
                Vector<Function> funcs = (Vector<Function>) parents.get(i).listeners.get(type).clone();
                for (Function func: funcs) {
                    func.call(args);
                }
            }
            if (!type.equals("mousemove")) {
                System.out.println("JS event: " + source.split(":")[1] + " " + e.target.tagName);
                /* if (relatedTarget != null) {
                    System.out.println("Related target: " + relatedTarget.tagName);
                } */
            }
        }

    };

    private Vector<HTMLElement> getParents() {
        Vector<HTMLElement> result = new Vector<HTMLElement>();
        result.add(this);
        JSValue p = items.get("parentNode");
        while (p != null && p instanceof HTMLElement) {
            result.add((HTMLElement)p);
            p = ((HTMLElement)p).items.get("parentNode");
        }
        return result;
    }

    public static HTMLElement create(Node node) {
        HTMLElement element = map.get(node);
        if (element == null) {
            element = new HTMLElement(node);
            map.put(node, element);
            element.calculateValues();
        }
        return element;
    }

    public void calculateValues() {
        items.put("nodeValue", new JSString(node.nodeValue));
        items.put("textContent", new JSString(node.getTextContent()));
        items.put("nodeType", new JSInt(node.nodeType));
        if (node.nodeType == 1) {
            items.put("tagName", new JSString(node.tagName));
        }

        Node parent = node != null ? node.parent : null;
        items.put("parentNode", parent != null ? HTMLElement.create(parent) : Null.getInstance());
        Node prev = node != null ? node.previousSibling : null;
        items.put("previousSibling", prev != null ? HTMLElement.create(prev) : Null.getInstance());
        Node next = node != null ? node.nextSibling : null;
        items.put("nextSibling", next != null ? HTMLElement.create(next) : Null.getInstance());
        Node prev_el = node != null ? node.previousElementSibling() : null;
        items.put("previousElementSibling", prev_el != null ? HTMLElement.create(prev_el) : Null.getInstance());
        Node next_el = node != null ? node.nextElementSibling() : null;
        items.put("nextElementSibling", next_el != null ? HTMLElement.create(next_el) : Null.getInstance());

        Node first_child = node != null ? node.firstChild() : null;
        items.put("firstChild", first_child != null ? HTMLElement.create(first_child) : Null.getInstance());
        Node last_child = node != null ? node.lastChild() : null;
        items.put("lastChild", last_child != null ? HTMLElement.create(last_child) : Null.getInstance());
        Node first_el_child = node != null ? node.firstElementChild() : null;
        items.put("firstElementChild", first_el_child != null ? HTMLElement.create(first_el_child) : Null.getInstance());
        Node last_el_child = node != null ? node.lastElementChild() : null;
        items.put("lastElementChild", last_el_child != null ? HTMLElement.create(last_el_child) : Null.getInstance());

        JSArray childNodes = new JSArray();
        for (Node child: node.children) {
            childNodes.push(HTMLElement.create(child));
        }
        items.put("childNodes", childNodes);
        
        JSArray children = new JSArray();
        for (Node child: node.children) {
            if (child.nodeType == 1) {
                children.push(HTMLElement.create(child));
            }
        }
        items.put("children", children);

        if (node.nodeType != 1) return;

        updateClassList();
        updateStyles();
        updateAttributesList();
        updateDataset();
    }

    class getParentFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node parent = node != null ? node.parent : null;
            return parent != null ? new HTMLElement(parent) : Null.getInstance();
        }
    }

    class getPreviousSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousSibling : null;
            return prev != null ? new HTMLElement(prev) : Null.getInstance();
        }
    }

    class getNextSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextSibling : null;
            return next != null ? new HTMLElement(next) : Null.getInstance();
        }
    }

    class getPreviousElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node prev = node != null ? node.previousElementSibling() : null;
            return prev != null ? new HTMLElement(prev) : Null.getInstance();
        }
    }

    class getNextElementSiblingFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            Node next = node != null ? node.nextElementSibling() : null;
            return next != null ? new HTMLElement(next) : Null.getInstance();
        }
    }

    class childNodesFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            for (Node child: node.children) {
                array.push(new HTMLElement(child));
            }
            return array;
        }
    }

    class childrenFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (node == null) {
                JSError e = new JSError(null, "TypeError: cannot read properties of null", getCaller().getStack());
                getCaller().error = e;
                return Undefined.getInstance();
            }
            JSArray array = new JSArray();
            for (Node child: node.children) {
                if (child.nodeType == 1) {
                    array.push(new HTMLElement(child));
                }
            }
            return array;
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
                Node n = node.document.getElementById(node, args.get(i).asString().getValue(), true);
                return n != null ? new HTMLElement(n) : Null.getInstance();
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
                Vector<Node> nodes = node.document.getElementsByTagName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new HTMLElement(node));
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
                Vector<Node> nodes = node.document.getElementsByName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new HTMLElement(node));
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
                Vector<Node> nodes = node.document.getElementsByClassName(node, args.get(i).asString().getValue(), true);
                for (Node node: nodes) {
                    array.push(new HTMLElement(node));
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
            node.setAttribute(key, args.get(1).asString().getValue());
            updateAttributesList();
            updateClassList();
            if (key.startsWith("data-")) {
                getDataset().items.put(key.substring(5), args.get(1).asString());
            }
            if (key.equals("style")) {
                updateStyles();
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
            String key = args.get(0).asString().getValue();
            node.removeAttribute(key);
            updateAttributesList();
            updateClassList();
            if (key.startsWith("data-")) {
                getDataset().items.remove(key.substring(5));
            }
            if (key.equals("style")) {
                updateStyles();
            }
            return Undefined.getInstance();
        }
    }

    public DOMStringMap getDataset() {
        return items.get("dataset") != null ? (DOMStringMap) items.get("dataset") : null;
    }

    public DOMStringMap getComputedStyles() {
        DOMStringMap style = new DOMStringMap(StyleMap.getNodeStyles(node).styles, node);
        return style;
    }

    public void updateStyles() {
        DOMStringMap style = new DOMStringMap(StyleMap.getNodeStyles(node).runtimeStyles, node) {
            @Override
            public void updateNode() {
                if (node.document == null) return;
                HashMap<String, String> st = StyleMap.getNodeStyles(node).runtimeStyles;
                st.clear();
                Set<String> keys = items.keySet();
                for (String key: keys) {
                    if (key.equals("__proto__")) continue;
                    st.put(key, items.get(key).asString().getValue());
                }
                node.fireEvent("stylesChanged", "node");
            }
        };
        items.put("style", style);
    }

    public void updateAttributesList() {
        Vector<JSValue> attrs = new Vector<JSValue>();
        for (String str: node.attributes.keySet()) {
            if (str.equals("__proto__")) continue;
            attrs.add(new JSString(str));
        }
        items.put("attributes", new JSArray(attrs) {
            @Override
            public JSArray push(JSValue value) {
                return this;
            }
            @Override
            public JSArray pop() {
                return this;
            }
            @Override
            public JSArray shift() {
                return this;
            }
            @Override
            public JSArray unshift(JSValue value) {
                return this;
            }
        });
    }

    public void updateDataset() {
        HashMap<String, String> data = new HashMap<String, String>();
        Set<String> keys = node.attributes.keySet();
        for (String key: keys) {
            if (key.startsWith("data-")) {
                data.put(key.substring(5), node.attributes.get(key));
            }
        }
        DOMStringMap attrs = new DOMStringMap(data, node);
        items.put("dataset", attrs);
    }

    public void updateClassList() {
        NodeClassList classes = new NodeClassList(node);
        items.put("classList", classes);
    }

    public void updateSize() {
        render.Block b0 = Mapper.get(node);
        if (b0 == null) return;

        render.Block b = b0.parts.size() == 0 ? b0 : b0.parts.get(0);

        if (b0.parts.size() == 0) {
            items.put("offsetWidth", new JSInt(b.orig_width));
            items.put("clientWidth", new JSFloat(b.width > 0 ? (double) b.width / b.ratio : 0));
            items.put("screenWidth", new JSInt(b.viewport_width));
            items.put("offsetHeight", new JSInt(b.orig_height));
            items.put("clientHeight", new JSFloat(b.height > 0 ? (double) b.height / b.ratio : 0));
            items.put("screenHeight", new JSInt(b.viewport_height));
        } else if (b.document.return_size_for_inlines || b.isImage) {
            int w = 0, h = 0;

            render.Line line = null;

            for (int i = 0; i < b0.parts.size(); i++) {
                w += b0.parts.get(i).width;
                if (b0.parts.get(i).line != line) {
                    h += b0.parts.get(i).height;
                    line = b0.parts.get(i).line;
                }
            }

            items.put("screenWidth", new JSInt(w));
            items.put("screenHeight", new JSInt(h));

            w = (int)Math.floor(w / b0.ratio);
            h = (int)Math.floor(h / b0.ratio);

            items.put("offsetWidth", new JSInt(w));
            items.put("clientWidth", new JSInt(w));
            items.put("offsetHeight", new JSInt(h));
            items.put("clientHeight", new JSInt(h));
        } else {
            items.put("offsetWidth", new JSInt(0));
            items.put("clientWidth", new JSInt(0));
            items.put("screenWidth", new JSInt(0));
            items.put("offsetHeight", new JSInt(0));
            items.put("clientHeight", new JSInt(0));
            items.put("screenHeight", new JSInt(0));
        }

        items.put("offsetX", new JSInt(b._x_ - (b.parent != null ? b.parent._x_ : 0)));
        items.put("offsetY", new JSInt(b._y_ - (b.parent != null ? b.parent._y_ : 0)));
        items.put("clientX", new JSInt(b._x_ - b.scroll_x));
        items.put("clientY", new JSInt(b._y_ - b.scroll_y));
        items.put("pageX", new JSInt(b._x_));
        items.put("pageY", new JSInt(b._y_));

        javax.swing.JFrame frame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(b);
        if (frame == null) return;
        items.put("screenX", new JSInt(b.document != null ? b._x_ + b.document.getBounds().x + frame.getLocation().x : 0));
        items.put("screenY", new JSInt(b.document != null ? b._y_ + b.document.getBounds().y + frame.getLocation().y : 0));
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
            Vector<Function> funcs = listeners.get(event_type);
            if (funcs == null) {
                funcs = new Vector<Function>();
                if (!capture_phase) listeners.put(event_type, funcs);
                else listeners_0.put(event_type, funcs);
            }
            funcs.add((Function) args.get(1));
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
            Vector<Function> funcs = !capture_phase ? listeners.get(event_type) : listeners_0.get(event_type);
            for (int i = 0; i < funcs.size(); i++) {
                if (funcs.get(i) == args.get(1)) {
                    funcs.remove(i);
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
            node.addClass(args.get(0).asString().getValue());
            
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
            String classes = node.getAttribute("class");
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
            node.removeClass(args.get(0).asString().getValue());

            return new JSBool(true);
        }
    }

    @Override
    public void set(String str, JSValue value) {
        if (str.startsWith("on") && (value instanceof Function || value instanceof Null || value instanceof Undefined)) {
            boolean add = !(value instanceof Null || value instanceof Undefined);
            Function func = (Function) items.get(add ? "addEventListener" : "removeEventListener");
            if (func != null) {
                Vector<JSValue> args = new Vector<JSValue>();
                args.add(new JSString(str.substring(2).toLowerCase()));
                args.add(add ? value : items.get(str));
                if (args.get(1) instanceof Function) func.call(this, args);
            }
        }
        if (str.equals("nodeValue")) {
            node.nodeValue = value.asString().getValue();
            items.put("nodeValue", new JSString(node.nodeValue));
            items.put("textContent", new JSString(node.getTextContent()));
            node.fireEvent("valueChanged", "node");
        }
        super.set(str, value);
    }

    @Override
    public void set(JSString str, JSValue value) {
        if (str.getValue().startsWith("on") && (value instanceof Function || value instanceof Null || value instanceof Undefined)) {
            boolean add = !(value instanceof Null || value instanceof Undefined);
            Function func = (Function) items.get(add ? "addEventListener" : "removeEventListener");
            if (func != null) {
                Vector<JSValue> args = new Vector<JSValue>();
                args.add(new JSString(str.getValue().substring(2).toLowerCase()));
                args.add(add ? value : items.get(str.getValue()));
                if (args.get(1) instanceof Function) func.call(this, args);
            }
        }
        if (str.getValue().equals("nodeValue")) {
            node.nodeValue = value.asString().getValue();
            items.put("nodeValue", new JSString(node.nodeValue));
            items.put("textContent", new JSString(node.getTextContent()));
            node.fireEvent("valueChanged", "node");
        }
        super.set(str, value);
    }

    public boolean equals(HTMLElement element) {
        return element.node == node;
    }

    @Override
    public String toString() {
        String result = "";
        Set keys = new TreeSet(items.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (str.equals("children") || str.equals("childNodes") || str.matches("(previous|next)(Element)?Sibling")) continue;
            if (str.equals("parentNode")) {
                if (result.length() > 0) result += ", ";
                result += "parentNode: " + (node.parent != null ? "HTMLElement[" + node.parent.tagName + "]" : "null");
                continue;
            }
            if (str.equals("textContent")) {
                if (result.length() > 0) result += ", ";
                String text = node.getTextContent();
                if (text.length() > 100) {
                    text = text.substring(0, 97) + "...";
                }
                result += "textContent: \"" + text + "\"";
                continue;
            }
            if (items.get(str) == this) continue;
            if (result.length() > 0) result += ", ";
            result += str + ": " + items.get(str).toString();
        }
        return "HTMLElement {" + result + "}";
    }

    public static HashMap<Node, HTMLElement> map = new HashMap<Node, HTMLElement>();

    LinkedHashMap<String, Vector<Function>> listeners = new LinkedHashMap<String, Vector<Function>>();
    LinkedHashMap<String, Vector<Function>> listeners_0 = new LinkedHashMap<String, Vector<Function>>();
    HashMap<String, String> styles = new HashMap<String, String>();
    public Node node;
}
