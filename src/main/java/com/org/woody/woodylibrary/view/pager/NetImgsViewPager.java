package com.org.woody.woodylibrary.view.pager;

import java.util.ArrayList;

import com.org.woody.woodylibrary.bitmap.BitmapManager;
import com.org.woody.woodylibrary.view.CircleProgressBarView;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**ViewPager的网络画廊组件，带进度的展示 */
public class NetImgsViewPager extends ViewPager {
	public NetImgsViewPager(Context context) {
		super(context);
	}
	public NetImgsViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	/**绑定一个指示器，必须在setAdapter/setImgs之后调用，必须在setOnPageChangeListener之后调用  */
	public void bindIndicator(final PointIndicator pointIndicator){
		pointIndicator.setSize(getAdapter().getCount());
		final OnPageChangeListener lastListener = onPageChangeListener;
		setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageSelected(int position) {
				pointIndicator.check(position);
				if(lastListener!=null) lastListener.onPageSelected(position);
			}
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if(lastListener!=null) lastListener.onPageScrolled(arg0, arg1, arg2);
			}
			public void onPageScrollStateChanged(int arg0) {
				if(lastListener!=null) lastListener.onPageScrollStateChanged(arg0);
			}
		});
	}
	ScaleType imageScaleType;
	public void setImageScaleType(ScaleType scaleType){
		imageScaleType=scaleType;
	}
	private OnPageChangeListener onPageChangeListener;
	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		super.setOnPageChangeListener(listener);
		onPageChangeListener=listener;
	}
	public OnPageChangeListener getOnPageChangeListener() {
		return onPageChangeListener;
	}
	public void setImgs(final String... mImgUrls){
		if(mImgUrls==null) return;
		setAdapter(new NetImgViewPagerAdapter(mImgUrls));
	}
	public void setImgs(final ArrayList<String> mImgUrlList){
		if(mImgUrlList==null) return;
		setAdapter(new NetImgViewPagerAdapter(mImgUrlList.toArray(new String[mImgUrlList.size()])));
	}
	class NetImgViewPagerAdapter extends PagerAdapter {
		private String[] mImgUrls;
		public NetImgViewPagerAdapter(String[] mImgUrls) {
			this.mImgUrls = mImgUrls;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);// 删除页卡
		}
		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			FrameLayout pagerView = new FrameLayout(getContext());
			final CircleProgressBarView progressView = new CircleProgressBarView(getContext());
			int progressSize = (int) (60 * getContext().getResources().getDisplayMetrics().density);
			pagerView.addView(progressView, new FrameLayout.LayoutParams(progressSize, progressSize, Gravity.CENTER));
			ImageView imgView = new ImageView(getContext());
			imgView.setScaleType(ScaleType.FIT_CENTER);
			pagerView.addView(imgView, -1, -1);
			
			BitmapManager.bindView(imgView,
					null, null, null, mImgUrls[position], new BitmapManager.BitmapLoadingListener() {
						@Override
						public void onBitmapLoading(int progress) {
							progressView.setProgress(progress);
						}
						@Override
						public void onBitmapLoadFinish(Bitmap bitmap, boolean isLoadSuccess) {
							progressView.setVisibility(View.GONE);
						}
					});
			container.addView(pagerView);// 添加页卡
			if(imageScaleType!=null){
				imgView.setScaleType(imageScaleType);
			}
			if(listener!=null) 
				listener.onInstantiateItem(pagerView, progressView, imgView, position);
			return pagerView;
		}
		@Override
		public int getCount() {
			return mImgUrls.length;// 返回页卡的数量
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return super.onInterceptTouchEvent(event);
	}
	OnInstantiateItemListener listener;
	public void setOnInstantiateItemListener(OnInstantiateItemListener listener){
		this.listener=listener;
	}
	public interface OnInstantiateItemListener{
		/**
		 * 用这个方法来监听对View初始化
		 * @param view 新实例化的对象
		 * @param position 对象的position
		 */
		public void onInstantiateItem(View view, CircleProgressBarView progressView, ImageView image, int position);
	}
}
