package com.example.application.myapplication.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.application.myapplication.R;
import com.example.application.myapplication.adapter.AudioAdapter;
import com.example.application.myapplication.domain.MediaItem;

import java.util.ArrayList;

/**
 * 本地音乐页面
 */

public class LocalAudioPager extends BasePager {
    private ArrayList<MediaItem> mediaItems;
    private AudioAdapter mAudioAdapter;

    private int playMode = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                rv_audio.setLayoutManager(linearLayoutManager);
                mAudioAdapter = new AudioAdapter(mediaItems, mContext,playMode);
                rv_audio.setAdapter(mAudioAdapter);
            }
        }
    };
    private LinearLayoutManager linearLayoutManager;

    public LocalAudioPager(Activity activity, Context context) {
        super(activity, context);
    }


    @Override
    public void initData() {
        tv_title.setText("本地音乐");
        linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        getDataFromLocal();
//        fl_content.addView(rv_audio);
    }

    public  void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                mediaItems = new ArrayList<>();
                ContentResolver contentResolver = mContext.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//音频在文件中的名称
                        MediaStore.Audio.Media.DURATION,//音频的总时长
                        MediaStore.Audio.Media.DATA,//音频在文件的路径
                        MediaStore.Audio.Media.SIZE,//音频的大小
                        MediaStore.Audio.Media.ARTIST,//音频的艺术家
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);
                        String name = cursor.getString(0);
                        mediaItem.setName(name);
                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);
                        String data = cursor.getString(2);
                        mediaItem.setData(data);
                        long size = cursor.getLong(3);
                        mediaItem.setSize(size);
                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
                System.out.println(mediaItems);
                mHandler.sendEmptyMessage(10);
            }
        }.start();
    }
}
