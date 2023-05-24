/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsparser;

/**
 *
 * @author Alex
 */
public class NaN extends JSValue {

    public boolean isNaN() {
        return true;
    }

    public JSBool asBool() {
        return new JSBool(true);
    }

    public JSInt asInt() {
        return null;
    }

    public JSFloat asFloat() {
        return null;
    }

    public JSString asString() {
        return new JSString(String.valueOf(value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String getType() {
        return type;
    }

    public static NaN getInstance() {
        return instance;
    }

    @Override
    public NaN clone() {
        return instance;
    }

    private static NaN instance = new NaN();
    
    private String value = "NaN";
    private String type = "NaN";
}
