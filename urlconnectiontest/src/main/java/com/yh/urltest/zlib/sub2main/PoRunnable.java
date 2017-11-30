package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli on 2017/2/15.
 */

public abstract class PoRunnable<T> extends PSRunnable.PHRunnable<T> {
    protected PoRunnable(Sub2MainInter<T> pInterface) {
        super(pInterface);
    }

    @Override
    public T type() {
        return null;
    }

    @Override
    public abstract T exRun() ;
}
