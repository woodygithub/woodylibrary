package com.org.woody.woodylibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**子view有左右滑动的ScrollView，防止子view与ScrollView滑动冲突 */
public class ChildHScrollScrollView extends ScrollView {
    
	public ChildHScrollScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ChildHScrollScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChildHScrollScrollView(Context context) {
		super(context);
	}
    private long touchDownTime;
    private float touchDownX;
    private float touchDownY;
    Boolean canScrollY;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
        	touchDownTime = System.currentTimeMillis();
        	touchDownX=ev.getX();
        	touchDownY=ev.getY();
        	canScrollY=null;
        }
        if(System.currentTimeMillis()-touchDownTime<50) return super.onInterceptTouchEvent(ev);
        if(canScrollY!=null) return canScrollY;
        float distanceX = ev.getX()-touchDownX;
        float distanceY = ev.getY()-touchDownY;
        canScrollY = (Math.abs(distanceY) >= Math.abs(distanceX));
		
        return super.onInterceptTouchEvent(ev) && canScrollY ;
	}
}
