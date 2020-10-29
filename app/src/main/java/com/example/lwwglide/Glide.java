package com.example.lwwglide;

import android.content.Context;

import com.example.lwwglide.request.BitmapRequest;

public class Glide {
    public static BitmapRequest with(Context context) {
        return new BitmapRequest(context);
    }
}
