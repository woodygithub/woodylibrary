package com.org.woody.woodylibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**可放大缩小拖动的图像(可以设置过度滑动，即滑出界)*/
public class TouchImageView extends ImageView {  
	  
    Matrix matrix;  
  
    // We can be in one of these 3 states   
    static final int NONE = 0;  
    static final int DRAG = 1;  
    static final int ZOOM = 2;  
    int mode = NONE;  
  
    // Remember some things for zooming   
    PointF last = new PointF();  
    PointF start = new PointF();  
    float minScale = .8f;  
    float maxScale = 3f;
    float[] m;  
  
  
    int viewWidth, viewHeight;  
    static final int CLICK = 3;  
    float saveScale = 1f;  
    protected float origWidth, origHeight;  
    int oldMeasuredWidth, oldMeasuredHeight; 
    //是否能移动过界(最多过一半)
    private boolean canMoveOverBoder=false;

    ScaleGestureDetector mScaleDetector;  
    GestureDetector doubleClickDetector;  
  
    Context context;  
  
    public TouchImageView(Context context) {  
        super(context);  
        sharedConstructing(context);  
    }  
  
    public TouchImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        sharedConstructing(context);  
    }  
    public void setCanMoveOverBoder(boolean canMoveOverBoder){
    	this.canMoveOverBoder=canMoveOverBoder;
    }
    private void sharedConstructing(Context context) {  
        super.setClickable(true);  
        this.context = context;  
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        doubleClickDetector=new GestureDetector(context, new SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if(saveScale==maxScale) setImageScale(1);
				else setImageScale(saveScale+(maxScale-1)/2, e.getX(), e.getY());
				return true;
			}
        });
        matrix = new Matrix();  
        m = new float[9];  
        setImageMatrix(matrix);  
        setScaleType(ScaleType.MATRIX);  
    }  
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	mScaleDetector.onTouchEvent(event);
    	doubleClickDetector.onTouchEvent(event);
        PointF curr = new PointF(event.getX(), event.getY());  

        switch (event.getAction()) {  
            case MotionEvent.ACTION_DOWN:  
                last.set(curr);  
                start.set(last);  
                mode = DRAG;  
                break;  
                  
            case MotionEvent.ACTION_MOVE:  
                if (mode == DRAG) {  
                    float deltaX = curr.x - last.x;  
                    float deltaY = curr.y - last.y;  
                    float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);  
                    float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);  
                    matrix.postTranslate(fixTransX, fixTransY);  
                    fixTrans();  
                    last.set(curr.x, curr.y);
                }
                break;  

            case MotionEvent.ACTION_UP:  
                mode = NONE;  
                int xDiff = (int) Math.abs(curr.x - start.x);  
                int yDiff = (int) Math.abs(curr.y - start.y);  
                if (xDiff < CLICK && yDiff < CLICK)  
                    performClick();  
                break;  

            case MotionEvent.ACTION_POINTER_UP:  
                mode = NONE;  
                break;  
        }  
          
        setImageMatrix(matrix);  
        invalidate();
        return true; // indicate event was handled   
	}

	public void setMaxZoom(float x) {  
        maxScale = x;  
    }  
  
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {  
        @Override  
        public boolean onScaleBegin(ScaleGestureDetector detector) {  
            mode = ZOOM;  
            return true;  
        }  
  
        @Override  
        public boolean onScale(ScaleGestureDetector detector) {  
            float mScaleFactor = detector.getScaleFactor();
            float mScale= saveScale * mScaleFactor;
            setImageScale(mScale, detector.getFocusX(), detector.getFocusY());
//            float origScale = saveScale;  
//            saveScale *= mScaleFactor;  
//            if (saveScale > maxScale) {  
//                saveScale = maxScale;  
//                mScaleFactor = maxScale / origScale;  
//            } else if (saveScale < minScale) {  
//                saveScale = minScale;  
//                mScaleFactor = minScale / origScale;  
//            }  
//  
//            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)  
//                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);  
//            else  
//                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());  
//  
//            fixTrans();  
            return true;  
        }  
    }  
    boolean fixTrans() {  
        matrix.getValues(m);  
        float transX = m[Matrix.MTRANS_X];  
        float transY = m[Matrix.MTRANS_Y];  
          
        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);  
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);  
  
        if (fixTransX != 0 || fixTransY != 0){
        	matrix.postTranslate(fixTransX, fixTransY);
        	return true;
        }
        return false;
    }  
  
    float getFixTrans(float trans, float viewSize, float contentSize) {  
        float minTrans, maxTrans;  
        if (contentSize <= viewSize) {  
            minTrans = 0;  
            maxTrans = viewSize - contentSize;  
        } else {  
            minTrans = viewSize - contentSize;  
            maxTrans = 0;  
        }   
        if(canMoveOverBoder){
        	minTrans -= viewSize/2; 
            maxTrans += viewSize/2;    
        }
  
        if (trans < minTrans)  
            return -trans + minTrans;  
        if (trans > maxTrans)  
            return -trans + maxTrans;  
        return 0;  
    }  
      
    float getFixDragTrans(float delta, float viewSize, float contentSize) {  
        if (!canMoveOverBoder && contentSize <= viewSize) {  
            return 0;
        }  
        return delta;  
    }  
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);  
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);  
          
        //   
        // Rescales image on rotation   
        //   
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight  
                || viewWidth == 0 || viewHeight == 0)  
            return;  
        oldMeasuredHeight = viewHeight;  
        oldMeasuredWidth = viewWidth;  
  
        if (saveScale == 1) {  
            //Fit to screen.   
            float scale;  
  
            Drawable drawable = getDrawable();  
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)  
                return;  
            int bmWidth = drawable.getIntrinsicWidth();  
            int bmHeight = drawable.getIntrinsicHeight();  
              
  
            float scaleX = (float) viewWidth / (float) bmWidth;  
            float scaleY = (float) viewHeight / (float) bmHeight;  
            scale = Math.min(scaleX, scaleY);  
            matrix.setScale(scale, scale);  
  
            // Center the image   
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);  
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);  
            redundantYSpace /= (float) 2;  
            redundantXSpace /= (float) 2;  
  
            matrix.postTranslate(redundantXSpace, redundantYSpace);  
  
            origWidth = viewWidth - 2 * redundantXSpace;  
            origHeight = viewHeight - 2 * redundantYSpace;  
            setImageMatrix(matrix);  
        }  
        fixTrans();  
    }
	@Override
	public void setImageBitmap(Bitmap bm) {
    	setImageScale(1);
		super.setImageBitmap(bm);
	}
	@Override
	public void setImageDrawable(Drawable drawable) {
    	setImageScale(1);
		super.setImageDrawable(drawable);
	}
	@Override
	public void setImageResource(int resId) {
    	setImageScale(1);
		super.setImageResource(resId);
	}
	@Override
	public void setImageState(int[] state, boolean merge) {
    	setImageScale(1);
		super.setImageState(state, merge);
	}
	@Override
	public void setImageURI(Uri uri) {
    	setImageScale(1);
		super.setImageURI(uri);
	}

	public void setImageScale(float scale){
		setImageScale(scale, viewWidth / 2, viewHeight / 2);
	}
	public void setImageScale(float scale, float focusX, float focusY){
        float origScale = saveScale;  
    	saveScale=scale;
        if (saveScale > maxScale) {  
            saveScale = maxScale;  
        } else if (saveScale < minScale) {  
            saveScale = minScale;  
        }  
        float mScaleFactor = saveScale / origScale;  

        if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)  
            matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);  
        else  
            matrix.postScale(mScaleFactor, mScaleFactor, focusX, focusY);  

        fixTrans();  
    }
}  