package com.example.application.myapplication.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.application.IMusicPlayerService;
import com.example.application.myapplication.R;
import com.example.application.myapplication.domain.MediaItem;
import com.example.application.myapplication.service.AudioPlayerService;
import com.example.application.myapplication.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private Utils utils;
    private TextView tvName;
    private TextView tvArtist;
    private LinearLayout llBottom;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private boolean notification;
    private int position;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        seekbarAudio.setProgress(currentPosition);
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));
                        mHandler.removeMessages(PROGRESS);
                        mHandler.sendEmptyMessageDelayed(PROGRESS,1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!notification) {
                        service.openAudio(position);
                    } else {
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private IMusicPlayerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getSupportActionBar().hide();
        initData();
        findViews();
        getData();
        bindAndStartService();
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction("com.ithem.mobileplayer_OPENAUDIO");
        bindService(intent,con, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void getData() {
        notification = getIntent().getBooleanExtra("Notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    private void findViews() {
        setContentView(R.layout.activity_audio_player);
        tvName = findViewById( R.id.tv_name );
        tvArtist = findViewById( R.id.tv_arits);
        llBottom = findViewById( R.id.ll_bottom );
        tvTime = findViewById( R.id.tv_time );
        seekbarAudio = findViewById( R.id.seekbar_audio );
        btnAudioPre = findViewById( R.id.btn_audio_pre );
        btnAudioStartPause = findViewById( R.id.btn_audio_start_pause );
        btnAudioNext = findViewById( R.id.btn_audio_next );

        btnAudioPre.setOnClickListener( this );
        btnAudioStartPause.setOnClickListener( this );
        btnAudioNext.setOnClickListener( this );
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    @Override
    public void onClick(View v) {
        if ( v == btnAudioPre ) {
            // Handle clicks for btnAudioPre
            if (service != null) {
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if ( v == btnAudioStartPause ) {
            // Handle clicks for btnAudioStartPause
            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        service.pause();
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                    } else {
                        service.start();
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if ( v == btnAudioNext ) {
            // Handle clicks for btnAudioNext
            if (service != null) {
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class MyRecevier extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem) {
        showViewData();
    }

    private void initData() {
        utils = new Utils();
        EventBus.getDefault().register(this);
    }
    private void showViewData() {
        try {
            tvArtist.setText(service.getArtist());
            tvName.setText(service.getName());
            seekbarAudio.setMax(service.getDuration());

            mHandler.sendEmptyMessage(PROGRESS);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (con != null) {
            unbindService(con);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
