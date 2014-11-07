package com.org.woody.woodylibrary.view.pager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.org.woody.woodylibrary.R;

/**ViewPager的指示器，变现形式为点 */
public class PointIndicator extends RadioGroup {
	OnPageChangeListener onPageChangeListener;

	public PointIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PointIndicator(Context context) {
		super(context);
		init();
	}
	private void init(){
		setOrientation(HORIZONTAL);
	}
	public void setSize(int size){
		removeAllViews();
		if(size<=1) return;
		for(int i=0;i<size;i++){
			RadioButton rb = createRadioButton();
			rb.setId(i);
			addView(rb);
		}
		check(0);
	}
	private RadioButton createRadioButton(){
		RadioButton rb= new RadioButton(getContext());
		ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
		colorDrawable.setBounds(0, 0, 0, 0);
		rb.setButtonDrawable(colorDrawable);
		rb.setEnabled(false);
		int padding = (int) (2 * getContext().getResources().getDisplayMetrics().density);
		rb.setPadding(padding, 0, padding, 0);
		rb.setTextSize(0);
		rb.setBackgroundResource(android.R.color.transparent);
		rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.point_indicator_point);
		return rb;
	}

    /**
     * 绑定这个ViewPager，会覆盖清空原有的OnPageChangeListener
     */
    public void bindViewPager(ViewPager viewPager){
        bindViewPager(viewPager, null);
    }
    /**
     * 绑定这个ViewPager，覆盖OnPageChangeListener
     * @param viewPager 需要绑定的ViewPager
     * @param onPageChangeListener viewpager需要设置的OnPageListener
     */
	public void bindViewPager(ViewPager viewPager, OnPageChangeListener pageChangeListener){
		setSize(viewPager.getAdapter().getCount());
		this.onPageChangeListener = pageChangeListener;
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				check(position);
                if(onPageChangeListener!=null) onPageChangeListener.onPageSelected(position);
			}
			public void onPageScrolled(int arg0, float arg1, int arg2) {
                if(onPageChangeListener!=null) onPageChangeListener.onPageScrolled(arg0, arg1, arg2);
			}
			public void onPageScrollStateChanged(int arg0) {
                if(onPageChangeListener!=null) onPageChangeListener.onPageScrollStateChanged(arg0);
			}
		});
	}
	public OnPageChangeListener getOnPageChangeListener() {
		return onPageChangeListener;
	}

	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
		this.onPageChangeListener = onPageChangeListener;
	}
}
