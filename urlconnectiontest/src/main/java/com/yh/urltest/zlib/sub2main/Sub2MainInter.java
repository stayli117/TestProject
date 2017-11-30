package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli
 * 执行的顺序先子线程，再主线程
 */
interface Sub2MainInter<S> {
    /**
     * 继续在子线程执行
     *子线程的执行顺序永远在第一位上
     * @param s
     */
    void runSub(S s);

    /**
     * 追加到队列中执行。
     * 同步进入主线程队列,
     * 等待主线程处理完成后继续执行子线程
     */
    void runOnMainThreadSync(S s);

    /**
     * 当前是主线程直接执行
     * 子线程就调用追加到队列中执行。
     * 异步进入主线程队列,无需等待
     */
    void runOnMainThreadAsync(S s);
}
