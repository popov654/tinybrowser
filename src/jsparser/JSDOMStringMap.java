package jsparser;

/**
 *
 * @author Alex
 */
public class JSDOMStringMap extends JSObject {

    @Override
    public void set(String key, JSValue value) {
        value = value.asString();
        super.set(key, value);
    }
}
