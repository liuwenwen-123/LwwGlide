package com.example.lwwglide.request;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.example.lwwglide.cache.ImageCache;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 3： 开启线层  执行任务
 * 获取到请求队列 中的请求对象
 */
public class BitmapDispatcher extends Thread {

    //     请求队列
    private LinkedBlockingQueue<BitmapRequest> mQueue;
    //     切换 线程
    private Handler handler = new Handler(Looper.getMainLooper());


    //    通过  构造方法 传递 请求队列
    public BitmapDispatcher(LinkedBlockingQueue<BitmapRequest> mQueue) {
        this.mQueue = mQueue;

    }


    @Override
    public void run() {
        super.run();
//        while (!isInterrupted())  线程没有被打扰 开启状态   h和 while (true) 一样  线程调用 interrupt 关闭了
        while (!isInterrupted()) {

            try {
                //          1:  获取请求对象
                BitmapRequest bitmapRequest = mQueue.take();
//                2：显示展位图
                showLodingView(bitmapRequest);
//                3 去服务器 下载图片
                Bitmap bitmap = lodingImg(bitmapRequest);
//                 显示控件
                showImg(bitmapRequest, bitmap);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    //    显示展位图
    private void showLodingView(BitmapRequest bitmapRequest) {
//        判断是否设置 展位图
        final int resId = bitmapRequest.getResId();
        final ImageView imageView = bitmapRequest.getImageView();
        if (resId != 0 && imageView != null) {
//             此时我们在 子线程 不能更细ui
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(resId);
                }
            });

        }
    }

    private Bitmap lodingImg(BitmapRequest bitmapRequest) {
//        先从缓存中 取出图片
        Bitmap bitmap = ImageCache.getInstance().getBitmapMemory(bitmapRequest.getUrlMD5());
        if (null == bitmap) {
//         从复用池中找   使用复用的内存   80  w  ,80 h ,1 缩放比例
            Bitmap resyable = ImageCache.getInstance().getResyable(80, 80, 1);
            //        在存 磁盘 取出图片
            bitmap = ImageCache.getInstance().getFromDis(bitmapRequest.getUrlMD5(), resyable);
            if (null == bitmap) {
                //        从网络下载
                bitmap = downLoad(bitmapRequest.getUrl());
//               存到内存和磁盘
                ImageCache.getInstance().putBitmapToMermary(bitmapRequest.getUrlMD5(), bitmap);
                ImageCache.getInstance().putdisCache(bitmapRequest.getUrlMD5(), bitmap);
                Log.e("e------", "从网络加载");
            } else {
                Log.e("e------", "从磁盘加载");
            }
        } else {
            Log.e("e------", "从内存加载");
        }


        return bitmap;
    }

    private Bitmap downLoad(String uri) {
        FileOutputStream fos = null;
        InputStream is = null;
        Bitmap bitmap = null;

//        创建一个url 对象
        try {
            URL url = new URL(uri);
//            使用HttpURLConnection 通过 创建一个url读取数据
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            is = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    /**
     * 显示图片
     *
     * @param bitmapRequest
     * @param bitmap
     */
    private void showImg(BitmapRequest bitmapRequest, final Bitmap bitmap) {
        final ImageView imageView = bitmapRequest.getImageView();
        if (bitmap != null && imageView != null && imageView.getTag().equals(bitmapRequest.getUrlMD5())) {
            //             此时我们在 子线程 不能更细ui
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }


}
