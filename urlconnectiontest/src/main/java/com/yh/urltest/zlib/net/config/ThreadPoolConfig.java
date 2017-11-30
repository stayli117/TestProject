package com.yh.urltest.zlib.net.config;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPoolConfig {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // 默认核心池大小
    private static final int DEFAULT_CORE_SIZE = CPU_COUNT + 1;
    // 最大线程数
    private static final int DEFAULT_MAX_SIZE = CPU_COUNT * 2 + 1;
    // 池中空余线程存活时间
    private static final long DEFAULT_KEEP_ALIVE_TIME = 15;
    // 时间单位
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    // 线程池阻塞队列(默认队列长度为50)
    private static final int BLOCKING_QUEUE_SIZE = 150;
    private static BlockingQueue<Runnable> defaultQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_SIZE);

    // 上下文环境对象
    public Context context;
    // 默认初始化
    int corePoolZie = DEFAULT_CORE_SIZE;
    int maxPoolSize = DEFAULT_MAX_SIZE;
    long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    TimeUnit timeUnit = DEFAULT_TIME_UNIT;
    BlockingQueue<Runnable> blockingQueue = defaultQueue;

    public ThreadPoolConfig(Context context) {
        this.context = context;
    }

    public ThreadPoolConfig corePoolZie(int corePoolZie) {
        this.corePoolZie = corePoolZie;
        return this;
    }

    public ThreadPoolConfig maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public ThreadPoolConfig keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public ThreadPoolConfig timeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public ThreadPoolConfig blockingQueue(BlockingQueue<Runnable> blockingQueue) {
        this.blockingQueue = blockingQueue;
        return this;
    }

}
