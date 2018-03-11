package com.example.application.myapplication.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.example.application.myapplication.R;
import com.example.application.myapplication.fragment.ContentFragment;
import com.example.application.myapplication.pager.LocalAudioPager;

import org.xutils.x;

import cn.jzvd.JZVideoPlayerStandard;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_CONTENT = "tag_content";
    private static final String TAG = "MainActivity";


    private NavigationView nav_menu;
    private DrawerLayout dl_left_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        x.Ext.init(getApplication());
        initUI();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            initFragment();
        }
    }
    private void initUI() {
        nav_menu = findViewById(R.id.nav_menu);
        dl_left_menu = findViewById(R.id.dl_left_menu);
    }

    public DrawerLayout getDl_left_menu() {
        return dl_left_menu;
    }

    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main,new ContentFragment(),TAG_CONTENT);
        fragmentTransaction.commit();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        initFragment();
                } else {
                    Toast.makeText(this, "你拒绝了这个权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        JZVideoPlayerStandard.releaseAllVideos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }
}
