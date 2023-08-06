package com.alstarsoft.tinybrowser.jsparser;

/**
 *
 * @author Alex
 */
public class CustomProperty {

    public CustomProperty(JSObject descriptor) {
        value = descriptor.get("value");
        if (descriptor.items.containsKey("configurable")) {
            configurable = descriptor.get("configurable").asBool().getValue();
        }
        if (descriptor.items.containsKey("enumerable")) {
            enumerable = descriptor.get("enumerable").asBool().getValue();
        }
        if (descriptor.items.containsKey("writable")) {
            writable = descriptor.get("writable").asBool().getValue();
        }
        if (descriptor.items.containsKey("get") && descriptor.get("get") instanceof Function) {
            get = (Function) descriptor.get("get");
        }
        if (descriptor.items.containsKey("set") && descriptor.get("set") instanceof Function) {
            set = (Function) descriptor.get("set");
        }
    }

    public JSValue value;
    public boolean configurable = true;
    public boolean enumerable = true;
    public boolean writable = true;
    public Function get;
    public Function set;
}
