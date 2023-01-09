package jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class MathObj extends JSObject {

    class absFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.abs(((JSFloat)val).getValue());
            if (value == Math.round(value)) {
                return new JSInt(value);
            }
            return new JSFloat(value);
        }
    }

    class roundFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            return new JSInt(Math.round(((JSFloat)val).getValue()));
        }
    }

    class ceilFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            return new JSInt(Math.round(Math.ceil(((JSFloat)val).getValue())));
        }
    }

    class floorFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            return new JSInt(Math.round(Math.floor(((JSFloat)val).getValue())));
        }
    }

    class sinFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.sin(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class asinFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.asin(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class cosFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.cos(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class acosFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.acos(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class tanFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.tan(((JSFloat)val).getValue());
            if (Math.abs((long)Math.round(value)) == 22877332 || Math.abs((long)Math.round(value)) == 16331239353195370L) {
                return Infinity.getInstance(value > 0);
            }
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt((long)Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class atanFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.atan(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class atan2Function extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) return new NaN();
            JSValue y = args.get(0).asFloat();
            JSValue x = args.get(1).asFloat();
            if (y == null || y.getType().equals("NaN") ||
                x == null || x.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.atan2(((JSFloat)y).getValue(), ((JSFloat)x).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class sinhFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.sinh(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class coshFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.cosh(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class tanhFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.tanh(((JSFloat)val).getValue());
            if (Math.abs(value - Math.round(value)) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class sqrtFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.sqrt(((JSFloat)val).getValue());
            if (value - Math.round(value) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class signFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() == 0) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.signum(((JSFloat)val).getValue());
            return new JSInt(value);
        }
    }

    class truncFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) return new NaN();
            JSValue val = args.get(0).asFloat();
            if (val == null || val.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            return new JSInt(((JSFloat)val).getValue());
        }
    }

    class powFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            if (args.size() < 2) return new NaN();
            JSValue a = args.get(0).asFloat();
            JSValue b = args.get(1).asFloat();
            if (a == null || a.getType().equals("NaN") ||
                b == null || b.getType().equals("NaN")) {
                return NaN.getInstance();
            }
            double value = Math.pow(((JSFloat)a).getValue(), ((JSFloat)b).getValue());
            if (value - Math.round(value) < 0.0000001) {
                return new JSInt(Math.round(value));
            }
            return new JSFloat(value);
        }
    }

    class randomFunction extends Function {
        @Override
        public JSValue call(JSObject context, Vector<JSValue> args, boolean as_constr) {
            return new JSFloat(Math.random());
        }
    }

    private MathObj() {
        items.put("PI", new JSFloat(Math.PI));
        items.put("E", new JSFloat(Math.E));
        items.put("LN10", new JSFloat(Math.log(10)));
        items.put("LN2", new JSFloat(Math.log(2)));
        items.put("LOG10E", new JSFloat(Math.log(Math.E) / Math.log(10)));
        items.put("LOG2E", new JSFloat(Math.log(Math.E) / Math.log(2)));
        items.put("SQRT2", new JSFloat(Math.sqrt(2)));
        items.put("SQRT1_2", new JSFloat(Math.sqrt(0.5)));
        items.put("abs", new absFunction());
        items.put("round", new roundFunction());
        items.put("ceil", new ceilFunction());
        items.put("floor", new floorFunction());
        items.put("sin", new sinFunction());
        items.put("asin", new asinFunction());
        items.put("cos", new cosFunction());
        items.put("acos", new acosFunction());
        items.put("tan", new tanFunction());
        items.put("atan", new atanFunction());
        items.put("atan2", new atan2Function());
        items.put("sinh", new sinhFunction());
        items.put("cosh", new coshFunction());
        items.put("tanh", new tanhFunction());
        items.put("sqrt", new sqrtFunction());
        items.put("sign", new signFunction());
        items.put("trunc", new truncFunction());
        items.put("pow", new powFunction());
        items.put("random", new randomFunction());
    }

    public static MathObj getInstance() {
        if (instance == null) {
            instance = new MathObj();
        }
        return instance;
    }

    private String type = "Object";
    private static MathObj instance = null;
}
