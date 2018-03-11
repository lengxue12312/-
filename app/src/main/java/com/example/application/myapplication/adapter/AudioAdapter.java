package com.example.application.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.application.myapplication.R;
import com.example.application.myapplication.activity.AudioPlayerActivity;
import com.example.application.myapplication.activity.VideoPlayerActivity;
import com.example.application.myapplication.domain.MediaItem;
import com.example.application.myapplication.utils.Utils;

import java.util.ArrayList;

/**
 * Created by 89565 on 2018/3/7.
 */

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {
    private ArrayList<MediaItem> mediaItems;
    private Context context;
    private Utils utils;
    private int playMode;
    public AudioAdapter(ArrayList<MediaItem> mediaItems, Context mContext, int playMode){
        this.mediaItems = mediaItems;
        this.context = mContext;
        this.playMode = playMode;
        utils = new Utils();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.rv_audio_item,null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        MediaItem mediaItem = mediaItems.get(position);
        Glide.with(context).load(mediaItem.getData()).centerCrop().into(holder.iv_img);
        holder.tv_name.setText(mediaItem.getName());
        holder.tv_arits.setText(mediaItem.getArtist());
        holder.tv_size.setText((Formatter.formatFileSize(context,mediaItem.getSize())));
        holder.tv_dura.setText(utils.stringForTime((int) mediaItem.getDuration()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playMode == 2) {
                    Intent intent = new Intent(context, AudioPlayerActivity.class);
                    context.startActivity(intent);
                } else if (playMode == 1) {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("videolist",mediaItems);
                    intent.putExtras(bundle);
                    intent.putExtra("position",position);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_img;
        TextView tv_name;
        TextView tv_arits;
        TextView tv_size;
        TextView tv_dura;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_img = itemView.findViewById(R.id.iv_img);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_arits = itemView.findViewById(R.id.tv_arits);
            tv_size = itemView.findViewById(R.id.tv_size);
            tv_dura = itemView.findViewById(R.id.tv_dura);
            cardView = (CardView) itemView;
        }
    }
    //添加数据
    public void addItem(int position,MediaItem mediaItem) {
        mediaItems.add(position, mediaItem);
        notifyItemInserted(position);//通知演示插入动画
        notifyItemRangeChanged(position,mediaItems.size()-position);//通知数据与界面重新绑定
    }

}
