package com.yh.urltest.zlib.sub2main;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by stayli on 2017/2/8.
 */

public final class MainOpe {
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * 任务执行到子线程中
     *
     * @param runnable 任务task
     */
    public static void exSubOpe(Runnable runnable) {
        if (runnable != null)
            executor.execute(runnable);
    }

    /**
     * 任务执行到主线程中
     *
     * @param runnable 任务task
     */
    public static void exMainOpe(Runnable runnable) {
        if (runnable != null)
            Sub2MainKit.runOnMainThreadAsync(runnable);
    }
}
