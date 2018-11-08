package jsparser;

import java.util.Calendar;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class DateC extends Function {

    public DateC() {
        items.put("prototype", DateProto.getInstance());
        DateProto.getInstance().set("constructor", this);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
        return call(context, args);
    }

    @Override
    public JSValue call(JSObject context, Vector<JSValue> args) {
        if (args.size() == 1 && args.get(0).getType().equals("Integer")) {
            return new JSDate(((JSInt)args.get(0)).getValue());
        } else if (args.size() == 1 && args.get(0).getType().equals("String")) {
            return new JSDate(((JSString)args.get(0)).getValue());
        } else if (args.size() >= 2 && args.get(0).getType().equals("Integer")) {
            int year = (int)args.get(0).asInt().getValue();
            int month = (int)args.get(1).asInt().getValue();
            int day = args.size() >= 3 ? (int)args.get(2).asInt().getValue() : 1;
            int hour = args.size() >= 4 ? (int)args.get(3).asInt().getValue() : 0;
            int minute = args.size() >= 5 ? (int)args.get(4).asInt().getValue() : 0;
            int second = args.size() >= 6 ? (int)args.get(5).asInt().getValue() : 0;
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, hour, minute, second);
            return new JSDate(c.getTimeInMillis());
        } else {
            return new JSDate();
        }
    }

}
