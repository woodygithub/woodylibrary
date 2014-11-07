package com.org.woody.woodylibrary.view.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by linfaxin on 2014/7/9 009.
 * Email: linlinfaxin@163.com
 * 直接在xml中定义好child page就ok了，省去繁琐的代码，但是page过多会影响内存
 */
public class XMLViewPager extends ViewPager{
    ViewsPagerAdapter pagerAdapter=new ViewsPagerAdapter();
    public XMLViewPager(Context context) {
        super(context);
        init();
    }

    public XMLViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

//    public XMLViewPager(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init();
//    }

//    @Override
//    public void addView(View child, int index, ViewGroup.LayoutParams params) {
//        super.addView(child, index, params);
//    }

    @Override
    public void addView(View child) {
        pagerAdapter.addView(child);
    }

//    @Override
//    public void addView(View child, int index) {
//        super.addView(child, index);
//    }
//
//    @Override
//    public void addView(View child, int width, int height) {
//        super.addView(child, width, height);
//    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        pagerAdapter.addView(child);
    }

    private void init(){
        setAdapter(pagerAdapter);
//        setOrientation(HORIZONTAL);
//        if(isInEditMode()) return;
//        ArrayList<View> views=new ArrayList<View>();
//        for(int i=0,count=getChildCount();i<count;i++){
//            views.add(getChildAt(i));
//        }
//        removeAllViews();
//        ViewPager viewPager=new ViewPager(getContext());
//        viewPager.setAdapter(new ViewsPagerAdapter(views));
//        addView(viewPager, -1, -1);
    }
//    public ViewPager getViewPager(){
//        return (ViewPager)getChildAt(0);
//    }
}
