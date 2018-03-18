package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.example.application.myapplication.adapter.NetVideoAdapter;
import com.example.application.myapplication.domain.NetVideoBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 网络音乐页面
 */

public class NetAudioPager extends BasePager {
    private static final String TAG = "NetAudioPager";
    private NetVideoBean netVideoBean;
    private ArrayList<NetVideoBean> netVideoBeans;
    private String url;

    private NetVideoAdapter netVideoAdapter;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            rv_audio.setLayoutManager(linearLayoutManager);
            netVideoAdapter = new NetVideoAdapter(netVideoBeans, mContext);
            rv_audio.setAdapter(netVideoAdapter);
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
        prossecHtml();
    }


    private void prossecHtml() {
        new Thread() {
            @Override
            public void run() {
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
                        netVideoBeans.add(netVideoBean);
                    }
                    mHandler.sendEmptyMessage(10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


}
