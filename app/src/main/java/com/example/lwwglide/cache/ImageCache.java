package com.example.lwwglide.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.example.lwwglide.cache.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImageCache {
    private static ImageCache imageCache;

    private ImageCache() {
    }

    public static ImageCache getInstance() {
        if (imageCache == null) {
            synchronized (ImageCache.class) {
                if (imageCache == null) {
                    imageCache = new ImageCache();
                }
            }
        }
        return imageCache;
    }

    // ------------    基本属性
    private Context context; // 上下文
    private LruCache<String, Bitmap> lruCache;  //  内存缓存  + 复用池
    private DiskLruCache diskLruCache; // 磁盘缓存
    BitmapFactory.Options options=new BitmapFactory.Options();
    //     复用池   假设 缓存中只能 存 10 张   如果第11张来了 会放在 双向 列表第一个
//     最后一个位置的图片 就会被挤出来     为了保障性能  在加一个 复用池  存放 被挤出的图片
    public static Set<WeakReference<Bitmap>> resuablePool;

    //  --- 初始化
    public void init(Context context, String url) {
        this.context = context;
//       创建  复用池
        resuablePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
//        磁盘缓存  取多大呢？ 一般去 系统分配的 1/8
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = activityManager.getMemoryClass();
        lruCache = new LruCache<String, Bitmap>(memoryClass / 8 * 1024 * 1024) {
            /**
             *   Bitmap 占用的大小
             * @param key
             * @param value
             * @return
             *    Bitmap  如果具备了复用功能 在 sdk 版本19 之前
             *    super.sizeOf(key, value);  返回这张图片的大小
             *    19 之后  必须 《=value的值
             */
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    return value.getAllocationByteCount();
                }
                return value.getByteCount();
            }

            /**
             *    复用池  中的 bitmap
             * @param evicted
             * @param key
             * @param oldValue
             * @param newValue
             */
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
//                oldValue 从lru中 拿出的bitmap
                if (oldValue.isMutable()) {  // 可以复用
                    resuablePool.add(new WeakReference<Bitmap>(oldValue, getReferenceQueue()));
                } else {
                    oldValue.recycle();
                }
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
//          初始化 磁盘缓存
        try {
            diskLruCache = DiskLruCache.open(new File(url), 1, 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //    引用对列
    ReferenceQueue referenceQueue;  //  gc 第一次  标记 第二次 回收  复用池中的图片
    Thread clearReferenceQueue;   //  如果发现 标记 直接回收
    boolean shutDown;

    //    用于主动监听 gc 回收
    private ReferenceQueue<Bitmap> getReferenceQueue() {
        if (referenceQueue == null) {
            referenceQueue = new ReferenceQueue();
            clearReferenceQueue = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!shutDown) {
                        try {
//                            remove  是带阻塞的 就是  如果referenceQueue 里面没数据 就会停留在这行不往下运行
                            Reference<Bitmap> reference = referenceQueue.remove();
                            Bitmap bitmap = reference.get();
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            clearReferenceQueue.start();
        }
        return referenceQueue;
    }

    // -------- 从缓存中获取
    public void putBitmapToMermary(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);

    }

    public Bitmap getBitmapMemory(String key) {
        return lruCache.get(key);
    }

    public void clearAllMemory() {
        lruCache.evictAll();
    }

    // --------
//    ==========从复用池中获取
    public Bitmap getResyable(int w, int h, int inSamepleSize) {
//  3.0 以下 不理会  因为 3.0 以下没有bitmap复用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return null;
        } else {
            Bitmap resuable = null;
            Iterator<WeakReference<Bitmap>> iterator = resuablePool.iterator();
            while (iterator.hasNext()) {
                Bitmap bitmap = iterator.next().get();
                if (bitmap != null) {
//                    可以复用
                    if (checkInBitmap(bitmap, w, h, inSamepleSize)) {
//                        使用了 复用池的bitma  就可以删除了
                        resuable = bitmap;
                        iterator.remove();
                        Log.e("eee", "在复用池找到了");
                        break;
                    }

                } else {
                    iterator.remove();
                }
            }
            return resuable;
        }
    }

    /**
     * 检查是否可以复用
     *
     * @param bitmap
     * @param w
     * @param h
     * @param inSamepleSize
     * @return
     */
    private boolean checkInBitmap(Bitmap bitmap, int w, int h, int inSamepleSize) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return bitmap.getWidth() == w && bitmap.getHeight() == h && inSamepleSize == 1;
        }
        if (inSamepleSize >= 1) {
            w /= inSamepleSize;
            h /= inSamepleSize;


        }
        int byteCount = w * h * getPixelsCount(bitmap.getConfig());
        return byteCount <= bitmap.getByteCount();
    }

    /**
     * 湖区bitmap的
     *
     * @param config
     * @return
     */
    private int getPixelsCount(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        }
        return 2;
       /* if (config==Bitmap.Config.ARGB_4444){
            return 2;
        }
        if (config==Bitmap.Config.RGB_565){
            return 2;
        }
        if (config==Bitmap.Config.ALPHA_8){
            return 1;
        }*/

    }

// ************磁盘缓存

    /**
     * 加入磁盘缓存
     */
    public void putdisCache(String key, Bitmap bitmap) {
        DiskLruCache.Snapshot snapshot = null;
        OutputStream os = null;
        try {
            snapshot = diskLruCache.get(key);
//              如果已经存在 不理会
            if (snapshot == null) {
                DiskLruCache.Editor edit = diskLruCache.edit(key);
                if (edit != null) {
                    os = edit.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                    edit.commit();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != snapshot) {
                snapshot.close();
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从磁盘获取
     */

    public Bitmap getFromDis(String key, Bitmap resuable) {
        DiskLruCache.Snapshot snapshot = null;
        Bitmap bitmap = null;
        try {
            snapshot = diskLruCache.get(key);
            if (null == snapshot) {
                return null;
            }
//        获取问津io 读取bitmap
            InputStream inputStream = snapshot.getInputStream(0);
//        解码图片  写入
            options.inMutable=true; //可复用内存
            options.inBitmap=resuable;
            bitmap = BitmapFactory.decodeStream(inputStream);
            if (null != bitmap) {
                lruCache.put(key, bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != snapshot) {
                snapshot.close();
            }
        }
        return bitmap;

    }
}
