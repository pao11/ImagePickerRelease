package com.pao11.imagepicker.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadHelper {
    public static final ThreadHelper INST = new ThreadHelper();

    private ExecutorService executors;

    private ThreadHelper(){
    }

    /**
     * 在线程中执行
     * @param runnable 要执行的runnable
     *
     * execute in thread
     */
    public void execute(Runnable runnable) {
        ExecutorService executorService = getExecutorService();
        if (executorService != null) {
            executorService.execute(runnable);
        } else {
            new Thread(runnable).start();
        }
    }

    /**
     * 获取缓存线程池
     * @return 缓存线程池服务
     *
     * Get the cache thread pool
     */
    private ExecutorService getExecutorService(){
        if (executors == null) {
            executors = Executors.newScheduledThreadPool(3);
        }
        return executors;
    }

}
