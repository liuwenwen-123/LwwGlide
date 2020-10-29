package com.example.lwwglide.cache;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 *  图片压缩
 */

public class ImageResize {
    /**
     * 优化  图片
     *
     * @param context  上下问
     * @param id       图片id
     * @param maxW     优化后 最大的宽
     * @param maxH     优化后 最大的高
     * @param hasAlpha 是否透明
     * @return
     */
    public static Bitmap resizeBitMap(Context context, int id, int maxW, int maxH,
                                      Boolean hasAlpha, Bitmap resuable) {
//       获取资源 对象
        Resources resources = context.getResources();
        BitmapFactory.Options options = new BitmapFactory.Options();
//        打开设置
        options.inJustDecodeBounds = true;
        //    通过 decodeResource  这个方式获取的资源   缩放形式 都是系统柜默认完成的
        BitmapFactory.decodeResource(resources, id, options);
//      使用了  BitmapFactory.Options  只是解码出  图片的相关的参数  这样调用只是创建了 bitmap对象 并没有加载到内存
        int w = options.outWidth;
        int h = options.outHeight;
//          社会缩放系数  只能去2的n次方
        options.inSampleSize = calcuteImSamesie(w, h, maxW, maxH);
        if (!hasAlpha) { // 不需要 透明度
//             取 图片格式  默认 argb_888()
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
//        关闭设置
        options.inJustDecodeBounds = false;
//    设置  服用内存块
        options.inMutable=true;
        options.inBitmap=resuable;
        return    BitmapFactory.decodeResource(resources, id, options);

    }

    /**
     * 压缩 图片
     *
     * @param w    真实图片的    宽
     * @param h    真实图片的    高
     * @param mxhW 需要的   宽
     * @param maxH 需要的   高
     * @return
     */
    public static int calcuteImSamesie(int w, int h, int mxhW, int maxH) {
        int isSamepleSize = 1;
        if (w > mxhW && h > maxH) {
            isSamepleSize = 2;
            while (w / isSamepleSize > mxhW && h / isSamepleSize > maxH) {
                isSamepleSize *= 2;
            }
        }
        return  isSamepleSize;
    }
}
