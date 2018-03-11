package com.example.application.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.application.myapplication.R;
import com.example.application.myapplication.activity.AudioPlayerActivity;
import com.example.application.myapplication.activity.VideoPlayerActivity;
import com.example.application.myapplication.domain.MediaItem;
import com.example.application.myapplication.domain.NetVideoBean;
import com.example.application.myapplication.utils.Utils;

import java.util.ArrayList;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by 89565 on 2018/3/7.
 */

public class NetVideoAdapter extends RecyclerView.Adapter<NetVideoAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<NetVideoBean> netVideoBeans;
    private static final String TAG = "NetVideoAdapter";
    public NetVideoAdapter(ArrayList<NetVideoBean> netVideoBeans, Context context){
        mContext = context;
        this.netVideoBeans = netVideoBeans;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.rv_net_video_item,null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        NetVideoBean netVideoBean = netVideoBeans.get(position);
        holder.jz_video.setUp(netVideoBean.getUrl(),JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,netVideoBean.getTitle());
//        holder.jz_video.thumbImageView.setImageURI(Uri.parse(netVideoBean.getImgUrl()));
        Glide.with(mContext).load(netVideoBean.getImgUrl()).centerCrop().into(holder.jz_video.thumbImageView);
    }


    @Override
    public int getItemCount() {
        return netVideoBeans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        JZVideoPlayerStandard jz_video;

        public JZVideoPlayerStandard getJz_video() {
            return jz_video;
        }

        public void setJz_video(JZVideoPlayerStandard jz_video) {
            this.jz_video = jz_video;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            jz_video = itemView.findViewById(R.id.jz_video);
        }
    }
    //添加数据
    public void addItem(int position,NetVideoBean netVideoBean) {
        netVideoBeans.add(position, netVideoBean);
        notifyItemInserted(position);//通知演示插入动画
        notifyItemRangeChanged(position,0);//通知数据与界面重新绑定
    }

}
