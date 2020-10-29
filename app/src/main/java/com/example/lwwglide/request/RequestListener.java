package com.example.lwwglide.request;

import android.graphics.Bitmap;

/**
 * 请求回调
 */
public interface RequestListener {
    void success(Bitmap bitmap);
    void onFaild();
}
