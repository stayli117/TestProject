package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli on 2017/3/21.
 */

public abstract class SubRun<S> implements Sub2MainInter<S> {
    @Override
    public abstract void runSub(S s);

    // 处理完毕如需要回调到主线程 必须调用 此方法 仅回调到一个同步方法
    protected final void runEndSub(final S s) {
        Sub2MainKit.runOnMainThreadSync(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadSync(s);
            }
        });
//        Sub2MainKit.runOnMainThreadAsync(new Runnable() {
//            @Override
//            public void run() {
//                runOnMainThreadAsync(s);
//            }
//        });
    }

    @Override
    public  void runOnMainThreadSync(final S s) {
    }

    @Override
    public final void runOnMainThreadAsync(final S s) {
    }

}
