package com.example.application.myapplication.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import com.example.application.IMusicPlayerService;
import com.example.application.myapplication.R;
import com.example.application.myapplication.activity.AudioPlayerActivity;
import com.example.application.myapplication.domain.MediaItem;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayerService extends Service {
    public static final String OPENAUDIO = "com.ithem.mobileplayer_OPENAUDIO";
    private ArrayList<MediaItem> mediaItems;
    private MediaPlayer mediaPlayer;
    private int position;
    private MediaItem mediaItem;

    public AudioPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDataFromLocal();
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        AudioPlayerService service = AudioPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }
    };

    private void openAudio(int position) {
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private NotificationManager manager;
    private void start() {
        mediaPlayer.start();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,AudioPlayerActivity.class);
        intent.putExtra("Notification",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("音乐")
                .setContentText("正在播放"+getName())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1,notification);
    }

    private void pause() {
        mediaPlayer.pause();
        manager.cancel(1);
    }

    private void stop() {
    }

    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    private int getDuration() {
        return mediaPlayer.getDuration();
    }

    private String getArtist() {
        return mediaItem.getArtist();
    }

    private String getName() {
        return mediaItem.getName();
    }

    private String getAudioPath() {
        return mediaItem.getData();
    }

    private void next() {
        position++;
        if (position > mediaItems.size()) {
            position = 0;
        }
        openAudio(position);
    }


    private void pre() {
        position--;
        if (position < 0) {
            position = mediaItems.size() - 1;
        }
        openAudio(position);
    }

    private void setPlayMode(int playMode) {

    }

    private int getPlayMode() {
        return 0;
    }

    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                mediaItems = new ArrayList<>();
                ContentResolver contentResolver = getContentResolver();
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
                System.out.println("----"+mediaItems);
            }
        }.start();
    }
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            EventBus.getDefault().post(mediaItem);
            start();
        }
    }
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }
    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }
}
