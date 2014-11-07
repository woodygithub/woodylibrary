package com.org.woody.woodylibrary.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

/**从网络获取图片的ImageView */
public class NetImageView extends RecycleImageView {

	public NetImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NetImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NetImageView(Context context) {
		super(context);
	}
	
	public void loadUrl(String url){
		BitmapManager.bindView(this, url);
	}
	private int progressSize;//进度圈的大小
	private int progress = 3;//默认起始进度
	private int max = 100;
	private Paint paint = new Paint();
	private RectF oval = new RectF();
	private int color = Color.DKGRAY;
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	/**设置进度的颜色 */
	public void setColor(int color) {
		this.color = color;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
		if(progressSize == 0) return;
		try {
			invalidate();
		} catch (Exception e) {
			postInvalidate();
		}
	}
	/**
	 * 设置要画进度圈的边长
	 * @param size 进度圈的边长，==0 则不画
	 */
	public void setProgressSize(int size){
		this.progressSize = size;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Bitmap bitmap=BitmapManager.getBitmapFromView(this);
		if(bitmap != null) return;//已有图像显示
		
		//开始画进度圈
		if(progressSize == 0) return;
		
		int width=getWidth();
		int height=getHeight();
		if(progressSize == -1) progressSize = Math.min(width, height);
		else progressSize = Math.min(progressSize, Math.min(width, height));
		
		float radius=progressSize/2f *9/10;//圆的半径，*9/10为了给画笔宽度留出空间
		float strokeWidth=radius/5f;
		
		paint.setAntiAlias(true);// 设置是否抗锯齿
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);// 帮助消除锯齿
		paint.setColor(Color.GRAY);// 设置画笔灰色
		paint.setStrokeWidth(strokeWidth);// 设置画笔宽度
		paint.setStyle(Paint.Style.STROKE);// 设置中空的样式
		
		oval.set(width/2-radius, height/2-radius, width/2+radius, height/2+radius);// 设置圆弧所在的矩形
		canvas.drawArc(oval, -90,  360, false, paint);// 画圆弧，第二个参数为：起始角度，第三个为跨的角度，第四个为true的时候是实心，false的时候为空心
		paint.setColor(color);// 设置画笔为白色
		canvas.drawArc(oval, -90, ((float) progress / max) * 360, false, paint);// 画圆弧，第二个参数为：起始角度，第三个为跨的角度，第四个为true的时候是实心，false的时候为空心
		
		paint.reset();// 将画笔重置
		paint.setStrokeWidth(3);// 再次设置画笔的宽度
		paint.setTextSize(radius/2);// 设置文字的大小
		paint.setColor(color);// 设置画笔颜色
		String text;
		if (progress >= max) {
			text=max+"%";
		} else {
			text=progress + "%";
		}
		
		canvas.drawText(text, width/2-paint.measureText(text)/2, height/2+paint.getTextSize()/2, paint);
	}
}
