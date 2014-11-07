package com.org.woody.woodylibrary.view.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

/**可以把ExpandList包裹进来，头上会有当前显示的组的View，同时有被下个group推上去的赶脚 */
public class ExpandListHeadContain extends FrameLayout {

	public ExpandListHeadContain(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExpandListHeadContain(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExpandListHeadContain(Context context) {
		super(context);
	}
	public ExpandListHeadContain(Context context, ExpandableListView listView) {
		super(context);
		setListView(listView);
	}
	
	@Override
	public void addView(View child) {
		super.addView(child);
		if(child instanceof ExpandableListView) setListView(listView);
	}
	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
		if(child instanceof ExpandableListView) setListView(listView);
	}
	@Override
	public void addView(View child, int width, int height) {
		super.addView(child, width, height);
		if(child instanceof ExpandableListView) setListView(listView);
	}
	public void addView(View child, LayoutParams params) {
		super.addView(child, params);
		if(child instanceof ExpandableListView) setListView(listView);
	}
	public void addView(View child, int index, LayoutParams params) {
		super.addView(child, index, params);
		if(child instanceof ExpandableListView) setListView(listView);
	}
	

	ExpandableListView listView;
	View groupHead;
	LayoutParams params = new LayoutParams(-1, -2, Gravity.TOP);
	int showingGroupPositon = -1;
	public void setListView(ExpandableListView expandableListView){
		if(listView == expandableListView) return;
		this.listView = expandableListView;
		removeAllViews();
		super.addView(listView, -1, -1);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int groupPositon = getGroupPositionFromItemPosition(firstVisibleItem);
				if(groupPositon == -1){
					showingGroupPositon = -1;
					if(groupHead != null) groupHead.setVisibility(View.INVISIBLE);
					return;
				}else if(groupHead != null) groupHead.setVisibility(View.VISIBLE);
				
				//改变groupHead位置。思路：遍历所有显示的Item，判断是否是group然后得到Top移动groupHead
				if(groupHead != null){
					if(groupHead.getHeight() == 0) refreshGroupHead();//初始化后竟然没有高度？需要再次
					for(int i = 1;i<visibleItemCount;i++){
						try {
							Log.d("fax", "groupHead.getHeight():"+groupHead.getHeight()+",topMargin:"+params.topMargin);
							View itemView = listView.getChildAt(i);
							if(itemView.getTop()>=groupHead.getHeight()){//没有与Head就交叉部分，中断遍历
								if(params.topMargin != 0){
									params.topMargin = 0;
									groupHead.setLayoutParams(params);
								}
								break;
							}
							if(getGroupPositionFromItemPosition(firstVisibleItem+i)>groupPositon){
								//这个位置是下一个group，同时与head有交叉部分
								params.topMargin = itemView.getTop() - groupHead.getHeight();
								groupHead.setLayoutParams(params);
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
					
					
				
				//改变groupHead
				if(showingGroupPositon != groupPositon){
					showingGroupPositon = groupPositon;
					refreshGroupHead();
					
					if(groupHead != null){
						groupHead.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								boolean isCollapse = listView.isGroupExpanded(showingGroupPositon);
								listView.performItemClick(v, getItemPositionFromGroupPosition(showingGroupPositon), 0);
								if(isCollapse && showingGroupPositon<listView.getExpandableListAdapter().getGroupCount()-1) 
									listView.setSelectedGroup(showingGroupPositon);
								refreshGroupHead();
							}
						});
					}
					
				}
			}
		});
	}

	private int getItemPositionFromGroupPosition(int groupPosition){
		return listView.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
	}
	
	private int getGroupPositionFromItemPosition(int itemPosition){
		return ExpandableListView.getPackedPositionGroup(listView.getExpandableListPosition(itemPosition));
	}
	
	private void refreshGroupHead(){
		if(showingGroupPositon>=0 && listView.getExpandableListAdapter()!=null){
			groupHead = listView.getExpandableListAdapter().getGroupView(showingGroupPositon, 
					listView.isGroupExpanded(showingGroupPositon), groupHead, listView);
			if(groupHead!=null && indexOfChild(groupHead)==-1){
				addView(groupHead, params);
			}
		}
	}

}
