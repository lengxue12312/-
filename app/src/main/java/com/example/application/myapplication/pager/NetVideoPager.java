package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.application.myapplication.adapter.NetVideoAdapter;
import com.example.application.myapplication.domain.NetVideoBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

/**
 * 网络视屏页面
 */

public class NetVideoPager extends BasePager {

    private static final int PROSSE = 1;
    private static final int REFSHPROSSE = 2;
    private ArrayList<NetVideoBean> netVideoBeans;
    private static final String TAG = "NetVideoPager";
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage: "+netVideoBeans);
            switch (msg.what) {
                case PROSSE:
                    rv_audio.setLayoutManager(linearLayoutManager);
                    netVideoAdapter = new NetVideoAdapter(netVideoBeans, mContext);
                    rv_audio.setAdapter(netVideoAdapter);
                    break;
                case REFSHPROSSE:
                    netVideoAdapter.notifyDataSetChanged();
                    rv_audio.smoothScrollToPosition(0);
                    sr_refresh.setRefreshing(false);
                    isResh = false;
                    break;
            }
        }
    };
    private NetVideoBean netVideoBean;
    private LinearLayoutManager linearLayoutManager;
    private NetVideoAdapter netVideoAdapter;
    private String url;
    private boolean isResh = false;
    private View rootView;

    public NetVideoPager(Activity activity, Context context) {
        super(activity, context);
    }

    @Override
    public void initData() {
        super.initData();
        tv_title.setText("网络视频");
        netVideoBeans = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        url = "https://sv.baidu.com/";
        prossecHtml(url,isResh);
//        Log.i(TAG, "initData: "+netVideoBeans);
        sr_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isResh = true;
                prossecHtml(url,isResh);
            }
        });
        rv_audio.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//                recyclerView.getChildAdapterPosition()
                int itemCount = recyclerView.getAdapter().getItemCount();
                for (int i = 0; i < itemCount; i++) {
                    View childView = recyclerView.getChildAt(i);
                    int childAdapterPosition = recyclerView.getChildAdapterPosition(childView);
                    if (childAdapterPosition < firstVisibleItemPosition || childAdapterPosition > lastVisibleItemPosition) {
//                            NetVideoAdapter.ViewHolder viewHolder = new NetVideoAdapter.ViewHolder(childView);
//                            JZVideoPlayerStandard jz_video = viewHolder.getJz_video();
                            JZVideoPlayerStandard.releaseAllVideos();
                    }
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void prossecHtml(final String url, final boolean isResh) {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements div = document.getElementsByTag("div");
                    for (Element soc : div) {
                        netVideoBean = new NetVideoBean();
                        String videoUrl = soc.attr("data-vsrc");
                        String title = soc.attr("data-title");
                        netVideoBean.setTitle(title);
                        String imgUrl = soc.getElementsByTag("img").attr("data-src");
                        netVideoBean.setImgUrl(imgUrl);
                        if (videoUrl.contains(".mp4")) {
//                            Log.i(TAG, "run: " + soc.attr("data-vsrc")+soc.attr("data-title"));
                            if (!TextUtils.isEmpty(videoUrl)) {
                                String subUrl = videoUrl.substring(0, videoUrl.indexOf(".mp4"));
                                subUrl = subUrl+".mp4";
                                netVideoBean.setUrl(subUrl);
//                                Log.i(TAG, "subUrl: "+subUrl+".mp4"+"-"+title);
                            }
                        }
                        if (isResh) {
                            if (!TextUtils.isEmpty(netVideoBean.getUrl()) && !TextUtils.isEmpty(netVideoBean.getTitle())) {
                                msg.what = REFSHPROSSE;
                                netVideoBeans.add(0, netVideoBean);
                            }
                        } else {
                            if (!TextUtils.isEmpty(netVideoBean.getUrl())&&!TextUtils.isEmpty(netVideoBean.getTitle())) {
                                msg.what = PROSSE;
                                netVideoBeans.add(netVideoBean);
                            }
                        }
                    }
                    mHandler.sendMessage(msg);
//                    Log.i(TAG, "run: "+netVideoBeans);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
