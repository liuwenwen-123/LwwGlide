package com.example.lwwglide.request;

import android.content.Context;
import android.widget.ImageView;

import com.example.lwwglide.MD5Utils;

import java.lang.ref.SoftReference;

/**
 *   1:创建  请求对象   链式调用   通过链条的方式来 初始化 对象
 */
public class BitmapRequest {
    private Context context;
    private String url;
    private SoftReference<ImageView> imageView;
    private RequestListener requestListener;
    //    请求表示 防止 图片错位
    private String urlMD5;
    //    展位图
    private int resId;

    public BitmapRequest(Context context) {
        this.context = context;
    }

    public BitmapRequest load(String url) {
        this.url = url;
        this.urlMD5 = MD5Utils.toMD5(url);
        return this;
    }

    public void into(ImageView imageView) {
//         设置tag控件
        imageView.setTag(urlMD5);
        this.imageView = new SoftReference<>(imageView);
//        调用 into  就开始请求 图片了 所以加入 请求队列
        RequetManager.getInstance().addBitmapRequest(this);
    }

    public BitmapRequest loading(int resid) {
        this.resId = resid;
        return this;
    }

    public BitmapRequest lodingListener(RequestListener requestListener) {
        this.requestListener = requestListener;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public  ImageView  getImageView() {
        return imageView.get();
    }

    public void setImageView(SoftReference<ImageView> imageView) {
        this.imageView = imageView;
    }

    public RequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public String getUrlMD5() {
        return urlMD5;
    }

    public void setUrlMD5(String urlMD5) {
        this.urlMD5 = urlMD5;
    }

    public int getResId() {
        return resId;
    }

    public BitmapRequest setResId(int resId) {
        this.resId = resId;
        return this;
    }
}
