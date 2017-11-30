package com.yh.urltest.zlib.sub2main;

import android.os.Looper;

/**
 * Created by stayli on 2017/2/8.
 * <p>
 * 对外提供类
 */

 class Sub2MainKit {
    private static HandlerPoster mainPoster = null;

    private static HandlerPoster getMainPoster() {
        if (mainPoster == null) {
            synchronized (Sub2MainKit.class) {
                if (mainPoster == null) {
                    mainPoster = new HandlerPoster(Looper.getMainLooper(), 20);
                }
            }
        }
        return mainPoster;
    }

    /**
     * 异步
     *与主线程相对应的子线程异步运行，
     *不阻塞子线程
     *
     * @param runnable Runnable Interface
     */
    public static void runOnMainThreadAsync(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        getMainPoster().async(runnable);
    }

    /**
     * 同步
     *子线程相对线程同步操作，
     *阻塞子线程，
     *主线程完成的线程
     *
     * @param runnable Runnable Interface
     */
     static void runOnMainThreadSync(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        SyncPost poster = new SyncPost(runnable);
        getMainPoster().sync(poster);
        poster.waitRun();
    }

    public static void dispose() {
        if (mainPoster != null) {
            mainPoster.dispose();
            mainPoster = null;
        }
    }
}
