package com.example.application.myapplication.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import com.example.application.myapplication.R;

/**
 * 闪屏页面
 */
public class SplashActivity extends AppCompatActivity {

    private LinearLayout ll_bg;
    private AnimationSet set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        initUI();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        ll_bg = findViewById(R.id.ll_bg);
        setAnim();
        ll_bg.setAnimation(set);
    }

    /**
     * 设置动画
     */
    private void setAnim() {
        ScaleAnimation sa = new ScaleAnimation(0,1,0,1, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        sa.setDuration(1000);
        sa.setFillAfter(true);

        RotateAnimation ra = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(1000);
        ra.setFillAfter(true);

        AlphaAnimation aa = new AlphaAnimation(0,1);
        aa.setDuration(2000);
        aa.setFillAfter(true);

        set = new AnimationSet(true);
        set.addAnimation(sa);
        set.addAnimation(ra);
        set.addAnimation(aa);

        set.setAnimationListener(new MyAnimationListener());
    }

    /**
     * 设置动画监听
     */
    class MyAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Intent intent = new Intent(SplashActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
