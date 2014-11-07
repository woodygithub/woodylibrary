/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package com.org.woody.woodylibrary.view.list;

import android.R.integer;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.org.woody.woodylibrary.R;

public class XListViewFooter extends LinearLayout {
	/**正常状态(可以点击)*/
	public final static int STATE_NORMAL = 0;
	/**准备就绪状态(松开即加载)*/
	public final static int STATE_READY = 1;
	/**载入中...*/
	public final static int STATE_LOADING = 2;
	/**没有更多项目*/
	public final static int STATE_NOMORE = 3;
	/**隐藏*/
	public final static int STATE_GONE = 4;

	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;
	
	public void setTextColor(int color){
		mHintView.setTextColor(color);
	}
	
	public void setTextColor(ColorStateList color){
		mHintView.setTextColor(color);
	}
	
	public XListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private int state;
	public int getState(){
		return state;
	}
	/**
	 * 改变显示状态
	 * @param state
	 */
	public void setState(int state) {
		this.state=state;
		if (state == STATE_READY) {
			mContentView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			mHintView.setText(R.string.xlistview_footer_hint_ready);
		} else if (state == STATE_LOADING) {
			mContentView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_header_hint_loading);
		}else if (state == STATE_NOMORE) {
			mContentView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			mHintView.setText(R.string.xlistview_footer_hint_nomore);
		}else if (state == STATE_GONE) {
			mContentView.setVisibility(View.GONE);
		} else {
			mContentView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			mHintView.setText(R.string.xlistview_footer_hint_normal);
		}
	}
	
	public void setBottomMargin(int height) {
		if (height < 0) return ;
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}
	
	public int getBottomMargin() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		return lp.bottomMargin;
	}
	
	
	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.xlistview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mContentView = moreView.findViewById(R.id.xlistview_footer_content);
		mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
		mHintView = (TextView)moreView.findViewById(R.id.xlistview_footer_hint_textview);
	}
	
	
}
