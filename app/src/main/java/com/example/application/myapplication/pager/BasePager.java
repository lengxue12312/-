package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.application.myapplication.R;
import com.example.application.myapplication.activity.MainActivity;
import com.example.application.myapplication.view.MarqueeTextView;

/**
 * Created by 89565 on 2018/3/6.
 */

public class BasePager {
    public Activity mActivity;
    public Context mContext;
    public FrameLayout fl_content;
    public View mRootView;
    public TextView tv_title;
    public RecyclerView rv_audio;
    public LinearLayout ll_search;
    public EditText et_serch;
    public ImageButton ib_search;
    public ProgressBar pb_down;
    public RelativeLayout rl_down;
    public MarqueeTextView tv_file_name;
    public TextView tv_file_size;
    public ImageButton ib_start;
    public ImageButton ib_pause;
    public ImageButton ib_cancle;
    public ImageButton ib_menu;
    public ImageButton ib_back;
    public WebView wv_view;
    public ProgressBar pb_loadurl;
    public SwipeRefreshLayout sr_refresh;
    private MainActivity mainActivity;
    public BasePager (Activity activity, Context context){
        mActivity = activity;
        mContext = context;
        mRootView = initView();
    }
    public View initView (){
        View view = View.inflate(mActivity, R.layout.base_pager,null);
        fl_content = view.findViewById(R.id.fl_content);
        tv_title = view.findViewById(R.id.tv_title);
        rv_audio = view.findViewById(R.id.rv_audio);
        ll_search = view.findViewById(R.id.ll_search);
        et_serch = view.findViewById(R.id.et_serch);
        ib_search = view.findViewById(R.id.ib_search);
        rl_down = view.findViewById(R.id.rl_down);
        tv_file_name = view.findViewById(R.id.tv_file_name);
        tv_file_size = view.findViewById(R.id.tv_file_size);
        pb_down = view.findViewById(R.id.pb_down);
        ib_start = view.findViewById(R.id.ib_start);
        ib_pause = view.findViewById(R.id.ib_pause);
        ib_cancle = view.findViewById(R.id.ib_cancle);
        wv_view = view.findViewById(R.id.wv_view);
        pb_loadurl = view.findViewById(R.id.pb_loadurl);
        ib_menu = view.findViewById(R.id.ib_menu);
        ll_search.setVisibility(View.GONE);
        ib_back = view.findViewById(R.id.ib_back);
        sr_refresh = view.findViewById(R.id.sr_refresh);
        mActivity = mainActivity;
        sr_refresh.setColorSchemeResources(R.color.colorPrimary);
        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl_left_menu = mainActivity.getDl_left_menu();
                if (dl_left_menu.isDrawerOpen(Gravity.START)) {
                    dl_left_menu.closeDrawers();
                } else {
                    dl_left_menu.openDrawer(Gravity.START);
                }

            }
        });
        return view;
    }
    public void initData(){
    }

}
