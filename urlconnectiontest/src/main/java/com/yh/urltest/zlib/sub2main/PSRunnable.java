package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli on 2017/2/8.
 */

interface PSRunnable<T> extends Runnable {

    T type();

    @Override
    void run();

    void callResult(T t);

    abstract class PHRunnable<T> implements PSRunnable<T> {

        private Sub2MainInter<T> mPInterface;

        PHRunnable(Sub2MainInter<T> pInterface) {
            mPInterface = pInterface;
        }

        @Override
        public abstract T type();

        @Override
        public void run() {
            callResult(exRun());
        }

        /**
         * @return 在此方法中执行耗时操作
         */
        public abstract T exRun();

        private static final String TAG = "PHRunnable";

        /**
         * @param t 耗时方法所得数据
         */
        @Override
        public void callResult(final T t) {
            if (mPInterface != null) {
                if (mPInterface instanceof SubRun) { // 仅回调到子线程
                    // 回调继续在子线程执行
                    mPInterface.runSub(t);

                }
                if (mPInterface instanceof SyncRun) { // 回调到子线程 和 同步进入主线程
                    // 回调继续在子线程执行
                    mPInterface.runSub(t);
                    // 同步进入主线程
                    Sub2MainKit.runOnMainThreadSync(new Runnable() {
                        @Override
                        public void run() {
                            mPInterface.runOnMainThreadSync(t);
                        }
                    });

                }
                if (mPInterface instanceof AsyncRun) {// 回调到子线程 和 异步进入主线程
                    // 回调继续在子线程执行
                    mPInterface.runSub(t);
                    //异步进入主线程
                    Sub2MainKit.runOnMainThreadAsync(new Runnable() {
                        @Override
                        public void run() {
                            mPInterface.runOnMainThreadAsync(t);
                        }
                    });

                }
            }
        }
    }

}
