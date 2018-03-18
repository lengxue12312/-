package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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

import cn.jzvd.JZVideoPlayerStandard;

/**
 * 网络音乐页面
 */

public class NetAudioPager extends BasePager {
    private static final String TAG = "NetAudioPager";
    private static final int REFSHPROSSE = 2;
    private static final int PROSSE = 1;
    private NetVideoBean netVideoBean;
    private ArrayList<NetVideoBean> netVideoBeans;
    private String url;
    private boolean isRefsh = false;
    private NetVideoAdapter netVideoAdapter;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFSHPROSSE:
                    netVideoAdapter.notifyDataSetChanged();
                    rv_audio.smoothScrollToPosition(0);
                    sr_refresh.setRefreshing(false);
                    isRefsh = false;
                    break;
                case PROSSE:
                    rv_audio.setLayoutManager(linearLayoutManager);
                    netVideoAdapter = new NetVideoAdapter(netVideoBeans, mContext);
                    rv_audio.setAdapter(netVideoAdapter);
                    break;
            }

        }
    };
    private LinearLayoutManager linearLayoutManager;



    public NetAudioPager(Activity activity, Context context) {
        super(activity, context);
    }

    @Override
    public void initData() {
        super.initData();
        tv_title.setText("音乐电台");
        netVideoBeans = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        url = "http://m.kuwo.cn/newh5/mv/list?id=236682731";
        prossecHtml(isRefsh);
        sr_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefsh = true;
                prossecHtml(isRefsh);
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


    private void prossecHtml(final boolean isRefsh) {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    Document document = Jsoup.connect(url).get();
//                    Elements ul = document.getElementsByTag("li");
                    Elements elementsByClass = document.getElementsByTag("li");
                    for (Element video : elementsByClass) {
                        netVideoBean = new NetVideoBean();
//                        Log.i(TAG, "run: "+video);
                        String onclick = video.attr("onclick");
                        String mid = onclick.substring(onclick.lastIndexOf("/"), onclick.lastIndexOf("'"));
                        String url = "http://m.kuwo.cn/newh5/mv/"+mid;
                        Log.i(TAG, "run: "+url);
                        netVideoBean.setUrl(url);
                        String src = video.getElementsByTag("img").attr("src");
                        Log.i(TAG, "run: "+src);
                        netVideoBean.setImgUrl(src);
//                        Elements select = video.getElementsByTag("p").select("singTexUp2");
                        Elements singTexUp2 = video.getElementsByClass("singTexUp2");
                        for (Element sing: singTexUp2) {
//                            Log.i(TAG, "run--: "+sing);
                            String text = sing.text();
                            Log.i(TAG, "run: --"+text);
                            netVideoBean.setTitle(text);
                        }
//                        Log.i(TAG, "run: "+select);
                        if (isRefsh) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


}
