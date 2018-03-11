package com.example.application.myapplication.fragment;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.application.myapplication.R;
import com.example.application.myapplication.pager.BasePager;
import com.example.application.myapplication.pager.LocalAudioPager;
import com.example.application.myapplication.pager.LocalVideoPager;
import com.example.application.myapplication.pager.NetAudioPager;
import com.example.application.myapplication.pager.NetVideoPager;

import java.util.ArrayList;

import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by 89565 on 2018/3/6.
 */

public class ContentFragment extends BaseFragment {

    private ViewPager vp_content;
    private RadioGroup rg_group;
    private ArrayList<BasePager> mPagers;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.content_fragment, null);
        vp_content = view.findViewById(R.id.vp_content);
        rg_group = view.findViewById(R.id.rg_group);
        return view;
    }

    @Override
    public void initData() {
        mPagers = new ArrayList<>();
        mPagers.add(new LocalVideoPager(mActivity,getContext()));
        mPagers.add(new NetVideoPager(mActivity,getContext()));
        mPagers.add(new LocalAudioPager(mActivity,getContext()));
        mPagers.add(new NetAudioPager(mActivity,getContext()));
        vp_content.setAdapter(new ViewPagerAdapter());
        rg_group.setOnCheckedChangeListener(new RadioGroupOnCheckedChangeListener());
        vp_content.setOnPageChangeListener(new ViewPagerOnPageChangeListener());
        mPagers.get(0).initData();
    }

    class ViewPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            BasePager basePager = mPagers.get(position);
            basePager.initData();
            JZVideoPlayerStandard.releaseAllVideos();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPagers.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            BasePager basePager = mPagers.get(position);
            View view = basePager.mRootView;
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    class RadioGroupOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_local_video:
                    vp_content.setCurrentItem(0,false);
                    break;
                case R.id.rb_net_video:
                    vp_content.setCurrentItem(1,false);
                    break;
                case R.id.rb_local_audio:
                    vp_content.setCurrentItem(2,false);
                    break;
                case R.id.rb_net_audio:
                    vp_content.setCurrentItem(3,false);
                    break;
            }
        }
    }
}
