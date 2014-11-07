package com.org.woody.woodylibrary.view.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Woody on 2014/10/31.
 */
public abstract class PullToRefreshAdapter<T> extends BaseAdapter {
    List<T> itemList;
    int itemLayoutId;
    Context context;

    public PullToRefreshAdapter(Context context, List<T> items, int itemLayoutId) {
        this.context = context;
        this.itemLayoutId = itemLayoutId;
        this.itemList = items;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public T getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        if (convertView ==null){
            view = View.inflate(context, itemLayoutId,parent);
        }else{
            view = convertView;
        }
        return getView(position,view);
    }

    protected abstract View getView(int position, View view);

    public List<T> getItemList() {
        return itemList;
    }
}
