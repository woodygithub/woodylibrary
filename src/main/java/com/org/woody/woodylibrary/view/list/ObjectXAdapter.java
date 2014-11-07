package com.org.woody.woodylibrary.view.list;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.org.woody.woodylibrary.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linfaxin on 2014/8/7 007.
 * Email: linlinfaxin@163.com
 * 给ObjectXListView用的数据的载入适配器
 */
public interface ObjectXAdapter<T>{
    public View bindView(T t, int position, View convertView);
    public List<T> instanceNewList(int page) throws Exception;
    public void onLoadSuc(List<T> list);
	public long getItemId(int position);
    public void onLoadFinish(List<T> allList);
    public void onItemClick(T t, View view, int position, long id);


    /**网络多页数据的载入适配器 */
    public static abstract class PagesAdapter<T> implements ObjectXAdapter<T> {
    	ObjectXListView listView;
    	public void setListView(ObjectXListView listView){
    		this.listView = listView;
    	}
        
        public List<T> instanceNewList(int page) throws Exception{
            String result = getJsonData(page);
            try {
                return instanceNewList(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        /**从网络获取json数据(耗时操作) */
        public String getJsonData(int page){
            return HttpUtils.reqForGet(getUrl(page));
        }
        public abstract String getUrl(int page);
        public abstract List<T> instanceNewList(String json) throws Exception;

        public void onLoadSuc(List<T> list){
        }
        public void onLoadFinish(List<T> allList){
        }
        public void onItemClick(T t, View view, int position, long id){
        }
        //setAdapter之后自动载入
        protected boolean isAutoLoadAfterInit(){
            return true;
        }
    	public long getItemId(int position){
    		return 0;
    	}
    }
    /**网络单页数据的载入适配器 */
    public static abstract class SinglePageAdapter<T> extends PagesAdapter<T> {
        @Override
        public String getUrl(int page) {
            return getUrl();
        }
        public abstract String getUrl();
    }
    /**本地单页数据的载入适配器 */
    public static abstract class SingleLocalPageAdapter<T> extends SinglePageAdapter<T>{
        public String getUrl(){
            return null;
        }
        @Override
        public List<T> instanceNewList(int page) throws Exception {
            return instanceNewList();
        }
        public abstract List<T> instanceNewList() throws Exception;
        @Override
        public List<T> instanceNewList(String json) throws Exception {
            return null;
        }
    }
    
    /**模仿GridView样式的item */
    public static abstract class GridPagesAdapter<T> extends PagesAdapter<T>{
    	int column;
    	ArrayList<T[]> tArray = new ArrayList<T[]>();
    	
    	public GridPagesAdapter(int column){
    		this.column = column;
    	}
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1, 1);
		@Override
		public View bindView(T lineFirstT, int row, View rowContain) {
			LinearLayout linear = (LinearLayout) rowContain;
			if(linear == null){
				linear = new LinearLayout(listView.getContext());
				for(int i=0;i<column;i++){
					FrameLayout grid = new FrameLayout(listView.getContext());
//					grid.setBackgroundResource(R.drawable.common_btn_in_white);
					grid.setBackgroundDrawable(listView.getSelector().mutate().getConstantState().newDrawable());
					linear.addView(grid, params);
				}
			}
			
			T[] gridArray = tArray.get(row);
			for(int i=0; i<column; i++){
				FrameLayout grid = (FrameLayout) linear.getChildAt(i);
				if(gridArray[i]==null){
					grid.setVisibility(View.INVISIBLE);
					grid.setOnClickListener(null);
				}else{
					grid.setVisibility(View.VISIBLE);
					View convert = ( grid.getChildCount()==0 ? null: grid.getChildAt(1) );
					final int position = row * column + i;
					final T t =gridArray[i];
					grid.removeAllViews();
					grid.addView(bindGridView(t, position, convert));
					grid.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onItemClick(t, v, position, getItemId(position));
						}
					});
				}
			}
			
			return linear;
		}
		/**
		 * 获得View
		 * @param t 数据实体
		 * @param position 位置
		 * @param convertView 
		 * @return
		 */
		protected abstract View bindGridView(T t, int position, View convertView);

		@Override
        public void onItemClick(T t, View view, int position, long id){
            if(listView!=null && listView.onItemClickListener!=null)
            	listView.onItemClickListener.onItemClick(listView, view, position, id);
        }
		
		@SuppressWarnings("unchecked")
		@Override
		public List<T> instanceNewList(int page) throws Exception {
			List<T> list = super.instanceNewList(page);
			ArrayList<T> temp = new ArrayList<T>(column);
			ArrayList<T> firstItemList = new ArrayList<T>();
			
			for(T t : list){
				if(temp.size() == 0) firstItemList.add(t);//一行的首个，作为开头记录
				
				temp.add(t);
				
				if(temp.size() >= column){//一行满了换下一行
					tArray.add((T[]) temp.toArray( new Object[column] ));
					temp.clear();
				}
			}
			//最后一行可能没填满的
			if(temp.size()>0) tArray.add((T[]) temp.toArray( new Object[column] ));
			
			return firstItemList;
		}
		
    	
    }
}
