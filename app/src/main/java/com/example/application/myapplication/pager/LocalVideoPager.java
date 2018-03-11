package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.example.application.myapplication.adapter.AudioAdapter;
import com.example.application.myapplication.domain.MediaItem;

import java.util.ArrayList;

/**
 * 本地视屏页面
 */

public class LocalVideoPager extends BasePager {

    private ArrayList<MediaItem> mediaItems;

    private int playMode = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                rv_audio.setLayoutManager(linearLayoutManager);
                audioAdapter = new AudioAdapter(mediaItems, mContext,playMode);
                rv_audio.setAdapter(audioAdapter);
            }
        }
    };
    private LinearLayoutManager linearLayoutManager;
    private AudioAdapter audioAdapter;


    public LocalVideoPager(Activity activity, Context context) {
        super(activity, context);
    }

    @Override
    public void initData() {
        super.initData();
        tv_title.setText("本地视频");
        linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        getDataFromLocal();
    }

    public void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                mediaItems = new ArrayList<>();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[]{
                        MediaStore.Video.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Video.Media.DATA,//视频路径
                        MediaStore.Video.Media.DURATION,//视频时长
                        MediaStore.Video.Media.SIZE,//视频大小
                };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);
                        String name = cursor.getString(0);
                        mediaItem.setName(name);
                        String data = cursor.getString(1);
                        mediaItem.setData(data);
                        long duration = cursor.getLong(2);
                        mediaItem.setDuration(duration);
                        long size = cursor.getLong(3);
                        mediaItem.setSize(size);
                    }
                    cursor.close();
                    System.out.println(mediaItems);
                }
                mHandler.sendEmptyMessage(10);
            }
        }.start();
    }
}
