package com.org.woody.woodylibrary.view.pager;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by linfaxin on 2014/7/15 015.
 * Email: linlinfaxin@163.com
 * 方便使用的pagerAdapter
 */
public abstract class BasePagerAdapter<T> extends PagerAdapter {
    ArrayList<T> list;
    public BasePagerAdapter(T... tArray){
        this(Arrays.asList(tArray));
    }
    public BasePagerAdapter(List<T> list){
        this.list=new ArrayList<T>(list);
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);// 删除页卡
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(container, position);
        container.addView(view);// 添加页卡
        return view;
    }

    public abstract View getView(ViewGroup container, int position);// 这个方法用来实例化页卡
    @Override
    public int getCount() {
        return list.size();// 返回页卡的数量
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;// 官方提示这样写
    }
}