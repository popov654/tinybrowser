package jsparser;

/**
 *
 * @author Alex
 */
public class Null extends JSValue {

    private Null() {
        
    }

    public static Null getInstance() {
        return instance;
    }

    public boolean isNaN() {
        return false;
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
    public String toString() {
        return type;
    }

    private static Null instance = new Null();
    private String type = "null";

}
