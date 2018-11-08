package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class FunctionC extends Function {

    public FunctionC() {
        items.put("prototype", FunctionProto.getInstance());
        FunctionProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        if (args.size() >= 1 && args.get(0).getType().equals("String")) {
            JSParser jp = new JSParser("{ " + ((JSString)args.get(0)).getValue() + " }");
            Block b = (Block)Expression.create(jp.getHead());
            b.parent_block = ((Window)context).getRoot();
            Vector<String> p = new Vector<String>();
            for (int i = 1; i < args.size(); i++) {
                if (args.get(i).getType().equals("String")) {
                    p.add(((JSString)args.get(i)).getValue());
                }
            }
            return new Function(p, b, "anonymous");
        }
        Block b = new Block();
        b.parent_block = ((Window)context).getRoot();
        return new Function(new Vector<String>(), b, "anonymous");
    }
}
