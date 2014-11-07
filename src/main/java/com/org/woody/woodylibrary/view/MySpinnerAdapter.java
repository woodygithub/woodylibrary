package com.org.woody.woodylibrary.view;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
	ArrayList<String> allLists;
	private static final int DefaultShowingTextColor=Color.BLACK;
	private int showingTextColor;
	public MySpinnerAdapter(ArrayList allLists) {
		this(allLists, DefaultShowingTextColor);
	}
	public MySpinnerAdapter(ArrayList allLists,int showingTextColor) {
		this.allLists=new ArrayList<String>();
		for(Object o: allLists){
			if(o!=null) this.allLists.add(o.toString());
			else this.allLists.add("");
		}
		this.showingTextColor = showingTextColor;
	}
	public MySpinnerAdapter(Object[] allLists) {
		this(allLists, DefaultShowingTextColor);
	}
	public MySpinnerAdapter(Object[] allLists,int showingTextColor) {
		this.allLists=new ArrayList<String>();
		for(Object o: allLists){
			if(o!=null) this.allLists.add(o.toString());
			else this.allLists.add("");
		}
		this.showingTextColor = showingTextColor;
	}

	@Override
	public int getCount() {
		return allLists.size();
	}

	@Override
	public Object getItem(int position) {
		return allLists.get(position);
	}
	public int getItemIndex(String object){
		return allLists.indexOf(object);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		TextView text = new TextView(parent.getContext());
		text.setTextColor(showingTextColor);
		text.setGravity(Gravity.CENTER);
//		text.setTextSize(20);
		String s=allLists.get(position);
		text.setText(s);
		return text;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView text = new TextView(parent.getContext());
		int padding=(int) (10*parent.getContext().getResources().getDisplayMetrics().density);
		text.setPadding(padding, padding, padding, padding);
//		text.setTextColor(Color.WHITE);
		String s=allLists.get(position);
		text.setText(s);
		
		return text;
//		View view=View.inflate(parent.getContext(), R.layout.spinner_item, null);
//		TextView text = (TextView) view.findViewById(android.R.id.text1);
//		text.setText(allLists.get(position).toString());
//		return view;
	}

}
