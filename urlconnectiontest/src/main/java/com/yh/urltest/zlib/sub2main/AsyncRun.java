package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli on 2017/2/15.
 */

public abstract class AsyncRun<S> implements Sub2MainInter<S> {
    @Override
    public abstract void runSub(S s);

    @Override
    public final void runOnMainThreadSync(S s) {

    }

    @Override
    public abstract void runOnMainThreadAsync(S s);


}
