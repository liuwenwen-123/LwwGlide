package com.example.lwwglide.request;


import java.util.concurrent.LinkedBlockingQueue;

/**
 * 2:创建 请求对列 管理类
 * 将求对象添加到 队列
 * 管理所有的线程
 */
public class RequetManager {
    //     请求对列
    private LinkedBlockingQueue<BitmapRequest> mQueue;
    private static RequetManager requetManager = new RequetManager();
    //      管理线程的数据
    private BitmapDispatcher[] bitmapDispatchers;

    private RequetManager() {
        mQueue = new LinkedBlockingQueue<>();
        stop();
        creteAndStart();
    }

    /**
     * 创建并 开启线程
     */
    private void creteAndStart() {
//        获取当前app 最大线程数
        int i = Runtime.getRuntime().availableProcessors();
//        创建线程数据
        bitmapDispatchers = new BitmapDispatcher[i];
        for (int a = 0; a < i; a++) {
            BitmapDispatcher bitmapDispatcher = new BitmapDispatcher(mQueue);
            bitmapDispatcher.start();
            bitmapDispatchers[a] = bitmapDispatcher;
        }

    }

    public static RequetManager getInstance() {
        return requetManager;
    }

    /**
     * 请求对象 添加到 请求对列
     *
     * @param bitmapRequest
     */
    public void addBitmapRequest(BitmapRequest bitmapRequest) {
        if (bitmapRequest != null && !mQueue.contains(mQueue)) {
            mQueue.add(bitmapRequest);
        }
    }

    /**
     * 关闭所有 线程
     */
    public void stop() {
        if (bitmapDispatchers != null && bitmapDispatchers.length != 0) {
            for (BitmapDispatcher bitmapDispatcher  :bitmapDispatchers) {
                bitmapDispatcher.interrupt();
            }
        }
    }

}
