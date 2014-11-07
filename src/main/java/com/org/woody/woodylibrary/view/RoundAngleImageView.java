package com.org.woody.woodylibrary.view;

import com.org.woody.woodylibrary.R;
import com.org.woody.woodylibrary.bitmap.RecycleableImageView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

/**圆角矩形 */
public class RoundAngleImageView extends RecycleableImageView {

	private Paint paint;
	private final int defaultRoundWidth=10;
	private final int defaultRoundHeight=10;
	private int roundWidth = 10;
	private int roundHeight = 10;
	private Paint paint2;
	private Paint borderPaint;

	public RoundAngleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public RoundAngleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RoundAngleImageView(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs) {
		float density = context.getResources().getDisplayMetrics().density;
		if(attrs != null) {   
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundAngleImageView); 
			roundWidth= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundWidth, (int) (defaultRoundWidth*density));
			roundHeight= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundHeight, (int) (defaultRoundHeight*density));
			
			int borderWidth= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_borderWidth, -1);
			int borderColor= a.getColor(R.styleable.RoundAngleImageView_borderColor, Color.BLACK);
			if(borderWidth>0 ){
				borderPaint=new Paint();
				borderPaint.setStyle(Style.STROKE);
				borderPaint.setStrokeWidth(borderWidth);
				borderPaint.setColor(borderColor);
				borderPaint.setAntiAlias(true);
			}
		}else {
			roundWidth = (int) (roundWidth*density);
			roundHeight = (int) (roundHeight*density);
		} 
		
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		
		paint2 = new Paint();
		paint2.setXfermode(null);
		
	}
	@Override
	public void draw(Canvas canvas) {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		Canvas canvas2 = new Canvas(bitmap);
		super.draw(canvas2);
		drawLiftUp(canvas2);
		drawRightUp(canvas2);
		drawLiftDown(canvas2);
		drawRightDown(canvas2);
		canvas.drawBitmap(bitmap, 0, 0, paint2);
		bitmap.recycle();
	}
	
	private void drawLiftUp(Canvas canvas) {
		Path path = new Path();
		path.moveTo(0, roundHeight);
		path.lineTo(0, 0);
		path.lineTo(roundWidth, 0);
		RectF arc = new RectF(0, 0, roundWidth * 2, roundHeight * 2);
		float startAngle=-90;
		float sweepAngle=-90;
		path.arcTo(arc, startAngle, sweepAngle);
		path.close();
		canvas.drawPath(path, paint);
		if(borderPaint!=null){
			float halfBorder=borderPaint.getStrokeWidth()/2f;
			arc.set(arc.left+halfBorder, arc.top+halfBorder, arc.right, arc.bottom);
			canvas.drawArc(arc, startAngle, sweepAngle, false, borderPaint);
		}
	}
	
	private void drawLiftDown(Canvas canvas) {
		Path path = new Path();
		path.moveTo(0, getHeight()-roundHeight);
		path.lineTo(0, getHeight());
		path.lineTo(roundWidth, getHeight());
		RectF arc = new RectF(0, getHeight() - roundHeight * 2, 0 + roundWidth * 2, getHeight());
		float startAngle=90;
		float sweepAngle=90;
		path.arcTo(arc, startAngle, sweepAngle);
		path.close();
		canvas.drawPath(path, paint);
		if(borderPaint!=null){
			float halfBorder=borderPaint.getStrokeWidth()/2f;
			arc.set(arc.left+halfBorder, arc.top, arc.right, arc.bottom-halfBorder);
			canvas.drawArc(arc, startAngle, sweepAngle, false, borderPaint);
		}
	}
	
	private void drawRightDown(Canvas canvas) {
		Path path = new Path();
		path.moveTo(getWidth()-roundWidth, getHeight());
		path.lineTo(getWidth(), getHeight());
		path.lineTo(getWidth(), getHeight()-roundHeight);
		RectF arc = new RectF(getWidth() - roundWidth * 2, getHeight() - roundHeight * 2, getWidth(), getHeight());
		float startAngle=0;
		float sweepAngle=90;
		path.arcTo(arc, startAngle, sweepAngle);
		path.close();
		canvas.drawPath(path, paint);
		if(borderPaint!=null){
			float halfBorder=borderPaint.getStrokeWidth()/2f;
			arc.set(arc.left, arc.top, arc.right-halfBorder, arc.bottom-halfBorder);
			canvas.drawArc(arc, startAngle, sweepAngle, false, borderPaint);
		}
	}
	
	private void drawRightUp(Canvas canvas) {
		Path path = new Path();
		path.moveTo(getWidth(), roundHeight);
		path.lineTo(getWidth(), 0);
		path.lineTo(getWidth()-roundWidth, 0);
		RectF arc = new RectF(getWidth() - roundWidth * 2, 0, getWidth(), 0 + roundHeight * 2);
		float startAngle=-90;
		float sweepAngle=90;
		path.arcTo(arc, startAngle, sweepAngle);
		path.close();
		canvas.drawPath(path, paint);
		if(borderPaint!=null){
			float halfBorder=borderPaint.getStrokeWidth()/2f;
			arc.set(arc.left, arc.top+halfBorder, arc.right-halfBorder, arc.bottom);
			canvas.drawArc(arc, startAngle, sweepAngle, false, borderPaint);
		}
	}

}
