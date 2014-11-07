package com.org.woody.woodylibrary.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.org.woody.woodylibrary.R;

@SuppressLint("NewApi")
public class CardTextView extends LinearLayout {
	View view;
	TextView title;
	TextView subView;
	public CardTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CardTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CardTextView(Context context) {
		super(context);
		init();
	}
	private void init(){
		view = View.inflate(getContext(), R.layout.cardbar_text, null);
		title = (TextView) view.findViewById(R.id.card_title);
		subView = (TextView) view.findViewById(R.id.subtext);
		View titleLayout = view.findViewById(R.id.card_title_layout);
		titleLayout.setBackground(getBackground());
		setBackground(null);
		titleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (subView.getVisibility() == VISIBLE) {
					subView.setVisibility(GONE);
				}else if (subView.getVisibility() != VISIBLE) {
					subView.setVisibility(VISIBLE);
				}
			}
		});
		addView(view);
	}
	
	public CardTextView setTitleCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom){
		title.setCompoundDrawables(left, top, right, bottom);
		return this;
	}
	public CardTextView setTitleCompoundDrawablePadding(int pad){
		title.setCompoundDrawablePadding(pad);
		return this;
	}
	
	public CardTextView setTitle(CharSequence text){
		title.setText(text);
		return this;
	}
	public CardTextView setSubText(CharSequence text){
		subView.setText(text);
		return this;
	}
	public TextView getTitle(){
		return title;
	}
	public TextView getSubText(){
		return subView;
	}
	public CardTextView closeView(){
		subView.setVisibility(GONE);
		return this;
	}
	public CardTextView openView(){
		subView.setVisibility(VISIBLE);
		return this;
	}
}