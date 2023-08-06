package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Timer {

    public Timer(Window w, Function func, int interval, boolean run_once, Vector<JSValue> params) {
        this.uid = getUID();
        this.func = func;
        this.params = params;
        this.interval = interval;
        this.run_once = run_once;
        this.w = w;
        this.last_done = System.currentTimeMillis();
    }

    public static int getUID() {
        return ++last_uid;
    }

    public void check() {
        if (System.currentTimeMillis() - last_done > interval) {
            func.call(null, params, false);
            this.last_done = System.currentTimeMillis();
            if (run_once) {
                this.w.removeTimer(this);
            }
        }
    }

    public Function getFunction() {
        return func;
    }

    public int getId() {
        return uid;
    }

    private int interval;
    public long last_done;
    private boolean run_once = true;
    private Function func;
    private Vector<JSValue> params;
    private Window w;
    private int uid;
    private static int last_uid = 0;
}
