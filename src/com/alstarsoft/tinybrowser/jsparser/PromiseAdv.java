/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.alstarsoft.tinybrowser.jsparser;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class PromiseAdv extends Promise {

    public PromiseAdv(Function f) {
        super(f);
        func = null;
    }

    public PromiseAdv(Function f, Vector<JSValue> p, int type) {
        super(f);
        for (int i = 0; i < p.size(); i++) {
            if (p.get(i) instanceof Promise) {
                ((Promise)p.get(i)).attachTo(this);
                values.push(Null.getInstance());
                total++;
                if (((Promise)p.get(i)).state == Promise.FULFILLED) {
                    successful++;
                    values.set(i, ((Promise)p.get(i)).getResult());
                }
            } else {
                values.push(p.get(i));
            }
        }
        promises = p;
        this.type = type;
    }

    public void notify(Promise p, JSValue result, int state) {
        int index = -1;
        for (int i = 0; i < promises.size(); i++) {
            if (promises.get(i) == p) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            values.set(index, result);
            if (state == Promise.FULFILLED) {
                successful++;
                if (successful == total && type == ALL || type == ANY) {
                    setResult(type == ALL ? values : result);
                    setState(state);
                }
            } else {
                setResult(result);
                setState(state);
            }
        }
        p.detachFrom(this);
    }

    protected static int ALL = 0;
    protected static int ANY = 1;

    protected int type = 0;

    protected Vector<JSValue> promises = new Vector<JSValue>();
    protected JSArray values = new JSArray();
    protected int successful = 0;
    protected int total = 0;
}
