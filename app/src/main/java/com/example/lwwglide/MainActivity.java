package com.example.lwwglide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.lwwglide.cache.ImageCache;

public class MainActivity extends AppCompatActivity {
    String url = "http://qzonestyle.gtimg.cn/qzone/app/weishi/client/testimage/64/1.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView viewById = findViewById(R.id.img);
        ImageCache.getInstance().init(this, "data/data/com.example.lwwglide/imgcache");
        final ListView listView = findViewById(R.id.lv);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(MainActivity.this).load(url).
                        setResId(R.drawable.ic_launcher_background).into(viewById);
                listView.setAdapter(new ImgAdapter());
            }
        });


    }

    class  ImgAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 30;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = View.inflate(MainActivity.this, R.layout.item_img, null);
            ImageView imageView = inflate.findViewById(R.id.item_img);
            Glide.with(MainActivity.this).load(url).
                    setResId(R.drawable.ic_launcher_background).into(imageView);
            return inflate;
        }
    }
}