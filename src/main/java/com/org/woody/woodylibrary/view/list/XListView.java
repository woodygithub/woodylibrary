/**
 * @file XListView.java
 * @package me.maxwin.view
 * @create Mar 18, 2012 6:28:41 PM
 * @author Maxwin
 * @description An ListView support (a) Pull down to refresh, (b) Pull up to load more.
 * 		Implement IXListViewListener, and see stopRefresh() / stopLoadMore().
 * 		修改增加了部分方法。
 */

package com.org.woody.woodylibrary.view.list;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import com.org.woody.woodylibrary.R;


public class XListView extends ListView implements OnScrollListener {
	
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	/**-- 下拉刷新 
	*下拉刷新的视图
	*/
	private XListViewHeader mHeaderView;
	/** 刷新视图内如，用于计算高度，不启用下拉刷新时直接隐藏 */
	private ViewGroup mHeaderViewContent;
	/**保存下拉刷新视图的高度*/
	private int mHeaderViewHeight;
	/**是否启用下拉刷新*/
	private boolean mEnablePullRefresh = true;
	/**当前是否处于下拉刷新状态*/
	private boolean mPullRefreshing = false;
	/**下拉刷新回调*/
	private IXListViewListener mListViewListener;

	/**-- 上拉载入更多*/
	private XListViewFooter mFooterView;
	private ViewGroup mFooterViewContent;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;

	/**总item，用于判断是否触底*/
	private int mTotalItemCount;
	/**获取头部View*/
	public XListViewHeader getmHeaderView() {
		return mHeaderView;
	}
	/**获取底部View*/
	public XListViewFooter getmFooterView() {
		return mFooterView;
	}
	/**启动mScroller调整位置时，需判断是顶部或底部*/
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	/**回滚动画时间*/
	private final static int SCROLL_DURATION = 400; // ms
	/**上拉50dp后提示*/
	private static final int PULL_LOAD_MORE_DELTA_DEFAULT = 50;
	/**上拉50dp后提示*/
	private static int PULL_LOAD_MORE_DELTA; // 
	/**实现下拉位置滞后效果*/
    private static final float DEFUALT_OFFSET_RADIO = 1.8f;	
    /**实现下拉位置滞后效果*/
	private float offsetRadio = DEFUALT_OFFSET_RADIO;
    /**当滑到底端时，自动加载更多*/
	private boolean isAutoLoadScrollEnd = false;
    public void setAutoLoadScrollEnd(boolean isAutoLoadScrollEnd) {
        this.isAutoLoadScrollEnd = isAutoLoadScrollEnd;
    }
	/**
	 * @param context
	 */
	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		setCacheColorHint(Color.TRANSPARENT);
		PULL_LOAD_MORE_DELTA = (int) (PULL_LOAD_MORE_DELTA_DEFAULT * context.getResources().getDisplayMetrics().density);
		
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// 将滚动回调绑定到this上，通过mWBScrollListner做proxy
		super.setOnScrollListener(this);

		if(isInEditMode()) return;
		// 初始化下拉刷新view
		mHeaderView = new XListViewHeader(context);
		addHeaderView(mHeaderView);
		
		mHeaderViewContent = (ViewGroup) mHeaderView.findViewById(R.id.xlistview_header_content);
		mHeaderViewContent.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mHeaderViewHeight = mHeaderViewContent.getMeasuredHeight();

		// 初始化上拉载入更多
		mFooterView = new XListViewFooter(context);
		mFooterViewContent = (ViewGroup) mFooterView
				.findViewById(R.id.xlistview_footer_content);

//		// 初始化header height
//		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
//				new OnGlobalLayoutListener() {
//					@SuppressWarnings("deprecation")
//					@Override
//					public void onGlobalLayout() {
//						mHeaderViewHeight = mHeaderViewContent .getHeight();
//						mHeaderView.getViewTreeObserver() .removeGlobalOnLayoutListener(this);
//					}
//				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		addFooterView(mFooterView);
		super.setAdapter(adapter);
	}

    /**是否开启过度滑动的方式刷新或者载入下一页，关闭后同时关闭过度滑动的行为 */
    public void setOverScrollLoadEnable(boolean enable){
        if(enable) offsetRadio=DEFUALT_OFFSET_RADIO;
        else offsetRadio=Float.MAX_VALUE;
    }
	/**
	 * 设置启用下拉刷新，禁用时下拉模块不可见、有过度滑动和回滚行为
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		if(isInEditMode()) return;
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}
	/**单独设置无视图的下拉刷新*/
	public void setPullRefreshWhitoutViewEnable(boolean enable) {
		mEnablePullRefresh = enable;
	}
//	public void removeRefreshHeaderView(){
//		setPullRefreshEnable(false);
//		if(mHeaderView==null) return;
//		removeHeaderView(mHeaderView);
//		mHeaderView=null;
//	}
    /**载入更多 是否可用 */
    public boolean isPullLoadEnable(){
        return mEnablePullLoad;
    }
	/** 
	 * 设置启用上拉载入更多，禁用时载入模块不可见、有过度滑动和回滚行为
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		if(isInEditMode()) return;
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterViewContent.getLayoutParams().height=0;
			mFooterViewContent.setVisibility(View.INVISIBLE);
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterViewContent.getLayoutParams().height=-2;
			mFooterViewContent.setVisibility(View.VISIBLE);
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mFooterView.getState()==XListViewFooter.STATE_NORMAL) {
						startLoadMore();
					}
				}
			});
		}
	}
	
	/**单独设置无视图的上拉载入*/
	public void setPullLoadWhitoutViewEnable(boolean enable) {
		mEnablePullLoad = enable;
	}

	/**
	 * 停止下拉刷新，回推下拉控件
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * 停止上拉载入更多,并设置为正常状态
	 */
	public void stopLoadMore() {
//		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
//		}
	}
	/**
	 * 停止上拉载入更多,并显示加载完毕
	 */
	public void showNoMore() {
//		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NOMORE);
//		}
	}

	/**
	 * 触发滚动回调，统一入口，会检查mScrollListener合法性
	 */
	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnWBScrollListener) {
			OnWBScrollListener l = (OnWBScrollListener) mScrollListener;
			l.onWBScrolling(this);
		}
	}

	private void updateHeaderHeight(int delta) {
		if(mHeaderView==null || delta==0) return;
		mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
			setSelection(0);
		}
	}

	/**
	 * 重置刷新视图高度
	 */
	private void resetHeaderHeight() {
		if(mHeaderView==null) return;
		int height = mHeaderView.getVisiableHeight();
		if (height == 0)
			return;
		// 正在刷新，且刷新视图仅显示部分，不做处理
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0;
		// 正在刷新，回退到仅显示刷新视图
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);

		invalidate();
	}

	private void updateFooterHeight(int delta) {
        if(delta==0) return;
		int height = mFooterView.getBottomMargin() + delta;
		if (mFooterView.getState()!=XListViewFooter.STATE_NOMORE) {
			if (height > PULL_LOAD_MORE_DELTA) {
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
			mFooterView.setBottomMargin(height);
			setSelection(mTotalItemCount - 1);
		}
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}
	
	public void startLoadMore() {
        if(mPullLoading || mPullRefreshing) return;//如果正在载入，中断
		if (mFooterView.getState()!=XListViewFooter.STATE_NOMORE) {
			mPullLoading = true;
			mFooterView.setState(XListViewFooter.STATE_LOADING);
			if (mListViewListener != null) {
				mListViewListener.onLoadMore();
			}
		}
	}
    /**仅更改显示为“正在载入...”的状态 */
	public void setLoadMoreState(){
		mFooterView.setState(XListViewFooter.STATE_LOADING);
	}
	public void startRefresh(){
		if(mPullLoading || mPullRefreshing) return;//如果正在载入，中断
		lastAutoLoadTotalCount=-1;
		mPullRefreshing = true;
		if(mHeaderView!=null) mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
		if (mListViewListener != null) {
			mListViewListener.onRefresh();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (mHeaderView!=null && getFirstVisiblePosition() == 0 && !mPullRefreshing
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// 第一行，且下拉组件已显示或者正在下拉
				updateHeaderHeight((int) (deltaY / offsetRadio));
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// 最后一行，上来组件已显示或者正在上来
				if(getHeight()==mFooterView.getBottom())
					updateFooterHeight((int) (-deltaY / offsetRadio));
			}
			break;
		default:
			mLastY = -1;
			if (getFirstVisiblePosition() == 0) {
				// 触发刷新
				if (mHeaderView!=null && mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					startRefresh();
				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// 触发载入更多
				if (mEnablePullLoad && mFooterView.getState() == XListViewFooter.STATE_READY) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			// Qzlog.d(TAG, "computeScroll:" + mScroller.getCurrY());
			if (mScrollBack == SCROLLBACK_HEADER) {
				if(mHeaderView!=null) mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}
	/**记录上次自动加载后的数目，避免加载失败死循环重复 */
	private int lastAutoLoadTotalCount=-1;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
		if(isAutoLoadScrollEnd && firstVisibleItem+visibleItemCount>=totalItemCount){//滑倒最底了
			if (mFooterView!=null && mFooterView.getState()==XListViewFooter.STATE_NORMAL 
					&& lastAutoLoadTotalCount!=totalItemCount){//状态可点击(避免载入失败后反复自动载入)
				lastAutoLoadTotalCount=totalItemCount;
				startLoadMore();
			}
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

    private long touchDownTime;
    private float touchDownX;
    private float touchDownY;
    Boolean canScrollY;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!isItemHScrollAble) return super.onInterceptTouchEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
        	touchDownTime = System.currentTimeMillis();
        	touchDownX=ev.getX();
        	touchDownY=ev.getY();
        	canScrollY=null;
        }
        if(System.currentTimeMillis()-touchDownTime<50) return super.onInterceptTouchEvent(ev);
        if(canScrollY!=null) return canScrollY;
        float distanceX = ev.getX()-touchDownX;
        float distanceY = ev.getY()-touchDownY;
        canScrollY = (Math.abs(distanceY) >= Math.abs(distanceX));
		
        return super.onInterceptTouchEvent(ev) && canScrollY ;
	}
	private boolean isItemHScrollAble;
	/**设置是否有item可以左右滑动 */
	public void setItemHScroll(boolean enable){
		if(enable) isItemHScrollAble=true;
		else isItemHScrollAble=false;
	}

	// 滚动接口
	public interface OnWBScrollListener extends OnScrollListener {
		public void onWBScrolling(View view);
	}

	// 下拉刷新接口
	public interface IXListViewListener {
		public void onRefresh();

		public void onLoadMore();
	}
}
