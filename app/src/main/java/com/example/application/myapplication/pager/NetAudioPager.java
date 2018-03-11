package com.example.application.myapplication.pager;

import android.app.Activity;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.application.myapplication.utils.Write;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.TbsVideo;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * 网络音乐页面
 */

public class NetAudioPager extends BasePager {


    private String url;
    private static final String TAG = "NetAudioPager";
    private Write write;
    private boolean canUseYunbo;

    public NetAudioPager(Activity activity, Context context) {
        super(activity, context);
    }

    @Override
    public void initData() {
        super.initData();
        write = new Write();
        ll_search.setVisibility(View.VISIBLE);
        wv_view.setVisibility(View.VISIBLE);
        ib_menu.setVisibility(View.GONE);
        ib_back.setVisibility(View.VISIBLE);
        setWebView();
        ib_search.setOnClickListener(new MyOnClickListener());
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wv_view.goBack();
            }
        });
        canUseYunbo = TbsVideo.canUseYunbo(mContext);
    }

    private void setWebView() {
        WebSettings settings = wv_view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.supportMultipleWindows();
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        wv_view.setDrawingCacheEnabled(true);
        wv_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        wv_view.loadUrl("https://sv.baidu.com/");
        wv_view.goBack();
        wv_view.goForward();
        wv_view.setWebViewClient(new MyWebViewClient());
        wv_view.setWebChromeClient(new MyChromeClient());
        wv_view.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.i(TAG, "onDownloadStart: " + url + "--" + userAgent + "--" + contentDisposition + "--" + mimetype + "---" + contentDisposition);
            }
        });
        ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et_serch.getText().toString().trim();
                wv_view.loadUrl(url);
            }
        });
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            pb_loadurl.setVisibility(View.VISIBLE);
            if (url.startsWith("http:") || url.startsWith("https:")) {
                view.loadUrl(url);
            }
            return true;
        }

//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
//            Log.i(TAG, "shouldInterceptRequest: "+webView+"--"+webResourceRequest+"--"+bundle.toString());
//            return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
//        }


        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            String url = webResourceRequest.getUrl().toString();
            if (!TextUtils.isEmpty(url)&&url.contains(".mp4")) {
                Log.i(TAG, "shouldInterceptRequest: "+webView+"--"+url);
                String newAttr = url.substring(0, url.indexOf(".mp4"));
                Log.i(TAG, "newrun: "+newAttr+".mp4");
                String playUrl = newAttr+".mp4";
                if (canUseYunbo) {
//                    TbsVideo.openVideo(mContext,playUrl);
                }
            }
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            Log.i(TAG, "onPageFinished: " + url);
            super.onPageFinished(view, url);
            et_serch.setText(url);
            prossecHtml(url);
        }
    }
    private void prossecHtml(final String url) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements html = document.select("html");
                    Elements div = document.getElementsByTag("div");

                    for (Element soc : div) {
                        String videoUrl = soc.attr("data-vsrc");
//                        TbsVideo.openVideo(mContext,videoUrl);
                        String title = soc.attr("data-title");
                        if (videoUrl.contains(".mp4")) {
                            Log.i(TAG, "soc--: "+soc);
                            Log.i(TAG, "run: " + soc.attr("data-vsrc")+soc.attr("data-title"));
                            Log.i(TAG, "data-src: "+soc.getElementsByTag("img").attr("data-src"));
                            if (!TextUtils.isEmpty(videoUrl)) {
                                String subUrl = videoUrl.substring(0, videoUrl.indexOf(".mp4"));
                                Log.i(TAG, "subUrl: "+subUrl+".mp4"+"-"+title);
                            }
                        }
//                        Elements a = soc.select("a");
//                        for (Element a1: a) {
////                            Log.i(TAG, "run: "+a1);
//                            String attrUrl = a1.attr("abs:data-playurl").trim();
//                            String attrTitle = a1.attr("abs:data-title");
//
//                            if (!TextUtils.isEmpty(attrUrl)) {
//                                String newAttr = attrUrl.substring(0, attrUrl.indexOf(".mp4"));
////                                Log.i(TAG, "newrun: "+newAttr+".mp4"+"-"+attrTitle);
//                            }
//
//                        }
//                        Log.i(TAG, "run: "+a);
                    }
//                    Log.i(TAG, "run: " + document);
//                    Elements select = document.select("source");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    class MyChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            pb_loadurl.setProgress(newProgress);
            if (newProgress == 100) {
                pb_loadurl.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            url = et_serch.getText().toString().trim();
            pb_loadurl.setVisibility(View.VISIBLE);
            wv_view.loadUrl(url);
        }
    }
}
