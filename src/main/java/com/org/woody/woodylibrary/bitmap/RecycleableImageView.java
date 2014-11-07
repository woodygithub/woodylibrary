package com.org.woody.woodylibrary.bitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**允许图片回收的ImageView，防止内存不足时回收了缓存图片而导致onDraw方法异常 */
public class RecycleableImageView extends ImageView{
	public RecycleableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public RecycleableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public RecycleableImageView(Context context) {
		super(context);
	}
	private String url;
	public void setUrl(String url){
		this.url=url;
	}
	public void setImageBitmap(Bitmap bitmap,String url){
		this.url=url;
		setImageBitmap(bitmap);
	}
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap bitmap=BitmapManager.getBitmapFromView(this);
		if(bitmap!=null && bitmap.isRecycled()){
			if(url==null) return;
			BitmapManager.bindView(this, null, null, null, url, new BitmapManager.BitmapLoadingListener() {
				public void onBitmapLoading(int progress) {
				}
				public void onBitmapLoadFinish(Bitmap bitmap, boolean isLoadSuccess) {
					if(isLoadSuccess) invalidate();
				}
			});
			url=null;
		}else super.onDraw(canvas);
	}
}