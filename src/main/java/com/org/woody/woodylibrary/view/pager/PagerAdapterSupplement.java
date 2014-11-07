package com.org.woody.woodylibrary.view.pager;

import android.support.v4.view.PagerAdapter;
import android.view.View;

public abstract class PagerAdapterSupplement extends PagerAdapter implements
		AdapterSupplementInterface {

	@Override
	public float getPageHeight(int position){
        return 1.f;
	}

	@Override
	public abstract int getCount();

	@Override
	public abstract boolean isViewFromObject(View arg0, Object arg1);

}
