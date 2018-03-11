package com.example.application.myapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.myapplication.R;
import com.example.application.myapplication.domain.MediaItem;
import com.example.application.myapplication.utils.Utils;
import com.example.application.myapplication.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VideoPlayerActivity extends Activity implements View.OnClickListener {
    private static final int DEFAULE_SCREEN = 2;
    private static final int PROGRESS = 1;
    private static final int FULL_SCREEN = 1;
    private static final int HIDE_MEDIACONTROLLER = 2;
    private static final int SHOW_NETSPEED = 3;
    private VideoView vvVideo;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView systemBatter;
    private TextView systemTime;
    private Button btnVoice;
    private SeekBar seekbarVioce;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrent;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private TextView tvBufferNetspeed;
    private TextView tv_loading_netspeed;
    private LinearLayout llLoading;
    private LinearLayout llBuffer;
    private RelativeLayout media_controller;

    private ArrayList<MediaItem> mediaItems;
    private int position;
    private int videoWidth;
    private int videoHeight;
    private Utils utils;
    private Uri uri;
    private MyReceiver myReceiver;
    private GestureDetector detector;
    private int screenwidth;
    private int screenHeight;
    private AudioManager am;
    private int currentVoice;
    private int maxVoice;

    private boolean isUserSystem = false;
    private int precurrentPosition;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_NETSPEED:
                    String netSpeed = utils.getNetSpeed(VideoPlayerActivity.this);
                    tv_loading_netspeed.setText("玩命加载中..." + netSpeed);
                    tvBufferNetspeed.setText("玩命缓冲中..." + netSpeed);
                    removeMessages(SHOW_NETSPEED);
                    sendEmptyMessageDelayed(SHOW_NETSPEED, 2000);
                    break;
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
                case PROGRESS:
                    int currentPosition = vvVideo.getCurrentPosition();
                    tvCurrent.setText(utils.stringForTime(currentPosition));
                    systemTime.setText(getSystemTime());
                    seekbarVideo.setProgress(currentPosition);
                    if (isNetUri) {
                        int bufferPercentage = vvVideo.getBufferPercentage();
                        int totalBuffer = bufferPercentage * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        seekbarVideo.setSecondaryProgress(0);
                    }
                    if (!isUserSystem && vvVideo.isPlaying()) {
                        if (vvVideo.isPlaying()) {
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                llBuffer.setVisibility(View.VISIBLE);
                            } else {
                                llBuffer.setVisibility(View.GONE);
                            }
                        } else {
                            llBuffer.setVisibility(View.GONE);
                        }
                    }
                    precurrentPosition = currentPosition;
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
            }
        }
    };

    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    private boolean isNetUri;
    private boolean isShowMediaController = false;
    private boolean isFullScreen = false;
    private boolean isMute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        setListener();

        getData();
        setData();
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());
            isNetUri = utils.isNetUri(mediaItem.getData());
            vvVideo.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tvName.setText(uri.toString());
            isNetUri = utils.isNetUri(uri.toString());
            vvVideo.setVideoURI(uri);
        } else {
            Toast.makeText(this, "亲，你没有传递数据", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void initData() {
        utils = new Utils();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReceiver, intentFilter);
        detector = new GestureDetector(this, new MySimpleOnGestureListener());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenwidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            startAndPause();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setFullandDefault();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isShowMediaController) {
                hideMediaController();
                mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            } else {
                showMediaController();
                mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    private void setFullandDefault() {
        if (isFullScreen) {
            setVideoType(FULL_SCREEN);
        } else {
            setVideoType(DEFAULE_SCREEN);
        }
    }

    private void startAndPause() {
        if (vvVideo.isPlaying()) {
            vvVideo.pause();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            vvVideo.start();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            systemBatter.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            systemBatter.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            systemBatter.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            systemBatter.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            systemBatter.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            systemBatter.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            systemBatter.setImageResource(R.drawable.ic_battery_100);
        } else {
            systemBatter.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void setListener() {
        vvVideo.setOnPreparedListener(new MyOnPreparedListener());
        vvVideo.setOnCompletionListener(new MyOnCompletionListener());
        vvVideo.setOnErrorListener(new MyOnErrorListener());
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVioce.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
        vvVideo.setOnInfoListener(new MyOnInfoListener());
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    llBuffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    llBuffer.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    private void updataVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVioce.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVioce.setProgress(progress);
            currentVoice = progress;
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                vvVideo.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
            vvVideo.start();
            int duration = vvVideo.getDuration();
            tvDuration.setText(utils.stringForTime(duration));
            seekbarVideo.setMax(duration);
            hideMediaController();
            llLoading.setVisibility(View.GONE);
            setVideoType(DEFAULE_SCREEN);
            mHandler.sendEmptyMessage(PROGRESS);
        }
    }

    private void setVideoType(int defauleScreen) {
        switch (defauleScreen) {
            case FULL_SCREEN:
                vvVideo.setVideoSize(screenwidth, screenHeight);
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_default_selector);
                isFullScreen = false;
                break;
            case DEFAULE_SCREEN:
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;
                int width = screenwidth;
                int height = screenHeight;
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                vvVideo.setVideoSize(width, height);
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_full_selector);
                isFullScreen = true;
                break;
        }
    }

    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isShowMediaController = false;
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            startVitamioPlayer();
            return false;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNextVideo();
        }
    }

    private void playNextVideo() {
        btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                llLoading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                vvVideo.setVideoPath(mediaItem.getData());
                setButtonState();
            }
        } else if (uri != null) {
            setButtonState();
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);
            } else if (mediaItems.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }

    private void startVitamioPlayer() {
        if (vvVideo != null) {
            vvVideo.stopPlayback();
        }
        Intent intent = new Intent(this, VitamioVideoPlayerActivity.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    private void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void findViews() {
        setContentView(R.layout.activity_video_player);
        vvVideo = findViewById(R.id.vv_video);
        llTop = findViewById(R.id.ll_top);
        tvName = findViewById(R.id.tv_name);
        systemBatter = findViewById(R.id.system_batter);
        systemTime = findViewById(R.id.system_time);
        btnVoice = findViewById(R.id.btn_voice);
        seekbarVioce = findViewById(R.id.seekbar_vioce);
        btnSwitchPlayer = findViewById(R.id.btn_switch_player);
        llBottom = findViewById(R.id.ll_bottom);
        tvCurrent = findViewById(R.id.tv_current);
        seekbarVideo = findViewById(R.id.seekbar_video);
        tvDuration = findViewById(R.id.tv_duration);
        btnExit = findViewById(R.id.btn_exit);
        btnVideoPre = findViewById(R.id.btn_video_pre);
        btnVideoStartPause = findViewById(R.id.btn_video_start_pause);
        btnVideoNext = findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen = findViewById(R.id.btn_video_switch_screen);
        tvBufferNetspeed = findViewById(R.id.tv_buffer_netspeed);
        tv_loading_netspeed = findViewById(R.id.tv_loading_netspeed);
        llLoading = findViewById(R.id.ll_loading);
        llBuffer = findViewById(R.id.ll_buffer);
        media_controller = findViewById(R.id.media_controller);

        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);

        seekbarVioce.setMax(maxVoice);
        seekbarVioce.setProgress(currentVoice);
        mHandler.sendEmptyMessage(SHOW_NETSPEED);
    }

    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // Handle clicks for btnVoice
            isMute = !isMute;
            updataVoice(currentVoice,isMute);
        } else if (v == btnSwitchPlayer) {
            // Handle clicks for btnSwitchPlayer
            showSwitchVitamioPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            playNextVideo();
        } else if (v == btnVideoSwitchScreen) {
            // Handle clicks for btnVideoSwitchScreen
            setFullandDefault();
        }
        mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
    }

    private void playPreVideo() {
        btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                llLoading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                vvVideo.setVideoPath(mediaItem.getData());
                setButtonState();
            }
        } else if (uri != null) {
            setButtonState();
        }
    }

    private void showSwitchVitamioPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提醒您:");
        builder.setMessage("当视频播放有问题时，请尝试切换播放器播放!!!!");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }
        super.onDestroy();
    }
    private float startY;
    private float mVol;
    private float touchRang;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenwidth);
                mHandler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;
                if (endX < screenwidth / 2) {
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(10);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-10);
                    }
                } else if (endX > screenwidth / 2) {
                    float delta = (distanceY / touchRang) * maxVoice;
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        updataVoice(voice, isMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setBrightness(int brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updataVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updataVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
