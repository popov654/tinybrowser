package jsparser;

import htmlparser.Node;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FormDataC extends Function {

    public FormDataC() {
        items.put("prototype", ObjectProto.getInstance());
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 0) {
            HTMLDocument document = (HTMLDocument) Block.getVar("document", getCaller());
            return new FormData(document);
        }
        if (args.size() == 1 && args.get(0) instanceof HTMLDocument) {
            return new FormData((HTMLDocument) args.get(0));
        }
        if (args.size() == 1 && args.get(0) instanceof HTMLElement) {
            Node node = ((HTMLElement) args.get(0)).node;
            if (node.tagName.equals("form")) {
                return new FormData((HTMLElement) args.get(0));
            }
        }
        return Undefined.getInstance();
    }
}
