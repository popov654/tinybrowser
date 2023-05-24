package jsparser;

/**
 *
 * @author Alex
 */
public class Undefined extends JSValue {

    private Undefined() {
        
    }

    public static Undefined getInstance() {
        return instance;
    }

    @Override
    public JSString asString() {
        return new JSString("");
    }

    @Override
    public JSInt asInt() {
        return new JSInt(0);
    }

    @Override
    public JSFloat asFloat() {
        return new JSFloat(0);
    }

    @Override
    public JSBool asBool() {
        return new JSBool(false);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Undefined clone() {
        return instance;
    }

    @Override
    public String toString() {
        return type;
    }

    private static Undefined instance = new Undefined();
    private String type = "undefined";

}
