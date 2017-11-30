package com.yh.urltest.zlib.net.config;


import com.yh.urltest.zlib.net.model.HttpRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    // 封装的线程池
    private static ThreadPoolExecutor pool;

    /**
     * 根据配置信息初始化线程池
     */
    public static void init(ThreadPoolConfig config) {
        pool = new ThreadPoolExecutor(config.corePoolZie,
                config.maxPoolSize, config.keepAliveTime,
                config.timeUnit, config.blockingQueue);
    }

    /**
     * 执行任务
     *
     * @param r
     */
    public static void execute(final Runnable r) {
        if (r != null) {
            try {
                if (!pool.isShutdown()) {
                    int activeCount = pool.getActiveCount();
                    int maximumPoolSize = pool.getMaximumPoolSize();
                    if (activeCount < maximumPoolSize) {
                        pool.execute(r);
                    } else {
                        BlockingQueue<Runnable> quene = getQuene();
                        if (quene.size() == maximumPoolSize) {
                            quene.put(r);
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param request 同步执行
     */
    public static void enqueue(HttpRequest request) {
        request.run();
    }

    /**
     * 清空阻塞队列
     */
    public static void removeAllTask() {
        pool.getQueue().clear();
    }

    /**
     * 从阻塞队列中删除指定任务
     *
     * @param obj 队列中保存的对象
     * @return 是否取消成功
     */
    public static boolean removeTaskFromQueue(final Object obj) {
        if (!pool.getQueue().contains(obj)) {
            return false;
        }

        pool.getQueue().remove(obj);
        return true;
    }

    /**
     * 获取阻塞队列
     *
     * @return 阻塞队列
     */
    public static BlockingQueue<Runnable> getQuene() {
        return pool.getQueue();
    }

    /**
     * 关闭，并等待任务执行完成，不接受新任务
     */
    public static void shutdown() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    /**
     * 关闭，立即关闭，并挂起所有正在执行的线程，不接受新任务
     */
    public static void shutdownRightnow() {
        if (pool != null) {
            pool.shutdownNow();
            try {
                // 设置超时极短，强制关闭所有任务
                pool.awaitTermination(1, TimeUnit.MICROSECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
